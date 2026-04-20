<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>NEXT DEBUT — MID EVALUATION</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <style>
    body{background:#f5f0ff;color:#1e293b;font-family:"Noto Sans KR",sans-serif;}
    .wrap{max-width:980px;margin:90px auto 80px;padding:0 20px;}
    .top{display:flex;justify-content:space-between;align-items:flex-end;gap:16px;flex-wrap:wrap;}
    .k{font-family:"Orbitron",sans-serif;letter-spacing:.28em;color:#64748b;font-size:11px;}
    h1{margin:8px 0 0;font-size:34px;color:#1e293b;}
    .card{margin-top:22px;padding:22px 20px;border-radius:18px;background:rgba(255,255,255,.9);border:1px solid rgba(167,139,250,.22);line-height:1.9;box-shadow:0 12px 32px rgba(99,102,241,.06);}
    .row{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;margin-top:16px;}
    .m{padding:14px 14px;border-radius:14px;background:rgba(245,240,255,.6);border:1px solid rgba(167,139,250,.18);}
    .m .t{font-family:"Orbitron",sans-serif;font-size:10px;letter-spacing:.22em;color:#64748b;margin-bottom:8px;}
    .m .v{font-size:26px;font-weight:900;color:#1e293b;}
    .btns{display:flex;justify-content:flex-end;gap:12px;margin-top:18px;flex-wrap:wrap;}
    .btn{display:inline-flex;align-items:center;gap:10px;padding:14px 22px;border-radius:999px;border:1px solid rgba(167,139,250,.3);background:rgba(255,255,255,.85);color:#334155;text-decoration:none;font-family:"Orbitron",sans-serif;letter-spacing:.18em;font-weight:900;}
    .btn.primary{background:linear-gradient(135deg,#f472b6,#c084fc,#60a5fa);border:none;color:#fff;}
    .list{margin-top:16px;max-height:320px;overflow:auto;border-radius:14px;border:1px solid rgba(167,139,250,.2);background:rgba(255,255,255,.7);}
    .it{display:flex;gap:12px;padding:12px 14px;border-bottom:1px solid rgba(167,139,250,.12);}
    .it:last-child{border-bottom:none;}
    .it .idx{font-family:"Orbitron",sans-serif;color:#64748b;width:62px;flex-shrink:0;}
    .it .txt{color:#334155;line-height:1.7;}

    /* 2026 redesign override */
    :root{--ux-primary:#FF8FAB;--ux-secondary:#C4B5FD;--ux-bg:#F5F3FF;--ux-bg-sub:#FDF2F8;--ux-text:#1F2937;--ux-muted:#6B7280;--ux-border:#E5E7EB;}
    body{background:linear-gradient(180deg,#fff 0%,var(--ux-bg) 58%,var(--ux-bg-sub) 100%);color:var(--ux-text);}
    .k,.m .t,.it .idx{color:var(--ux-muted);}
    .card,.m,.list,.it,.btn{border-color:var(--ux-border);}
    .btn{border-color:rgba(196,181,253,.55);}
    .btn.primary{background:linear-gradient(135deg,var(--ux-secondary),var(--ux-primary));}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>
<div class="wrap">
  <div class="top">
    <div>
      <div class="k">MID EVALUATION REPORT</div>
      <h1>중간 평가 리포트</h1>
    </div>
    <div class="k">RUN ${runId}</div>
  </div>

  <div class="card">
    <div style="font-size:18px;font-weight:900;margin-bottom:10px;">요약</div>
    <div>지금까지의 선택 기록을 기반으로 중간 평가를 산출했습니다. 다음 구간(2개월차)에서 난이도와 영향력이 더 커집니다.</div>

    <div class="row">
      <div class="m"><div class="t">TOTAL</div><div class="v">${total}</div></div>
      <div class="m"><div class="t">CHEM</div><div class="v">${chemistry.chemGrade}</div></div>
      <div class="m"><div class="t">LOGS</div><div class="v">${logCount}</div></div>
    </div>

    <div style="margin-top:16px;font-size:14px;color:rgba(255,255,255,.78);">
      <strong style="color:#fff;">버프/패널티(실제 적용)</strong>:
      <c:choose>
        <c:when test="${total ge 240}">다음 7턴 동안 하락 확률 감소 + +2 확률 증가</c:when>
        <c:when test="${total ge 200}">다음 7턴 동안 하락 확률 감소</c:when>
        <c:when test="${total ge 160}">변화 없음</c:when>
        <c:otherwise>다음 7턴 동안 하락(-1) 확률 증가</c:otherwise>
      </c:choose>
    </div>
  </div>

  <div class="card">
    <div style="font-size:18px;font-weight:900;margin-bottom:10px;">선택 타임라인 (최근 20턴)</div>
    <div class="list">
      <c:forEach var="l" items="${recentLogs}">
        <div class="it">
          <div class="idx">TURN ${l.turnIndex}</div>
          <div class="txt">
            ${l.phase} · ${l.choiceKey} · ${l.statTarget}
            <c:choose><c:when test="${l.delta ge 0}">▲ +${l.delta}</c:when><c:otherwise>▼ ${l.delta}</c:otherwise></c:choose>
            <span style="color:rgba(255,255,255,.45);">(${l.beforeVal}→${l.afterVal})</span>
          </div>
        </div>
      </c:forEach>
    </div>
    <div class="btns">
      <a class="btn" href="${ctx}/game/run/${runId}/replay"
         data-btn-bounce="true"
         onclick="startPageTransition(this.href); return false;">리플레이</a>
      <a class="btn primary" href="${ctx}/game/run/${runId}/eval/mid/continue"
         data-btn-bounce="true"
         onclick="startPageTransition(this.href); return false;">다음 달로</a>
    </div>
  </div>
</div>
</body>
<script>
  // 중간 평가 진입 시에도 페이지 진입 애니메이션이 한번 더 느껴지도록 약간 지연
  window.addEventListener('load', function() {
    document.documentElement.classList.add('page-enter-active');
  });
</script>
</html>

