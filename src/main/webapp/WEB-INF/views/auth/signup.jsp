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
  <script defer src="<c:url value='/js/signup.js'/>"></script>
  <script src="//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>
</head>

<body class="page-main min-h-screen flex flex-col">
  <%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

  <main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">

    <c:if test="${not empty error}">
      <div class="toast toast--err"><c:out value="${error}"/></div>
    </c:if>

    <div class="auth-card premium-glass" style="max-width: 620px;">
      <h2>회원가입</h2>

      <form method="post" action="<c:url value='/signup'/>" id="signupForm" novalidate>

        <!-- 아이디 -->
        <div class="form-group">
          <label for="username">아이디 <span class="text-danger">*</span></label>
          <input type="text" name="username" id="username" placeholder="아이디 입력" required autocomplete="off" value="<c:out value='${prev_username}'/>" />
          <small id="id_check_msg" class="help-msg"></small>

          <div class="rule-box">
            <div class="rule-title">아이디 규칙</div>
            <div class="rule-item">• 6~20자</div>
            <div class="rule-item">• 영문/숫자만 사용 가능</div>
            <div class="rule-item">• 영문과 숫자를 모두 포함 권장</div>
          </div>
        </div>

        <!-- 이름 -->
        <div class="form-group">
          <label for="real_name">이름 <span class="text-danger">*</span></label>
          <input type="text" name="real_name" id="real_name" placeholder="실명 입력" required value="<c:out value='${prev_real_name}'/>" />
        </div>

        <!-- 주민등록번호 -->
        <div class="form-group">
          <label>주민등록번호 <span class="text-danger">*</span></label>
          <div class="split-row split-row--rrn">
            <input type="text" id="jumin1" placeholder="앞 6자리" inputmode="numeric" maxlength="6" class="split-input" required autocomplete="off" />
            <span class="split-dash">-</span>
            <div class="input-with-toggle">
              <input type="password" id="jumin2" placeholder="뒤 7자리" inputmode="numeric" maxlength="7" class="split-input" required autocomplete="off" />
              <button type="button" class="toggle-eye" data-toggle-target="jumin2" aria-label="주민번호 뒷자리 보기">보기</button>
            </div>
          </div>
          <input type="hidden" name="jumin" id="jumin" />
          <small id="jumin_msg" class="help-msg"></small>
        </div>

        <!-- 닉네임 -->
        <div class="form-group">
          <label for="nickname">닉네임 <span class="text-danger">*</span></label>
          <input type="text" name="nickname" id="nickname" placeholder="닉네임 입력" required autocomplete="off" value="<c:out value='${prev_nickname}'/>" />
          <small id="nickname_check_msg" class="help-msg"></small>

          <div class="rule-box">
            <div class="rule-title">닉네임 규칙</div>
            <div class="rule-item">• 3~12자</div>
            <div class="rule-item">• 한글/영문/숫자 사용 가능</div>
          </div>
        </div>

        <!-- 비밀번호 -->
        <div class="form-group">
          <label for="password1">비밀번호 <span class="text-danger">*</span></label>
          <div class="input-with-toggle">
            <input type="password" name="password1" id="password1" placeholder="비밀번호 입력" required />
            <button type="button" class="toggle-eye" data-toggle-target="password1" aria-label="비밀번호 보기">보기</button>
          </div>

          <div class="rule-box">
            <div class="rule-title">비밀번호 조건</div>
            <div class="rule-item">• 최소 8자 이상</div>
            <div class="rule-item">• 영문 + 숫자 + 특수기호 포함 권장</div>
            <div class="rule-sub">(최종 검증은 서버에서 처리됩니다.)</div>
          </div>

          <small id="pw_strength_msg" class="help-msg"></small>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="form-group">
          <label for="password2">비밀번호 확인 <span class="text-danger">*</span></label>
          <div class="input-with-toggle">
            <input type="password" name="password2" id="password2" placeholder="비밀번호 재입력" required />
            <button type="button" class="toggle-eye" data-toggle-target="password2" aria-label="비밀번호 확인 보기">보기</button>
          </div>
          <small id="pw_match_msg" class="help-msg"></small>
        </div>

        <!-- 주소 -->
        <div class="form-group">
          <label>주소</label>
          <div class="inline-row">
            <input type="text" id="address_main" name="address" placeholder="주소" style="flex:1;" required value="<c:out value='${prev_address}'/>" />
            <button type="button" id="btnAddressSearch" class="btn-check">검색</button>
          </div>

          <input type="text" id="address_detail" name="address_detail" placeholder="상세주소 입력 (예: 101동 1203호)" style="margin-top: 8px;" value="<c:out value='${prev_address_detail}'/>" />
</div>

        <!-- 휴대폰 (선택) -->
        <div class="form-group">
          <label>휴대폰 번호</label>
          <div class="split-row split-row--phone">
            <input type="text" id="phone1" placeholder="010" inputmode="numeric" maxlength="3" class="split-input" />
            <span class="split-dash">-</span>
            <input type="text" id="phone2" placeholder="0000" inputmode="numeric" maxlength="4" class="split-input" />
            <span class="split-dash">-</span>
            <input type="text" id="phone3" placeholder="0000" inputmode="numeric" maxlength="4" class="split-input" />
          </div>
          <input type="hidden" name="phone" id="phone" value="<c:out value='${prev_phone}'/>" />
        </div>

		<!-- 이메일 -->
		<div class="form-group">
		  <label for="email">이메일 <span class="text-danger">*</span></label>
		  <div class="inline-row">
		    <input type="email" name="email" id="email" placeholder="example@naver.com"
		           required value="<c:out value='${prev_email}'/>" style="flex:1;" />
		    <button type="button" id="btnSendCode" class="btn-check">인증번호 전송</button>
		  </div>
		  <div id="codeArea" style="display:none; margin-top:8px;">
		    <div class="inline-row">
		      <input type="text" id="emailCode" placeholder="인증번호 6자리"
		             maxlength="6" style="flex:1;" />
		      <button type="button" id="btnVerifyCode" class="btn-check">확인</button>
		    </div>
		  </div>
		  <small id="email_verify_msg" class="help-msg"></small>
		</div>

        <button type="submit" class="auth-submit" id="btnSignupSubmit" disabled>가입하기</button>
      </form>

      <div class="login-links">
        이미 계정이 있나요? <a href="<c:url value='/login'/>"><b>로그인</b></a>
      </div>
    </div>
  </main>

  <%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
