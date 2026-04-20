/**
 * 컨디션 패널 — UI 갱신만 담당 (로직은 NdxConditionLogic)
 */
(function (global) {
  'use strict';

  var L = function () {
    return global.NdxConditionLogic;
  };

  function setMeterTone(el, tone) {
    if (!el) return;
    el.setAttribute('data-tone', tone || 'safe');
    el.classList.remove('ai-cond-meter--safe', 'ai-cond-meter--warning', 'ai-cond-meter--danger', 'ai-cond-meter--critical');
    el.classList.add('ai-cond-meter--' + (tone || 'safe'));
  }

  function clampTeamMeterPct(n) {
    var x = Number(n) || 0;
    if (x < 0) x = 0;
    if (x > 100) x = 100;
    return Math.round(x * 2) / 2;
  }

  function renderStatBlock(root, key, pct, nameKo, nameEn, statusFn, commentFn) {
    var logic = L();
    if (!logic || !root) return;
    var p = key === 'team' ? clampTeamMeterPct(pct) : logic.clampPct(pct);
    var st = statusFn(p);
    var comment = commentFn(p);

    root.setAttribute('data-pct', String(p));
    setMeterTone(root, st.tone);
    if (key === 'team') {
      var instab = logic.TEAMWORK_INSTABILITY_PCT != null ? logic.TEAMWORK_INSTABILITY_PCT : 20;
      var warnMax = logic.TEAMWORK_WARNING_PCT != null ? logic.TEAMWORK_WARNING_PCT : 30;
      root.setAttribute('data-team-instability', String(instab));
      root.setAttribute('data-team-warn', String(warnMax));
      root.removeAttribute('data-elim-threshold');
      root.classList.remove('ai-cond-meter--team-flash', 'ai-cond-meter--team-flash-warn');
      if (p <= instab) {
        root.classList.add('ai-cond-meter--team-flash');
      } else if (p <= warnMax) {
        root.classList.add('ai-cond-meter--team-flash-warn');
      }
    } else if (key === 'condition') {
      var elimC = logic.CONDITION_ELIM_THRESHOLD_PCT != null ? logic.CONDITION_ELIM_THRESHOLD_PCT : 19;
      root.setAttribute('data-elim-threshold', String(elimC));
      root.classList.remove('ai-cond-meter--team-flash', 'ai-cond-meter--team-flash-warn');
    } else {
      root.classList.remove('ai-cond-meter--team-flash', 'ai-cond-meter--team-flash-warn');
    }

    var valEl = root.querySelector('.status-bar__val');
    var badgeEl = root.querySelector('.ai-cond-meter__badge');
    var commentEl = root.querySelector('.ai-cond-meter__comment');
    var fill = root.querySelector('.status-fill');
    var enEl = root.querySelector('.ai-cond-meter__en');

    if (valEl) {
      valEl.textContent =
        key === 'team' && p % 1 !== 0 ? String(p.toFixed(1)) + '%' : String(p) + '%';
    }
    if (badgeEl) badgeEl.textContent = '[' + st.label + ']';
    if (commentEl) commentEl.textContent = comment;
    if (enEl) enEl.textContent = nameEn;
    if (fill) {
      fill.style.transition = 'width .7s cubic-bezier(.23,1,.46,1)';
      fill.style.width = p + '%';
    }
  }

  function renderSquad(members) {
    var host = document.getElementById('ndxCondSquad');
    if (!host) return;
    host.innerHTML = '';
    if (!members || !members.length) {
      host.setAttribute('data-empty', '1');
      return;
    }
    host.removeAttribute('data-empty');
    members.forEach(function (m) {
      if (!m) return;
      var chip = document.createElement('span');
      chip.className = 'ai-cond-squad__chip' + (m.alive ? '' : ' ai-cond-squad__chip--out');
      chip.setAttribute('data-member-id', String(m.id));
      chip.textContent = m.alive ? m.name : m.name + ' · 탈락';
      host.appendChild(chip);
    });
  }

  function renderSummary(lines) {
    var box = document.getElementById('ndxAiSummary');
    if (!box) return;
    var text = document.getElementById('ndxAiSummaryText');
    if (!text) return;
    text.innerHTML = '';
    (lines || []).forEach(function (line) {
      var p = document.createElement('p');
      p.className = 'ai-cond-summary__line';
      p.textContent = line;
      text.appendChild(p);
    });
  }

  function zeroTraineeStatsOnCard(card) {
    if (!card) return;
    var keys = ['v', 'd', 's', 'm', 't'];
    keys.forEach(function (k) {
      var valEl = card.querySelector('.sval[data-key="' + k + '"]');
      var bar = card.querySelector('.sfill--' + k);
      if (valEl) valEl.textContent = '0';
      if (bar) bar.style.width = '0%';
    });
    var totEl = card.querySelector('.ctotal-num');
    if (totEl) totEl.textContent = '0';
  }

  function applyEliminationToMcard(memberId) {
    var card = document.querySelector('.mcard[data-tid="' + String(memberId) + '"]');
    if (!card) return;
    card.classList.add('mcard--eliminated');
    card.setAttribute('data-eliminated', '1');
    zeroTraineeStatsOnCard(card);
    if (!card.querySelector('.mcard-eliminated-badge')) {
      var b = document.createElement('span');
      b.className = 'mcard-eliminated-badge';
      b.setAttribute('aria-label', '탈락');
      b.textContent = '탈락';
      var wrap = card.querySelector('.cname-wrap') || card.querySelector('.ctop') || card.querySelector('.cinfo');
      if (wrap) wrap.appendChild(b);
      else card.appendChild(b);
    }
    try {
      if (typeof window.removeEliminatedDialogueFromChatLog === 'function') {
        window.removeEliminatedDialogueFromChatLog();
      }
    } catch (e) {}
  }

  function showGameOver(show) {
    var ov = document.getElementById('ndxGameOverOverlay');
    if (!ov) return;
    ov.setAttribute('aria-hidden', show ? 'false' : 'true');
    ov.classList.toggle('is-open', !!show);
    if (show) {
      try {
        if (window.__ndxGameOverRedirectTimer) {
          clearTimeout(window.__ndxGameOverRedirectTimer);
          window.__ndxGameOverRedirectTimer = null;
        }
        var cfg = window.NDX_GAME_CONFIG || {};
        var ctx =
          typeof CTX !== 'undefined' ? String(CTX || '') : String(cfg.ctx || '');
        var rid =
          typeof RUN_ID !== 'undefined' ? String(RUN_ID || '') : String(cfg.runId || '');
        if (ctx && rid) {
          window.__ndxGameOverRedirectTimer = setTimeout(function () {
            window.__ndxGameOverRedirectTimer = null;
            window.location.href = ctx + '/game/run/' + rid + '/roster';
          }, 2600);
        }
      } catch (e) {}
    } else {
      try {
        if (window.__ndxGameOverRedirectTimer) {
          clearTimeout(window.__ndxGameOverRedirectTimer);
          window.__ndxGameOverRedirectTimer = null;
        }
      } catch (e2) {}
    }
  }

  function showEliminationBanner(message) {
    var text = message || '컨디션이 너무 낮아서 멤버가 탈락했습니다.';
    var line = '탈락 · ' + text;
    if (typeof window.showToast === 'function') {
      window.showToast(line, 'warn', 4800);
      return;
    }
    try {
      alert(line);
    } catch (e) {}
  }

  var COND_HELP = {
    focus: {
      title: '집중도 — 이 숫자는 뭐예요?',
      paragraphs: [
        '“지금 팀이 연습에 얼마나 잘 집중하고 있나요?”를 0~100%로 보여 주는 막대예요. 카드에 있는 능력치들이 함께 반영돼요.',
        '숫자가 낮으면 [부족]이 떠요. 그때는 스트레스를 먼저 가라앉히고, 칭찬·응원 같은 말을 섞어 주면 도움이 될 수 있어요.',
        '높을수록 좋아요. 너무 낮으면 훈련 결과가 잘 안 나올 수 있다고 생각하면 됩니다.'
      ]
    },
    stress: {
      title: '스트레스 — 이 숫자는 뭐예요?',
      paragraphs: [
        '스트레스는 팀이 받는 압박 정도를 보여 주는 수치예요. 첫날은 0%에서 시작하고, 일차가 넘어갈 때마다 기본적으로 조금씩 올라가요.',
        '수치가 높아질수록 컨디션과 집중도 관리가 어려워져요. 특히 70% 이상부터는 불리함이 커지고, 90% 이상은 매우 위험한 구간이에요.',
        '중요: 스트레스가 100%에 도달하면 게임이 즉시 종료(엔딩 이동)돼요. 한계치에 닿기 전에 반드시 낮춰야 해요.',
        '낮추고 싶다면 “쉬자”, “괜찮아”, “힘내” 같은 안정·응원 멘트를 자주 섞고, 스트레스 완화 이벤트를 활용해 주세요.'
      ]
    },
    condition: {
      title: '컨디션 — 이 숫자는 뭐예요?',
      paragraphs: [
        '“팀 전체가 버틸 힘이 얼마나 남았나요?”를 보여 주는 막대예요. 이게 가장 중요해요. 여기가 바닥나면 연습생이 나갈 수 있어요.',
        '19% 이하로 떨어지면 위험해요. 그때는 살아 있는 멤버 중 한 명이 무작위로 탈락할 수 있어요. 20~39%도 “위험” 구간이니 빨리 회복시키세요.',
        '스트레스가 높으면 컨디션이 더 빨리 깎여요. “쉬자”, “휴식”, “괜찮아”처럼 쉬게 해 주거나 응원하는 채팅이 도움이 돼요.'
      ]
    },
    team: {
      title: '팀워크 — 이 숫자는 뭐예요?',
      paragraphs: [
        '팀워크는 팀 분위기와 협업 상태를 보여 주는 수치예요. 첫날은 100%에서 시작하고, 일차가 넘어갈 때마다 조금씩 자연 감소해요.',
        '수치가 낮아질수록 분위기가 흔들려요. 특히 30% 이하는 관리가 필요한 구간이고, 20% 이하면 스트레스가 더 쉽게 오를 수 있어요.',
        '중요: 팀워크가 낮다고 멤버가 바로 탈락하지는 않아요. 자동 탈락은 컨디션이 임계치(19% 이하)일 때만 발생해요.',
        '팀워크를 올리고 싶다면 “같이 하자”, “얘기해 보자” 같은 화합형 멘트를 자주 사용해 주세요. 반대로 비난·압박 톤은 팀워크를 떨어뜨릴 수 있어요.'
      ]
    }
  };

  function openNdxCondHelpModal(key) {
    var cfg = COND_HELP[key];
    if (!cfg) return;
    var modal = document.getElementById('ndxCondHelpModal');
    var titleEl = document.getElementById('ndxCondHelpTitle');
    var bodyEl = document.getElementById('ndxCondHelpBody');
    if (!modal || !titleEl || !bodyEl) return;
    titleEl.textContent = cfg.title;
    bodyEl.innerHTML = '';
    (cfg.paragraphs || []).forEach(function (line) {
      var p = document.createElement('p');
      p.textContent = line;
      bodyEl.appendChild(p);
    });
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    try {
      document.body.style.overflow = 'hidden';
    } catch (e) {}
  }

  function closeNdxCondHelpModal() {
    var modal = document.getElementById('ndxCondHelpModal');
    if (!modal) return;
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
    try {
      document.body.style.overflow = '';
    } catch (e) {}
  }

  function initNdxCondHelpModal() {
    document.addEventListener('click', function (e) {
      var t = e.target;
      if (!t || !t.closest) return;
      var helpBtn = t.closest('[data-cond-help]');
      if (helpBtn) {
        var k = helpBtn.getAttribute('data-cond-help');
        if (k) {
          e.preventDefault();
          openNdxCondHelpModal(k);
        }
        return;
      }
      if (t.closest('[data-ndx-cond-help-close]')) {
        closeNdxCondHelpModal();
      }
    });
  }

  initNdxCondHelpModal();
  global.closeNdxCondHelpModal = closeNdxCondHelpModal;

  /**
   * @param {{ focus:number, stress:number, teamwork:number, condition:number, progress:number, members:Array }} state
   */
  function render(state) {
    var logic = L();
    if (!logic) return;

    var focusEl = document.querySelector('.ai-cond-meter[data-key="focus"]');
    var stressEl = document.querySelector('.ai-cond-meter[data-key="stress"]');
    var condEl = document.querySelector('.ai-cond-meter[data-key="condition"]');
    var teamEl = document.querySelector('.ai-cond-meter[data-key="team"]');
    var progEl = document.querySelector('.ai-cond-meter[data-key="progress"]');

    renderStatBlock(focusEl, 'focus', state.focus, '집중도', 'Focus', logic.getFocusStatus, logic.getFocusComment);
    renderStatBlock(stressEl, 'stress', state.stress, '스트레스', 'Stress', logic.getStressStatus, logic.getStressComment);
    renderStatBlock(
      condEl,
      'condition',
      state.condition != null ? state.condition : 0,
      '컨디션',
      'Condition',
      logic.getConditionStatus,
      logic.getConditionComment
    );
    renderStatBlock(teamEl, 'team', state.teamwork, '팀워크', 'Teamwork', logic.getTeamworkStatus, logic.getTeamworkComment);
    renderStatBlock(progEl, 'progress', state.progress, '진행도', 'Progress', logic.getProgressStatus, logic.getProgressComment);

    renderSquad(state.members || []);
    renderSummary(logic.generateAISummary(state));
  }

  global.NdxConditionPanelView = {
    render: render,
    applyEliminationToMcard: applyEliminationToMcard,
    showGameOver: showGameOver,
    showEliminationBanner: showEliminationBanner
  };
})(typeof window !== 'undefined' ? window : this);
