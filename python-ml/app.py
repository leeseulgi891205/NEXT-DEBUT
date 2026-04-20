from __future__ import annotations

from typing import Dict, List, Optional

from fastapi import FastAPI
from pydantic import BaseModel, Field

from model import ChoiceInput, load_model


app = FastAPI(title="ProjectX Choice Predictor", version="0.1.0")
predictor = load_model()


class ChoicePayload(BaseModel):
    key: str = Field(..., description="Choice key, e.g., A/B/C/D/SPECIAL/NONE")
    text: str = ""
    statTarget: str = ""


class PredictionRequest(BaseModel):
    userText: str = ""
    sceneId: Optional[int] = None
    phase: str = ""
    choices: List[ChoicePayload] = Field(default_factory=list)


class PredictionResponse(BaseModel):
    predictedKey: Optional[str]
    confidence: float
    scoreByKey: Dict[str, float]


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok"}


@app.post("/predict-choice", response_model=PredictionResponse)
def predict_choice(request: PredictionRequest) -> PredictionResponse:
    choices = [
        ChoiceInput(
            key=(c.key or "").strip().upper(),
            text=(c.text or "").strip(),
            stat_target=(c.statTarget or "").strip(),
        )
        for c in request.choices
        if c.key
    ]
    result = predictor.predict_choice(
        user_text=request.userText or "",
        choices=choices,
        phase=request.phase or "",
        scene_id=request.sceneId,
    )
    return PredictionResponse(
        predictedKey=result.get("predicted_key"),
        confidence=float(result.get("confidence", 0.0)),
        scoreByKey=dict(result.get("score_by_key", {})),
    )


@app.post("/retrain")
def retrain() -> Dict[str, object]:
    predictor.reload()
    return {"ok": True, "sampleCount": predictor.sample_count, "modelEnabled": predictor.pipeline is not None}
