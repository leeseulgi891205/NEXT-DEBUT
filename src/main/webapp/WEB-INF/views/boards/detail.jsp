<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isAuthor" value="${not empty loginMember and loginMember.nickname eq post.authorNick}" />
<c:set var="isAdmin" value="${not empty loginMember and loginMember.role eq 'ADMIN'}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>NEXT DEBUT - ${boardTitle}</title>
    <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
    <style>
        .meta-row { display:flex; align-items:center; gap:10px; flex-wrap:wrap; font-size:13px; color:rgba(51,65,85,0.75); margin-top:8px; }
        .meta-badge { display:inline-flex; align-items:center; gap:5px; padding:3px 10px; border-radius:999px; background:rgba(15,23,42,0.03); border:1px solid rgba(148,163,184,0.35); font-size:12px; }
        .meta-badge.views { color:rgba(186,198,220,0.85); }
        .meta-badge.likes { color:rgba(233,176,196,0.85); cursor:pointer; transition:all 200ms ease; user-select:none; }
        .meta-badge.likes:hover { background:rgba(233,176,196,0.12); border-color:rgba(233,176,196,0.45); }
        .meta-badge.likes.liked { background:rgba(233,176,196,0.18); border-color:rgba(233,176,196,0.50); color:rgba(233,176,196,1); }
        .meta-badge.likes.liked i { animation: heartPop 300ms cubic-bezier(.23,1.5,.46,.98); }
        @keyframes heartPop { 0%{transform:scale(1)} 50%{transform:scale(1.4)} 100%{transform:scale(1)} }

        .author-actions { display:flex; gap:8px; }
        .btn-edit, .btn-del {
            font-size:12px; padding:5px 14px; border-radius:999px;
            border:1px solid rgba(148,163,184,0.40); background:rgba(255,255,255,0.65);
            color:rgba(30,41,59,0.85); cursor:pointer; transition:all 200ms ease; text-decoration:none;
            display:inline-flex; align-items:center; gap:5px;
        }
        .btn-edit:hover { background:#fff; color:#0f172a; }
        .btn-del  { border-color:rgba(248,113,113,0.30); color:rgba(248,113,113,0.75); }
        .btn-del:hover { background:rgba(248,113,113,0.15); color:rgba(248,113,113,1); }
        .btn-report-post { font-size:12px; padding:5px 14px; border-radius:999px; border:1px solid rgba(251,191,36,0.30);
            background:rgba(255,255,255,0.65); color:rgba(180,130,0,0.75); cursor:pointer; transition:all 200ms ease;
            display:inline-flex; align-items:center; gap:5px; }
        .btn-report-post:hover { background:rgba(251,191,36,0.10); color:rgba(180,130,0,1); }
        .badge-secret-view { display:inline-flex; align-items:center; gap:4px; font-size:11px; padding:3px 10px; border-radius:999px;
            background:rgba(100,116,139,0.08); border:1px solid rgba(148,163,184,0.30); color:rgba(100,116,139,0.80); }
        .badge-cat { display:inline-flex; align-items:center; font-size:11px; padding:3px 10px; border-radius:999px; font-weight:600; }
        .badge-cat.bug { background:rgba(251,191,36,0.15); color:rgba(180,130,0,0.90); border:1px solid rgba(251,191,36,0.30); }
        .badge-cat.report { background:rgba(248,113,113,0.12); color:rgba(185,28,28,0.80); border:1px solid rgba(248,113,113,0.25); }

        .post-body-card { margin-top:24px; background:rgba(255,255,255,0.74); border:1px solid rgba(226,232,240,0.95); border-radius:24px; padding:24px; }
        .post-body-text { white-space:pre-wrap; color:rgba(30,41,59,0.92); line-height:1.8; font-size:15px; min-height:160px; }
        .post-attachment-box { margin-top:20px; padding-top:20px; border-top:1px solid rgba(226,232,240,0.95); }
        .attachment-label { font-size:11px; letter-spacing:0.24em; color:rgba(100,116,139,0.85); margin-bottom:12px; font-family:"Orbitron",sans-serif; }
        .attachment-file { display:flex; align-items:center; justify-content:space-between; gap:12px; padding:14px 16px; border-radius:18px; background:rgba(255,255,255,0.9); border:1px solid rgba(226,232,240,0.95); }
        .attachment-file__name { color:rgba(51,65,85,0.92); display:flex; align-items:center; gap:8px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
        .attachment-image { display:block; width:100%; max-height:640px; object-fit:contain; border-radius:20px; border:1px solid rgba(226,232,240,0.95); background:#fff; }

        .post-place-box { margin-top:24px; background:rgba(255,255,255,0.74); border:1px solid rgba(226,232,240,0.95); border-radius:24px; padding:24px; }
        .post-place-name { font-size:15px; font-weight:600; color:rgba(30,41,59,0.92); margin-bottom:14px; display:flex; align-items:center; gap:8px; }
        .post-place-map { width:100%; height:320px; border-radius:20px; border:1px solid rgba(226,232,240,0.95); overflow:hidden; background:rgba(241,245,249,0.9); }

        .comment-section { margin-top:32px; }
        .comment-section__title {
            font-family:"Orbitron",sans-serif; font-size:11px; letter-spacing:0.28em;
            color:rgba(51,65,85,0.70); margin-bottom:16px;
        }
        .comment-input-wrap { display:flex; gap:10px; margin-bottom:20px; }
        .comment-input-wrap textarea {
            flex:1; padding:12px 16px; border-radius:16px; resize:none; min-height:56px;
            background:rgba(255,255,255,0.90); border:1px solid rgba(148,163,184,0.35);
            color:#0f172a; font-size:14px; outline:none; transition:border 200ms ease; font-family:inherit;
        }
        .comment-input-wrap textarea:focus { border-color:rgba(233,176,196,0.50); }
        .comment-input-wrap textarea::placeholder { color:rgba(100,116,139,0.75); }
        .btn-comment-submit {
            padding:0 20px; border-radius:16px; border:none; cursor:pointer;
            background:linear-gradient(135deg,rgba(233,176,196,0.80),rgba(204,186,216,0.80));
            color:rgba(20,10,30,0.90); font-weight:700; font-size:13px;
            transition:all 200ms ease; white-space:nowrap; align-self:flex-end; height:44px;
        }
        .btn-comment-submit:hover { filter:brightness(1.1); transform:translateY(-2px); }

        .comment-item {
            padding:14px 16px; border-radius:16px; margin-bottom:10px;
            background:rgba(15,23,42,0.03); border:1px solid rgba(148,163,184,0.28);
            transition:background 200ms ease;
        }
        .comment-item:hover { background:rgba(15,23,42,0.05); }
        .comment-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:6px; }
        .comment-nick { font-size:13px; font-weight:700; color:rgba(233,176,196,0.90); }
        .comment-date { font-size:11px; color:rgba(100,116,139,0.85); }
        .comment-content { font-size:14px; color:rgba(30,41,59,0.90); line-height:1.6; white-space:pre-wrap; }
        .comment-edit-wrap { display:none; margin-top:10px; }
        .comment-edit-wrap.is-open { display:block; }
        .comment-edit-wrap textarea {
            width:100%; min-height:88px; resize:vertical; padding:12px 14px; border-radius:14px;
            border:1px solid rgba(148,163,184,0.35); background:rgba(255,255,255,0.92); color:#0f172a; outline:none;
            font-size:14px; line-height:1.6; font-family:inherit;
        }
        .comment-edit-wrap textarea:focus { border-color:rgba(233,176,196,0.55); }
        .comment-actions { display:flex; align-items:center; gap:8px; }
        .btn-comment-edit, .btn-comment-del, .btn-comment-cancel {
            font-size:11px; padding:3px 10px; border-radius:999px; background:transparent; cursor:pointer;
            transition:all 180ms ease; border:1px solid rgba(148,163,184,0.25);
        }
        .btn-comment-edit, .btn-comment-cancel { color:rgba(71,85,105,0.85); }
        .btn-comment-edit:hover, .btn-comment-cancel:hover { background:rgba(148,163,184,0.12); color:rgba(15,23,42,1); }
        .btn-comment-del { border-color:rgba(248,113,113,0.25); color:rgba(248,113,113,0.60); }
        .btn-comment-del:hover { background:rgba(248,113,113,0.12); color:rgba(248,113,113,1); }
        .btn-comment-report { font-size:11px; padding:3px 10px; border-radius:999px; background:transparent; cursor:pointer;
            transition:all 180ms ease; border:1px solid rgba(251,191,36,0.25); color:rgba(180,130,0,0.60); }
        .btn-comment-report:hover { background:rgba(251,191,36,0.10); color:rgba(180,130,0,1); }

        .comment-edit-actions { display:flex; justify-content:flex-end; gap:8px; margin-top:10px; }
        .btn-comment-save {
            font-size:11px; padding:6px 12px; border-radius:999px; border:none; cursor:pointer;
            background:linear-gradient(135deg,rgba(233,176,196,0.8),rgba(204,186,216,0.8)); color:rgba(15,23,42,0.95); font-weight:700;
        }

        .no-comments { text-align:center; padding:28px 0; color:rgba(100,116,139,0.85); font-size:13px; }
        .flash-msg { padding:12px 16px; border-radius:14px; margin-bottom:16px; font-size:13px; }
        .flash-msg.ok  { background:rgba(134,239,172,0.12); border:1px solid rgba(134,239,172,0.30); color:rgba(22,101,52,0.95); }
        .flash-msg.err { background:rgba(248,113,113,0.12); border:1px solid rgba(248,113,113,0.30); color:rgba(185,28,28,0.95); }
        .like-spinner { display:none; width:12px; height:12px; border:2px solid rgba(233,176,196,0.3); border-top-color:rgba(233,176,196,1); border-radius:50%; animation:spin 600ms linear infinite; }
        @keyframes spin { to { transform:rotate(360deg); } }
        .report-modal-backdrop { display:none; position:fixed; inset:0; background:rgba(0,0,0,0.45); z-index:1000; align-items:center; justify-content:center; }
        .report-modal-backdrop.is-open { display:flex; }
        .report-modal { background:#fff; border-radius:24px; padding:28px 28px 24px; width:90%; max-width:420px;
            box-shadow:0 20px 60px rgba(0,0,0,0.18); animation:modalIn 240ms cubic-bezier(.23,1.5,.46,.98); }
        @keyframes modalIn { from{transform:scale(0.92);opacity:0} to{transform:scale(1);opacity:1} }
        .report-modal__title { font-family:"Orbitron",sans-serif; font-size:13px; letter-spacing:0.2em; color:#0f172a; margin-bottom:18px; }
        .report-modal select, .report-modal textarea { width:100%; padding:10px 14px; border-radius:12px; border:1px solid rgba(148,163,184,0.35);
            font-size:13px; outline:none; color:#0f172a; margin-bottom:12px; transition:border 200ms ease; font-family:inherit; }
        .report-modal select:focus, .report-modal textarea:focus { border-color:rgba(233,176,196,0.55); }
        .report-modal__actions { display:flex; justify-content:flex-end; gap:8px; margin-top:6px; }
        .btn-modal-cancel { font-size:12px; padding:8px 16px; border-radius:999px; border:1px solid rgba(148,163,184,0.35);
            background:transparent; color:rgba(71,85,105,0.85); cursor:pointer; transition:all 180ms ease; }
        .btn-modal-cancel:hover { background:rgba(148,163,184,0.12); }
        .btn-modal-submit { font-size:12px; padding:8px 18px; border-radius:999px; border:none;
            background:linear-gradient(135deg,rgba(248,113,113,0.80),rgba(239,68,68,0.80)); color:#fff; font-weight:700; cursor:pointer; transition:all 180ms ease; }
        .btn-modal-submit:hover { filter:brightness(1.1); }
        .casting-event-panel {
            margin-top: 20px; padding: 20px 22px; border-radius: 22px;
            background: linear-gradient(135deg, rgba(233,176,196,0.14), rgba(196,181,253,0.12));
            border: 1px solid rgba(233,176,196,0.35);
        }
        .casting-event-panel__k { font-family: "Orbitron", sans-serif; font-size: 10px; letter-spacing: 0.22em; color: rgba(71,85,105,0.85); margin-bottom: 10px; }
        .casting-event-status { display:inline-flex; align-items:center; padding:4px 12px; border-radius:999px; font-size:12px; font-weight:800; }
        .casting-event-status--live { background: rgba(16,185,129,0.15); color: rgb(6,95,70); }
        .casting-event-status--soon { background: rgba(59,130,246,0.12); color: rgb(30,64,175); }
        .casting-event-status--end { background: rgba(148,163,184,0.15); color: rgb(71,85,105); }
        .btn-casting-join {
            display:inline-flex; align-items:center; gap:8px; margin-top:14px; padding:12px 22px; border-radius:999px; font-weight:800; font-size:14px;
            border: none; cursor: pointer; text-decoration:none;
            background: linear-gradient(135deg, rgba(99,102,241,0.95), rgba(139,92,246,0.88));
            color: #fff; box-shadow: 0 10px 28px rgba(99,102,241,0.25);
            transition: transform .15s ease, filter .15s ease;
        }
        .btn-casting-join:hover { filter: brightness(1.06); transform: translateY(-2px); }
        .fanmeet-host-box { margin-top:18px; border:1px solid rgba(167,139,250,0.28); background:rgba(245,243,255,0.5); border-radius:18px; padding:16px; }
        .fanmeet-host-head { display:flex; align-items:center; justify-content:space-between; gap:8px; flex-wrap:wrap; margin-bottom:10px; }
        .fanmeet-host-title { font-size:13px; font-family:"Orbitron",sans-serif; letter-spacing:0.12em; color:#5b21b6; }
        .fanmeet-host-tools { display:flex; gap:6px; flex-wrap:wrap; }
        .fanmeet-mini-btn { font-size:11px; padding:6px 10px; border-radius:999px; border:1px solid rgba(148,163,184,0.35); background:#fff; color:#334155; }
        .fanmeet-mini-btn.pick { border-color:rgba(16,185,129,0.35); color:#065f46; }
        .fanmeet-mini-btn.del { border-color:rgba(248,113,113,0.35); color:#b91c1c; }
        .fanmeet-apply-box { margin-top:14px; display:flex; gap:8px; align-items:center; flex-wrap:wrap; }
        .fanmeet-participant-table { width:100%; border-collapse:collapse; font-size:12px; }
        .fanmeet-participant-table th, .fanmeet-participant-table td { border-bottom:1px solid rgba(196,181,253,0.22); padding:8px 6px; text-align:left; }
        .fanmeet-status-pill { display:inline-flex; align-items:center; border-radius:999px; padding:2px 8px; font-size:10px; font-weight:700; }
        .fanmeet-status-pill.picked { background:rgba(16,185,129,0.15); color:#065f46; }
        .fanmeet-status-pill.applied { background:rgba(59,130,246,0.12); color:#1d4ed8; }
        .fanmeet-status-pill.approved { background:rgba(16,185,129,0.15); color:#065f46; }
        .fanmeet-status-pill.waiting { background:rgba(251,191,36,0.2); color:#92400e; }
    </style>
</head>

<body class="page-main min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
    <div class="container mx-auto max-w-4xl">
        <section class="glass-card p-8 md:p-10">
            <div class="flex items-start justify-between gap-4 mb-6">
                <h1 class="font-orbitron text-2xl md:text-4xl font-black text-slate-900">${boardTitle}</h1>
                <a class="nav-link shrink-0" href="${ctx}/boards/${boardType}">
                    <i class="fa-solid fa-arrow-left"></i> 목록
                </a>
            </div>

            <c:if test="${not empty success}"><div class="flash-msg ok"><i class="fas fa-check-circle"></i> ${success}</div></c:if>
            <c:if test="${not empty error}"><div class="flash-msg err"><i class="fas fa-exclamation-circle"></i> ${error}</div></c:if>

            <c:if test="${(boardType eq 'map' or boardType eq 'fanmeeting') and not post.fanMeetApproved and (isAuthor or isAdmin)}">
                <div class="flash-msg err" style="background:rgba(251,191,36,0.12);border-color:rgba(251,191,36,0.35);color:rgba(120,53,15,0.95);">
                    <i class="fas fa-hourglass-half"></i> 관리자 승인 대기 중입니다. 승인 전에는 다른 사용자에게 목록에 보이지 않습니다.
                </div>
            </c:if>

            <div class="pb-4 border-b border-slate-200">
                <c:if test="${boardType eq 'report' and not empty post.category}">
                    <span class="badge-cat ${post.category}" style="margin-bottom:8px;display:inline-flex;">${post.categoryLabel}</span>
                </c:if>
                <c:if test="${post.secret}">
                    <span class="badge-secret-view" style="margin-bottom:8px;margin-left:6px;display:inline-flex;"><i class="fa-solid fa-lock fa-xs"></i> 비밀글</span>
                </c:if>
                <h2 class="text-xl md:text-2xl font-bold text-slate-900">${post.title}</h2>
                <div class="meta-row">
                    <span class="meta-badge"><i class="fas fa-user fa-xs"></i> ${post.authorNick}</span>
                    <span class="meta-badge"><i class="fas fa-clock fa-xs"></i> ${post.createdAtStr}</span>
                    <span class="meta-badge views"><i class="fas fa-eye fa-xs"></i> <span id="view-count">${post.viewCount}</span></span>
                    <span class="meta-badge likes ${liked ? 'liked' : ''}" id="like-btn" onclick="toggleLike(${post.id}, '${boardType}')">
                        <span class="like-spinner" id="like-spinner"></span>
                        <i class="fas fa-heart fa-xs" id="like-icon"></i>
                        <span id="like-count">${post.likeCount}</span>
                    </span>
                    <c:if test="${isAdmin or (boardType ne 'notice' and isAuthor)}">
                        <span class="author-actions ml-auto">
                            <a href="${ctx}/boards/${boardType}/${post.id}/edit" class="btn-edit"><i class="fas fa-pen fa-xs"></i> 수정</a>
                            <button type="button" class="btn-del" onclick="confirmDelete()"><i class="fas fa-trash fa-xs"></i> 삭제</button>
                            <form id="deleteForm" method="post" action="${ctx}/boards/${boardType}/${post.id}/delete" style="display:none;"></form>
                        </span>
                    </c:if>
                    <c:if test="${not empty loginMember and not isAuthor}">
                        <button type="button" class="btn-report-post" onclick="openReportModal('board', ${post.id})"><i class="fas fa-flag fa-xs"></i> 신고</button>
                    </c:if>
                </div>
                <c:if test="${boardType eq 'map' or boardType eq 'fanmeeting'}">
                    <div class="meta-row" style="margin-top:12px;">
                        <c:if test="${not empty postTrainee}">
                            <span class="meta-badge"><i class="fas fa-id-badge fa-xs"></i> ${postTrainee.name}</span>
                        </c:if>
                        <c:if test="${not empty post.eventAtStr}">
                            <span class="meta-badge" style="color:rgba(190,24,93,0.85);"><i class="fas fa-calendar-day fa-xs"></i> ${post.eventAtStr}</span>
                        </c:if>
                        <span class="meta-badge"><i class="fas fa-bolt fa-xs"></i> ${post.fanMeetScheduleLabel}</span>
                        <span class="meta-badge"><i class="fas fa-door-open fa-xs"></i> ${post.recruitStatusLabel}</span>
                        <c:if test="${not empty post.participationTypeLabel}">
                            <span class="meta-badge"><i class="fas fa-users fa-xs"></i> ${post.participationTypeLabel}</span>
                        </c:if>
                        <c:if test="${post.maxCapacity != null}">
                            <span class="meta-badge"><i class="fas fa-user-group fa-xs"></i> 정원 ${post.maxCapacity}명</span>
                        </c:if>
                    </div>
                </c:if>
                <c:if test="${boardType eq 'map'}">
                    <div class="casting-event-panel">
                        <div class="casting-event-panel__k">CASTING EVENT</div>
                        <div class="flex flex-wrap items-center gap-2 mb-2">
                            <c:choose>
                                <c:when test="${post.castingEventStatusLabel eq '진행중'}">
                                    <span class="casting-event-status casting-event-status--live">${post.castingEventStatusLabel}</span>
                                </c:when>
                                <c:when test="${post.castingEventStatusLabel eq '예정'}">
                                    <span class="casting-event-status casting-event-status--soon">${post.castingEventStatusLabel}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="casting-event-status casting-event-status--end">${post.castingEventStatusLabel}</span>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${not empty post.castingEventPeriodStr}">
                                <span class="text-xs text-slate-600"><i class="fas fa-clock fa-xs mr-1 opacity-70"></i>${post.castingEventPeriodStr}</span>
                            </c:if>
                        </div>
                        <c:if test="${not empty post.castingEffectSummaryLine}">
                            <div class="text-sm font-semibold text-slate-800 leading-relaxed">
                                <i class="fas fa-wand-magic-sparkles text-violet-500 mr-1"></i><c:out value="${post.castingEffectSummaryLine}" />
                            </div>
                        </c:if>
                        <c:if test="${empty post.castingEffectSummaryLine}">
                            <p class="text-sm text-slate-600">관리자가 이벤트 효과를 설정하면 이곳에 표시됩니다.</p>
                        </c:if>
                        <a class="btn-casting-join" href="${ctx}/boards/map">
                            <i class="fas fa-map-location-dot"></i> 캐스팅 맵으로 탐색하기
                        </a>
                    </div>
                </c:if>
                <c:if test="${boardType eq 'fanmeeting'}">
                    <c:if test="${not empty loginMember and not fanMeetingHost and not isAdmin}">
                        <div class="fanmeet-apply-box">
                            <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/participants/apply" style="margin:0;">
                                <button type="submit" class="btn-edit" <c:if test="${post.recruitStatus eq 'DONE'}">disabled</c:if>>
                                    <i class="fas fa-user-plus fa-xs"></i> 참여 신청
                                </button>
                            </form>
                        </div>
                    </c:if>
                    <c:if test="${fanMeetingHost or isAdmin}">
                        <div class="fanmeet-host-box">
                            <div class="fanmeet-host-head">
                                <div class="fanmeet-host-title">PARTICIPANTS</div>
                                <div class="fanmeet-host-tools">
                                    <span class="meta-badge">신청 ${fanMeetingAppliedCount}명</span>
                                    <span class="meta-badge">선정 ${fanMeetingPickedCount}명</span>
                                    <c:if test="${post.participationType eq 'LOTTERY'}">
                                        <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/lottery-draw" style="margin:0;">
                                            <button type="submit" class="fanmeet-mini-btn pick">일괄 추첨</button>
                                        </form>
                                    </c:if>
                                    <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/close" style="margin:0;">
                                        <button type="submit" class="fanmeet-mini-btn" <c:if test="${post.recruitStatus eq 'DONE'}">disabled</c:if>>
                                            모집 마감
                                        </button>
                                    </form>
                                </div>
                            </div>
                            <c:choose>
                                <c:when test="${empty fanMeetingParticipants}">
                                    <p class="text-sm text-slate-500">아직 참여 신청자가 없습니다.</p>
                                </c:when>
                                <c:otherwise>
                                    <table class="fanmeet-participant-table">
                                        <thead>
                                            <tr>
                                                <th>닉네임</th>
                                                <th>상태</th>
                                                <th>관리</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="fp" items="${fanMeetingParticipants}">
                                                <tr>
                                                    <td>${fp.userNick}</td>
                                                    <td>
                                                        <span class="fanmeet-status-pill
                                                            <c:choose>
                                                                <c:when test="${fp.status eq 'PICKED'}">picked</c:when>
                                                                <c:when test="${fp.status eq 'APPROVED'}">approved</c:when>
                                                                <c:when test="${fp.status eq 'WAITING'}">waiting</c:when>
                                                                <c:otherwise>applied</c:otherwise>
                                                            </c:choose>">${fp.statusLabel}</span>
                                                    </td>
                                                    <td>
                                                        <div class="fanmeet-host-tools">
                                                            <c:if test="${post.participationType eq 'CONTACT'}">
                                                                <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/participants/${fp.id}/approve" style="margin:0;">
                                                                    <button type="submit" class="fanmeet-mini-btn pick">승인</button>
                                                                </form>
                                                                <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/participants/${fp.id}/wait" style="margin:0;">
                                                                    <button type="submit" class="fanmeet-mini-btn">대기</button>
                                                                </form>
                                                            </c:if>
                                                            <c:if test="${post.participationType ne 'CONTACT' and fp.status ne 'PICKED'}">
                                                                <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/participants/${fp.id}/pick" style="margin:0;">
                                                                    <button type="submit" class="fanmeet-mini-btn pick">선정</button>
                                                                </form>
                                                            </c:if>
                                                            <form method="post" action="${ctx}/boards/fanmeeting/${post.id}/participants/${fp.id}/delete" style="margin:0;">
                                                                <button type="submit" class="fanmeet-mini-btn del">제외</button>
                                                            </form>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                </c:if>
            </div>

            <div class="post-body-card">
                <div class="post-body-text"><c:out value="${post.content}" /></div>

                <c:if test="${not empty post.originalFilename and not empty post.storedFilename}">
                    <div class="post-attachment-box">
                        <div class="attachment-label">ATTACHMENT</div>

                        <c:choose>
                            <c:when test="${post.image}">
                                <img src="${ctx}/boards/files/${post.storedFilename}?inline=true"
                                     alt="${post.originalFilename}"
                                     class="attachment-image" />
                            </c:when>
                            <c:otherwise>
                                <div class="attachment-file">
                                    <span class="attachment-file__name">
                                        <i class="fa-solid fa-paperclip"></i>
                                        ${post.originalFilename}
                                    </span>
                                    <a class="nav-link shrink-0" href="${ctx}/boards/files/${post.storedFilename}">
                                        <i class="fa-solid fa-download"></i> 다운로드
                                    </a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:if>
            </div>

            <c:if test="${post.hasMapLocation}">
                <div class="post-place-box">
                    <div class="attachment-label">LOCATION</div>
                    <div class="post-place-name">
                        <i class="fa-solid fa-location-dot" style="color:rgba(233,176,196,0.95);"></i>
                        <span><c:out value="${post.placeName}" /></span>
                    </div>
                    <c:if test="${not empty post.address}">
                        <p class="text-sm text-slate-600 mb-3"><i class="fa-solid fa-road mr-1"></i><c:out value="${post.address}" /></p>
                    </c:if>
                    <c:if test="${boardType eq 'map' or boardType eq 'fanmeeting'}">
                        <div class="flex flex-wrap gap-2 mb-3">
                            <c:if test="${post.eventAt ne null}">
                                <a class="btn-edit" style="text-decoration:none;" href="${ctx}/boards/${boardType}/${post.id}/fanmeet.ics"><i class="fas fa-calendar-plus fa-xs"></i> 캘린더(.ics) 내보내기</a>
                            </c:if>
                            <a class="btn-edit" style="text-decoration:none;" target="_blank" rel="noopener noreferrer"
                               href="https://map.kakao.com/link/map/${post.lat},${post.lng}"><i class="fas fa-map fa-xs"></i> 카카오맵에서 보기</a>
                            <a class="btn-edit" style="text-decoration:none;" target="_blank" rel="noopener noreferrer"
                               href="https://www.google.com/maps/search/?api=1&amp;query=${post.lat},${post.lng}"><i class="fas fa-location-crosshairs fa-xs"></i> Google 지도</a>
                        </div>
                    </c:if>
                    <c:choose>
                        <c:when test="${not empty kakaoMapJavascriptKey}">
                            <div id="boardDetailMap" class="post-place-map"></div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-sm text-slate-600">카카오맵 JavaScript 키가 없어 지도를 표시할 수 없습니다. 위·경도는 저장되어 있습니다.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <div class="comment-section" id="comments">
                <div class="comment-section__title">COMMENTS (${fn:length(comments)})</div>

                <c:choose>
                    <c:when test="${not empty loginMember}">
                        <form method="post" action="${ctx}/boards/${boardType}/${post.id}/comments" class="comment-input-wrap">
                            <textarea name="content" placeholder="댓글을 입력하세요 (최대 500자)" maxlength="500" rows="2"></textarea>
                            <button type="submit" class="btn-comment-submit"><i class="fas fa-paper-plane"></i></button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div style="padding:14px 16px; border-radius:16px; background:rgba(255,255,255,0.55); border:1px solid rgba(226,232,240,0.95); text-align:center; color:rgba(71,85,105,0.85); font-size:13px; margin-bottom:20px;">
                            <a href="${ctx}/login" style="color:rgba(190,24,93,0.8); text-decoration:underline;">로그인</a> 후 댓글을 작성할 수 있습니다.
                        </div>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${empty comments}">
                        <div class="no-comments"><i class="fas fa-comment-slash"></i> 아직 댓글이 없어요. 첫 댓글을 남겨보세요!</div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="c" items="${comments}">
                            <div class="comment-item" id="comment-${c.id}">
                                <div class="comment-header">
                                    <span class="comment-nick"><i class="fas fa-user fa-xs" style="opacity:0.6;margin-right:4px;"></i>${c.authorNick}</span>
                                    <div class="comment-actions">
                                        <span class="comment-date">${c.createdAtStr}</span>
                                        <c:if test="${not empty loginMember and (loginMember.mno eq c.authorMno or isAdmin)}">
                                            <button type="button" class="btn-comment-edit" onclick="toggleCommentEdit(${c.id})">수정</button>
                                            <form method="post" action="${ctx}/boards/${boardType}/${post.id}/comments/${c.id}/delete" style="display:inline;">
                                                <button type="submit" class="btn-comment-del" onclick="return confirm('댓글을 삭제하시겠습니까?')">삭제</button>
                                            </form>
                                        </c:if>
                                        <c:if test="${not empty loginMember and loginMember.mno ne c.authorMno}">
                                            <button type="button" class="btn-comment-report" onclick="openReportModal('comment', ${c.id})">신고</button>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="comment-content"><c:out value="${c.content}" /></div>

                                <c:if test="${not empty loginMember and (loginMember.mno eq c.authorMno or isAdmin)}">
                                    <form method="post" action="${ctx}/boards/${boardType}/${post.id}/comments/${c.id}/edit"
                                          class="comment-edit-wrap" id="comment-edit-${c.id}">
                                        <textarea name="content" maxlength="500"><c:out value="${c.content}" /></textarea>
                                        <div class="comment-edit-actions">
                                            <button type="button" class="btn-comment-cancel" onclick="toggleCommentEdit(${c.id})">취소</button>
                                            <button type="submit" class="btn-comment-save">저장</button>
                                        </div>
                                    </form>
                                </c:if>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </section>
    </div>
</main>

<div class="report-modal-backdrop" id="reportModalBackdrop" onclick="closeReportModal(event)">
    <div class="report-modal">
        <div class="report-modal__title"><i class="fas fa-flag fa-xs" style="margin-right:6px;color:rgba(248,113,113,0.8);"></i> 신고하기</div>
        <input type="hidden" id="reportTargetType" value="" />
        <input type="hidden" id="reportTargetId" value="" />
        <select id="reportReason">
            <option value="" disabled selected>신고 사유를 선택해주세요</option>
            <option value="spam">스팸 / 광고</option>
            <option value="obscene">음란 / 불쾌한 내용</option>
            <option value="abuse">욕설 / 비방</option>
            <option value="illegal">불법 정보</option>
            <option value="other">기타</option>
        </select>
        <textarea id="reportDescription" rows="3" placeholder="추가 설명 (선택, 최대 200자)" maxlength="200"></textarea>
        <div class="report-modal__actions">
            <button type="button" class="btn-modal-cancel" onclick="closeReportModal()">취소</button>
            <button type="button" class="btn-modal-submit" onclick="submitReport()"><i class="fas fa-flag fa-xs"></i> 신고 접수</button>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
<c:if test="${post.hasMapLocation and not empty kakaoMapJavascriptKey}">
<script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapJavascriptKey}&autoload=false" charset="UTF-8"></script>
<script>
(function() {
    function domReady(fn) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', fn);
        } else {
            fn();
        }
    }
    domReady(function() {
        if (typeof kakao === 'undefined' || !kakao.maps) return;
        kakao.maps.load(function() {
            var el = document.getElementById('boardDetailMap');
            if (!el) return;
            var lat = ${post.lat};
            var lng = ${post.lng};
            var pos = new kakao.maps.LatLng(lat, lng);
            var map = new kakao.maps.Map(el, { center: pos, level: 3 });
            new kakao.maps.Marker({ position: pos, map: map });
            function relayout() {
                map.relayout();
            }
            setTimeout(relayout, 100);
            setTimeout(relayout, 400);
            window.addEventListener('resize', relayout);
        });
    });
})();
</script>
</c:if>
<script>
var CTX = '${ctx}';
var BOARD_TYPE = '${boardType}';
var POST_ID = ${post.id};
var LIKED = ${liked};
var IS_LOGGED_IN = ${not empty loginMember ? 'true' : 'false'};

function openReportModal(targetType, targetId) {
    if (!IS_LOGGED_IN) {
        alert('로그인이 필요합니다.');
        window.location.href = CTX + '/login';
        return;
    }
    document.getElementById('reportTargetType').value = targetType;
    document.getElementById('reportTargetId').value = targetId;
    document.getElementById('reportReason').value = '';
    document.getElementById('reportDescription').value = '';
    document.getElementById('reportModalBackdrop').classList.add('is-open');
}

function closeReportModal(event) {
    if (event && event.target !== document.getElementById('reportModalBackdrop')) return;
    document.getElementById('reportModalBackdrop').classList.remove('is-open');
}

function submitReport() {
    var targetType = document.getElementById('reportTargetType').value;
    var targetId = document.getElementById('reportTargetId').value;
    var reason = document.getElementById('reportReason').value;
    var description = document.getElementById('reportDescription').value;
    if (!reason) {
        alert('신고 사유를 선택해주세요.');
        return;
    }
    var url = targetType === 'board'
        ? CTX + '/boards/' + BOARD_TYPE + '/' + POST_ID + '/report'
        : CTX + '/boards/' + BOARD_TYPE + '/' + POST_ID + '/comments/' + targetId + '/report';
    var formData = new FormData();
    formData.append('reason', reason);
    if (description) formData.append('description', description);
    fetch(url, { method: 'POST', body: formData, headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            document.getElementById('reportModalBackdrop').classList.remove('is-open');
            if (data.success) {
                alert(data.message || '신고가 접수되었습니다.');
            } else {
                alert(data.error || '신고 처리 중 오류가 발생했습니다.');
            }
        })
        .catch(function() {
            alert('신고 처리 중 오류가 발생했습니다. 다시 시도해주세요.');
        });
}

function toggleLike(postId, boardType) {
    var btn = document.getElementById('like-btn');
    var spinner = document.getElementById('like-spinner');
    var icon = document.getElementById('like-icon');
    var count = document.getElementById('like-count');
    if (!IS_LOGGED_IN) {
        alert('로그인이 필요합니다.');
        window.location.href = CTX + '/login';
        return;
    }

    btn.style.pointerEvents = 'none';
    spinner.style.display = 'inline-block';
    icon.style.display = 'none';

    safeFetch(CTX + '/boards/' + boardType + '/' + postId + '/like', {
        method: 'POST',
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(function(res) { return res.json(); })
    .then(function(data) {
        LIKED = data.liked;
        count.textContent = data.likeCount;
        if (data.liked) {
            btn.classList.add('liked');
            showToast('좋아요를 눌렀어요 ♥', 'ok');
        } else {
            btn.classList.remove('liked');
            showToast('좋아요를 취소했어요', 'info');
        }
    })
    .catch(function() {})
    .finally(function() {
        spinner.style.display = 'none';
        icon.style.display = 'inline';
        btn.style.pointerEvents = 'auto';
    });
}

function confirmDelete() {
    if (confirm('정말로 삭제하시겠습니까? 댓글도 함께 삭제됩니다.')) {
        document.getElementById('deleteForm').submit();
    }
}

function toggleCommentEdit(commentId) {
    var wrap = document.getElementById('comment-edit-' + commentId);
    if (!wrap) {
        return;
    }

    var isOpen = wrap.classList.contains('is-open');
    document.querySelectorAll('.comment-edit-wrap.is-open').forEach(function(node) {
        node.classList.remove('is-open');
    });

    if (!isOpen) {
        wrap.classList.add('is-open');
        var textarea = wrap.querySelector('textarea');
        if (textarea) {
            textarea.focus();
            textarea.setSelectionRange(textarea.value.length, textarea.value.length);
        }
    }
}
</script>
</body>
</html>
