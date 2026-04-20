<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 · 공지사항 · NEXT DEBUT</title>
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
      text-decoration:none;cursor:pointer;font-family:inherit;
    }
    .btn:hover{background:rgba(253,242,248,.9);}
    .btn.primary{background:linear-gradient(135deg,#f472b6,#e879f9);border-color:transparent;color:#fff;}
    .btn.primary:hover{filter:brightness(1.03);}
    .section-head{
      padding:12px 16px;font-size:12px;font-weight:800;letter-spacing:.1em;font-family:"Orbitron",sans-serif;
      border-bottom:1px solid rgba(196,181,253,.55);color:#7f62a3;
    }
    .section-body{padding:14px 16px 18px;}
    .input{
      border:1px solid rgba(244,114,182,.24);
      border-radius:12px;
      padding:10px 12px;
      background:#fff;
      color:#332b30;
      font-size:14px;
      width:100%;
      font-family:inherit;
    }
    textarea.input{min-height:120px;resize:vertical;line-height:1.5;}
    .members-table-wrap{overflow:auto;border:1px solid rgba(244,114,182,.12);border-radius:14px;}
    .members-table{width:100%;border-collapse:separate;border-spacing:0;min-width:360px;}
    .members-table th{text-align:left;font-size:10px;letter-spacing:.08em;color:#8d5f74;padding:10px;border-bottom:1px solid rgba(244,114,182,.18);}
    .members-table td{padding:11px 10px;border-bottom:1px solid rgba(244,114,182,.10);font-size:13px;}
    .members-table tbody tr:hover{background:rgba(253,242,248,.35);}
    .empty{font-size:13px;color:#8e6f7f;padding:12px;text-align:center;}
    .alert{
      padding:11px 14px;border-radius:14px;font-size:13px;
      border:1px solid rgba(134,239,172,.32);
      background:rgba(220,252,231,.65);color:#166534;
    }
    .form-grid{display:grid;gap:10px;max-width:640px;}
    .form-actions{margin-top:4px;}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="ad-wrap">
  <section class="card hero">
    <div>
      <h1>NOTICES · 공지</h1>
      <p>공지사항을 등록하면 사용자 공지 게시판(<a href="${ctx}/boards/notice" style="color:#db2777;font-weight:700;">/boards/notice</a>)에 노출됩니다.</p>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;">
      <a class="btn" href="${ctx}/admin"><i class="fas fa-arrow-left"></i> 대시보드</a>
      <a class="btn" href="${ctx}/boards/notice"><i class="fas fa-bullhorn"></i> 사용자 공지판</a>
    </div>
  </section>

  <c:if test="${not empty success}">
    <div class="alert"><i class="fas fa-check-circle"></i> ${success}</div>
  </c:if>

  <section class="card">
    <div class="section-head">새 공지 등록</div>
    <div class="section-body">
      <form method="post" action="${ctx}/admin/notices" class="form-grid">
        <div>
          <label for="noticeTitle" style="display:block;font-size:12px;color:#6b7280;margin-bottom:6px;">제목</label>
          <input id="noticeTitle" class="input" type="text" name="title" required maxlength="200" placeholder="공지 제목"/>
        </div>
        <div>
          <label for="noticeContent" style="display:block;font-size:12px;color:#6b7280;margin-bottom:6px;">내용</label>
          <textarea id="noticeContent" class="input" name="content" required placeholder="공지 본문"></textarea>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn primary"><i class="fas fa-paper-plane"></i> 등록</button>
        </div>
      </form>
    </div>
  </section>

  <section class="card">
    <div class="section-head">등록된 공지 · <fmt:formatNumber value="${empty notices ? 0 : fn:length(notices)}" pattern="#,##0"/>건</div>
    <div class="section-body">
      <c:choose>
        <c:when test="${empty notices}">
          <div class="empty">등록된 공지가 없습니다.</div>
        </c:when>
        <c:otherwise>
          <div class="members-table-wrap">
            <table class="members-table">
              <thead>
              <tr>
                <th style="width:72px;">ID</th>
                <th>제목</th>
                <th style="width:160px;">작성일</th>
                <th style="width:100px;text-align:right;">보기</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="n" items="${notices}">
                <tr>
                  <td>#${n.id}</td>
                  <td style="min-width:0;">
                    <a href="${ctx}/boards/notice/${n.id}" style="color:#be185d;font-weight:700;text-decoration:none;">
                      <c:out value="${n.title}"/>
                    </a>
                  </td>
                  <td style="font-size:12px;color:#6b7280;white-space:nowrap;"><c:out value="${n.createdAtStr}"/></td>
                  <td style="text-align:right;">
                    <a class="btn" href="${ctx}/boards/notice/${n.id}" style="font-size:12px;padding:7px 10px;">열기</a>
                  </td>
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
