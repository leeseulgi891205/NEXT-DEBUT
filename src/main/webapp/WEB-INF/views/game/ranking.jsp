<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>UNITX — RANKING</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&family=Noto+Sans+KR:wght@400;500;700;900&display=swap" rel="stylesheet">
  <style>
    :root{
      --primary:#e9b0d9;
      --primary-strong:#cf8fcd;
      --primary-soft:#f7e8fb;
      --system:#cdb9ff;
      --system-soft:#ece3ff;
      --system-line:rgba(205,185,255,.42);
      --bg:#fbf8ff;
      --surface:#ffffff;
      --border:#eadffd;
      --text:#2f2a2c;
      --muted:#7a6f73;
      --shadow-soft:0 8px 28px rgba(205,185,255,.22);
      --shadow-lg:0 20px 45px rgba(205,185,255,.24);
      --gold:#f3c967;
      --silver:#c4cad4;
      --bronze:#d7a88a;
    }
    *{box-sizing:border-box}
    body{
      margin:0;
      font-family:"Noto Sans KR",sans-serif;
      color:var(--text);
      background:
        radial-gradient(circle at 12% 8%, rgba(205,185,255,.26), transparent 38%),
        radial-gradient(circle at 86% 10%, rgba(232,221,255,.85), transparent 42%),
        linear-gradient(180deg, #fffefe 0%, var(--bg) 45%, #f4efff 100%);
    }
    .rank-page{max-width:1200px;margin:0 auto;padding:104px 24px 68px}
    .rank-head{
      background:linear-gradient(155deg, rgba(255,255,255,.97), rgba(236,227,255,.65));
      border:1px solid var(--system-line);
      border-radius:20px;
      box-shadow:var(--shadow-soft);
      padding:24px 26px;
      margin-bottom:18px;
    }
    .rank-kicker{
      font-family:"Orbitron",sans-serif;
      font-size:11px;
      letter-spacing:.28em;
      color:var(--muted);
      margin-bottom:8px;
    }
    .rank-title{margin:0;font-size:44px;line-height:1.08;font-weight:900}
    .rank-sub{margin:10px 0 18px;color:var(--muted);font-size:15px}
    .rank-filters{display:flex;gap:8px;flex-wrap:wrap}
    .filter-chip{
      border:1px solid var(--border);
      border-radius:999px;
      padding:8px 14px;
      background:var(--surface);
      color:var(--muted);
      font-size:12px;
      font-weight:700;
      letter-spacing:.04em;
      transition:all .18s ease;
      cursor:pointer;
      text-decoration:none;
      display:inline-flex;
      align-items:center;
      justify-content:center;
    }
    .filter-chip:hover{border-color:var(--system-line);background:rgba(236,227,255,.65);color:var(--text)}
    .filter-chip.is-active,
    .filter-chip.is-active:visited{
      border-color:var(--system-line);
      background:linear-gradient(135deg,var(--primary-strong),var(--primary));
      color:#fff;
      box-shadow:0 8px 24px rgba(233,176,217,.28);
    }

    .top3{
      display:grid;
      grid-template-columns:1fr 1.2fr 1fr;
      gap:14px;
      align-items:end;
      margin-top:16px;
    }
    .podium-card{
      border-radius:18px;
      border:1px solid var(--system-line);
      background:var(--surface);
      box-shadow:var(--shadow-soft);
      padding:16px 14px;
      text-align:center;
      position:relative;
      overflow:hidden;
      transition:transform .2s ease, box-shadow .2s ease;
    }
    .podium-card:hover{transform:translateY(-3px);box-shadow:var(--shadow-lg)}
    .podium-card::after{
      content:"";
      position:absolute;
      left:10px;right:10px;bottom:0;height:5px;border-radius:999px;
      background:linear-gradient(90deg, transparent, rgba(205,185,255,.55), transparent);
      opacity:.5;
    }
    .podium-card--1{
      padding:22px 18px;
      border-width:2px;
      border-color:var(--system-line);
      box-shadow:0 16px 34px rgba(205,185,255,.26), 0 0 0 2px rgba(232,221,255,.4) inset;
      min-height:258px;
    }
    .podium-card--1 .podium-avatar{width:84px;height:84px}
    .podium-card--2,.podium-card--3{min-height:220px}
    .podium-rank{
      position:absolute;top:10px;left:10px;
      min-width:40px;height:28px;padding:0 10px;
      display:inline-flex;align-items:center;justify-content:center;
      border-radius:999px;
      font-family:"Orbitron",sans-serif;font-size:11px;font-weight:900;
      color:#fff;
    }
    .podium-rank--1{background:linear-gradient(135deg,#f0c95e,#e0a83f)}
    .podium-rank--2{background:linear-gradient(135deg,#b8c0cc,#9ba4b1)}
    .podium-rank--3{background:linear-gradient(135deg,#d0a189,#b98367)}
    .podium-crown{
      position:absolute;
      top:8px;
      right:10px;
      font-size:17px;
      color:#f0c95e;
      text-shadow:0 3px 12px rgba(240,201,94,.45);
      animation:crownPulse 1.8s ease-in-out infinite;
    }
    .podium-avatar{
      width:72px;height:72px;margin:24px auto 10px;border-radius:50%;
      border:3px solid rgba(205,185,255,.35);
      background:linear-gradient(135deg,#fff,#f2ecff);
      display:flex;align-items:center;justify-content:center;
      color:var(--muted);font-size:26px;font-weight:900;
      box-shadow:0 8px 18px rgba(205,185,255,.25);
      overflow:hidden;
    }
    .podium-avatar img{width:100%;height:100%;object-fit:cover}
    .podium-name{font-size:16px;font-weight:900}
    .podium-run{font-size:11px;color:var(--muted);margin-top:2px}
    .podium-played{font-size:10px;color:var(--muted);margin-top:4px;opacity:.92}
    .podium-score{
      margin-top:10px;
      font-family:"Orbitron",sans-serif;
      font-size:25px;font-weight:900;
      color:var(--primary-strong);
      animation:scorePop 1.2s ease both;
    }
    .podium-score::after{content:"/1000";font-size:12px;opacity:.72;margin-left:2px}
    .podium-tag{
      margin-top:8px;
      display:inline-flex;
      padding:5px 11px;
      border-radius:999px;
      font-family:"Orbitron",sans-serif;
      font-size:10px;
      font-weight:800;
      letter-spacing:.08em;
      background:rgba(236,227,255,.72);
      border:1px solid var(--system-line);
      color:var(--muted);
    }
    .podium-empty{
      min-height:220px;
      display:flex;
      flex-direction:column;
      align-items:center;
      justify-content:center;
      gap:8px;
      color:var(--muted);
      background:linear-gradient(155deg, rgba(255,255,255,.95), rgba(236,227,255,.6));
    }
    .podium-empty b{
      font-family:"Orbitron",sans-serif;
      font-size:12px;
      letter-spacing:.08em;
    }

    .board{
      margin-top:18px;
      border-radius:20px;
      border:1px solid var(--system-line);
      background:linear-gradient(160deg, rgba(255,255,255,.97), rgba(236,227,255,.52));
      box-shadow:var(--shadow-soft);
      padding:14px;
    }
    .board-title{
      display:flex;
      align-items:center;
      justify-content:space-between;
      gap:10px;
      padding:8px 8px 2px;
      margin-bottom:8px;
    }
    .board-title strong{
      font-family:"Orbitron",sans-serif;
      font-size:13px;
      letter-spacing:.14em;
    }
    .board-title span{font-size:12px;color:var(--muted)}
    .rank-row{
      display:grid;
      grid-template-columns:90px 1.2fr minmax(140px,180px);
      gap:10px;
      align-items:center;
      background:var(--surface);
      border:1px solid var(--system-line);
      border-radius:16px;
      padding:14px 12px;
      margin-top:10px;
      transition:all .18s ease;
    }
    .rank-row:hover{
      transform:translateY(-3px) scale(1.005);
      border-color:var(--system-line);
      box-shadow:0 12px 24px rgba(205,185,255,.24);
    }
    .rank-row.my-row{
      border-color:var(--system-line);
      background:linear-gradient(135deg, rgba(233,176,217,.16), rgba(255,255,255,.94));
      box-shadow:0 0 0 1px rgba(205,185,255,.24) inset;
    }
    .rank-no{
      font-family:"Orbitron",sans-serif;
      font-size:22px;font-weight:900;
      color:#5f4f55;
    }
    .player-name{font-weight:800;font-size:15px}
    .player-run{font-size:11px;color:var(--muted)}
    .player-played{font-size:10px;color:var(--muted);margin-top:3px}
    .rank-stat-mini{
      font-size:10px;line-height:1.45;color:var(--muted);font-weight:600;
      margin-top:6px;padding-top:8px;border-top:1px dashed var(--border);
      word-break:keep-all;
    }
    .podium-card .rank-stat-mini{margin-top:8px;padding-top:10px;border-top-color:rgba(205,185,255,.55)}
    .score-cell{
      display:flex;
      flex-direction:column;
      gap:7px;
    }
    .score-num{
      font-family:"Orbitron",sans-serif;
      font-size:22px;
      font-weight:900;
      color:var(--primary-strong);
      line-height:1;
    }
    .score-num::after{content:"/1000";font-size:11px;opacity:.72;margin-left:2px}
    .score-bar{
      height:7px;border-radius:999px;
      background:rgba(236,227,255,.62);
      border:1px solid var(--system-line);
      overflow:hidden;
    }
    .score-bar > i{
      display:block;height:100%;
      border-radius:999px;
      background:linear-gradient(90deg,var(--primary-strong),var(--primary));
      box-shadow:0 0 10px rgba(233,176,217,.32);
      transition:width .7s ease;
    }
    .muted{color:var(--muted)}
    .rank-row.empty{
      background:linear-gradient(145deg, rgba(255,255,255,.92), rgba(236,227,255,.42));
      border-style:dashed;
      color:var(--muted);
    }
    .rank-row.empty .score-num{color:#b89ba5}
    .my-rank{
      margin-top:18px;
      border-radius:20px;
      border:1px solid var(--system-line);
      background:linear-gradient(145deg, rgba(233,176,217,.2), rgba(255,255,255,.95));
      box-shadow:var(--shadow-soft);
      padding:18px 20px;
      display:grid;
      grid-template-columns:120px 1fr 180px;
      align-items:center;
      gap:12px;
    }
    .my-rank-label{
      font-family:"Orbitron",sans-serif;
      font-size:14px;
      letter-spacing:.18em;
      color:var(--primary-strong);
    }
    .my-rank-no{
      font-family:"Orbitron",sans-serif;
      font-size:42px;
      font-weight:900;
      color:var(--text);
      text-shadow:0 4px 14px rgba(205,185,255,.24);
    }
    .my-rank-score{
      text-align:right;
      font-family:"Orbitron",sans-serif;
      font-size:38px;
      font-weight:900;
      color:var(--primary-strong);
    }
    .my-rank-score::after{content:"/1000";font-size:12px;opacity:.75;margin-left:2px}
    .my-rank-played{
      grid-column:1/-1;
      font-size:12px;
      color:var(--muted);
      text-align:center;
      margin-top:4px;
    }

    .actions{display:flex;justify-content:center;gap:12px;margin-top:22px;flex-wrap:wrap}
    .btn{
      display:inline-flex;align-items:center;justify-content:center;
      min-height:48px;padding:0 24px;border-radius:999px;text-decoration:none;
      font-family:"Orbitron",sans-serif;font-size:12px;font-weight:800;letter-spacing:.12em;
      transition:all .18s ease;
    }
    .btn-secondary{
      border:1px solid var(--system-line);
      background:var(--surface);
      color:var(--text);
      box-shadow:var(--shadow-soft);
    }
    .btn-secondary:hover{transform:translateY(-1px);background:rgba(236,227,255,.7)}
    .btn-primary{
      border:1px solid var(--system-line);
      background:linear-gradient(135deg,var(--primary-strong),var(--primary));
      color:#fff;
      box-shadow:0 12px 26px rgba(233,176,217,.32);
    }
    .btn-primary:hover{transform:translateY(-1px);filter:brightness(1.03)}

    @keyframes scorePop{
      0%{opacity:.2;transform:translateY(8px) scale(.94)}
      100%{opacity:1;transform:translateY(0) scale(1)}
    }
    @keyframes crownPulse{
      0%,100%{transform:scale(1);opacity:.95}
      50%{transform:scale(1.08);opacity:1}
    }

    @media (max-width:980px){
      .top3{grid-template-columns:1fr;gap:12px}
      .podium-card--1,.podium-card--2,.podium-card--3{min-height:auto}
      .rank-row{
        grid-template-columns:1fr;
        gap:6px;
        padding:14px;
      }
      .score-num{font-size:20px}
      .my-rank{
        grid-template-columns:1fr;
        text-align:left;
      }
      .my-rank-score{text-align:left}
    }

    /* ===== 2026 redesign token override (ranking) ===== */
    :root{
      --primary:#FF8FAB;
      --primary-strong:#fb6f92;
      --primary-soft:#FDF2F8;
      --secondary:#C4B5FD;
      --bg:#F5F3FF;
      --surface:#FFFFFF;
      --border:#E5E7EB;
      --text:#1F2937;
      --muted:#6B7280;
      --lav-line:rgba(196,181,253,.55);
    }
    .rank-head,.board,.rank-row,.podium-card,.my-rank{border-color:var(--border);}
    .rank-row:hover,.podium-card:hover{border-color:var(--lav-line); box-shadow:0 12px 24px rgba(196,181,253,.22);}
    .filter-chip:hover,.btn-secondary:hover{background:rgba(196,181,253,.2);}
    .score-bar > i{background:linear-gradient(90deg,var(--secondary),var(--primary));}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<div class="rank-page">
  <section class="rank-head">
    <div class="rank-kicker">UNITX LEADERBOARD</div>
    <h1 class="rank-title">RANKING</h1>
    <p class="rank-sub">상위 플레이어 기록과 내 순위를 한눈에 확인하세요.</p>
    <div class="rank-filters">
      <c:set var="rankFromQ" value="${rankingFromMain ? '?from=main' : ''}" />
      <c:set var="rankFromAmp" value="${rankingFromMain ? '&amp;from=main' : ''}" />
      <a href="${ctx}/game/run/${runId}/ranking${rankFromQ}" class="filter-chip ${rankingPeriod == 'all' ? 'is-active' : ''}">전체</a>
      <a href="${ctx}/game/run/${runId}/ranking?period=week${rankFromAmp}" class="filter-chip ${rankingPeriod == 'week' ? 'is-active' : ''}">주간</a>
      <a href="${ctx}/game/run/${runId}/ranking?period=month${rankFromAmp}" class="filter-chip ${rankingPeriod == 'month' ? 'is-active' : ''}">월간</a>
    </div>
  </section>

  <section class="my-rank">
    <div class="my-rank-label">YOUR RANK</div>
    <div class="my-rank-no">#${myRank}</div>
    <div class="my-rank-score js-score" data-score="${myScore}">${myScore}</div>
    <c:if test="${not empty myPlayedAtLabel}">
      <div class="my-rank-played">이 기록 플레이 ${myPlayedAtLabel}</div>
    </c:if>
  </section>

  <section class="top3">
    <c:forEach var="podiumRank" begin="1" end="3">
      <c:set var="slotFound" value="false" />
      <c:forEach var="row" items="${rankingRows}">
        <c:if test="${row.rank == podiumRank}">
          <c:set var="slotFound" value="true" />
          <c:set var="podiumClass" value="${row.rank == 1 ? 'podium-card podium-card--1' : (row.rank == 2 ? 'podium-card podium-card--2' : 'podium-card podium-card--3')}" />
          <div class="${podiumClass}" style="order:${row.rank == 1 ? 2 : (row.rank == 2 ? 1 : 3)}">
            <div class="podium-rank podium-rank--${row.rank}">${row.rank}위</div>
            <c:if test="${row.rank == 1}">
              <div class="podium-crown">👑</div>
            </c:if>
            <div class="podium-avatar">
              <span>${fn:substring(row.playerLabel, 0, 1)}</span>
            </div>
            <div class="podium-name">${row.playerLabel}</div>
            <div class="podium-run">RUN ${row.runId}</div>
            <c:if test="${not empty row.playedAtLabel}">
              <div class="podium-played">플레이 ${row.playedAtLabel}</div>
            </c:if>
            <div class="podium-score js-score" data-score="${row.score}">${row.score}</div>
            <c:if test="${not empty row.statSums}">
              <div class="rank-stat-mini">보컬 ${row.statSums.vocalSum} · 댄스 ${row.statSums.danceSum} · 스타 ${row.statSums.starSum} · 멘탈 ${row.statSums.mentalSum} · 팀워크 ${row.statSums.teamworkSum} · 로스터 합 ${row.statSums.abilityTotal}/${row.rosterMemberCount * 100}</div>
            </c:if>
            <div class="podium-tag">${row.rank == 1 ? 'CHAMPION' : (row.rank == 2 ? 'CONTENDER' : 'RISING')}</div>
          </div>
        </c:if>
      </c:forEach>
      <c:if test="${not slotFound}">
        <div class="podium-card podium-empty" style="order:${podiumRank == 1 ? 2 : (podiumRank == 2 ? 1 : 3)}">
          <div class="podium-rank podium-rank--${podiumRank}">${podiumRank}위</div>
          <div class="podium-avatar"><span>-</span></div>
          <b>도전자 대기중</b>
          <div class="podium-run">기록이 아직 없습니다</div>
        </div>
      </c:if>
    </c:forEach>
  </section>

  <section class="board">
    <div class="board-title">
      <strong>LEADERBOARD</strong>
      <span>4위부터 순위 경쟁 구간</span>
    </div>
    <c:forEach var="rankNo" begin="4" end="10">
      <c:set var="listFound" value="false" />
      <c:forEach var="row" items="${rankingRows}">
        <c:if test="${row.rank == rankNo}">
          <c:set var="listFound" value="true" />
          <c:set var="scorePct" value="${topScore > 0 ? (row.score * 100) / topScore : 0}" />
          <div class="rank-row ${row.me ? 'my-row' : ''}">
            <div class="rank-no">#${row.rank}</div>
            <div>
              <div class="player-name">${row.playerLabel}</div>
              <div class="player-run">RUN ${row.runId}</div>
              <c:if test="${not empty row.playedAtLabel}">
                <div class="player-played">플레이 ${row.playedAtLabel}</div>
              </c:if>
            </div>
            <div class="score-cell">
              <div class="score-num js-score" data-score="${row.score}">${row.score}</div>
              <div class="score-bar"><i style="width:${scorePct}%"></i></div>
            </div>
            <c:if test="${not empty row.statSums}">
              <div class="rank-stat-mini" style="grid-column:1/-1">보컬 ${row.statSums.vocalSum} · 댄스 ${row.statSums.danceSum} · 스타 ${row.statSums.starSum} · 멘탈 ${row.statSums.mentalSum} · 팀워크 ${row.statSums.teamworkSum} · 로스터 합 ${row.statSums.abilityTotal}/${row.rosterMemberCount * 100} · 표시 점수는 케미·진행 보정 포함</div>
            </c:if>
          </div>
        </c:if>
      </c:forEach>
      <c:if test="${not listFound}">
        <div class="rank-row empty">
          <div class="rank-no">#${rankNo}</div>
          <div>
            <div class="player-name">도전자 대기중</div>
            <div class="player-run">기록이 등록되면 표시됩니다</div>
          </div>
          <div class="score-cell">
            <div class="score-num">0</div>
            <div class="score-bar"><i style="width:0%"></i></div>
          </div>
        </div>
      </c:if>
    </c:forEach>
  </section>

  <div class="actions">
    <c:choose>
      <c:when test="${rankingFromMain}">
        <a class="btn btn-secondary" href="${ctx}/main">메인 페이지</a>
      </c:when>
      <c:otherwise>
        <a class="btn btn-secondary" href="${ctx}/game/run/${runId}/ending">결과로 돌아가기</a>
      </c:otherwise>
    </c:choose>
    <a class="btn btn-primary" href="${ctx}/game">NEW GAME</a>
  </div>
</div>

<script>
  (function(){
    var targets=document.querySelectorAll('.js-score');
    targets.forEach(function(el){
      var end=Number(el.getAttribute('data-score')||0);
      var dur=900;
      var startTime=null;
      function tick(ts){
        if(!startTime) startTime=ts;
        var p=Math.min(1,(ts-startTime)/dur);
        var now=Math.floor(end*p);
        el.textContent=now.toLocaleString('ko-KR');
        if(p<1) requestAnimationFrame(tick);
      }
      requestAnimationFrame(tick);
    });
  })();
</script>
</body>
</html>