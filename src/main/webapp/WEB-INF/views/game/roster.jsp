<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>NEXT DEBUT — 선발 결과</title>
<%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
<link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Noto+Sans+KR:wght@400;500;700;900&display=swap" rel="stylesheet">
<style>
:root{--pk:#e879a3;--lv:#a78bfa;--bl:#7dd3fc;--gd:#eab308;--bg:#f5f0ff;}
*{box-sizing:border-box}
body{margin:0;color:#1e293b;font-family:"Noto Sans KR",sans-serif;background:var(--bg);}
.main-wrap{padding:calc(var(--nav-h,68px) + 28px) 20px 72px;}
.inner{max-width:1240px;margin:0 auto;}
.hero{padding:28px 28px 34px;border-radius:28px;background:rgba(255,255,255,.9);backdrop-filter:blur(18px);border:1px solid rgba(167,139,250,.22);box-shadow:0 20px 60px rgba(99,102,241,.08);text-align:center;margin-bottom:24px;}
.kicker{font-family:"Orbitron",sans-serif;font-size:10px;letter-spacing:.42em;color:#475569;margin-bottom:14px}
.title{font-family:"Orbitron",sans-serif;font-size:clamp(2.2rem,5.6vw,4.6rem);font-weight:900;line-height:1.05;background:linear-gradient(120deg,#1e293b,var(--pk),var(--lv),var(--bl));background-size:260%;-webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent;}
.roster-grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:20px;margin-bottom:24px}
@media(max-width:1100px){.roster-grid{grid-template-columns:repeat(2,minmax(0,1fr));}}
@media(max-width:640px){.roster-grid{grid-template-columns:1fr;}}
.card{border-radius:24px;overflow:hidden;background:rgba(255,255,255,.92);border:1px solid rgba(167,139,250,.2);backdrop-filter:blur(18px);box-shadow:0 16px 40px rgba(99,102,241,.06);opacity:0;transform:translateY(20px);animation:cardIn .45s ease forwards}
@keyframes cardIn{to{opacity:1;transform:none}}
.card:nth-child(1){animation-delay:.02s}.card:nth-child(2){animation-delay:.08s}.card:nth-child(3){animation-delay:.14s}.card:nth-child(4){animation-delay:.20s}
.photo{position:relative;aspect-ratio:3/4;background:rgba(245,240,255,.6)}
.photo img{width:100%;height:100%;object-fit:cover;object-position:center top;display:block}
.photo::after{content:"";position:absolute;inset:0;background:linear-gradient(to top,rgba(30,41,59,.7),transparent 50%)}
.pick{position:absolute;top:10px;left:10px;z-index:2;width:28px;height:28px;border-radius:8px;background:linear-gradient(135deg,var(--pk),var(--lv));display:flex;align-items:center;justify-content:center;font-family:"Orbitron",sans-serif;font-weight:900;font-size:12px;color:#fff}
.namebox{position:absolute;left:14px;right:14px;bottom:12px;z-index:2}
.name{font-size:1.8rem;font-weight:900;line-height:1.1;margin-bottom:8px;color:#fff;text-shadow:0 2px 8px rgba(0,0,0,.4)}
.badges{display:flex;gap:6px;flex-wrap:wrap;align-items:center}.badge{padding:3px 9px;border-radius:999px;font-size:10px;font-weight:700;border:1px solid rgba(167,139,250,.35);background:rgba(255,255,255,.25);color:#334155}
.tier-badge{padding:2px 8px;border-radius:999px;font-size:9px;font-weight:900;font-family:"Orbitron",sans-serif;border:1px solid rgba(255,255,255,.35);text-shadow:0 1px 2px rgba(0,0,0,.2)}
.tier--n{background:rgba(148,163,184,.85);color:#1e293b}
.tier--r{background:rgba(96,165,250,.88);color:#1e3a8a}
.tier--sr{background:rgba(167,139,250,.9);color:#3b0764}
.tier--ssr{background:linear-gradient(135deg,#fde68a,#f59e0b);color:#422006;border-color:rgba(251,191,36,.6)}
.enhance-pill{
  display:inline-flex;align-items:center;justify-content:center;
  padding:2px 7px;border-radius:999px;
  font-family:"Orbitron",sans-serif;font-size:9px;font-weight:900;letter-spacing:.06em;
  border:1px solid rgba(148,163,184,.4);background:rgba(255,255,255,.82);color:#64748b;
}
.enhance-pill.lv-0{color:#64748b;}
.enhance-pill.lv-1{color:#b45309;border-color:rgba(251,191,36,.4);background:rgba(254,243,199,.85);}
.enhance-pill.lv-2{color:#a16207;border-color:rgba(251,191,36,.5);background:rgba(254,240,138,.84);}
.enhance-pill.lv-3{color:#92400e;border-color:rgba(245,158,11,.58);background:rgba(253,230,138,.86);}
.enhance-pill.lv-4{color:#7c2d12;border-color:rgba(245,158,11,.65);background:rgba(251,191,36,.34);}
.enhance-pill.lv-5{
  color:#713f12;border-color:rgba(234,179,8,.72);background:linear-gradient(135deg,rgba(254,249,195,.9),rgba(250,204,21,.8));
  box-shadow:0 0 10px rgba(250,204,21,.35);
}
.pc-pill{
  display:inline-flex;align-items:center;justify-content:center;
  padding:2px 7px;border-radius:999px;
  font-family:"Orbitron",sans-serif;font-size:9px;font-weight:900;letter-spacing:.06em;
  border:1px solid rgba(167,139,250,.35);background:rgba(255,255,255,.75);color:#5b21b6;
}
.pc-pill--r{color:#5b21b6;border-color:rgba(139,92,246,.4);background:rgba(245,243,255,.9);}
.pc-pill--sr{color:#334155;border-color:rgba(148,163,184,.5);background:rgba(241,245,249,.9);}
.pc-pill--ssr{color:#92400e;border-color:rgba(251,191,36,.55);background:rgba(254,243,199,.85);}
.pc-pill--none{color:#64748b;border-color:rgba(148,163,184,.42);background:rgba(248,250,252,.88);}
.grade{font-family:"Orbitron",sans-serif}.body{padding:14px 14px 16px}.srow{display:flex;align-items:center;gap:8px;margin-bottom:8px}.slbl{width:40px;font-size:12px;color:#475569}.track{flex:1;height:6px;border-radius:999px;background:rgba(167,139,250,.15);overflow:hidden}.fill{height:100%;border-radius:999px}.v{background:linear-gradient(90deg,#f472b6,#ec4899)}.d{background:linear-gradient(90deg,#c084fc,#8b5cf6)}.s{background:linear-gradient(90deg,#fbbf24,#f59e0b)}.m{background:linear-gradient(90deg,#60a5fa,#3b82f6)}.t{background:linear-gradient(90deg,#34d399,#10b981)}.sval{width:24px;text-align:right;font-size:12px;color:#334155}
.sval[data-eb]:not([data-eb="0"])::after{
  content:" (+" attr(data-eb) ")";
  color:#b45309;
  font-weight:900;
  font-size:10px;
}
.total{display:flex;justify-content:space-between;align-items:end;margin-top:12px;padding-top:10px;border-top:1px solid rgba(167,139,250,.18)}.tt{font-family:"Orbitron",sans-serif;font-size:10px;letter-spacing:.15em;color:#64748b}.tv{font-family:"Orbitron",sans-serif;font-size:2rem;font-weight:900;background:linear-gradient(120deg,var(--pk),var(--lv),var(--bl));-webkit-background-clip:text;background-clip:text;-webkit-text-fill-color:transparent}
.chem-box{background:rgba(255,255,255,.88);border:1px solid rgba(167,139,250,.22);border-radius:28px;padding:24px 26px;margin-bottom:22px;backdrop-filter:blur(18px);box-shadow:0 16px 48px rgba(99,102,241,.06)}
.chem-head{display:flex;align-items:center;gap:16px;padding:16px 18px;border-radius:28px;background:rgba(245,240,255,.7);border:1px solid rgba(167,139,250,.25);margin-bottom:18px;flex-wrap:wrap}.chem-head .label{font-family:"Orbitron",sans-serif;font-size:12px;letter-spacing:.22em;color:#64748b}.chem-grade{font-family:"Orbitron",sans-serif;font-size:2.4rem;font-weight:900;line-height:1}.chem-grade.g-S{color:#eab308}.chem-grade.g-A{color:#f472b6}.chem-grade.g-B{color:#a78bfa}.chem-grade.g-C{color:#7dd3fc}.chem-grade.g-D{color:#94a3b8}.chem-name{font-size:1.2rem;font-weight:800;color:#1e293b}.chem-actions{margin-left:auto;display:flex;align-items:center;gap:8px;flex-wrap:wrap;justify-content:flex-end}.chem-bonus{font-family:"Orbitron",sans-serif;font-size:11px;padding:6px 12px;border-radius:999px;background:rgba(251,191,36,.15);border:1px solid rgba(251,191,36,.35);color:#b45309;white-space:nowrap}.chem-action-btn{display:inline-flex;align-items:center;justify-content:center;gap:8px;padding:9px 14px;border-radius:999px;border:1px solid rgba(167,139,250,.28);background:rgba(255,255,255,.82);color:#475569;font-family:"Orbitron",sans-serif;font-size:10px;letter-spacing:.12em;cursor:pointer;white-space:nowrap}.chem-action-btn:hover{border-color:rgba(244,114,182,.42);box-shadow:0 12px 24px rgba(244,114,182,.12)}
.chem-grid{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(0,1fr);gap:18px;align-items:stretch}
@media(max-width:980px){.chem-grid{grid-template-columns:1fr}.chem-actions{margin-left:0}}
.chem-copy{height:100%;padding:20px;border-radius:22px;background:rgba(255,248,252,.6);border:1px solid rgba(167,139,250,.18)}
.chem-copy h3{font-family:"Orbitron",sans-serif;font-size:13px;letter-spacing:.18em;color:#1e293b;margin:0 0 14px}.chem-copy p{font-size:15px;line-height:1.8;color:#475569;margin:0}
.chem-points{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;align-content:start}
@media(max-width:640px){.chem-points{grid-template-columns:1fr}}
.chem-point{min-height:108px;padding:16px;border-radius:20px;background:rgba(255,255,255,.7);border:1px solid rgba(167,139,250,.2);display:flex;gap:12px;align-items:flex-start}
.chem-icon{width:30px;height:30px;border-radius:999px;display:flex;align-items:center;justify-content:center;flex-shrink:0;background:linear-gradient(135deg,var(--pk),var(--bl));color:#fff;font-weight:900}.chem-text strong{display:block;font-size:14px;line-height:1.4;color:#1e293b;margin-bottom:6px}.chem-text span{display:block;font-size:13px;line-height:1.55;color:#475569}
.actions{display:flex;justify-content:center;align-items:center;gap:14px;flex-wrap:wrap}.btn{display:inline-flex;align-items:center;justify-content:center;gap:10px;padding:14px 28px;min-width:170px;min-height:64px;border-radius:999px;font-family:"Orbitron",sans-serif;font-size:12px;letter-spacing:.12em;text-decoration:none;border:1px solid rgba(167,139,250,.35);background:rgba(255,255,255,.85);color:#334155;cursor:pointer;text-align:center}.btn:disabled{opacity:.45;cursor:not-allowed}.btn-primary{background:linear-gradient(135deg,var(--pk),var(--lv),var(--bl));color:#fff;border:none;font-weight:900}.reroll-btn{flex-direction:column;gap:4px;line-height:1.2}.reroll-main{display:inline-flex;align-items:center;justify-content:center;gap:8px}.reroll-sub{font-size:11px;letter-spacing:.02em;color:#7c3aed;font-family:"Noto Sans KR",sans-serif;font-weight:700}
.item-pick{background:rgba(255,255,255,.9);border:1px solid rgba(167,139,250,.22);border-radius:24px;padding:16px 18px;margin:0 0 18px;box-shadow:0 14px 36px rgba(99,102,241,.06)}
.item-pick-head{display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:10px}
.item-pick-title{font-family:"Orbitron",sans-serif;font-size:12px;letter-spacing:.12em;color:#334155}
.item-pick-count{font-size:12px;font-weight:800;color:#7c3aed}
.item-pick-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px}
@media(max-width:768px){.item-pick-grid{grid-template-columns:1fr}}
.item-chip{display:flex;align-items:center;gap:8px;padding:10px;border:1px solid rgba(167,139,250,.2);border-radius:14px;background:rgba(245,240,255,.6)}
.item-chip input{accent-color:#c026d3}
.item-chip-name{font-weight:700;color:#334155;font-size:13px}
.toast{position:fixed;top:calc(var(--nav-h,68px) + 18px);left:50%;transform:translateX(-50%);z-index:1000;background:rgba(255,255,255,.96);border:1px solid rgba(167,139,250,.3);padding:12px 18px;border-radius:14px;box-shadow:0 12px 26px rgba(99,102,241,.12);color:#1e293b;opacity:0;pointer-events:none;transition:opacity .25s,transform .25s}.toast.show{opacity:1;transform:translateX(-50%) translateY(0)}
.chem-modal{position:fixed;inset:0;z-index:820;display:none;padding:24px;background:rgba(15,23,42,.34);backdrop-filter:blur(10px);overflow:auto}.chem-modal.show{display:block}.chem-modal__dialog{position:relative;margin:0 auto;width:min(760px,calc(100vw - 48px));display:flex;flex-direction:column;overflow:hidden;border-radius:28px;padding:24px;background:rgba(255,255,255,.98);border:1px solid rgba(167,139,250,.28);box-shadow:0 28px 80px rgba(15,23,42,.18)}.chem-modal__head{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;margin-bottom:16px;flex-shrink:0}.chem-modal__body{overflow:auto;padding-right:4px;min-height:0}.chem-modal__eyebrow{font-family:"Orbitron",sans-serif;font-size:10px;letter-spacing:.28em;color:#64748b;margin-bottom:8px}.chem-modal__title{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.chem-modal__grade{display:inline-flex;align-items:center;justify-content:center;min-width:44px;height:44px;padding:0 14px;border-radius:999px;font-family:"Orbitron",sans-serif;font-size:18px;font-weight:900;background:linear-gradient(135deg,rgba(244,114,182,.18),rgba(192,132,252,.22));border:1px solid rgba(167,139,250,.3)}.chem-modal__name{font-size:24px;font-weight:900;color:#1e293b}.chem-modal__bonus{display:inline-flex;align-items:center;gap:8px;margin-top:10px;padding:8px 12px;border-radius:999px;background:rgba(251,191,36,.14);border:1px solid rgba(251,191,36,.35);font-family:"Orbitron",sans-serif;font-size:11px;letter-spacing:.12em;color:#b45309}.chem-modal__close{width:42px;height:42px;border:none;border-radius:14px;background:rgba(245,240,255,.9);color:#475569;cursor:pointer;flex-shrink:0}.chem-modal__close:hover{background:#fff;box-shadow:0 8px 18px rgba(99,102,241,.12)}.chem-modal__desc{padding:16px 18px;border-radius:20px;background:rgba(245,240,255,.56);border:1px solid rgba(167,139,250,.18);font-size:14px;line-height:1.8;color:#475569;margin-bottom:16px}.chem-modal__grid{display:grid;grid-template-columns:1fr;gap:12px}.chem-chip{display:flex;gap:12px;align-items:flex-start;padding:16px;border-radius:20px;background:rgba(255,255,255,.82);border:1px solid rgba(167,139,250,.18)}.chem-chip__icon{width:34px;height:34px;border-radius:12px;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,var(--pk),var(--lv),var(--bl));color:#fff;font-weight:900;flex-shrink:0}.chem-chip__title{font-size:14px;font-weight:900;color:#1e293b;margin-bottom:4px}.chem-chip__desc{font-size:13px;line-height:1.6;color:#475569}.chem-chip__members{display:flex;flex-wrap:wrap;gap:6px;margin-top:10px}.chem-chip__member{display:inline-flex;align-items:center;padding:5px 10px;border-radius:999px;background:rgba(244,114,182,.1);border:1px solid rgba(244,114,182,.24);color:#be185d;font-size:11px;font-weight:700}.chem-list{display:grid;grid-template-columns:1fr;gap:10px}.chem-list-item{padding:14px 16px;border-radius:18px;background:rgba(245,240,255,.56);border:1px solid rgba(167,139,250,.18)}.chem-list-item strong{display:block;font-size:14px;color:#1e293b;margin-bottom:6px}.chem-list-item span{display:block;font-size:13px;line-height:1.65;color:#475569}

@media(max-width:768px){.chem-modal{padding:16px}.chem-modal__dialog{width:min(94vw,760px);padding:20px 18px}.chem-modal__body{padding-right:2px}}

/* 2026 redesign override */
:root{
  --pk:#FF8FAB;
  --lv:#C4B5FD;
  --bl:#C4B5FD;
  --bg:#F5F3FF;
}
body{background:linear-gradient(180deg,#fff 0%,#F5F3FF 58%,#FDF2F8 100%);}
.hero,.card,.chem-box,.item-pick,.chem-modal__dialog,.chem-copy,.chem-point,.chem-chip,.chem-list-item{border-color:#E5E7EB;}
.btn-primary,.chem-icon,.chem-chip__icon,.chem-fab__icon{background:linear-gradient(135deg,var(--lv),var(--pk));}
.m,.t,.chem-grade.g-C,.stat-chip--male,.filter-tab--male.active{background:rgba(196,181,253,.16)!important;border-color:rgba(196,181,253,.45)!important;color:#7c6db8!important;}
.track,.kpi-track,.score-bar{background:rgba(196,181,253,.16)!important;}
.v,.d,.s,.m,.t,.kpi-fill,.lbar{background:linear-gradient(90deg,var(--lv),var(--pk))!important;}

/* 로스터 카드 포토카드 글로우 (페이지 내 보장 적용) */
.card.card-glow-r{
  box-shadow:0 0 24px rgba(139,92,246,0.42),0 16px 40px rgba(99,102,241,.10)!important;
  border-color:rgba(139,92,246,.56)!important;
}
.card.card-glow-sr{
  box-shadow:0 0 24px rgba(226,232,240,0.98),0 16px 40px rgba(99,102,241,.10)!important;
  border-color:rgba(203,213,225,.95)!important;
}
.card.card-glow-ssr{
  box-shadow:0 0 26px rgba(251,191,36,0.48),0 16px 40px rgba(245,158,11,.14)!important;
  border-color:rgba(251,191,36,.7)!important;
}
</style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>
<div class="toast" id="toastMsg"></div>
<main class="main-wrap">
  <div class="inner">
    <section class="hero">
      <div class="kicker">DEBUT SELECTION RESULT</div>
      <div class="title">선발된 멤버</div>
    </section>

    <section class="chem-box" id="chemBox">
      <div class="chem-head">
        <span class="label">CHEMISTRY</span>
        <span class="chem-grade g-${chemistry.chemGrade}" id="chemGrade">${chemistry.chemGrade}</span>
        <span class="chem-name" id="chemLabel">${chemistry.chemLabel}</span>
        <div class="chem-actions">
          <c:if test="${chemistry.totalBonus > 0}"><span class="chem-bonus" id="chemBonus">+${chemistry.totalBonus}% BOOST (시너지 ${chemistry.baseBonus}% + 등급 ${chemistry.gradeBonus}%)</span></c:if>
          <button type="button" class="chem-action-btn" id="chemMemberBtn" onclick="openChemMemberModal()"><i class="fas fa-users"></i> 멤버 확인</button>
          <button type="button" class="chem-action-btn" id="chemCatalogBtn" onclick="openChemCatalogModal()"><i class="fas fa-list"></i> 전체 케미스트리 확인</button>
        </div>
      </div>
      <div class="chem-grid">
        <div class="chem-copy">
          <h3>TEAM ANALYSIS</h3>
          <p id="chemDesc">
            <c:choose>
              <c:when test="${chemistry.hasSynergy()}">
                현재 조합은 <strong style="color:#fff;">${chemistry.chemLabel}</strong> 시너지가 형성된 상태입니다.
                <c:if test="${chemistry.totalBonus > 0}"> 전체 보너스 <strong style="color:#fbbf24;">+${chemistry.totalBonus}%</strong>가 적용됩니다. <strong style="color:#c084fc;">시너지 ${chemistry.baseBonus}% + 등급 ${chemistry.gradeBonus}%</strong> 구성입니다.</c:if>
                멤버 간 밸런스와 포지션 합을 기준으로 최종 데뷔 완성도가 올라갑니다.
              </c:when>
              <c:otherwise>
                아직 뚜렷한 시너지 조합은 없지만 카드 밸런스는 안정적입니다. 다시뽑기나 게임 진행 중 능력치 변화에 따라 팀 분위기가 달라질 수 있습니다.
              </c:otherwise>
            </c:choose>
          </p>
        </div>
        <div class="chem-points" id="chemPoints">
          <c:choose>
            <c:when test="${chemistry.hasSynergy()}">
              <c:forEach var="syn" items="${chemistry.synergies}">
                <div class="chem-point"><div class="chem-icon">${syn.icon}</div><div class="chem-text"><strong>${syn.name} · +${syn.bonusPct}%</strong><span>${syn.description}</span></div></div>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <div class="chem-point"><div class="chem-icon"><i class="fas fa-sparkles"></i></div><div class="chem-text"><strong>추가 시너지 없음</strong><span>현재 조합은 기본 성능만 반영됩니다.</span></div></div>
              <div class="chem-point"><div class="chem-icon"><i class="fas fa-chart-line"></i></div><div class="chem-text"><strong>성장 여지 있음</strong><span>진행 중 능력치 변화로 후반 결과가 달라질 수 있습니다.</span></div></div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </section>
    
    <section class="roster-grid" id="rosterGrid">
      <c:forEach var="m" items="${result.roster}" varStatus="st">
        <c:set var="rpcGlow" value=""/>
        <c:set var="pcGradeNorm" value="${fn:toUpperCase(fn:trim(m.photoCardGrade))}"/>
        <c:set var="pcGradeDisplay" value="${pcGradeNorm == 'R' || pcGradeNorm == 'SR' || pcGradeNorm == 'SSR' ? pcGradeNorm : 'NONE'}"/>
        <c:set var="pcBadgeClass" value="${pcGradeDisplay == 'NONE' ? 'none' : fn:toLowerCase(pcGradeDisplay)}"/>
        <c:set var="enhanceBonus" value="0"/>
        <c:choose>
          <c:when test="${m.enhanceLevel >= 5}"><c:set var="enhanceBonus" value="7"/></c:when>
          <c:when test="${m.enhanceLevel == 4}"><c:set var="enhanceBonus" value="4"/></c:when>
          <c:when test="${m.enhanceLevel == 3}"><c:set var="enhanceBonus" value="3"/></c:when>
          <c:when test="${m.enhanceLevel == 2}"><c:set var="enhanceBonus" value="2"/></c:when>
          <c:when test="${m.enhanceLevel == 1}"><c:set var="enhanceBonus" value="1"/></c:when>
        </c:choose>
        <c:choose>
          <c:when test="${pcGradeNorm == 'R'}"><c:set var="rpcGlow" value="card-glow-r"/></c:when>
          <c:when test="${pcGradeNorm == 'SR'}"><c:set var="rpcGlow" value="card-glow-sr"/></c:when>
          <c:when test="${pcGradeNorm == 'SSR'}"><c:set var="rpcGlow" value="card-glow-ssr"/></c:when>
        </c:choose>
        <article class="card ${rpcGlow}" style="animation-delay:${st.index * 0.06}s">
          <div class="photo">
            <span class="pick">${m.pickOrder}</span>
            <c:choose>
              <c:when test="${not empty m.imagePath}"><img src="${ctx}${m.imagePath}" alt="${m.name}"/></c:when>
              <c:otherwise><div style="height:100%;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,.2);font-size:68px"><i class="fas fa-user"></i></div></c:otherwise>
            </c:choose>
            <div class="namebox">
              <div class="name">${m.name}</div>
              <div class="badges">
                <span class="badge">${m.gender == 'MALE' ? '남자' : '여자'}</span>
                <c:if test="${not empty m.grade}">
                  <span class="tier-badge tier--${fn:toLowerCase(m.grade)}">${m.grade}</span>
                </c:if>
                <span class="enhance-pill lv-${m.enhanceLevel}">${m.enhanceLevel >= 5 ? 'MAX' : '+'.concat(m.enhanceLevel)}</span>
                <span class="pc-pill pc-pill--${pcBadgeClass}">PC ${pcGradeDisplay}</span>
              </div>
            </div>
          </div>
          <div class="body">
            <div class="srow">
              <span class="slbl">성격</span>
              <div style="flex:1;display:flex;gap:8px;align-items:center">
                <select class="js-personality" data-trainee-id="${m.traineeId}"
                        style="width:100%;padding:8px 10px;border-radius:12px;border:1px solid rgba(167,139,250,.35);background:rgba(255,255,255,.9);font-weight:800;color:#334155;">
                  <c:forEach var="p" items="${personalityOptions}">
                    <option value="${p.name()}" <c:if test="${m.personalityCode == p.name()}">selected</c:if>>
                      ${p.shortLabel}
                    </option>
                  </c:forEach>
                </select>
              </div>
            </div>
            <div class="srow"><span class="slbl">보컬</span><div class="track"><div class="fill v" style="width:${m.vocal}%"></div></div><span class="sval" data-eb="${enhanceBonus}">${m.vocal}</span></div>
            <div class="srow"><span class="slbl">댄스</span><div class="track"><div class="fill d" style="width:${m.dance}%"></div></div><span class="sval" data-eb="${enhanceBonus}">${m.dance}</span></div>
            <div class="srow"><span class="slbl">스타</span><div class="track"><div class="fill s" style="width:${m.star}%"></div></div><span class="sval" data-eb="${enhanceBonus}">${m.star}</span></div>
            <div class="srow"><span class="slbl">멘탈</span><div class="track"><div class="fill m" style="width:${m.mental}%"></div></div><span class="sval" data-eb="${enhanceBonus}">${m.mental}</span></div>
            <div class="srow"><span class="slbl">팀웍</span><div class="track"><div class="fill t" style="width:${m.teamwork}%"></div></div><span class="sval" data-eb="${enhanceBonus}">${m.teamwork}</span></div>
            <div class="total"><span class="tt">TOTAL</span><span class="tv">${m.vocal + m.dance + m.star + m.mental + m.teamwork}</span></div>
          </div>
        </article>
      </c:forEach>
    </section>

    <section class="item-pick">
      <div class="item-pick-head">
        <div class="item-pick-title">ITEM SELECT (MAX 5)</div>
        <div class="item-pick-count" id="itemPickCount">0/5 선택</div>
      </div>
      <div class="item-pick-grid" id="itemPickGrid">
        <c:choose>
          <c:when test="${empty myItems}">
            <div class="item-chip"><span class="item-chip-name">보유 아이템이 없습니다.</span></div>
          </c:when>
          <c:otherwise>
            <c:forEach var="it" items="${myItems}">
              <label class="item-chip">
                <input type="checkbox" class="js-item-pick" value="${it.id}">
                <span class="item-chip-name">${it.itemName}</span>
              </label>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </section>

    <div class="actions">
      <button type="button" class="btn reroll-btn" id="rerollBtn" <c:if test="${rerollRemaining le 0}">disabled</c:if>>
        <span class="reroll-main"><i class="fas fa-rotate-right"></i> 다시뽑기 <span id="rerollRemaining">${rerollRemaining}</span>/3</span>
        <span class="reroll-sub" id="rerollChargeInfo"></span>
      </button>
      <a href="${ctx}/game/run/${result.runId}/start" class="btn btn-primary" id="gameStartBtn"><i class="fas fa-play"></i> GAME START</a>
      <a href="${ctx}/main" class="btn"><i class="fas fa-home"></i> 메인으로</a>
    </div>
  </div>
</main>

<div class="chem-modal" id="chemMemberModal" onclick="closeChemMemberModal(event)">
  <div class="chem-modal__dialog" role="dialog" aria-modal="true" aria-labelledby="chemMemberModalName" onclick="event.stopPropagation()">
    <div class="chem-modal__head">
      <div>
        <div class="chem-modal__eyebrow">ACTIVE CHEMISTRY</div>
        <div class="chem-modal__title">
          <span class="chem-modal__grade" id="chemMemberModalGrade">${chemistry.chemGrade}</span>
          <span class="chem-modal__name" id="chemMemberModalName">${chemistry.chemLabel}</span>
        </div>
        <div class="chem-modal__bonus" id="chemMemberModalBonus"><i class="fas fa-bolt"></i> +${chemistry.totalBonus}% BOOST (시너지 ${chemistry.baseBonus}% + 등급 ${chemistry.gradeBonus}%)</div>
      </div>
      <button type="button" class="chem-modal__close" onclick="closeChemMemberModal()"><i class="fas fa-xmark"></i></button>
    </div>
    <div class="chem-modal__body">
      <div class="chem-modal__desc" id="chemMemberModalDesc"></div>
      <div class="chem-modal__grid" id="chemMemberModalGrid"></div>
    </div>
  </div>
</div>

<div class="chem-modal" id="chemCatalogModal" onclick="closeChemCatalogModal(event)">
  <div class="chem-modal__dialog" role="dialog" aria-modal="true" aria-labelledby="chemCatalogModalName" onclick="event.stopPropagation()">
    <div class="chem-modal__head">
      <div>
        <div class="chem-modal__eyebrow">CHEMISTRY GUIDE</div>
        <div class="chem-modal__title">
          <span class="chem-modal__grade"><i class="fas fa-list"></i></span>
          <span class="chem-modal__name" id="chemCatalogModalName">전체 케미스트리 종류</span>
        </div>
      </div>
      <button type="button" class="chem-modal__close" onclick="closeChemCatalogModal()"><i class="fas fa-xmark"></i></button>
    </div>
    <div class="chem-modal__body">
      <div class="chem-modal__desc">현재 프로젝트에 적용된 케미 조건 목록입니다. 실제 발동 시에는 상위 4개가 선택되며, 시너지 합산 보너스에 등급 보너스가 추가됩니다.</div>
      <div class="chem-list" id="chemCatalogList"></div>
    </div>
  </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
<script>
const CTX='${ctx}';
const RUN_ID='${result.runId}';
const rerollBtn=document.getElementById('rerollBtn');
const toast=document.getElementById('toastMsg');
const PERSONALITY_OPTIONS=[
  <c:forEach var="p" items="${personalityOptions}" varStatus="st">
  {code:'${p.name()}',label:'${p.shortLabel}'}<c:if test="${!st.last}">,</c:if>
  </c:forEach>
];
const CHEMISTRY_CATALOG=[
  {name:'하모니 라인',bonus:4,desc:'팀 보컬 평균이 높을 때 발동. 상위 보컬 멤버가 핵심입니다.'},
  {name:'퍼포먼스 라인',bonus:4,desc:'팀 댄스 평균이 높을 때 발동. 퍼포먼스 강점 조합입니다.'},
  {name:'안정된 팀워크',bonus:6,desc:'멘탈과 팀워크 평균이 모두 높을 때 발동. 흔들림이 적습니다.'},
  {name:'완벽한 조화',bonus:6,desc:'남녀 2:2 혼성 조합일 때 발동. 혼성 무대 시너지가 큽니다.'},
  {name:'동일 성별 결속',bonus:6,desc:'동일 성별 4인 조합일 때 발동. 팀 호흡이 빨리 맞습니다.'},
  {name:'꿀보이스 조합',bonus:4,desc:'고보컬 멤버가 2명 이상일 때 발동. 파트 분배가 안정됩니다.'},
  {name:'칼군무 라인',bonus:4,desc:'고댄스 멤버가 2명 이상일 때 발동. 퍼포먼스 완성도가 올라갑니다.'},
  {name:'친구 사이',bonus:3,desc:'동갑 멤버가 2명 이상일 때 발동. 멘탈 케어와 친밀도가 높습니다.'},
  {name:'분위기 메이커',bonus:4,desc:'멘탈/팀워크 강점 멤버가 많을 때 발동. 팀 텐션이 안정됩니다.'},
  {name:'무대 장악',bonus:6,desc:'스타성 높은 멤버가 3명 이상일 때 발동. 무대 집중력이 크게 올라갑니다.'},
  {name:'시선 캐치',bonus:3,desc:'스타성 높은 멤버가 2명일 때 발동. 시선 집중도를 보강합니다.'},
  {name:'스타 포지션 밸런스',bonus:4,desc:'보컬/댄스/스타 평균이 고르게 높을 때 발동. 무대 핵심 축이 안정적입니다.'},
  {name:'멘탈 버팀목',bonus:3,desc:'고멘탈 멤버가 3명 이상일 때 발동. 장기전에서 흔들림이 줄어듭니다.'},
  {name:'팀워크 코어',bonus:3,desc:'고팀워크 멤버가 3명 이상일 때 발동. 협업 완성도가 높아집니다.'},
  {name:'에이스 듀오',bonus:6,desc:'총합 높은 멤버가 2명 이상일 때 발동. 팀 중심축이 단단해집니다.'},
  {name:'올라운더 밸런스',bonus:3,desc:'모든 평균 스탯이 일정 기준 이상일 때 발동. 전체 밸런스형 조합입니다.'},
  {name:'등급 보너스 C',bonus:1,desc:'활성 시너지 1개일 때 추가 적용됩니다.'},
  {name:'등급 보너스 B',bonus:3,desc:'활성 시너지 2개일 때 추가 적용됩니다.'},
  {name:'등급 보너스 A',bonus:5,desc:'활성 시너지 3개일 때 추가 적용됩니다.'},
  {name:'등급 보너스 S',bonus:10,desc:'활성 시너지 4개 이상일 때 추가 적용됩니다.'}
];
let currentChemistry={
  chemGrade:'${chemistry.chemGrade}',
  chemLabel:'${chemistry.chemLabel}',
  baseBonus:${chemistry.baseBonus},
  gradeBonus:${chemistry.gradeBonus},
  totalBonus:${chemistry.totalBonus},
  synergies:[
    <c:forEach var="syn" items="${chemistry.synergies}" varStatus="st">
    {name:'${syn.name}',description:'${syn.description}',icon:'${syn.icon}',bonusPct:${syn.bonusPct},involvedMembers:[<c:forEach var="member" items="${syn.involvedMembers}" varStatus="memberSt">'${member}'<c:if test="${!memberSt.last}">,</c:if></c:forEach>]}<c:if test="${!st.last}">,</c:if>
    </c:forEach>
  ]
};

function showToast(msg){
  toast.textContent=msg;
  toast.classList.add('show');
  setTimeout(function(){ toast.classList.remove('show'); },1800);
}

function esc(s){
  return String(s ?? '').replace(/[&<>"']/g,function(ch){
    return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch];
  });
}

function total(m){
  return (m.vocal||0)+(m.dance||0)+(m.star||0)+(m.mental||0)+(m.teamwork||0);
}

function rosterCard(m,i){
  var pcg = m && m.photoCardGrade ? String(m.photoCardGrade).trim().toUpperCase() : '';
  var pcDisplay = (pcg === 'R' || pcg === 'SR' || pcg === 'SSR') ? pcg : 'NONE';
  var pcBadgeCls = pcDisplay === 'NONE' ? 'none' : pcDisplay.toLowerCase();
  var pcCls = '';
  if (pcg === 'R' || pcg === 'SR' || pcg === 'SSR') {
    pcCls = ' card-glow-' + pcg.toLowerCase();
  }
  var img = m.imagePath
    ? '<img src="' + CTX + m.imagePath + '" alt="' + esc(m.name) + '">'
    : '<div style="height:100%;display:flex;align-items:center;justify-content:center;color:rgba(255,255,255,.2);font-size:68px"><i class="fas fa-user"></i></div>';
  var lv = Number(m.enhanceLevel || 0);
  if (!isFinite(lv) || lv < 0) lv = 0;
  if (lv > 5) lv = 5;
  var lvText = lv >= 5 ? 'MAX' : ('+' + lv);
  var enhanceBonus = lv >= 5 ? 7 : lv;
  var genderText = m.gender === 'MALE' ? '남자' : '여자';
  var gr = (m.grade && String(m.grade).trim()) ? String(m.grade).trim() : '';
  var tierHtml = gr
    ? '<span class="tier-badge tier--' + esc(gr.toLowerCase()) + '">' + esc(gr) + '</span>'
    : '';
  var personalitySelect = '<select class="js-personality" data-trainee-id="' + (m.traineeId||'') + '"'
    + ' style="width:100%;padding:8px 10px;border-radius:12px;border:1px solid rgba(167,139,250,.35);background:rgba(255,255,255,.9);font-weight:800;color:#334155;">'
    + PERSONALITY_OPTIONS.map(function(p){
        var sel = (String(m.personalityCode||'') === String(p.code)) ? ' selected' : '';
        return '<option value="' + esc(p.code) + '"' + sel + '>' + esc(p.label) + '</option>';
      }).join('')
    + '</select>';
  return ''
    + '<article class="card' + pcCls + '" style="animation-delay:' + (i*0.06) + 's">'
    +   '<div class="photo">'
    +     '<span class="pick">' + m.pickOrder + '</span>'
    +     img
    +     '<div class="namebox">'
    +       '<div class="name">' + esc(m.name) + '</div>'
    +       '<div class="badges"><span class="badge">' + genderText + '</span>' + tierHtml + '<span class="enhance-pill lv-' + lv + '">' + lvText + '</span><span class="pc-pill pc-pill--' + pcBadgeCls + '">PC ' + pcDisplay + '</span></div>'
    +     '</div>'
    +   '</div>'
    +   '<div class="body">'
    +     '<div class="srow"><span class="slbl">성격</span><div style="flex:1;display:flex;gap:8px;align-items:center">' + personalitySelect + '</div></div>'
    +     '<div class="srow"><span class="slbl">보컬</span><div class="track"><div class="fill v" style="width:' + (m.vocal||0) + '%"></div></div><span class="sval" data-eb="' + enhanceBonus + '">' + (m.vocal||0) + '</span></div>'
    +     '<div class="srow"><span class="slbl">댄스</span><div class="track"><div class="fill d" style="width:' + (m.dance||0) + '%"></div></div><span class="sval" data-eb="' + enhanceBonus + '">' + (m.dance||0) + '</span></div>'
    +     '<div class="srow"><span class="slbl">스타</span><div class="track"><div class="fill s" style="width:' + (m.star||0) + '%"></div></div><span class="sval" data-eb="' + enhanceBonus + '">' + (m.star||0) + '</span></div>'
    +     '<div class="srow"><span class="slbl">멘탈</span><div class="track"><div class="fill m" style="width:' + (m.mental||0) + '%"></div></div><span class="sval" data-eb="' + enhanceBonus + '">' + (m.mental||0) + '</span></div>'
    +     '<div class="srow"><span class="slbl">팀웍</span><div class="track"><div class="fill t" style="width:' + (m.teamwork||0) + '%"></div></div><span class="sval" data-eb="' + enhanceBonus + '">' + (m.teamwork||0) + '</span></div>'
    +     '<div class="total"><span class="tt">TOTAL</span><span class="tv">' + total(m) + '</span></div>'
    +   '</div>'
    + '</article>';
}

function chemistryDescription(chem){
  if(chem && chem.synergies && chem.synergies.length){
    var msg='현재 조합은 ' + (chem.chemLabel || '기본 케미') + ' 상태입니다.';
    if((chem.totalBonus||0)>0){
      msg += ' 총 보너스 +' + chem.totalBonus + '%가 적용됩니다.';
      msg += ' (시너지 ' + (chem.baseBonus||0) + '% + 등급 ' + (chem.gradeBonus||0) + '%)';
    }
    msg += ' 멤버 확인에서 어떤 멤버가 어떤 시너지에 들어갔는지 바로 볼 수 있습니다.';
    return msg;
  }
  return '아직 강하게 발동한 케미는 없습니다. 다시뽑기나 게임 진행 중 능력치 변화로 조합이 바뀔 수 있습니다.';
}

function chemistryBonusText(chem){
  var total=chem&&chem.totalBonus?chem.totalBonus:0;
  if(total<=0) return '';
  return '+' + total + '% BOOST (시너지 ' + (chem.baseBonus||0) + '% + 등급 ' + (chem.gradeBonus||0) + '%)';
}

function renderChemMemberModal(){
  var grade=currentChemistry.chemGrade||'D';
  var label=currentChemistry.chemLabel||'케미 없음';
  document.getElementById('chemMemberModalGrade').textContent=grade;
  document.getElementById('chemMemberModalName').textContent=label;
  document.getElementById('chemMemberModalDesc').textContent=chemistryDescription(currentChemistry);
  var bonus=document.getElementById('chemMemberModalBonus');
  if((currentChemistry.totalBonus||0)>0){
    bonus.style.display='inline-flex';
    bonus.innerHTML='<i class="fas fa-bolt"></i> ' + chemistryBonusText(currentChemistry);
  }else{
    bonus.style.display='none';
  }
  var grid=document.getElementById('chemMemberModalGrid');
  var list=currentChemistry.synergies||[];
  if(!list.length){
    grid.innerHTML='<div class="chem-chip"><div class="chem-chip__icon"><i class="fas fa-chart-line"></i></div><div><div class="chem-chip__title">아직 강한 조합 없음</div><div class="chem-chip__desc">현재 로스터에는 활성 시너지가 없습니다.</div></div></div>';
    return;
  }
  grid.innerHTML=list.map(function(item){
    var members=(item.involvedMembers||[]).map(function(name){
      return '<span class="chem-chip__member">' + esc(name) + '</span>';
    }).join('');
    return '<div class="chem-chip">'
      + '<div class="chem-chip__icon">' + esc(item.icon||'✦') + '</div>'
      + '<div>'
      +   '<div class="chem-chip__title">' + esc(item.name||'시너지') + ' · +' + (item.bonusPct||0) + '%</div>'
      +   '<div class="chem-chip__desc">' + esc(item.description||'') + '</div>'
      +   (members ? '<div class="chem-chip__members">' + members + '</div>' : '')
      + '</div>'
      + '</div>';
  }).join('');
}

function renderCatalogModal(){
  document.getElementById('chemCatalogList').innerHTML=CHEMISTRY_CATALOG.map(function(item){
    return '<div class="chem-list-item"><strong>' + esc(item.name) + ' · +' + (item.bonus||0) + '%</strong><span>' + esc(item.desc) + '</span></div>';
  }).join('');
}

function renderChem(chem){
  currentChemistry=chem||{chemGrade:'D',chemLabel:'케미 없음',baseBonus:0,gradeBonus:0,totalBonus:0,synergies:[]};
  document.getElementById('chemGrade').textContent = currentChemistry.chemGrade || '-';
  document.getElementById('chemGrade').className = 'chem-grade g-' + (currentChemistry.chemGrade || 'D');
  document.getElementById('chemLabel').textContent = currentChemistry.chemLabel || '기본 케미';
  document.getElementById('chemDesc').textContent = chemistryDescription(currentChemistry);

  var bonus = document.getElementById('chemBonus');
  var totalBonus = currentChemistry.totalBonus || 0;
  var actions=document.querySelector('.chem-actions');
  if (totalBonus > 0) {
    if (!bonus) {
      bonus = document.createElement('span');
      bonus.id = 'chemBonus';
      bonus.className = 'chem-bonus';
      actions.insertBefore(bonus, document.getElementById('chemMemberBtn'));
    }
    bonus.textContent = chemistryBonusText(currentChemistry);
  } else if (bonus) {
    bonus.remove();
  }

  var points = document.getElementById('chemPoints');
  if (currentChemistry.synergies && currentChemistry.synergies.length) {
    points.innerHTML = currentChemistry.synergies.map(function(s){
      return '<div class="chem-point"><div class="chem-icon">' + esc(s.icon || '✦') + '</div><div class="chem-text"><strong>' + esc(s.name) + ' · +' + (s.bonusPct||0) + '%</strong><span>' + esc(s.description || '') + '</span></div></div>';
    }).join('');
  } else {
    points.innerHTML = ''
      + '<div class="chem-point"><div class="chem-icon"><i class="fas fa-sparkles"></i></div><div class="chem-text"><strong>추가 시너지 없음</strong><span>현재 조합은 기본 성능만 반영됩니다.</span></div></div>'
      + '<div class="chem-point"><div class="chem-icon"><i class="fas fa-chart-line"></i></div><div class="chem-text"><strong>성장 여지 있음</strong><span>진행 중 능력치 변화로 후반 결과가 달라질 수 있습니다.</span></div></div>';
  }

  renderChemMemberModal();
}

function placeChemModal(modalId){
  var modal = document.getElementById(modalId);
  if(!modal || !modal.classList.contains('show')) return;

  var dialog = modal.querySelector('.chem-modal__dialog');
  var body = modal.querySelector('.chem-modal__body');
  var head = modal.querySelector('.chem-modal__head');
  if(!dialog || !body || !head) return;

  var safe = window.innerWidth <= 768 ? 16 : 24;
  var viewportHeight = window.innerHeight;
  var reserved = head.offsetHeight + safe * 2 + 24;
  var bodyMax = Math.max(180, viewportHeight - reserved);
  body.style.maxHeight = bodyMax + 'px';

  requestAnimationFrame(function(){
    var dialogHeight = dialog.offsetHeight;
    var topSpace = Math.max(safe, Math.floor((viewportHeight - dialogHeight) / 2));
    modal.style.paddingTop = topSpace + 'px';
    modal.style.paddingBottom = safe + 'px';
    body.scrollTop = 0;
  });
}

function showChemModal(modalId){
  var modal = document.getElementById(modalId);
  if(!modal) return;
  modal.classList.add('show');
  document.body.style.overflow='hidden';
  requestAnimationFrame(function(){
    placeChemModal(modalId);
  });
}

function openChemMemberModal(){
  renderChemMemberModal();
  showChemModal('chemMemberModal');
}
function closeChemMemberModal(evt){
  if(evt && evt.target && evt.target !== document.getElementById('chemMemberModal')) return;
  document.getElementById('chemMemberModal').classList.remove('show');
  document.body.style.overflow='';
}
function openChemCatalogModal(){
  renderCatalogModal();
  showChemModal('chemCatalogModal');
}
function closeChemCatalogModal(evt){
  if(evt && evt.target && evt.target !== document.getElementById('chemCatalogModal')) return;
  document.getElementById('chemCatalogModal').classList.remove('show');
  document.body.style.overflow='';
}


window.addEventListener('resize',function(){
  placeChemModal('chemMemberModal');
  placeChemModal('chemCatalogModal');
});

document.addEventListener('keydown',function(evt){
  if(evt.key==='Escape'){
    closeChemMemberModal();
    closeChemCatalogModal();
  }
});

async function doReroll(){
  if (rerollBtn.disabled) return;
  rerollBtn.disabled = true;
  try {
    const res = await fetch(CTX + '/game/run/' + RUN_ID + '/reroll', {
      method: 'POST',
      headers: {'X-Requested-With': 'XMLHttpRequest'}
    });
    const data = await res.json();
    if (!res.ok) {
      showToast(data.error || '다시뽑기에 실패했습니다.');
      if ((data.rerollRemaining || 0) > 0) rerollBtn.disabled = false;
      return;
    }
    document.getElementById('rosterGrid').innerHTML = (data.roster || []).map(rosterCard).join('');
    renderChem(data.chemistry || {});
    document.getElementById('rerollRemaining').textContent = data.rerollRemaining || 0;
    rerollBtn.disabled = (data.rerollRemaining || 0) <= 0;
    startRerollTimer(data.rerollRemaining || 0, data.nextChargeInSeconds || 0);
    window.scrollTo({ top: 0, behavior: 'smooth' });
    showToast(data.message || '로스터를 다시 선발했습니다.');
  } catch (e) {
    rerollBtn.disabled = false;
    showToast('다시뽑기에 실패했습니다.');
  }
}

rerollBtn?.addEventListener('click', doReroll);

// 성격 변경 저장(이벤트 위임)
let personalitySaving = false;
document.getElementById('rosterGrid')?.addEventListener('change', async function(e){
  const el = e && e.target ? e.target : null;
  if(!el || !el.classList || !el.classList.contains('js-personality')) return;
  const traineeId = el.getAttribute('data-trainee-id');
  const personalityCode = el.value;
  if(!traineeId || !personalityCode) return;
  if(personalitySaving) return;
  personalitySaving = true;
  try{
    const res = await fetch(CTX + '/game/run/' + RUN_ID + '/roster/personality', {
      method:'POST',
      headers:{'Content-Type':'application/json','X-Requested-With':'XMLHttpRequest'},
      body: JSON.stringify({ traineeId: traineeId, personalityCode: personalityCode })
    });
    const data = await res.json();
    if(!res.ok){
      showToast((data && data.error) ? data.error : '성격 저장에 실패했습니다.');
      return;
    }
    showToast((data && data.message) ? data.message : '성격을 저장했습니다.');
  }catch(err){
    showToast('성격 저장에 실패했습니다.');
  }finally{
    personalitySaving = false;
  }
});

const rerollChargeInfo=document.getElementById('rerollChargeInfo');
let rerollTimer=null;

function formatRemain(sec){
  var total=Math.max(0, Number(sec)||0);
  var h=Math.floor(total/3600);
  var m=Math.floor((total%3600)/60);
  var s=total%60;
  if(h>0){
    return String(h).padStart(2,'0')+':'+String(m).padStart(2,'0')+':'+String(s).padStart(2,'0');
  }
  return String(m).padStart(2,'0')+':'+String(s).padStart(2,'0');
}

function renderRerollChargeInfo(remaining, seconds){
  if(!rerollChargeInfo) return;
  if((Number(remaining)||0) >= 3){
    rerollChargeInfo.textContent='충전 완료 · 최대 3/3';
    return;
  }
  var sec=Math.max(0, Number(seconds)||0);
  rerollChargeInfo.textContent='다음 충전까지 '+formatRemain(sec);
}

function startRerollTimer(remaining, seconds){
  if(rerollTimer){ clearInterval(rerollTimer); rerollTimer=null; }
  var remain=Math.max(0, Number(seconds)||0);
  renderRerollChargeInfo(remaining, remain);
  if((Number(remaining)||0) >= 3) return;
  rerollTimer=setInterval(function(){
    remain=Math.max(0, remain-1);
    renderRerollChargeInfo(remaining, remain);
    if(remain <= 0){
      clearInterval(rerollTimer);
      rerollTimer=null;
      syncRerollFromServer();
    }
  }, 1000);
}

async function syncRerollFromServer(){
  try{
    const res = await fetch(CTX + '/game/run/' + RUN_ID + '/reroll/status', {
      method: 'GET',
      headers: { 'X-Requested-With': 'XMLHttpRequest' }
    });
    if(!res.ok) return;
    const data = await res.json();
    const n = Number(data && data.rerollRemaining != null ? data.rerollRemaining : 0) || 0;
    const sec = Number(data && data.nextChargeInSeconds != null ? data.nextChargeInSeconds : 0) || 0;
    const el = document.getElementById('rerollRemaining');
    if (el) el.textContent = n;
    if (rerollBtn) rerollBtn.disabled = n <= 0;
    startRerollTimer(n, sec);
  }catch(e){}
}

window.addEventListener('pageshow', function(e){
  if (e && e.persisted) syncRerollFromServer();
  else syncRerollFromServer();
});

const itemPickBoxes = Array.from(document.querySelectorAll('.js-item-pick'));
const itemPickCount = document.getElementById('itemPickCount');

function selectedItemIds(){
  return itemPickBoxes.filter(function(box){ return box.checked; }).map(function(box){ return box.value; });
}

function renderItemPickCount(){
  if(!itemPickCount) return;
  itemPickCount.textContent = selectedItemIds().length + '/5 선택';
}

itemPickBoxes.forEach(function(box){
  box.addEventListener('change', function(){
    var picked = selectedItemIds();
    if(picked.length > 5){
      this.checked = false;
      showToast('아이템은 최대 5개까지 선택할 수 있습니다.');
      return;
    }
    renderItemPickCount();
  });
});

document.getElementById('gameStartBtn').addEventListener('click', function(e){
  e.preventDefault();
  var ids = selectedItemIds();
  var base = this.href;
  if(ids.length === 0){
    window.location.href = base;
    return;
  }
  window.location.href = base + '?itemIds=' + encodeURIComponent(ids.join(','));
});
(function(){
  renderChem(currentChemistry);
  renderCatalogModal();
  renderItemPickCount();
})();
</script>
</body>
</html>
