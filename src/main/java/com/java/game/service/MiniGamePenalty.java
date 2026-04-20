package com.java.game.service;

/**
 * 채팅 전송 후 미니게임 실패 시 추가로 적용되는 스탯 패널티.
 */
public record MiniGamePenalty(
		Long traineeId,
		String traineeName,
		int pickOrder,
		String statName,
		int delta,
		int beforeVal,
		int afterVal
) {
}
