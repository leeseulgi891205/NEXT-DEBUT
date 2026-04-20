/**
 * 채팅 기반 바이탈 시뮬 (순수 로직)
 * 의도(응원·중재·휴식·압박·기타) + 키워드·톤 — 스트레스·컨디션·집중도·팀워크
 */
(function (global) {
  'use strict';

  /** UI 연출용 채팅 시뮬 밸런스(과도한 스트레스·팀워크 깎임 완화) */
  var CHAT_SIM_BALANCE = {
    decaySumScale: 0.52,
    eventPenaltyScale: 0.55,
    /** 컨디션 하락만 추가 완화(압박·톤·액션 겹침 시 한 턴에 -15%p 같은 체감 방지) */
    conditionPenDownScale: 0.48,
    maxConditionDownPerTurn: 7
  };

  var ActionType = {
    FOCUS_TRAINING: 'FOCUS_TRAINING',
    PRESSURE_TRAINING: 'PRESSURE_TRAINING',
    REST: 'REST',
    TEAM_ACTIVITY: 'TEAM_ACTIVITY',
    NONE: 'NONE'
  };

  var ToneType = {
    POSITIVE: 'POSITIVE',
    NEUTRAL: 'NEUTRAL',
    NEGATIVE: 'NEGATIVE'
  };

  var ChatIntent = {
    SUPPORT: 'SUPPORT',
    MEDIATE: 'MEDIATE',
    REST: 'REST',
    PRESSURE: 'PRESSURE',
    OTHER: 'OTHER'
  };

  var PenaltyKind = {
    NONE: 'NONE',
    FORCED_SCHEDULE: 'FORCED_SCHEDULE',
    FAVORITISM: 'FAVORITISM',
    NO_REST: 'NO_REST'
  };

  var EventPenaltyType = {
    NONE: 'NONE',
    MINOR_CONFLICT: 'MINOR_CONFLICT',
    MAJOR_CONFLICT: 'MAJOR_CONFLICT',
    EMOTIONAL_BREAKDOWN: 'EMOTIONAL_BREAKDOWN'
  };

  var INTENT_LABEL_KO = {
    SUPPORT: '응원',
    MEDIATE: '중재·화합',
    REST: '휴식·회복',
    PRESSURE: '압박',
    OTHER: '일반 멘트'
  };

  function clampPct(n) {
    var x = Math.round(Number(n) || 0);
    if (x < 0) return 0;
    if (x > 100) return 100;
    return x;
  }

  function norm(s) {
    return String(s || '')
      .toLowerCase()
      .replace(/\s+/g, ' ')
      .trim();
  }

  function jitter(n, mag) {
    mag = mag || 1;
    var j = Math.floor((Math.random() * (2 * mag + 1)) - mag);
    return (Number(n) || 0) + j;
  }

  /**
   * 키워드 기반 의도 힌트 (분류 보조)
   */
  function applyKeywordVitalityDeltas(t) {
    var stress = 0;
    var teamwork = 0;
    var condition = 0;
    var focus = 0;
    var tags = [];

    if (
      /응원|칭찬|격려|괜찮아|괜찮|잘했|잘 했|할 수 있|할수있|힘내|고생|최고|화이팅|fighting|수고|대단|멋져|자랑|믿어|포기하지|괜찮을|걱정마|괜찮앙|위로|안아줄|끝까지|사랑해|ㄳ|고마워|좋아|훌륭|파이팅/.test(
        t
      )
    ) {
      tags.push('응원·칭찬');
    }

    if (/소통|같이|함께|맞춰|맞춰보|팀\b|화합|정리하자|중재|대화|이야기하|듣고|한마디|솔직히|오픈톡|캐미|케미|화해|갈등|오해|풀자|조율/.test(t)) {
      tags.push('소통·팀');
    }

    if (/쉬자|휴식|잠깐 쉬|잠깐쉬|정비|회복|릴렉스|재충전|스탑|멈추|천천히|밥 먹|잠자|수면|낮잠|off\b|break\b|rest\b/.test(t)) {
      tags.push('휴식·회복');
    }

    if (
      /비난|압박|몰아붙|몰아세|혼내|편애|왜 못|한심|실망|짜증|최악|형편없|없애|빡세게만|안쉬|쉬지마|쉬지 마|못하|나빠|bad|terrible/.test(
        t
      )
    ) {
      tags.push('비난·압박');
    }

    return { stress: stress, teamwork: teamwork, condition: condition, focus: focus, tags: tags };
  }

  function parseChatInput(text) {
    var raw = String(text || '');
    var t = norm(raw);

    var toneType = ToneType.NEUTRAL;
    if (
      /잘했|고생|최고|화이팅|fighting|great|good job|좋아|훌륭|칭찬|ㄳ|고마워|사랑해|응원|멋져|대단|괜찮아|할 수 있|격려|믿어|힘내|수고|파이팅|ㅎㅎ|ㅋㅋ|ㅇㅇ|nice|thanks|thank|love/.test(
        t
      )
    ) {
      toneType = ToneType.POSITIVE;
    } else if (
      /왜 못|못하|나빠|최악|혼내|bad|terrible|한심|실망|없애|왜 이렇게|못했|형편없|짜증|비난|압박|편애|몰아붙|싫어|짜증|답답|실망스/.test(t)
    ) {
      toneType = ToneType.NEGATIVE;
    }

    var actionType = ActionType.NONE;
    if (/(휴식|쉬자|쉬어|쉼|쉬게|rest|break|릴렉스|재충전|정비|회복)/.test(t)) {
      actionType = ActionType.REST;
    } else if (/(팀.{0,3}단합|단합|소통|회식|볼링|team building|팀 활동|유대|협업|화합|역할|분배|조율|맞춰보|같이)/.test(t)) {
      actionType = ActionType.TEAM_ACTIVITY;
    } else if (
      /(압박|강하게|빡세|혹사|pressure|밀어붙|더 빡|강도|승부수|도전|리스크|올인|한계|파격|과감|승부|무리해|승부걸|강훈련|몰아|과부하|극한|빡세게|전력)/.test(
        t
      )
    ) {
      actionType = ActionType.PRESSURE_TRAINING;
    } else if (
      /(집중|포커스|focus|열심히|훈련 강화|강도 올|더 연습|훈련|연습|드릴|반복|달리자)/.test(t) ||
      /(보컬|발성|노래|라이브|음정|호흡|가성|파트|후렴|보이스|vocal|싱잉|댄스|안무|춤|동선|퍼포|제스처|포인트|댄브|dance|킥|스텝|스타|카메라|미디어|노출|존재감|임팩트|화제|클립|star|비주얼|멘탈)/.test(
        t
      )
    ) {
      actionType = ActionType.FOCUS_TRAINING;
    }

    var penaltyKind = PenaltyKind.NONE;
    if (/(강제.{0,6}일정|일정.{0,4}몰아|스케줄.{0,4}빡|forced.{0,4}schedule)/.test(t)) {
      penaltyKind = PenaltyKind.FORCED_SCHEDULE;
    } else if (/(편애|한 명만|한명만|favorite|편들|차별)/.test(t)) {
      penaltyKind = PenaltyKind.FAVORITISM;
    } else if (/(잠도 안|쉬지 말|무리하게|연속.{0,4}연습|쉬게.{0,2}말)/.test(t)) {
      penaltyKind = PenaltyKind.NO_REST;
    }

    return {
      actionType: actionType,
      toneType: toneType,
      penaltyKind: penaltyKind,
      raw: raw
    };
  }

  /**
   * 문장 의도 분류 (키워드 우선, 없으면 톤·행동 유형)
   * @returns {{ intent:string, strength:string, reasons:string[], tags:string[] }}
   */
  function analyzeChatIntent(text, parsed) {
    parsed = parsed || parseChatInput(text);
    var t = norm(text);
    var tags = applyKeywordVitalityDeltas(t).tags;
    var reasons = [];

    if (/비난|압박|몰아붙|몰아세|혼내|편애|왜 못|한심|실망|짜증|최악|형편없|없애|빡세게만|안쉬|쉬지마|쉬지 마|못하|나빠|bad|terrible|싫어|답답/.test(t)) {
      reasons.push('압박·비난 표현이 감지됐어요');
      return { intent: ChatIntent.PRESSURE, strength: 'full', reasons: reasons, tags: tags };
    }

    if (/(휴식|쉬자|쉬어|쉼|쉬게|rest|break|릴렉스|재충전|정비|회복|잠깐 쉬|잠자|수면|낮잠)/.test(t)) {
      reasons.push('휴식·회복 쪽으로 이해했어요');
      return { intent: ChatIntent.REST, strength: 'full', reasons: reasons, tags: tags };
    }

    if (/소통|같이|함께|맞춰|화합|중재|대화|이야기|듣고|한마디|솔직히|캐미|케미|화해|갈등|오해|풀자|조율|팀\b|단합/.test(t)) {
      reasons.push('팀 분위기·소통을 다잡는 말로 이해했어요');
      return { intent: ChatIntent.MEDIATE, strength: 'full', reasons: reasons, tags: tags };
    }

    if (
      /응원|칭찬|격려|괜찮아|잘했|힘내|고생|최고|화이팅|fighting|수고|대단|멋져|믿어|위로|파이팅|good|great|nice|thanks|고마워|사랑해/.test(
        t
      )
    ) {
      reasons.push('응원·칭찬으로 이해했어요');
      return { intent: ChatIntent.SUPPORT, strength: 'full', reasons: reasons, tags: tags };
    }

    if (parsed.actionType === ActionType.REST) {
      reasons.push('휴식 지시로 이해했어요');
      return { intent: ChatIntent.REST, strength: 'full', reasons: reasons, tags: tags };
    }
    if (parsed.actionType === ActionType.TEAM_ACTIVITY) {
      reasons.push('팀 활동·화합으로 이해했어요');
      return { intent: ChatIntent.MEDIATE, strength: 'full', reasons: reasons, tags: tags };
    }
    if (parsed.actionType === ActionType.PRESSURE_TRAINING) {
      reasons.push('강한 훈련·압박 톤으로 이해했어요');
      return { intent: ChatIntent.PRESSURE, strength: 'full', reasons: reasons, tags: tags };
    }
    if (parsed.actionType === ActionType.FOCUS_TRAINING && parsed.toneType !== ToneType.NEGATIVE) {
      reasons.push('연습·집중을 독려하는 말로 이해했어요');
      return { intent: ChatIntent.SUPPORT, strength: 'full', reasons: reasons, tags: tags };
    }

    if (parsed.toneType === ToneType.POSITIVE) {
      reasons.push('긍정적인 말투라 응원에 가깝게 반영했어요');
      return { intent: ChatIntent.SUPPORT, strength: 'weak', reasons: reasons, tags: tags };
    }
    if (parsed.toneType === ToneType.NEGATIVE) {
      reasons.push('부정적인 말투라 압박으로 일부 반영했어요');
      return { intent: ChatIntent.PRESSURE, strength: 'weak', reasons: reasons, tags: tags };
    }

    if (t.length >= 2) {
      reasons.push('팀에게 말을 건 것만으로도 작은 안정감이 생겼어요');
      return { intent: ChatIntent.OTHER, strength: 'neutral', reasons: reasons, tags: tags };
    }

    return { intent: ChatIntent.OTHER, strength: 'none', reasons: [], tags: tags };
  }

  /**
   * 의도만으로 수치 델타 (퍼센트 포인트)
   */
  function applyChatEffect(intent, strength) {
    var i = String(intent || ChatIntent.OTHER).toUpperCase();
    var st = String(strength || 'full').toLowerCase();
    return getIntentVitalityDeltas(i, st);
  }

  function getIntentVitalityDeltas(intent, strength) {
    var full = strength === 'full';
    var weak = strength === 'weak';
    var neutral = strength === 'neutral';

    if (intent === ChatIntent.SUPPORT) {
      if (full) {
        return {
          stress: jitter(-5, 1),
          teamwork: jitter(4, 1),
          condition: jitter(2, 1),
          focus: jitter(2, 1)
        };
      }
      if (weak) {
        return { stress: jitter(-3, 1), teamwork: jitter(2, 1), condition: jitter(1, 1), focus: jitter(1, 1) };
      }
    }
    if (intent === ChatIntent.MEDIATE) {
      return {
        stress: jitter(-2, 1),
        teamwork: jitter(7, 1),
        condition: jitter(1, 1),
        focus: jitter(0, 1)
      };
    }
    if (intent === ChatIntent.REST) {
      return {
        stress: jitter(-6, 1),
        teamwork: jitter(1, 1),
        condition: jitter(6, 1),
        focus: jitter(0, 1)
      };
    }
    if (intent === ChatIntent.PRESSURE) {
      if (full) {
        return {
          stress: jitter(4, 1),
          teamwork: jitter(-3, 1),
          condition: jitter(-1, 1),
          focus: jitter(-1, 1)
        };
      }
      if (weak) {
        return { stress: jitter(2, 1), teamwork: jitter(-1, 1), condition: jitter(-1, 1), focus: jitter(-1, 1) };
      }
    }
    if (intent === ChatIntent.OTHER && neutral) {
      return { stress: jitter(-1, 1), teamwork: jitter(1, 1), condition: 0, focus: 0 };
    }
    return { stress: 0, teamwork: 0, condition: 0, focus: 0 };
  }

  function shouldSkipActionVitality(intent, actionType) {
    var i = String(intent || '');
    var a = String(actionType || ActionType.NONE).toUpperCase();
    if (a === String(ActionType.NONE).toUpperCase()) return false;
    if (i === ChatIntent.REST && a === String(ActionType.REST).toUpperCase()) return true;
    if (i === ChatIntent.MEDIATE && a === String(ActionType.TEAM_ACTIVITY).toUpperCase()) return true;
    if (i === ChatIntent.PRESSURE && a === String(ActionType.PRESSURE_TRAINING).toUpperCase()) return true;
    return false;
  }

  /** 톤만으로도 소폭 반영 (OTHER + 톤 조합용) */
  function getToneVitalityDeltas(toneType) {
    var t = String(toneType || '').toUpperCase();
    if (t === ToneType.POSITIVE) return { stress: -2, teamwork: 1, condition: 1, focus: 0 };
    if (t === ToneType.NEGATIVE) return { stress: 2, teamwork: -1, condition: -1, focus: 0 };
    return { stress: 0, teamwork: 0, condition: 0, focus: 0 };
  }

  function getStressTeamworkPenalty(stress) {
    var s = clampPct(stress);
    if (s >= 80) return 2;
    if (s >= 60) return 1;
    if (s >= 40) return 1;
    return 0;
  }

  function getActionTeamworkPenalty(actionType, penaltyKind) {
    var pk = String(penaltyKind || PenaltyKind.NONE).toUpperCase();
    if (pk === PenaltyKind.FORCED_SCHEDULE) return 2;
    if (pk === PenaltyKind.FAVORITISM) return 2;
    if (pk === PenaltyKind.NO_REST) return 1;
    var a = String(actionType || '').toUpperCase();
    if (a === ActionType.PRESSURE_TRAINING) return 2;
    return 0;
  }

  function getEventTeamworkPenalty(eventType) {
    var e = String(eventType || EventPenaltyType.NONE).toUpperCase();
    if (e === EventPenaltyType.MINOR_CONFLICT || e === 'MINOR') return 3;
    if (e === EventPenaltyType.MAJOR_CONFLICT || e === 'MAJOR') return 5;
    if (e === EventPenaltyType.EMOTIONAL_BREAKDOWN || e === 'BREAKDOWN') return 7;
    return 0;
  }

  function applyTeamworkDecay(state, parsed, eventType) {
    var p = parsed || {
      actionType: ActionType.NONE,
      penaltyKind: PenaltyKind.NONE
    };
    var ev = eventType != null && String(eventType).length ? eventType : EventPenaltyType.NONE;
    var stressP = getStressTeamworkPenalty(state.stress);
    var actP = getActionTeamworkPenalty(p.actionType, p.penaltyKind);
    var evtP = Math.round(getEventTeamworkPenalty(ev) * CHAT_SIM_BALANCE.eventPenaltyScale);
    var teamworkLoss = stressP + actP + evtP;
    teamworkLoss = Math.max(0, Math.round(teamworkLoss * CHAT_SIM_BALANCE.decaySumScale));
    return {
      teamworkLoss: teamworkLoss,
      stressPenalty: stressP,
      actionPenalty: actP,
      eventPenalty: evtP,
      teamworkAfterDecay: clampPct(state.teamwork - teamworkLoss),
      parsed: p
    };
  }

  function getActionTeamworkBonus(actionType) {
    var a = String(actionType || '').toUpperCase();
    if (a === ActionType.REST) return 2;
    if (a === ActionType.TEAM_ACTIVITY) return 4;
    if (a === ActionType.FOCUS_TRAINING) return -2;
    return 0;
  }

  function getActionStressDelta(actionType) {
    var a = String(actionType || '').toUpperCase();
    if (a === ActionType.FOCUS_TRAINING) return 3;
    if (a === ActionType.PRESSURE_TRAINING) return 4;
    if (a === ActionType.REST) return -5;
    if (a === ActionType.TEAM_ACTIVITY) return -2;
    return 0;
  }

  function getActionConditionDelta(actionType) {
    var a = String(actionType || '').toUpperCase();
    if (a === ActionType.REST) return 3;
    if (a === ActionType.TEAM_ACTIVITY) return 1;
    if (a === ActionType.PRESSURE_TRAINING) return -2;
    if (a === ActionType.FOCUS_TRAINING) return -1;
    return 0;
  }

  function getActionFocusDelta(actionType) {
    var a = String(actionType || '').toUpperCase();
    if (a === ActionType.FOCUS_TRAINING) return 2;
    if (a === ActionType.REST) return 0;
    return 0;
  }

  function applyChatEffects(stateAfterDecay, actionType, toneType) {
    var tone = getToneVitalityDeltas(toneType);
    var tw =
      clampPct(stateAfterDecay.teamwork) +
      tone.teamwork +
      getActionTeamworkBonus(actionType);
    var st = clampPct(stateAfterDecay.stress) + tone.stress + getActionStressDelta(actionType);
    var cd = clampPct(stateAfterDecay.condition) + tone.condition + getActionConditionDelta(actionType);
    var fc = clampPct(stateAfterDecay.focus) + tone.focus + getActionFocusDelta(actionType);
    return {
      teamwork: clampPct(tw),
      stress: clampPct(st),
      condition: clampPct(cd),
      focus: clampPct(fc)
    };
  }

  function checkGameState(state) {
    if (global.NdxConditionLogic && typeof global.NdxConditionLogic.checkGameState === 'function') {
      return global.NdxConditionLogic.checkGameState(state);
    }
    var stress = clampPct(state.stress);
    if (stress >= 100) return 'STRESS_EXPLOSION';
    return 'NORMAL';
  }

  function isMeaningfulChatAction(parsed, text, intentResult) {
    if (intentResult && intentResult.strength === 'none') return false;
    return String(text || '').trim().length >= 1;
  }

  function statDeltaLine(label, delta, cause) {
    if (!delta) return '';
    var sign = delta > 0 ? '+' : '';
    return label + ' ' + sign + delta + ' (' + cause + ')';
  }

  function buildFeedbackLines(prev, next, intentResult, decay) {
    var lines = [];
    var ds = next.stress - prev.stress;
    var dc = next.condition - prev.condition;
    var df = next.focus - prev.focus;
    var dt = next.teamwork - prev.teamwork;
    var intentKo = INTENT_LABEL_KO[intentResult.intent] || '멘트';
    var cause = intentResult.reasons && intentResult.reasons.length ? intentResult.reasons[0] : intentKo + ' 반영';

    if (ds) lines.push(statDeltaLine('스트레스', ds, cause));
    if (dt) lines.push(statDeltaLine('팀워크', dt, cause));
    if (dc) lines.push(statDeltaLine('컨디션', dc, cause));
    if (df) lines.push(statDeltaLine('집중도', df, cause));

    if (!lines.length) {
      lines.push('이번 멘트는 수치 변화가 거의 없었어요. (이미 한계에 가깝거나 효과가 상쇄됐을 수 있어요)');
    }

    lines.push('분류: 「' + intentKo + '」' + (decay.teamworkLoss > 0 ? ' · 상황으로 팀워크 ' + decay.teamworkLoss + ' 먼저 깎임' : ''));

    if (intentResult.tags && intentResult.tags.length) {
      lines.push('참고 키워드: ' + intentResult.tags.join(', '));
    }
    return lines;
  }

  function buildAiSummaryLine(intentResult, decay, prev, next) {
    var intentKo = INTENT_LABEL_KO[intentResult.intent] || '멘트';
    var head = '팀 반응: 「' + intentKo + '」로 처리했어요.';
    if (intentResult.reasons && intentResult.reasons.length) {
      head += ' ' + intentResult.reasons[0];
    }
    var parts = [];
    var ds = next.stress - prev.stress;
    var dc = next.condition - prev.condition;
    var df = next.focus - prev.focus;
    var dt = next.teamwork - prev.teamwork;
    if (ds) parts.push('스트레스 ' + (ds > 0 ? '+' : '') + ds);
    if (dt) parts.push('팀워크 ' + (dt > 0 ? '+' : '') + dt);
    if (dc) parts.push('컨디션 ' + (dc > 0 ? '+' : '') + dc);
    if (df) parts.push('집중도 ' + (df > 0 ? '+' : '') + df);
    if (parts.length) head += ' → ' + parts.join(', ') + '.';
    if (decay.teamworkLoss > 0) {
      head += ' (긴장한 상황으로 팀워크 ' + decay.teamworkLoss + ' 먼저 반영)';
    }
    return head;
  }

  /**
   * @param {{ teamwork:number, stress:number, condition:number, focus:number }} prevState
   */
  function runChatSimulationTurn(prevState, text, eventType) {
    var trimmed = String(text || '').trim();
    if (!trimmed.length) {
      return {
        parsed: parseChatInput(text),
        intentResult: { intent: ChatIntent.OTHER, strength: 'none', reasons: [], tags: [] },
        decay: {
          teamworkLoss: 0,
          stressPenalty: 0,
          actionPenalty: 0,
          eventPenalty: 0,
          teamworkAfterDecay: clampPct(prevState.teamwork),
          parsed: parseChatInput(text)
        },
        nextState: {
          teamwork: clampPct(prevState.teamwork),
          stress: clampPct(prevState.stress),
          condition: clampPct(prevState.condition),
          focus: clampPct(prevState.focus)
        },
        deltaTeamwork: 0,
        deltaStress: 0,
        deltaCondition: 0,
        deltaFocus: 0,
        aiLine: '',
        logLines: [],
        feedbackLines: [],
        intentLabelKo: '',
        gameState: checkGameState(prevState)
      };
    }

    var parsed = parseChatInput(text);
    var intentResult = analyzeChatIntent(text, parsed);

    var prev = {
      teamwork: clampPct(prevState.teamwork),
      stress: clampPct(prevState.stress),
      condition: clampPct(prevState.condition),
      focus: clampPct(prevState.focus)
    };

    if (!isMeaningfulChatAction(parsed, text, intentResult)) {
      return {
        parsed: parsed,
        intentResult: intentResult,
        decay: {
          teamworkLoss: 0,
          stressPenalty: 0,
          actionPenalty: 0,
          eventPenalty: 0,
          teamworkAfterDecay: prev.teamwork,
          parsed: parsed
        },
        nextState: prev,
        deltaTeamwork: 0,
        deltaStress: 0,
        deltaCondition: 0,
        deltaFocus: 0,
        aiLine: '',
        logLines: [],
        feedbackLines: [],
        intentLabelKo: '',
        gameState: checkGameState(prev)
      };
    }

    var decay = applyTeamworkDecay(prev, parsed, eventType);
    var id = getIntentVitalityDeltas(intentResult.intent, intentResult.strength);

    var mid = {
      teamwork: clampPct(decay.teamworkAfterDecay + id.teamwork),
      stress: clampPct(prev.stress + id.stress),
      condition: clampPct(prev.condition + id.condition),
      focus: clampPct(prev.focus + id.focus)
    };

    var effAction = parsed.actionType;
    if (shouldSkipActionVitality(intentResult.intent, parsed.actionType)) {
      effAction = ActionType.NONE;
    }

    var toneForResidual = intentResult.intent === ChatIntent.OTHER ? parsed.toneType : ToneType.NEUTRAL;

    var next = applyChatEffects(
      {
        teamwork: mid.teamwork,
        stress: mid.stress,
        condition: mid.condition,
        focus: mid.focus
      },
      effAction,
      toneForResidual
    );

    var dCondRaw = next.condition - prev.condition;
    if (dCondRaw < 0) {
      var adj = Math.round(dCondRaw * CHAT_SIM_BALANCE.conditionPenDownScale);
      var cap = CHAT_SIM_BALANCE.maxConditionDownPerTurn;
      if (cap > 0 && adj < -cap) adj = -cap;
      next.condition = clampPct(prev.condition + adj);
    }

    var feedbackLines = buildFeedbackLines(prev, next, intentResult, decay);
    var aiLine = buildAiSummaryLine(intentResult, decay, prev, next);
    var logLines = feedbackLines.slice();

    return {
      parsed: parsed,
      intentResult: intentResult,
      decay: decay,
      nextState: next,
      deltaTeamwork: next.teamwork - prev.teamwork,
      deltaStress: next.stress - prev.stress,
      deltaCondition: next.condition - prev.condition,
      deltaFocus: next.focus - prev.focus,
      aiLine: aiLine,
      logLines: logLines,
      feedbackLines: feedbackLines,
      intentLabelKo: INTENT_LABEL_KO[intentResult.intent] || '',
      gameState: checkGameState(next)
    };
  }

  function mapServerEventToPenaltyType(serverEvent) {
    if (!serverEvent) return EventPenaltyType.NONE;
    var u = String(serverEvent).toUpperCase();
    if (/\bMAJOR\b|MAJOR_CONFLICT|심각|대립|폭발/.test(u)) return EventPenaltyType.MAJOR_CONFLICT;
    if (/\bBREAKDOWN\b|붕괴|쇼크|멘탈붕괴/.test(u)) return EventPenaltyType.EMOTIONAL_BREAKDOWN;
    if (/\bMINOR\b|MINOR_CONFLICT|싸움|갈등/.test(u)) return EventPenaltyType.MINOR_CONFLICT;
    return EventPenaltyType.NONE;
  }

  global.NdxChatSimulation = {
    ActionType: ActionType,
    ToneType: ToneType,
    ChatIntent: ChatIntent,
    PenaltyKind: PenaltyKind,
    EventPenaltyType: EventPenaltyType,
    parseChatInput: parseChatInput,
    analyzeChatIntent: analyzeChatIntent,
    applyChatEffect: applyChatEffect,
    isMeaningfulChatAction: isMeaningfulChatAction,
    getToneVitalityDeltas: getToneVitalityDeltas,
    applyKeywordVitalityDeltas: applyKeywordVitalityDeltas,
    getStressTeamworkPenalty: getStressTeamworkPenalty,
    getActionTeamworkPenalty: getActionTeamworkPenalty,
    getEventTeamworkPenalty: getEventTeamworkPenalty,
    applyTeamworkDecay: applyTeamworkDecay,
    applyChatEffects: applyChatEffects,
    checkGameState: checkGameState,
    runChatSimulationTurn: runChatSimulationTurn,
    mapServerEventToPenaltyType: mapServerEventToPenaltyType,
    clampPct: clampPct,
    INTENT_LABEL_KO: INTENT_LABEL_KO
  };
})(typeof window !== 'undefined' ? window : this);
