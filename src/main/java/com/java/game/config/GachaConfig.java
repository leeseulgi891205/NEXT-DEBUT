package com.java.game.config;

import java.util.LinkedHashMap;
import java.util.Map;

import com.java.game.entity.Grade;

/**
 * 가챠 풀·가격·등급 확률 설정. 운영 시 프로퍼티/DB로 이전하기 쉽도록 한곳에 모음.
 * 확률은 10,000분율(베이시스 포인트) 합계 10,000.
 */
public final class GachaConfig {

	public static final String DEFAULT_POOL_ID = "DEFAULT";

	/** 1회 뽑기 가격 (코인) */
	public static final int COIN_PRICE_SINGLE = 100;

	/** 5회 뽑기 가격 (코인) */
	public static final int COIN_PRICE_MULTI = 450;

	/** 10회 뽑기 가격 (코인) */
	public static final int COIN_PRICE_10 = 850;

	/** 5회 뽑기 횟수 */
	public static final int MULTI_PULL_COUNT = 5;

	/** 10회 뽑기 횟수 */
	public static final int PULL_COUNT_10 = 10;

	/** 등급별 가중치 (합 10,000) — N 55%, R 30%, SR 12%, SSR 3% */
	private static final Map<Grade, Integer> GRADE_WEIGHT_BP = new LinkedHashMap<>();

	static {
		GRADE_WEIGHT_BP.put(Grade.N, 5500);
		GRADE_WEIGHT_BP.put(Grade.R, 3000);
		GRADE_WEIGHT_BP.put(Grade.SR, 1200);
		GRADE_WEIGHT_BP.put(Grade.SSR, 300);
	}

	private GachaConfig() {
	}

	public static Map<Grade, Integer> getGradeWeightsBasisPoints() {
		return Map.copyOf(GRADE_WEIGHT_BP);
	}

	/** UI·API용: 등급 → 퍼센트 문자열 */
	public static Map<String, String> getGradeProbabilitiesPercent() {
		Map<String, String> m = new LinkedHashMap<>();
		for (var e : GRADE_WEIGHT_BP.entrySet()) {
			m.put(e.getKey().name(), String.format("%.2f", e.getValue() / 100.0));
		}
		return m;
	}

	public static int priceForPulls(int pulls) {
		return switch (pulls) {
		case 1 -> COIN_PRICE_SINGLE;
		case 5 -> COIN_PRICE_MULTI;
		case 10 -> COIN_PRICE_10;
		default -> throw new IllegalArgumentException("pulls must be 1, 5, or 10");
		};
	}
}
