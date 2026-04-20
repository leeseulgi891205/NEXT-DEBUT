/**
 * 컨디션 패널 — 순수 로직 (DOM 없음)
 * 생존(컨디션) / 압박(스트레스) / 성공률(집중도) / 팀 안정성(팀워크) 분리 모델
 */
(function (global) {
  'use strict';

  function clampPct(n) {
    var x = Math.round(Number(n) || 0);
    if (x < 0) return 0;
    if (x > 100) return 100;
    return x;
  }

  /** 이 값(%) 이하이면 생존 붕괴 → 무작위 1인 탈락 */
  var CONDITION_ELIM_THRESHOLD_PCT = 19;
  /** 팀워크 이 값 이하이면 경고 UI·AI 요약 */
  var TEAMWORK_WARNING_PCT = 30;
  /** 팀워크 이 값 이하이면(붕괴 압박) 스트레스 추가 상승 — game.js 교차효과에서 처리 */
  var TEAMWORK_INSTABILITY_PCT = 20;

  /** @deprecated 탈락은 컨디션 기준으로 변경됨. UI/레거시 호환용 */
  var TEAMWORK_ELIM_THRESHOLD_PCT = CONDITION_ELIM_THRESHOLD_PCT;
  var TEAMWORK_CAUTION_FLASH_MAX_PCT = TEAMWORK_WARNING_PCT;

  function getFocusStatus(value) {
    var v = clampPct(value);
    if (v < 40) return { label: '부족', tone: 'warning' };
    if (v < 70) return { label: '보통', tone: 'safe' };
    return { label: '좋음', tone: 'safe' };
  }

  function getFocusComment(value) {
    var v = clampPct(value);
    if (v < 40) return '집중이 잘 안 잡혀 있어요. 스트레스를 줄이고 응원을 해 주면 나아질 수 있어요.';
    if (v < 70) return '집중은 그럭저럭이에요. 더 올리고 싶으면 팀 상태를 안정시켜 보세요.';
    return '집중이 잘 유지되고 있어요.';
  }

  function getStressStatus(value) {
    var v = clampPct(value);
    if (v >= 100) return { label: '한계', tone: 'critical' };
    if (v >= 90) return { label: '위험', tone: 'critical' };
    if (v >= 70) return { label: '높음', tone: 'danger' };
    if (v >= 60) return { label: '부담', tone: 'warning' };
    if (v >= 40) return { label: '조금 있음', tone: 'safe' };
    return { label: '편함', tone: 'safe' };
  }

  function getStressComment(value) {
    var v = clampPct(value);
    if (v >= 100) return '최고치예요. 컨디션·집중이 계속 깎일 수 있어요. 휴식·응원으로 내려 주세요.';
    if (v >= 90) return '거의 한계 직전이에요. “쉬자”, “괜찮아” 같은 말로 당장 낮추세요.';
    if (v >= 70) return '꽤 높아요. 이러면 컨디션·집중이 계속 깎여요.';
    if (v >= 60) return '조금 부담이 커지고 있어요. 응원이나 휴식 멘트를 섞어 보세요.';
    if (v >= 40) return '아직 감당할 만한 수준이에요.';
    return '마음이 비교적 편한 상태예요.';
  }

  /** 생존 자원 — 탈락 판정은 이 막대 기준 */
  function getConditionStatus(value) {
    var v = clampPct(value);
    if (v <= CONDITION_ELIM_THRESHOLD_PCT) return { label: '탈락 가능', tone: 'critical' };
    if (v <= 25) return { label: '아주 위험', tone: 'critical' };
    if (v <= 39) return { label: '위험', tone: 'danger' };
    if (v <= 59) return { label: '주의', tone: 'warning' };
    if (v < 80) return { label: '괜찮음', tone: 'safe' };
    return { label: '좋음', tone: 'safe' };
  }

  function getConditionComment(value) {
    var v = clampPct(value);
    var th = CONDITION_ELIM_THRESHOLD_PCT;
    if (v <= th)
      return '위험해요. ' + th + '% 이하면 멤버 한 명이 나갈 수 있어요. 지금 당장 쉬게 해 주거나 응원하세요.';
    if (v <= 25) return '탈락 직전이에요. 휴식·회복 말을 자주 넣어 주세요.';
    if (v <= 39) return '몸이 많이 지쳤어요. 스트레스도 높으면 더 빨리 나빠져요.';
    if (v <= 59) return '힘이 빠지고 있어요. 그대로 두면 더 떨어질 수 있어요.';
    if (v < 80) return '괜찮은 편이에요.';
    return '컨디션이 좋아요.';
  }

  function getTeamworkStatus(value) {
    var v = clampPct(value);
    if (v <= TEAMWORK_INSTABILITY_PCT) return { label: '매우 낮음', tone: 'critical' };
    if (v <= TEAMWORK_WARNING_PCT) return { label: '나쁨', tone: 'danger' };
    if (v < 55) return { label: '보통', tone: 'warning' };
    if (v < 80) return { label: '괜찮음', tone: 'safe' };
    return { label: '좋음', tone: 'safe' };
  }

  function getTeamworkComment(value) {
    var v = clampPct(value);
    if (v <= TEAMWORK_INSTABILITY_PCT)
      return '팀 분위기가 너무 안 좋아요. 스트레스가 더 잘 올라가요. “같이 하자”, “얘기하자” 같은 말을 해 보세요.';
    if (v <= TEAMWORK_WARNING_PCT)
      return '싸우기 쉬운 분위기예요. 화내는 말은 피하고 다독여 주세요.';
    if (v < 55) return '그럭저럭이에요. 화합하는 말을 하면 올라가요.';
    if (v < 80) return '팀이 꽤 잘 맞아요.';
    return '팀이 아주 단단해요.';
  }

  function getProgressStatus(value) {
    var v = clampPct(value);
    if (v < 25) return { label: '초반', tone: 'warning' };
    if (v < 60) return { label: '중반', tone: 'safe' };
    if (v < 90) return { label: '후반', tone: 'safe' };
    return { label: '거의 끝', tone: 'danger' };
  }

  function getProgressComment(value) {
    var v = clampPct(value);
    if (v < 25) return '이번 시즌 초반이에요. 컨디션 관리 습관을 들이기 좋은 때예요.';
    if (v < 60) return '한창 진행 중이에요. 중간에 팀 상태가 흔들리지 않게 챙기세요.';
    if (v < 90) return '후반이에요. 지금부터 컨디션·스트레스가 승부를 가를 수 있어요.';
    return '거의 끝이에요. 막대들을 한 번씩 다시 확인하세요.';
  }

  /**
   * @param {{ focus:number, stress:number, teamwork:number, condition:number, progress:number }} state
   */
  function generateAISummary(state) {
    var lines = [];
    var stress = clampPct(state.stress);
    var team = clampPct(state.teamwork);
    var focus = clampPct(state.focus);
    var cond = clampPct(state.condition);

    if (cond <= 39) {
      lines.push('컨디션이 위험해요. 쉬게 해 주거나 응원하지 않으면 누군가 탈락할 수 있어요.');
    }
    if (stress >= 70) {
      lines.push('스트레스가 높아요. 칭찬·휴식 말을 하면 컨디션·집중이 덜 깎여요.');
    }
    if (team <= TEAMWORK_WARNING_PCT) {
      lines.push('팀 분위기가 안 좋아요. “같이”, “대화” 같은 말로 팀워크를 올려 보세요.');
    }
    if (focus < 40) {
      lines.push('집중이 부족해요. 스트레스를 먼저 낮추는 게 좋아요.');
    }
    if (lines.length === 0) {
      lines.push('지금은 무난해요. 이대로 리듬만 유지해 보세요.');
    }
    if (lines.length > 3) lines = lines.slice(0, 3);
    return lines;
  }

  /**
   * @param {{ stress:number, teamwork:number, condition:number, conditionRosterCount?:number }} state
   * @returns {'NORMAL'|'ELIMINATION'|'STRESS_EXPLOSION'}
   */
  function checkGameState(state) {
    var stress = clampPct(state.stress);
    var cond = clampPct(state.condition);
    var rc = state.conditionRosterCount;
    if (rc === 0) return 'NORMAL';
    if (stress >= 100) return 'STRESS_EXPLOSION';
    if (cond <= CONDITION_ELIM_THRESHOLD_PCT) return 'ELIMINATION';
    return 'NORMAL';
  }

  function eliminateLowestStatMember(members, getTotalForId) {
    if (!members || !members.length || typeof getTotalForId !== 'function') return null;
    var alive = members.filter(function (m) {
      return m && m.alive;
    });
    if (!alive.length) return null;
    alive.sort(function (a, b) {
      var ta = Number(getTotalForId(String(a.id))) || 0;
      var tb = Number(getTotalForId(String(b.id))) || 0;
      if (ta !== tb) return ta - tb;
      var pa = Number(a.pickOrder);
      var pb = Number(b.pickOrder);
      if (!isFinite(pa)) pa = 0;
      if (!isFinite(pb)) pb = 0;
      return pb - pa;
    });
    var victim = alive[0];
    victim.alive = false;
    return victim;
  }

  function eliminateRandomAliveMember(members) {
    if (!members || !members.length) return null;
    var alive = members.filter(function (m) {
      return m && m.alive;
    });
    if (!alive.length) return null;
    var victim = alive[Math.floor(Math.random() * alive.length)];
    if (!victim) return null;
    victim.alive = false;
    return victim;
  }

  /**
   * 상황 지문용 힌트 + 코치 한 줄 (맵 이벤트·채팅 전 복기용)
   * @param {{ stress?:number, teamwork?:number, condition?:number, focus?:number }} state
   */
  function generateSituationHint(state) {
    var stress = clampPct(state && state.stress != null ? state.stress : 50);
    var team = clampPct(state && state.teamwork != null ? state.teamwork : state && state.team != null ? state.team : 50);
    var cond = clampPct(state && state.condition != null ? state.condition : 50);
    var focus = clampPct(state && state.focus != null ? state.focus : 50);
    var hintLine = '';
    var coachLine = '';

    if (cond <= 39) {
      hintLine = '지금은 컨디션을 지키는 게 최우선이에요. 휴식·응원이 담긴 멘트가 도움이 될 수 있어요.';
      coachLine = '[코치] 몸이 먼저예요. 무리한 지시 전에 짧게라도 회복을 넣어 보세요.';
    } else if (stress >= 70) {
      hintLine = '스트레스가 높아 보여요. 압박보다 다독이는 말이 방향에 맞을 수 있어요.';
      coachLine = '[코치] 지금은 속도보다 멘탈 케어가 먼저일 때가 많아요.';
    } else if (team <= TEAMWORK_WARNING_PCT) {
      hintLine = '팀 분위기를 살릴 시점이에요. 같이 가자, 이야기 나누자 같은 톤을 권해요.';
      coachLine = '[코치] 소통·화합 쪽 멘트로 숫자를 끌어올릴 수 있어요.';
    } else if (focus < 40) {
      hintLine = '집중이 흔들릴 수 있어요. 스트레스를 먼저 가라앉히면 연습 지시가 더 잘 먹혀요.';
      coachLine = '[코치] 칭찬으로 안정감을 주면 집중도가 따라오기 쉬워요.';
    } else {
      hintLine = '전반적으로 무난해요. 성장과 휴식 밸런스를 채팅으로 조절해 보세요.';
      coachLine = '[코치] 지금 리듬을 유지하되, 피로가 보이면 잠깐 숨 돌리기도 잊지 마세요.';
    }

    return { hintLine: hintLine, coachLine: coachLine };
  }

  global.NdxConditionLogic = {
    CONDITION_ELIM_THRESHOLD_PCT: CONDITION_ELIM_THRESHOLD_PCT,
    TEAMWORK_WARNING_PCT: TEAMWORK_WARNING_PCT,
    TEAMWORK_INSTABILITY_PCT: TEAMWORK_INSTABILITY_PCT,
    TEAMWORK_ELIM_THRESHOLD_PCT: TEAMWORK_ELIM_THRESHOLD_PCT,
    TEAMWORK_CAUTION_FLASH_MAX_PCT: TEAMWORK_CAUTION_FLASH_MAX_PCT,
    clampPct: clampPct,
    getFocusStatus: getFocusStatus,
    getConditionStatus: getConditionStatus,
    getTeamworkStatus: getTeamworkStatus,
    getStressStatus: getStressStatus,
    getProgressStatus: getProgressStatus,
    getFocusComment: getFocusComment,
    getConditionComment: getConditionComment,
    getTeamworkComment: getTeamworkComment,
    getStressComment: getStressComment,
    getProgressComment: getProgressComment,
    generateAISummary: generateAISummary,
    generateSituationHint: generateSituationHint,
    checkGameState: checkGameState,
    eliminateLowestStatMember: eliminateLowestStatMember,
    eliminateRandomAliveMember: eliminateRandomAliveMember
  };
})(typeof window !== 'undefined' ? window : this);
