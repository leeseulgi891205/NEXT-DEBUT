package com.java.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "member", uniqueConstraints = { @UniqueConstraint(name = "uk_member_mid", columnNames = "mid"),
		@UniqueConstraint(name = "uk_member_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_member_nickname", columnNames = "nickname") })
public class Member {

	@Id
	// H2/Oracle 모두 호환되는 AUTO 전략
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long mno;

	@Column(nullable = false, length = 50)
	private String mid;

	@Column(nullable = false, length = 100)
	private String mpw;

	@Column(nullable = false, length = 50)
	private String mname;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Column(nullable = false, length = 120)
	private String email;

	@Column(length = 30)
	private String phone;

	@Column(length = 255)
	private String address;

	@Column(name = "address_detail", length = 255)
	private String addressDetail;

	// 주민번호는 암호화(ENC:...) 문자열로 저장되므로 길이를 충분히 확보
	@Column(length = 255)
	private String jumin;

	/** 프로필 이미지 저장 파일명 (없으면 null) */
	@Column(name = "PROFILE_IMAGE", length = 255)
	private String profileImage;

	/** 권한: USER(기본) / ADMIN */
	@Column(name = "role", nullable = false, length = 20)
	private String role;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	/** 계정 정지 만료 시각 (null 이면 활성) */
	@Column(name = "SUSPENDED_UNTIL")
	private LocalDateTime suspendedUntil;

	/** 다시뽑기 남은 횟수 (계정 기준, 0~3) */
	@Column(name = "REROLL_REMAINING", nullable = false)
	private int rerollRemaining = 3;

	/** 마지막 충전 기준 시각 (1시간당 +1) */
	@Column(name = "REROLL_LAST_AT")
	private LocalDateTime rerollLastAt;

	/** 누적 회원 등급 경험치 (팬 기반, 차감 없음) */
	@Column(name = "RANK_EXP", nullable = false)
	private int rankExp = 0;

	/** 현재 등급 코드 (MemberRank.name(), 서버에서 rankExp 기준으로 갱신) */
	@Column(name = "MEMBER_RANK", nullable = false, length = 32)
	private String memberRankCode = MemberRank.ROOKIE.name();

	/** 그룹 해금 비트마스크 (기본: RIIZE + HEARTS2HEARTS) */
	@Column(name = "GROUP_UNLOCK_MASK", nullable = false)
	private int groupUnlockMask = 5;

	/** 계정 진행 버전 (마이그레이션 1회 적용용) */
	@Column(name = "PROGRESS_VERSION", nullable = false)
	private int progressVersion = 2;

	/** 마이페이지 플레이 기록 대표 연습생 (보유 연습생만, MY_TRAINEE 기준) */
	@Column(name = "MYPAGE_REP_TRAINEE_ID")
	private Long mypageRepTraineeId;

	/** 마이페이지 프로필 카드에 표시할 연습생 (보유 연습생만) */
	@Column(name = "MYPAGE_CARD_TRAINEE_ID")
	private Long mypageCardTraineeId;

	@PrePersist
	private void onCreate() {
		if (createdAt == null)
			createdAt = LocalDateTime.now();
		if (role == null || role.isBlank())
			role = "USER";
		if (rerollRemaining < 0 || rerollRemaining > 3)
			rerollRemaining = 3;
		if (rerollLastAt == null)
			rerollLastAt = LocalDateTime.now();
		if (memberRankCode == null || memberRankCode.isBlank())
			memberRankCode = MemberRank.ROOKIE.name();
		if (rankExp < 0)
			rankExp = 0;
		if (groupUnlockMask <= 0)
			groupUnlockMask = 5;
		if (progressVersion <= 0)
			progressVersion = 2;
	}

	public Long getMno() {
		return mno;
	}

	public void setMno(Long mno) {
		this.mno = mno;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getMpw() {
		return mpw;
	}

	public void setMpw(String mpw) {
		this.mpw = mpw;
	}

	public String getMname() {
		return mname;
	}

	public void setMname(String mname) {
		this.mname = mname;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}

	public String getJumin() {
		return jumin;
	}

	public void setJumin(String jumin) {
		this.jumin = jumin;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getRerollRemaining() { return rerollRemaining; }
	public void setRerollRemaining(int v) { this.rerollRemaining = Math.max(0, Math.min(3, v)); }
	public LocalDateTime getRerollLastAt() { return rerollLastAt; }
	public void setRerollLastAt(LocalDateTime t) { this.rerollLastAt = t; }
	public LocalDateTime getSuspendedUntil() {
		return suspendedUntil;
	}
	public void setSuspendedUntil(LocalDateTime suspendedUntil) {
		this.suspendedUntil = suspendedUntil;
	}

	public int getRankExp() {
		return rankExp;
	}

	public void setRankExp(int rankExp) {
		this.rankExp = Math.max(0, rankExp);
	}

	public String getMemberRankCode() {
		return memberRankCode;
	}

	public void setMemberRankCode(String memberRankCode) {
		this.memberRankCode = (memberRankCode == null || memberRankCode.isBlank())
				? MemberRank.ROOKIE.name()
				: memberRankCode.trim();
	}

	public int getGroupUnlockMask() {
		return groupUnlockMask;
	}

	public void setGroupUnlockMask(int groupUnlockMask) {
		this.groupUnlockMask = Math.max(0, groupUnlockMask);
	}

	public int getProgressVersion() {
		return progressVersion;
	}

	public void setProgressVersion(int progressVersion) {
		this.progressVersion = Math.max(1, progressVersion);
	}

	public Long getMypageRepTraineeId() {
		return mypageRepTraineeId;
	}

	public void setMypageRepTraineeId(Long mypageRepTraineeId) {
		this.mypageRepTraineeId = mypageRepTraineeId;
	}

	public Long getMypageCardTraineeId() {
		return mypageCardTraineeId;
	}

	public void setMypageCardTraineeId(Long mypageCardTraineeId) {
		this.mypageCardTraineeId = mypageCardTraineeId;
	}

	/** 1시간당 1개 충전 (최대 3). 변경되면 true */
	public boolean rechargeRerollIfNeeded(LocalDateTime now) {
		if (now == null) now = LocalDateTime.now();
		if (rerollLastAt == null) rerollLastAt = now;
		if (rerollRemaining >= 3) return false;
		long hours = ChronoUnit.HOURS.between(rerollLastAt, now);
		if (hours <= 0) return false;
		int before = rerollRemaining;
		int next = Math.min(3, before + (int) hours);
		rerollRemaining = next;
		rerollLastAt = rerollLastAt.plusHours(hours);
		return next != before;
	}

	/** 다음 충전 시각 (없으면 null) */
	public LocalDateTime getNextRerollChargeAt(LocalDateTime now) {
		if (now == null) now = LocalDateTime.now();
		if (rerollRemaining >= 3) return null;
		if (rerollLastAt == null) return now.plusHours(1);
		return rerollLastAt.plusHours(1);
	}

	/** 다음 충전까지 남은 초 (풀충전이면 0) */
	public long getSecondsUntilNextReroll(LocalDateTime now) {
		if (now == null) now = LocalDateTime.now();
		LocalDateTime next = getNextRerollChargeAt(now);
		if (next == null) return 0L;
		long seconds = ChronoUnit.SECONDS.between(now, next);
		return Math.max(0L, seconds);
	}

	/** 다시뽑기 1회 사용. (부족하면 false) */
	public boolean consumeReroll(LocalDateTime now) {
		if (now == null) now = LocalDateTime.now();
		rechargeRerollIfNeeded(now);
		if (rerollRemaining <= 0) return false;
		// 3에서 첫 사용 시 타이머 시작
		if (rerollRemaining >= 3) rerollLastAt = now;
		rerollRemaining = Math.max(0, rerollRemaining - 1);
		return true;
	}

	/** JSP에서 ${member.createdAtStr} 로 포맷된 날짜 사용 */
	public String getCreatedAtStr() {
		return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
	}

	/** MM/dd 포맷 (어드민 차트용) */
	public String getCreatedAtDay() {
		return createdAt != null ? createdAt.format(DateTimeFormatter.ofPattern("MM/dd")) : "";
	}

	public boolean isSuspendedNow() {
		return suspendedUntil != null && suspendedUntil.isAfter(LocalDateTime.now());
	}

	public String getSuspendedUntilStr() {
		return suspendedUntil != null ? suspendedUntil.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
	}

	public long getSuspendRemainingDays() {
		if (!isSuspendedNow()) {
			return 0L;
		}
		long days = ChronoUnit.DAYS.between(LocalDateTime.now(), suspendedUntil);
		return Math.max(1L, days + 1L);
	}

	public String getAccountStatusLabel() {
		if (isSuspendedNow()) {
			return getSuspendRemainingDays() + "일 정지";
		}
		return "활성";
	}
}