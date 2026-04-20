-- 튜토리얼(첫날) 씬 본문 — 결과·분위기 중심 지문 (기존 DB 수동 반영용)
-- SceneSeedRunner와 동일 문구. 제목 REPLACE는 구버전 DB 호환.

UPDATE game_scene
SET title = REPLACE(title, ' — 시스템 안내 (', ' — 연습실 첫 걸음 ('),
    description =
    '문이 열리자 차가운 공기가 얼굴을 스친다. 거울 너머로 실루엣만 겹쳐 보인다. ' ||
    '누군가의 발끝이 먼저 움직이기 시작한다.'
WHERE phase LIKE 'TUTORIAL\_%' ESCAPE '\'
  AND title LIKE '첫날 % — 시스템 안내 (%';

UPDATE game_scene
SET title = REPLACE(title, ' — 트레이닝 규칙 (', ' — 첫 루틴 ('),
    description =
    '타이머가 돌아갈수록 땀이 바닥에 떨어졌다 증발한다. 숨이 겹치기 시작하고 리듬이 천천히 하나로 맞는다.'
WHERE phase LIKE 'TUTORIAL\_%' ESCAPE '\'
  AND title LIKE '첫날 % — 트레이닝 규칙 (%';

UPDATE game_scene
SET title = REPLACE(title, ' — 팀 분위기 (', ' — 멤버와의 첫 호흡 ('),
    description =
    '연습이 잠시 멈추고 긴장이 조금씩 풀린다. 멤버들의 표정이 한결 부드러워지며 분위기가 가벼워진다.'
WHERE phase LIKE 'TUTORIAL\_%' ESCAPE '\'
  AND title LIKE '첫날 % — 팀 분위기 (%';

UPDATE game_scene
SET description =
    '문이 열리자 차가운 공기가 얼굴을 스친다. 거울 너머로 실루엣만 겹쳐 보인다. ' ||
    '누군가의 발끝이 먼저 움직이기 시작한다.'
WHERE phase LIKE 'TUTORIAL\_%' ESCAPE '\'
  AND title LIKE '첫날 % — 연습실 첫 걸음 (%';

UPDATE game_scene
SET description =
    '타이머가 돌아갈수록 땀이 바닥에 떨어졌다 증발한다. 숨이 겹치기 시작하고 리듬이 천천히 하나로 맞는다.'
WHERE phase LIKE 'TUTORIAL\_%' ESCAPE '\'
  AND title LIKE '첫날 % — 첫 루틴 (%';

UPDATE game_scene
SET description =
    '연습이 잠시 멈추고 긴장이 조금씩 풀린다. 멤버들의 표정이 한결 부드러워지며 분위기가 가벼워진다.'
WHERE phase LIKE 'TUTORIAL\_%' ESCAPE '\'
  AND title LIKE '첫날 % — 멤버와의 첫 호흡 (%';
