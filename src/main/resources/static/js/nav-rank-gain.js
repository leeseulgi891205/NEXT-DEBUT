
(function () {
  var cfg = window.__NAV_RANK_GAIN__;
  if (!cfg || typeof cfg.toPct !== 'number') return;

  function q(sel) {
    return document.querySelector(sel);
  }

  function easeOutCubic(t) {
    return 1 - Math.pow(1 - t, 3);
  }

  function run() {
    var fill = q('#navMemberRankExpFill');
    if (!fill) return;

    var fromPct = Math.max(0, Math.min(100, Number(cfg.fromPct) || 0));
    var toPct = Math.max(0, Math.min(100, Number(cfg.toPct) || 0));
    var fromExp = Number(cfg.fromExp) || 0;
    var toExp = Number(cfg.toExp) || 0;
    var delta = Number(cfg.delta) || Math.max(0, toExp - fromExp);

    fill.style.transition = 'none';
    fill.style.width = fromPct + '%';

    var host = q('.nav-user-block') || q('.mypage-trigger');
    var pop = document.createElement('div');
    pop.className = 'nav-rank-gain-pop';
    pop.setAttribute('aria-live', 'polite');
    pop.innerHTML =
      '<span class="nav-rank-gain-pop__delta">+' +
      delta +
      ' EXP</span>' +
      '<span class="nav-rank-gain-pop__exp">' +
      fromExp +
      ' → ' +
      toExp +
      '</span>';
    if (host) {
      host.style.position = 'relative';
      host.appendChild(pop);
    }

    requestAnimationFrame(function () {
      requestAnimationFrame(function () {
        fill.style.transition = 'width 1.15s cubic-bezier(.23, 1, .32, 1)';
        fill.style.width = toPct + '%';
        if (fill.parentElement) {
          fill.parentElement.setAttribute('aria-valuenow', String(Math.round(toPct)));
        }
      });
    });

    window.setTimeout(function () {
      pop.classList.add('is-out');
      window.setTimeout(function () {
        try {
          pop.remove();
        } catch (e) {}
      }, 600);
    }, 2200);

    var expLine = q('#navMemberRankExpLine');
    var expLineHoldMs = 10000;
    window.setTimeout(function () {
      if (expLine) {
        expLine.classList.add('flash-out');
      }
    }, expLineHoldMs);

    var expEl = q('#navMemberRankExpTicker');
    if (expEl && fromExp !== toExp) {
      var dur = 1100;
      var t0 = performance.now();
      function tick(now) {
        var u = Math.min(1, (now - t0) / dur);
        var e = Math.round(fromExp + (toExp - fromExp) * easeOutCubic(u));
        expEl.textContent = e;
        if (u < 1) requestAnimationFrame(tick);
      }
      requestAnimationFrame(tick);
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', run);
  } else {
    run();
  }
})();
