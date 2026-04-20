
(function(){
  var STORAGE_KEY = 'unitx_bgm_state_v4';
  var cfg = window.__UNITX_BGM_CONFIG || {};
  var tracks = Array.isArray(cfg.tracks) ? cfg.tracks.filter(function(t){ return t && t.src; }) : [];
  if (!tracks.length) return;

  var state = loadState();
  var audio = null;
  var ui = {};

  function loadState(){
    var base = {
      enabled: true,
      playing: true,
      volume: typeof cfg.defaultVolume === 'number' ? cfg.defaultVolume : 0.28,
      trackIndex: 0,
      currentTime: 0,
      volumeOpen: false,
      panelOpen: false
    };
    try {
      var saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}');
      if (typeof saved.enabled === 'boolean') base.enabled = saved.enabled;
      if (typeof saved.playing === 'boolean') base.playing = saved.playing;
      if (typeof saved.volume === 'number') base.volume = clampVolume(saved.volume);
      if (typeof saved.trackIndex === 'number') base.trackIndex = normalizeIndex(saved.trackIndex);
      if (typeof saved.currentTime === 'number' && saved.currentTime >= 0) base.currentTime = saved.currentTime;
      if (typeof saved.panelOpen === 'boolean') base.panelOpen = saved.panelOpen;
    } catch(e) {}
    return base;
  }

  function saveState(){
    if (!audio) return;
    state.currentTime = audio.currentTime || 0;
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      enabled: state.enabled,
      playing: state.playing,
      volume: state.volume,
      trackIndex: state.trackIndex,
      currentTime: state.currentTime,
      panelOpen: state.panelOpen
    }));
  }

  function normalizeIndex(idx){
    if (!tracks.length) return 0;
    return ((idx % tracks.length) + tracks.length) % tracks.length;
  }

  function clampVolume(v){
    if (isNaN(v)) return 0.28;
    return Math.max(0, Math.min(1, v));
  }

  function chooseRandomTrack(exclude){
    if (tracks.length <= 1) return 0;
    var next = exclude;
    while (next === exclude) next = Math.floor(Math.random() * tracks.length);
    return next;
  }

  function currentTrack(){
    return tracks[normalizeIndex(state.trackIndex)];
  }

  function prevTrackIndex(){
    return normalizeIndex(state.trackIndex - 1);
  }

  function setTrack(index, resetTime){
    state.trackIndex = normalizeIndex(index);
    var track = currentTrack();
    if (audio.src !== track.src) {
      audio.src = track.src;
      audio.load();
    }
    state.currentTime = resetTime ? 0 : Math.max(0, state.currentTime || 0);
    updateTicker(track.title || 'UNKNOWN TRACK');
    updateUi();
  }

  function requestPlay(options){
    options = options || {};
    if (!state.enabled) return Promise.resolve(false);
    state.playing = true;
    audio.muted = !!options.mutedBoot;
    audio.volume = clampVolume(state.volume);
    var playPromise = audio.play();
    if (playPromise && typeof playPromise.then === 'function') {
      return playPromise.then(function(){
        if (options.mutedBoot) {
          window.setTimeout(function(){
            audio.muted = false;
            audio.volume = clampVolume(state.volume);
          }, 180);
        }
        updateUi();
        return true;
      }).catch(function(){
        audio.muted = false;
        updateUi();
        return false;
      });
    }
    updateUi();
    return Promise.resolve(!audio.paused);
  }

  function pauseAudio(markManual){
    audio.pause();
    if (markManual) state.playing = false;
    updateUi();
  }

  function createMarkup(){
    return ''
      + '<div class="bgm-floating" id="bgmFloating">'
      + '  <div class="bgm-floating__title"><div class="bgm-floating__track" id="bgmTickerTrack"></div></div>'
      + '  <div class="bgm-floating__controls">'
      + '    <button type="button" class="bgm-floating__btn" id="bgmPrevBtn" title="이전 곡"><i class="fas fa-backward-step"></i></button>'
      + '    <button type="button" class="bgm-floating__btn bgm-floating__btn--play" id="bgmPlayBtn" title="재생/일시정지"><i class="fas fa-play"></i></button>'
      + '    <button type="button" class="bgm-floating__btn" id="bgmNextBtn" title="다음 곡"><i class="fas fa-forward-step"></i></button>'
      + '    <div class="bgm-floating__volume-wrap">'
      + '      <button type="button" class="bgm-floating__btn" id="bgmVolumeBtn" title="볼륨"><i class="fas fa-volume-high"></i></button>'
      + '      <div class="bgm-floating__volume-pop" id="bgmVolumePop">'
      + '        <input type="range" min="0" max="100" step="1" value="28" class="bgm-floating__volume-slider" id="bgmVolumeSlider">'
      + '      </div>'
      + '    </div>'
      + '  </div>'
      + '</div>';
  }

  function initUi(){
    var old = document.getElementById('bgmFloating');
    if (old) old.remove();
    var slot = document.getElementById('heroBgmSlot')
      || document.getElementById('gameTopBgmSlot')
      || document.getElementById('globalBgmSlot')
      || document.getElementById('navBgmSlot');
    if (slot) {
      slot.insertAdjacentHTML('afterbegin', createMarkup());
    } else {
      document.body.insertAdjacentHTML('beforeend', createMarkup());
    }
    ui.root = document.getElementById('bgmFloating');
    ui.tickerTrack = document.getElementById('bgmTickerTrack');
    ui.prevBtn = document.getElementById('bgmPrevBtn');
    ui.playBtn = document.getElementById('bgmPlayBtn');
    ui.nextBtn = document.getElementById('bgmNextBtn');
    ui.volumeBtn = document.getElementById('bgmVolumeBtn');
    ui.volumePop = document.getElementById('bgmVolumePop');
    ui.volume = document.getElementById('bgmVolumeSlider');
    ui.toggleBtn = document.getElementById('gameBgmToggleBtn');

    ui.prevBtn.addEventListener('click', function(){
      setTrack(prevTrackIndex(), true);
      requestPlay();
      saveState();
    });

    ui.playBtn.addEventListener('click', function(){
      if (audio.paused) requestPlay();
      else pauseAudio(true);
      saveState();
    });

    ui.nextBtn.addEventListener('click', function(){
      setTrack(chooseRandomTrack(state.trackIndex), true);
      requestPlay();
      saveState();
    });

    ui.volumeBtn.addEventListener('click', function(e){
      e.stopPropagation();
      state.volumeOpen = !state.volumeOpen;
      updateUi();
    });

    ui.volume.addEventListener('input', function(){
      state.volume = clampVolume(Number(ui.volume.value || 0) / 100);
      audio.volume = state.volume;
      updateUi();
      saveState();
    });

    if (ui.toggleBtn) {
      ui.toggleBtn.addEventListener('click', function(e){
        e.preventDefault();
        e.stopPropagation();
        state.panelOpen = !state.panelOpen;
        state.volumeOpen = false;
        updateUi();
        saveState();
      });
    }

    document.addEventListener('click', function(e){
      var clickedRoot = ui.root && ui.root.contains(e.target);
      var clickedToggle = ui.toggleBtn && ui.toggleBtn.contains(e.target);
      if (!clickedRoot && !clickedToggle) {
        state.volumeOpen = false;
        if (state.panelOpen) state.panelOpen = false;
        updateUi();
        saveState();
      }
    });
  }

  function updateTicker(title){
    if (!ui.tickerTrack) return;
    var safeTitle = String(title || 'UNKNOWN TRACK');
    ui.tickerTrack.innerHTML = '<span class="bgm-floating__text">' + safeTitle + '</span><span class="bgm-floating__text">' + safeTitle + '</span>';
  }

  function updateUi(){
    if (!ui.root || !audio) return;
    var isPlaying = state.enabled && !audio.paused;
    var panelOpen = !!state.panelOpen;
    ui.playBtn.innerHTML = isPlaying ? '<i class="fas fa-pause"></i>' : '<i class="fas fa-play"></i>';
    if (state.volume <= 0.001) ui.volumeBtn.innerHTML = '<i class="fas fa-volume-xmark"></i>';
    else if (state.volume < 0.5) ui.volumeBtn.innerHTML = '<i class="fas fa-volume-low"></i>';
    else ui.volumeBtn.innerHTML = '<i class="fas fa-volume-high"></i>';
    ui.volume.value = Math.round(clampVolume(state.volume) * 100);
    ui.volumePop.classList.toggle('is-open', !!state.volumeOpen && panelOpen);
    ui.root.classList.toggle('is-collapsed', !panelOpen);
    document.body.classList.toggle('bgm-panel-open', panelOpen);
    if (ui.toggleBtn) ui.toggleBtn.classList.toggle('is-open', panelOpen);
  }

  function bindLifecycle(seekToSavedThenBoot){
    audio.addEventListener('loadedmetadata', function(){
      seekToSavedThenBoot();
    });

    audio.addEventListener('play', function(){
      state.playing = true;
      updateUi();
      saveState();
    });

    audio.addEventListener('pause', function(){
      updateUi();
      saveState();
    });

    audio.addEventListener('ended', function(){
      setTrack(chooseRandomTrack(state.trackIndex), true);
      requestPlay();
    });

    // 이동/탭 전환 직전에 currentTime을 최대한 정확히 저장 (페이지 이동 후에도 이어듣기)
    setInterval(function(){ if (audio && !audio.paused) saveState(); }, 500);
    window.addEventListener('pagehide', function(){ try { saveState(); } catch(e) {} });
    window.addEventListener('beforeunload', function(){ try { saveState(); } catch(e) {} });
    window.addEventListener('focus', function(){ if (state.enabled && state.playing && audio.paused) requestPlay(); });
    document.addEventListener('visibilitychange', function(){
      if (document.hidden) {
        try { saveState(); } catch(e) {}
        return;
      }
      if (state.enabled && state.playing && audio.paused) requestPlay();
    });
  }

  document.addEventListener('DOMContentLoaded', function(){
    audio = new Audio();
    audio.preload = 'auto';
    audio.loop = false;
    audio.playsInline = true;
    audio.volume = clampVolume(state.volume);

    initUi();

    var bootedAfterMeta = false;
    function bootAutoPlay(){
      if (!state.enabled) return;
      requestPlay({ mutedBoot: true }).then(function(ok){ if (ok) saveState(); });
      window.setTimeout(function(){ requestPlay({ mutedBoot: true }); }, 250);
      window.setTimeout(function(){ requestPlay({ mutedBoot: true }); }, 1200);
    }
    function seekToSavedThenBoot(){
      if (state.currentTime > 0 && isFinite(audio.duration) && state.currentTime < audio.duration) {
        try { audio.currentTime = state.currentTime; } catch (e) {}
      }
      updateUi();
      if (!bootedAfterMeta) {
        bootedAfterMeta = true;
        bootAutoPlay();
      }
    }

    bindLifecycle(seekToSavedThenBoot);
    setTrack(state.trackIndex, false);
    updateUi();

    /* loadedmetadata가 늦을 때 대비 */
    window.setTimeout(function(){
      if (!bootedAfterMeta && audio && audio.readyState >= 1) {
        seekToSavedThenBoot();
      }
    }, 1800);

    var unlockOnce = function(){
      if (!state.enabled) return;
      requestPlay();
      document.removeEventListener('pointerdown', unlockOnce, true);
      document.removeEventListener('keydown', unlockOnce, true);
      document.removeEventListener('touchstart', unlockOnce, true);
    };
    document.addEventListener('pointerdown', unlockOnce, true);
    document.addEventListener('keydown', unlockOnce, true);
    document.addEventListener('touchstart', unlockOnce, true);
  });
})();
