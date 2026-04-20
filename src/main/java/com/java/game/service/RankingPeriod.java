package com.java.game.service;

import java.util.Locale;

/**
 * 랭킹 페이지 기간 필터(전체 / 이번 주 / 이번 달).
 */
public enum RankingPeriod {
	ALL,
	WEEK,
	MONTH;

	public static RankingPeriod fromParam(String raw) {
		if (raw == null || raw.isBlank()) {
			return ALL;
		}
		String s = raw.trim().toLowerCase(Locale.ROOT);
		return switch (s) {
			case "week", "weekly", "w" -> WEEK;
			case "month", "monthly", "m" -> MONTH;
			default -> ALL;
		};
	}
}
