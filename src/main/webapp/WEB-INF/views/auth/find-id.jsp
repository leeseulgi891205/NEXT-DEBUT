<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NEXT DEBUT - 아이디 찾기</title>
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
      <h2>아이디 찾기</h2>
      <p class="auth-desc">가입 시 등록한 이름과 이메일을 입력하면 아이디를 알려드립니다.</p>

      <%-- 결과 표시 --%>
      <c:if test="${not empty foundId}">
        <div class="find-result">
          <div class="find-result__label">찾은 아이디</div>
          <div class="find-result__value"><c:out value="${foundId}"/></div>
          <a href="<c:url value='/login'/>" class="auth-submit" style="display:block;text-align:center;text-decoration:none;margin-top:14px;">
            로그인하러 가기
          </a>
        </div>
      </c:if>

      <%-- 입력 폼 (결과 없을 때 표시) --%>
      <c:if test="${empty foundId}">
        <form method="post" action="<c:url value='/find-id'/>">
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
          <div class="actions">
            <button type="submit" class="auth-submit">아이디 찾기</button>
          </div>
        </form>
      </c:if>

      <div class="login-links">
        <a href="<c:url value='/login'/>">로그인</a> |
        <a href="<c:url value='/find-pw'/>">비밀번호 찾기</a> |
        <a href="<c:url value='/signup'/>"><b>회원가입</b></a>
      </div>
    </div>
  </main>

  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
