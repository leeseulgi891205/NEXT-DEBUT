/**
 * /boards/map street scout interactions
 */
(function () {
  var boot = document.getElementById('castMapBoot');
  if (!boot) return;

  var CTX = boot.getAttribute('data-ctx') || '';
  var exploreUrl = boot.getAttribute('data-explore-url') || '';
  var loggedIn = boot.getAttribute('data-logged-in') === 'true';
  var hasKakao = boot.getAttribute('data-has-kakao') === 'true';

  var MSG = {
    needRegion: boot.getAttribute('data-msg-need-region') || '',
    spotFound: boot.getAttribute('data-msg-spot-found') || '',
    lackCoin: boot.getAttribute('data-msg-lack') || '',
    errorGeneric: boot.getAttribute('data-msg-error') || '',
    noneFound: boot.getAttribute('data-msg-none') || '',
    exploreError: boot.getAttribute('data-msg-explore-error') || ''
  };

  var UI = {
    modalTitle: boot.getAttribute('data-modal-title') || '',
    modalEyebrow: boot.getAttribute('data-modal-eyebrow') || 'SCOUT REPORT',
    previewTitle: boot.getAttribute('data-preview-title') || '',
    previewCatch: boot.getAttribute('data-preview-catch') || '',
    previewVibe: boot.getAttribute('data-preview-vibe') || '',
    previewTip: boot.getAttribute('data-preview-tip') || '',
    buffIdle: boot.getAttribute('data-buff-idle') || '',
    buffDivider: boot.getAttribute('data-buff-divider') || ' / '
  };

  var regionsEl = document.getElementById('castMapRegionsData');
  var REGION_MARKERS = [];
  try {
    if (regionsEl && regionsEl.textContent) {
      REGION_MARKERS = JSON.parse(regionsEl.textContent.trim());
    }
  } catch (e) {
    REGION_MARKERS = [];
  }

  var selectedId = null;
  var defaultExploreLabel = (document.getElementById('castExploreBtnLabel') || {}).textContent || '';
  var mapRef = null;
  var mapReady = false;
  var defaultCenter = null;
  var defaultLevel = 12;
  var focusLevel = 5;
  var pendingFocusId = null;

  function findRegion(id) {
    return REGION_MARKERS.find(function (x) { return x.id === id; }) || null;
  }

  function setText(id, value) {
    var el = document.getElementById(id);
    if (el) el.textContent = value;
  }

  function renderPreview(region) {
    if (!region) {
      setText('castPreviewTitle', UI.previewTitle);
      setText('castPreviewCatch', UI.previewCatch);
      setText('castPreviewVibe', UI.previewVibe);
      setText('castPreviewFocus', '-');
      setText('castPreviewPickup', '-');
      setText('castPreviewTip', UI.previewTip);
      return;
    }
    setText('castPreviewTitle', region.label);
    setText('castPreviewCatch', region.catchCopy || '');
    setText('castPreviewVibe', region.vibe || '');
    setText('castPreviewFocus', region.focusLabel || '-');
    setText('castPreviewPickup', region.pickupType || '-');
    setText('castPreviewTip', region.tip || UI.previewTip);
  }

  function focusRegionById(id) {
    if (!id) return;
    if (!mapRef || !mapReady) {
      pendingFocusId = id;
      return;
    }
    var region = findRegion(id);
    if (!region) return;

    var target = new kakao.maps.LatLng(region.lat, region.lng);
    try {
      mapRef.panTo(target);
      window.setTimeout(function () {
        try {
          mapRef.setLevel(focusLevel, {
            anchor: target,
            animate: { duration: 650 }
          });
        } catch (e2) {
          mapRef.setLevel(focusLevel);
        }
        window.setTimeout(function () {
          try {
            mapRef.panTo(target);
          } catch (e3) {}
        }, 120);
      }, 180);
    } catch (e) {
      mapRef.setCenter(target);
      mapRef.setLevel(focusLevel);
    }
  }

  function setSelected(id) {
    selectedId = id || null;
    var label = '-';
    var region = findRegion(selectedId);
    if (region) label = region.label;
    setText('castSelectedLabel', label);
    setText('castExploreBtnLabel', region ? (region.label + ' / ' + defaultExploreLabel) : defaultExploreLabel);
    renderPreview(region);

    document.querySelectorAll('#castRegionChips .cast-region-chip').forEach(function (btn) {
      var on = btn.getAttribute('data-region-id') === selectedId;
      btn.classList.toggle('is-on', on);
      btn.setAttribute('aria-pressed', on ? 'true' : 'false');
    });
  }

  document.querySelectorAll('#castRegionChips .cast-region-chip').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var regionId = btn.getAttribute('data-region-id');
      setSelected(regionId);
      focusRegionById(regionId);
    });
  });

  if (hasKakao && typeof kakao !== 'undefined' && kakao.maps) {
    kakao.maps.load(function () {
      var el = document.getElementById('castMapCanvas');
      if (!el) return;
      defaultCenter = new kakao.maps.LatLng(36.15, 127.85);

      mapRef = new kakao.maps.Map(el, {
        center: defaultCenter,
        level: defaultLevel
      });
      mapReady = true;

      REGION_MARKERS.forEach(function (m) {
        var pos = new kakao.maps.LatLng(m.lat, m.lng);
        var mk = new kakao.maps.Marker({ position: pos, map: mapRef });
        kakao.maps.event.addListener(mk, 'click', function () {
          setSelected(m.id);
          focusRegionById(m.id);
        });
      });

      function relayout() {
        try {
          mapRef.relayout();
        } catch (e2) {}
      }

      setTimeout(relayout, 150);
      setTimeout(relayout, 500);
      setTimeout(relayout, 1000);

      if (typeof ResizeObserver !== 'undefined') {
        var observer = new ResizeObserver(relayout);
        observer.observe(el);
      }

      window.addEventListener('resize', relayout);

      if (pendingFocusId) {
        focusRegionById(pendingFocusId);
        pendingFocusId = null;
      } else if (selectedId) {
        focusRegionById(selectedId);
      }
    });
  }

  function updateStatus(data) {
    if (!data) return;

    if (data.freeExploresLeftToday != null) {
      setText('castFreeLeft', String(data.freeExploresLeftToday));
      setText('castStatusFree', String(data.freeExploresLeftToday));
    }
    if (data.nextExploreCost != null) setText('castNextCost', String(data.nextExploreCost));
    if (data.currentCoin != null) setText('castStatusCoin', String(data.currentCoin));

    var activeBuff = data.activeBuff || null;
    var titleEl = document.getElementById('castBuffTitle');
    var metaEl = document.getElementById('castBuffMeta');
    var cardEl = document.querySelector('.cast-status-card--buff');
    if (titleEl && metaEl && cardEl) {
      if (activeBuff) {
        titleEl.textContent = activeBuff.spotLabel || UI.buffIdle;
        metaEl.textContent = (activeBuff.effectLine || '') + (activeBuff.expireAt ? UI.buffDivider + activeBuff.expireAt : '');
        cardEl.classList.add('is-live');
      } else {
        titleEl.textContent = UI.buffIdle;
        metaEl.textContent = UI.buffIdle;
        cardEl.classList.remove('is-live');
      }
    }
  }

  var dim = document.getElementById('castResultDim');
  var msg = document.getElementById('castResultMsg');
  var goGacha = document.getElementById('castGoGacha');
  var titleEl = document.getElementById('castResultTitle');
  var eyebrowEl = document.getElementById('castResultEyebrow');
  var detailEl = document.getElementById('castResultDetail');
  var spotEl = document.getElementById('castResultSpot');
  var effectEl = document.getElementById('castResultEffect');
  var expireEl = document.getElementById('castResultExpire');
  var iconEl = document.getElementById('castResultIcon');

  function openModal(options) {
    if (msg) msg.textContent = options.message || '';
    if (goGacha) goGacha.style.display = options.showGacha ? 'inline-flex' : 'none';
    if (titleEl) titleEl.textContent = options.title || UI.modalTitle;
    if (eyebrowEl) eyebrowEl.textContent = options.eyebrow || UI.modalEyebrow;
    if (iconEl) {
      iconEl.classList.toggle('is-rare', options.rarity === 'rare');
      iconEl.classList.toggle('is-fail', options.rarity === 'fail');
      iconEl.innerHTML = options.rarity === 'fail'
        ? '<i class="fas fa-user-secret text-slate-500"></i>'
        : options.rarity === 'rare'
          ? '<i class="fas fa-bolt text-amber-500"></i>'
          : '<i class="fas fa-wand-magic-sparkles text-violet-600"></i>';
    }
    if (detailEl) {
      if (options.detail) {
        detailEl.hidden = false;
        if (spotEl) spotEl.textContent = options.detail.spotLabel || '-';
        if (effectEl) effectEl.textContent = options.detail.effectLine || '-';
        if (expireEl) expireEl.textContent = options.detail.expireAt || '-';
      } else {
        detailEl.hidden = true;
      }
    }
    if (dim) {
      dim.classList.add('is-open');
      document.body.classList.add('modal-open');
      requestAnimationFrame(function () {
        dim.classList.add('is-visible');
      });
    }
  }

  function closeModal() {
    if (!dim) return;
    dim.classList.remove('is-visible');
    setTimeout(function () {
      dim.classList.remove('is-open');
      document.body.classList.remove('modal-open');
    }, 280);
  }

  var closeBtn = document.getElementById('castResultClose');
  if (closeBtn) closeBtn.addEventListener('click', closeModal);
  if (dim) {
    dim.addEventListener('click', function (e) {
      if (e.target === dim) closeModal();
    });
  }

  var exploreBtn = document.getElementById('castExploreBtn');
  if (exploreBtn) {
    exploreBtn.addEventListener('click', async function () {
      if (!loggedIn) {
        location.href = CTX + '/login?redirect=/boards/map';
        return;
      }
      if (!selectedId) {
        alert(MSG.needRegion);
        return;
      }

      exploreBtn.disabled = true;
      exploreBtn.classList.add('is-loading');
      try {
        var res = await fetch(exploreUrl, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ regionId: selectedId })
        });
        var data = await res.json();
        updateStatus(data);

        if (data.result === 'logout') {
          location.href = CTX + '/login?redirect=/boards/map';
          return;
        }
        if (data.result === 'lack') {
          openModal({
            title: UI.modalTitle,
            eyebrow: 'BUDGET CHECK',
            message: data.message || MSG.lackCoin,
            rarity: 'fail',
            showGacha: false
          });
          return;
        }
        if (data.result === 'error') {
          openModal({
            title: UI.modalTitle,
            eyebrow: 'SYSTEM ALERT',
            message: data.message || MSG.errorGeneric,
            rarity: 'fail',
            showGacha: false
          });
          return;
        }
        if (data.discovery === 'none') {
          openModal({
            title: UI.modalTitle,
            eyebrow: 'NO CONTACT',
            message: data.message || MSG.noneFound,
            rarity: 'fail',
            showGacha: false
          });
          return;
        }

        var buff = data.buff || {};
        openModal({
          title: MSG.spotFound,
          eyebrow: buff.discoveryLabel || UI.modalEyebrow,
          message: data.message || '',
          rarity: buff.rarityClass || data.discovery,
          showGacha: true,
          detail: {
            spotLabel: buff.spotLabel || '-',
            effectLine: buff.effectLine || '-',
            expireAt: buff.expireAt || '-'
          }
        });
      } catch (e) {
        openModal({
          title: UI.modalTitle,
          eyebrow: 'SYSTEM ALERT',
          message: MSG.exploreError,
          rarity: 'fail',
          showGacha: false
        });
      } finally {
        exploreBtn.disabled = false;
        exploreBtn.classList.remove('is-loading');
      }
    });
  }

  renderPreview(null);
})();
