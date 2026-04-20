<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>관리자 · 상황 지문 관리 · NEXT DEBUT</title>
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
    .muted{font-size:12px;color:#8e6f7f;}
  </style>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>
<main class="ad-wrap">
  <section class="card hero">
    <div>
      <h1>GAME · SCENES</h1>
      <p>게임 지문은 전용 화면에서 추가하고, 오른쪽 목록에서 바로 수정/삭제할 수 있게 분리했습니다.</p>
    </div>
    <div style="display:flex;flex-wrap:wrap;gap:8px;">
      <a class="btn" href="${ctx}/admin"><i class="fas fa-arrow-left"></i> 대시보드</a>
      <a class="btn" href="${ctx}/admin/game-choices"><i class="fas fa-list-check"></i> 채팅 매핑 규칙 관리</a>
      <a class="btn" href="${ctx}/admin/game-events"><i class="fas fa-bolt"></i> 이벤트 관리</a>
      <a class="btn" href="${ctx}/admin/game-stats"><i class="fas fa-chart-bar"></i> 게임 통계</a>
    </div>
  </section>

  <datalist id="dl-game-phase">
    <c:forEach var="p" items="${gamePhaseSuggestions}">
      <option value="<c:out value='${p}'/>"></option>
    </c:forEach>
  </datalist>
  <datalist id="dl-game-event-type">
    <c:forEach var="e" items="${gameEventTypeSuggestions}">
      <option value="<c:out value='${e}'/>"></option>
    </c:forEach>
  </datalist>

  <section class="card">
    <div class="section-head">상황 지문 관리</div>
    <div class="section-body grid">
      <div>
        <div style="font-weight:800;font-size:14px;margin-bottom:10px;color:#7c1d51;">지문 추가</div>
        <form method="post" action="${ctx}/admin/game-scenes" class="card" style="padding:14px;">
          <div style="display:grid;grid-template-columns:1fr;gap:8px;">
            <input class="input" type="text" name="phase" list="dl-game-phase" autocomplete="off" placeholder="페이즈 예: M1_H1_MORNING" required>
            <input class="input" type="text" name="eventType" list="dl-game-event-type" autocomplete="off" placeholder="이벤트 유형 예: TRAINING_EVENT" required>
            <input class="input" type="text" name="title" placeholder="제목" required>
            <textarea class="input" name="description" rows="6" placeholder="내용" required></textarea>
          </div>
          <div style="margin-top:10px;display:flex;justify-content:flex-end;">
            <button class="btn" type="submit">지문 추가</button>
          </div>
        </form>
      </div>

      <div>
        <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:10px;flex-wrap:wrap;">
          <div style="font-weight:800;font-size:14px;color:#7c1d51;">지문 목록 · 수정</div>
          <input class="input" type="search" id="gameSceneEditFilter" placeholder="phase · 제목 · 이벤트 · 본문 검색…" style="max-width:340px;">
        </div>
        <c:choose>
          <c:when test="${empty gameScenes}">
            <div class="empty card">등록된 게임 지문이 없습니다.</div>
          </c:when>
          <c:otherwise>
            <div class="list">
              <c:forEach var="scene" items="${gameScenes}">
                <c:set var="sceneEditSearchRaw" value="${scene.phase} ${scene.title} ${scene.eventType} ${scene.description}" />
                <form method="post" action="${ctx}/admin/game-scenes/${scene.id}" class="item js-game-scene-edit" data-search="${fn:toLowerCase(sceneEditSearchRaw)}">
                  <div class="item-head">
                    <div style="font-weight:800;color:#7c1d51;">#${scene.id}</div>
                    <span class="chip">${scene.phase}</span>
                  </div>
                  <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;">
                    <input class="input" type="text" name="phase" value="${scene.phase}" list="dl-game-phase" autocomplete="off" required>
                    <input class="input" type="text" name="eventType" value="${scene.eventType}" list="dl-game-event-type" autocomplete="off" required>
                  </div>
                  <input class="input" type="text" name="title" value="${scene.title}" required style="margin-top:8px;">
                  <textarea class="input" name="description" rows="5" required style="margin-top:8px;"><c:out value="${scene.description}"/></textarea>
                  <div class="muted" style="margin-top:8px;"><c:out value="${scene.eventType}"/></div>
                  <div style="margin-top:10px;display:flex;justify-content:flex-end;gap:8px;flex-wrap:wrap;">
                    <button class="btn" type="submit">저장</button>
                    <button class="btn danger" type="submit" formaction="${ctx}/admin/game-scenes/${scene.id}/delete" formnovalidate onclick="return confirm('이 지문을 삭제할까요?');">삭제</button>
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
wireListFilter('gameSceneEditFilter', '.js-game-scene-edit');
</script>
</body>
</html>
