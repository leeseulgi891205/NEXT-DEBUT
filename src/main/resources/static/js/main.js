function initRefLivePickCarousel() {
  var root = document.getElementById('refLivePickCarousel');
  if (!root) {
    return;
  }
  var slides = root.querySelectorAll('.ref-live-pick__slide');
  if (!slides.length) {
    return;
  }
  if (slides.length === 1) {
    slides[0].classList.add('is-active');
    return;
  }

  var index = 0;
  var timer = null;
  var AUTO_MS = 5000;

  function goTo(next) {
    index = ((next % slides.length) + slides.length) % slides.length;
    slides.forEach(function (slide, idx) {
      slide.classList.toggle('is-active', idx === index);
    });
    root.querySelectorAll('.ref-live-pick__dot').forEach(function (dot, idx) {
      dot.classList.toggle('is-active', idx === index);
      dot.setAttribute('aria-selected', idx === index ? 'true' : 'false');
    });
  }

  function startAuto() {
    if (timer !== null) {
      window.clearInterval(timer);
    }
    timer = window.setInterval(function () {
      goTo(index + 1);
    }, AUTO_MS);
  }

  function stopAuto() {
    if (timer !== null) {
      window.clearInterval(timer);
      timer = null;
    }
  }

  goTo(0);

  root.addEventListener('click', function (e) {
    var t = e.target;
    if (!t || !t.closest) {
      return;
    }
    var dot = t.closest('.ref-live-pick__dot');
    if (!dot || !root.contains(dot)) {
      return;
    }
    e.preventDefault();
    var raw = dot.getAttribute('data-dot');
    var idx = raw == null || raw === '' ? NaN : parseInt(raw, 10);
    if (isNaN(idx)) {
      return;
    }
    goTo(idx);
    startAuto();
  });

  document.addEventListener('visibilitychange', function () {
    if (document.hidden) {
      stopAuto();
    } else {
      startAuto();
    }
  });

  startAuto();
}

/** 메인 하단: TOP 랭킹·인기 게시글·인기 아이템 — 자동 슬라이드(간격 넓혀 CPU 부담 완화) */
function initRefDashboardCarousels() {
  var roots = [
    document.getElementById('refRankCarousel'),
    document.getElementById('refBoardCarousel'),
    document.getElementById('refItemsCarousel')
  ];

  var AUTO_MS = 8000;

  function collectSlides(track) {
    var out = [];
    var i;
    for (i = 0; i < track.children.length; i++) {
      var el = track.children[i];
      if (el && el.classList && el.classList.contains('ref-dashboard-carousel__slide')) {
        out.push(el);
      }
    }
    if (out.length) {
      return out;
    }
    return Array.prototype.slice.call(track.querySelectorAll('.ref-dashboard-carousel__slide'));
  }

  roots.forEach(function (root) {
    if (!root || root.getAttribute('data-ref-dashboard-init') === '1') {
      return;
    }

    var track = root.querySelector('.ref-dashboard-carousel__slides');
    if (!track) {
      return;
    }

    var slides = collectSlides(track);
    if (!slides.length) {
      return;
    }

    root.setAttribute('data-ref-dashboard-init', '1');

    if (slides.length === 1) {
      slides[0].classList.add('is-active');
      return;
    }

    var index = 0;
    var timer = null;
    var slidesLen = slides.length;

    function goTo(next) {
      index = ((next % slidesLen) + slidesLen) % slidesLen;
      var s;
      for (s = 0; s < slides.length; s++) {
        slides[s].classList.toggle('is-active', s === index);
      }
      var dots = root.querySelectorAll('.ref-dashboard-carousel__dot');
      var d;
      for (d = 0; d < dots.length; d++) {
        dots[d].classList.toggle('is-active', d === index);
        dots[d].setAttribute('aria-selected', d === index ? 'true' : 'false');
      }
    }

    function startAuto() {
      if (timer !== null) {
        window.clearInterval(timer);
        timer = null;
      }
      timer = window.setInterval(function () {
        goTo(index + 1);
      }, AUTO_MS);
    }

    function stopAuto() {
      if (timer !== null) {
        window.clearInterval(timer);
        timer = null;
      }
    }

    goTo(0);

    root.addEventListener('click', function (e) {
      var t = e.target;
      if (!t || !t.closest) {
        return;
      }
      var dot = t.closest('.ref-dashboard-carousel__dot');
      if (!dot || !root.contains(dot)) {
        return;
      }
      e.preventDefault();
      var raw = dot.getAttribute('data-dot');
      var idx = raw == null || raw === '' ? NaN : parseInt(raw, 10);
      if (isNaN(idx)) {
        return;
      }
      goTo(idx);
      startAuto();
    });

    document.addEventListener('visibilitychange', function () {
      if (document.hidden) {
        stopAuto();
      } else {
        startAuto();
      }
    });

    // 다음 이벤트 루프에서 시작: 초기 로드 시 visibilitychange가 동기로 stopAuto를 호출해
    // 타이머가 바로 사라지거나, document.hidden만 보고 시작을 건너뛰는 경우를 피함
    window.setTimeout(function () {
      startAuto();
    }, 0);
  });
}

/** 히어로 부제: 타자 연출은 레이아웃·타이머 부담이 커서 즉시 전체 문구 표시 */
function initHeroSubtitleTypewriter() {
  var root = document.getElementById('heroTypeSubtitle');
  if (!root || root.getAttribute('data-typed') === 'true') {
    return;
  }

  var chunks = Array.prototype.slice.call(root.querySelectorAll('.ref-typewriter__chunk'));
  if (!chunks.length) {
    return;
  }

  root.setAttribute('data-typed', 'true');
  chunks.forEach(function (chunk) {
    var full = chunk.getAttribute('data-full-text');
    if (full == null || full === '') {
      full = chunk.textContent || '';
      chunk.setAttribute('data-full-text', full);
    }
    chunk.textContent = full;
  });
  root.classList.add('is-complete');
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initRefLivePickCarousel);
} else {
  initRefLivePickCarousel();
}

function runDashboardCarouselInit() {
  initRefDashboardCarousels();
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', runDashboardCarouselInit);
} else {
  runDashboardCarouselInit();
}

window.addEventListener('load', runDashboardCarouselInit);

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initHeroSubtitleTypewriter);
} else {
  initHeroSubtitleTypewriter();
}

(function () {
  var ticker = document.getElementById('refRankTicker');
  if (!ticker) {
    return;
  }

  var items = ticker.querySelectorAll('.ref-rank-ticker__item');
  if (!items.length) {
    return;
  }

  var index = 0;
  function show(next) {
    items.forEach(function (item, idx) {
      item.classList.toggle('is-active', idx === next);
    });
  }

  show(index);
  if (items.length < 2) {
    return;
  }

  window.setInterval(function () {
    index = (index + 1) % items.length;
    show(index);
  }, 4000);
})();

(function () {
  var revealEls = document.querySelectorAll('[data-reveal]');
  if (!revealEls.length) {
    return;
  }

  if ('IntersectionObserver' in window) {
    var io = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.16, rootMargin: '0px 0px -6% 0px' });

    revealEls.forEach(function (el) {
      io.observe(el);
    });
  } else {
    revealEls.forEach(function (el) {
      el.classList.add('is-visible');
    });
  }
})();

(function () {
  var modal = document.getElementById('gameStartModal');
  var triggers = document.querySelectorAll('.js-open-game-modal');
  if (!modal || !triggers.length) {
    return;
  }

  var closeTargets = modal.querySelectorAll('[data-close-game-modal]');
  var closeBtn = document.getElementById('gameModalClose');
  var startBtn = document.getElementById('gameModalStartBtn');
  var statusEl = document.getElementById('gameModalStatus');
  var groupEls = modal.querySelectorAll('[data-group-option]');

  function openModal() {
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  }

  function closeModal() {
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden', 'true');
    document.body.style.overflow = '';
  }

  triggers.forEach(function (trigger) {
    trigger.addEventListener('click', function (e) {
      var loggedIn = trigger.getAttribute('data-li') === 'true';
      if (!loggedIn) {
        e.preventDefault();
        alert('로그인 후 팀을 생성할 수 있습니다.');
        window.location.href = (window.mainPageConfig.ctx + '/login?redirect=') + encodeURIComponent('/main');
        return;
      }
      e.preventDefault();
      openModal();
    });
  });

  closeTargets.forEach(function (el) {
    el.addEventListener('click', closeModal);
  });

  if (closeBtn) {
    closeBtn.addEventListener('click', closeModal);
  }

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && modal.classList.contains('is-open')) {
      closeModal();
    }
  });

  groupEls.forEach(function (groupEl) {
    groupEl.addEventListener('click', function () {
      groupEls.forEach(function (item) {
        item.classList.remove('is-selected');
      });
      groupEl.classList.add('is-selected');
      var input = groupEl.querySelector('input[name="groupType"]');
      if (input) {
        input.checked = true;
      }
      if (statusEl) {
        statusEl.textContent = '선택한 그룹 구성으로 로스터를 생성합니다.';
      }
    });
  });

  if (startBtn) {
    startBtn.addEventListener('click', function () {
      var checked = modal.querySelector('input[name="groupType"]:checked');
      if (!checked) {
        if (statusEl) {
          statusEl.textContent = '그룹 타입을 먼저 선택해 주세요.';
        }
        return;
      }

      startBtn.disabled = true;
      if (statusEl) {
        statusEl.textContent = '데뷔 팀을 준비하고 있습니다...';
      }

      var form = document.createElement('form');
      form.method = 'post';
      form.action = window.mainPageConfig.ctx + '/game/run';

      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = 'groupType';
      input.value = checked.value;
      form.appendChild(input);
      var formParent = document.body || document.documentElement;
      if (!formParent) {
        return;
      }
      formParent.appendChild(form);
      form.submit();
    });
  }
})();

(function () {
  if (!window.mainPageConfig) {
    return;
  }

  var base = window.mainPageConfig.ctx || '';

  document.addEventListener('click', function (e) {
    var link = e.target && e.target.closest ? e.target.closest('a[href]') : null;
    if (!link) {
      return;
    }

    var href = link.getAttribute('href') || '';
    if (href === '/continue' || href.endsWith('/continue')) {
      e.preventDefault();
      window.location.href = base + '/game/continue';
    }
  });
})();

(function () {
  function handleHeroShopBuy(e) {
    var btn = e.target && e.target.closest ? e.target.closest('.js-hero-shop-buy') : null;
    if (!btn) {
      return;
    }
    var cfg = window.mainPageConfig;
    if (!cfg) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();

    var base = cfg.ctx || '';
    var itemName = btn.getAttribute('data-item-name');
    var price = parseInt(btn.getAttribute('data-price'), 10);

    if (!cfg.loggedIn) {
      alert('로그인 후 아이템을 구매할 수 있습니다.');
      window.location.href = base + '/login?redirect=' + encodeURIComponent('/main');
      return;
    }

    if (!itemName || !Number.isFinite(price) || price <= 0) {
      alert('상품 정보를 불러올 수 없습니다.');
      return;
    }

    if (!window.confirm(price + ' COIN으로 ' + itemName + ' 아이템을 구매하시겠어요?')) {
      return;
    }

    btn.disabled = true;
    fetch(base + '/market/buyItem', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemName: itemName, price: price })
    })
      .then(function (res) { return res.text(); })
      .then(function (raw) {
        var data;
        try {
          data = JSON.parse(raw);
        } catch (err) {
          data = { result: (raw || '').trim() };
        }

        if (data.result === 'success') {
          alert(itemName + ' 구매가 완료되었습니다.');
        } else if (data.result === 'lack') {
          alert('코인이 부족합니다.');
        } else if (data.result === 'logout') {
          alert('로그인이 필요합니다.');
          window.location.href = base + '/login?redirect=' + encodeURIComponent('/main');
        } else {
          alert('구매 처리에 실패했습니다.');
        }
      })
      .catch(function () {
        alert('서버 요청 중 오류가 발생했습니다.');
      })
      .finally(function () {
        btn.disabled = false;
      });
  }

  document.addEventListener('click', handleHeroShopBuy, true);
})();
