<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 · 코인 사용 통계 · NEXT DEBUT</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js" defer></script>
  <style>
    :root{--ad-bg:#fff8fc;--ad-card:#fff;--ad-border:rgba(244,114,182,.20);--ad-text:#332b30;--ad-shadow:0 14px 34px rgba(236,72,153,.10);--ad-r:18px;}
    *{box-sizing:border-box}
    body{margin:0;color:var(--ad-text);background:radial-gradient(1200px 500px at 0% -20%, rgba(251,207,232,.5), transparent 56%),radial-gradient(900px 480px at 100% -10%, rgba(253,242,248,.9), transparent 58%),var(--ad-bg);}
    .ad-wrap{max-width:1200px;margin:0 auto;padding:calc(var(--nav-h,68px) + 22px) 20px 42px;display:grid;gap:14px;}
    .card{background:var(--ad-card);border:1px solid var(--ad-border);border-radius:var(--ad-r);box-shadow:var(--ad-shadow);}
    .hero{padding:18px;display:flex;justify-content:space-between;align-items:flex-start;gap:14px;flex-wrap:wrap;}
    .hero h1{font-family:"Orbitron",sans-serif;font-size:clamp(1.1rem,2.4vw,1.45rem);margin:0 0 8px;letter-spacing:.06em;}
    .hero p{margin:0;font-size:13px;color:#6B7280;max-width:520px;line-height:1.55;}
    .btn{display:inline-flex;align-items:center;gap:6px;padding:9px 14px;border-radius:12px;border:1px solid rgba(244,114,182,.35);background:#fff;color:#9d174d;font-size:13px;font-weight:700;text-decoration:none;cursor:pointer;}
    .btn:hover{background:rgba(253,242,248,.9);}
    .section-head{padding:12px 16px;font-size:12px;font-weight:800;letter-spacing:.1em;font-family:"Orbitron",sans-serif;border-bottom:1px solid rgba(196,181,253,.55);color:#7f62a3;}
    .section-body{padding:14px 16px 18px;}
    .period-tabs{display:flex;gap:8px;flex-wrap:wrap;}
    .period-tabs .btn.on{background:linear-gradient(135deg,#fbcfe8,#f472b6);color:#831843;}
    .kpi-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px;}
    @media(max-width:900px){.kpi-grid{grid-template-columns:1fr 1fr;}} @media(max-width:640px){.kpi-grid{grid-template-columns:1fr;}}
    .metric{padding:10px;border-radius:12px;border:1px solid rgba(244,114,182,.12);background:rgba(253,242,248,.45);}
    .metric .k{font-size:10px;color:#8f6f7f;} .metric .v{font-size:22px;font-weight:900;} .metric .s{margin-top:6px;font-size:11px;color:#9ca3af;line-height:1.35;}
    .chart-grid{display:grid;grid-template-columns:2fr 1fr;gap:12px;} @media(max-width:1050px){.chart-grid{grid-template-columns:1fr;}}
    .panel{border:1px solid rgba(244,114,182,.14);border-radius:14px;background:#fff;padding:12px;}
    .panel-title{font-size:11px;letter-spacing:.12em;color:#8d5f74;font-family:"Orbitron",sans-serif;margin-bottom:9px;}
    .canvas-wrap{position:relative;min-height:280px;}
    .table-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;} @media(max-width:900px){.table-grid{grid-template-columns:1fr;}}
    .tb-wrap{overflow:auto;border:1px solid rgba(244,114,182,.12);border-radius:14px;}
    .tb{width:100%;border-collapse:separate;border-spacing:0;min-width:440px;}
    .tb th{text-align:left;font-size:10px;letter-spacing:.08em;color:#8d5f74;padding:10px;border-bottom:1px solid rgba(244,114,182,.18);}
    .tb td{padding:11px 10px;border-bottom:1px solid rgba(244,114,182,.10);font-size:13px;}
    .tb tbody tr:hover{background:rgba(253,242,248,.35);}
    .empty-msg{font-size:13px;color:#8e6f7f;padding:16px;text-align:center;}
    .members-pager{display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:10px;margin-top:12px;padding-top:12px;border-top:1px solid rgba(244,114,182,.12);}
    .members-pager .info{font-size:12px;color:#8b6a79;}
    .members-pager .nav{display:flex;flex-wrap:wrap;gap:8px;align-items:center;}
    .members-pager .nav .btn.page-on{background:linear-gradient(135deg,#fbcfe8,#f472b6);border-color:rgba(244,114,182,.5);color:#831843;}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>
<main class="ad-wrap">
  <section class="card hero">
    <div>
      <h1>Coin Usage Analytics</h1>
      <p>상점/뽑기를 분리하지 않고 코인 소비 행동 중심으로 집계합니다.</p>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;">
      <a class="btn" href="${ctx}/admin"><i class="fas fa-arrow-left"></i> 이전화면</a>
    </div>
  </section>

  <section class="card">
    <div class="section-head">기간 필터</div>
    <div class="section-body">
      <div class="period-tabs">
        <a class="btn ${usagePeriod eq 'daily' ? 'on' : ''}" href="${ctx}/admin/analytics/usage?period=daily&amp;logPage=1">일간</a>
        <a class="btn ${usagePeriod eq 'weekly' ? 'on' : ''}" href="${ctx}/admin/analytics/usage?period=weekly&amp;logPage=1">주간</a>
        <a class="btn ${usagePeriod eq 'monthly' ? 'on' : ''}" href="${ctx}/admin/analytics/usage?period=monthly&amp;logPage=1">월간</a>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">상단 KPI</div>
    <div class="section-body">
      <div class="kpi-grid">
        <div class="metric"><div class="k">오늘 총 사용 코인</div><div class="v"><fmt:formatNumber value="${kpi.totalUsage}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">오늘 총 사용 건수</div><div class="v"><fmt:formatNumber value="${kpi.usageCount}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">사용 유저 수</div><div class="v"><fmt:formatNumber value="${kpi.userCount}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">평균 1회 사용 코인</div><div class="v"><fmt:formatNumber value="${kpi.avgUsage}" pattern="#,##0.0"/></div></div>
        <div class="metric"><div class="k">상점 소비 비중</div><div class="v"><fmt:formatNumber value="${kpi.shopRatio}" pattern="#,##0.0"/>%</div></div>
        <div class="metric"><div class="k">뽑기 소비 비중</div><div class="v"><fmt:formatNumber value="${kpi.gachaRatio}" pattern="#,##0.0"/>%</div></div>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">사용 흐름 / 사용처 비율</div>
    <div class="section-body">
      <div class="chart-grid">
        <div class="panel"><div class="panel-title">코인 사용 흐름</div><div class="canvas-wrap"><canvas id="usageFlowChart"></canvas></div></div>
        <div class="panel"><div class="panel-title">사용처 비율</div><div class="canvas-wrap"><canvas id="usageRatioChart"></canvas></div></div>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">소비 분석 TOP</div>
    <div class="section-body">
      <div class="table-grid">
        <div class="panel">
          <div class="panel-title">상점 소비 TOP</div>
          <div class="tb-wrap"><table class="tb"><thead><tr><th>상품명</th><th>구매 횟수</th><th>총 사용 코인</th><th>구매 유저 수</th></tr></thead><tbody>
          <c:forEach var="r" items="${shopTopList}"><tr><td><c:out value="${r.name}"/></td><td><fmt:formatNumber value="${r.usageCount}" pattern="#,##0"/></td><td><fmt:formatNumber value="${r.totalUsage}" pattern="#,##0"/></td><td><fmt:formatNumber value="${r.userCount}" pattern="#,##0"/></td></tr></c:forEach>
          </tbody></table></div>
        </div>
        <div class="panel">
          <div class="panel-title">뽑기 소비 TOP</div>
          <p style="margin:0 0 8px;font-size:11px;color:#9ca3af;">연습생 뽑기 총 코인은 로그 건수×1회 가격 기준 추정입니다.</p>
          <div class="tb-wrap"><table class="tb"><thead><tr><th>뽑기명</th><th>실행 횟수</th><th>총 사용 코인</th><th>이용 유저 수</th></tr></thead><tbody>
          <c:forEach var="r" items="${gachaTopList}"><tr><td><c:out value="${r.name}"/></td><td><fmt:formatNumber value="${r.usageCount}" pattern="#,##0"/></td><td><fmt:formatNumber value="${r.totalUsage}" pattern="#,##0"/></td><td><fmt:formatNumber value="${r.userCount}" pattern="#,##0"/></td></tr></c:forEach>
          </tbody></table></div>
        </div>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">최근 사용 로그</div>
    <div class="section-body">
      <div class="tb-wrap">
        <table class="tb" style="min-width:900px;">
          <thead><tr><th>시각</th><th>회원</th><th>구분</th><th>항목명</th><th>코인 변화량</th><th>비고</th></tr></thead>
          <tbody>
            <c:choose>
              <c:when test="${empty usageLogs}">
                <tr><td colspan="6"><div class="empty-msg">해당 기간 로그가 없습니다.</div></td></tr>
              </c:when>
              <c:otherwise>
                <c:forEach var="log" items="${usageLogs}">
                  <tr><td><c:out value="${log.time}"/></td><td><c:out value="${log.user}"/></td><td><c:out value="${log.type}"/></td><td><c:out value="${log.itemName}"/></td><td><fmt:formatNumber value="${log.amount}" pattern="#,##0"/></td><td><c:out value="${empty log.note ? '-' : log.note}"/></td></tr>
                </c:forEach>
              </c:otherwise>
            </c:choose>
          </tbody>
        </table>
      </div>
      <c:if test="${usageLogTotal > 0}">
        <div class="members-pager">
          <div class="info">
            <c:choose>
              <c:when test="${usageLogTotalPages > 1}">
                ${usageLogRowFrom}–${usageLogRowTo}번째 · 전체 ${usageLogTotal}건 · 페이지 ${usageLogPage} / ${usageLogTotalPages}
              </c:when>
              <c:otherwise>
                전체 ${usageLogTotal}건
              </c:otherwise>
            </c:choose>
          </div>
          <c:if test="${usageLogTotalPages > 1}">
            <div class="nav">
              <c:if test="${usageLogPage > 1}">
                <c:url var="ulFirst" value="/admin/analytics/usage"><c:param name="period" value="${usagePeriod}"/><c:param name="logPage" value="1"/></c:url>
                <a class="btn" href="${ulFirst}">« 처음</a>
                <c:url var="ulPrev" value="/admin/analytics/usage"><c:param name="period" value="${usagePeriod}"/><c:param name="logPage" value="${usageLogPage - 1}"/></c:url>
                <a class="btn" href="${ulPrev}"><i class="fas fa-chevron-left"></i> 이전</a>
              </c:if>
              <c:forEach var="pn" items="${usageLogPageNumbers}">
                <c:url var="ulPg" value="/admin/analytics/usage"><c:param name="period" value="${usagePeriod}"/><c:param name="logPage" value="${pn}"/></c:url>
                <c:choose>
                  <c:when test="${pn == usageLogPage}"><span class="btn page-on" aria-current="page">${pn}</span></c:when>
                  <c:otherwise><a class="btn" href="${ulPg}">${pn}</a></c:otherwise>
                </c:choose>
              </c:forEach>
              <c:if test="${usageLogPage < usageLogTotalPages}">
                <c:url var="ulNext" value="/admin/analytics/usage"><c:param name="period" value="${usagePeriod}"/><c:param name="logPage" value="${usageLogPage + 1}"/></c:url>
                <a class="btn" href="${ulNext}">다음 <i class="fas fa-chevron-right"></i></a>
                <c:url var="ulLast" value="/admin/analytics/usage"><c:param name="period" value="${usagePeriod}"/><c:param name="logPage" value="${usageLogTotalPages}"/></c:url>
                <a class="btn" href="${ulLast}">마지막 »</a>
              </c:if>
            </div>
          </c:if>
        </div>
      </c:if>
    </div>
  </section>
</main>
<script>
const FLOW = {
  daily: { labels: ${flowDailyLabelsJson}, total: ${flowDailyTotalJson}, shop: ${flowDailyShopJson}, gacha: ${flowDailyGachaJson} },
  weekly: { labels: ${flowWeeklyLabelsJson}, total: ${flowWeeklyTotalJson}, shop: ${flowWeeklyShopJson}, gacha: ${flowWeeklyGachaJson} },
  monthly: { labels: ${flowMonthlyLabelsJson}, total: ${flowMonthlyTotalJson}, shop: ${flowMonthlyShopJson}, gacha: ${flowMonthlyGachaJson} }
};
const selectedPeriod = '${usagePeriod}';
const ratioData = [${ratioShop}, ${ratioGacha}, ${ratioEtc}];
document.addEventListener('DOMContentLoaded', function() {
  if (typeof Chart === 'undefined') return;
  const ds = FLOW[selectedPeriod] || FLOW.daily;
  new Chart(document.getElementById('usageFlowChart'), {
    type: 'line',
    data: { labels: ds.labels, datasets: [
      { label: '전체 사용 코인', data: ds.total, borderColor: '#ec4899', backgroundColor: 'rgba(236,72,153,.1)', tension: .25 },
      { label: '상점 사용 코인', data: ds.shop, borderColor: '#8b5cf6', backgroundColor: 'rgba(139,92,246,.1)', tension: .25 },
      { label: '뽑기 사용 코인', data: ds.gacha, borderColor: '#14b8a6', backgroundColor: 'rgba(20,184,166,.1)', tension: .25 }
    ]},
    options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
  });
  new Chart(document.getElementById('usageRatioChart'), {
    type: 'doughnut',
    data: { labels: ['SHOP', 'GACHA', 'ETC'], datasets: [{ data: ratioData, backgroundColor: ['#f472b6','#8b5cf6','#c4b5fd'], borderWidth: 0 }] },
    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
  });
});
</script>
</body>
</html>
