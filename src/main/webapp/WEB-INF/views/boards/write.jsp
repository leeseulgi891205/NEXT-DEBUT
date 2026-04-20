<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>UNIT-X - ${boardTitle} 글쓰기</title>

    <%@ include file="/WEB-INF/views/fragments/head-common.jspf" %>
    <style>
        .form-select { width:100%; padding:12px 16px; border-radius:14px; background:rgba(255,255,255,0.90);
            border:1px solid rgba(148,163,184,0.35); color:#0f172a; font-size:14px; outline:none; cursor:pointer;
            transition:border 200ms ease; appearance:none;
            background-image:url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2394a3b8' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
            background-repeat:no-repeat; background-position:right 14px center; }
        .form-select:focus { border-color:rgba(233,176,196,0.50); }
        .secret-wrap { display:flex; align-items:center; gap:10px; padding:14px 16px; border-radius:14px;
            background:rgba(100,116,139,0.05); border:1px solid rgba(148,163,184,0.25); cursor:pointer; transition:background 200ms ease; }
        .secret-wrap:hover { background:rgba(100,116,139,0.10); }
        .secret-wrap input[type="checkbox"] { width:16px; height:16px; accent-color:rgba(168,85,247,0.80); cursor:pointer; }
        .secret-label { font-size:14px; color:rgba(51,65,85,0.85); user-select:none; }
        .secret-desc { font-size:12px; color:rgba(100,116,139,0.75); margin-top:2px; }
    </style>
</head>

<body class="page-main min-h-screen flex flex-col">
<%@ include file="/WEB-INF/views/fragments/topnav.jspf" %>

<main class="flex-1 px-6 pb-16" style="padding-top: calc(var(--nav-h) + 24px);">
    <div class="container mx-auto max-w-4xl">
        <section class="glass-card p-8 md:p-10">
            <div class="flex items-start justify-between gap-4">
                <div>
                    <h1 class="font-orbitron text-3xl md:text-5xl font-black text-slate-900 drop-shadow mb-2">${boardTitle}</h1>
                    <p class="text-slate-600">글쓰기</p>
                </div>
                <a class="nav-link" href="${ctx}/boards/${boardType}">
                    <i class="fa-solid fa-arrow-left"></i>
                    목록
                </a>
            </div>

            <c:if test="${not empty error}">
                <div class="mt-6 px-4 py-3 rounded-xl bg-red-500/10 border border-red-200 text-red-800">
                    ${error}
                </div>
            </c:if>

            <form class="mt-8 space-y-5" action="${ctx}/boards/${boardType}/write" method="post" enctype="multipart/form-data">

                <c:if test="${boardType eq 'report'}">
                    <div>
                        <label class="block text-slate-700 mb-2 font-semibold" for="category">분류 <span class="text-red-400 font-normal text-xs">*필수</span></label>
                        <select id="category" name="category" required class="form-select">
                            <option value="" disabled selected>분류를 선택해주세요</option>
                            <option value="bug">🐛 버그</option>
                            <option value="report">🚨 신고</option>
                        </select>
                    </div>
                </c:if>

                <c:if test="${boardType eq 'free'}">
                    <div>
                        <label class="block text-slate-700 mb-2 font-semibold" for="communityKind">글 종류</label>
                        <select id="communityKind" name="communityKind" class="form-select">
                            <option value="lounge" selected>자유</option>
                            <option value="guide">공략</option>
                        </select>
                    </div>
                </c:if>

                <div>
                    <label class="block text-slate-700 mb-2 font-semibold" for="title">제목</label>
                    <input id="title" name="title" type="text" maxlength="120" required
                           class="w-full px-4 py-3 rounded-xl bg-white/90 border border-slate-200 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-pink-200"
                           placeholder="제목을 입력" />
                </div>

                <div>
                    <label class="block text-slate-700 mb-2 font-semibold" for="content">내용</label>
                    <textarea id="content" name="content" rows="10" required
                              class="w-full px-4 py-3 rounded-xl bg-white/90 border border-slate-200 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-pink-200"
                              placeholder="내용을 입력"></textarea>
                </div>

                <c:if test="${boardType eq 'map' or boardType eq 'fanmeeting'}">
                <div class="rounded-2xl border border-pink-100 bg-pink-50/40 p-5 space-y-4">
                    <label class="block text-slate-800 font-semibold">모임 일정 · 모집</label>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="traineeId">연습생 <span class="text-red-600">*</span></label>
                        <input id="traineeSearch" type="text" maxlength="40" placeholder="연습생 이름 검색"
                               class="w-full max-w-md px-4 py-2.5 mb-2 rounded-xl bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-pink-200" />
                        <select id="traineeId" name="traineeId" class="w-full max-w-md px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900"
                                <c:if test="${boardType eq 'fanmeeting'}">required</c:if>>
                            <option value="">연습생 선택</option>
                            <c:forEach var="t" items="${trainees}">
                                <option value="${t.id}">${t.name}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="eventAt">모임 일시 <span class="text-red-600">*</span></label>
                        <input id="eventAt" name="eventAt" type="datetime-local" required
                               class="w-full max-w-md px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-pink-200" />
                    </div>
                    <div class="grid sm:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-slate-700 mb-2 text-sm font-medium" for="recruitStatus">모집 상태</label>
                            <select id="recruitStatus" name="recruitStatus" class="w-full px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                                <option value="RECRUITING" selected>모집중</option>
                                <option value="PLANNED">예정</option>
                                <option value="DONE">완료</option>
                            </select>
                        </div>
                        <div>
                            <label class="block text-slate-700 mb-2 text-sm font-medium" for="participationType">참여 방식</label>
                            <select id="participationType" name="participationType" class="w-full px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900">
                                <option value="FIRST_COME">선착순</option>
                                <option value="LOTTERY">추첨</option>
                                <option value="CONTACT">문의 후 참여</option>
                                <option value="FREE" selected>자유 참여</option>
                            </select>
                        </div>
                    </div>
                    <div>
                        <label class="block text-slate-700 mb-2 text-sm font-medium" for="maxCapacity">최대 인원 (선택)</label>
                        <input id="maxCapacity" name="maxCapacity" type="number" min="1" max="99999" placeholder="예: 30"
                               class="w-full max-w-xs px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900" />
                    </div>
                    <p class="text-xs text-slate-600">등록 후 <strong>관리자 승인</strong>이 있어야 목록에 공개됩니다.</p>
                </div>
                </c:if>

                <c:if test="${boardType eq 'map' or boardType eq 'fanmeeting'}">
                <div class="rounded-2xl border border-slate-200 bg-white/80 p-5 space-y-4">
                    <label class="block text-slate-700 font-semibold">장소 <span class="text-red-600">*</span></label>
                    <p class="text-xs text-slate-500">키워드 검색 또는 지도 클릭으로 장소를 선택할 수 있습니다.</p>
                    <c:choose>
                        <c:when test="${not empty kakaoMapJavascriptKey}">
                            <div class="flex flex-col sm:flex-row gap-2">
                                <input type="text" id="placeKeyword" maxlength="80" placeholder="예: 강남역 맛집"
                                       class="flex-1 px-4 py-3 rounded-xl bg-white border border-slate-200 text-slate-900 focus:outline-none focus:ring-2 focus:ring-pink-200" />
                                <button type="button" class="btn-primary px-5 py-3 shrink-0" id="btnPlaceSearch">장소 검색</button>
                            </div>
                            <ul id="placeResults" class="mt-2 max-h-48 overflow-y-auto rounded-xl border border-slate-100 divide-y divide-slate-100 text-sm hidden"></ul>
                            <input type="hidden" name="placeName" id="placeName" value="" />
                            <input type="hidden" name="address" id="address" value="" />
                            <input type="hidden" name="lat" id="lat" value="" />
                            <input type="hidden" name="lng" id="lng" value="" />
                            <input type="text" id="pickedAddress" readonly placeholder="선택한 주소가 표시됩니다."
                                   class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-700" />
                            <div class="mt-3">
                                <div class="text-xs font-orbitron tracking-widest text-slate-500 mb-2">▸ 지도에서 클릭하여 위치 선택</div>
                                <div id="mapPreview" class="w-full rounded-2xl border border-slate-200 overflow-hidden bg-slate-100" style="height:220px;"></div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p class="text-sm text-amber-800 bg-amber-50 border border-amber-100 rounded-xl px-4 py-3">카카오맵 키가 설정되지 않아 장소 검색을 사용할 수 없습니다. <code class="text-xs">kakao.map.javascript-key</code> 를 확인하세요.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
                </c:if>

                <div>
                    <label class="block text-slate-700 mb-2 font-semibold" for="file">
                        이미지 / 파일 첨부 <span class="text-slate-400 font-normal text-xs">(선택 · JPG·PNG·GIF·WEBP 등)</span>
                    </label>
                    <input id="file" name="file" type="file"
                           accept="image/*,.pdf,.zip,.txt,.doc,.docx"
                           onchange="previewImage(this)"
                           class="w-full text-slate-700 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:bg-slate-900 file:text-white hover:file:bg-slate-800 cursor-pointer" />
                    <%-- 이미지 미리보기 --%>
                    <div id="preview-wrap" style="display:none; margin-top:14px;">
                        <div class="text-xs font-orbitron tracking-widest text-slate-500 mb-2">▸ 미리보기</div>
                        <img id="preview-img" src="" alt="미리보기"
                             class="rounded-2xl border border-slate-200 max-w-full"
                             style="max-height:300px; object-fit:contain;" />
                    </div>
                </div>

                <c:if test="${boardType ne 'notice' and boardType ne 'map' and boardType ne 'fanmeeting'}">
                    <label class="secret-wrap" for="secret">
                        <input type="checkbox" id="secret" name="secret" value="true" />
                        <div>
                            <div class="secret-label"><i class="fa-solid fa-lock fa-xs" style="margin-right:4px;"></i> 비밀글로 작성</div>
                            <div class="secret-desc">체크하면 목록에서 숨겨지며, 본인과 관리자만 열람할 수 있어요.</div>
                        </div>
                    </label>
                </c:if>

                <div class="pt-2 flex justify-end gap-3">
                    <a class="nav-link" href="${ctx}/boards/${boardType}">취소</a>
                    <button class="btn-primary" type="submit">
                        <i class="fa-solid fa-check"></i>
                        등록
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
function previewImage(input) {
    var wrap = document.getElementById('preview-wrap');
    var img  = document.getElementById('preview-img');
    if (input.files && input.files[0]) {
        var file = input.files[0];
        if (file.type.startsWith('image/')) {
            var reader = new FileReader();
            reader.onload = function(e) {
                img.src = e.target.result;
                wrap.style.display = 'block';
            };
            reader.readAsDataURL(file);
        } else {
            wrap.style.display = 'none';
        }
    } else {
        wrap.style.display = 'none';
    }
}

(function () {
    var searchEl = document.getElementById('traineeSearch');
    var selectEl = document.getElementById('traineeId');
    if (!searchEl || !selectEl) return;

    var originalOptions = Array.from(selectEl.options).map(function (opt) {
        return { value: opt.value, label: opt.textContent };
    });

    function rerenderOptions(keyword) {
        var q = (keyword || '').trim().toLowerCase();
        var current = selectEl.value;
        selectEl.innerHTML = '';

        originalOptions.forEach(function (item, idx) {
            if (idx === 0 || item.label.toLowerCase().indexOf(q) !== -1) {
                var opt = document.createElement('option');
                opt.value = item.value;
                opt.textContent = item.label;
                selectEl.appendChild(opt);
            }
        });

        if (Array.from(selectEl.options).some(function (o) { return o.value === current; })) {
            selectEl.value = current;
        } else {
            selectEl.value = '';
        }
    }

    searchEl.addEventListener('input', function () {
        rerenderOptions(searchEl.value);
    });
})();

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
        byId('address').value = place.road_address_name || place.address_name || '';
        byId('pickedAddress').value = byId('address').value;
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

            showPreview(DEFAULT_LAT, DEFAULT_LNG, false);

            var ps = new kakao.maps.services.Places();
            var geocoder = new kakao.maps.services.Geocoder();

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

            kakao.maps.event.addListener(previewMap, 'click', function(mouseEvent) {
                var latlng = mouseEvent.latLng;
                var lat = latlng.getLat();
                var lng = latlng.getLng();
                byId('lat').value = String(lat);
                byId('lng').value = String(lng);
                showPreview(lat, lng, true);
                geocoder.coord2Address(lng, lat, function(result, status) {
                    if (status !== kakao.maps.services.Status.OK || !result || !result.length) {
                        byId('placeName').value = '선택한 위치';
                        byId('address').value = '';
                        byId('pickedAddress').value = '';
                        return;
                    }
                    var item = result[0];
                    var road = item.road_address ? item.road_address.address_name : '';
                    var jibun = item.address ? item.address.address_name : '';
                    byId('address').value = road || jibun || '';
                    byId('pickedAddress').value = byId('address').value;
                    byId('placeName').value = road || jibun || '선택한 위치';
                });
            });

            window.addEventListener('resize', relayoutMap);
        });
    });
})();
</c:if>
</script>
</body>
</html>
