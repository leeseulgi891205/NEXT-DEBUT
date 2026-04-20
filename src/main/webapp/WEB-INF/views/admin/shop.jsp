<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 · 상점 통계 · NEXT DEBUT</title>
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
    .hero p{margin:0;font-size:13px;color:#6B7280;max-width:520px;line-height:1.55;}
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
    .shop-panels{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:12px;}
    @media(max-width:1180px){.shop-panels{grid-template-columns:1fr 1fr;}}
    @media(max-width:900px){.shop-panels{grid-template-columns:1fr;}}
    .metric .s{margin-top:6px;font-size:11px;color:#9ca3af;line-height:1.35;}
    @media(max-width:720px){
      .shop-charge-row{grid-template-columns:1fr !important;}
    }
    .members-table-wrap{overflow:auto;border:1px solid rgba(244,114,182,.12);border-radius:14px;}
    .members-table{width:100%;border-collapse:separate;border-spacing:0;min-width:360px;}
    .members-table th{text-align:left;font-size:10px;letter-spacing:.08em;color:#8d5f74;padding:10px;border-bottom:1px solid rgba(244,114,182,.18);}
    .members-table td{padding:11px 10px;border-bottom:1px solid rgba(244,114,182,.10);font-size:13px;}
    .members-table tbody tr:hover{background:rgba(253,242,248,.35);}
    .empty{font-size:13px;color:#8e6f7f;padding:12px;text-align:center;}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="ad-wrap">
  <section class="card hero">
    <div>
      <h1>SHOP · MARKET</h1>
      <p>사용자 상점(<a href="${ctx}/market/shop" style="color:#db2777;font-weight:700;">/market/shop</a>)과 동일한 DB 기준으로 코인·인벤토리를 집계합니다.</p>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;">
      <a class="btn" href="${ctx}/admin"><i class="fas fa-arrow-left"></i> 대시보드</a>
      <a class="btn" href="${ctx}/market/shop"><i class="fas fa-store"></i> 사용자 상점</a>
      <a class="btn" href="${ctx}/admin/gacha"><i class="fas fa-dice"></i> 뽑기 통계</a>
    </div>
  </section>

  <section class="card">
    <div class="section-head">핵심 지표</div>
    <div class="section-body">
      <div class="ops-metric">
        <div class="metric"><div class="k">전체 코인 합계</div><div class="v"><fmt:formatNumber value="${shopTotalCoins}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">아이템 총 수량</div><div class="v"><fmt:formatNumber value="${shopTotalItemQty}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">인벤 보유 회원</div><div class="v"><fmt:formatNumber value="${shopMembersWithInventory}" pattern="#,##0"/></div></div>
        <div class="metric"><div class="k">회원당 평균 코인</div><div class="v"><fmt:formatNumber value="${shopAvgCoin}" pattern="#,##0"/></div></div>
      </div>
      <div class="shop-charge-row" style="margin-top:12px;display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:8px;">
        <div class="metric"><div class="k">충전 코인 · 오늘</div><div class="v"><fmt:formatNumber value="${shopChargeDay}" pattern="#,##0"/></div><div class="s">자정(서울) 이후</div></div>
        <div class="metric"><div class="k">충전 코인 · 최근 7일</div><div class="v"><fmt:formatNumber value="${shopChargeWeek}" pattern="#,##0"/></div><div class="s">롤링 7일</div></div>
        <div class="metric"><div class="k">충전 코인 · 이번 달</div><div class="v"><fmt:formatNumber value="${shopChargeMonth}" pattern="#,##0"/></div><div class="s">1일 0시(서울)부터</div></div>
      </div>
    </div>
  </section>

  <section class="card">
    <div class="section-head">아이템 · 코인 순위</div>
    <div class="section-body">
      <div class="shop-panels">
        <div class="ops-card">
          <div class="ops-title">아이템별 누적 보유 수량</div>
          <c:choose>
            <c:when test="${empty shopTopItems}">
              <div class="empty">인벤토리 데이터가 없습니다.</div>
            </c:when>
            <c:otherwise>
              <div class="members-table-wrap">
                <table class="members-table">
                  <thead><tr><th>아이템</th><th style="text-align:right;">수량</th></tr></thead>
                  <tbody>
                  <c:forEach var="it" items="${shopTopItems}">
                    <tr>
                      <td><c:out value="${it.itemName}"/></td>
                      <td style="text-align:right;font-weight:800;"><fmt:formatNumber value="${it.totalQty}" pattern="#,##0"/></td>
                    </tr>
                  </c:forEach>
                  </tbody>
                </table>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
        <div class="ops-card">
          <div class="ops-title">코인 보유 상위 회원</div>
          <c:choose>
            <c:when test="${empty shopTopCoinMembers}">
              <div class="empty">회원 데이터가 없습니다.</div>
            </c:when>
            <c:otherwise>
              <div class="members-table-wrap">
                <table class="members-table">
                  <thead><tr><th>회원</th><th style="text-align:right;">코인</th></tr></thead>
                  <tbody>
                  <c:forEach var="mc" items="${shopTopCoinMembers}">
                    <tr>
                      <td>#${mc.mno} <c:out value="${empty mc.nickname ? '(닉없음)' : mc.nickname}"/></td>
                      <td style="text-align:right;font-weight:800;"><fmt:formatNumber value="${mc.coin}" pattern="#,##0"/></td>
                    </tr>
                  </c:forEach>
                  </tbody>
                </table>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
        <div class="ops-card">
          <div class="ops-title">상품별 구매 건수 (상위)</div>
          <c:choose>
            <c:when test="${empty shopTopPurchaseItems}">
              <div class="empty">구매 로그가 없습니다.</div>
            </c:when>
            <c:otherwise>
              <div class="members-table-wrap">
                <table class="members-table">
                  <thead><tr><th>상품</th><th style="text-align:right;">구매</th></tr></thead>
                  <tbody>
                  <c:forEach var="pi" items="${shopTopPurchaseItems}">
                    <tr>
                      <td><c:out value="${pi.itemName}"/></td>
                      <td style="text-align:right;font-weight:800;"><fmt:formatNumber value="${pi.purchaseCount}" pattern="#,##0"/>건</td>
                    </tr>
                  </c:forEach>
                  </tbody>
                </table>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </section>
</main>
</body>
</html>
