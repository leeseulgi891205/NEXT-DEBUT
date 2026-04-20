from __future__ import annotations

import json
import math
import re
from dataclasses import dataclass
from pathlib import Path
import time
from typing import Dict, List

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline

TOKEN_RE = re.compile(r"[가-힣a-zA-Z0-9]+")
DEFAULT_DATA_PATH = Path(__file__).resolve().parent / "data" / "chat_choice_training.jsonl"


@dataclass(frozen=True)
class ChoiceInput:
    key: str
    text: str
    stat_target: str


class ChoicePredictModel:
    """
    학습 데이터(jsonl)가 충분하면 sklearn 분류기를 사용하고,
    부족하면 휴리스틱으로 동작한다.
    """

    def __init__(
        self,
        data_path: Path = DEFAULT_DATA_PATH,
        min_train_samples: int = 30,
        auto_retrain_interval_sec: int = 8,
        auto_retrain_new_samples: int = 1,
        ambiguity_gap_threshold: float = 0.03,
    ) -> None:
        self.data_path = data_path
        self.min_train_samples = min_train_samples
        self.auto_retrain_interval_sec = max(3, auto_retrain_interval_sec)
        # 1이면 jsonl이 1줄만 늘어도(로그 누적) mtime·줄 수 조건 만족 시 재학습
        self.auto_retrain_new_samples = max(1, auto_retrain_new_samples)
        self.ambiguity_gap_threshold = max(0.0, float(ambiguity_gap_threshold))
        self.pipeline: Pipeline | None = None
        self.sample_count: int = 0
        self.last_retrain_ts: float = 0.0
        self.last_data_mtime: float = 0.0
        self.keyword_bias = {
            "A": {"보컬", "발성", "음정", "노래", "호흡", "vocal"},
            "B": {"댄스", "안무", "퍼포", "동선", "춤", "dance"},
            "C": {"팀워크", "팀웍", "호흡", "합", "조율", "협업", "team"},
            "D": {"회복", "휴식", "멘탈", "컨디션", "안정", "정비", "rest"},
            "SPECIAL": {"무리", "승부", "도전", "올인", "리스크", "한방", "special"},
            "NONE": {"모르겠", "아무거나", "무응답", "패스"},
        }
        self.reload()

    def reload(self) -> None:
        samples = _load_samples(self.data_path)
        self.sample_count = len(samples)
        self.last_retrain_ts = time.time()
        self.last_data_mtime = self._current_data_mtime()
        self.pipeline = None
        if self.sample_count < self.min_train_samples:
            return
        x_train: List[str] = []
        y_train: List[str] = []
        for sample in samples:
            label = str(sample.get("resolvedKey", "")).strip().upper()
            if not label:
                continue
            user_text = str(sample.get("userText", ""))
            phase = str(sample.get("phase", ""))
            scene_id = sample.get("sceneId")
            choices = sample.get("choices", [])
            feature = _build_feature_text(user_text=user_text, choices=choices, phase=phase, scene_id=scene_id)
            x_train.append(feature)
            y_train.append(label)
        if len(x_train) < self.min_train_samples:
            return
        self.pipeline = Pipeline(
            [
                ("tfidf", TfidfVectorizer(ngram_range=(1, 2), min_df=1)),
                ("clf", LogisticRegression(max_iter=700, class_weight="balanced")),
            ]
        )
        self.pipeline.fit(x_train, y_train)

    def predict_choice(self, user_text: str, choices: List[ChoiceInput], phase: str = "", scene_id: int | None = None) -> Dict[str, object]:
        self._auto_reload_if_needed()
        if not choices:
            return {"predicted_key": None, "confidence": 0.0, "score_by_key": {}}

        heuristic_scores = self._heuristic_scores(user_text, choices)
        model_scores = self._model_scores(user_text, choices, phase, scene_id)
        blended = _blend_scores(heuristic_scores, model_scores, alpha=0.7 if model_scores else 0.0)
        score_by_key = _softmax(blended)
        predicted_key = max(score_by_key, key=score_by_key.get)
        confidence = score_by_key.get(predicted_key, 0.0)
        # top1-top2 격차가 너무 작으면 모호한 입력으로 간주해 RULE fallback 유도
        # (Java 단에서 NONE이 유효 선택지가 아니면 RULE로 재해석됨)
        if _top_two_gap(score_by_key) < self.ambiguity_gap_threshold:
            predicted_key = "NONE"
        return {
            "predicted_key": predicted_key,
            "confidence": confidence,
            "score_by_key": score_by_key,
            "model_sample_count": self.sample_count,
            "model_enabled": self.pipeline is not None,
        }

    def _auto_reload_if_needed(self) -> None:
        now = time.time()
        if (now - self.last_retrain_ts) < self.auto_retrain_interval_sec:
            return
        current_mtime = self._current_data_mtime()
        if current_mtime <= 0 or current_mtime <= self.last_data_mtime:
            return
        latest_samples = _count_lines(self.data_path)
        if latest_samples >= (self.sample_count + self.auto_retrain_new_samples):
            self.reload()
            return
        # 파일 변경 시각은 갱신해 중복 체크를 줄임
        self.last_data_mtime = current_mtime
        self.last_retrain_ts = now

    def _current_data_mtime(self) -> float:
        if not self.data_path.exists():
            return 0.0
        try:
            return self.data_path.stat().st_mtime
        except OSError:
            return 0.0

    def _heuristic_scores(self, user_text: str, choices: List[ChoiceInput]) -> Dict[str, float]:
        user_tokens = _tokenize(user_text)
        scores: Dict[str, float] = {}
        for choice in choices:
            choice_tokens = _tokenize(f"{choice.text} {choice.stat_target}")
            overlap = _jaccard(user_tokens, choice_tokens)
            bias = _keyword_bias_score(user_tokens, self.keyword_bias.get(choice.key, set()))
            stat_bias = _keyword_bias_score(user_tokens, _tokenize(choice.stat_target))
            score = 0.55 * overlap + 0.35 * bias + 0.10 * stat_bias
            scores[choice.key] = max(0.0, score)
        return scores

    def _model_scores(self, user_text: str, choices: List[ChoiceInput], phase: str, scene_id: int | None) -> Dict[str, float]:
        if self.pipeline is None:
            return {}
        available_keys = [c.key for c in choices]
        feature = _build_feature_text(
            user_text=user_text,
            choices=[{"key": c.key, "text": c.text, "statTarget": c.stat_target} for c in choices],
            phase=phase,
            scene_id=scene_id,
        )
        proba = self.pipeline.predict_proba([feature])[0]
        classes = [str(c).upper() for c in self.pipeline.classes_]
        by_class = {k: float(v) for k, v in zip(classes, proba)}
        return {key: by_class.get(key, 0.0) for key in available_keys}


def train_dummy_model() -> ChoicePredictModel:
    return ChoicePredictModel()


def load_model() -> ChoicePredictModel:
    return train_dummy_model()


def _load_samples(path: Path) -> List[dict]:
    if not path.exists():
        return []
    samples: List[dict] = []
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            raw = line.strip()
            if not raw:
                continue
            try:
                row = json.loads(raw)
            except json.JSONDecodeError:
                continue
            if isinstance(row, dict):
                samples.append(row)
    return samples


def _count_lines(path: Path) -> int:
    if not path.exists():
        return 0
    count = 0
    with path.open("r", encoding="utf-8") as f:
        for line in f:
            if line.strip():
                count += 1
    return count


def _build_feature_text(user_text: str, choices: List[dict], phase: str, scene_id: int | None) -> str:
    parts: List[str] = [f"user:{user_text}", f"phase:{phase}", f"scene:{scene_id if scene_id is not None else ''}"]
    for choice in choices:
        key = str(choice.get("key", "")).upper()
        text = str(choice.get("text", ""))
        stat = str(choice.get("statTarget", ""))
        parts.append(f"[{key}] {text} stat:{stat}")
    return " ".join(parts)


def _tokenize(text: str) -> set[str]:
    if not text:
        return set()
    return {t.lower() for t in TOKEN_RE.findall(text)}


def _jaccard(a: set[str], b: set[str]) -> float:
    if not a or not b:
        return 0.0
    union = a | b
    if not union:
        return 0.0
    return len(a & b) / len(union)


def _keyword_bias_score(tokens: set[str], keywords: set[str]) -> float:
    if not tokens or not keywords:
        return 0.0
    hits = len(tokens & {k.lower() for k in keywords})
    return min(1.0, hits / 2.0)


def _blend_scores(heuristic: Dict[str, float], model: Dict[str, float], alpha: float) -> Dict[str, float]:
    keys = set(heuristic.keys()) | set(model.keys())
    if not keys:
        return {}
    alpha = max(0.0, min(1.0, alpha))
    out: Dict[str, float] = {}
    for key in keys:
        out[key] = (1.0 - alpha) * heuristic.get(key, 0.0) + alpha * model.get(key, 0.0)
    return out


def _softmax(scores: Dict[str, float]) -> Dict[str, float]:
    if not scores:
        return {}
    max_score = max(scores.values())
    exps = {k: math.exp(v - max_score) for k, v in scores.items()}
    total = sum(exps.values())
    if total <= 0:
        n = len(scores)
        return {k: 1.0 / n for k in scores}
    return {k: round(v / total, 6) for k, v in exps.items()}


def _top_two_gap(scores: Dict[str, float]) -> float:
    if not scores:
        return 0.0
    vals = sorted(scores.values(), reverse=True)
    if len(vals) < 2:
        return vals[0]
    return vals[0] - vals[1]
