package com.java.entity;

/**
 * 캐스팅 이벤트 효과 — 자유 입력 금지, DB/폼은 이 값만 사용.
 */
public enum CastingEventEffectType {

	SSR_UP,
	SR_UP,
	POSITION_PICKUP,
	DISCOUNT_PULL,
	BONUS_PULL;

	public static CastingEventEffectType fromDb(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return CastingEventEffectType.valueOf(raw.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
