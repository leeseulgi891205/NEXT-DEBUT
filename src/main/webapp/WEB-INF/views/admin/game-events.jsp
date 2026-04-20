<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 · 이벤트 관리 · NEXT DEBUT</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <style>
    :root{
      --ad-bg:#fff8fc; --ad-card:#ffffff; --ad-border:rgba(244,114,182,.20); --ad-text:#332b30;
      --ad-shadow:0 14px 34px rgba(236,72,153,.10); --ad-r:18px;
    }
    *{box-sizing:border-box}
    body{margin:0;color:var(--ad-text);background:
      radial-gradient(1200px 500px at 0% -20%, rgba(251,207,232,.5), transparent 56%),
      radial-gradient(900px 480px at 100% -10%, rgba(253,242,248,.9), transparent 58%), var(--ad-bg);}
    .ad-wrap{max-width:1200px;margin:0 auto;padding:calc(var(--nav-h,68px) + 22px) 20px 42px;display:grid;gap:14px;}
    .card{background:var(--ad-card);border:1px solid var(--ad-border);border-radius:var(--ad-r);box-shadow:var(--ad-shadow);}
    .hero{padding:18px;display:flex;justify-content:space-between;align-items:flex-start;gap:14px;flex-wrap:wrap;}
    .hero h1{font-family:"Orbitron",sans-serif;font-size:clamp(1.1rem,2.4vw,1.45rem);margin:0 0 8px;letter-spacing:.06em;}
    .hero p{margin:0;font-size:13px;color:#6B7280;max-width:620px;line-height:1.55;}
    .btn{display:inline-flex;align-items:center;gap:6px;padding:9px 14px;border-radius:12px;border:1px solid rgba(244,114,182,.35);background:#fff;color:#9d174d;font-size:13px;font-weight:700;text-decoration:none;cursor:pointer;}
    .btn:hover{background:rgba(253,242,248,.9);}
    .btn.danger{background:#fff1f2;color:#be123c;border-color:#fecdd3;}
    .section-head{padding:12px 16px;font-size:12px;font-weight:800;letter-spacing:.1em;font-family:"Orbitron",sans-serif;border-bottom:1px solid rgba(196,181,253,.55);color:#7f62a3;}
    .section-body{padding:14px 16px 18px;}
    .input{width:100%;padding:10px 12px;border:1px solid rgba(244,114,182,.18);border-radius:12px;background:#fff;color:#332b30;font-size:13px;}
    textarea.input{resize:vertical;min-height:120px;}
    .empty{font-size:13px;color:#8e6f7f;padding:12px;text-align:center;}
    .grid{display:grid;grid-template-columns:360px minmax(0,1fr);gap:14px;}
    @media(max-width:980px){.grid{grid-template-columns:1fr;}}
    .list{display:grid;gap:10px;max-height:720px;overflow:auto;padding-right:4px;}
    .item{border:1px solid rgba(244,114,182,.12);border-radius:12px;padding:12px;background:#fff8fc;}
    .item-head{display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:8px;}
    .chip{font-size:10px;padding:5px 8px;border-radius:999px;border:1px solid rgba(244,114,182,.24);background:rgba(253,242,248,.8);color:#9d1b5f;white-space:nowrap;}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>
<main class="ad-wrap">
  <section class="card hero">
    <div>
      <h1>GAME · EVENTS</h1>
      <p>인게임 이벤트(아이돌 이름 맞추기) 문제를 등록/수정/비활성화하고 표시 순서를 관리합니다.</p>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;">
      <a class="btn" href="${ctx}/admin"><i class="fas fa-arrow-left"></i> 대시보드</a>
      <a class="btn" href="${ctx}/admin/game-scenes"><i class="fas fa-scroll"></i> 상황 지문 관리</a>
      <a class="btn" href="${ctx}/admin/game-choices"><i class="fas fa-list-check"></i> 채팅 매핑 규칙 관리</a>
    </div>
  </section>

  <section class="card">
    <div class="section-head">이벤트 문제 관리</div>
    <div class="section-body grid">
      <div>
        <div style="font-weight:800;font-size:14px;margin-bottom:10px;color:#7c1d51;">문제 추가</div>
        <form method="post" action="${ctx}/admin/game-events" class="card" style="padding:14px;">
          <div style="display:grid;grid-template-columns:1fr;gap:8px;">
            <textarea class="input" name="hint" rows="5" placeholder="힌트 문구" required></textarea>
            <input class="input" type="text" name="answer" placeholder="정답 그룹명" required>
            <input class="input" type="number" min="1" name="sortOrder" value="1" required>
            <label style="display:flex;align-items:center;gap:8px;font-size:13px;color:#6b7280;">
              <input type="checkbox" name="enabled" checked>
              인게임 노출 활성화
            </label>
          </div>
          <div style="margin-top:10px;display:flex;justify-content:flex-end;">
            <button class="btn" type="submit">문제 추가</button>
          </div>
        </form>
      </div>

      <div>
        <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:10px;flex-wrap:wrap;">
          <div style="font-weight:800;font-size:14px;color:#7c1d51;">문제 목록 · 수정</div>
          <input class="input" type="search" id="gameEventEditFilter" placeholder="힌트 · 정답 검색…" style="max-width:340px;">
        </div>
        <c:choose>
          <c:when test="${empty gameMiniQuizzes}">
            <div class="empty card">등록된 이벤트 문제가 없습니다.</div>
          </c:when>
          <c:otherwise>
            <div class="list">
              <c:forEach var="quiz" items="${gameMiniQuizzes}">
                <c:set var="quizSearchRaw" value="${quiz.hint} ${quiz.answer}" />
                <form method="post" action="${ctx}/admin/game-events/${quiz.id}" class="item js-game-event-edit" data-search="${fn:toLowerCase(quizSearchRaw)}">
                  <div class="item-head">
                    <div style="font-weight:800;color:#7c1d51;">#${quiz.id}</div>
                    <span class="chip">${quiz.enabled ? '활성' : '비활성'}</span>
                  </div>
                  <textarea class="input" name="hint" rows="4" required><c:out value="${quiz.hint}"/></textarea>
                  <div style="display:grid;grid-template-columns:1fr .6fr;gap:8px;margin-top:8px;">
                    <input class="input" type="text" name="answer" value="${quiz.answer}" required>
                    <input class="input" type="number" min="1" name="sortOrder" value="${quiz.sortOrder}" required>
                  </div>
                  <label style="display:flex;align-items:center;gap:8px;font-size:13px;color:#6b7280;margin-top:8px;">
                    <input type="checkbox" name="enabled" value="true" ${quiz.enabled ? 'checked' : ''}>
                    인게임 노출 활성화
                  </label>
                  <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;flex-wrap:wrap;">
                    <button class="btn" type="submit">저장</button>
                    <button class="btn danger" type="submit" formaction="${ctx}/admin/game-events/${quiz.id}/delete" formnovalidate onclick="return confirm('이 이벤트 문제를 삭제할까요?');">삭제</button>
                  </div>
                </form>
              </c:forEach>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
    </div>
  </section>
</main>
<script>
function wireListFilter(inputId, itemSelector){
  var input = document.getElementById(inputId);
  if (!input) return;
  var items = Array.prototype.slice.call(document.querySelectorAll(itemSelector));
  function applyFilter(){
    var q = (input.value || '').trim().toLowerCase();
    items.forEach(function(el){
      var hay = String(el.getAttribute('data-search') || '').toLowerCase();
      el.style.display = (!q || hay.indexOf(q) !== -1) ? '' : 'none';
    });
  }
  input.addEventListener('input', applyFilter);
  applyFilter();
}
wireListFilter('gameEventEditFilter', '.js-game-event-edit');
</script>
</body>
</html>
