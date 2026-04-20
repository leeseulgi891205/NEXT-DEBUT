<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NEXT DEBUT - 회원가입</title>

  <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
  <link rel="stylesheet" href="<c:url value='/css/auth.css'/>" />
</head>

<body class="page-main min-h-screen flex flex-col">
  <%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

  <main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
    <div class="auth-card premium-glass signup-select-card" style="max-width: 620px;">
    <div class="signup-hero">
        <img src="<c:url value='/images/signup.png'/>" width="160" height="160" alt="회원가입 환영" />
    </div>
    <div class="signup-welcome-text">
        NEXT DEBUT에 오신 것을 환영합니다!
    </div>
    <p class="signup-select-desc">
      이메일로 가입하거나 SNS 계정으로 바로 시작할 수 있어요.
    </p>

      <div class="signup-select-list">
        <a href="<c:url value='/signup/form'/>" class="signup-select-btn signup-select-btn--email">
          <span class="signup-select-btn__icon">✉</span>
          <span class="signup-select-btn__body">
            <span class="signup-select-btn__main">기본 회원가입</span>
            <span class="signup-select-btn__sub">이메일과 비밀번호로 계정을 생성합니다</span>
          </span>
        </a>

        <a href="<c:url value='/oauth2/authorization/google'/>" class="signup-select-btn signup-select-btn--google">
          <span class="signup-select-btn__icon">G</span>
          <span class="signup-select-btn__body">
            <span class="signup-select-btn__main">Google로 회원가입</span>
            <span class="signup-select-btn__sub">구글 계정으로 간편하게 가입합니다</span>
          </span>
        </a>

        <a href="<c:url value='/oauth2/authorization/naver'/>" class="signup-select-btn signup-select-btn--naver">
          <span class="signup-select-btn__icon">N</span>
          <span class="signup-select-btn__body">
            <span class="signup-select-btn__main">Naver로 회원가입</span>
            <span class="signup-select-btn__sub">네이버 계정으로 간편하게 가입합니다</span>
          </span>
        </a>

        <a href="<c:url value='/oauth2/authorization/kakao'/>" class="signup-select-btn signup-select-btn--kakao">
            <span class="signup-select-btn__icon">K</span>
            <span class="signup-select-btn__body">
                <span class="signup-select-btn__main">Kakao로 회원가입 / 로그인</span>
                <span class="signup-select-btn__sub">카카오 계정으로 가입 후 바로 로그인합니다</span>
            </span>
        </a>

      </div>
    </div>
  </main>

  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
