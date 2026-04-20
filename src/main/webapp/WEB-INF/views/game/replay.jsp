<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>NEXT DEBUT — REPLAY</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <style>
    :root{
      --rp-primary:#f472b6;
      --rp-secondary:#c084fc;
      --rp-accent:#818cf8;
      --rp-bg0:#ffffff;
      --rp-bg1:#f8f7ff;
      --rp-bg2:#fdf2f8;
      --rp-surface:rgba(255,255,255,.94);
      --rp-muted:#64748b;
      --rp-faint:#94a3b8;
      --rp-text:#0f172a;
      --rp-text2:#334155;
      --rp-border:#e2e8f0;
      --rp-border-soft:rgba(167,139,250,.2);
      --rp-shadow:0 14px 40px rgba(99,102,241,.08);
      --rp-radius:18px;
      --rp-mono:"Orbitron",ui-monospace,sans-serif;
    }
    *{box-sizing:border-box;}
    body{
      margin:0;
      min-height:100vh;
      background:linear-gradient(165deg,var(--rp-bg0) 0%,var(--rp-bg1) 45%,var(--rp-bg2) 100%);
      color:var(--rp-text);
      font-family:"Pretendard Variable",Pretendard,"Noto Sans KR",sans-serif;
      font-size:15px;
      line-height:1.55;
    }
    .wrap{max-width:1120px;margin:88px auto 72px;padding:0 22px;}
    .embed-body{overflow:hidden;height:100vh;}
    .embed-body .wrap{
      margin:0 auto;
      padding:18px 20px 16px;
      height:100vh;
      max-width:none;
      display:flex;
      flex-direction:column;
    }
    .embed-body .top{margin-bottom:10px;flex-shrink:0;}
    .embed-body .shell{margin-top:0;flex:1;min-height:0;}
    .embed-body .card--summary{display:flex;flex-direction:column;min-height:0;}
    .embed-body .card--summary .btns{margin-top:auto;padding-top:16px;}
    .embed-body .card--timeline{display:flex;flex-direction:column;min-height:0;}
    .embed-body .card--timeline .section-title{flex-shrink:0;}
    .embed-body .list{flex:1;min-height:0;max-height:none;}

    .top{
      display:flex;
      justify-content:space-between;
      align-items:flex-end;
      gap:20px;
      flex-wrap:wrap;
      padding-bottom:14px;
      border-bottom:1px solid var(--rp-border);
    }
    .top__kicker{
      font-family:var(--rp-mono);
      letter-spacing:.24em;
      font-size:10px;
      font-weight:700;
      color:var(--rp-muted);
      text-transform:uppercase;
    }
    .top h1{
      margin:6px 0 0;
      font-size:clamp(22px,3.2vw,30px);
      font-weight:800;
      letter-spacing:-.02em;
      color:var(--rp-text);
    }
    .top__run{
      font-family:var(--rp-mono);
      font-size:11px;
      letter-spacing:.14em;
      color:var(--rp-faint);
      padding:8px 12px;
      border-radius:999px;
      background:rgba(248,250,252,.9);
      border:1px solid var(--rp-border);
    }

    .shell{
      margin-top:18px;
      display:grid;
      grid-template-columns:minmax(268px,300px) minmax(0,1fr);
      gap:16px;
      align-items:stretch;
    }
    @media(max-width:900px){
      .shell{grid-template-columns:1fr;}
    }

    .card{
      padding:20px 18px;
      border-radius:var(--rp-radius);
      background:var(--rp-surface);
      border:1px solid var(--rp-border-soft);
      box-shadow:var(--rp-shadow);
    }
    .section-title{
      display:flex;
      flex-direction:column;
      gap:2px;
      margin-bottom:14px;
    }
    .section-title__en{
      font-family:var(--rp-mono);
      font-size:9px;
      letter-spacing:.24em;
      color:var(--rp-faint);
      text-transform:uppercase;
    }
    .section-title__ko{
      font-size:17px;
      font-weight:800;
      color:var(--rp-text);
      letter-spacing:-.02em;
    }

    .metric{
      display:grid;
      grid-template-columns:repeat(3,minmax(0,1fr));
      gap:10px;
    }
    .m{
      padding:12px 10px;
      border-radius:14px;
      background:linear-gradient(180deg,#fafafa 0%,#f4f4f5 100%);
      border:1px solid var(--rp-border);
      text-align:center;
    }
    .m .t{
      font-family:var(--rp-mono);
      font-size:9px;
      letter-spacing:.18em;
      color:var(--rp-muted);
      margin-bottom:6px;
    }
    .m .v{
      font-size:21px;
      font-weight:900;
      color:var(--rp-text);
      font-variant-numeric:tabular-nums;
    }

    .replay-hint{
      margin-top:14px;
      padding:12px 14px;
      border-radius:12px;
      background:rgba(99,102,241,.06);
      border:1px solid rgba(99,102,241,.12);
      font-size:13px;
      color:var(--rp-text2);
      line-height:1.65;
    }

    .btns{display:flex;justify-content:flex-end;gap:10px;margin-top:14px;flex-wrap:wrap;}
    .btn{
      display:inline-flex;
      align-items:center;
      justify-content:center;
      gap:8px;
      padding:11px 20px;
      border-radius:999px;
      border:1px solid rgba(196,181,253,.55);
      background:#fff;
      color:var(--rp-text2);
      text-decoration:none;
      font-family:var(--rp-mono);
      font-size:11px;
      letter-spacing:.12em;
      font-weight:800;
      cursor:pointer;
      transition:transform .12s ease,box-shadow .12s ease,border-color .12s ease;
    }
    .btn:hover{
      border-color:var(--rp-secondary);
      box-shadow:0 6px 18px rgba(192,132,252,.18);
    }
    .btn.primary{
      border:none;
      color:#fff;
      background:linear-gradient(120deg,var(--rp-secondary),var(--rp-primary));
      box-shadow:0 8px 22px rgba(244,114,182,.28);
    }
    .btn.primary:hover{box-shadow:0 10px 26px rgba(244,114,182,.35);}

    .list{
      max-height:520px;
      overflow:auto;
      border-radius:14px;
      border:1px solid var(--rp-border);
      background:#fff;
      scrollbar-gutter:stable;
    }
    .list--empty{
      display:flex;
      align-items:center;
      justify-content:center;
      min-height:160px;
      color:var(--rp-muted);
      font-size:14px;
    }

    .replay-row{
      display:grid;
      grid-template-columns:52px minmax(0,1fr) 40px;
      gap:12px;
      align-items:start;
      padding:14px 14px 14px 12px;
      border-bottom:1px solid var(--rp-border);
    }
    .replay-row:last-child{border-bottom:none;}
    .replay-row__turn{
      font-family:var(--rp-mono);
      font-size:11px;
      font-weight:800;
      letter-spacing:.06em;
      color:var(--rp-muted);
      width:44px;
      height:44px;
      display:flex;
      align-items:center;
      justify-content:center;
      border-radius:12px;
      background:#f8fafc;
      border:1px solid var(--rp-border);
    }
    .replay-row__body{min-width:0;}
    .replay-row__phase{
      font-size:12px;
      font-weight:600;
      color:var(--rp-text2);
      margin-bottom:6px;
      word-break:break-word;
    }
    .replay-row__line{
      display:flex;
      flex-wrap:wrap;
      align-items:center;
      gap:6px 8px;
      font-size:14px;
      color:var(--rp-text);
      font-weight:500;
    }
    .replay-row__sep{color:var(--rp-faint);font-weight:400;}
    .replay-row__trace{
      margin-top:6px;
      font-size:12px;
      color:var(--rp-faint);
      font-variant-numeric:tabular-nums;
    }
    .delta{
      display:inline-flex;
      align-items:center;
      gap:3px;
      padding:3px 10px;
      border-radius:999px;
      font-size:12px;
      font-weight:800;
      font-variant-numeric:tabular-nums;
    }
    .delta--up{
      background:rgba(16,185,129,.12);
      color:#047857;
    }
    .delta--down{
      background:rgba(244,63,94,.1);
      color:#be123c;
    }
    .replay-row__icon{
      width:36px;
      height:36px;
      border-radius:10px;
      display:flex;
      align-items:center;
      justify-content:center;
      color:#64748b;
      background:linear-gradient(145deg,#e8eef5,#dce4ee);
      border:1px solid #cbd5e1;
      font-size:14px;
      flex-shrink:0;
    }
    .replay-row.playing{
      background:linear-gradient(90deg,rgba(244,114,182,.1),rgba(192,132,252,.06));
    }
    .replay-row.playing .replay-row__turn{
      color:#db2777;
      border-color:rgba(244,114,182,.45);
      background:rgba(253,242,248,.9);
    }
  </style>
</head>
<body class="${empty param.embed ? '' : 'embed-body'}">
<c:if test="${empty param.embed}">
  <jsp:include page="/WEB-INF/views/fragments/topnav.jspf" />
</c:if>
<div class="wrap">
  <header class="top">
    <div>
      <div class="top__kicker">Replay · timeline</div>
      <h1>선택 리플레이</h1>
    </div>
    <div class="top__run" title="런 ID">RUN ${runId}</div>
  </header>

  <div class="shell">
    <section class="card card--summary" aria-labelledby="replay-summary-title">
      <div class="section-title" id="replay-summary-title">
        <span class="section-title__en">Summary</span>
        <span class="section-title__ko">요약</span>
      </div>
      <div class="metric">
        <div class="m"><div class="t">LOGS</div><div class="v">${logCount}</div></div>
        <div class="m"><div class="t">TOTAL</div><div class="v">${total}</div></div>
        <div class="m"><div class="t">CHEM</div><div class="v">${chemistry.chemGrade}</div></div>
      </div>
      <p class="replay-hint">
        턴마다 어떤 선택을 했는지, 어떤 스탯이 얼마나 변했는지 한눈에 확인할 수 있어요.
      </p>
      <div class="btns">
        <a class="btn" href="${ctx}/game/run/${runId}/start"
           data-btn-bounce="true"
           onclick="startPageTransition(this.href); return false;">돌아가기</a>
        <button class="btn primary" type="button" id="autoBtn"
                data-btn-bounce="true"
                onclick="toggleAuto()">AUTO PLAY</button>
      </div>
    </section>

    <section class="card card--timeline" aria-labelledby="replay-timeline-title">
      <div class="section-title" id="replay-timeline-title">
        <span class="section-title__en">Full timeline</span>
        <span class="section-title__ko">전체 타임라인</span>
      </div>
      <div class="list" id="list">
        <c:choose>
          <c:when test="${empty logs}">
            <div class="list--empty">기록된 로그가 없습니다.</div>
          </c:when>
          <c:otherwise>
            <c:forEach var="l" items="${logs}">
              <div class="replay-row it">
                <div class="idx replay-row__turn">T${l.turnIndex}</div>
                <div class="replay-row__body">
                  <div class="replay-row__phase">${l.phase} · ${l.bucket}</div>
                  <div class="replay-row__line">
                    <span>${l.choiceKey}</span>
                    <span class="replay-row__sep">→</span>
                    <span>${l.statTarget}</span>
                    <c:choose>
                      <c:when test="${l.delta ge 0}">
                        <span class="delta delta--up">▲ +${l.delta}</span>
                      </c:when>
                      <c:otherwise>
                        <span class="delta delta--down">▼ ${l.delta}</span>
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div class="replay-row__trace">
                    ${l.beforeVal} → ${l.afterVal}
                    <c:if test="${not empty l.eventType}">
                      <span class="replay-row__sep"> · </span>${l.eventType}
                    </c:if>
                  </div>
                </div>
                <div class="replay-row__icon" title="턴 로그"><i class="fa-solid fa-file-lines" aria-hidden="true"></i></div>
              </div>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </section>
  </div>
</div>

<script>
var _auto = { running:false, timer:null, idx:0 };
function toggleAuto(){
  _auto.running = !_auto.running;
  var btn = document.getElementById('autoBtn');
  if(btn) btn.textContent = _auto.running ? 'STOP' : 'AUTO PLAY';
  if(_auto.running) startAuto();
  else stopAuto();
}
function startAuto(){
  stopAuto();
  var items = document.querySelectorAll('#list .it');
  if(!items || items.length===0){ _auto.running=false; return; }
  _auto.idx = 0;
  _auto.timer = setInterval(function(){
    items.forEach(function(el){ el.classList.remove('playing'); });
    var cur = items[_auto.idx];
    if(cur){
      cur.classList.add('playing');
      cur.scrollIntoView({block:'center', behavior:'smooth'});
    }
    _auto.idx++;
    if(_auto.idx >= items.length){
      stopAuto();
      var btn = document.getElementById('autoBtn');
      if(btn) btn.textContent = 'AUTO PLAY';
    }
  }, 520);
}
function stopAuto(){
  if(_auto.timer){ clearInterval(_auto.timer); _auto.timer=null; }
  var items = document.querySelectorAll('#list .it');
  items && items.forEach(function(el){ el.classList.remove('playing'); });
}
</script>
</body>
</html>
