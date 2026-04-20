<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 · 뽑기 통계 · NEXT DEBUT</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <style>
    :root{
      --ad-bg:#fff8fc;
      --ad-card:#ffffff;
      --ad-border:rgba(244,114,182,.20);
      --ad-text:#332b30;
      --ad-shadow:0 14px 34px rgba(236,72,153,.10);
      --ad-r:18px;
    }
    *{box-sizing:border-box}
    body{margin:0;color:var(--ad-text);
      background:
        radial-gradient(1200px 500px at 0% -20%, rgba(251,207,232,.5), transparent 56%),
        radial-gradient(900px 480px at 100% -10%, rgba(253,242,248,.9), transparent 58%),
        var(--ad-bg);
    }
    .ad-wrap{max-width:1100px;margin:0 auto;padding:calc(var(--nav-h,68px) + 22px) 20px 42px;display:grid;gap:14px;}
    .card{
      background:var(--ad-card);
      border:1px solid var(--ad-border);
      border-radius:var(--ad-r);
      box-shadow:var(--ad-shadow);
    }
    .hero{padding:18px;display:flex;justify-content:space-between;align-items:flex-start;gap:14px;flex-wrap:wrap;}
    .hero h1{font-family:"Orbitron",sans-serif;font-size:clamp(1.1rem,2.4vw,1.45rem);margin:0 0 8px;letter-spacing:.06em;}
    .hero p{margin:0;font-size:13px;color:#6B7280;max-width:560px;line-height:1.55;}
    .btn{
      display:inline-flex;align-items:center;gap:6px;padding:9px 14px;border-radius:12px;
      border:1px solid rgba(244,114,182,.35);background:#fff;color:#9d174d;font-size:13px;font-weight:700;
      text-decoration:none;cursor:pointer;
    }
    .btn:hover{background:rgba(253,242,248,.9);}
    .section-head{
      padding:12px 16px;font-size:12px;font-weight:800;letter-spacing:.1em;font-family:"Orbitron",sans-serif;
      border-bottom:1px solid rgba(196,181,253,.55);color:#7f62a3;
    }
    .section-body{padding:14px 16px 18px;}
    .ops-metric{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;}
    @media(min-width:720px){.ops-metric{grid-template-columns:repeat(4,minmax(0,1fr));}}
    .metric{padding:10px;border-radius:12px;border:1px solid rgba(244,114,182,.12);background:rgba(253,242,248,.45);}
    .metric .k{font-size:10px;color:#8f6f7f;}
    .metric .v{font-size:20px;font-weight:900;}
    .ops-card{border:1px solid rgba(244,114,182,.14);border-radius:14px;background:#fff;padding:12px;}
    .ops-title{font-size:11px;letter-spacing:.12em;color:#8d5f74;font-family:"Orbitron",sans-serif;margin-bottom:9px;}
    .shop-panels{display:grid;grid-template-columns:1fr 1fr;gap:12px;}
    @media(max-width:900px){.shop-panels{grid-template-columns:1fr;}}
    .members-table-wrap{overflow:auto;border:1px solid rgba(244,114,182,.12);border-radius:14px;}
    .members-table{width:100%;border-collapse:separate;border-spacing:0;min-width:520px;}
    .members-table th{text-align:left;font-size:10px;letter-spacing:.08em;color:#8d5f74;padding:10px;border-bottom:1px solid rgba(244,114,182,.18);}
    .members-table td{padding:11px 10px;border-bottom:1px solid rgba(244,114,182,.10);font-size:13px;}
    .members-table tbody tr:hover{background:rgba(253,242,248,.35);}
    .empty{font-size:13px;color:#8e6f7f;padding:12px;text-align:center;}
    .prob-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:8px;font-size:13px;}
    @media(min-width:640px){.prob-grid{grid-template-columns:repeat(4,minmax(0,1fr));}}
    .prob-row{border:1px solid rgba(244,114,182,.14);border-radius:10px;padding:8px 10px;background:rgba(253,242,248,.35);}
    .prob-row .lbl{font-size:10px;color:#8f6f7f;}
    .prob-row .val{font-weight:800;color:#0f172a;}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="ad-wrap">
  <section class="card hero">
    <div>
      <h1>GACHA · 뽑기</h1>
      <p>
        사용자 뽑기 화면(<a href="${ctx}/market/gacha" style="color:#db2777;font-weight:700;">/market/gacha</a>)과 동일한
        <code style="font-size:12px;background:rgba(244,114,182,.12);padding:2px 6px;border-radius:6px;">GACHA_PULL_LOG</code> 기준 집계입니다.
        가격·확률은 코드(<code style="font-size:12px;">GachaConfig</code>)에 정의되어 있으며, 변경 시 재배포가 필요합니다.
      </p>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;">
      <a class="btn" href="${ctx}/admin"><i class="fas fa-arrow-left"></i> 대시보드</a>
      <a class="btn" href="${ctx}/market/gacha"><i class="fas fa-dice"></i> 사용자 뽑기</a>
      <a class="btn" href="${ctx}/admin/shop"><i class="fas fa-coins"></i> 상점 통계</a>
    </div>
  </section>

  <section class="card">
    <div class="section-head">핵심 지표</div>
    <div class="section-body">
      <div class="ops-metric">
        <div class="metric"><div class="k">누적 뽑기 횟수 (로그 건수)</div><div class="v"><fmt:formatNumber value="${gachaTotalPulls}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">뽑기 참여 회원 수</div><div class="v"><fmt:formatNumber value="${gachaDistinctPullers}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">1회 가격 (코인)</div><div class="v"><fmt:formatNumber value="${gachaSettings.priceSingle}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">${gachaSettings.multiCount}회 가격 (코인)</div><div class="v"><fmt:formatNumber value="${gachaSettings.priceMulti}" pattern="#,##0"/></div></div>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">설정 요약 (공개 확률)</div>
    <div class="section-body">
      <div class="prob-grid">
        <c:forEach var="probRow" items="${gachaSettings.gradeProbabilities}">
          <div class="prob-row">
            <div class="lbl"><c:out value="${probRow['key']}"/></div>
            <div class="val"><c:out value="${probRow['value']}"/>%</div>
          </div>
        </c:forEach>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">실제 누적 분포 (등급별 로그 건수)</div>
    <div class="section-body">
      <c:choose>
        <c:when test="${empty gachaPullsByGrade}">
          <div class="empty">뽑기 로그가 없습니다.</div>
        </c:when>
        <c:otherwise>
          <div class="members-table-wrap">
            <table class="members-table">
              <thead><tr><th>등급</th><th style="text-align:right;">건수</th></tr></thead>
              <tbody>
              <c:forEach var="gr" items="${gachaPullsByGrade}">
                <tr>
                  <td><c:out value="${gr['grade']}"/></td>
                  <td style="text-align:right;font-weight:800;"><fmt:formatNumber value="${gr['count']}" pattern="#,##0"/></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </section>

  <section class="card">
    <div class="section-head">최근 뽑기 로그 (최대 100건)</div>
    <div class="section-body">
      <c:choose>
        <c:when test="${empty gachaRecentLogs}">
          <div class="empty">뽑기 로그가 없습니다.</div>
        </c:when>
        <c:otherwise>
          <div class="members-table-wrap">
            <table class="members-table">
              <thead>
                <tr>
                  <th>시각</th>
                  <th>회원</th>
                  <th>연습생</th>
                  <th>등급</th>
                  <th>풀</th>
                </tr>
              </thead>
              <tbody>
              <c:forEach var="row" items="${gachaRecentLogs}">
                <tr>
                  <td style="white-space:nowrap;font-size:12px;"><c:out value="${row.createdAtStr}"/></td>
                  <td>#${row.memberId} <c:out value="${empty row.memberNickname ? '' : row.memberNickname}"/></td>
                  <td>#${row.traineeId} <c:out value="${row.traineeName}"/></td>
                  <td><c:out value="${row.grade}"/></td>
                  <td><c:out value="${row.poolId}"/></td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </section>
</main>
</body>
</html>
