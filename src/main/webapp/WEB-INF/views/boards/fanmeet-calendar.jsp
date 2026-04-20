<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>UNIT-X - ${boardTitle} 캘린더</title>
    <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
    <style>
        .cal-nav { display:flex; flex-wrap:wrap; align-items:center; gap:12px; margin-bottom:20px; }
        .cal-grid { display:grid; grid-template-columns: repeat(7, 1fr); gap:6px; }
        .cal-head { font-size:11px; font-weight:800; text-align:center; color:rgba(71,85,105,.75); padding:8px 4px; font-family:"Orbitron",sans-serif; letter-spacing:.08em; }
        .cal-cell { min-height:88px; border-radius:14px; border:1px solid rgba(226,232,240,.95); background:rgba(255,255,255,.65); padding:6px; font-size:12px; }
        .cal-cell--blank { background:transparent; border-color:transparent; }
        .cal-daynum { font-weight:800; color:rgba(15,23,42,.85); margin-bottom:4px; }
        .cal-ev { display:block; font-size:10px; line-height:1.35; color:rgba(190,24,93,.88); text-decoration:none; margin-top:3px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
        .cal-ev:hover { text-decoration:underline; }
    </style>
</head>
<body class="page-main min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
    <div class="container mx-auto max-w-5xl">
        <section class="glass-card p-8 md:p-10">
            <div class="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
                <div>
                    <h1 class="font-orbitron text-2xl md:text-4xl font-black text-slate-900">${boardTitle}</h1>
                    <p class="text-slate-600 mt-1">월별 일정</p>
                </div>
                <a class="nav-link shrink-0" href="${ctx}/boards/map"><i class="fa-solid fa-list"></i> 목록·지도</a>
            </div>

            <div class="cal-nav mt-8">
                <c:set var="prevY" value="${calendarMonth == 1 ? calendarYear - 1 : calendarYear}" />
                <c:set var="prevM" value="${calendarMonth == 1 ? 12 : calendarMonth - 1}" />
                <c:set var="nextY" value="${calendarMonth == 12 ? calendarYear + 1 : calendarYear}" />
                <c:set var="nextM" value="${calendarMonth == 12 ? 1 : calendarMonth + 1}" />
                <a class="btn-primary px-4 py-2 text-sm" href="${ctx}/boards/map/calendar?year=${prevY}&month=${prevM}">&lt; 이전 달</a>
                <span class="text-lg font-bold text-slate-800">${calendarYear}년 ${calendarMonth}월</span>
                <a class="btn-primary px-4 py-2 text-sm" href="${ctx}/boards/map/calendar?year=${nextY}&month=${nextM}">다음 달 &gt;</a>
                <a class="nav-link text-sm" href="${ctx}/boards/map/calendar">오늘 달로</a>
            </div>

            <div class="cal-grid">
                <div class="cal-head">MON</div>
                <div class="cal-head">TUE</div>
                <div class="cal-head">WED</div>
                <div class="cal-head">THU</div>
                <div class="cal-head">FRI</div>
                <div class="cal-head">SAT</div>
                <div class="cal-head">SUN</div>

                <c:forEach begin="1" end="${calendarLeadingBlanks}" var="x">
                    <div class="cal-cell cal-cell--blank"></div>
                </c:forEach>

                <c:forEach var="dayKey" items="${calendarDayKeys}">
                    <c:set var="dayNum" value="${fn:substring(dayKey, 8, 10)}" />
                    <c:set var="evs" value="${fanMeetByDay[dayKey]}" />
                    <div class="cal-cell">
                        <div class="cal-daynum">${dayNum}</div>
                        <c:if test="${not empty evs}">
                            <c:forEach var="ev" items="${evs}">
                                <a class="cal-ev" href="${ctx}/boards/map/${ev.id}">
                                    <c:out value="${ev.title}" />
                                </a>
                            </c:forEach>
                        </c:if>
                    </div>
                </c:forEach>
            </div>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
</body>
</html>
