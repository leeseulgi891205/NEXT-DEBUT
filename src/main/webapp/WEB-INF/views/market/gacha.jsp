<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="loggedIn" value="${not empty sessionScope.LOGIN_MEMBER}" />

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>뽑기 - NEXT DEBUT</title>
<%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
<script>
/* 로드 실패 시 m01로 바꾸면 전원 같은 사진처럼 보임 → 중립 플레이스홀더만 사용 */
window.__GACHA_TRAINEE_PLACEHOLDER = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="480"><rect fill="#e8edf5" width="100%" height="100%"/><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="#94a3b8" font-size="15" font-family="system-ui,sans-serif">IMG</text></svg>'
);
</script>
<style>
.shop-shell{
    position: relative;
    border-radius: 30px;
    overflow: hidden;
    background: var(--card);
    border: 1px solid var(--card-border);
    box-shadow: 0 26px 90px rgba(99,102,241,0.10);
}
.board-filter-bar { display:flex; flex-wrap:wrap; align-items:center; gap:8px; }
.board-filter-bar .filter-chip {
    display:inline-flex; align-items:center; gap:6px; padding:8px 18px; border-radius:999px; font-size:13px; font-weight:600;
    border:1px solid rgba(148,163,184,0.35); background:rgba(255,255,255,0.55); color:rgba(71,85,105,0.88);
    text-decoration:none; transition:all 200ms ease;
}
.board-filter-bar .filter-chip:hover { background:rgba(255,255,255,0.92); color:#0f172a; }
.board-filter-bar .filter-chip.is-active {
    background:linear-gradient(135deg,rgba(233,176,196,0.88),rgba(204,186,216,0.85));
    color:rgba(20,10,30,0.95); border-color:transparent;
}
.gacha-prob-btn{
    font-size:10px; letter-spacing:0.06em; padding:4px 10px; border-radius:999px;
    border:1px solid rgba(148,163,184,0.45); background:rgba(255,255,255,0.5); color:rgba(71,85,105,0.75);
    cursor:pointer; font-weight:700;
}
.gacha-prob-btn:hover{ background:rgba(255,255,255,0.9); color:#0f172a; }
.gacha-pull-btn{
    min-width:140px; padding:14px 22px; border-radius:999px; font-weight:900; font-size:14px; letter-spacing:0.04em;
    border:1px solid rgba(232,164,184,0.45);
    background:linear-gradient(135deg,rgba(233,176,196,0.95),rgba(196,181,253,0.55));
    color:rgba(20,10,30,0.92); transition:transform .15s ease, box-shadow .15s ease;
}
.gacha-pull-btn:hover:not(:disabled){ transform:translateY(-2px); box-shadow:0 12px 28px rgba(232,164,184,0.28); }
.gacha-pull-btn:disabled{ opacity:.45; cursor:not-allowed; transform:none; box-shadow:none; }
.grade-badge{
    display:inline-flex; align-items:center; justify-content:center; min-width:36px; padding:2px 8px; border-radius:8px;
    font-size:11px; font-weight:900; font-family:"Orbitron",sans-serif; letter-spacing:0.06em;
}
.grade-badge.N{ background:linear-gradient(135deg,#94a3b8,#64748b); color:#fff; }
.grade-badge.R{ background:linear-gradient(135deg,#34d399,#059669); color:#fff; }
.grade-badge.SR{ background:linear-gradient(135deg,#a78bfa,#7c3aed); color:#fff; }
.grade-badge.SSR{ background:linear-gradient(135deg,#fde047,#f59e0b); color:#422006; }
#gachaModalDim{ display:none; position:fixed; inset:0; z-index:10060;
  background:rgba(15,23,42,.28);
  backdrop-filter:blur(10px); align-items:center; justify-content:center; padding:20px;
  transition:opacity .35s ease;
}
#gachaModalDim.is-open{ display:flex; }
#gachaModalPanel.gacha-result-panel{
    max-width:min(560px,100%); max-height:min(92vh,820px);
    border-radius:26px; padding:0;
    background:linear-gradient(180deg,#fff 0%,rgba(253,250,252,.98) 55%,rgba(248,245,252,.96) 100%);
    border:1px solid rgba(232,164,184,.38);
    box-shadow:
      0 0 0 1px rgba(255,255,255,.85) inset,
      0 28px 70px rgba(30,27,75,.12),
      0 12px 36px rgba(99,102,241,.08);
    position:relative;
    display:flex;
    flex-direction:column;
    overflow:hidden;
}
#gachaModalPanel.gacha-result-panel::before{
  content:""; position:absolute; inset:0; pointer-events:none; border-radius:inherit;
  background:
    radial-gradient(ellipse 90% 55% at 50% -15%, rgba(233,176,196,.22), transparent 52%),
    linear-gradient(180deg, rgba(255,255,255,.65), transparent 42%);
}
#gachaModalPanel.gacha-result-panel .gacha-result-panel__head{
  position:relative; padding:22px 22px 14px;
  border-bottom:1px solid rgba(148,163,184,.2);
  flex-shrink:0;
}
#gachaModalPanel.gacha-result-panel .gacha-result-panel__title{
  font-family:"Orbitron",sans-serif; font-size:1.05rem; font-weight:900;
  letter-spacing:.12em; color:#1e293b;
}
#gachaModalPanel.gacha-result-panel .gacha-result-panel__sub{
  font-size:11px; color:rgba(71,85,105,.78); margin-top:6px; letter-spacing:.04em;
}
#gachaModalPanel.gacha-result-panel #gachaModalClose{
  color:rgba(100,116,139,.75);
}
#gachaModalPanel.gacha-result-panel #gachaModalClose:hover{ color:#0f172a; }
#gachaModalPanel.gacha-result-panel #gachaResultBody{
  position:relative; padding:16px 18px 12px; gap:0;
  flex:1 1 auto;
  min-height:0;
  min-width:0;
  overflow-y:auto;
  overflow-x:hidden;
  -webkit-overflow-scrolling:touch;
}
#gachaModalPanel.gacha-result-panel .gacha-result-footer{
  position:relative; padding:16px 18px 22px;
  border-top:1px solid rgba(148,163,184,.2);
  flex-shrink:0;
  background:linear-gradient(180deg,rgba(255,255,255,.92),#fff);
  box-shadow:0 -8px 24px rgba(248,250,252,.9);
}
#gachaModalPanel.gacha-result-panel .gacha-pull-btn:disabled{
  opacity:.48; cursor:not-allowed; filter:grayscale(.25);
}
#gachaModalPanel.gacha-result-panel .gacha-pull-btn--stopall{
  background:linear-gradient(135deg,#6366f1,#4f46e5);
  border:1px solid rgba(255,255,255,.18);
  box-shadow:0 8px 24px rgba(99,102,241,.35);
  min-width:180px;
}

/* 슬롯 + 수동 뒤집기 */
#gachaResultBody .gacha-slot-phase{ padding-bottom:6px; }
#gachaResultBody .gacha-flip-phase{ display:none; }
#gachaResultBody .gacha-flip-phase.is-active{
  display:block;
  width:100%;
  min-width:0;
}
#gachaResultBody .gacha-slot-phase.is-hidden{ display:none !important; visibility:hidden !important; height:0 !important; overflow:hidden !important; padding:0 !important; margin:0 !important; }
#gachaModalPanel #gachaSlotPhase.gacha-slot-phase.is-hidden{ display:none !important; }

.gacha-slot-row{
  display:flex; align-items:stretch; gap:10px; margin-bottom:12px;
  animation:gachaSlotRowEnter .38s ease backwards;
}
@keyframes gachaSlotRowEnter{
  from{ transform:translateY(6px); }
  to{ transform:none; }
}
.gacha-slot-row:nth-child(1){ animation-delay:0s; }
.gacha-slot-row:nth-child(2){ animation-delay:.04s; }
.gacha-slot-row:nth-child(3){ animation-delay:.08s; }
.gacha-slot-row:nth-child(4){ animation-delay:.12s; }
.gacha-slot-row:nth-child(5){ animation-delay:.16s; }

.gacha-slot-machine{
  flex:1; min-width:0;
  border-radius:14px; padding:10px;
  background:rgba(255,255,255,.72);
  border:1px solid rgba(203,213,225,.55);
  box-shadow:0 1px 0 rgba(255,255,255,.9) inset;
}
.gacha-slot-window{
  height:72px; overflow:hidden; border-radius:10px;
  border:1px solid rgba(148,163,184,.35);
  background:linear-gradient(180deg,#f1f5f9,#e2e8f0);
  box-shadow:inset 0 2px 8px rgba(15,23,42,.06);
}
.gacha-slot-strip{
  will-change:transform;
  transform:translateZ(0);
}
.gacha-slot-strip--spin{
  animation:gachaSlotMarquee 3.4s linear infinite;
}
.gacha-slot-strip.is-stopped{
  animation:none !important;
}
@keyframes gachaSlotMarquee{
  0%{ transform:translate3d(0,0,0); }
  100%{ transform:translate3d(0,-2160px,0); }
}
/* 슬롯: 세로 카드 비율 — 높이·한 바퀴 픽셀은 JS GACHA_SLOT_CELL_H 와 동기 */
.gacha-slot-card{
  height:72px; box-sizing:border-box; padding:4px 0;
  border-bottom:1px solid rgba(148,163,184,.12);
  display:flex; align-items:center; justify-content:center;
}
.gacha-slot-card__face{
  width:52px;
  height:64px;
  max-height:100%;
  border-radius:10px;
  display:flex; align-items:center; justify-content:center;
  background:linear-gradient(145deg,#6366f1 0%,#4c1d95 42%,#312e81 100%);
  border:1px solid rgba(251,191,36,.35);
  box-shadow:inset 0 0 12px rgba(255,255,255,.08), 0 2px 6px rgba(30,27,75,.15);
  position:relative; overflow:hidden;
}
.gacha-slot-card__face::after{
  content:""; position:absolute; inset:0;
  background:linear-gradient(105deg,transparent 38%,rgba(255,255,255,.2) 50%,transparent 62%);
  animation:gachaSlotSheen 2.4s ease-in-out infinite;
  opacity:.45;
}
@keyframes gachaSlotSheen{
  0%,100%{ opacity:.28; }
  50%{ opacity:.72; }
}
.gacha-slot-card__face span{
  font-family:"Orbitron",sans-serif; font-size:1.05rem; font-weight:900;
  color:rgba(253,230,138,.92);
  text-shadow:0 0 16px rgba(250,204,21,.45);
  position:relative; z-index:1;
}

.gacha-btn-slot-stop{
  flex-shrink:0; align-self:center;
  padding:10px 14px; border-radius:12px; cursor:pointer; font-weight:900; font-size:12px;
  border:1px solid rgba(232,164,184,.55);
  background:linear-gradient(135deg,rgba(255,255,255,.95),rgba(253,242,248,.9));
  color:#6b21a8; white-space:nowrap;
  box-shadow:0 2px 8px rgba(99,102,241,.1);
}
.gacha-btn-slot-stop:hover{ filter:brightness(1.02); box-shadow:0 4px 12px rgba(99,102,241,.14); }
.gacha-btn-slot-stop:disabled{ opacity:.45; cursor:not-allowed; }

/* 연속 뽑기: 슬롯 가로 한 줄 — 숨김일 때는 아래 :not(.is-hidden)보다 약해야 함 */
#gachaModalPanel #gachaSlotPhase.gacha-slot-phase--hrow:not(.is-hidden){
  display:flex !important;
  flex-direction:row !important;
  flex-wrap:nowrap !important;
  gap:6px;
  justify-content:space-between;
  align-items:flex-start;
  padding:4px 2px 8px;
  overflow-x:hidden;
  width:100%;
  min-width:0;
}
#gachaModalPanel #gachaSlotPhase.gacha-slot-phase--hrow:not(.is-hidden) .gacha-slot-row{
  flex:1 1 0;
  min-width:0;
  max-width:none;
  width:0;
  flex-direction:column;
  margin-bottom:0;
  gap:6px;
  animation:none !important;
}
#gachaModalPanel #gachaSlotPhase.gacha-slot-phase--hrow:not(.is-hidden) .gacha-slot-machine{
  width:100%;
  padding:6px;
}
#gachaModalPanel #gachaSlotPhase.gacha-slot-phase--hrow:not(.is-hidden) .gacha-btn-slot-stop{
  width:100%;
  padding:8px 4px;
  font-size:10px;
  white-space:normal;
  line-height:1.15;
}

/* 결과: 1회 = 큰 카드 1장 / 복수 = 그리드(열 수 --gacha-cols) — width:0 금지(1회 붕괴 방지) */
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid:not(.gacha-flip-grid--single){
  display:grid !important;
  grid-template-columns:repeat(var(--gacha-cols, 5), minmax(0, 1fr));
  gap:10px;
  width:100%;
  min-width:0;
  padding:10px 0 12px;
  overflow-x:hidden;
  box-sizing:border-box;
  align-items:start;
}
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid:not(.gacha-flip-grid--single) .gacha-flip-card-wrap{
  width:100%;
  min-width:0;
}
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid.gacha-flip-grid--single{
  display:flex !important;
  flex-direction:row !important;
  justify-content:center !important;
  align-items:flex-start;
  padding:12px 8px 8px;
  width:100%;
}
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid.gacha-flip-grid--single .gacha-flip-card-wrap{
  flex:0 1 auto;
  width:100%;
  max-width:min(300px, 92vw);
  min-width:220px;
}
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid.gacha-flip-grid--single .gacha-flip.gacha-flip--manual{
  max-width:100%;
  min-height:260px;
  max-height:min(58vh, 520px);
}
/* 같은 요소에 .gacha-flip + .gacha-flip--manual — 세로 카드 비율, inner에 filter 금지(앞면 회색 버그 방지) */
.gacha-flip.gacha-flip--manual{
  position:relative;
  cursor:pointer;
  width:100%;
  max-width:min(120px,100%);
  min-width:0;
  margin:0 auto;
  height:auto;
  aspect-ratio:3 / 4;
  max-height:min(42vh,320px);
  perspective:900px;
}
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid:not(.gacha-flip-grid--single) .gacha-flip.gacha-flip--manual{
  max-width:100%;
  max-height:min(42vh, 260px);
  min-height:140px;
}
.gacha-flip.gacha-flip--manual .gacha-flip__inner{
  position:absolute;
  inset:0;
  width:100%;
  height:100%;
}
.gacha-flip--manual.is-revealed .gacha-flip__inner{
  transform:rotateY(0deg) translateZ(0);
}
.gacha-flip--manual .gacha-flip__face--back{
  background:linear-gradient(145deg,#6366f1,#4c1d95 48%,#312e81);
  position:relative; overflow:hidden;
}
.gacha-flip--manual:not(.is-revealed) .gacha-flip__face--back::after{
  content:""; position:absolute; inset:0;
  background:linear-gradient(110deg,transparent 40%,rgba(255,255,255,.14) 50%,transparent 60%);
  animation:gachaFlipSheen 2.8s ease-in-out infinite;
  opacity:.4;
  pointer-events:none;
}
.gacha-flip--manual.is-revealed .gacha-flip__face--back::after{ display:none; }
@keyframes gachaFlipSheen{
  0%,100%{ opacity:.28; }
  50%{ opacity:.55; }
}
.gacha-flip--manual .gacha-flip__tap-hint{
  position:absolute; bottom:8px; left:0; right:0; text-align:center;
  font-size:10px; color:rgba(91,33,182,.88); font-weight:800;
  pointer-events:none;
  text-shadow:0 1px 0 rgba(255,255,255,.6);
}
.gacha-flip--manual.is-revealed .gacha-flip__tap-hint{ display:none; }
.gacha-flip.gacha-flip--manual .gacha-flip__face--front img{
  width:100%;
  height:100%;
  min-height:120px;
  object-fit:cover;
  background:#e8edf5;
}
.gacha-flip-card-wrap{ display:flex; flex-direction:column; align-items:center; }
#gachaModalPanel #gachaFlipPhase .gacha-flip-grid:not(.gacha-flip-grid--single) .gacha-flip-card-meta{
  font-size:10px;
  line-height:1.35;
  word-break:keep-all;
}
.gacha-flip-card-meta{
  margin-top:8px; font-size:11px; color:rgba(51,65,85,.92);
  text-align:center; opacity:0; max-height:0; overflow:hidden;
  transition:opacity .35s ease .08s, max-height .35s ease;
}
.gacha-flip-card-wrap.is-revealed .gacha-flip-card-meta{
  opacity:1; max-height:120px;
}
.gacha-flip-hint{ color:rgba(71,85,105,.88) !important; }
#gachaModalPanel.gacha-result-panel .gacha-flip-card-meta .gacha-result-row__name{ color:#0f172a; }

/* ── 뽑기 진행 오버레이 ── */
#gachaPullingDim.gacha-pulling{
  display:none; position:fixed; inset:0; z-index:10058;
  align-items:center; justify-content:center; padding:24px;
  background:radial-gradient(ellipse 90% 70% at 50% 40%, rgba(49,46,129,.55), rgba(15,23,42,.94));
  backdrop-filter:blur(8px);
}
#gachaPullingDim.gacha-pulling.is-open{ display:flex; }
.gacha-pulling__bg{
  position:absolute; inset:0; opacity:.4; pointer-events:none;
  background-image:
    radial-gradient(2px 2px at 20% 30%, rgba(255,255,255,.9), transparent),
    radial-gradient(2px 2px at 80% 20%, rgba(255,255,255,.6), transparent),
    radial-gradient(1px 1px at 50% 80%, rgba(255,255,255,.5), transparent);
  background-size:200px 200px, 240px 240px, 180px 180px;
  animation:gachaStars 20s linear infinite;
}
@keyframes gachaStars{ to{ transform:translateY(24px); } }
.gacha-pulling__content{ position:relative; text-align:center; max-width:320px; }
.gacha-pulling__rings{ position:relative; width:140px; height:140px; margin:0 auto 20px; }
.gacha-pulling__ring{
  position:absolute; inset:0; border-radius:50%;
  border:2px solid rgba(244,114,182,.35);
  animation:gachaPullSpin 2.2s linear infinite;
}
.gacha-pulling__ring--2{
  inset:-12px; border-color:rgba(167,139,250,.25);
  animation-duration:3.4s; animation-direction:reverse;
}
@keyframes gachaPullSpin{ to{ transform:rotate(360deg); } }
.gacha-pulling__icon{
  position:absolute; left:50%; top:50%; transform:translate(-50%,-50%);
  width:72px; height:72px; border-radius:20px;
  display:flex; align-items:center; justify-content:center;
  font-size:1.75rem; color:#fff;
  background:linear-gradient(145deg,#f472b6,#a78bfa 60%,#6366f1);
  box-shadow:0 12px 40px rgba(236,72,153,.45), 0 0 0 4px rgba(255,255,255,.12) inset;
  animation:gachaPullPulse 1.1s ease-in-out infinite;
}
@keyframes gachaPullPulse{
  0%,100%{ transform:translate(-50%,-50%) scale(1); }
  50%{ transform:translate(-50%,-50%) scale(1.06); }
}
.gacha-pulling__text{
  font-family:"Orbitron",sans-serif; font-size:13px; font-weight:800; letter-spacing:.35em;
  color:#fce7f3; margin:0 0 8px;
}
.gacha-pulling__sub{ font-size:12px; color:rgba(226,232,240,.65); margin:0; }

/* ── 결과 카드 행 ── */
.gacha-result-row{
  position:relative; margin-bottom:12px; border-radius:16px; overflow:hidden;
  opacity:0;
  animation:gachaRowIn .55s cubic-bezier(.22,1,.36,1) forwards;
}
@keyframes gachaRowIn{
  from{ opacity:0; transform:translateY(18px) scale(.96); }
  to{ opacity:1; transform:translateY(0) scale(1); }
}
.gacha-result-row__shine{
  position:absolute; inset:0; pointer-events:none; opacity:0;
  background:linear-gradient(105deg, transparent 40%, rgba(255,255,255,.12) 50%, transparent 60%);
  animation:gachaShine 1.8s ease-in-out infinite;
}
.gacha-result-row--ssr .gacha-result-row__shine{ opacity:1; animation-duration:2.4s; }
@keyframes gachaShine{
  0%,100%{ transform:translateX(-100%); }
  50%{ transform:translateX(100%); }
}
.gacha-result-row__body{
  position:relative; padding:12px 12px;
  background:linear-gradient(135deg, rgba(255,255,255,.07), rgba(255,255,255,.02));
  border:1px solid rgba(255,255,255,.1);
  border-radius:16px;
}
.gacha-result-row--ssr .gacha-result-row__body{
  border-color:rgba(250,204,21,.45);
  box-shadow:0 0 28px rgba(250,204,21,.15), inset 0 0 20px rgba(250,204,21,.06);
  animation:gachaSsrGlow 2s ease-in-out infinite;
}
.gacha-result-row--sr .gacha-result-row__body{
  border-color:rgba(167,139,250,.4);
  box-shadow:0 0 20px rgba(139,92,246,.12);
}
@keyframes gachaSsrGlow{
  0%,100%{ box-shadow:0 0 28px rgba(250,204,21,.15), inset 0 0 20px rgba(250,204,21,.06); }
  50%{ box-shadow:0 0 36px rgba(250,204,21,.28), inset 0 0 24px rgba(250,204,21,.1); }
}
.gacha-result-row__meta{
  min-width:0; flex:1; display:flex; flex-direction:column; justify-content:center; gap:4px;
  opacity:0;
  animation:gachaMetaReveal .42s ease forwards;
}
.gacha-result-row__name{ font-weight:900; color:#f8fafc; font-size:15px; letter-spacing:-.02em; }
.gacha-result-row__duprow{ display:flex; align-items:center; flex-wrap:wrap; gap:8px; row-gap:4px; }
.gacha-result-dup{
  font-size:10px; font-weight:800; padding:2px 8px; border-radius:999px;
  background:rgba(251,191,36,.2); color:#fcd34d; border:1px solid rgba(251,191,36,.35);
}
.gacha-result-new{
  font-size:10px; font-weight:800; padding:2px 8px; border-radius:999px;
  background:rgba(52,211,153,.18); color:#6ee7b7; border:1px solid rgba(52,211,153,.35);
}
.gacha-result-row__own{ font-size:11px; color:rgba(148,163,184,.85); }

#gachaModalPanel.gacha-result-panel .gacha-pull-btn{
  min-width:200px;
  background:linear-gradient(135deg,#f472b6,#a855f7 55%,#6366f1);
  border:1px solid rgba(255,255,255,.2);
  color:#fff;
  box-shadow:0 10px 32px rgba(168,85,247,.35);
}
#gachaModalPanel.gacha-result-panel .gacha-pull-btn:hover:not(:disabled){
  box-shadow:0 14px 40px rgba(236,72,153,.4);
}

/* SSR 풀스크린 플래시 (짧게) */
#gachaSsrFlash.gacha-ssr-flash{
  position:fixed; inset:0; z-index:10061; pointer-events:none;
  opacity:0; visibility:hidden;
  background:
    radial-gradient(ellipse 85% 70% at 50% 42%, rgba(254,252,232,.98), rgba(250,204,21,.45) 38%, transparent 72%),
    radial-gradient(circle at 50% 50%, rgba(255,255,255,.5), transparent 55%);
}
#gachaSsrFlash.gacha-ssr-flash.is-active{
  visibility:visible;
  animation:gachaSsrFlashAnim .88s ease-out forwards;
}
@keyframes gachaSsrFlashAnim{
  0%{ opacity:0; filter:brightness(1.4) saturate(1.2); }
  22%{ opacity:1; }
  100%{ opacity:0; filter:brightness(1) saturate(1); visibility:hidden; }
}

/* 결과 카드 이미지 3D 뒤집기 */
.gacha-flip{
  width:72px; height:92px; flex-shrink:0;
  perspective:720px;
}
.gacha-flip__inner{
  position:relative; width:100%; height:100%;
  transform-style:preserve-3d;
  transition:transform .68s cubic-bezier(.2,.85,.25,1);
  transform:rotateY(180deg);
}
.gacha-flip__inner.is-flipped{
  transform:rotateY(0deg);
}
.gacha-flip__face{
  position:absolute; inset:0; border-radius:12px; overflow:hidden;
  backface-visibility:hidden;
  -webkit-backface-visibility:hidden;
}
.gacha-flip__face--front{
  transform:rotateY(0deg);
  box-shadow:0 8px 20px rgba(0,0,0,.35);
  border:1px solid rgba(255,255,255,.12);
}
.gacha-flip__face--front img{
  width:100%; height:100%; object-fit:cover; display:block; background:#e8edf5;
}
.gacha-flip__face--back{
  transform:rotateY(180deg);
  background:
    linear-gradient(145deg,#3730a3 0%,#1e1b4b 45%,#0f172a 100%);
  border:1px solid rgba(250,204,21,.22);
  box-shadow:inset 0 0 36px rgba(0,0,0,.45);
  display:flex; align-items:center; justify-content:center;
}
.gacha-flip__face--back span{
  font-family:"Orbitron",sans-serif; font-size:1.65rem; font-weight:900;
  color:rgba(253,230,138,.9);
  text-shadow:0 0 24px rgba(250,204,21,.5);
}
@keyframes gachaMetaReveal{
  from{ opacity:0; transform:translateX(6px); }
  to{ opacity:1; transform:translateX(0); }
}

@media (prefers-reduced-motion: reduce){
  .gacha-result-row{ animation-duration:.01s; }
  .gacha-pulling__ring, .gacha-pulling__bg, .gacha-pulling__icon, .gacha-result-row__shine, .gacha-result-row--ssr .gacha-result-row__body{ animation:none !important; }
  .gacha-result-row{ opacity:1; transform:none; }
  #gachaSsrFlash.gacha-ssr-flash.is-active{ animation:none !important; opacity:0 !important; visibility:hidden !important; }
  .gacha-result-row .gacha-flip__inner{ transform:rotateY(0deg) !important; transition:none !important; }
  .gacha-result-row__meta{ opacity:1 !important; animation:none !important; transform:none !important; }
  .gacha-slot-strip--spin{ animation:none !important; }
  .gacha-slot-card__face::after{ animation:none !important; opacity:.35 !important; }
  .gacha-flip--manual:not(.is-revealed) .gacha-flip__inner{ transform:rotateY(180deg) !important; transition:none !important; }
  .gacha-flip--manual:not(.is-revealed) .gacha-flip__face--back::after{ animation:none !important; opacity:.35 !important; }
  .gacha-flip--manual.is-revealed .gacha-flip__inner{ transform:rotateY(0deg) !important; transition:none !important; }
}
#gachaProbModalDim{ display:none; position:fixed; inset:0; z-index:10070; background:rgba(15,23,42,0.4); backdrop-filter:blur(4px); align-items:center; justify-content:center; padding:20px; }
#gachaProbModalDim.is-open{ display:flex; }
.owned-card{
    border-radius:16px; border:1px solid rgba(148,163,184,0.22); overflow:hidden;
    background:rgba(255,255,255,0.65);
}
.owned-card img{ width:100%; aspect-ratio:3/4; object-fit:cover; background:#e8edf5; }
body.modal-open{ overflow:hidden !important; }

/* 상점과 동일 톤: 충전 버튼 */
.coin-my-item-btn{
    position:relative;display:inline-flex;align-items:center;gap:8px;padding:10px 18px;border-radius:999px;
    border:1px solid rgba(255,255,255,0.6);font-size:15px;font-weight:900;letter-spacing:0.04em;cursor:pointer;
    background:linear-gradient(135deg,rgba(255,255,255,0.85),rgba(255,245,250,0.65),rgba(255,255,255,0.85));
    color:#7c3aed;
    box-shadow:0 6px 20px rgba(244,114,182,0.15),inset 0 1px 0 rgba(255,255,255,0.7);
    backdrop-filter:blur(10px);transition:all .18s ease;
}
.coin-my-item-btn:hover{ transform:translateY(-2px); box-shadow:0 12px 28px rgba(244,114,182,0.25),inset 0 1px 0 rgba(255,255,255,0.9); }
.coin-charge-btn{
    color:#b45309;
    background:linear-gradient(135deg,rgba(255,255,255,0.9),rgba(255,237,213,0.85),rgba(255,255,255,0.9));
    border:1px solid rgba(251,146,60,0.4);
    box-shadow:0 6px 20px rgba(251,146,60,0.25),inset 0 1px 0 rgba(255,255,255,0.7);
}
.coin-charge-btn:hover{ box-shadow:0 12px 30px rgba(251,146,60,0.35),inset 0 1px 0 rgba(255,255,255,0.9); }
.coin-charge-btn i{ color:#f97316; }
.coin-charge-btn span{ color:#ea580c; }

/* 카카오페이 충전 패널 (상점과 동일) */
.kakao-modal{ position:fixed; top:0; right:-420px; width:400px; max-width:90%; height:100vh; background:rgba(0,0,0,0.35);
  backdrop-filter:blur(6px); transition:right .35s ease; z-index:99999; }
.kakao-modal.show{ right:0; }
.kakao-modal-content{ position:absolute; top:0; right:0; width:100%; height:100%; background:#fff;
  box-shadow:-8px 0 24px rgba(0,0,0,0.18); padding:24px 20px; box-sizing:border-box; }
.kakao-modal-header{ display:flex; justify-content:space-between; align-items:center; font-size:20px; font-weight:700; margin-bottom:20px; }
#gachaKakaoCloseBtn{ border:none; background:none; font-size:28px; cursor:pointer; }
.kakao-modal-body p{ margin:0 0 16px; font-size:15px; color:#444; }
.charge-list{ display:flex; flex-direction:column; gap:10px; margin-bottom:20px; }
.charge-item{ width:100%; padding:14px; border:1px solid #ddd; border-radius:12px; background:#f8f8f8; cursor:pointer; font-weight:700; }
.charge-item.active{ background:#fff9cc; border:2px solid #f7d000; }
.kakao-pay-btn{ width:100%; padding:15px; border:none; border-radius:12px; background:#ffe812; font-weight:800; font-size:16px; cursor:pointer; }

/* ── 뽑기 페이지 분위기 (연출 모달 제외 UI만) ── */
.gacha-page{
  position:relative;
  isolation:isolate;
  overflow:hidden;
}
.gacha-page::before{
  content:"";
  position:absolute; inset:-100px -40px 40%;
  background:
    radial-gradient(ellipse 90% 55% at 50% -15%, rgba(244,114,182,.28), transparent 58%),
    radial-gradient(ellipse 55% 45% at 85% 15%, rgba(167,139,250,.22), transparent 52%),
    radial-gradient(ellipse 45% 40% at 12% 25%, rgba(251,207,232,.4), transparent 50%);
  pointer-events:none; z-index:0;
}
.gacha-page::after{
  content:"";
  position:absolute; inset:0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(255,255,255,.35) 0, transparent 1px),
    radial-gradient(circle at 80% 70%, rgba(255,255,255,.2) 0, transparent 1px);
  background-size:120px 120px, 180px 180px;
  opacity:.45; pointer-events:none; z-index:0;
  animation:gacha-drift 28s linear infinite;
}
@keyframes gacha-drift{
  0%{ transform:translate(0,0); }
  50%{ transform:translate(-12px,8px); }
  100%{ transform:translate(0,0); }
}
.gacha-page .gacha-inner{ position:relative; z-index:1; }

.gacha-shell{
  position:relative;
  border-radius:32px;
  box-shadow:
    0 28px 100px rgba(99,102,241,.12),
    0 0 0 1px rgba(255,255,255,.5) inset;
  background:
    linear-gradient(165deg, rgba(255,255,255,.97) 0%, rgba(253,250,255,.92) 45%, rgba(250,245,255,.88) 100%);
}
.gacha-shell::before{
  content:"";
  position:absolute; inset:0; border-radius:inherit; pointer-events:none;
  background:
    linear-gradient(135deg, rgba(233,176,196,.14) 0%, transparent 42%),
    linear-gradient(315deg, rgba(196,181,253,.12) 0%, transparent 38%);
}
.gacha-shell::after{
  content:"";
  position:absolute; top:-2px; left:8%; right:8%; height:1px;
  background:linear-gradient(90deg, transparent, rgba(244,114,182,.35), rgba(167,139,250,.35), transparent);
  border-radius:999px; pointer-events:none;
}

.gacha-hero{
  display:grid;
  gap:1.25rem;
  margin-bottom:2rem;
  align-items:start;
}
@media(min-width:768px){
  .gacha-hero{ grid-template-columns:minmax(0,1fr) auto; align-items:center; gap:2rem; }
}
.gacha-hero__visual{
  position:relative;
  display:flex; align-items:center; justify-content:center;
  min-height:120px;
}
@media(min-width:768px){ .gacha-hero__visual{ min-height:140px; justify-content:flex-start; padding-left:.5rem; } }
@media(max-width:640px){
  .gacha-hero__visual{ min-height:92px; }
  .gacha-orbit{ width:88px; height:88px; }
  .gacha-orbit--2{ width:104px; height:104px; }
  .gacha-orbit-core{ width:64px; height:64px; font-size:1.4rem; border-radius:18px; }
}
.gacha-orbit{
  position:absolute; width:112px; height:112px; border-radius:50%;
  border:2px solid rgba(244,114,182,.25);
  animation:gacha-spin 18s linear infinite;
}
.gacha-orbit--2{
  width:140px; height:140px; border-color:rgba(167,139,250,.2);
  animation-duration:26s; animation-direction:reverse;
}
@keyframes gacha-spin{ to{ transform:rotate(360deg); } }
.gacha-orbit-core{
  position:relative; z-index:2;
  width:76px; height:76px; border-radius:22px;
  display:flex; align-items:center; justify-content:center;
  font-size:1.75rem;
  color:#fff;
  background:linear-gradient(145deg,#f472b6,#a78bfa 55%,#818cf8);
  box-shadow:
    0 16px 40px rgba(244,114,182,.4),
    0 0 0 4px rgba(255,255,255,.5) inset;
}
.gacha-tier-pills{
  display:flex; flex-wrap:wrap; gap:6px; margin-top:.75rem;
}
.gacha-tier-pill{
  font-size:10px; font-weight:900; font-family:"Orbitron",sans-serif;
  letter-spacing:.08em; padding:4px 10px; border-radius:999px;
  border:1px solid rgba(148,163,184,.25);
  background:rgba(255,255,255,.7); color:#64748b;
}
.gacha-tier-pill.is-n{ background:linear-gradient(135deg,#94a3b8,#64748b); color:#fff; border-color:transparent; }
.gacha-tier-pill.is-r{ background:linear-gradient(135deg,#34d399,#059669); color:#fff; border-color:transparent; }
.gacha-tier-pill.is-sr{ background:linear-gradient(135deg,#a78bfa,#7c3aed); color:#fff; border-color:transparent; }
.gacha-tier-pill.is-ssr{
  background:linear-gradient(135deg,#fde047,#f59e0b); color:#422006; border-color:transparent;
}

.gacha-title-gradient{
  background:linear-gradient(100deg,#1e293b 0%,#db2777 38%,#a855f7 72%,#6366f1 100%);
  -webkit-background-clip:text; background-clip:text; -webkit-text-fill-color:transparent;
}

.gacha-draw-stage{
  position:relative;
  margin-bottom:2.5rem;
  padding:1.5rem 1.25rem 1.75rem;
  border-radius:24px;
  border:1px solid rgba(232,164,184,.28);
  background:
    linear-gradient(180deg, rgba(255,255,255,.85), rgba(253,242,248,.55)),
    radial-gradient(120% 80% at 50% 0%, rgba(244,114,182,.1), transparent 55%);
  box-shadow:0 18px 50px rgba(99,102,241,.08);
}
.gacha-draw-stage__head{
  display:flex; align-items:center; gap:10px; margin-bottom:1.25rem;
}
.gacha-draw-stage__head .line{ flex:1; height:1px; background:linear-gradient(90deg, rgba(148,163,184,.35), transparent); }
.gacha-draw-stage__k{
  font-size:11px; font-family:"Orbitron",sans-serif; letter-spacing:.32em; color:#94a3b8; font-weight:700;
}
.gacha-draw-stage__hint{
  margin:-4px 0 14px;
  font-size:12px;
  color:#64748b;
  line-height:1.5;
}

.gacha-wallet-card{
  width:100%;
  max-width:280px;
  margin-left:auto;
  padding:14px 16px;
  border-radius:18px;
  border:1px solid rgba(244,114,182,.22);
  background:linear-gradient(165deg, rgba(255,255,255,.92), rgba(253,242,248,.75));
  box-shadow:0 8px 28px rgba(99,102,241,.08);
}
@media(max-width:767px){
  .gacha-wallet-card{ max-width:none; margin-left:0; }
}
.gacha-guest-banner{
  display:flex; flex-wrap:wrap; align-items:center; gap:8px 12px;
  margin-top:10px; padding:10px 12px;
  border-radius:12px;
  font-size:12px; font-weight:700;
  color:#9f1239;
  background:linear-gradient(135deg, rgba(254,242,242,.95), rgba(255,241,242,.88));
  border:1px solid rgba(251,113,133,.35);
}
.gacha-guest-banner i{ opacity:.85; }
.gacha-guest-banner__link{
  margin-left:auto;
  padding:6px 12px;
  border-radius:999px;
  font-size:11px; font-weight:800;
  text-decoration:none;
  color:#fff;
  background:linear-gradient(135deg,#f472b6,#db2777);
  border:1px solid rgba(255,255,255,.4);
}
.gacha-guest-banner__link:hover{ filter:brightness(1.05); }
@media(max-width:420px){
  .gacha-guest-banner{ flex-direction:column; align-items:stretch; }
  .gacha-guest-banner__link{ margin-left:0; text-align:center; width:100%; }
}

.gacha-draw-grid{
  display:grid;
  gap:12px;
  grid-template-columns:1fr;
}
@media(min-width:640px){
  .gacha-draw-grid{ grid-template-columns:1fr 1fr; gap:14px; }
}

.gacha-draw-card{
  position:relative;
  overflow:hidden;
  display:flex;
  flex-direction:row;
  align-items:center;
  gap:14px;
  text-align:left;
  min-height:0;
  padding:14px 16px;
  border-radius:18px;
  border:1px solid rgba(244,114,182,.35);
  cursor:pointer;
  font:inherit;
  color:#0f172a;
  background:linear-gradient(155deg, rgba(255,255,255,.95) 0%, rgba(253,242,248,.9) 100%);
  box-shadow:0 10px 32px rgba(236,72,153,.12);
  transition:transform .18s ease, box-shadow .18s ease, border-color .18s ease;
}
.gacha-draw-card__left{
  flex-shrink:0;
  width:52px; height:52px;
  border-radius:16px;
  display:flex; align-items:center; justify-content:center;
  font-size:1.35rem;
  color:#fff;
  background:linear-gradient(145deg,#f472b6,#ec4899);
  box-shadow:0 6px 16px rgba(236,72,153,.35), inset 0 1px 0 rgba(255,255,255,.35);
}
.gacha-draw-card--multi .gacha-draw-card__left{
  background:linear-gradient(145deg,#a78bfa,#6366f1);
  box-shadow:0 6px 16px rgba(129,140,248,.4), inset 0 1px 0 rgba(255,255,255,.35);
}
.gacha-draw-card__right{
  flex:1;
  min-width:0;
  display:flex;
  flex-direction:column;
  align-items:flex-start;
  gap:2px;
}
.gacha-draw-card::before{
  content:"";
  position:absolute; inset:-40% 50% 40% -30%;
  background:linear-gradient(120deg, transparent 30%, rgba(255,255,255,.55) 48%, transparent 62%);
  transform:translateX(-100%);
  transition:transform .65s ease;
  pointer-events:none;
}
.gacha-draw-card:hover:not(:disabled)::before{ transform:translateX(100%); }
.gacha-draw-card:hover:not(:disabled){
  transform:translateY(-3px);
  box-shadow:0 18px 44px rgba(236,72,153,.2);
  border-color:rgba(244,114,182,.55);
}
.gacha-draw-card:disabled{
  opacity:.48; cursor:not-allowed; transform:none; box-shadow:none;
}
.gacha-draw-card--multi{
  border-color:rgba(167,139,250,.4);
  background:linear-gradient(155deg, rgba(255,255,255,.96) 0%, rgba(245,243,255,.92) 100%);
  box-shadow:0 10px 32px rgba(129,140,248,.14);
}
.gacha-draw-card--multi:hover:not(:disabled){
  border-color:rgba(129,140,248,.55);
  box-shadow:0 18px 44px rgba(129,140,248,.22);
}
.gacha-draw-card__tag{
  font-size:9px; font-weight:900; letter-spacing:.12em; font-family:"Orbitron",sans-serif;
  color:#db2777;
}
.gacha-draw-card--multi .gacha-draw-card__tag{ color:#6d28d9; }
.gacha-draw-card__title{ font-size:1rem; font-weight:900; letter-spacing:-.02em; line-height:1.25; color:#0f172a; }
.gacha-draw-card__price{
  font-size:12px; color:#64748b; font-weight:600;
}
.gacha-draw-card__price .num{
  font-size:1.05rem; font-weight:900; color:#0f172a; font-family:"Orbitron",sans-serif;
  margin-right:3px;
}

.gacha-empty-owned{
  text-align:center;
  padding:28px 20px 32px;
  border-radius:16px;
  border:2px dashed rgba(148,163,184,.45);
  background:rgba(248,250,252,.65);
}
.gacha-empty-owned__icon{
  font-size:2rem;
  margin-bottom:10px;
  background:linear-gradient(135deg,#cbd5e1,#94a3b8);
  -webkit-background-clip:text; background-clip:text;
  -webkit-text-fill-color:transparent;
}
.gacha-empty-owned__t{ font-size:14px; font-weight:800; color:#475569; margin:0 0 6px; }
.gacha-empty-owned__s{ font-size:12px; color:#94a3b8; margin:0; line-height:1.5; }

.gacha-owned-panel{
  position:relative;
  margin-top:.5rem;
  padding:1.25rem 1rem 1.35rem;
  border-radius:22px;
  border:1px solid rgba(148,163,184,.2);
  background:rgba(255,255,255,.55);
  backdrop-filter:blur(8px);
}
.gacha-owned-panel .shop-section-h{ margin-bottom:.75rem; }
.gacha-owned-panel .owned-card{
  transition:transform .15s ease, box-shadow .15s ease;
}
.gacha-owned-panel .owned-card:hover{
  transform:translateY(-2px);
  box-shadow:0 12px 28px rgba(99,102,241,.12);
}

#gachaProbModalDim .gacha-prob-panel{
  max-width:min(380px,100%);
  border-radius:22px;
  padding:1.35rem 1.25rem;
  border:1px solid rgba(232,164,184,.3);
  background:linear-gradient(180deg,#fff,rgba(253,250,252,.98));
  box-shadow:0 24px 60px rgba(30,27,75,.12);
}
#gachaProbModalDim .gacha-prob-panel table tbody tr:last-child{ border-bottom:none; }

@media (prefers-reduced-motion: reduce){
  .gacha-page::after,
  .gacha-orbit,
  .gacha-orbit--2{ animation:none !important; }
  .gacha-draw-card::before{ transition:none !important; }
}
.gacha-event-banner{
  margin-bottom:1.25rem; padding:1rem 1.15rem; border-radius:1.25rem;
  border:1px solid rgba(167,139,250,0.35);
  background:linear-gradient(135deg,rgba(233,176,196,0.16),rgba(196,181,253,0.14));
  box-shadow:0 12px 36px rgba(99,102,241,0.08);
}
.gacha-event-banner__t{ font-weight:900; color:#0f172a; font-size:1rem; margin-bottom:.25rem; }
.gacha-event-banner__s{ font-size:.875rem; color:rgba(51,65,85,0.88); line-height:1.45; }
.gacha-event-banner__r{ font-size:.72rem; font-family:"Orbitron",sans-serif; letter-spacing:.12em; color:rgba(100,116,139,0.9); margin-top:.5rem; }
.gacha-spot-buff-banner{
  margin-bottom:1.25rem; padding:1rem 1.15rem; border-radius:1.25rem;
  border:1px solid rgba(139,92,246,0.38);
  background:linear-gradient(135deg,rgba(196,181,253,0.2),rgba(233,176,196,0.14));
  box-shadow:0 12px 36px rgba(99,102,241,0.08);
}
.gacha-spot-buff-banner__t{ font-weight:900; color:#0f172a; font-size:1rem; margin-bottom:.25rem; }
.gacha-spot-buff-banner__s{ font-size:.875rem; color:rgba(51,65,85,0.9); line-height:1.45; }
.gacha-spot-buff-banner__r{ font-size:.72rem; font-family:"Orbitron",sans-serif; letter-spacing:.12em; color:rgba(100,116,139,0.9); margin-top:.5rem; }
</style>
</head>
<body class="page-main min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="gacha-page flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
  <div class="container mx-auto max-w-5xl gacha-inner">
    <section class="shop-shell gacha-shell px-7 py-9 md:px-10 md:py-11">

      <div class="flex flex-wrap items-start justify-between gap-3 mb-6">
        <div class="board-filter-bar mb-0">
          <a href="${ctx}/market/shop" class="filter-chip ${empty marketTab or marketTab eq 'shop' ? 'is-active' : ''}">아이템</a>
          <a href="${ctx}/market/gacha" class="filter-chip ${marketTab eq 'gacha' ? 'is-active' : ''}">연습생 뽑기</a>
          <a href="${ctx}/market/photocard" class="filter-chip ${marketTab eq 'photocard' ? 'is-active' : ''}">포토카드 뽑기</a>
        </div>
        <button type="button" class="gacha-prob-btn" id="gachaProbOpen" aria-label="등급 확률 안내"><i class="fas fa-chart-pie mr-1 opacity-70"></i> 확률 정보</button>
      </div>

      <c:if test="${gachaSettings.castingSpotBuffActive}">
        <div class="gacha-spot-buff-banner" role="status" id="gachaSpotBuffBanner">
          <div class="gacha-spot-buff-banner__t"><i class="fas fa-map-location-dot text-violet-600 mr-1"></i> <c:out value="${gachaSettings.castingSpotBuffTitle}"/> 적용 중</div>
          <div class="gacha-spot-buff-banner__s"><c:out value="${gachaSettings.castingSpotBuffEffectLine}"/></div>
          <div class="gacha-spot-buff-banner__r">남은 시간 · <span id="gachaSpotBuffRemain">—</span></div>
        </div>
      </c:if>

      <c:if test="${gachaSettings.castingEventActive}">
        <div class="gacha-event-banner" role="status">
          <div class="gacha-event-banner__t"><i class="fas fa-bolt text-amber-500 mr-1"></i> <c:out value="${gachaSettings.castingEventTitle}"/></div>
          <div class="gacha-event-banner__s"><c:out value="${gachaSettings.castingEventEffectLine}"/></div>
          <div class="gacha-event-banner__r">남은 기간 · <c:out value="${gachaSettings.castingEventRemaining}"/></div>
        </div>
      </c:if>

      <div class="shop-head gacha-hero mb-2 relative">
        <div class="flex flex-wrap items-start gap-5 md:gap-8 min-w-0 flex-1">
          <div class="gacha-hero__visual shrink-0">
            <span class="gacha-orbit gacha-orbit--2" aria-hidden="true"></span>
            <span class="gacha-orbit" aria-hidden="true"></span>
            <div class="gacha-orbit-core" aria-hidden="true"><i class="fas fa-dice"></i></div>
          </div>
          <div class="min-w-0">
            <div class="shop-kicker text-[11px] tracking-[0.38em] uppercase font-orbitron mb-2 text-slate-500">DEBUT GACHA</div>
            <h1 class="font-orbitron text-3xl md:text-[2.65rem] font-black leading-tight mb-3 gacha-title-gradient">연습생 뽑기</h1>
            <p class="text-sm md:text-[15px] text-slate-600 leading-relaxed max-w-xl">
              코인으로 연습생을 영입합니다. 같은 카드가 나오면 <span class="text-slate-800 font-bold">보유 수량</span>만 늘어납니다.
            </p>
            <div class="gacha-tier-pills" aria-hidden="true">
              <span class="gacha-tier-pill is-n">N</span>
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
                <span>로그인 후 뽑기·보유 목록을 쓸 수 있어요.</span>
                <a class="gacha-guest-banner__link" href="${ctx}/login?redirect=/market/gacha">로그인</a>
              </div>
            </c:if>
          </div>
        </div>
      </div>

      <div class="gacha-draw-stage">
        <div class="gacha-draw-stage__head">
          <span class="gacha-draw-stage__k">DRAW</span>
          <div class="line"></div>
          <span class="text-xs font-bold text-slate-400">영입 실행</span>
        </div>
        <p class="gacha-draw-stage__hint">한 번 탭하면 바로 차감·결과 확인까지 이어집니다. 코인이 부족하면 뽑기가 되지 않아요.</p>
        <div class="gacha-draw-grid gacha-draw-grid--triple">
          <button type="button" class="gacha-draw-card gacha-draw-card--single" id="btnPull1" data-pulls="1"
            ${not loggedIn ? 'disabled' : ''}>
            <span class="gacha-draw-card__left" aria-hidden="true"><i class="fas fa-star"></i></span>
            <span class="gacha-draw-card__right">
              <span class="gacha-draw-card__tag">SINGLE</span>
              <span class="gacha-draw-card__title">1회 뽑기</span>
              <span class="gacha-draw-card__price"><span class="num">${gachaSettings.priceSingle}</span>코인</span>
            </span>
          </button>
          <button type="button" class="gacha-draw-card gacha-draw-card--multi" id="btnPull5" data-pulls="5"
            ${not loggedIn ? 'disabled' : ''}>
            <span class="gacha-draw-card__left" aria-hidden="true"><i class="fas fa-layer-group"></i></span>
            <span class="gacha-draw-card__right">
              <span class="gacha-draw-card__tag">${gachaSettings.multiCount}연속 · BUNDLE<c:if test="${not empty gachaSettings.multiBonusNote}"> · ${gachaSettings.multiBonusNote}</c:if></span>
              <span class="gacha-draw-card__title">${gachaSettings.multiCount}회 뽑기</span>
              <span class="gacha-draw-card__price"><span class="num">${gachaSettings.priceMulti}</span>코인</span>
            </span>
          </button>
          <button type="button" class="gacha-draw-card gacha-draw-card--multi" id="btnPull10" data-pulls="10"
            ${not loggedIn ? 'disabled' : ''}>
            <span class="gacha-draw-card__left" aria-hidden="true"><i class="fas fa-bolt"></i></span>
            <span class="gacha-draw-card__right">
              <span class="gacha-draw-card__tag">${gachaSettings.count10}연속 · BUNDLE<c:if test="${not empty gachaSettings.multi10BonusNote}"> · ${gachaSettings.multi10BonusNote}</c:if></span>
              <span class="gacha-draw-card__title">${gachaSettings.count10}회 뽑기</span>
              <span class="gacha-draw-card__price"><span class="num">${gachaSettings.price10}</span>코인</span>
            </span>
          </button>
        </div>
      </div>

      <div class="gacha-owned-panel">
      <div class="shop-section-h flex items-center gap-4 mb-3">
        <div class="text-[13px] font-orbitron tracking-[0.28em] uppercase text-slate-500">MY TRAINEES</div>
        <div class="flex-1 h-px bg-slate-200"></div>
      </div>
      <p class="text-xs text-slate-500 mb-4">보유한 연습생 · 수량은 향후 로스터/게임 연동에 사용할 수 있도록 저장됩니다.</p>

      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-3" id="ownedGrid">
        <c:choose>
          <c:when test="${empty myTrainees}">
            <div class="gacha-empty-owned col-span-full">
              <div class="gacha-empty-owned__icon" aria-hidden="true"><i class="fas fa-clone"></i></div>
              <p class="gacha-empty-owned__t">아직 보유한 연습생이 없습니다</p>
              <p class="gacha-empty-owned__s">위에서 뽑기를 실행하면 카드가 여기에 쌓입니다. 같은 연습생은 수량으로 표시돼요.</p>
            </div>
          </c:when>
          <c:otherwise>
            <c:forEach var="t" items="${myTrainees}">
              <div class="owned-card" data-tid="${t.traineeId}">
                <img src="${ctx}${t.imagePath}?v=${t.traineeId}" alt="${t.name}" loading="lazy" onerror="this.onerror=null;this.src=window.__GACHA_TRAINEE_PLACEHOLDER"/>
                <div class="p-2">
                  <div class="flex items-center justify-between gap-1">
                    <span class="text-xs font-black text-slate-800 truncate">${t.name}</span>
                    <span class="grade-badge ${t.grade}">${t.grade}</span>
                  </div>
                  <div class="text-[10px] text-slate-500 font-orbitron mt-0.5">× ${t.quantity}</div>
                </div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
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
    <div class="gacha-pulling__icon" aria-hidden="true"><i class="fas fa-dice"></i></div>
    <p class="gacha-pulling__text">영입 중</p>
    <p class="gacha-pulling__sub">카드가 열리고 있어요…</p>
  </div>
</div>

<div id="gachaModalDim" aria-hidden="true">
  <div id="gachaModalPanel" class="gacha-result-panel" role="dialog" aria-modal="true" aria-labelledby="gachaModalTitle">
    <div class="gacha-result-panel__head flex justify-between items-start gap-3">
      <div>
        <div id="gachaModalTitle" class="gacha-result-panel__title">뽑기 결과</div>
        <div id="gachaModalSub" class="gacha-result-panel__sub">슬롯을 멈춘 뒤 카드를 뒤집어 확인하세요</div>
      </div>
      <button type="button" class="text-2xl leading-none opacity-80 hover:opacity-100 transition-opacity" id="gachaModalClose" aria-label="닫기">&times;</button>
    </div>
    <div id="gachaResultBody" class="flex flex-col"></div>
    <div class="gacha-result-footer text-center">
      <button type="button" class="gacha-pull-btn gacha-pull-btn--skip mb-2" id="gachaBtnSkipAnim">연출 스킵</button>
      <button type="button" class="gacha-pull-btn gacha-pull-btn--stopall mb-2" id="gachaBtnStopAll">전체 멈춤</button>
      <p id="gachaFlipHint" class="gacha-flip-hint hidden text-[11px] text-slate-400 mb-2">카드를 눌러 앞면을 확인하세요</p>
      <button type="button" class="gacha-pull-btn" id="gachaModalOk" disabled>확인</button>
    </div>
  </div>
</div>

<div id="gachaSsrFlash" class="gacha-ssr-flash" aria-hidden="true"></div>

<div id="gachaProbModalDim" aria-hidden="true">
  <div class="gacha-prob-panel w-full">
    <div class="flex justify-between items-center mb-3">
      <span class="font-orbitron text-xs tracking-widest text-slate-500">RATES</span>
      <button type="button" class="text-slate-400 hover:text-slate-700 text-xl" id="gachaProbClose">&times;</button>
    </div>
    <table class="w-full text-sm">
      <tbody>
        <c:forEach var="probRow" items="${gachaSettings.gradeProbabilities}">
          <tr class="border-b border-slate-100">
            <td class="py-2 font-bold text-slate-700">${probRow['key']}</td>
            <td class="py-2 text-right text-slate-600">${probRow['value']}%</td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
    <p class="text-[10px] text-slate-400 mt-3 leading-relaxed">1회 ${gachaSettings.priceSingle}코인 · 5회 ${gachaSettings.priceMulti}코인 · 10회 ${gachaSettings.price10}코인 · 풀: ${gachaSettings.poolId}</p>
  </div>
</div>

<script>
(function(){
  var CTX = window.__UNITX_CTX || '${ctx}';
  var PH = window.__GACHA_TRAINEE_PLACEHOLDER || '';
  var GACHA_EVENT_ID = <c:choose><c:when test="${gachaEventId != null}">${gachaEventId}</c:when><c:otherwise>null</c:otherwise></c:choose>;
  var SPOT_BUFF_EXPIRE = '<c:out value="${gachaSettings.castingSpotBuffExpireAt}"/>';
  (function spotBuffTimer(){
    var el = document.getElementById('gachaSpotBuffRemain');
    if (!el || !SPOT_BUFF_EXPIRE) return;
    function fmt(){
      var t = Date.parse(SPOT_BUFF_EXPIRE);
      if (isNaN(t)) { el.textContent = '—'; return; }
      var ms = Math.max(0, t - Date.now());
      var s = Math.floor(ms / 1000);
      var m = Math.floor(s / 60);
      var h = Math.floor(m / 60);
      m = m % 60;
      s = s % 60;
      if (h > 0) {
        el.textContent = h + ':' + String(m).padStart(2,'0') + ':' + String(s).padStart(2,'0');
      } else {
        el.textContent = m + ':' + String(s).padStart(2,'0');
      }
    }
    fmt();
    setInterval(fmt, 1000);
  })();
  var coinEl = document.getElementById('gachaCoinText');
  var dim = document.getElementById('gachaModalDim');
  var probDim = document.getElementById('gachaProbModalDim');
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

  function rowGlowClass(grade){
    var g = (grade||'').toUpperCase();
    if (g === 'SSR') return 'gacha-result-row--ssr';
    if (g === 'SR') return 'gacha-result-row--sr';
    return '';
  }

  function hasSsrInPulls(pulls){
    return (pulls || []).some(function(p){ return (p.grade || '').toUpperCase() === 'SSR'; });
  }

  function prefersReducedMotion(){
    try {
      return window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    } catch (e) { return false; }
  }

  var GACHA_SLOT_CELL_H = 72;
  var GACHA_SLOT_LEN = 30;
  var GACHA_SLOT_WIN_IDX = 14;

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

  function buildSlotStripHtml(){
    var i, parts = [];
    for (i = 0; i < GACHA_SLOT_LEN; i++) {
      parts.push(
        '<div class="gacha-slot-card" aria-hidden="true"><div class="gacha-slot-card__face"><span>?</span></div></div>'
      );
    }
    return parts.join('');
  }

  /**
   * 멈춤 위치를 브라우저에서 읽지 않고 항상 당첨 줄(finalY)로 맞춤.
   * 한 바퀴(cycleH) 위에서 시작해 finalY로만 감속 → 빈 칸/반칸 오프셋 제거.
   */
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

  function buildFlipCardHtml(p, idx){
    var g = (p.grade||'N').toUpperCase();
    var dup = p.duplicate
      ? '<span class="gacha-result-dup">중복 +1</span>'
      : '<span class="gacha-result-new">NEW</span>';
    var img = (p.imagePath && p.imagePath.charAt(0)==='/') ? (CTX + p.imagePath) : (CTX + '/' + (p.imagePath||''));
    if (p.traineeId) { img += (img.indexOf('?') >= 0 ? '&' : '?') + 'v=' + p.traineeId; }
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
      '보유 합계 '+escHtml(String(p.ownedTotalAfter!=null?p.ownedTotalAfter:1))+'명'+
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
    var flipGrid = rows.map(function(p, idx){ return buildFlipCardHtml(p, idx); }).join('');
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
    /* 뽑기 버튼에 포커스가 남아 있으면 Enter가 또 뽑기를 호출함 → 확인으로 이동 */
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

  /* 결과 모달이 열린 동안 Enter는 확인(닫기)만 — 포커스 유실 시에도 재뽑기 방지 */
  document.addEventListener('keydown', function (e) {
    if (e.key !== 'Enter') return;
    if (!dim || !dim.classList.contains('is-open')) return;
    var okBtn = document.getElementById('gachaModalOk');
    if (okBtn && okBtn.disabled) return;
    e.preventDefault();
    e.stopPropagation();
    closeModal();
  }, true);

  document.getElementById('gachaProbOpen')?.addEventListener('click', function(){ probDim.classList.add('is-open'); });
  document.getElementById('gachaProbClose')?.addEventListener('click', function(){ probDim.classList.remove('is-open'); });
  probDim?.addEventListener('click', function(e){ if(e.target===probDim) probDim.classList.remove('is-open'); });

  function refreshOwnedGrid(list){
    var grid = document.getElementById('ownedGrid');
    if(!grid || !list) return;
    grid.innerHTML = list.map(function(t){
      var img = (t.imagePath && t.imagePath.charAt(0)==='/') ? (CTX + t.imagePath) : (CTX + '/' + (t.imagePath||''));
      if (t.traineeId) { img += (img.indexOf('?') >= 0 ? '&' : '?') + 'v=' + t.traineeId; }
      var g = (t.grade||'N').toUpperCase();
      return '<div class="owned-card" data-tid="'+t.traineeId+'">'+
        '<img src="'+img+'" alt="'+ (t.name||'') +'" loading="lazy" onerror="this.onerror=null;this.src=\''+PH+'\'"/>'+
        '<div class="p-2"><div class="flex items-center justify-between gap-1">'+
        '<span class="text-xs font-black text-slate-800 truncate">'+(t.name||'')+'</span>'+
        '<span class="grade-badge '+g+'">'+g+'</span></div>'+
        '<div class="text-[10px] text-slate-500 font-orbitron mt-0.5">× '+t.quantity+'</div></div></div>';
    }).join('');
  }

  async function doPull(pulls){
    var price1 = ${gachaSettings != null and gachaSettings.priceSingle != null ? gachaSettings.priceSingle : 100};
    var price5 = ${gachaSettings != null and gachaSettings.priceMulti != null ? gachaSettings.priceMulti : 450};
    var price10 = ${gachaSettings != null and gachaSettings.price10 != null ? gachaSettings.price10 : 850};
    var need = (pulls===10) ? price10 : (pulls===5) ? price5 : price1;
    var cur = parseInt((coinEl && coinEl.textContent) ? coinEl.textContent : '0', 10);
    if(isNaN(cur) || cur < need){
      alert('코인이 부족합니다.');
      return;
    }
    showPulling();
    try{
      var res = await fetch(CTX + '/market/gacha/pull', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({ pulls: pulls, poolId: 'DEFAULT', eventId: GACHA_EVENT_ID })
      });
      var data = await res.json();
      hidePulling();
      if(data.result === 'logout'){ alert('로그인이 필요합니다.'); location.href = CTX + '/login?redirect=/market/gacha'; return; }
      if(data.result === 'lack'){ setCoin(data.currentCoin); alert('코인이 부족합니다.'); return; }
      if(data.result !== 'success'){ alert(data.message || '처리할 수 없습니다.'); return; }
      setCoin(data.currentCoin);
      renderResults(data.pulls);
      openModal();
      if(data.myTrainees) refreshOwnedGrid(data.myTrainees);
    }catch(e){
      hidePulling();
      alert('요청 중 오류가 발생했습니다.');
    }
  }

  document.getElementById('btnPull1')?.addEventListener('click', function(){ doPull(1); });
  document.getElementById('btnPull5')?.addEventListener('click', function(){ doPull(5); });
  document.getElementById('btnPull10')?.addEventListener('click', function(){ doPull(10); });
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
  chargeItems.forEach(function (item) {
    item.addEventListener('click', function () {
      chargeItems.forEach(function (i) { i.classList.remove('active'); });
      this.classList.add('active');
      selectedAmount = Number(this.dataset.amount);
    });
  });
  if (payBtn) {
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

</body>
</html>
