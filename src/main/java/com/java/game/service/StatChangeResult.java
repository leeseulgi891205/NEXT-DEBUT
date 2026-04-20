package com.java.game.service;

/**
 * 선택지 적용 후 스탯/팬 변경 결과 DTO.
 * 팬: {@code coreFan*} = 국내, {@code casualFan*} = 해외(구 라이트 합산). {@code lightFan*}은 레거시 호환용(0).
 */
public record StatChangeResult(
		Long traineeId,
		String traineeName,
		String statName,
		int delta,
		int beforeVal,
		int afterVal,
		String nextPhase,
		java.util.List<RosterItem> updatedRoster,
		int fanDelta,
		int coreFanDelta,
		int casualFanDelta,
		int lightFanDelta,
		int totalFans,
		int coreFans,
		int casualFans,
		int lightFans,
		String fanReactionTitle,
		String fanReactionDesc,
		String unlockedEvent,
		String activeStatusCode,
		String activeStatusLabel,
		String activeStatusDesc,
		Integer activeStatusTurnsLeft,
		String statusEffectText
) {
}
