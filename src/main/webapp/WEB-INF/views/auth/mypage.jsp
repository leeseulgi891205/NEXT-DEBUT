<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NEXT DEBUT - 마이페이지</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js" defer></script>
  <style>
    :root{
      --mp-bg:#fff7fb;
      --mp-card:rgba(255,255,255,0.92);
      --mp-border:rgba(244,114,182,0.20);
      --mp-text:#2f2a2c;
      --mp-muted:#7d6670;
      --mp-pink:#f472b6;
      --mp-pink-deep:#ec4899;
      --mp-rose:#fda4af;
      --mp-gold:#f59e0b;
      --mp-shadow:0 18px 42px rgba(236,72,153,0.10);
      --mp-radius:18px;
    }
    *{box-sizing:border-box}
    body{
      margin:0;
      background:
        radial-gradient(1200px 540px at 18% -10%, rgba(251,207,232,0.45), transparent 60%),
        radial-gradient(900px 520px at 100% 0%, rgba(253,242,248,0.8), transparent 60%),
        var(--mp-bg);
      color:var(--mp-text);
    }
    .mypage-shell{
      max-width:1280px;
      margin:0 auto;
      padding:calc(var(--nav-h,68px) + 26px) 22px 44px;
    }
    .mypage-head{
      display:flex; justify-content:space-between; align-items:flex-end; gap:16px;
      margin-bottom:16px;
    }
    .mypage-title{
      font-family:"Orbitron",sans-serif;
      letter-spacing:.08em;
      font-size:clamp(24px,4vw,40px);
      margin:0;
      background:linear-gradient(100deg,#2f2a2c,var(--mp-pink),#f9a8d4);
      -webkit-background-clip:text;
      background-clip:text;
      -webkit-text-fill-color:transparent;
    }
    .mypage-sub{margin:6px 0 0; color:var(--mp-muted); font-size:13px; letter-spacing:.08em;}
    .mypage-toast{
      margin-bottom:14px; padding:12px 14px; border-radius:14px; font-size:13px;
    }
    .mypage-toast.ok{background:rgba(134,239,172,.18); border:1px solid rgba(134,239,172,.34);}
    .mypage-toast.err{background:rgba(251,113,133,.14); border:1px solid rgba(251,113,133,.34);}

    .mypage-grid{
      display:grid;
      grid-template-columns:320px 1fr 290px;
      gap:14px;
      align-items:start;
    }
    @media (max-width:1140px){ .mypage-grid{grid-template-columns:1fr;}}

    .card{
      background:var(--mp-card);
      border:1px solid var(--mp-border);
      border-radius:var(--mp-radius);
      box-shadow:var(--mp-shadow);
      backdrop-filter:blur(8px);
    }
    .card-head{
      padding:14px 16px 10px;
      border-bottom:1px solid rgba(244,114,182,0.12);
      font-family:"Orbitron",sans-serif;
      font-size:11px;
      letter-spacing:.20em;
      color:#b03d7a;
    }
    .card-body{padding:14px 16px 16px}

    .profile-top{display:flex; gap:14px; align-items:center; margin-bottom:14px;}
    .avatar{
      width:88px; height:88px; border-radius:18px; overflow:hidden;
      border:2px solid rgba(244,114,182,.28);
      box-shadow:0 10px 26px rgba(244,114,182,.18);
      flex-shrink:0;
      background:#fff;
      cursor:pointer;
      position:relative;
    }
    .avatar img{width:100%; height:100%; object-fit:cover}
    .avatar-fallback{width:100%; height:100%; display:flex; align-items:center; justify-content:center; color:#c08497; font-size:32px;}
    .avatar::after{
      content:"변경";
      position:absolute; right:6px; bottom:6px;
      font-size:10px; font-weight:800; color:#fff;
      background:rgba(236,72,153,.85);
      border-radius:999px; padding:3px 7px;
    }
    .nick{font-size:24px; font-weight:900; line-height:1.15}
    .grade{
      margin-top:6px; display:inline-flex; align-items:center; gap:6px;
      font-size:11px; font-weight:800; padding:5px 10px; border-radius:999px;
      background:rgba(244,114,182,.13); border:1px solid rgba(244,114,182,.24); color:#a31863;
    }
    .stat-row{display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-top:10px;}
    .mini{
      border:1px solid rgba(244,114,182,.16); border-radius:14px; padding:10px;
      background:rgba(255,255,255,.74);
    }
    .mini-k{font-size:10px; letter-spacing:.14em; color:#9d7185; font-family:"Orbitron",sans-serif;}
    .mini-v{margin-top:4px; font-size:22px; font-weight:900; line-height:1.1;}

    .gold-badge{
      margin-top:12px;
      border-radius:14px;
      border:1px solid rgba(245,158,11,.34);
      background:linear-gradient(120deg, rgba(255,247,213,.95), rgba(255,236,178,.9));
      display:flex; align-items:center; justify-content:space-between; gap:8px;
      padding:12px 14px;
    }
    .gold-left{display:flex; align-items:center; gap:10px;}
    .gold-icon{
      width:34px; height:34px; border-radius:50%;
      display:flex; align-items:center; justify-content:center;
      background:linear-gradient(130deg,#fde68a,#f59e0b);
      color:#7a4a00; font-size:15px; box-shadow:0 8px 18px rgba(245,158,11,.24);
    }
    .gold-k{font-size:10px; letter-spacing:.18em; font-family:"Orbitron",sans-serif; color:#9a6800;}
    .gold-v{font-family:"Orbitron",sans-serif; font-size:24px; font-weight:900; color:#a45a00;}

    .best-run{
      border-radius:14px; padding:12px; border:1px solid rgba(244,114,182,.18);
      background:linear-gradient(145deg,rgba(255,255,255,.92),rgba(255,241,247,.9));
      margin-bottom:12px;
    }
    .best-run h4{margin:0 0 8px; font-size:13px; letter-spacing:.08em; color:#9f336f;}
    .best-run-grid{display:grid; grid-template-columns:repeat(3,minmax(0,1fr)); gap:8px;}
    .best-item{background:#fff; border:1px solid rgba(244,114,182,.14); border-radius:12px; padding:8px;}
    .best-item .k{font-size:9px; color:#a47c8e; letter-spacing:.12em;}
    .best-item .v{font-size:16px; font-weight:900; margin-top:3px;}

    .rep-char{
      display:flex; align-items:center; justify-content:space-between;
      gap:10px; border:1px solid rgba(244,114,182,.14); border-radius:14px;
      padding:10px 12px; background:#fff;
    }
    .rep-char-name{font-weight:900; font-size:14px;}
    .rep-char-tag{
      font-size:10px; padding:4px 8px; border-radius:999px;
      border:1px solid rgba(253,164,175,.44); color:#aa3455; background:rgba(255,241,242,.9);
    }

    .history-list{display:flex; flex-direction:column; gap:9px;}
    .history-row{
      border-radius:14px; border:1px solid rgba(244,114,182,.14); background:#fff;
      padding:10px 12px; display:flex; align-items:center; justify-content:space-between; gap:8px;
    }
    .history-left{min-width:0;}
    .history-title{font-size:13px; font-weight:800; color:#3f2a33; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;}
    .history-meta{margin-top:4px; font-size:11px; color:#926b7d;}
    .pill{
      font-size:10px; font-family:"Orbitron",sans-serif; letter-spacing:.08em;
      padding:5px 8px; border-radius:999px; border:1px solid rgba(244,114,182,.2); color:#b03076; background:rgba(253,242,248,.9);
      white-space:nowrap;
    }
    .history-link{
      margin-top:10px; display:inline-flex; align-items:center; gap:6px;
      font-size:12px; color:#a31863; text-decoration:none; font-weight:800;
    }

    .stats-grid{display:grid; gap:10px;}
    .stat-box{
      background:#fff; border:1px solid rgba(244,114,182,.14); border-radius:14px;
      padding:12px;
    }
    .stat-box .k{
      font-size:10px; letter-spacing:.14em; color:#9d7185; font-family:"Orbitron",sans-serif;
    }
    .stat-box .v{
      margin-top:5px; font-family:"Orbitron",sans-serif; font-size:25px; font-weight:900;
      color:#2f2a2c;
    }
    .rank-card{
      border-radius:14px; border:1px solid rgba(190,24,93,.16);
      padding:12px; background:linear-gradient(135deg,rgba(255,255,255,.96),rgba(255,236,244,.92));
    }
    .rank-title{font-size:11px; letter-spacing:.12em; color:#a31863; font-family:"Orbitron",sans-serif;}
    .rank-value{margin-top:6px; font-size:20px; font-weight:900;}
    .settings{
      margin-top:10px;
      border:1px solid rgba(244,114,182,.16);
      border-radius:14px;
      background:#fff;
      padding:10px;
    }
    .settings-head{
      display:flex; justify-content:space-between; align-items:center;
      margin-bottom:8px;
      font-family:"Orbitron",sans-serif; font-size:10px; letter-spacing:.14em; color:#a33674;
    }
    .settings-panel{display:none; margin-top:8px;}
    .settings-panel.open{display:block;}
    .settings-acc{display:flex; flex-direction:column; gap:7px;}
    .settings-item{
      border:1px solid rgba(244,114,182,.18);
      border-radius:12px;
      background:rgba(255,250,252,.92);
      overflow:hidden;
    }
    .settings-toggle{
      width:100%;
      border:0;
      background:transparent;
      cursor:pointer;
      display:flex;
      align-items:center;
      justify-content:space-between;
      padding:10px 11px;
      font-size:12px;
      color:#8d2f63;
      font-weight:800;
      text-align:left;
    }
    .settings-toggle i{transition:transform .2s ease}
    .settings-toggle.active i{transform:rotate(180deg)}
    .settings-content{
      display:none;
      padding:0 10px 10px;
      border-top:1px solid rgba(244,114,182,.14);
      background:#fff;
    }
    .settings-content.open{display:block}
    .settings-panel form{display:grid; gap:8px;}
    .settings-panel label{font-size:11px; color:#8e6476; font-weight:700;}
    .settings-panel input{
      width:100%; border:1px solid rgba(244,114,182,.2); border-radius:10px; padding:9px 10px;
      background:#fff; color:#2f2a2c; font-size:13px;
    }
    .settings-submit{
      border:0; border-radius:10px; padding:9px 10px; cursor:pointer;
      background:linear-gradient(135deg,#f472b6,#f9a8d4); color:#fff; font-weight:800; font-size:12px;
    }
    .pw-match-msg{display:none; font-size:11px;}

    .empty-note{
      text-align:center; padding:22px 10px; color:#9d7f8b; font-size:13px;
      border:1px dashed rgba(244,114,182,.3); border-radius:14px; background:rgba(255,255,255,.7);
    }
    .withdraw-wrap{margin-top:12px; display:flex; justify-content:flex-end;}
    .withdraw-btn{
      border:1px solid rgba(251,113,133,.38); background:rgba(255,241,242,.8); color:#be123c;
      border-radius:10px; padding:7px 10px; font-size:11px; font-weight:700; cursor:pointer;
    }

    .withdraw-overlay {
      position:fixed; inset:0; z-index:10000; background:rgba(2,1,8,0.88);
      backdrop-filter:blur(24px); display:flex; align-items:center; justify-content:center;
      opacity:0; pointer-events:none; transition:opacity 350ms ease;
    }
    .withdraw-overlay.show { opacity:1; pointer-events:auto; }
    .withdraw-modal {
      position:relative; width:min(520px,94vw); border-radius:28px; overflow:hidden;
      background:linear-gradient(160deg,rgba(18,6,36,0.99),rgba(8,3,20,0.99));
      border:1px solid rgba(248,113,113,0.20); box-shadow:0 60px 120px rgba(0,0,0,0.90);
      transform:translateY(60px) scale(0.88); transition:transform 460ms cubic-bezier(.22,1.4,.46,.98);
    }
    .withdraw-overlay.show .withdraw-modal { transform:translateY(0) scale(1); }
    .withdraw-topbar { height:3px; background:linear-gradient(90deg,rgba(248,113,113,0.9),rgba(251,146,60,0.7)); }
    .withdraw-close {
      position:absolute; top:16px; right:16px; width:30px; height:30px; border-radius:50%;
      background:rgba(255,255,255,0.07); border:1px solid rgba(255,255,255,0.13); color:rgba(255,255,255,0.50);
      font-size:11px; display:flex; align-items:center; justify-content:center; cursor:pointer;
      transition:all 220ms ease;
    }
    .withdraw-close:hover { background:rgba(255,255,255,0.17); color:#fff; transform:rotate(90deg); }
    .withdraw-body { padding:28px 28px 24px; }
    .withdraw-title { font-family:"Orbitron",sans-serif; font-size:15px; font-weight:900; color:rgba(248,113,113,0.90); text-align:center; margin-bottom:4px; letter-spacing:0.10em; }
    .withdraw-sub { text-align:center; font-size:11px; color:rgba(255,255,255,0.30); letter-spacing:0.14em; margin-bottom:20px; font-family:"Orbitron",sans-serif; }
    .terms-box { height:220px; overflow-y:auto; background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.10); border-radius:16px; padding:18px 20px; margin-bottom:16px; font-size:12px; line-height:1.85; color:rgba(255,255,255,0.65); }
    .terms-box::-webkit-scrollbar { width:4px; }
    .terms-box::-webkit-scrollbar-thumb { background:rgba(248,113,113,0.35); border-radius:4px; }
    .terms-box h4 { font-family:"Orbitron",sans-serif; font-size:10px; letter-spacing:0.20em; color:rgba(248,113,113,0.75); margin:16px 0 6px; }
    .terms-box h4:first-child { margin-top:0; }
    .terms-scroll-hint { text-align:center; font-size:10px; color:rgba(248,113,113,0.55); font-family:"Orbitron",sans-serif; letter-spacing:0.14em; margin-bottom:14px; transition:opacity 300ms ease; }
    .withdraw-agree { display:flex; align-items:center; gap:10px; padding:12px 16px; border-radius:12px; border:1px solid rgba(255,255,255,0.10); background:rgba(255,255,255,0.04); margin-bottom:16px; cursor:pointer; }
    .withdraw-agree input[type="checkbox"] { width:16px; height:16px; accent-color:rgba(248,113,113,0.9); cursor:pointer; flex-shrink:0; }
    .withdraw-agree label { font-size:12px; color:rgba(255,255,255,0.70); cursor:pointer; line-height:1.4; }
    .withdraw-agree label span { color:rgba(248,113,113,0.85); font-weight:700; }
    .withdraw-pw-wrap { margin-bottom:16px; display:none; }
    .withdraw-pw-wrap.show { display:block; }
    .withdraw-pw-wrap label { display:block; font-size:11px; color:rgba(255,255,255,0.50); margin-bottom:6px; letter-spacing:0.10em; }
    .withdraw-pw-wrap input { width:100%; padding:12px 16px; border-radius:12px; background:rgba(255,255,255,0.07); border:1px solid rgba(255,255,255,0.15); color:#fff; font-size:14px; outline:none; box-sizing:border-box; }
    .withdraw-pw-wrap input:focus { border-color:rgba(248,113,113,0.50); }
    .withdraw-submit { width:100%; padding:14px; border-radius:14px; background:rgba(248,113,113,0.10); border:1px solid rgba(248,113,113,0.30); color:rgba(248,113,113,0.60); font-family:"Orbitron",sans-serif; font-size:10px; font-weight:700; letter-spacing:0.20em; cursor:not-allowed; transition:all 250ms ease; display:flex; align-items:center; justify-content:center; gap:8px; }
    .withdraw-submit.ready { color:rgba(248,113,113,1); border-color:rgba(248,113,113,0.60); background:rgba(248,113,113,0.15); cursor:pointer; }
    .withdraw-submit.ready:hover { background:rgba(248,113,113,0.25); transform:translateY(-1px); }

    /* semantic rule: pink=accent, lavender=system */
    :root{
      --mp-system:#C4B5FD;
      --mp-system-soft:#F5F3FF;
      --mp-system-line:rgba(196,181,253,.55);
      --mp-accent:#FF8FAB;
      --mp-accent-deep:#fb6f92;
    }
    body{
      background:
        radial-gradient(1200px 540px at 18% -10%, rgba(205,185,255,.30), transparent 60%),
        radial-gradient(900px 520px at 100% 0%, rgba(232,221,255,.72), transparent 60%),
        #F5F3FF;
    }
    .card,
    .mini,
    .best-run,
    .best-item,
    .rep-char,
    .history-row,
    .stat-box,
    .rank-card,
    .settings,
    .settings-item,
    .settings-content,
    .empty-note{
      border-color:var(--mp-system-line) !important;
    }
    .card-head{
      border-bottom:1px solid var(--mp-system-line);
      color:#7f62a3;
    }
    .mypage-title{
      background:linear-gradient(100deg,#2f2a2c,var(--mp-accent),#f2c7ea);
      -webkit-background-clip:text;
      background-clip:text;
    }
    .avatar{
      border-color:var(--mp-system-line);
      box-shadow:0 10px 26px rgba(205,185,255,.22);
    }
    .grade,
    .pill,
    .rep-char-tag{
      border-color:var(--mp-system-line);
      background:rgba(232,221,255,.52);
      color:#7f4da4;
    }
    .history-link,
    .rank-title{
      color:var(--mp-accent-deep);
    }
    .settings-toggle{
      color:#7f4da4;
    }
    .settings-content{
      border-top:1px solid var(--mp-system-line);
    }
    .mypage-shell,.mypage-title,.nick,.history-title,.stat-box .v{color:#1F2937;}
    .mypage-sub,.history-meta,.mini-k,.stat-box .k{color:#6B7280;}

    /* 마이페이지 추가 UI */
    .mp-hidden{display:none;}
    .mp-more-btn{
      margin-top:10px;
      display:inline-flex; align-items:center; gap:6px;
      border:0; background:transparent; cursor:pointer;
      color:#a31863; font-weight:800; font-size:12px; padding:0;
    }
    .mp-shop-summary{
      margin-top:12px;
      padding-top:12px;
      border-top:1px solid rgba(244,114,182,0.12);
    }
    .mp-shop-top{
      display:flex; gap:10px; flex-wrap:wrap; margin-top:10px;
    }
    .mp-shop-top-item{
      flex:1;
      min-width:210px;
      display:flex; gap:10px; align-items:center;
      border:1px solid rgba(244,114,182,.12);
      background:rgba(255,255,255,.74);
      border-radius:14px;
      padding:10px;
    }
    .mp-shop-top-item img{
      width:44px; height:44px; border-radius:14px;
      object-fit:cover; border:1px solid rgba(244,114,182,.14);
      background:#fff; flex-shrink:0;
    }
    .mp-shop-top-item .name{font-weight:900;font-size:13px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;}
    .mp-shop-top-item .qty{margin-top:3px; font-size:12px; color:#6B7280; font-weight:800;}

    .mp-section-head{
      font-size:11px; letter-spacing:.16em; font-family:'Orbitron',sans-serif;
      color:#9d7185; margin:12px 0 8px;
    }
    .mp-phase-list{display:flex; gap:8px; flex-wrap:wrap; margin-top:8px;}
    .mp-note-list{display:flex; flex-direction:column; gap:9px;}
    .mp-note-row{
      border-radius:14px; border:1px solid rgba(244,114,182,.14); background:#fff;
      padding:10px 12px; display:flex; justify-content:space-between; gap:10px; align-items:flex-start;
    }
    .mp-note-title{font-weight:900; color:#3f2a33; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; max-width:210px;}
    .mp-note-meta{margin-top:4px; font-size:11px; color:#926b7d;}
  </style>
</head>
<body class="page-main min-h-screen flex flex-col">
  <%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

  <c:set var="playCount" value="${empty gameHistory ? 0 : gameHistory.size()}"/>
  <c:set var="finishedCount" value="0"/>
  <c:set var="sumScore" value="0"/>
  <c:set var="bestScore" value="0"/>
  <c:set var="bestRunId" value="0"/>
  <c:set var="bestRunPhase" value=""/>
  <c:set var="bestGroupType" value=""/>
  <c:set var="bestMainChar" value=""/>
  <c:forEach var="run" items="${gameHistory}">
    <c:set var="runScore" value="0"/>
    <c:forEach var="m" items="${run.roster}">
      <c:set var="runScore" value="${runScore + m.vocal + m.dance + m.star + m.mental + m.teamwork}"/>
    </c:forEach>
    <c:set var="sumScore" value="${sumScore + runScore}"/>
    <c:if test="${run.phase eq 'FINISHED'}"><c:set var="finishedCount" value="${finishedCount + 1}"/></c:if>
    <c:if test="${runScore gt bestScore}">
      <c:set var="bestScore" value="${runScore}"/>
      <c:set var="bestRunId" value="${run.runId}"/>
      <c:set var="bestRunPhase" value="${run.phase}"/>
      <c:set var="bestGroupType" value="${run.groupType}"/>
      <c:if test="${not empty run.roster}">
        <c:set var="bestMainChar" value="${run.roster[0].name}"/>
      </c:if>
    </c:if>
  </c:forEach>
  <c:set var="avgScore" value="${playCount gt 0 ? (sumScore / playCount) : 0}"/>

  <main class="flex-1">
    <div class="mypage-shell">
      <form id="avatarForm" method="post" action="${ctx}/mypage/profile-image" enctype="multipart/form-data" style="display:none;">
        <input type="file" id="avatarInput" name="file" accept="image/*" onchange="document.getElementById('avatarForm').submit();" />
      </form>
      <div class="mypage-head">
        <div>
          <h1 class="mypage-title">내 게임 프로필</h1>
          <p class="mypage-sub">내 기록과 성장 상태를 확인하는 대시보드</p>
        </div>
      </div>

      <c:if test="${not empty toast}">
        <div class="mypage-toast ok"><i class="fas fa-check-circle"></i> <c:out value="${toast}"/></div>
      </c:if>
      <c:if test="${not empty error}">
        <div class="mypage-toast err"><i class="fas fa-circle-exclamation"></i> <c:out value="${error}"/></div>
      </c:if>

      <section class="mypage-grid">
        <article class="card">
          <div class="card-head">프로필 카드</div>
          <div class="card-body">
            <div class="profile-top">
              <div class="avatar" onclick="document.getElementById('avatarInput').click();" title="프로필 이미지 변경">
                <c:choose>
                  <c:when test="${not empty member.profileImage}">
                    <img src="${ctx}/profile-image/${member.profileImage}" alt="프로필"/>
                  </c:when>
                  <c:otherwise>
                    <div class="avatar-fallback"><i class="fas fa-user"></i></div>
                  </c:otherwise>
                </c:choose>
              </div>
              <div>
                <div class="nick"><c:out value="${member.nickname}"/></div>
                <span class="grade"><i class="fas fa-star"></i>
                  <c:choose>
                    <c:when test="${not empty memberRankLabel}"><c:out value="${memberRankLabel}"/></c:when>
                    <c:otherwise>루키</c:otherwise>
                  </c:choose>
                  <span style="font-size:11px;color:#986b7f;font-weight:700;margin-left:6px;">회원 등급</span>
                </span>
              </div>
            </div>

            <div class="mp-trainee-pick" style="margin-top:12px;padding:12px;border-radius:14px;border:1px solid rgba(244,114,182,.22);background:rgba(255,255,255,.72);">
              <div style="display:flex;align-items:center;gap:12px;margin-bottom:8px;">
                <c:choose>
                  <c:when test="${not empty mypageCardTrainee}">
                    <div style="width:52px;height:52px;border-radius:14px;overflow:hidden;border:2px solid rgba(244,114,182,.35);flex-shrink:0;background:#fde7f3;">
                      <c:choose>
                        <c:when test="${not empty mypageCardTrainee.imagePath}">
                          <img src="${ctx}${mypageCardTrainee.imagePath}" alt="" style="width:100%;height:100%;object-fit:cover;"/>
                        </c:when>
                        <c:otherwise><div style="display:flex;align-items:center;justify-content:center;height:100%;"><i class="fas fa-user" style="color:#e879a9;"></i></div></c:otherwise>
                      </c:choose>
                    </div>
                    <div style="min-width:0;">
                      <div style="font-size:10px;color:#986b7f;letter-spacing:.14em;font-family:'Orbitron',sans-serif;">프로필 카드 연습생</div>
                      <div style="font-weight:900;font-size:15px;color:#2f2a2c;"><c:out value="${mypageCardTrainee.name}"/></div>
                    </div>
                  </c:when>
                  <c:otherwise>
                    <div style="font-size:12px;color:#8e6476;font-weight:800;">프로필 카드에 표시할 연습생을 선택하세요 (보유 연습생만).</div>
                  </c:otherwise>
                </c:choose>
              </div>
              <form method="post" action="${ctx}/mypage/card-trainee" style="display:flex;gap:8px;flex-wrap:wrap;align-items:center;">
                <select name="traineeId" style="flex:1;min-width:160px;border-radius:10px;border:1px solid rgba(244,114,182,.28);padding:8px 10px;font-size:13px;background:#fff;">
                  <option value="">선택 안 함</option>
                  <c:forEach var="ot" items="${ownedTraineesForMypage}">
                    <option value="${ot.id}" <c:if test="${member.mypageCardTraineeId ne null && member.mypageCardTraineeId eq ot.id}">selected="selected"</c:if>>
                      <c:out value="${ot.name}"/>
                    </option>
                  </c:forEach>
                </select>
                <button type="submit" style="border-radius:10px;border:none;background:linear-gradient(135deg,#f472b6,#ec4899);color:#fff;font-weight:800;padding:8px 14px;font-size:12px;cursor:pointer;">저장</button>
              </form>
              <c:if test="${empty ownedTraineesForMypage}">
                <div style="margin-top:8px;font-size:11px;color:#b48a9e;">연습생 목록에 보유 카드가 있어야 선택할 수 있습니다.</div>
              </c:if>
            </div>

            <div class="stat-row">
              <div class="mini">
                <div class="mini-k">최고 점수</div>
                <div class="mini-v">${bestScore}</div>
              </div>
              <div class="mini">
                <div class="mini-k">완료</div>
                <div class="mini-v">${finishedCount}</div>
              </div>
            </div>

            <div class="gold-badge">
              <div class="gold-left">
                <div class="gold-icon"><i class="fas fa-coins"></i></div>
                <div>
                  <div class="gold-k">현재 골드</div>
                  <div class="gold-v">${empty currentCoin ? 0 : currentCoin}</div>
                </div>
              </div>
            </div>

            <!-- 1) 상점 요약 -->
            <div class="mp-shop-summary">
              <div class="mp-section-head" style="margin:0 0 8px;">상점 · 인벤토리</div>
              <div style="font-size:12px;color:#6B7280;font-weight:800;line-height:1.6;">
                보유 아이템 <span style="color:#a31863;">${myItemTotalQty}</span>개 · 대표 아이템:
                <c:choose>
                  <c:when test="${empty repItemName}">없음</c:when>
                  <c:otherwise><span style="color:#7f4da4;">${repItemName}</span></c:otherwise>
                </c:choose>
              </div>

              <div style="margin-top:8px;font-size:12px;color:#6B7280;line-height:1.6;">
                효과:
                <c:choose>
                  <c:when test="${empty repItemEffect}">-</c:when>
                  <c:otherwise><c:out value="${repItemEffect}"/></c:otherwise>
                </c:choose>
              </div>

              <div class="mp-shop-top">
                <c:choose>
                  <c:when test="${empty myTopItems}">
                    <div class="empty-note" style="padding:14px 10px;">아직 보유 아이템이 없습니다.</div>
                  </c:when>
                  <c:otherwise>
                    <c:forEach var="it" items="${myTopItems}">
                      <div class="mp-shop-top-item">
                        <c:choose>
                          <c:when test="${not empty it.imagePath}">
                            <img src="${it.imagePath}" alt="${it.itemName}" />
                          </c:when>
                          <c:otherwise>
                            <img src="/images/items/star.png" alt="${it.itemName}" />
                          </c:otherwise>
                        </c:choose>
                        <div style="min-width:0;">
                          <div class="name"><c:out value="${it.itemName}"/></div>
                          <div class="qty">수량 ${it.quantity}개</div>
                        </div>
                      </div>
                    </c:forEach>
                  </c:otherwise>
                </c:choose>
              </div>

              <a class="history-link" href="${ctx}/market/shop" style="margin-top:12px;">
                <i class="fas fa-store"></i> 상점에서 구매/적용 확인
              </a>
            </div>
          </div>
        </article>

        <article class="card">
          <div class="card-head">플레이 기록</div>
          <div class="card-body">
            <section class="best-run">
              <h4><i class="fas fa-crown"></i> 베스트 런</h4>
              <div class="best-run-grid">
                <div class="best-item">
                  <div class="k">최고 점수</div>
                  <div class="v">${bestScore}</div>
                </div>
                <div class="best-item">
                  <div class="k">런 번호</div>
                  <div class="v">${bestRunId gt 0 ? bestRunId : '-'}</div>
                </div>
                <div class="best-item">
                  <div class="k">페이즈</div>
                  <div class="v">${empty bestRunPhase ? '-' : bestRunPhase}</div>
                </div>
              </div>
            </section>

            <section style="margin-bottom:12px;">
              <div class="rep-char">
                <div>
                  <div style="font-size:10px;color:#986b7f;letter-spacing:.14em;font-family:'Orbitron',sans-serif;">대표 캐릭터</div>
                  <div class="rep-char-name">
                    <c:choose>
                      <c:when test="${not empty mypageRepTrainee}"><c:out value="${mypageRepTrainee.name}"/></c:when>
                      <c:when test="${not empty bestMainChar}"><c:out value="${bestMainChar}"/></c:when>
                      <c:otherwise>미지정</c:otherwise>
                    </c:choose>
                  </div>
                </div>
                <span class="rep-char-tag">${empty bestGroupType ? '데이터 없음' : bestGroupType}</span>
              </div>
              <form method="post" action="${ctx}/mypage/rep-trainee" style="margin-top:10px;display:flex;gap:8px;flex-wrap:wrap;align-items:center;">
                <select name="traineeId" style="flex:1;min-width:160px;border-radius:10px;border:1px solid rgba(244,114,182,.28);padding:8px 10px;font-size:13px;background:#fff;">
                  <option value="">자동(베스트 런 기준)</option>
                  <c:forEach var="ot" items="${ownedTraineesForMypage}">
                    <option value="${ot.id}" <c:if test="${member.mypageRepTraineeId ne null && member.mypageRepTraineeId eq ot.id}">selected="selected"</c:if>>
                      <c:out value="${ot.name}"/>
                    </option>
                  </c:forEach>
                </select>
                <button type="submit" style="border-radius:10px;border:none;background:linear-gradient(135deg,#f472b6,#ec4899);color:#fff;font-weight:800;padding:8px 14px;font-size:12px;cursor:pointer;">저장</button>
              </form>
              <c:if test="${empty ownedTraineesForMypage}">
                <div style="margin-top:8px;font-size:11px;color:#b48a9e;">보유 연습생이 없으면 베스트 런 로스터 이름만 표시됩니다.</div>
              </c:if>
            </section>

            <section>
              <div style="font-size:11px;color:#9d7185;letter-spacing:.16em;font-family:'Orbitron',sans-serif;margin-bottom:8px;">최근 플레이 기록</div>
              <c:choose>
                <c:when test="${empty gameHistory}">
                  <div class="empty-note">아직 플레이 기록이 없습니다.</div>
                </c:when>
                <c:otherwise>
                  <div class="history-list">
                    <c:forEach var="run" items="${gameHistory}" varStatus="vs">
                      <div class="history-row mp-history-row ${vs.index >= 5 ? 'mp-hidden mp-history-extra' : ''}">
                        <div class="history-left">
                          <div class="history-title">런 #${run.runId}</div>
                          <div class="history-meta">그룹 ${run.groupType} · ${run.phase}</div>
                        </div>
                        <span class="pill">${run.phase eq 'FINISHED' ? '클리어' : '진행 중'}</span>
                      </div>
                    </c:forEach>
                  </div>

                  <c:if test="${playCount gt 5}">
                    <button type="button" class="mp-more-btn" id="mpHistoryToggleBtn" onclick="toggleMpHistoryMore()">
                      <i class="fas fa-plus-circle"></i> 더보기
                    </button>
                  </c:if>

                  <!-- 2) 최근 런 점수 그래프 -->
                  <section style="margin-top:14px;">
                    <div class="mp-section-head" style="margin-top:0;">최근 런 점수 흐름</div>
                    <div style="position:relative;height:220px;">
                      <canvas id="mpChartScores" aria-label="최근 런 점수 그래프"></canvas>
                    </div>
                  </section>

                  <a class="history-link" href="${ctx}/game/run/ranking" style="margin-top:12px;">
                    <i class="fas fa-arrow-right"></i> 랭킹 보러가기
                  </a>
                </c:otherwise>
              </c:choose>
            </section>
          </div>
        </article>

        <article class="card">
          <div class="card-head">플레이어 통계</div>
          <div class="card-body">
            <div class="stats-grid" style="grid-template-columns:repeat(2,minmax(0,1fr));">
              <div class="stat-box">
                <div class="k">총 플레이</div>
                <div class="v">${playCount}</div>
              </div>
              <div class="stat-box">
                <div class="k">평균 점수</div>
                <div class="v">${avgScore}</div>
              </div>
              <c:if test="${not empty memberRankLabel and not empty memberRankCode}">
                <div class="stat-box member-rank-tier member-rank-tier--<c:out value="${memberRankCode}"/>">
                  <div class="k">회원 등급</div>
                  <div class="v"><c:out value="${memberRankLabel}"/></div>
                </div>
                <div class="stat-box member-rank-tier member-rank-tier--<c:out value="${memberRankCode}"/>">
                  <div class="k">누적 등급 EXP</div>
                  <div class="v" style="font-size:22px;"><c:out value="${memberRankExp}"/></div>
                </div>
              </c:if>
              <div class="stat-box">
                <div class="k">주요 그룹</div>
                <div class="v">${empty topGroupType ? '-' : topGroupType}</div>
              </div>
              <div class="rank-card">
                <div class="rank-title">플레이어 ID</div>
                <div class="rank-value"><c:out value="${member.mid}"/></div>
              </div>
            </div>

            <!-- 3) 페이즈 분포(최근) -->
            <div style="margin-top:12px;">
              <div class="mp-section-head" style="margin:0 0 8px;">페이즈 분포(최근)</div>
              <c:if test="${empty phaseCounts}">
                <div class="empty-note">데이터가 없습니다.</div>
              </c:if>
              <c:if test="${not empty phaseCounts}">
                <div class="mp-phase-list">
                  <c:forEach var="e" items="${phaseCounts}">
                    <span class="pill">${e.key} ${e.value}회</span>
                  </c:forEach>
                </div>
              </c:if>
            </div>

            <!-- 4) 커뮤니티 활동(내 글) -->
            <div style="margin-top:14px;">
              <div class="mp-section-head" style="margin:0 0 8px;">내 최근 글</div>
              <c:choose>
                <c:when test="${empty myRecentPosts}">
                  <div class="empty-note" style="padding:18px 10px;">아직 작성한 글이 없습니다.</div>
                </c:when>
                <c:otherwise>
                  <div class="mp-note-list">
                    <c:forEach var="p" items="${myRecentPosts}">
                      <div class="mp-note-row">
                        <div style="min-width:0;">
                          <a href="${ctx}/boards/${p.boardType}/${p.id}" style="color:inherit;text-decoration:none;">
                            <div class="mp-note-title"><c:out value="${p.title}"/></div>
                          </a>
                          <div class="mp-note-meta">
                            <c:choose>
                              <c:when test="${p.boardType eq 'notice'}">공지</c:when>
                              <c:when test="${p.boardType eq 'free'}">자유</c:when>
                              <c:when test="${p.boardType eq 'lounge'}">자유</c:when>
                              <c:when test="${p.boardType eq 'guide'}">공략</c:when>
                              <c:when test="${p.boardType eq 'report'}">리포트</c:when>
                              <c:when test="${p.boardType eq 'map'}">캐스팅 이벤트</c:when>
                              <c:otherwise>${p.boardType}</c:otherwise>
                            </c:choose>
                            · ${p.createdAtStr}
                          </div>
                        </div>
                        <span class="pill">
                          <c:choose>
                            <c:when test="${p.boardType eq 'map'}">캐스팅 이벤트</c:when>
                            <c:when test="${p.boardType eq 'notice'}">공지</c:when>
                            <c:when test="${p.boardType eq 'free'}">자유</c:when>
                            <c:when test="${p.boardType eq 'lounge'}">자유</c:when>
                            <c:when test="${p.boardType eq 'guide'}">공략</c:when>
                            <c:when test="${p.boardType eq 'report'}">리포트</c:when>
                            <c:otherwise>글</c:otherwise>
                          </c:choose>
                        </span>
                      </div>
                    </c:forEach>
                  </div>
                </c:otherwise>
              </c:choose>
            </div>

            <!-- 5) 알림/공지 + 개인 설정(옵션) -->
            <div style="margin-top:14px;">
              <div class="mp-section-head" style="margin:0 0 8px;">최근 공지</div>
              <c:choose>
                <c:when test="${empty recentNotices}">
                  <div class="empty-note" style="padding:18px 10px;">표시할 공지가 없습니다.</div>
                </c:when>
                <c:otherwise>
                  <div class="mp-note-list">
                    <c:forEach var="n" items="${recentNotices}">
                      <div class="mp-note-row">
                        <div style="min-width:0;">
                          <a href="${ctx}/boards/notice/${n.id}" style="color:inherit;text-decoration:none;">
                            <div class="mp-note-title"><c:out value="${n.title}"/></div>
                          </a>
                          <div class="mp-note-meta">공지 · ${n.createdAtStr}</div>
                        </div>
                        <span class="pill">읽기</span>
                      </div>
                    </c:forEach>
                  </div>
                </c:otherwise>
              </c:choose>

              <div style="margin-top:12px;border:1px solid rgba(244,114,182,.14);border-radius:14px;background:#fff;padding:12px 14px;">
                <div style="font-size:11px;letter-spacing:.16em;font-family:'Orbitron',sans-serif;color:#a31863;font-weight:900;margin-bottom:8px;">
                  알림/개인 설정(추후 연동)
                </div>
                <div style="display:flex;flex-direction:column;gap:8px;">
                  <label style="display:flex;align-items:center;gap:10px;font-size:12px;color:#8e6476;font-weight:800;">
                    <input type="checkbox" checked disabled style="width:auto;margin:0;accent-color:rgba(244,114,182,1);" />
                    게임 관련 알림
                  </label>
                  <label style="display:flex;align-items:center;gap:10px;font-size:12px;color:#8e6476;font-weight:800;">
                    <input type="checkbox" checked disabled style="width:auto;margin:0;accent-color:rgba(244,114,182,1);" />
                    이메일 알림
                  </label>
                  <div style="font-size:11px;color:#9d7185;line-height:1.5;">
                    현재는 UI만 제공됩니다. 실제 연동은 추후 알림 저장 로직을 추가하면 가능합니다.
                  </div>
                </div>
              </div>
            </div>

            <div class="settings">
              <div class="settings-head">
                계정 설정
              </div>
              <div class="settings-acc">
                <div class="settings-item">
                  <button type="button" class="settings-toggle active" onclick="toggleSetting(this,'panelNick')">
                    <span>닉네임 변경</span><i class="fas fa-chevron-down"></i>
                  </button>
                  <div class="settings-content open" id="panelNick">
                    <div class="settings-panel open">
                      <form method="post" action="${ctx}/mypage/nickname">
                        <label for="nickname">닉네임 변경</label>
                        <input type="text" id="nickname" name="nickname" placeholder="새 닉네임 (3~12자)" minlength="3" maxlength="12" required />
                        <button type="submit" class="settings-submit">닉네임 변경</button>
                      </form>
                    </div>
                  </div>
                </div>

                <div class="settings-item">
                  <button type="button" class="settings-toggle" onclick="toggleSetting(this,'panelEmail')">
                    <span>이메일 변경</span><i class="fas fa-chevron-down"></i>
                  </button>
                  <div class="settings-content" id="panelEmail">
                    <div class="settings-panel open">
                      <form method="post" action="${ctx}/mypage/email">
                        <label for="newEmail">이메일 변경</label>
                        <input type="email" id="newEmail" name="newEmail" placeholder="새 이메일" required />
                        <input type="password" name="currentPw" placeholder="현재 비밀번호 확인" required />
                        <button type="submit" class="settings-submit">이메일 변경</button>
                      </form>
                    </div>
                  </div>
                </div>

                <div class="settings-item">
                  <button type="button" class="settings-toggle" onclick="toggleSetting(this,'panelPw')">
                    <span>비밀번호 변경</span><i class="fas fa-chevron-down"></i>
                  </button>
                  <div class="settings-content" id="panelPw">
                    <div class="settings-panel open">
                      <form method="post" action="${ctx}/mypage/password">
                        <label for="newPw1">비밀번호 변경</label>
                        <input type="password" id="currentPw" name="currentPw" placeholder="현재 비밀번호" required />
                        <input type="password" id="newPw1" name="newPw1" placeholder="새 비밀번호 (6자 이상)" minlength="6" required oninput="checkMatch()" />
                        <input type="password" id="newPw2" name="newPw2" placeholder="새 비밀번호 확인" minlength="6" required oninput="checkMatch()" />
                        <small id="pwMatchMsg" class="pw-match-msg"></small>
                        <button type="submit" class="settings-submit">비밀번호 변경</button>
                      </form>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="withdraw-wrap">
              <button type="button" class="withdraw-btn" onclick="openWithdrawModal()">회원탈퇴</button>
            </div>
          </div>
        </article>
      </section>
    </div>
  </main>

  <div class="withdraw-overlay" id="withdraw-overlay">
    <div class="withdraw-modal">
      <div class="withdraw-topbar"></div>
      <button class="withdraw-close" onclick="closeWithdrawModal()"><i class="fas fa-times"></i></button>
      <div class="withdraw-body">
        <div class="withdraw-title">회원 탈퇴</div>
        <div class="withdraw-sub">아래 약관을 끝까지 읽고 동의해 주세요</div>
        <div class="terms-box" id="terms-box">
          <h4>제 1조 · 탈퇴 안내</h4>
          <p>회원 탈퇴 시 계정 관련 정보가 영구 삭제되며 복구가 불가능합니다.</p>
          <h4>제 2조 · 삭제 항목</h4>
          <ul>
            <li>회원 계정 정보</li>
            <li>프로필 이미지 및 개인 설정</li>
            <li>게임 진행 기록</li>
            <li>게시글 및 댓글</li>
          </ul>
          <h4>제 3조 · 주의사항</h4>
          <p>탈퇴 처리는 즉시 완료되며 취소할 수 없습니다.</p>
        </div>
        <div class="terms-scroll-hint" id="scroll-hint"><i class="fas fa-chevron-down"></i> 아래로 스크롤하여 약관을 읽어주세요</div>
        <div class="withdraw-agree" id="agree-wrap" style="opacity:0.35;pointer-events:none;">
          <input type="checkbox" id="agreeCheck" onchange="onAgreeChange()"/>
          <label for="agreeCheck">약관을 모두 읽었고, <span>데이터 삭제</span>에 동의합니다.</label>
        </div>
        <div class="withdraw-pw-wrap" id="pw-wrap">
          <label>비밀번호 확인</label>
          <input type="password" id="withdrawPw" placeholder="비밀번호를 입력하세요"/>
        </div>
        <form method="post" action="${ctx}/mypage/delete" id="withdraw-form">
          <input type="hidden" name="password" id="withdrawPwHidden"/>
          <button type="button" class="withdraw-submit" id="withdraw-btn" onclick="submitWithdraw()">
            <i class="fas fa-triangle-exclamation"></i> 탈퇴 확인 (비활성)
          </button>
        </form>
      </div>
    </div>
  </div>

  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
  <script>
    function toggleSetting(btn, panelId){
      var panel = document.getElementById(panelId);
      if(!panel) return;
      var isOpen = panel.classList.contains('open');
      var allPanels = document.querySelectorAll('.settings-content');
      var allToggles = document.querySelectorAll('.settings-toggle');
      allPanels.forEach(function(p){ p.classList.remove('open'); });
      allToggles.forEach(function(t){ t.classList.remove('active'); });
      if(!isOpen){
        panel.classList.add('open');
        btn.classList.add('active');
      }
    }

    function checkMatch() {
      var pw1=document.getElementById('newPw1');
      var pw2=document.getElementById('newPw2');
      var msg=document.getElementById('pwMatchMsg');
      if(!pw1 || !pw2 || !msg) return;
      if(!pw2.value){ msg.style.display='none'; return; }
      msg.style.display='block';
      if(pw1.value===pw2.value){
        msg.textContent='비밀번호가 일치합니다.';
        msg.style.color='rgba(22,163,74,0.92)';
      }else{
        msg.textContent='비밀번호가 일치하지 않습니다.';
        msg.style.color='rgba(225,29,72,0.92)';
      }
    }

    var termsRead = false;
    function openWithdrawModal() {
      termsRead = false;
      document.getElementById('agreeCheck').checked = false;
      document.getElementById('agree-wrap').style.opacity = '0.35';
      document.getElementById('agree-wrap').style.pointerEvents = 'none';
      document.getElementById('pw-wrap').classList.remove('show');
      document.getElementById('withdrawPw').value = '';
      document.getElementById('scroll-hint').style.opacity = '1';
      setWithdrawBtn(false);
      document.getElementById('terms-box').scrollTop = 0;
      document.getElementById('withdraw-overlay').classList.add('show');
      document.body.style.overflow = 'hidden';
    }
    function closeWithdrawModal() {
      document.getElementById('withdraw-overlay').classList.remove('show');
      document.body.style.overflow = '';
    }
    document.getElementById('terms-box').addEventListener('scroll', function() {
      if (termsRead) return;
      if (this.scrollTop + this.clientHeight >= this.scrollHeight * 0.90) {
        termsRead = true;
        document.getElementById('agree-wrap').style.opacity = '1';
        document.getElementById('agree-wrap').style.pointerEvents = 'auto';
        document.getElementById('scroll-hint').style.opacity = '0';
      }
    });
    function onAgreeChange() {
      var checked = document.getElementById('agreeCheck').checked;
      document.getElementById('pw-wrap').classList.toggle('show', checked);
      if (!checked) setWithdrawBtn(false);
      document.getElementById('withdrawPw').oninput = function() {
        setWithdrawBtn(checked && this.value.length >= 1);
      };
    }
    function setWithdrawBtn(ready) {
      var btn = document.getElementById('withdraw-btn');
      if (ready) {
        btn.classList.add('ready');
        btn.innerHTML = '<i class="fas fa-triangle-exclamation"></i> 최종 탈퇴 확인';
      } else {
        btn.classList.remove('ready');
        btn.innerHTML = '<i class="fas fa-triangle-exclamation"></i> 탈퇴 확인 (비활성)';
      }
    }
    function submitWithdraw() {
      var btn = document.getElementById('withdraw-btn');
      if (!btn.classList.contains('ready')) return;
      var pw = document.getElementById('withdrawPw').value;
      if (!pw) { alert('비밀번호를 입력해주세요.'); return; }
      if (!confirm('정말로 탈퇴하시겠습니까?\n이 작업은 되돌릴 수 없습니다.')) return;
      document.getElementById('withdrawPwHidden').value = pw;
      document.getElementById('withdraw-form').submit();
    }

    // 2) 더보기/차트(최근 런 점수 흐름)
    var mpHistoryExpanded = false;
    function toggleMpHistoryMore() {
      mpHistoryExpanded = !mpHistoryExpanded;
      document.querySelectorAll('.mp-history-extra').forEach(function(r) {
        if (mpHistoryExpanded) r.classList.remove('mp-hidden');
        else r.classList.add('mp-hidden');
      });
      var btn = document.getElementById('mpHistoryToggleBtn');
      if (btn) {
        btn.innerHTML = mpHistoryExpanded
          ? '<i class="fas fa-minus-circle"></i> 접기'
          : '<i class="fas fa-plus-circle"></i> 더보기';
      }
    }

    const mpRunIds = [
      <c:forEach var="id" items="${recentRunIds}" varStatus="vs">${id}<c:if test="${!vs.last}">,</c:if></c:forEach>
    ];
    const mpRunScores = [
      <c:forEach var="s" items="${recentRunScores}" varStatus="vs">${s}<c:if test="${!vs.last}">,</c:if></c:forEach>
    ];
    const mpRunPhases = [
      <c:forEach var="ph" items="${recentRunPhases}" varStatus="vs">'${ph}'<c:if test="${!vs.last}">,</c:if></c:forEach>
    ];

    document.addEventListener('DOMContentLoaded', function() {
      if (typeof Chart === 'undefined') return;
      var el = document.getElementById('mpChartScores');
      if (!el || !Array.isArray(mpRunScores) || mpRunScores.length === 0) return;

      var labels = mpRunIds.map(function(id) { return '런 #' + id; });

      new Chart(el, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: '점수',
            data: mpRunScores.map(function(v) { return Number(v); }),
            borderColor: 'rgba(236,72,153,.9)',
            backgroundColor: 'rgba(253,242,248,.55)',
            fill: true,
            tension: 0.28,
            pointRadius: 3
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: {
              callbacks: {
                title: function(items) {
                  var i = items && items[0] ? items[0].dataIndex : 0;
                  return labels[i] || '';
                },
                label: function(ctx) {
                  var i = ctx.dataIndex;
                  var phase = mpRunPhases[i] || '';
                  return phase + ' · ' + ctx.parsed.y + '점';
                }
              }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { callback: function(v) { return v + '점'; } }
            }
          }
        }
      });
    });

    document.addEventListener('keydown', function(e){ if(e.key==='Escape') closeWithdrawModal(); });
    document.getElementById('withdraw-overlay').addEventListener('click', function(e){ if(e.target===this) closeWithdrawModal(); });
  </script>
</body>
</html>
