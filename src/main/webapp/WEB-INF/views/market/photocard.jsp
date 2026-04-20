<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="loggedIn" value="${not empty sessionScope.LOGIN_MEMBER}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>포토카드 뽑기 - NEXT DEBUT</title>
<%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
<script>
window.__GACHA_TRAINEE_PLACEHOLDER = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="480"><rect fill="#e8edf5" width="100%" height="100%"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#94a3b8" font-size="15" font-family="system-ui,sans-serif">IMG</text></svg>'
);
</script>
<style>
<%@ include file="/WEB-INF/views/fragments/gacha-photocard-styles.jspf" %>
</style>
</head>
<body class="page-main min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="gacha-page flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
  <div class="container mx-auto max-w-5xl gacha-inner">
    <section class="shop-shell gacha-shell px-7 py-9 md:px-10 md:py-11">

      <div class="board-filter-bar mb-6">
        <a href="${ctx}/market/shop" class="filter-chip ${empty marketTab or marketTab eq 'shop' ? 'is-active' : ''}">아이템</a>
        <a href="${ctx}/market/gacha" class="filter-chip ${marketTab eq 'gacha' ? 'is-active' : ''}">연습생 뽑기</a>
        <a href="${ctx}/market/photocard" class="filter-chip ${marketTab eq 'photocard' ? 'is-active' : ''}">포토카드 뽑기</a>
      </div>

      <div class="shop-head gacha-hero mb-2 relative">
        <div class="flex flex-wrap items-start gap-5 md:gap-8 min-w-0 flex-1">
          <div class="gacha-hero__visual shrink-0">
            <span class="gacha-orbit gacha-orbit--2" aria-hidden="true"></span>
            <span class="gacha-orbit" aria-hidden="true"></span>
            <div class="gacha-orbit-core" aria-hidden="true"><i class="fas fa-clone"></i></div>
          </div>
          <div class="min-w-0">
            <div class="shop-kicker text-[11px] tracking-[0.38em] uppercase font-orbitron mb-2 text-slate-500">PHOTOCARD</div>
            <h1 class="font-orbitron text-3xl md:text-[2.65rem] font-black leading-tight mb-3 gacha-title-gradient">포토카드 뽑기</h1>
            <p class="text-sm md:text-[15px] text-slate-600 leading-relaxed max-w-xl">
              연습생 뽑기와 같은 방식으로 <strong>슬롯 → 카드 뒤집기</strong>로 결과를 확인합니다.
              이미 보유한 조합이 나오면 <span class="text-slate-800 font-bold">중복</span>으로 표시되며 코인은 차감되지 않습니다.
              신규 획득 시 <span class="text-slate-800 font-bold">보유 최고 등급이 자동 장착</span>되며, 도감에서 바꿀 수 있습니다.
            </p>
            <div class="gacha-tier-pills" aria-hidden="true">
              <span class="gacha-tier-pill is-r">R</span>
              <span class="gacha-tier-pill is-sr">SR</span>
              <span class="gacha-tier-pill is-ssr">SSR</span>
            </div>
          </div>
        </div>
        <div class="flex flex-col items-stretch md:items-end gap-0 w-full md:w-auto">
          <div class="gacha-wallet-card">
            <div class="shop-coin text-base font-bold flex items-center gap-2 justify-between text-slate-900 mb-3">
              <span class="text-xs font-orbitron tracking-wider text-slate-500">보유 코인</span>
              <span class="flex items-center gap-1.5">
                <i class="fas fa-coins text-amber-500"></i>
                <span id="gachaCoinText">${empty currentCoin ? 0 : currentCoin}</span>
              </span>
            </div>
            <button type="button" class="coin-my-item-btn coin-charge-btn w-full justify-center" id="gachaCoinChargeBtn">
              <i class="fas fa-coins"></i>
              <span>코인 충전</span>
            </button>
            <c:if test="${not loggedIn}">
              <div class="gacha-guest-banner">
                <i class="fas fa-lock"></i>
                <span>로그인 후 이용할 수 있어요.</span>
                <a class="gacha-guest-banner__link" href="${ctx}/login?redirect=/market/photocard">로그인</a>
              </div>
            </c:if>
          </div>
        </div>
      </div>

      <div class="gacha-draw-stage">
        <div class="gacha-draw-stage__head">
          <span class="gacha-draw-stage__k">DRAW</span>
          <div class="line"></div>
          <span class="text-xs font-bold text-slate-400">포토카드 실행</span>
        </div>
        <p class="gacha-draw-stage__hint">1회 ${pullCost}코인 · 5회 ${photoCardPrice5}코인 · 10회 ${photoCardPrice10}코인. 1회만 중복이면 코인은 차감되지 않고, 5·10회는 묶음 가격이 먼저 차감된 뒤 각 장이 결정됩니다.</p>
        <div class="gacha-draw-grid gacha-draw-grid--triple">
          <button type="button" class="gacha-draw-card gacha-draw-card--single" id="btnPcPull1"
            ${not loggedIn ? 'disabled' : ''}>
            <span class="gacha-draw-card__left" aria-hidden="true"><i class="fas fa-image"></i></span>
            <span class="gacha-draw-card__right">
              <span class="gacha-draw-card__tag">SINGLE</span>
              <span class="gacha-draw-card__title">1회 뽑기</span>
              <span class="gacha-draw-card__price"><span class="num">${pullCost}</span>코인</span>
            </span>
          </button>
          <button type="button" class="gacha-draw-card gacha-draw-card--multi" id="btnPcPull5" data-pulls="5"
            ${not loggedIn ? 'disabled' : ''}>
            <span class="gacha-draw-card__left" aria-hidden="true"><i class="fas fa-layer-group"></i></span>
            <span class="gacha-draw-card__right">
              <span class="gacha-draw-card__tag">5연속 · BUNDLE</span>
              <span class="gacha-draw-card__title">5회 뽑기</span>
              <span class="gacha-draw-card__price"><span class="num">${photoCardPrice5}</span>코인</span>
            </span>
          </button>
          <button type="button" class="gacha-draw-card gacha-draw-card--multi" id="btnPcPull10" data-pulls="10"
            ${not loggedIn ? 'disabled' : ''}>
            <span class="gacha-draw-card__left" aria-hidden="true"><i class="fas fa-bolt"></i></span>
            <span class="gacha-draw-card__right">
              <span class="gacha-draw-card__tag">10연속 · BUNDLE</span>
              <span class="gacha-draw-card__title">10회 뽑기</span>
              <span class="gacha-draw-card__price"><span class="num">${photoCardPrice10}</span>코인</span>
            </span>
          </button>
        </div>
      </div>

    </section>
  </div>
</main>

<div id="gachaPullingDim" class="gacha-pulling" aria-hidden="true">
  <div class="gacha-pulling__bg" aria-hidden="true"></div>
  <div class="gacha-pulling__content">
    <div class="gacha-pulling__rings" aria-hidden="true">
      <span class="gacha-pulling__ring"></span>
      <span class="gacha-pulling__ring gacha-pulling__ring--2"></span>
    </div>
    <div class="gacha-pulling__icon" aria-hidden="true"><i class="fas fa-clone"></i></div>
    <p class="gacha-pulling__text">뽑는 중</p>
    <p class="gacha-pulling__sub">포토카드를 열고 있어요…</p>
  </div>
</div>

<div id="gachaModalDim" aria-hidden="true">
  <div id="gachaModalPanel" class="gacha-result-panel" role="dialog" aria-modal="true" aria-labelledby="gachaModalTitle">
    <div class="gacha-result-panel__head flex justify-between items-start gap-3">
      <div>
        <div id="gachaModalTitle" class="gacha-result-panel__title">포토카드 결과</div>
        <div id="gachaModalSub" class="gacha-result-panel__sub">슬롯을 멈춘 뒤 카드를 뒤집어 확인하세요</div>
      </div>
      <button type="button" class="text-2xl leading-none opacity-80 hover:opacity-100 transition-opacity" id="gachaModalClose" aria-label="닫기">&times;</button>
    </div>
    <div id="gachaResultBody" class="flex flex-col"></div>
    <div class="gacha-result-footer text-center">
      <button type="button" class="gacha-pull-btn gacha-pull-btn--skip mb-2 hidden" id="gachaBtnSkipAnim">연출 스킵</button>
      <button type="button" class="gacha-pull-btn gacha-pull-btn--stopall mb-2" id="gachaBtnStopAll">전체 멈춤</button>
      <p id="gachaFlipHint" class="gacha-flip-hint hidden text-[11px] text-slate-400 mb-2">카드를 눌러 앞면을 확인하세요</p>
      <button type="button" class="gacha-pull-btn" id="gachaModalOk" disabled>확인</button>
    </div>
  </div>
</div>

<div id="gachaSsrFlash" class="gacha-ssr-flash" aria-hidden="true"></div>

<script>
(function(){
  var CTX = window.__UNITX_CTX || '${ctx}';
  var PH = window.__GACHA_TRAINEE_PLACEHOLDER || '';
  var PULL_COST = ${pullCost != null ? pullCost : 100};
  var PRICE_5 = ${photoCardPrice5 != null ? photoCardPrice5 : 450};
  var PRICE_10 = ${photoCardPrice10 != null ? photoCardPrice10 : 850};
  var coinEl = document.getElementById('gachaCoinText');

  function priceForPulls(n){
    n = Number(n) || 1;
    if (n === 5) return PRICE_5;
    if (n === 10) return PRICE_10;
    return PULL_COST;
  }
  var dim = document.getElementById('gachaModalDim');
  var pullingDim = document.getElementById('gachaPullingDim');

  function setCoin(n){ if(coinEl) coinEl.textContent = n; }

  function escHtml(s){
    return String(s == null ? '' : s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  function badgeClass(g){
    g = (g||'').toUpperCase();
    if(['N','R','SR','SSR'].indexOf(g)>=0) return 'grade-badge '+g;
    return 'grade-badge N';
  }

  function prefersReducedMotion(){
    try {
      return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    } catch (e) { return false; }
  }

  function triggerSsrFlash(){
    if (prefersReducedMotion()) return;
    var flash = document.getElementById('gachaSsrFlash');
    if (!flash) return;
    flash.classList.remove('is-active');
    void flash.offsetWidth;
    flash.classList.add('is-active');
    flash.setAttribute('aria-hidden','false');
    setTimeout(function(){
      flash.classList.remove('is-active');
      flash.setAttribute('aria-hidden','true');
    }, 920);
  }

  var GACHA_SLOT_CELL_H = 72;
  var GACHA_SLOT_LEN = 30;
  var GACHA_SLOT_WIN_IDX = 14;

  function buildSlotStripHtml(){
    var i, parts = [];
    for (i = 0; i < GACHA_SLOT_LEN; i++) {
      parts.push(
        '<div class="gacha-slot-card" aria-hidden="true"><div class="gacha-slot-card__face"><span>?</span></div></div>'
      );
    }
    return parts.join('');
  }

  function stopSlotStrip(stripEl){
    if (!stripEl || stripEl.classList.contains('is-stopped') || stripEl.classList.contains('is-stopping')) return;
    stripEl.classList.add('is-stopping');
    var cellH = GACHA_SLOT_CELL_H;
    var cycleH = GACHA_SLOT_LEN * cellH;
    var finalY = -GACHA_SLOT_WIN_IDX * cellH;
    var fromY = finalY - cycleH;

    stripEl.classList.remove('gacha-slot-strip--spin');
    stripEl.style.animation = 'none';
    stripEl.style.transition = 'none';
    stripEl.style.transform = 'translate3d(0,' + fromY + 'px,0)';
    void stripEl.offsetHeight;
    stripEl.style.transition = 'transform 0.88s cubic-bezier(0.2, 0.82, 0.12, 1)';
    requestAnimationFrame(function(){
      requestAnimationFrame(function(){
        stripEl.style.transform = 'translate3d(0,' + finalY + 'px,0)';
      });
    });
    var settled = false;
    function finish(){
      if (settled) return;
      settled = true;
      stripEl.classList.remove('is-stopping');
      stripEl.classList.add('is-stopped');
      stripEl.style.transition = 'none';
      stripEl.style.transform = 'translate3d(0,' + finalY + 'px,0)';
      stripEl.style.removeProperty('transition');
      stripEl.removeEventListener('transitionend', onEnd);
      tryFinishSlotPhase();
    }
    function onEnd(e){
      if (e.propertyName !== 'transform') return;
      finish();
    }
    stripEl.addEventListener('transitionend', onEnd);
    setTimeout(function(){
      if (stripEl.classList.contains('is-stopping')) finish();
    }, 1100);
  }

  function buildPcFlipCardHtml(p, idx){
    var g = (p.grade||'R').toUpperCase();
    var isDup = p.duplicate === true || p.result === 'duplicate';
    var dup = isDup
      ? '<span class="gacha-result-dup">중복</span>'
      : '<span class="gacha-result-new">NEW</span>';
    var img = (p.imagePath && p.imagePath.charAt(0)==='/') ? (CTX + p.imagePath) : (CTX + '/' + (p.imagePath||''));
    if (p.traineeId) { img += (img.indexOf('?') >= 0 ? '&' : '?') + 'v=' + p.traineeId + '&pc=' + encodeURIComponent(g); }
    var foot = isDup
      ? (p._bundle ? '묶음 뽑기 · 이미 보유한 카드' : '코인 차감 없음 · 이미 보유한 카드입니다')
      : '최고 보유 등급 자동 장착 · 도감에서 변경 가능';
    return '<div class="gacha-flip-card-wrap" data-index="'+idx+'">'+
      '<div class="gacha-flip gacha-flip--manual" data-grade="'+escHtml(g)+'" data-index="'+idx+'">'+
      '<div class="gacha-flip__inner">'+
      '<div class="gacha-flip__face gacha-flip__face--back"><span>?</span></div>'+
      '<div class="gacha-flip__face gacha-flip__face--front">'+
      '<img src="'+img+'" alt="" loading="eager" decoding="async" onerror="this.onerror=null;this.src=\''+PH+'\'"/>'+
      '</div></div>'+
      '<div class="gacha-flip__tap-hint" aria-hidden="true">탭하여 뒤집기</div></div>'+
      '<div class="gacha-flip-card-meta" aria-hidden="true">'+
      '<span class="'+badgeClass(p.grade)+'">'+escHtml(g)+'</span> '+
      '<span class="gacha-result-row__name">'+escHtml(p.name||'')+'</span> '+dup+'<br/>'+
      escHtml(foot)+
      '</div></div>';
  }

  function updateGachaOkButton(){
    var ok = document.getElementById('gachaModalOk');
    if (!ok) return;
    var total = document.querySelectorAll('#gachaFlipPhase .gacha-flip--manual').length;
    var rev = document.querySelectorAll('#gachaFlipPhase .gacha-flip--manual.is-revealed').length;
    ok.disabled = total === 0 || rev < total;
  }

  function transitionToFlipPhase(force){
    var flipPh = document.getElementById('gachaFlipPhase');
    if (!force && flipPh && flipPh.classList.contains('is-active')) return;
    var slotPh = document.getElementById('gachaSlotPhase');
    var stopAll = document.getElementById('gachaBtnStopAll');
    var skipBtn = document.getElementById('gachaBtnSkipAnim');
    var hint = document.getElementById('gachaFlipHint');
    var body = document.getElementById('gachaResultBody');
    if (body) body.scrollTop = 0;
    if (slotPh) slotPh.classList.add('is-hidden');
    if (flipPh) flipPh.classList.add('is-active');
    if (stopAll) stopAll.classList.add('hidden');
    if (skipBtn) skipBtn.classList.add('hidden');
    if (hint) hint.classList.remove('hidden');
    updateGachaOkButton();
  }

  function skipGachaAnimation(){
    var strips = document.querySelectorAll('#gachaSlotPhase .gacha-slot-strip');
    var cellH = GACHA_SLOT_CELL_H;
    var finalY = -GACHA_SLOT_WIN_IDX * cellH;
    strips.forEach(function(strip){
      strip.classList.remove('gacha-slot-strip--spin');
      strip.classList.add('is-stopped');
      strip.style.animation = 'none';
      strip.style.transition = 'none';
      strip.style.transform = 'translate3d(0,' + finalY + 'px,0)';
    });
    var stops = document.querySelectorAll('#gachaSlotPhase .gacha-btn-slot-stop');
    stops.forEach(function(b){ b.disabled = true; });
    transitionToFlipPhase(true);
    var anySsr = false;
    document.querySelectorAll('#gachaFlipPhase .gacha-flip--manual').forEach(function(card){
      card.classList.add('is-revealed');
      var wrap = card.closest('.gacha-flip-card-wrap');
      if (wrap) {
        wrap.classList.add('is-revealed');
        var meta = wrap.querySelector('.gacha-flip-card-meta');
        if (meta) meta.setAttribute('aria-hidden','false');
      }
      if ((card.getAttribute('data-grade')||'').toUpperCase() === 'SSR') anySsr = true;
    });
    if (anySsr) triggerSsrFlash();
    var hint = document.getElementById('gachaFlipHint');
    if (hint) hint.classList.add('hidden');
    updateGachaOkButton();
    var ok = document.getElementById('gachaModalOk');
    if (ok) ok.disabled = false;
  }

  function onAllSlotsStopped(){
    transitionToFlipPhase();
  }

  function tryFinishSlotPhase(){
    var strips = document.querySelectorAll('#gachaSlotPhase .gacha-slot-strip');
    var all = Array.prototype.every.call(strips, function(s){ return s.classList.contains('is-stopped'); });
    if (all) onAllSlotsStopped();
  }

  function wireGachaSlotPhase(){
    var stops = document.querySelectorAll('#gachaSlotPhase .gacha-btn-slot-stop');
    var strips = document.querySelectorAll('#gachaSlotPhase .gacha-slot-strip');
    stops.forEach(function(btn, i){
      btn.addEventListener('click', function(){
        if (btn.disabled) return;
        var strip = strips[i];
        if (!strip || strip.classList.contains('is-stopped') || strip.classList.contains('is-stopping')) return;
        btn.disabled = true;
        stopSlotStrip(strip);
      });
    });
    var stopAll = document.getElementById('gachaBtnStopAll');
    if (stopAll) {
      stopAll.onclick = function(){
        var i = 0;
        function next(){
          if (i >= stops.length) return;
          var b = stops[i];
          i++;
          if (b && !b.disabled) b.click();
          if (i < stops.length) setTimeout(next, 130);
        }
        next();
      };
    }
  }

  function wireGachaFlipPhase(){
    var flipPh = document.getElementById('gachaFlipPhase');
    if (!flipPh) return;
    flipPh.addEventListener('click', function(e){
      var card = e.target.closest('.gacha-flip--manual');
      if (!card || !flipPh.contains(card)) return;
      if (card.classList.contains('is-revealed')) return;
      card.classList.add('is-revealed');
      var wrap = card.closest('.gacha-flip-card-wrap');
      if (wrap) {
        wrap.classList.add('is-revealed');
        var meta = wrap.querySelector('.gacha-flip-card-meta');
        if (meta) meta.setAttribute('aria-hidden','false');
      }
      var g = (card.getAttribute('data-grade')||'').toUpperCase();
      if (g === 'SSR') triggerSsrFlash();
      updateGachaOkButton();
    });
  }

  function initGachaResultInteraction(pulls, reduced){
    var ok = document.getElementById('gachaModalOk');
    var hint = document.getElementById('gachaFlipHint');
    var stopAll = document.getElementById('gachaBtnStopAll');
    var skipBtn = document.getElementById('gachaBtnSkipAnim');
    if (ok) ok.disabled = true;
    if (hint) hint.classList.add('hidden');
    if (skipBtn) {
      if (reduced) {
        skipBtn.classList.add('hidden');
        skipBtn.onclick = null;
      } else {
        skipBtn.classList.remove('hidden');
        skipBtn.onclick = skipGachaAnimation;
      }
    }
    if (stopAll) {
      stopAll.classList.remove('hidden');
      stopAll.disabled = false;
    }
    if (reduced) {
      transitionToFlipPhase(true);
      wireGachaFlipPhase();
      return;
    }
    wireGachaSlotPhase();
    wireGachaFlipPhase();
    try {
      var firstStop = document.querySelector('#gachaSlotPhase .gacha-btn-slot-stop');
      if (firstStop) setTimeout(function(){ try { firstStop.focus(); } catch (e) {} }, 80);
    } catch (e2) {}
  }

  function renderResults(pulls){
    var el = document.getElementById('gachaResultBody');
    if(!el) return;
    var rows = pulls || [];
    var reduced = prefersReducedMotion();
    var stripInner = buildSlotStripHtml();
    var slotRows = rows.map(function(p, idx){
      var stripCls = 'gacha-slot-strip' + (reduced ? ' is-stopped' : ' gacha-slot-strip--spin');
      var stripStyle = reduced ? (' style="transform:translate3d(0,-'+(GACHA_SLOT_WIN_IDX * GACHA_SLOT_CELL_H)+'px,0)"') : '';
      var btnDis = reduced ? ' disabled' : '';
      return '<div class="gacha-slot-row" data-slot-idx="'+idx+'">'+
        '<div class="gacha-slot-machine">'+
        '<div class="gacha-slot-window">'+
        '<div class="'+stripCls+'"'+stripStyle+'">'+stripInner+'</div>'+
        '</div></div>'+
        '<button type="button" class="gacha-btn-slot-stop"'+btnDis+'>멈춤</button>'+
        '</div>';
    }).join('');
    var n = rows.length;
    var slotPhaseCls = 'gacha-slot-phase' + (reduced ? ' is-hidden' : '') + (n >= 2 ? ' gacha-slot-phase--hrow' : '');
    var flipPhaseCls = 'gacha-flip-phase' + (reduced ? ' is-active' : '');
    var flipGrid = rows.map(function(p, idx){ return buildPcFlipCardHtml(p, idx); }).join('');
    var flipGridCls = 'gacha-flip-grid' + (n === 1 ? ' gacha-flip-grid--single' : '');
    var flipGridStyle = n === 1 ? '' : (' style="--gacha-cols:'+n+'"');
    el.innerHTML =
      '<div id="gachaSlotPhase" class="'+slotPhaseCls+'">'+slotRows+'</div>'+
      '<div id="gachaFlipPhase" class="'+flipPhaseCls+'"><div class="'+flipGridCls+'"'+flipGridStyle+'>'+flipGrid+'</div></div>';
    initGachaResultInteraction(rows, reduced);
  }

  function openModal(){
    dim.classList.add('is-open');
    dim.setAttribute('aria-hidden','false');
    document.body.classList.add('modal-open');
    var okBtn = document.getElementById('gachaModalOk');
    try {
      if (document.activeElement && typeof document.activeElement.blur === 'function') {
        document.activeElement.blur();
      }
    } catch (e) {}
    if (okBtn && !okBtn.disabled) {
      setTimeout(function () { try { okBtn.focus(); } catch (e2) {} }, 320);
    }
  }
  function closeModal(){
    dim.classList.remove('is-open');
    dim.setAttribute('aria-hidden','true');
    document.body.classList.remove('modal-open');
    var flash = document.getElementById('gachaSsrFlash');
    if (flash) {
      flash.classList.remove('is-active');
      flash.setAttribute('aria-hidden','true');
    }
  }

  document.getElementById('gachaModalClose')?.addEventListener('click', closeModal);
  document.getElementById('gachaModalOk')?.addEventListener('click', closeModal);
  dim?.addEventListener('click', function(e){ if(e.target===dim) closeModal(); });

  document.addEventListener('keydown', function (e) {
    if (e.key !== 'Enter') return;
    if (!dim || !dim.classList.contains('is-open')) return;
    var okBtn = document.getElementById('gachaModalOk');
    if (okBtn && okBtn.disabled) return;
    e.preventDefault();
    e.stopPropagation();
    closeModal();
  }, true);

  function showPulling(){
    if (!pullingDim) return;
    pullingDim.classList.add('is-open');
    pullingDim.setAttribute('aria-hidden','false');
    document.body.classList.add('modal-open');
  }
  function hidePulling(){
    if (!pullingDim) return;
    pullingDim.classList.remove('is-open');
    pullingDim.setAttribute('aria-hidden','true');
    if (!dim || !dim.classList.contains('is-open')) {
      document.body.classList.remove('modal-open');
    }
  }

  async function doPcPull(pulls){
    pulls = Number(pulls) || 1;
    var need = priceForPulls(pulls);
    var cur = parseInt((coinEl && coinEl.textContent) ? coinEl.textContent : '0', 10);
    if(isNaN(cur) || cur < need){
      alert('코인이 부족합니다.');
      return;
    }
    showPulling();
    try{
      var res = await fetch(CTX + '/market/photocard/pull', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ pulls: pulls })
      });
      var data = await res.json();
      hidePulling();
      if(data.result === 'logout'){ alert('로그인이 필요합니다.'); location.href = CTX + '/login?redirect=/market/photocard'; return; }
      if(data.result === 'lack'){ setCoin(data.currentCoin); alert('코인이 부족합니다.'); return; }
      if(data.result !== 'success' && data.result !== 'duplicate'){
        alert(data.message || '처리할 수 없습니다.');
        return;
      }
      if (typeof data.currentCoin === 'number') setCoin(data.currentCoin);
      var rows;
      var bundle = pulls > 1;
      if (data.lines && data.lines.length) {
        rows = data.lines.map(function(line){
          return {
            grade: line.grade,
            name: line.traineeName || line.displayName || '',
            imagePath: line.imagePath,
            duplicate: line.result === 'duplicate',
            traineeId: line.traineeId,
            result: line.result,
            _bundle: bundle
          };
        });
      } else {
        rows = [{
          grade: data.grade,
          name: data.traineeName || data.displayName || '',
          imagePath: data.imagePath,
          duplicate: data.result === 'duplicate',
          traineeId: data.traineeId,
          result: data.result
        }];
      }
      renderResults(rows);
      openModal();
    }catch(e){
      hidePulling();
      alert('요청 중 오류가 발생했습니다.');
    }
  }

  document.getElementById('btnPcPull1')?.addEventListener('click', function(){ doPcPull(1); });
  document.getElementById('btnPcPull5')?.addEventListener('click', function(){ doPcPull(5); });
  document.getElementById('btnPcPull10')?.addEventListener('click', function(){ doPcPull(10); });
})();

document.addEventListener('DOMContentLoaded', function () {
  var chargeBtn = document.getElementById('gachaCoinChargeBtn');
  var kakaoModal = document.getElementById('gachaKakaoPayModal');
  var closeBtn = document.getElementById('gachaKakaoCloseBtn');
  var payBtn = document.getElementById('gachaKakaoPayBtn');
  var chargeItems = document.querySelectorAll('#gachaKakaoPayModal .charge-item');
  var selectedAmount = 0;
  var base = (window.__UNITX_CTX || '${ctx}');

  if (chargeBtn && kakaoModal) {
    chargeBtn.addEventListener('click', function () {
      kakaoModal.classList.add('show');
    });
  }
  if (closeBtn && kakaoModal) {
    closeBtn.addEventListener('click', function () {
      kakaoModal.classList.remove('show');
    });
  }
  if (chargeItems.length) {
    chargeItems.forEach(function (item) {
      item.addEventListener('click', function () {
        chargeItems.forEach(function (i) { i.classList.remove('active'); });
        this.classList.add('active');
        selectedAmount = Number(this.dataset.amount);
      });
    });
  }
  if (payBtn && kakaoModal) {
    payBtn.addEventListener('click', function () {
      if (!selectedAmount) {
        alert('금액을 선택하세요.');
        return;
      }
      fetch(base + '/kakao/ready', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ amount: selectedAmount })
      })
        .then(async function (res) {
          var text = await res.text();
          if (!res.ok) throw new Error(text);
          return JSON.parse(text);
        })
        .then(function (data) {
          if (data.result === 'logout') {
            alert('로그인이 필요합니다.');
            location.href = base + '/login?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
            return;
          }
          if (data.result === 'success' && data.redirectUrl) {
            location.href = data.redirectUrl;
            return;
          }
          alert('결제 준비 중 오류가 발생했습니다.');
        })
        .catch(function (err) {
          console.error(err);
          alert('서버 요청 중 오류가 발생했습니다.');
        });
    });
  }
});
</script>

<div id="gachaKakaoPayModal" class="kakao-modal">
  <div class="kakao-modal-content">
    <div class="kakao-modal-header">
      <span>코인 충전</span>
      <button type="button" id="gachaKakaoCloseBtn">&times;</button>
    </div>
    <div class="kakao-modal-body">
      <p>충전할 금액을 선택하세요</p>
      <div class="charge-list">
        <button type="button" class="charge-item" data-amount="1000">1,000 코인</button>
        <button type="button" class="charge-item" data-amount="5000">5,000 코인</button>
        <button type="button" class="charge-item" data-amount="10000">10,000 코인</button>
        <button type="button" class="charge-item" data-amount="100000">100,000 코인</button>
      </div>
      <button type="button" id="gachaKakaoPayBtn" class="kakao-pay-btn">카카오페이 결제하기</button>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
