package com.java.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 게시판 글 엔티티 H2 파일 DB에 영구 저장 → 서버 재시작해도 유지
 */
@Entity
@Table(name = "BOARD")
public class Board {

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final DateTimeFormatter INPUT_DT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/** notice / free / map / report */
	@Column(name = "BOARD_TYPE", nullable = false, length = 20)
	private String boardType;

	/** report 게시판 분류: bug, report 등 */
	@Column(name = "CATEGORY", length = 30)
	private String category;

	@Column(nullable = false, length = 200)
	private String title;

	/** 본문 — 긴 텍스트 허용 */
	@Column(nullable = false, columnDefinition = "CLOB")
	private String content;

	/** 첨부파일 원본명 (없으면 null) */
	@Column(name = "ORIGINAL_FILENAME", length = 255)
	private String originalFilename;

	/** 서버에 저장된 UUID 파일명 (없으면 null) */
	@Column(name = "STORED_FILENAME", length = 255)
	private String storedFilename;

	/** 이미지 여부 (true면 뷰에서 <img> 태그로 표시) */
	@Column(name = "IS_IMAGE", nullable = false)
	private boolean image = false;

	/** 작성자 닉네임 (로그인 안 했으면 "익명") */
	@Column(name = "AUTHOR_NICK", length = 60)
	private String authorNick;

	@Column(name = "AUTHOR_MNO")
	private Long authorMno;

	/** 조회수 */
	@Column(name = "VIEW_COUNT", nullable = false)
	private long viewCount = 0;

	/** 좋아요 수 */
	@Column(name = "LIKE_COUNT", nullable = false)
	private long likeCount = 0;

	/** 관리자용 노출 여부 (true: 보임, false: 숨김) */
	@Column(name = "VISIBLE", nullable = false)
	private boolean visible = true;

	/** 메인 팝업 공지 여부 */
	@Column(name = "POPUP", nullable = false)
	private boolean popup = false;

	@Column(name = "SECRET", nullable = false)
	private boolean secret = false;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	/** 장소명 (카카오 장소 검색 등) */
	@Column(name = "PLACE_NAME", length = 200)
	private String placeName;

	/** 상세 주소 */
	@Column(name = "ADDRESS", length = 255)
	private String address;

	/** 연습생 FK */
	@Column(name = "TRAINEE_ID")
	private Long traineeId;

	/** 위도 */
	@Column(name = "LAT")
	private Double lat;

	/** 경도 */
	@Column(name = "LNG")
	private Double lng;

	/** 팬미팅 일시 (map 타입) */
	@Column(name = "EVENT_AT")
	private LocalDateTime eventAt;

	/** 모집 상태: OPEN, CLOSED */
	@Column(name = "RECRUIT_STATUS", length = 20)
	private String recruitStatus = "OPEN";

	/** 최대 인원 (null이면 미표기) */
	@Column(name = "MAX_CAPACITY")
	private Integer maxCapacity;

	/** 참여 방식 코드: FIRST_COME, LOTTERY, CONTACT, FREE */
	@Column(name = "PARTICIPATION_TYPE", length = 30)
	private String participationType;

	/** 관리자 승인 후 목록 노출 (map 신규/수정 시 false) */
	@Column(name = "FAN_MEET_APPROVED", nullable = false)
	private boolean fanMeetApproved = true;

	/** 캐스팅 이벤트 효과 타입 (CastingEventEffectType 이름) */
	@Column(name = "EFFECT_TYPE", length = 40)
	private String effectType;

	/** 효과 값 (숫자·포지션 코드 등, 타입별 해석) */
	@Column(name = "EFFECT_VALUE", length = 80)
	private String effectValue;

	@Column(name = "EVENT_START_AT")
	private LocalDateTime eventStartAt;

	@Column(name = "EVENT_END_AT")
	private LocalDateTime eventEndAt;

	@Column(name = "IS_EVENT_ACTIVE", nullable = false)
	private boolean eventActive = false;

	@Column(name = "BANNER_ENABLED", nullable = false)
	private boolean bannerEnabled = true;

	@PrePersist
	void onCreate() {
		if (createdAt == null)
			createdAt = LocalDateTime.now();
	}

	protected Board() {
	}

	public Board(String boardType, String title, String content, String originalFilename, String storedFilename,
			boolean image, String authorNick) {
		this(boardType, title, content, originalFilename, storedFilename, image, authorNick, null, null, null);
	}

	public Board(String boardType, String title, String content, String originalFilename, String storedFilename,
			boolean image, String authorNick, String placeName, Double lat, Double lng) {
		this.boardType = boardType;
		this.title = title;
		this.content = content;
		this.originalFilename = originalFilename;
		this.storedFilename = storedFilename;
		this.image = image;
		this.authorNick = authorNick;
		this.placeName = placeName;
		this.lat = lat;
		this.lng = lng;
	}

	/** 공지·자유·신고 등 (카테고리·비밀글·작성자 mno) */
	public Board(String boardType, String category, String title, String content, String originalFilename,
			String storedFilename, boolean image, String authorNick, Long authorMno, boolean secret) {
		this.boardType = boardType;
		this.category = category;
		this.title = title;
		this.content = content;
		this.originalFilename = originalFilename;
		this.storedFilename = storedFilename;
		this.image = image;
		this.authorNick = authorNick;
		this.authorMno = authorMno;
		this.secret = secret;
	}

	/* ── Getter ── */
	public Long getId() {
		return id;
	}

	public String getBoardType() {
		return boardType;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public String getStoredFilename() {
		return storedFilename;
	}

	public boolean isImage() {
		return image;
	}

	public String getAuthorNick() {
		return authorNick;
	}

	public Long getAuthorMno() {
		return authorMno;
	}

	public void setAuthorMno(Long authorMno) {
		this.authorMno = authorMno;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	public long getViewCount() {
		return viewCount;
	}

	public void setViewCount(long viewCount) {
		this.viewCount = Math.max(0, viewCount);
	}

	public long getLikeCount() {
		return likeCount;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isPopup() {
		return popup;
	}

	public void setPopup(boolean popup) {
		this.popup = popup;
	}

	public void incrementViewCount() {
		this.viewCount++;
	}

	public void incrementLikeCount() {
		this.likeCount++;
	}

	public void decrementLikeCount() {
		if (this.likeCount > 0)
			this.likeCount--;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public void setTitle(String t) {
		this.title = t;
	}

	public void setContent(String c) {
		this.content = c;
	}

	public String getCreatedAtStr() {
		return createdAt != null ? createdAt.format(FMT) : "";
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Long getTraineeId() {
		return traineeId;
	}

	public void setTraineeId(Long traineeId) {
		this.traineeId = traineeId;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	/** 좌표가 유효할 때만 true (상세 지도 표시용) */
	public boolean isHasMapLocation() {
		return lat != null && lng != null && !lat.isNaN() && !lng.isNaN() && !lat.isInfinite()
				&& !lng.isInfinite();
	}

	public void clearLocation() {
		this.placeName = null;
		this.address = null;
		this.lat = null;
		this.lng = null;
	}

	public LocalDateTime getEventAt() {
		return eventAt;
	}

	public void setEventAt(LocalDateTime eventAt) {
		this.eventAt = eventAt;
	}

	public String getRecruitStatus() {
		return recruitStatus;
	}

	public void setRecruitStatus(String recruitStatus) {
		this.recruitStatus = recruitStatus;
	}

	public Integer getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(Integer maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public String getParticipationType() {
		return participationType;
	}

	public void setParticipationType(String participationType) {
		this.participationType = participationType;
	}

	public boolean isFanMeetApproved() {
		return fanMeetApproved;
	}

	public void setFanMeetApproved(boolean fanMeetApproved) {
		this.fanMeetApproved = fanMeetApproved;
	}

	public String getEventAtStr() {
		return eventAt != null ? eventAt.format(FMT) : "";
	}

	/** datetime-local input value */
	public String getEventAtInputValue() {
		return eventAt != null ? eventAt.format(INPUT_DT) : "";
	}

	public boolean isRecruitOpen() {
		return recruitStatus == null || "OPEN".equalsIgnoreCase(recruitStatus)
				|| "RECRUITING".equalsIgnoreCase(recruitStatus);
	}

	public String getRecruitStatusLabel() {
		if (recruitStatus == null) {
			return "모집중";
		}
		return switch (recruitStatus.toUpperCase()) {
		case "PLANNED" -> "예정";
		case "DONE", "CLOSED" -> "완료";
		case "RECRUITING", "OPEN" -> "모집중";
		default -> "모집중";
		};
	}

	/** 팬미팅 UI·지도 핀 스타일용: RECRUITING / PLANNED / DONE */
	public String getFanMeetingStatusKey() {
		if (recruitStatus == null) {
			return "RECRUITING";
		}
		return switch (recruitStatus.toUpperCase()) {
			case "PLANNED" -> "PLANNED";
			case "DONE", "CLOSED" -> "DONE";
			case "RECRUITING", "OPEN" -> "RECRUITING";
			default -> "RECRUITING";
		};
	}

	public String getFanMeetScheduleLabel() {
		if (eventAt == null) {
			return "일정 미정";
		}
		LocalDate d = eventAt.toLocalDate();
		long days = ChronoUnit.DAYS.between(LocalDate.now(), d);
		if (days > 0) {
			return "D-" + days;
		}
		if (days == 0) {
			return "D-Day";
		}
		return "지난 일정";
	}

	public String getParticipationTypeLabel() {
		if (participationType == null) {
			return "";
		}
		return switch (participationType) {
		case "FIRST_COME" -> "선착순";
		case "LOTTERY" -> "추첨";
		case "CONTACT" -> "문의 후 참여";
		case "FREE" -> "자유 참여";
		default -> participationType;
		};
	}

	public String getCategoryLabel() {
		if (category == null)
			return "";
		return switch (category) {
		case "bug" -> "버그";
		case "report" -> "신고";
		default -> category;
		};
	}

	public String getEffectType() {
		return effectType;
	}

	public void setEffectType(String effectType) {
		this.effectType = effectType;
	}

	public String getEffectValue() {
		return effectValue;
	}

	public void setEffectValue(String effectValue) {
		this.effectValue = effectValue;
	}

	public LocalDateTime getEventStartAt() {
		return eventStartAt;
	}

	public void setEventStartAt(LocalDateTime eventStartAt) {
		this.eventStartAt = eventStartAt;
	}

	public LocalDateTime getEventEndAt() {
		return eventEndAt;
	}

	public void setEventEndAt(LocalDateTime eventEndAt) {
		this.eventEndAt = eventEndAt;
	}

	public boolean isEventActive() {
		return eventActive;
	}

	public void setEventActive(boolean eventActive) {
		this.eventActive = eventActive;
	}

	public boolean isBannerEnabled() {
		return bannerEnabled;
	}

	public void setBannerEnabled(boolean bannerEnabled) {
		this.bannerEnabled = bannerEnabled;
	}

	/** 캐스팅 이벤트 기간 표시 (event_start_at ~ event_end_at) */
	public String getCastingEventPeriodStr() {
		if (eventStartAt == null || eventEndAt == null) {
			return "";
		}
		return eventStartAt.format(FMT) + " ~ " + eventEndAt.format(FMT);
	}

	/**
	 * 이벤트 UI 상태: 진행중 / 예정 / 종료 (캐스팅 이벤트 기간 기준)
	 */
	public String getCastingEventStatusLabel() {
		if (!eventActive || effectType == null || effectType.isBlank()) {
			return "종료";
		}
		if (eventStartAt == null || eventEndAt == null) {
			return "종료";
		}
		LocalDateTime now = LocalDateTime.now();
		if (now.isBefore(eventStartAt)) {
			return "예정";
		}
		if (now.isAfter(eventEndAt)) {
			return "종료";
		}
		return "진행중";
	}

	public String getCastingEventRemainingShort() {
		if (eventEndAt == null) {
			return "";
		}
		LocalDateTime now = LocalDateTime.now();
		if (!now.isBefore(eventEndAt)) {
			return "종료됨";
		}
		long days = ChronoUnit.DAYS.between(now.toLocalDate(), eventEndAt.toLocalDate());
		if (days > 0) {
			return days + "일 남음";
		}
		long hours = ChronoUnit.HOURS.between(now, eventEndAt);
		if (hours > 0) {
			return hours + "시간 남음";
		}
		long mins = ChronoUnit.MINUTES.between(now, eventEndAt);
		return Math.max(1, mins) + "분 남음";
	}

	/** 상세·가챠 배너용 효과 한 줄 설명 */
	public String getCastingEffectSummaryLine() {
		CastingEventEffectType t = CastingEventEffectType.fromDb(effectType);
		if (t == null) {
			return "";
		}
		int bp = parseEffectBasisPoints();
		switch (t) {
		case SSR_UP:
			return "SSR 확률 +" + (bp / 100.0) + "% 증가";
		case SR_UP:
			return "SR 확률 +" + (bp / 100.0) + "% 증가";
		case POSITION_PICKUP:
			return "포지션 " + positionLabel(effectValue) + " 출현 가중치 증가";
		case DISCOUNT_PULL:
			return "뽑기 코인 " + parseDiscountPercent() + "% 할인";
		case BONUS_PULL:
			return "5회 뽑기 시 1회 추가 지급";
		default:
			return "";
		}
	}

	public List<String> getCastingEffectBadgeList() {
		CastingEventEffectType t = CastingEventEffectType.fromDb(effectType);
		if (t == null) {
			return List.of();
		}
		return switch (t) {
		case SSR_UP -> List.of("SSR UP");
		case SR_UP -> List.of("SR UP");
		case POSITION_PICKUP -> List.of(positionLabel(effectValue));
		case DISCOUNT_PULL -> List.of("할인중");
		case BONUS_PULL -> List.of("5+1");
		};
	}

	private static int parseBpOrDefault(String raw, int defBp) {
		if (raw == null || raw.isBlank()) {
			return defBp;
		}
		try {
			return Math.max(0, Integer.parseInt(raw.trim()));
		} catch (NumberFormatException e) {
			return defBp;
		}
	}

	/** SSR/SR 업 — 베이시스 포인트 (예: 200 = 2%) */
	public int parseEffectBasisPoints() {
		CastingEventEffectType t = CastingEventEffectType.fromDb(effectType);
		if (t == CastingEventEffectType.SSR_UP || t == CastingEventEffectType.SR_UP) {
			return parseBpOrDefault(effectValue, 200);
		}
		return 0;
	}

	public int parseDiscountPercent() {
		if (CastingEventEffectType.fromDb(effectType) != CastingEventEffectType.DISCOUNT_PULL) {
			return 0;
		}
		try {
			if (effectValue == null || effectValue.isBlank()) {
				return 10;
			}
			int p = Integer.parseInt(effectValue.trim());
			return Math.max(0, Math.min(90, p));
		} catch (NumberFormatException e) {
			return 10;
		}
	}

	private static String positionLabel(String code) {
		if (code == null || code.isBlank()) {
			return "—";
		}
		return switch (code.trim().toUpperCase()) {
		case "VOCAL" -> "보컬";
		case "DANCE" -> "댄스";
		case "STAR" -> "스타성";
		case "MENTAL" -> "멘탈";
		case "TEAMWORK" -> "팀워크";
		default -> code.trim();
		};
	}

	/** 목록용: 지역(장소명) */
	public String getRegionLabel() {
		return placeName != null && !placeName.isBlank() ? placeName : "—";
	}
}