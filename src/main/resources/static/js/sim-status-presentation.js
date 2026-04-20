/**
 * 아이돌 육성 시뮬 — UI 상태 연출 (게이지·배너·로그·폭발·탈락)
 * window.IdolSimStatusPresentation
 */
(function (global) {
  'use strict';

  function clampPct(n) {
    var x = Math.round(Number(n) || 0);
    if (x < 0) return 0;
    if (x > 100) return 100;
    return x;
  }

  function qs(sel, root) {
    return (root || document).querySelector(sel);
  }

  function meterEl(key) {
    return qs('.ai-cond-meter[data-key="' + key + '"]');
  }

  function stripSimClasses(el) {
    if (!el) return;
    var prefix = [
      'sim-stress--shake-weak',
      'sim-stress--border-blink',
      'sim-stress--crit',
      'sim-condition--warn',
      'sim-condition--blink-red',
      'sim-condition--elim-risk',
      'sim-team--warn',
      'sim-team--caution',
      'sim-focus--low',
      'sim-gauge-strip'
    ];
    prefix.forEach(function (c) {
      el.classList.remove(c);
    });
    el.classList.remove('is-sim-crit');
    el.querySelectorAll('.sim-gauge-hint').forEach(function (h) {
      try {
        h.remove();
      } catch (e) {}
    });
  }

  function setMeterValue(el, pct) {
    if (!el) return;
    var key = el.getAttribute && el.getAttribute('data-key');
    var p;
    if (key === 'team') {
      p = Number(pct) || 0;
      if (p < 0) p = 0;
      if (p > 100) p = 100;
      p = Math.round(p * 2) / 2;
    } else {
      p = clampPct(pct);
    }
    el.setAttribute('data-pct', String(p));
    var val = el.querySelector('.status-bar__val');
    var fill = el.querySelector('.status-fill');
    if (val) {
      val.textContent =
        key === 'team' && p % 1 !== 0 ? String(p.toFixed(1)) + '%' : String(p) + '%';
    }
    if (fill) {
      fill.style.transition = 'width .55s cubic-bezier(.23,1,.46,1)';
      fill.style.width = p + '%';
    }
  }

  function applyStressPresentation(el, stress) {
    stripSimClasses(el);
    el.classList.add('sim-gauge-strip');
    if (stress >= 90) {
      el.classList.add('sim-stress--crit', 'is-sim-crit');
      var row = el.querySelector('.status-bar__row') || el;
      var hint = document.createElement('div');
      hint.className = 'sim-gauge-hint';
      hint.innerHTML = '<i class="fas fa-triangle-exclamation" aria-hidden="true"></i><span>위험: 스트레스 한계에 가깝습니다. 휴식·멘탈 관리가 필요합니다.</span>';
      var track = el.querySelector('.status-bar__track');
      if (track && track.parentNode === el) {
        el.insertBefore(hint, track.nextSibling);
      } else {
        el.appendChild(hint);
      }
    } else if (stress >= 70) {
      el.classList.add('sim-stress--border-blink');
    } else if (stress >= 40) {
      el.classList.add('sim-stress--shake-weak');
    }
  }

  function ndxLogic() {
    var L = typeof global !== 'undefined' && global.NdxConditionLogic ? global.NdxConditionLogic : null;
    if (!L && typeof window !== 'undefined') L = window.NdxConditionLogic;
    return L;
  }

  function ndxConditionElimThresholdPct() {
    var L = ndxLogic();
    return L && L.CONDITION_ELIM_THRESHOLD_PCT != null ? L.CONDITION_ELIM_THRESHOLD_PCT : 19;
  }

  function ndxTeamInstabilityPct() {
    var L = ndxLogic();
    return L && L.TEAMWORK_INSTABILITY_PCT != null ? L.TEAMWORK_INSTABILITY_PCT : 20;
  }

  function ndxTeamWarningPct() {
    var L = ndxLogic();
    return L && L.TEAMWORK_WARNING_PCT != null ? L.TEAMWORK_WARNING_PCT : 30;
  }

  /** 40~59 주의 · 20~39 붉은 경고 · ≤25 탈락 위험(컨디션 생존선 근접) */
  function applyConditionPresentation(el, condition) {
    stripSimClasses(el);
    if (condition <= 25) {
      el.classList.add('sim-condition--elim-risk', 'sim-gauge-strip', 'is-sim-crit');
    } else if (condition <= 39) {
      el.classList.add('sim-condition--blink-red', 'sim-gauge-strip');
    } else if (condition <= 59) {
      el.classList.add('sim-condition--warn', 'sim-gauge-strip');
    }
  }

  function applyTeamPresentation(el, teamwork) {
    stripSimClasses(el);
    var instab = ndxTeamInstabilityPct();
    if (teamwork <= instab) {
      el.classList.add('sim-team--warn', 'sim-gauge-strip');
    } else if (teamwork <= ndxTeamWarningPct()) {
      el.classList.add('sim-team--caution', 'sim-gauge-strip');
    }
  }

  function applyFocusPresentation(el, focus) {
    stripSimClasses(el);
    if (focus <= 30) {
      el.classList.add('sim-focus--low', 'sim-gauge-strip');
    }
  }

  /**
   * 위험 배너 자동 판정 (1=주의, 2=위험, 3=임계)
   */
  function resolveBanner(stress, condition, teamwork) {
    var cElim = ndxConditionElimThresholdPct();
    var twI = ndxTeamInstabilityPct();
    var twW = ndxTeamWarningPct();
    if (stress >= 90 || condition <= 25 || teamwork <= twI) {
      return {
        level: 3,
        text:
          stress >= 90
            ? '스트레스가 거의 한계예요. 100%에 닿으면 막대는 그대로 최고치로 유지되고, 컨디션·집중은 계속 깎일 수 있어요. 휴식·응원이 필요해요.'
            : condition <= 25
              ? '곧 탈락할 수 있어요. 컨디션이 ' + cElim + '% 이하로 떨어지면 멤버 한 명이 무작위로 나가요.'
              : '팀워크가 ' + twI + '% 이하예요. 팀이 너무 안 좋으면 스트레스가 더 잘 올라가요.'
      };
    }
    if (stress >= 70 || condition <= 39 || teamwork <= twW) {
      return {
        level: 2,
        text:
          stress >= 70
            ? '스트레스가 높아요. 컨디션·집중이 조금씩 계속 깎여요. 쉬게 해 주거나 응원해 주세요.'
            : condition <= 39
              ? '컨디션이 위험해요. 스트레스가 높으면 더 빨리 나빠져요. 휴식·응원 말을 넣어 보세요.'
              : '팀워크가 ' + twW + '% 이하예요. “같이 하자”, “얘기하자” 같은 말로 올려 보세요.'
      };
    }
    if (stress >= 40 || condition <= 59) {
      return {
        level: 1,
        text: '가끔 스트레스·컨디션 막대만 봐 주세요.'
      };
    }
    return null;
  }

  var lastSnapshot = { stress: null, condition: null, focus: null, teamwork: null };

  /**
   * @param {{ stress?:number, condition?:number, focus?:number, teamwork?:number }} stats
   * @param {{ logLines?:string[], skipBanner?:boolean, skipGauges?:boolean }} options
   */
  function updateStatusUI(stats, options) {
    stats = stats || {};
    options = options || {};
    var stress = stats.stress != null ? clampPct(stats.stress) : lastSnapshot.stress;
    var condition = stats.condition != null ? clampPct(stats.condition) : lastSnapshot.condition;
    var focus = stats.focus != null ? clampPct(stats.focus) : lastSnapshot.focus;
    var teamwork = stats.teamwork != null ? clampPct(stats.teamwork) : lastSnapshot.teamwork;

    if (stress != null) lastSnapshot.stress = stress;
    if (condition != null) lastSnapshot.condition = condition;
    if (focus != null) lastSnapshot.focus = focus;
    if (teamwork != null) lastSnapshot.teamwork = teamwork;

    if (!options.skipGauges) {
      var elS = meterEl('stress');
      var elC = meterEl('condition');
      var elF = meterEl('focus');
      var elT = meterEl('team');

      if (elS && stress != null) {
        stripSimClasses(elS);
        setMeterValue(elS, stress);
        applyStressPresentation(elS, stress);
      }
      if (elC && condition != null) {
        stripSimClasses(elC);
        setMeterValue(elC, condition);
        applyConditionPresentation(elC, condition);
        var bc = elC.querySelector('.ai-cond-meter__badge');
        if (bc) {
          var Lg = ndxLogic();
          var lb = '—';
          if (Lg && typeof Lg.getConditionStatus === 'function') {
            lb = Lg.getConditionStatus(condition).label;
          } else {
            lb =
              condition <= 19
                ? '탈락 가능'
                : condition <= 25
                  ? '아주 위험'
                  : condition <= 39
                    ? '위험'
                    : condition <= 59
                      ? '주의'
                      : condition < 80
                        ? '괜찮음'
                        : '좋음';
          }
          bc.textContent = '[' + lb + ']';
        }
      }
      if (elF && focus != null) {
        stripSimClasses(elF);
        setMeterValue(elF, focus);
        applyFocusPresentation(elF, focus);
      }
      if (elT && teamwork != null) {
        stripSimClasses(elT);
        setMeterValue(elT, teamwork);
        applyTeamPresentation(elT, teamwork);
      }
    }

    if (options.logLines && options.logLines.length) {
      appendLogLines(options.logLines);
    }

    if (!options.skipBanner && stress != null && condition != null && teamwork != null) {
      var b = resolveBanner(stress, condition, teamwork);
      if (b) showWarningBanner(b.text, b.level);
      else hideWarningBanner();
    }

    /* 탈락 임박 로그 (한 번만 중복 방지는 호출 측에서) */
    if (condition != null && condition <= 22 && condition > 19) {
      /* optional whisper — avoid spam: only if crossing */
    }
  }

  function showWarningBanner(message, level) {
    var el = qs('#ndxSimWarnBanner');
    if (!el) return;
    var lv = Math.min(3, Math.max(1, Number(level) || 1));
    el.querySelector('.sim-warn-banner__text').textContent = message || '';
    el.setAttribute('data-sim-level', String(lv));
    el.classList.add('is-visible');
    el.setAttribute('aria-hidden', 'false');
  }

  function hideWarningBanner() {
    var el = qs('#ndxSimWarnBanner');
    if (!el) return;
    el.classList.remove('is-visible');
    el.setAttribute('aria-hidden', 'true');
  }

  function appendLogLine(text) {
    if (text == null || String(text).trim() === '') return;
    var host = qs('#ndxSimStatusLogLines');
    if (!host) return;
    var line = document.createElement('div');
    line.className = 'sim-status-log__line';
    var t = String(text);
    if (/탈락|붕괴|폭발|임계|멘탈|스트레스 한계|터질/.test(t)) {
      line.classList.add('sim-status-log__line--crit');
    } else if (/위험|주의|저하/.test(t)) {
      line.classList.add('sim-status-log__line--warn');
    }
    line.textContent = t;
    host.appendChild(line);
    host.scrollTop = host.scrollHeight;
  }

  function appendLogLines(lines) {
    (lines || []).forEach(appendLogLine);
  }

  function clearLog() {
    var host = qs('#ndxSimStatusLogLines');
    if (host) host.innerHTML = '';
  }

  function shakeLayer(selector, className) {
    var el = qs(selector) || document.body;
    if (!el) return;
    el.classList.remove(className);
    void el.offsetWidth;
    el.classList.add(className);
    setTimeout(function () {
      try {
        el.classList.remove(className);
      } catch (e) {}
    }, className === 'sim-ui-shake-strong' ? 900 : 500);
  }

  /** 레거시 API 유지. 폭발 모달·70% 고정은 제거됨 — 콜백만 호출 */
  function triggerStressExplosion(done) {
    if (typeof done === 'function') done();
  }

  /**
   * 탈락 연출: 카드 강조 → 회색, 사이드 흔들림, 로그
   * @param {string|number} traineeId
   * @param {function():void} [done]
   * @param {{ skipDomAlter?: boolean }} [opts] 게임 본편에서 이미 탈락 DOM 처리 시 true (흔들림·로그만)
   */
  function triggerEliminationEffect(traineeId, done, opts) {
    opts = opts || {};
    var id = traineeId != null ? String(traineeId) : '';
    var card = id ? qs('.mcard[data-tid="' + id.replace(/"/g, '') + '"]') : null;

    appendLogLine('멤버 한 명이 탈락했어요 (컨디션이 너무 낮았어요)');
    appendLogLine('이후 막대·로그에 반영돼요.');

    shakeLayer('#ipadSidePanel', 'sim-ui-shake-strong');
    shakeLayer('#gameUiRoot', 'sim-screen-shake');

    if (opts.skipDomAlter) {
      if (typeof done === 'function') setTimeout(done, 100);
      return;
    }

    if (card) {
      card.classList.add('sim-elim--pulse');
      setTimeout(function () {
        try {
          card.classList.remove('sim-elim--pulse');
          card.classList.add('sim-elim--grayed', 'mcard--eliminated');
        } catch (e) {}
        if (typeof done === 'function') done();
      }, 2000);
    } else {
      if (typeof done === 'function') setTimeout(done, 400);
    }
  }

  /**
   * 채팅 직후 피드백 로그 (초보자용 원인 설명)
   * @param {{ lines?:string[], intentLabel?:string }} result
   */
  function showChatFeedbackLog(result) {
    var lines = result && result.lines;
    if (!lines || !lines.length) return;
    appendLogLine('── 채팅이 팀 상태에 반영됐어요 ──');
    lines.forEach(function (ln) {
      appendLogLine(ln);
    });
    if (result.intentLabel) {
      appendLogLine('(의도: ' + result.intentLabel + ')');
    }
  }

  /** 시뮬 전용 컨디션 값 저장 (게임 본편과 병행 시) */
  function setPresentationCondition(pct, reason) {
    lastSnapshot.condition = clampPct(pct);
    var elC = meterEl('condition');
    if (elC) {
      stripSimClasses(elC);
      setMeterValue(elC, lastSnapshot.condition);
      applyConditionPresentation(elC, lastSnapshot.condition);
    }
    if (reason) appendLogLine('컨디션 ' + clampPct(pct) + '% — ' + reason);
  }

  global.IdolSimStatusPresentation = {
    updateStatusUI: updateStatusUI,
    showWarningBanner: showWarningBanner,
    hideWarningBanner: hideWarningBanner,
    appendLogLine: appendLogLine,
    appendLogLines: appendLogLines,
    showChatFeedbackLog: showChatFeedbackLog,
    clearLog: clearLog,
    triggerStressExplosion: triggerStressExplosion,
    triggerEliminationEffect: triggerEliminationEffect,
    setPresentationCondition: setPresentationCondition,
    clampPct: clampPct,
    /** 예시 로그 (튜토리얼·디버그) */
    logExampleTurn: function () {
      appendLogLines([
        '스트레스 +4 (피로 누적)',
        '스트레스 +3 (팀워크 저하)',
        '컨디션 −5 (스트레스 영향)',
        '집중도 −3 (스트레스 영향)'
      ]);
    }
  };
})(typeof window !== 'undefined' ? window : this);
