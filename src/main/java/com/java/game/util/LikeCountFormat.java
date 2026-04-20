package com.java.game.util;

import java.util.Locale;

/**
 * 연습생 좋아요 등 큰 수를 짧게 표시 (1000 이상: 1.3k, 2k).
 */
public final class LikeCountFormat {

	private LikeCountFormat() {
	}

	public static String compact(long n) {
		if (n < 0) {
			n = 0;
		}
		if (n < 1000) {
			return Long.toString(n);
		}
		double k = n / 1000.0;
		if (k == Math.floor(k)) {
			return String.format(Locale.US, "%.0fk", k);
		}
		return String.format(Locale.US, "%.1fk", k);
	}
}
