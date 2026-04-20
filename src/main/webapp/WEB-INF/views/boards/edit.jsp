<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>UNIT-X - ${boardTitle} 글 수정</title>
    <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
</head>

<body class="page-main min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
    <div class="container mx-auto max-w-4xl">
        <section class="glass-card p-8 md:p-10">
            <div class="flex items-start justify-between gap-4">
                <div>
                    <h1 class="font-orbitron text-3xl md:text-5xl font-black text-slate-900 drop-shadow mb-2">${boardTitle}</h1>
                    <p class="text-slate-600">글 수정</p>
                </div>
                <a class="nav-link" href="${ctx}/boards/${boardType}/${post.id}">
                    <i class="fa-solid fa-arrow-left"></i>
                    상세보기
                </a>
            </div>

            <c:if test="${not empty error}">
                <div class="mt-6 px-4 py-3 rounded-xl bg-red-500/10 border border-red-200 text-red-800">
                    ${error}
                </div>
            </c:if>

            <form class="mt-8 space-y-5" action="${ctx}/boards/${boardType}/${post.id}/edit" method="post">
                <div>
                    <label class="block text-slate-700 mb-2 font-semibold" for="title">제목</label>
                    <input id="title" name="title" type="text" maxlength="120" required value="${post.title}"
                           class="w-full px-4 py-3 rounded-xl bg-white/90 border border-slate-200 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-pink-200"
                           placeholder="제목을 입력" />
                </div>

                <div>
                    <label class="block text-slate-700 mb-2 font-semibold" for="content">내용</label>
                    <textarea id="content" name="content" rows="10" required maxlength="10000"
                              class="w-full px-4 py-3 rounded-xl bg-white/90 border border-slate-200 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-pink-200"
                              placeholder="내용을 입력"><c:out value="${post.content}" /></textarea>
                </div>

                <c:if test="${boardType eq 'fanmeeting'}">
                <div class="rounded-2xl border border-violet-100 bg-violet-50/35 p-5 space-y-4">
                    <label class="block text-slate-800 font-semibold">팬미팅 일정 · 모집 <span class="text-xs font-normal text-slate-500">(길거리 캐스팅과 별도)</span></label>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="traineeId">연습생 <span class="text-red-600">*</span></label>
                        <select id="traineeId" name="traineeId" required class="w-full max-w-md px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                            <option value="">연습생 선택</option>
                            <c:forEach var="t" items="${trainees}">
                                <option value="${t.id}" ${post.traineeId eq t.id ? 'selected' : ''}>${t.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="eventAt">팬미팅 일시 <span class="text-red-600">*</span></label>
                        <input id="eventAt" name="eventAt" type="datetime-local" required value="${post.eventAtInputValue}"
                               class="w-full max-w-md px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-violet-200" />
                    </div>
                    <div class="grid sm:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-slate-700 mb-2 text-sm font-medium" for="recruitStatus">상태</label>
                            <select id="recruitStatus" name="recruitStatus" class="w-full px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                                <option value="RECRUITING" ${post.recruitStatus eq 'RECRUITING' or post.recruitStatus eq 'OPEN' ? 'selected' : ''}>모집중</option>
                                <option value="PLANNED" ${post.recruitStatus eq 'PLANNED' ? 'selected' : ''}>예정</option>
                                <option value="DONE" ${post.recruitStatus eq 'DONE' or post.recruitStatus eq 'CLOSED' ? 'selected' : ''}>완료</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-slate-700 mb-2 text-sm font-medium" for="participationType">참여 방식</label>
                            <select id="participationType" name="participationType" class="w-full px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                                <option value="FIRST_COME" ${post.participationType eq 'FIRST_COME' ? 'selected' : ''}>선착순</option>
                                <option value="LOTTERY" ${post.participationType eq 'LOTTERY' ? 'selected' : ''}>추첨</option>
                                <option value="CONTACT" ${post.participationType eq 'CONTACT' ? 'selected' : ''}>문의 후 참여</option>
                                <option value="FREE" ${post.participationType eq 'FREE' or empty post.participationType ? 'selected' : ''}>자유 참여</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="maxCapacity">최대 인원 (선택)</label>
                        <input id="maxCapacity" name="maxCapacity" type="number" min="1" max="99999" placeholder="예: 30" value="${post.maxCapacity}"
                               class="w-full max-w-xs px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900" />
                    </div>
                    <c:if test="${not post.fanMeetApproved}">
                        <p class="text-xs text-amber-800">현재 <strong>승인 대기</strong> 상태입니다. 수정 시 다시 승인이 필요합니다.</p>
                    </c:if>
                </div>
                </c:if>

                <c:if test="${boardType eq 'map'}">
                <div class="rounded-2xl border border-pink-100 bg-pink-50/40 p-5 space-y-4">
                    <label class="block text-slate-800 font-semibold">모임 일정 · 모집</label>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="eventAt">모임 일시 <span class="text-red-600">*</span></label>
                        <input id="eventAt" name="eventAt" type="datetime-local" required value="${post.eventAtInputValue}"
                               class="w-full max-w-md px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-pink-200" />
                    </div>
                    <div class="grid sm:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-slate-700 mb-2 text-sm font-medium" for="recruitStatus">모집 상태</label>
                            <select id="recruitStatus" name="recruitStatus" class="w-full px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                                <option value="OPEN" ${post.recruitOpen ? 'selected' : ''}>모집 중</option>
                                <option value="CLOSED" ${post.recruitOpen ? '' : 'selected'}>마감</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-slate-700 mb-2 text-sm font-medium" for="participationType">참여 방식</label>
                            <select id="participationType" name="participationType" class="w-full px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                                <option value="FIRST_COME" ${post.participationType eq 'FIRST_COME' ? 'selected' : ''}>선착순</option>
                                <option value="LOTTERY" ${post.participationType eq 'LOTTERY' ? 'selected' : ''}>추첨</option>
                                <option value="CONTACT" ${post.participationType eq 'CONTACT' ? 'selected' : ''}>문의 후 참여</option>
                                <option value="FREE" ${post.participationType eq 'FREE' or empty post.participationType ? 'selected' : ''}>자유 참여</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="maxCapacity">최대 인원 (선택)</label>
                        <input id="maxCapacity" name="maxCapacity" type="number" min="1" max="99999" placeholder="예: 30" value="${post.maxCapacity}"
                               class="w-full max-w-xs px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900" />
                    </div>
                    <c:if test="${not post.fanMeetApproved}">
                        <p class="text-xs text-amber-800">현재 <strong>승인 대기</strong> 상태입니다. 수정 시 다시 승인이 필요합니다.</p>
                    </c:if>
                </div>
                </c:if>

                <c:if test="${boardType eq 'map' or boardType eq 'fanmeeting'}">
                <div class="rounded-2xl border border-slate-200 bg-white/80 p-5 space-y-4">
                    <label class="block text-slate-700 font-semibold">장소 <span class="text-red-600">*</span></label>
                    <p class="text-xs text-slate-500">검색 후 목록에서 선택하면 장소가 저장됩니다. <strong>장소 제거</strong>를 누른 뒤 저장하면 지도 정보가 삭제됩니다.</p>
                    <c:choose>
                        <c:when test="${not empty kakaoMapJavascriptKey}">
                            <div class="flex flex-wrap gap-2 mb-2">
                                <button type="button" class="text-xs px-3 py-2 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50" id="btnClearPlace">장소 제거</button>
                            </div>
                            <div class="flex flex-col sm:flex-row gap-2">
                                <input type="text" id="placeKeyword" maxlength="80" placeholder="예: 강남역 맛집"
                                       class="flex-1 px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-pink-200" />
                                <button type="button" class="btn-primary px-5 py-3 shrink-0" id="btnPlaceSearch">장소 검색</button>
                            </div>
                            <ul id="placeResults" class="mt-2 max-h-48 overflow-y-auto rounded-xl border border-slate-100 divide-y divide-slate-100 text-sm hidden"></ul>
                            <input type="hidden" name="placeName" id="placeName" value="<c:out value='${post.placeName}' />" />
                            <input type="hidden" name="address" id="address" value="<c:out value='${post.address}' />" />
                            <input type="hidden" name="lat" id="lat" value="<c:out value='${post.lat}' />" />
                            <input type="hidden" name="lng" id="lng" value="<c:out value='${post.lng}' />" />
                            <div class="mt-3">
                                <div class="text-xs font-orbitron tracking-widest text-slate-500 mb-2">▸ 지도 미리보기</div>
                                <div id="mapPreview" class="w-full rounded-2xl border border-slate-200 overflow-hidden bg-slate-100" style="height:220px;"></div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <input type="hidden" name="placeName" value="<c:out value='${post.placeName}' />" />
                            <input type="hidden" name="lat" value="<c:out value='${post.lat}' />" />
                            <input type="hidden" name="lng" value="<c:out value='${post.lng}' />" />
                            <p class="text-sm text-amber-800 bg-amber-50 border border-amber-100 rounded-xl px-4 py-3">카카오맵 키가 없어 장소를 바꿀 수 없습니다. 기존 장소 정보는 그대로 유지됩니다.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
                </c:if>

                <c:if test="${boardType ne 'notice' and boardType ne 'map' and boardType ne 'fanmeeting'}">
                    <label class="flex items-start gap-3 p-4 rounded-xl border border-slate-200 bg-slate-50/50 cursor-pointer">
                        <input type="checkbox" name="secret" value="true" class="mt-1" ${post.secret ? 'checked' : ''} />
                        <span class="text-sm text-slate-700"><strong>비밀글</strong> — 목록에 제한 표시, 본인과 관리자만 열람</span>
                    </label>
                </c:if>

                <c:if test="${not empty post.originalFilename}">
                    <div>
                        <label class="block text-slate-700 mb-2 font-semibold">현재 첨부파일</label>
                        <div class="flex items-center justify-between gap-3 px-4 py-3 rounded-xl bg-white/90 border border-slate-200">
                            <span class="text-slate-700 flex items-center gap-2 overflow-hidden text-ellipsis whitespace-nowrap">
                                <i class="fa-solid fa-paperclip"></i>
                                ${post.originalFilename}
                            </span>
                            <a class="nav-link shrink-0" href="${ctx}/boards/files/${post.storedFilename}${post.image ? '?inline=true' : ''}">
                                <i class="fa-solid ${post.image ? 'fa-image' : 'fa-download'}"></i>
                                ${post.image ? '보기' : '다운로드'}
                            </a>
                        </div>
                    </div>
                </c:if>

                <div class="pt-2 flex justify-end gap-3">
                    <a class="nav-link" href="${ctx}/boards/${boardType}/${post.id}">취소</a>
                    <button class="btn-primary" type="submit">
                        <i class="fa-solid fa-check"></i>
                        수정 완료
                    </button>
                </div>
            </form>
        </section>
    </div>
</main>

<%@ include file="/WEB-INF/views/fragments/footer.jspf" %>
<c:if test="${(boardType eq 'map' or boardType eq 'fanmeeting') and not empty kakaoMapJavascriptKey}">
<script src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapJavascriptKey}&libraries=services&autoload=false" charset="UTF-8"></script>
</c:if>
<script>
<c:if test="${(boardType eq 'map' or boardType eq 'fanmeeting') and not empty kakaoMapJavascriptKey}">
(function() {
    var previewMap = null;
    var previewMarker = null;
    var DEFAULT_LAT = 37.5666805;
    var DEFAULT_LNG = 126.9784147;
    var DEFAULT_LEVEL = 5;

    function byId(id) { return document.getElementById(id); }

    function domReady(fn) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', fn);
        } else {
            fn();
        }
    }

    function relayoutMap() {
        if (previewMap) {
            previewMap.relayout();
        }
    }

    function showPreview(lat, lng, withMarker) {
        var wrap = byId('mapPreview');
        if (!wrap || typeof kakao === 'undefined' || !kakao.maps) return;
        var pos = new kakao.maps.LatLng(lat, lng);
        if (!previewMap) {
            previewMap = new kakao.maps.Map(wrap, { center: pos, level: withMarker ? 3 : DEFAULT_LEVEL });
        } else {
            previewMap.setCenter(pos);
            if (withMarker) {
                previewMap.setLevel(3);
            }
        }
        setTimeout(relayoutMap, 100);
        setTimeout(relayoutMap, 400);
        if (previewMarker) {
            previewMarker.setMap(null);
            previewMarker = null;
        }
        if (withMarker) {
            previewMarker = new kakao.maps.Marker({ position: pos, map: previewMap });
        }
    }

    function selectPlace(place) {
        var lat = parseFloat(place.y);
        var lng = parseFloat(place.x);
        if (isNaN(lat) || isNaN(lng)) return;
        byId('placeName').value = place.place_name || '';
        byId('lat').value = String(lat);
        byId('lng').value = String(lng);
        showPreview(lat, lng, true);
        var ul = byId('placeResults');
        if (ul) ul.classList.add('hidden');
    }

    function renderResults(places) {
        var ul = byId('placeResults');
        if (!ul) return;
        ul.innerHTML = '';
        if (!places || !places.length) {
            ul.classList.remove('hidden');
            var li = document.createElement('li');
            li.className = 'px-4 py-3 text-slate-500';
            li.textContent = '검색 결과가 없습니다.';
            ul.appendChild(li);
            return;
        }
        ul.classList.remove('hidden');
        places.forEach(function(p) {
            var li = document.createElement('li');
            li.className = 'px-4 py-3 cursor-pointer hover:bg-pink-50/60 transition-colors';
            var addr = p.road_address_name || p.address_name || '';
            li.innerHTML = '<div class="font-medium text-slate-800">' + (p.place_name || '') + '</div>' +
                (addr ? '<div class="text-xs text-slate-500 mt-1">' + addr + '</div>' : '');
            li.addEventListener('click', function() { selectPlace(p); });
            ul.appendChild(li);
        });
    }

    domReady(function() {
        if (typeof kakao === 'undefined' || !kakao.maps) {
            return;
        }
        kakao.maps.load(function() {
            var wrap = byId('mapPreview');
            var btn = byId('btnPlaceSearch');
            var kw = byId('placeKeyword');
            if (!wrap || !btn || !kw || !kakao.maps.services) return;

            var latEl = byId('lat');
            var lngEl = byId('lng');
            if (latEl && lngEl && latEl.value && lngEl.value) {
                var la = parseFloat(latEl.value);
                var ln = parseFloat(lngEl.value);
                if (!isNaN(la) && !isNaN(ln)) {
                    showPreview(la, ln, true);
                } else {
                    showPreview(DEFAULT_LAT, DEFAULT_LNG, false);
                }
            } else {
                showPreview(DEFAULT_LAT, DEFAULT_LNG, false);
            }

            var clearBtn = byId('btnClearPlace');
            if (clearBtn) {
                clearBtn.addEventListener('click', function() {
                    byId('placeName').value = '';
                    byId('lat').value = '';
                    byId('lng').value = '';
                    var ul = byId('placeResults');
                    if (ul) { ul.innerHTML = ''; ul.classList.add('hidden'); }
                    showPreview(DEFAULT_LAT, DEFAULT_LNG, false);
                });
            }

            var ps = new kakao.maps.services.Places();

            function runSearch() {
                var q = kw.value.trim();
                if (!q) {
                    alert('검색어를 입력하세요.');
                    return;
                }
                ps.keywordSearch(q, function(data, status) {
                    if (status === kakao.maps.services.Status.OK) {
                        renderResults(data);
                    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
                        renderResults([]);
                    } else {
                        alert('장소 검색에 실패했습니다. 잠시 후 다시 시도하세요.');
                    }
                });
            }

            btn.addEventListener('click', runSearch);
            kw.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    runSearch();
                }
            });

            window.addEventListener('resize', relayoutMap);
        });
    });
})();
</c:if>
</script>
</body>
</html>
