package com.java.game.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GAME_RUN")
public class GameRun {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long runId;

	@Column(name = "GROUP_TYPE", nullable = false, length = 20)
	private String groupType;

	/** 플레이한 회원 mno (비로그인이면 null) */
	@Column(name = "PLAYER_MNO")
	private Long playerMno;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	// 선발 확정 여부
	@Column(name = "CONFIRMED", nullable = false)
	private boolean confirmed = false;

	// 현재 진행 단계:
	// DAY1_MORNING → ... → DAY56_EVENING → MID_EVAL → DAY57_MORNING → ... → DAY84_EVENING → DEBUT_EVAL → FINISHED
	@Column(name = "PHASE", nullable = false, length = 30)
	private String phase = "DAY1_MORNING";

	/** DAY2 이후 랜덤으로 선택된 씬 ID (phase 전환 시 초기화) */
	@Column(name = "CURRENT_SCENE_ID")
	private Long currentSceneId;

	/** MID_EVAL 결과 등급(S/A/B/C/D) */
	@Column(name = "MID_EVAL_TIER", length = 2)
	private String midEvalTier;

	/** MID_EVAL 버프/패널티 적용 종료 턴(포함) */
	@Column(name = "MID_EVAL_EFFECT_UNTIL_TURN")
	private Integer midEvalEffectUntilTurn;

	/** 피로도(0~100). 높을수록 하락 확률 증가/성장 둔화 */
	@Column(name = "FATIGUE", nullable = false)
	private int fatigue = 0;


	/** 팬 분포 */
	@Column(name = "CORE_FANS")
	private Integer coreFans = 0;

	@Column(name = "CASUAL_FANS")
	private Integer casualFans = 0;

	@Column(name = "LIGHT_FANS")
	private Integer lightFans = 0;

	/** 팬 이벤트 해금 플래그 비트마스크 */
	@Column(name = "FAN_EVENT_FLAGS")
	private Integer fanEventFlags = 0;

	/** 이 플레이에서 회원 rankExp(팬→경험치) 반영 여부 (엔딩 1회) */
	@Column(name = "FAN_REWARD_APPLIED", nullable = false)
	private boolean fanRewardApplied = false;

	/** 다시뽑기 남은 횟수 (0~3) */
	@Column(name = "REROLL_REMAINING", nullable = false)
	private int rerollRemaining = 3;

	/** 마지막 충전 기준 시각 (1시간당 +1) */
	@Column(name = "REROLL_LAST_AT")
	private LocalDateTime rerollLastAt;

	/** 클리어(FINISHED) 시각. 기존 데이터는 null일 수 있음(필터 시 CREATED_AT 등으로 보완). */
	@Column(name = "FINISHED_AT")
	private LocalDateTime finishedAt;

	/** 랭킹/대시보드 조회용 최종 표시 점수 캐시(0~1000). */
	@Column(name = "SCORE_CACHE")
	private Integer scoreCache;

	protected GameRun() {
	}

	public GameRun(String groupType) {
		this.groupType = groupType;
		this.createdAt = LocalDateTime.now();
		this.confirmed = false;
		this.phase = "DAY1_MORNING";
		this.rerollRemaining = 3;
		this.rerollLastAt = LocalDateTime.now();
	}

	public Long getRunId() {
		return runId;
	}

	public String getGroupType() {
		return groupType;
	}

	public Long getPlayerMno() {
		return playerMno;
	}

	public void setPlayerMno(Long mno) {
		this.playerMno = mno;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/** 데모 시드 등에서만 사용. 일반 플로우는 생성자 시각을 유지한다. */
	public void setCreatedAt(LocalDateTime createdAt) {
		if (createdAt != null) {
			this.createdAt = createdAt;
		}
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public String getPhase() {
		return phase;
	}

	/** 데모 시드·관리용. 일반 진행은 {@link #nextPhase()} 를 사용한다. */
	public void setPhase(String phase) {
		if (phase != null && !phase.isBlank()) {
			this.phase = phase;
		}
	}

	/**
	 * 단축 데뷔 등으로 엔딩 URL만 열었을 때 phase가 남아 있으면 클리어·팬 보상 집계가 되지 않는다.
	 * FINISHED가 아니면 강제로 종료 처리한다.
	 */
	public void forceFinishForEnding() {
		if (!"FINISHED".equals(this.phase)) {
			this.phase = "FINISHED";
			if (this.finishedAt == null) {
				this.finishedAt = LocalDateTime.now();
			}
		}
	}

	public void confirm() {
		this.confirmed = true;
	}

	public Long getCurrentSceneId() { return currentSceneId; }
	public void setCurrentSceneId(Long id) { this.currentSceneId = id; }
	public String getMidEvalTier() { return midEvalTier; }
	public void setMidEvalTier(String tier) { this.midEvalTier = tier; }
	public Integer getMidEvalEffectUntilTurn() { return midEvalEffectUntilTurn; }
	public void setMidEvalEffectUntilTurn(Integer turn) { this.midEvalEffectUntilTurn = turn; }
	public int getFatigue() { return fatigue; }
	public void setFatigue(int fatigue) { this.fatigue = clamp01to100(fatigue); }

	public int getRerollRemaining() { return rerollRemaining; }
	public void setRerollRemaining(int v) { this.rerollRemaining = Math.max(0, Math.min(3, v)); }
	public LocalDateTime getRerollLastAt() { return rerollLastAt; }
	public void setRerollLastAt(LocalDateTime t) { this.rerollLastAt = t; }

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(LocalDateTime finishedAt) {
		this.finishedAt = finishedAt;
	}

	public Integer getScoreCache() {
		return scoreCache;
	}

	public void setScoreCache(Integer scoreCache) {
		if (scoreCache == null) {
			this.scoreCache = null;
			return;
		}
		this.scoreCache = Math.max(0, Math.min(1000, scoreCache));
	}

	/** 1시간당 1개 충전 (최대 3). 변경되면 true */
	public boolean rechargeRerollIfNeeded(LocalDateTime now) {
		if (now == null) now = LocalDateTime.now();
		if (rerollLastAt == null) rerollLastAt = now;
		if (rerollRemaining >= 3) {
			// 꽉 차있으면 기준시각을 현재로 맞춰 과충전 방지
			rerollLastAt = now;
			return false;
		}
		long hours = ChronoUnit.HOURS.between(rerollLastAt, now);
		if (hours <= 0) return false;
		int before = rerollRemaining;
		int next = Math.min(3, before + (int) hours);
		rerollRemaining = next;
		// 충전된 만큼 기준 시간을 앞으로 당김
		rerollLastAt = rerollLastAt.plusHours(hours);
		// max에 도달하면 기준시각을 현재로 정리
		if (rerollRemaining >= 3) rerollLastAt = now;
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
		if (rerollRemaining >= 3) rerollLastAt = now; // 꽉 찬 상태에서 첫 사용 시 타이머 시작
		rerollRemaining = Math.max(0, rerollRemaining - 1);
		return true;
	}


	public int getCoreFans() { return coreFans == null ? 0 : coreFans; }
	public void setCoreFans(Integer coreFans) { this.coreFans = Math.max(0, coreFans == null ? 0 : coreFans); }
	public int getCasualFans() { return casualFans == null ? 0 : casualFans; }
	public void setCasualFans(Integer casualFans) { this.casualFans = Math.max(0, casualFans == null ? 0 : casualFans); }
	public int getLightFans() { return lightFans == null ? 0 : lightFans; }
	public void setLightFans(Integer lightFans) { this.lightFans = Math.max(0, lightFans == null ? 0 : lightFans); }
	public int getFanEventFlags() { return fanEventFlags == null ? 0 : fanEventFlags; }
	public void setFanEventFlags(Integer fanEventFlags) { this.fanEventFlags = fanEventFlags == null ? 0 : fanEventFlags; }
	public int getTotalFans() { return getCoreFans() + getCasualFans() + getLightFans(); }

	/** 해외 팬: CASUAL + LIGHT (LIGHT는 레거시 병합 전까지 합산). */
	public int getForeignFans() {
		return getCasualFans() + getLightFans();
	}

	/**
	 * 국내(CORE) / 해외(CASUAL) 팬만 변동. LIGHT 컬럼은 레거시이며 핫픽스로 CASUAL에 합산 후 0으로 둔다.
	 */
	public void applyFanDelta(int domesticDelta, int foreignDelta) {
		setCoreFans(getCoreFans() + domesticDelta);
		setCasualFans(getCasualFans() + foreignDelta);
	}

	public boolean isFanRewardApplied() {
		return fanRewardApplied;
	}

	public void setFanRewardApplied(boolean fanRewardApplied) {
		this.fanRewardApplied = fanRewardApplied;
	}

	private int clamp01to100(int v) { return Math.max(0, Math.min(100, v)); }

	/** 다음 단계로 진행 (랜덤 씬 ID 초기화) */
	public void nextPhase() {
		// currentSceneId는 "마지막으로 보여준 씬" 용도로 유지 (즉시 반복 방지에 사용)
		if ("MID_EVAL".equals(this.phase)) {
			this.phase = "DAY57_MORNING";
			return;
		}
		if ("DEBUT_EVAL".equals(this.phase)) {
			this.phase = "FINISHED";
			if (this.finishedAt == null) {
				this.finishedAt = LocalDateTime.now();
			}
			return;
		}
		PhaseParts p = parseOrNull(this.phase);
		if (p == null) {
			this.phase = "FINISHED";
			if (this.finishedAt == null) {
				this.finishedAt = LocalDateTime.now();
			}
			return;
		}
		if (p.day == 56 && p.isEvening) {
			this.phase = "MID_EVAL";
			return;
		}
		if (p.day == 84 && p.isEvening) {
			this.phase = "DEBUT_EVAL";
			return;
		}
		if (!p.isEvening) {
			this.phase = "DAY" + p.day + "_EVENING";
		} else {
			this.phase = "DAY" + (p.day + 1) + "_MORNING";
		}
	}

	private PhaseParts parseOrNull(String phase) {
		if (phase == null) return null;
		if (!phase.startsWith("DAY")) return null;
		int us = phase.indexOf('_');
		if (us <= 3) return null;
		String dayStr = phase.substring(3, us);
		String part = phase.substring(us + 1);
		try {
			int day = Integer.parseInt(dayStr);
			boolean isEvening = "EVENING".equals(part);
			boolean isMorning = "MORNING".equals(part);
			if (!isEvening && !isMorning) return null;
			if (day < 1 || day > 84) return null;
			return new PhaseParts(day, isEvening);
		} catch (Exception e) {
			return null;
		}
	}

	private static class PhaseParts {
		final int day;
		final boolean isEvening;
		PhaseParts(int day, boolean isEvening) {
			this.day = day;
			this.isEvening = isEvening;
		}
	}
}