(function () {
  function qs(sel) { return document.querySelector(sel); }
  function qsa(sel) { return Array.from(document.querySelectorAll(sel)); }

  const MID_RE = /^[A-Za-z0-9]{6,20}$/;
  const NICK_RE = /^[A-Za-z0-9가-힣]{3,12}$/;

  const state = {
    midRule: false,
    midAvailable: false,
    nickRule: false,
    nickAvailable: false,
    pwStrong: false,
    pwMatch: false,
    requiredFilled: false,
    emailOk: false,
    nameOk: false,
    addressOk: false,
    juminOk: false,
  };

  function setMsg(el, msg, ok) {
    if (!el) return;
    el.textContent = msg || '';
    el.dataset.ok = ok === true ? "1" : ok === false ? "0" : "";
  }

  function paintByOk(input, ok, bad) {
    if (!input) return;
    input.classList.toggle('input-ok', !!ok);
    input.classList.toggle('input-bad', !!bad);
  }

  function debounce(fn, wait) {
    let t = null;
    return function (...args) {
      if (t) clearTimeout(t);
      t = setTimeout(() => fn.apply(this, args), wait);
    };
  }

  async function getJson(url) {
    const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error('request_failed');
    return res.json();
  }

  function hasStrongPw(pw) {
    if (!pw) return false;
    const lenOk = pw.length >= 8;
    const hasAlpha = /[A-Za-z]/.test(pw);
    const hasNum = /\d/.test(pw);
    const hasSpecial = /[^A-Za-z\d]/.test(pw);
    return lenOk && hasAlpha && hasNum && hasSpecial;
  }

  function recomputeRequired() {
    const username = qs('#username');
    const realName = qs('#real_name');
    const nickname = qs('#nickname');
    const email = qs('#email');
    const address = qs('#address_main');
    const pw1 = qs('#password1');
    const pw2 = qs('#password2');
    const jumin = qs('#jumin');

    state.nameOk = !!realName && realName.value.trim().length > 0;
    state.addressOk = !!address && address.value.trim().length > 0;

    state.requiredFilled = [username, realName, nickname, email, address, pw1, pw2, jumin]
      .filter(Boolean)
      .every(el => el.value.trim().length > 0);
  }

  function updateSubmit() {
    recomputeRequired();
    const submitBtn = qs('#btnSignupSubmit');
    if (!submitBtn) return;

    const can =
      state.requiredFilled &&
      state.midRule && state.midAvailable &&
      state.nickRule && state.nickAvailable &&
      state.pwStrong && state.pwMatch &&
      state.emailOk && state.nameOk && state.addressOk &&
      state.juminOk;

    submitBtn.disabled = !can;
  }

  function bindPasswordRules() {
    const pw1 = qs('#password1');
    const pw2 = qs('#password2');
    const strengthMsg = qs('#pw_strength_msg');
    const matchMsg = qs('#pw_match_msg');

    function update() {
      const p1 = pw1 ? pw1.value : '';
      const p2 = pw2 ? pw2.value : '';

      state.pwStrong = hasStrongPw(p1);
      setMsg(strengthMsg, state.pwStrong ? '비밀번호 조건 OK' : '8자 이상 + 영문/숫자/특수문자 포함', p1.length === 0 ? null : state.pwStrong);
      paintByOk(pw1, state.pwStrong, p1.length > 0 && !state.pwStrong);

      state.pwMatch = (p1.length > 0 && p1 === p2);
      setMsg(matchMsg, (p2.length === 0) ? '' : (state.pwMatch ? '비밀번호 일치' : '비밀번호가 다릅니다'), p2.length === 0 ? null : state.pwMatch);
      paintByOk(pw2, state.pwMatch, p2.length > 0 && !state.pwMatch);

      updateSubmit();
    }

    [pw1, pw2].filter(Boolean).forEach(el => el.addEventListener('input', update));
    update();
  }

  function bindToggles() {
    qsa('.toggle-eye').forEach(btn => {
      btn.addEventListener('click', () => {
        const targetId = btn.getAttribute('data-toggle-target');
        const input = targetId ? document.getElementById(targetId) : null;
        if (!input) return;
        input.type = (input.type === 'password') ? 'text' : 'password';
      });
    });
  }

  function bindMidAutoCheck() {
    const input = qs('#username');
    const msgEl = qs('#id_check_msg');
    if (!input) return;

    let isComposing = false; // 한글 조합 중 여부 추적

    // 한글 조합 시작 (타이핑 중인 상태)
    input.addEventListener('compositionstart', () => { isComposing = true; });

    // 한글 조합 완료 → 이때 한글 제거
    input.addEventListener('compositionend', () => {
      isComposing = false;
      const pos = input.selectionStart;
      const before = input.value;
      input.value = before.replace(/[ㄱ-ㅎㅏ-ㅣ가-힣]/g, '');
      // 커서 위치 보정 (제거된 글자 수만큼 앞으로)
      const removed = before.length - input.value.length;
      input.setSelectionRange(pos - removed, pos - removed);
      state.midAvailable = false;
      run();
      updateSubmit();
    });

    const run = debounce(async () => {
      const v = input.value.trim();
      state.midAvailable = false;

      if (v.length === 0) {
        state.midRule = false;
        setMsg(msgEl, '', null);
        paintByOk(input, false, false);
        updateSubmit();
        return;
      }

      state.midRule = MID_RE.test(v);
      if (!state.midRule) {
        setMsg(msgEl, '아이디 규칙: 영문+숫자만, 6~20자', false);
        paintByOk(input, false, true);
        updateSubmit();
        return;
      }

      setMsg(msgEl, '중복 확인 중...', null);
      paintByOk(input, false, false);

      try {
        const data = await getJson(`/api/auth/check-mid?mid=${encodeURIComponent(v)}`);
        state.midAvailable = !!data.available;
        if (state.midAvailable) {
          setMsg(msgEl, '아이디 조건 OK · 사용 가능', true);
          paintByOk(input, true, false);
        } else {
          setMsg(msgEl, '이미 사용 중인 아이디입니다.', false);
          paintByOk(input, false, true);
        }
      } catch (_) {
        setMsg(msgEl, '중복 확인에 실패했습니다. 잠시 후 다시 시도해 주세요.', false);
        paintByOk(input, false, true);
      }

      updateSubmit();
    }, 1000);

    input.addEventListener('input', () => {
      if (isComposing) return; // 조합 중이면 아무것도 하지 않음
      state.midAvailable = false;
      run();
      updateSubmit();
    });
  }

  function bindNickAutoCheck() {
    const input = qs('#nickname');
    const msgEl = qs('#nickname_check_msg');
    if (!input) return;

    const run = debounce(async () => {
      const v = input.value.trim();
      state.nickAvailable = false;

      if (v.length === 0) {
        state.nickRule = false;
        setMsg(msgEl, '', null);
        paintByOk(input, false, false);
        updateSubmit();
        return;
      }

      state.nickRule = NICK_RE.test(v);
      if (!state.nickRule) {
        setMsg(msgEl, '닉네임 규칙: 한글/영문/숫자, 3~12자', false);
        paintByOk(input, false, true);
        updateSubmit();
        return;
      }

      setMsg(msgEl, '중복 확인 중...', null);
      paintByOk(input, false, false);

      try {
        const data = await getJson(`/api/auth/check-nickname?nickname=${encodeURIComponent(v)}`);
        state.nickAvailable = !!data.available;

        if (state.nickAvailable) {
          setMsg(msgEl, '닉네임 조건 OK · 사용 가능', true);
          paintByOk(input, true, false);
        } else {
          setMsg(msgEl, '이미 사용 중인 닉네임입니다.', false);
          paintByOk(input, false, true);
        }
      } catch (_) {
        setMsg(msgEl, '중복 확인에 실패했습니다. 잠시 후 다시 시도해 주세요.', false);
        paintByOk(input, false, true);
      }

      updateSubmit();
    }, 1000);

    input.addEventListener('input', () => {
      state.nickAvailable = false;
      run();
      updateSubmit();
    });
  }

  function bindAddressStub() {
    const btn = qs('#btnAddressSearch');
    if (!btn) return;
    btn.addEventListener('click', () => {
      new daum.Postcode({
        oncomplete: function(data) {
          // 도로명 주소 우선, 없으면 지번
          const addr = data.roadAddress || data.jibunAddress;
          const addrMain = qs('#address_main');
          const addrDetail = qs('#address_detail');
          if (addrMain) addrMain.value = addr;
          if (addrDetail) { addrDetail.value = ''; addrDetail.focus(); }
          updateSubmit();
        }
      }).open();
    });
  }
  
  function bindPhone() {
    const fields = [
      qs('#phone1'),
      qs('#phone2'),
      qs('#phone3')
    ];
    const hidden = qs('#phone'); // 서버로 전송되는 hidden 필드

    const phone1 = qs('#phone1');
    if (hidden && hidden.value.trim()) {
      const digits = hidden.value.replace(/\D/g, '');
      if (digits.length >= 10) {
        if (qs('#phone1')) qs('#phone1').value = digits.slice(0, 3);
        if (qs('#phone2')) qs('#phone2').value = digits.length === 10 ? digits.slice(3, 6) : digits.slice(3, 7);
        if (qs('#phone3')) qs('#phone3').value = digits.length === 10 ? digits.slice(6, 10) : digits.slice(7, 11);
      }
    } else if (phone1 && phone1.value === '') {
      phone1.value = '010';
    }

    syncHidden();

    fields.forEach(input => {
      if (!input) return;

      // 숫자 외 키 입력 자체를 차단
      input.addEventListener('keydown', (e) => {
        const allowed = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
        const isDigit = (e.key >= '0' && e.key <= '9') || (e.code >= 'Numpad0' && e.code <= 'Numpad9');
        if (!isDigit && !allowed.includes(e.key)) {
          e.preventDefault();
        }
      });

      // 붙여넣기/자동완성 등 우회 케이스 대비 — 숫자만 남기기
      input.addEventListener('input', () => {
        input.value = input.value.replace(/\D/g, '');
        syncHidden();
      });
    });

    // hidden 필드에 010-1234-5678 형태로 합쳐서 저장
    function syncHidden() {
      if (!hidden) return;
      const v1 = qs('#phone1') ? qs('#phone1').value : '';
      const v2 = qs('#phone2') ? qs('#phone2').value : '';
      const v3 = qs('#phone3') ? qs('#phone3').value : '';
      hidden.value = [v1, v2, v3].filter(Boolean).join('-');
    }
  }

  function bindJumin() {
    const j1 = qs('#jumin1');
    const j2 = qs('#jumin2');
    const hidden = qs('#jumin');
    const msgEl = qs('#jumin_msg');
    if (!j1 || !j2 || !hidden) return;

    const digitsOnly = (input) => {
      input.value = input.value.replace(/\D/g, '');
    };

    const sync = () => {
      digitsOnly(j1);
      digitsOnly(j2);
      hidden.value = `${j1.value}-${j2.value}`;

      const ok = (j1.value.length === 6 && j2.value.length === 7);
      state.juminOk = ok;
      setMsg(msgEl, (j1.value.length === 0 && j2.value.length === 0) ? '' : (ok ? '주민번호 형식 확인 완료' : '주민번호 13자리를 정확히 입력해주세요.'), (j1.value.length === 0 && j2.value.length === 0) ? null : ok);
      paintByOk(j1, ok, !ok && (j1.value.length > 0 || j2.value.length > 0));
      paintByOk(j2, ok, !ok && (j1.value.length > 0 || j2.value.length > 0));
      updateSubmit();
    };

    [j1, j2].forEach((input) => {
      input.addEventListener('keydown', (e) => {
        const allowed = ['Backspace', 'Delete', 'ArrowLeft', 'ArrowRight', 'Tab', 'Home', 'End'];
        const isDigit = (e.key >= '0' && e.key <= '9') || (e.code >= 'Numpad0' && e.code <= 'Numpad9');
        if (!isDigit && !allowed.includes(e.key)) {
          e.preventDefault();
        }
      });
      input.addEventListener('input', sync);
    });

    sync();
  }
  
  function bindEmailVerify() {
    const sendBtn    = qs('#btnSendCode');
    const verifyBtn  = qs('#btnVerifyCode');
    const codeArea   = qs('#codeArea');
    const msgEl      = qs('#email_verify_msg');
    const emailInput = qs('#email');
    if (!sendBtn || !emailInput) return;

    function resetEmailVerificationUi() {
      state.emailOk = false;
      setMsg(msgEl, '', null);
      if (codeArea) codeArea.style.display = 'none';
      if (verifyBtn) verifyBtn.disabled = false;
      if (sendBtn) sendBtn.disabled = false;
      if (sendBtn) sendBtn.textContent = '인증번호 전송';
      const codeInput = qs('#emailCode');
      if (codeInput) {
        codeInput.disabled = false;
        codeInput.value = '';
      }
      updateSubmit();
    }

    emailInput.addEventListener('input', resetEmailVerificationUi);

    sendBtn.addEventListener('click', async () => {
      const email = emailInput ? emailInput.value.trim() : '';
      if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        setMsg(msgEl, '올바른 이메일을 입력해주세요.', false);
        return;
      }
      sendBtn.disabled = true;
      sendBtn.textContent = '전송 중...';
      try {
        const res = await fetch('/api/auth/send-email-code', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: 'email=' + encodeURIComponent(email)
        });
        const data = await res.json();
        if (data.sent) {
          if (codeArea) codeArea.style.display = '';
          setMsg(msgEl, 'CMD 콘솔에서 인증번호를 확인하세요.', null);
          sendBtn.textContent = '재전송';
        } else {
          setMsg(msgEl, data.message || '전송에 실패했습니다.', false);
          sendBtn.textContent = '인증번호 전송';
        }
      } catch (_) {
        setMsg(msgEl, '전송에 실패했습니다.', false);
        sendBtn.textContent = '인증번호 전송';
      } finally {
        sendBtn.disabled = false; // 성공/실패 무관하게 항상 버튼 활성화
      }
    });

	if (verifyBtn) {
	  verifyBtn.addEventListener('click', async () => {
	    const email = emailInput ? emailInput.value.trim() : '';
	    const code  = qs('#emailCode') ? qs('#emailCode').value.trim() : '';
	    if (!code) { setMsg(msgEl, '인증번호를 입력해주세요.', false); return; }
	    try {
	      const res = await fetch('/api/auth/verify-email-code', {
	        method: 'POST',
	        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
	        body: 'email=' + encodeURIComponent(email) + '&code=' + encodeURIComponent(code)
	      });
	      const data = await res.json();
	      if (data.verified) {
	        state.emailOk = true;
	        setMsg(msgEl, '이메일 인증 완료!', true);
	        verifyBtn.disabled = true;
	        sendBtn.disabled = true;
	        if (qs('#emailCode')) qs('#emailCode').disabled = true;
	      } else {
	        // 서버에서 내려온 메시지 있으면 그걸 표시, 없으면 기본 메시지
	        setMsg(msgEl, data.message || '인증번호가 올바르지 않습니다.', false);
	      }
	      updateSubmit();
	    } catch (_) {
	      setMsg(msgEl, '확인에 실패했습니다.', false);
	    }
	  });
	}
  }

  function bindGenericRequiredWatch() {
    const ids = ['#real_name', '#email', '#address_main'];
    ids.forEach(sel => {
      const el = qs(sel);
      if (!el) return;
      el.addEventListener('input', () => updateSubmit());
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    bindToggles();
    bindPasswordRules();
    bindMidAutoCheck();
    bindNickAutoCheck();
    bindAddressStub();
	bindPhone();
    bindJumin();
	bindEmailVerify();
    bindGenericRequiredWatch();
    updateSubmit();
  });
})();