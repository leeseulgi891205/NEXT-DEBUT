<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NEXT DEBUT - 비밀번호 찾기</title>
  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <link rel="stylesheet" href="<c:url value='/css/auth.css'/>" />
</head>

<body class="page-main min-h-screen flex flex-col">
  <%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

  <main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">

    <%-- 토스트 알림 --%>
    <c:if test="${not empty error}">
      <div class="toast toast--err"><c:out value="${error}"/></div>
    </c:if>

    <div class="auth-card premium-glass">
      <h2>비밀번호 재설정</h2>
      <p class="auth-desc">아이디, 이름, 이메일이 모두 일치하면 새 비밀번호로 변경할 수 있습니다.</p>

      <form method="post" action="<c:url value='/find-pw'/>">

        <div class="form-group">
          <label for="mid">아이디</label>
          <input type="text" name="mid" id="mid"
                 placeholder="가입한 아이디"
                 required value="<c:out value='${prev_mid}'/>" />
        </div>

        <div class="form-group">
          <label for="mname">이름</label>
          <input type="text" name="mname" id="mname"
                 placeholder="가입 시 입력한 이름"
                 required value="<c:out value='${prev_mname}'/>" />
        </div>

        <div class="form-group">
          <label for="email">이메일</label>
          <input type="email" name="email" id="email"
                 placeholder="가입 시 입력한 이메일"
                 required value="<c:out value='${prev_email}'/>" />
        </div>

        <%-- 구분선 --%>
        <div class="find-divider">새 비밀번호 입력</div>

        <div class="form-group">
          <label for="newPassword1">새 비밀번호</label>
          <input type="password" name="newPassword1" id="newPassword1"
                 placeholder="6자 이상 입력하세요"
                 required minlength="6"
                 oninput="checkPwMatch()" />
        </div>

        <div class="form-group">
          <label for="newPassword2">새 비밀번호 확인</label>
          <input type="password" name="newPassword2" id="newPassword2"
                 placeholder="비밀번호를 다시 입력하세요"
                 required minlength="6"
                 oninput="checkPwMatch()" />
          <small id="pw-match-msg" style="display:none; margin-top:6px; font-size:12px;"></small>
        </div>

        <div class="actions">
          <button type="submit" class="auth-submit" id="btnSubmit">비밀번호 변경하기</button>
        </div>
      </form>

      <div class="login-links">
        <a href="<c:url value='/login'/>">로그인</a> |
        <a href="<c:url value='/find-id'/>">아이디 찾기</a> |
        <a href="<c:url value='/signup'/>"><b>회원가입</b></a>
      </div>
    </div>
  </main>

  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>

  <script>
    /* 비밀번호 일치 여부 실시간 확인 */
    function checkPwMatch() {
      var pw1 = document.getElementById('newPassword1').value;
      var pw2 = document.getElementById('newPassword2').value;
      var msg = document.getElementById('pw-match-msg');
      var btn = document.getElementById('btnSubmit');
      if (pw2.length === 0) { msg.style.display = 'none'; return; }
      msg.style.display = 'block';
      if (pw1 === pw2) {
        msg.textContent = '✔ 비밀번호가 일치합니다';
        msg.style.color = 'rgba(134,239,172,0.9)';
        btn.disabled = false;
      } else {
        msg.textContent = '✖ 비밀번호가 일치하지 않습니다';
        msg.style.color = 'rgba(248,113,113,0.9)';
      }
    }
  </script>
</body>
</html>
