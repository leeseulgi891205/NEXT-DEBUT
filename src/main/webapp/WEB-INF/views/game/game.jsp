<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="gpDay" value="${empty dayNum ? 5 : dayNum}" />
<c:set var="gpSlot" value="${empty timeLabel ? '저녁' : timeLabel}" />
<c:set var="gpWeekDay" value="${empty weekDayName ? '목요일' : weekDayName}" />
<c:set var="charName" value="${empty focusName ? '민서' : focusName}" />
<c:set var="charImg" value="${empty focusImage ? '' : focusImage}" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<meta name="theme-color" content="#fff5f7"/>
<title>UNITX — PLAY</title>
<%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
<link rel="stylesheet" href="${ctx}/css/pages/game-play.css" />
</head>
<body class="page-main page-game-play min-h-screen flex flex-col">

<div class="topnav-shell">
  <%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>
</div>

<main class="game-play flex-1" id="game-play-root" role="main">
  <div class="game-play__bg" aria-hidden="true"></div>

  <div class="game-play__inner">
    <%-- 좌측: 능력치 · DAY/턴 · 피로도 --%>
    <aside class="game-play__col game-play__col--left" aria-label="능력치와 상태">
      <div class="gp-panel">
        <div class="gp-panel__head">
          <span class="gp-panel__tag">STATUS</span>
          <span class="gp-panel__title">능력치</span>
        </div>
        <div class="gp-meta">
          <span class="gp-chip gp-chip--accent"><i class="fas fa-calendar-day" aria-hidden="true"></i> DAY <strong id="gp-day-num">${gpDay}</strong></span>
          <span class="gp-chip"><c:out value="${gpWeekDay}"/> · <c:out value="${gpSlot}"/></span>
        </div>
        <p class="gp-turn-note">TURN · 스케줄 선택</p>
        <div class="gp-fatigue">
          <div class="gp-fatigue__lbl">
            <span>피로도</span>
            <strong><span id="fatigue-val">28</span>%</strong>
          </div>
          <div class="gp-fatigue__track"><span class="gp-fatigue__fill" id="fatigue-bar" style="width:28%"></span></div>
        </div>

        <div class="gp-stat" data-stat="vocal">
          <span class="gp-stat__name">VOCAL</span>
          <div class="gp-stat__track"><span class="gp-stat__fill" id="bar-vocal" style="width:58%"></span></div>
          <span class="gp-stat__val" id="val-vocal">58</span>
        </div>
        <div class="gp-stat" data-stat="dance">
          <span class="gp-stat__name">DANCE</span>
          <div class="gp-stat__track"><span class="gp-stat__fill" id="bar-dance" style="width:52%"></span></div>
          <span class="gp-stat__val" id="val-dance">52</span>
        </div>
        <div class="gp-stat" data-stat="star">
          <span class="gp-stat__name">STAR</span>
          <div class="gp-stat__track"><span class="gp-stat__fill" id="bar-star" style="width:61%"></span></div>
          <span class="gp-stat__val" id="val-star">61</span>
        </div>
        <div class="gp-stat" data-stat="mental">
          <span class="gp-stat__name">MENTAL</span>
          <div class="gp-stat__track"><span class="gp-stat__fill" id="bar-mental" style="width:47%"></span></div>
          <span class="gp-stat__val" id="val-mental">47</span>
        </div>
        <div class="gp-stat" data-stat="teamwork">
          <span class="gp-stat__name">TEAM</span>
          <div class="gp-stat__track"><span class="gp-stat__fill" id="bar-teamwork" style="width:55%"></span></div>
          <span class="gp-stat__val" id="val-teamwork">55</span>
        </div>
      </div>
    </aside>

    <%-- 중앙: 스토리 · 선택지 · 결과 --%>
    <section class="game-play__col game-play__col--center" aria-labelledby="gp-story-heading">
      <div class="gp-panel gp-story">
        <div class="gp-panel__head">
          <span class="gp-panel__tag">SCENE</span>
          <span class="gp-panel__title" id="gp-story-heading">스토리</span>
        </div>
        <div class="gp-story__card">
          <p class="gp-story__eyebrow">UNITX · TRAINEE MODE</p>
          <p id="story-text" class="gp-story__text">
            <c:choose>
              <c:when test="${not empty sceneText}"><c:out value="${sceneText}"/></c:when>
              <c:otherwise>새벽 연습실. 매니저가 오늘 저녁 스케줄 카드를 건넨다. 한 가지만 고를 수 있다. 무엇에 에너지를 쓸까?</c:otherwise>
            </c:choose>
          </p>
        </div>

        <div class="gp-panel__head gp-panel__head--flush">
          <span class="gp-panel__tag">CHOICE</span>
          <span class="gp-panel__title">선택</span>
        </div>
        <div class="gp-choices" id="gp-choices" role="group" aria-label="선택지">
          <button type="button" class="gp-choice" data-i="0">
            <span class="gp-choice__key" aria-hidden="true">A</span>
            보컬 부스에서 발성과 호흡만 파고든다
          </button>
          <button type="button" class="gp-choice" data-i="1">
            <span class="gp-choice__key" aria-hidden="true">B</span>
            안무 영상을 돌려보며 댄스 파트를 반복한다
          </button>
          <button type="button" class="gp-choice" data-i="2">
            <span class="gp-choice__key" aria-hidden="true">C</span>
            팀원과 짧게 미팅하고 케미를 맞춘다
          </button>
        </div>

        <div id="gp-result" class="gp-result" role="status" aria-live="polite">
          <div class="gp-result__ttl">RESULT</div>
          <p id="gp-result-body" class="gp-result__body"></p>
          <div id="gp-result-fx" class="gp-result__fx"></div>
        </div>
      </div>
    </section>

    <%-- 우측: 캐릭터 --%>
    <aside class="game-play__col game-play__col--right" aria-label="포커스 연습생">
      <div class="gp-panel gp-char">
        <div class="gp-panel__head">
          <span class="gp-panel__tag">FOCUS</span>
          <span class="gp-panel__title">프로필</span>
        </div>
        <div class="gp-char__frame">
          <div class="ipad-image-frame">
            <span class="ipad-image-frame__camera" aria-hidden="true"></span>
            <div class="ipad-image-frame__screen">
              <c:choose>
                <c:when test="${not empty charImg}">
                  <img src="${ctx}${charImg}" alt="" id="gp-char-img" width="400" height="520" loading="lazy" decoding="async"
                       onerror="this.style.display='none';document.getElementById('gp-char-ph').style.display='flex';"/>
                  <div id="gp-char-ph" class="gp-char__placeholder" style="display:none" aria-hidden="true"><i class="fas fa-user"></i></div>
                </c:when>
                <c:otherwise>
                  <img src="${ctx}/images/1111.jpg" alt="" id="gp-char-img" width="400" height="520" loading="lazy" decoding="async"
                       onerror="this.onerror=null;this.src='${ctx}/images/bg_game.jpg';"/>
                </c:otherwise>
              </c:choose>
            </div>
            <span class="ipad-image-frame__homebar" aria-hidden="true"></span>
          </div>
        </div>
        <p class="gp-char__name"><c:out value="${charName}"/></p>
        <div class="gp-char__tags">
          <span class="gp-chip" id="tag-condition">컨디션 · 보통</span>
          <span class="gp-chip" id="tag-emotion">감정 · 설렘</span>
        </div>
        <div class="gp-mood">
          현재 상태
          <strong id="char-status-line">컨디션 유지 중 · 다음 선택이 성장을 가른다</strong>
        </div>
      </div>
    </aside>
  </div>
</main>

<nav class="game-play__dock" aria-label="빠른 이동">
  <a href="${ctx}/main"><i class="fas fa-house" aria-hidden="true"></i> LOBBY</a>
  <a href="${ctx}/game/run/ranking"><i class="fas fa-ranking-star" aria-hidden="true"></i> RANK</a>
</nav>

<script>
(function () {
  var CHOICES = [
    {
      result: '보컬에 몰입했다. 목소리 안정감이 조금 올라갔다. 긴장이 조금 쌓인다.',
      deltas: { vocal: 4, dance: 0, star: 1, mental: -1, teamwork: 0 },
      fatigue: 9,
      condition: '목 케어 필요',
      emotion: '집중',
      chips: [
        { t: 'VOCAL +4', cls: 'gp-result__chip--up' },
        { t: 'MENTAL -1', cls: 'gp-result__chip--down' },
        { t: 'FATIGUE +9%', cls: 'gp-result__chip--down' }
      ]
    },
    {
      result: '댄스 루틴을 반복했다. 몸이 뜨겁고, 스타성이 살짝 반짝였다.',
      deltas: { vocal: 0, dance: 5, star: 2, mental: 0, teamwork: 0 },
      fatigue: 12,
      condition: '근육 피로',
      emotion: '도전',
      chips: [
        { t: 'DANCE +5', cls: 'gp-result__chip--up' },
        { t: 'STAR +2', cls: 'gp-result__chip--up' },
        { t: 'FATIGUE +12%', cls: 'gp-result__chip--down' }
      ]
    },
    {
      result: '팀과 호흡을 맞췄다. 시너지가 올라가고 멘탈이 안정됐다.',
      deltas: { vocal: 0, dance: 0, star: 0, mental: 3, teamwork: 5 },
      fatigue: 4,
      condition: '밸런스형',
      emotion: '여유',
      chips: [
        { t: 'TEAM +5', cls: 'gp-result__chip--up' },
        { t: 'MENTAL +3', cls: 'gp-result__chip--up' },
        { t: 'FATIGUE +4%', cls: 'gp-result__chip--down' }
      ]
    }
  ];

  var keys = ['vocal', 'dance', 'star', 'mental', 'teamwork'];
  var vals = { vocal: 58, dance: 52, star: 61, mental: 47, teamwork: 55 };
  var fatigue = 28;

  function clamp(n, lo, hi) {
    return Math.max(lo, Math.min(hi, n));
  }

  function setStat(key, v) {
    vals[key] = clamp(v, 0, 100);
    var el = document.getElementById('val-' + key);
    var bar = document.getElementById('bar-' + key);
    var row = el && el.closest('.gp-stat');
    if (el) el.textContent = String(vals[key]);
    if (bar) bar.style.width = vals[key] + '%';
    if (row) {
      row.classList.remove('is-flash');
      void row.offsetWidth;
      row.classList.add('is-flash');
    }
  }

  function showDelta(row, d) {
    if (!row || !d) return;
    var old = row.querySelector('.gp-delta');
    if (old) old.remove();
    var span = document.createElement('span');
    span.className = 'gp-delta ' + (d > 0 ? 'gp-delta--up' : 'gp-delta--down');
    span.textContent = (d > 0 ? '+' : '') + d;
    row.style.position = 'relative';
    row.appendChild(span);
    setTimeout(function () {
      if (span.parentNode) span.remove();
    }, 1100);
  }

  function applyDeltas(deltas) {
    keys.forEach(function (k) {
      var d = deltas[k] || 0;
      if (!d) return;
      var row = document.querySelector('.gp-stat[data-stat="' + k + '"]');
      showDelta(row, d);
      setStat(k, vals[k] + d);
    });
  }

  function setFatigue(delta) {
    fatigue = clamp(fatigue + delta, 0, 100);
    document.getElementById('fatigue-val').textContent = String(fatigue);
    var bar = document.getElementById('fatigue-bar');
    if (bar) bar.style.width = fatigue + '%';
  }

  function renderResult(idx) {
    var c = CHOICES[idx];
    if (!c) return;
    var box = document.getElementById('gp-result');
    var body = document.getElementById('gp-result-body');
    var fx = document.getElementById('gp-result-fx');
    body.textContent = c.result;
    fx.innerHTML = '';
    (c.chips || []).forEach(function (ch) {
      var s = document.createElement('span');
      s.className = 'gp-result__chip ' + (ch.cls || '');
      s.textContent = ch.t;
      fx.appendChild(s);
    });
    document.getElementById('tag-condition').textContent = '컨디션 · ' + c.condition;
    document.getElementById('tag-emotion').textContent = '감정 · ' + c.emotion;
    document.getElementById('char-status-line').textContent =
      c.condition + ' · ' + c.emotion + ' 상태로 다음 턴을 맞이한다.';
    box.classList.add('is-visible');
  }

  document.querySelectorAll('.gp-choice').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var idx = parseInt(btn.getAttribute('data-i'), 10);
      var c = CHOICES[idx];
      if (!c) return;

      document.querySelectorAll('.gp-choice').forEach(function (b) {
        b.classList.add('is-disabled');
        b.classList.remove('is-selected');
      });
      btn.classList.remove('is-disabled');
      btn.classList.add('is-selected');

      applyDeltas(c.deltas);
      setFatigue(c.fatigue);
      renderResult(idx);
    });
  });
})();
</script>
</body>
</html>
