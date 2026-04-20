package com.java.entity;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 회원 등급 (누적 rankExp 기준). 최소 필요 경험치는 오름차순으로 정의한다.
 */
public enum MemberRank {

	ROOKIE(0, "루키"),
	TRAINEE(300, "트레이니"),
	RISING_STAR(800, "라이징 스타"),
	IDOL(1800, "아이돌"),
	SUPERSTAR(3500, "슈퍼스타"),
	LEGEND(6000, "레전드"),
	MYTHIC(10000, "미스틱");

	private static final MemberRank[] BY_DESCENDING_MIN_EXP = Arrays.stream(values())
			.sorted(Comparator.comparingInt(MemberRank::minExp).reversed())
			.toArray(MemberRank[]::new);

	private final int minExp;
	private final String displayName;

	MemberRank(int minExp, String displayName) {
		this.minExp = minExp;
		this.displayName = displayName;
	}

	public int minExp() {
		return minExp;
	}

	public String displayName() {
		return displayName;
	}

	/**
	 * 누적 경험치로 현재 등급을 반환한다. (경험치는 차감하지 않는 누적형)
	 */
	public static MemberRank getRankByExp(int exp) {
		int e = Math.max(0, exp);
		for (MemberRank r : BY_DESCENDING_MIN_EXP) {
			if (e >= r.minExp) {
				return r;
			}
		}
		return ROOKIE;
	}

	public static MemberRank fromCode(String code) {
		if (code == null || code.isBlank()) {
			return ROOKIE;
		}
		try {
			return MemberRank.valueOf(code.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			return ROOKIE;
		}
	}

	public static int fanToRankExpDelta(int fanCount) {
		return Math.max(0, fanCount) / 10;
	}

	/** 바로 위 등급(더 높은 티어 한 단계). 최고 등급이면 null */
	public MemberRank nextTier() {
		int idx = ordinal() + 1;
		MemberRank[] v = values();
		return idx < v.length ? v[idx] : null;
	}

	/**
	 * 현재 티어 구간에서 다음 티어까지의 진행률(0~100).
	 * 최고 등급이면 막대는 100%로 표시하는 용도({@link NextTierProgress#maxTier()}).
	 */
	public static NextTierProgress nextTierProgress(int totalExp) {
		int exp = Math.max(0, totalExp);
		MemberRank cur = getRankByExp(exp);
		int floor = cur.minExp();
		MemberRank nx = cur.nextTier();
		if (nx == null) {
			return new NextTierProgress(100, exp, 0, true, cur.displayName(), null);
		}
		int nextFloor = nx.minExp();
		int span = nextFloor - floor;
		int untilNext = Math.max(0, nextFloor - exp);
		if (span <= 0) {
			return new NextTierProgress(100, exp, 0, true, cur.displayName(), nx.displayName());
		}
		long numer = (long) exp - floor;
		int pct = (int) Math.min(100L, Math.max(0L, (numer * 100L) / span));
		return new NextTierProgress(pct, exp, untilNext, false, cur.displayName(), nx.displayName());
	}

	/**
	 * @param barPercent 현재 티어 내 다음 등급까지 진행률
	 * @param expUntilNext 다음 티어 최소 exp까지 남은 양 (최고 등급이면 0)
	 * @param maxTier 최고 등급 여부
	 */
	public record NextTierProgress(
			int barPercent,
			int totalExp,
			int expUntilNext,
			boolean maxTier,
			String currentTierLabel,
			String nextTierLabel
	) {
	}
}
