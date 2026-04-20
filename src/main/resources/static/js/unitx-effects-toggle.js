/**
 * 저사양 환경용: 배경 캔버스·파티클·커서 등 장식 효과 끄기 (설정은 localStorage에 저장)
 */
(function () {
  var STORAGE_KEY = 'unitx_fx_off';

  function isFxOff() {
    return document.documentElement.classList.contains('fx-off');
  }

  function syncHeroMotion() {
    var off = isFxOff();
    window.__unitxFxOff = off;
    if (!window.__heroMotionState) {
      window.__heroMotionState = { paused: off };
    } else {
      window.__heroMotionState.paused = off;
    }
  }

  function updateFxButton() {
    var btn = document.getElementById('unitxFxToggleBtn');
    var icon = document.getElementById('unitxFxToggleIcon');
    if (!btn) return;
    var off = isFxOff();
    btn.setAttribute('aria-pressed', off ? 'true' : 'false');
    btn.title = off ? '배경 효과 켜기' : '배경 효과 끄기';
    btn.setAttribute('aria-label', off ? '배경 효과 켜기' : '배경 효과 끄기');
    if (icon) {
      icon.className = 'fas ' + (off ? 'fa-bolt' : 'fa-gauge-simple');
    }
    btn.classList.toggle('is-fx-off', off);
  }

  function apply() {
    try {
      var off = localStorage.getItem(STORAGE_KEY) === '1';
      document.documentElement.classList.toggle('fx-off', off);
      try {
        if (document.body) document.body.classList.toggle('fx-off', off);
      } catch (e3) {}
      syncHeroMotion();
      updateFxButton();
    } catch (e) {}
  }

  function bindFxButton() {
    var btn = document.getElementById('unitxFxToggleBtn');
    if (!btn || btn.dataset.fxBound === '1') return;
    btn.dataset.fxBound = '1';
    btn.addEventListener('click', function (ev) {
      ev.preventDefault();
      if (typeof window.toggleUnitxEffects === 'function') window.toggleUnitxEffects();
    });
  }

  window.toggleUnitxEffects = function () {
    try {
      var next = localStorage.getItem(STORAGE_KEY) !== '1';
      localStorage.setItem(STORAGE_KEY, next ? '1' : '0');
      apply();
      try {
        window.dispatchEvent(new CustomEvent('unitxFxChange'));
      } catch (e2) {}
    } catch (e) {}
  };

  window.isUnitxFxOff = isFxOff;

  apply();
  bindFxButton();
  document.addEventListener('DOMContentLoaded', function () {
    apply();
    bindFxButton();
  });
})();
