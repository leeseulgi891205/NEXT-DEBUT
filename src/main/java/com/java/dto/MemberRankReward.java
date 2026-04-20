package com.java.dto;

/**
 * 플레이 종료 시 계정에 반영된 회원 등급(경험치) 변화 (엔딩 화면 표시용).
 */
public final class MemberRankReward {

	private final boolean eligible;
	private final int runTotalFans;
	private final int rankExpDelta;
	private final int rankExpBefore;
	private final int rankExpAfter;
	private final String rankCode;
	private final String rankDisplayName;
	private final boolean alreadyApplied;
	/** 엔딩 화면 분기용: GUEST(비로그인), PHASE_PENDING(아직 FINISHED 동기화 전) 등 */
	private final String detail;

	public MemberRankReward(boolean eligible, int runTotalFans, int rankExpDelta, int rankExpBefore, int rankExpAfter,
			String rankCode, String rankDisplayName, boolean alreadyApplied, String detail) {
		this.eligible = eligible;
		this.runTotalFans = runTotalFans;
		this.rankExpDelta = rankExpDelta;
		this.rankExpBefore = rankExpBefore;
		this.rankExpAfter = rankExpAfter;
		this.rankCode = rankCode;
		this.rankDisplayName = rankDisplayName;
		this.alreadyApplied = alreadyApplied;
		this.detail = detail;
	}

	public static MemberRankReward notLoggedIn(int runTotalFans) {
		return new MemberRankReward(false, runTotalFans, 0, 0, 0, null, null, false, "GUEST");
	}

	public static MemberRankReward notFinishedRun() {
		return new MemberRankReward(false, 0, 0, 0, 0, null, null, false, null);
	}

	/** FINISHED 전이거나 DB와 화면 phase가 어긋난 경우(팬 수는 표시) */
	public static MemberRankReward phasePending(int runTotalFans) {
		return new MemberRankReward(false, Math.max(0, runTotalFans), 0, 0, 0, null, null, false, "PHASE_PENDING");
	}

	public boolean isEligible() {
		return eligible;
	}

	public int getRunTotalFans() {
		return runTotalFans;
	}

	public int getRankExpDelta() {
		return rankExpDelta;
	}

	public int getRankExpBefore() {
		return rankExpBefore;
	}

	public int getRankExpAfter() {
		return rankExpAfter;
	}

	public String getRankCode() {
		return rankCode;
	}

	public String getRankDisplayName() {
		return rankDisplayName;
	}

	public boolean isAlreadyApplied() {
		return alreadyApplied;
	}

	public String getDetail() {
		return detail;
	}
}
