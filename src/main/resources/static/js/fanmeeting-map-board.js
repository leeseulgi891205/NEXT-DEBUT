(function () {
  var boot = document.getElementById("fanMeetingMapBoot");
  if (!boot) return;

  var ctx = boot.getAttribute("data-ctx") || "";
  var hasKakao = boot.getAttribute("data-has-kakao") === "true";
  var loggedIn = boot.getAttribute("data-logged-in") === "true";
  var mapJsonRaw = boot.getAttribute("data-map-json") || "[]";
  var posts = [];
  try {
    posts = JSON.parse(mapJsonRaw);
  } catch (e) {
    posts = [];
  }

  var PIN_COLORS = ["#e11d48", "#7c3aed", "#2563eb", "#059669", "#d97706", "#db2777", "#0d9488", "#ea580c"];

  function colorForTrainee(traineeId) {
    if (traineeId == null || traineeId === "null") return "#64748b";
    var n = Math.abs(parseInt(String(traineeId), 10)) || 0;
    return PIN_COLORS[n % PIN_COLORS.length];
  }

  function pinDataUrl(hex, statusKey) {
    var dim = statusKey === "DONE";
    var w = 34;
    var h = 42;
    var canvas = document.createElement("canvas");
    canvas.width = w;
    canvas.height = h;
    var g = canvas.getContext("2d");
    if (dim) g.globalAlpha = 0.42;
    g.fillStyle = hex;
    g.beginPath();
    g.arc(w / 2, 13, 11, 0, Math.PI * 2);
    g.fill();
    g.beginPath();
    g.moveTo(w / 2 - 7, 22);
    g.lineTo(w / 2, h - 3);
    g.lineTo(w / 2 + 7, 22);
    g.closePath();
    g.fill();
    if (statusKey === "PLANNED") {
      g.globalAlpha = dim ? 0.35 : 0.9;
      g.strokeStyle = "rgba(255,255,255,0.95)";
      g.lineWidth = 2;
      g.beginPath();
      g.arc(w / 2, 13, 13, 0, Math.PI * 2);
      g.stroke();
    }
    return canvas.toDataURL("image/png");
  }

  var activeId = null;
  var markerById = new Map();
  var map = null;
  var pickMarker = null;
  var detailOverlay = null;
  var geocoder = null;
  var defaultZ = 1;

  function getFormEls() {
    return {
      placeName: document.getElementById("fmPlaceName"),
      address: document.getElementById("fmAddress"),
      lat: document.getElementById("fmLat"),
      lng: document.getElementById("fmLng"),
      picked: document.getElementById("fmPickedAddr"),
      form: document.getElementById("fmInlineWriteForm")
    };
  }

  function setActiveCard(postId) {
    activeId = postId;
    document.querySelectorAll(".fm-card").forEach(function (el) {
      el.classList.toggle("is-active", String(postId) === el.getAttribute("data-post-id"));
    });
    markerById.forEach(function (m, id) {
      if (!m || !m.setZIndex) return;
      m.setZIndex(String(id) === String(postId) ? 30 : defaultZ);
    });
  }

  function closeDetailOverlay() {
    if (detailOverlay) {
      detailOverlay.setMap(null);
    }
  }

  function openDetailOverlay(pos, post) {
    if (!map || !kakao || !kakao.maps) return;
    var detailUrl = ctx + "/boards/fanmeeting/" + post.id;
    var html =
      '<div class="fm-map-popup" style="padding:10px 12px;border-radius:14px;border:1px solid rgba(148,163,184,.4);background:#fff;box-shadow:0 10px 28px rgba(15,23,42,.15);max-width:240px;">' +
      '<div style="font-size:11px;color:#7c3aed;font-weight:800;">' +
      escapeHtml(post.traineeName || "") +
      "</div>" +
      '<div style="font-size:13px;font-weight:800;color:#0f172a;margin-top:3px;line-height:1.35;">' +
      escapeHtml(post.title || "") +
      "</div>" +
      '<div style="font-size:12px;color:#475569;margin-top:4px;">' +
      escapeHtml(post.placeName || "") +
      "</div>" +
      '<div style="font-size:11px;color:#64748b;margin-top:4px;">' +
      escapeHtml(post.status || "") +
      " · " +
      escapeHtml(post.date || "") +
      "</div>" +
      '<a href="' +
      detailUrl +
      '" style="display:inline-block;margin-top:8px;font-size:12px;font-weight:800;color:#7c3aed;text-decoration:none;">자세히 보기 →</a>' +
      "</div>";
    if (!detailOverlay) {
      detailOverlay = new kakao.maps.CustomOverlay({
        content: html,
        position: pos,
        yAnchor: 1.15,
        zIndex: 50
      });
    } else {
      detailOverlay.setContent(html);
      detailOverlay.setPosition(pos);
    }
    detailOverlay.setMap(map);
  }

  function escapeHtml(s) {
    if (!s) return "";
    return String(s)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function ensurePickMarker(pos) {
    if (!map) return;
    if (pickMarker) {
      pickMarker.setPosition(pos);
      pickMarker.setMap(map);
      return;
    }
    var imageSrc = pinDataUrl("#f472b6", "RECRUITING");
    var size = new kakao.maps.Size(34, 42);
    var opt = { offset: new kakao.maps.Point(17, 40) };
    var mi = new kakao.maps.MarkerImage(imageSrc, size, opt);
    pickMarker = new kakao.maps.Marker({ position: pos, map: map, image: mi, zIndex: 40 });
  }

  function applyGeocodeToForm(lat, lng) {
    var els = getFormEls();
    if (!els.form) return;
    if (els.lat) els.lat.value = String(lat);
    if (els.lng) els.lng.value = String(lng);
    if (!geocoder) return;
    geocoder.coord2Address(lng, lat, function (result, status) {
      if (status !== kakao.maps.services.Status.OK || !result || !result.length) {
        if (els.placeName) els.placeName.value = "선택한 위치";
        if (els.address) els.address.value = "";
        if (els.picked) els.picked.value = "";
        return;
      }
      var item = result[0];
      var road = item.road_address ? item.road_address.address_name : "";
      var jibun = item.address ? item.address.address_name : "";
      var line = road || jibun || "";
      if (els.address) els.address.value = line;
      if (els.picked) els.picked.value = line;
      if (els.placeName) els.placeName.value = line || "선택한 위치";
    });
  }

  if (hasKakao && typeof kakao !== "undefined" && kakao.maps) {
    kakao.maps.load(function () {
      var canvas = document.getElementById("fanMeetingMapCanvas");
      if (!canvas) return;

      map = new kakao.maps.Map(canvas, {
        center: new kakao.maps.LatLng(37.5666805, 126.9784147),
        level: 8
      });

      if (kakao.maps.services) {
        geocoder = new kakao.maps.services.Geocoder();
      }

      var bounds = new kakao.maps.LatLngBounds();

      posts.forEach(function (post) {
        if (typeof post.lat !== "number" || typeof post.lng !== "number") return;
        var pos = new kakao.maps.LatLng(post.lat, post.lng);
        bounds.extend(pos);
        var hex = colorForTrainee(post.traineeId);
        var sk = post.statusKey || "RECRUITING";
        var src = pinDataUrl(hex, sk);
        var size = new kakao.maps.Size(34, 42);
        var opt = { offset: new kakao.maps.Point(17, 40) };
        var mi = new kakao.maps.MarkerImage(src, size, opt);
        var marker = new kakao.maps.Marker({ map: map, position: pos, image: mi, zIndex: defaultZ });
        markerById.set(String(post.id), marker);

        kakao.maps.event.addListener(marker, "click", function () {
          setActiveCard(post.id);
          openDetailOverlay(pos, post);
        });
      });

      if (posts.length > 0) {
        try {
          map.setBounds(bounds);
        } catch (e) {}
      }

      kakao.maps.event.addListener(map, "click", function (e) {
        closeDetailOverlay();
        if (!loggedIn) return;
        var els = getFormEls();
        if (!els.form) return;
        var latlng = e.latLng;
        var lat = latlng.getLat();
        var lng = latlng.getLng();
        ensurePickMarker(latlng);
        applyGeocodeToForm(lat, lng);
      });
    });
  }

  document.querySelectorAll(".fm-card").forEach(function (card) {
    card.addEventListener("mouseenter", function () {
      var id = card.getAttribute("data-post-id");
      setActiveCard(id);
      if (map && markerById.has(id)) {
        var marker = markerById.get(id);
        map.panTo(marker.getPosition());
      }
    });
  });
})();
