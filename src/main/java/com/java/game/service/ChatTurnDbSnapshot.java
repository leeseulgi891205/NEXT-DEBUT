package com.java.game.service;

/**
 * 채팅 턴 DB 처리 직후 스냅샷 (AI 호출은 트랜잭션 밖에서).
 */
public record ChatTurnDbSnapshot(
		String resolvedKey,
		String trainingCategory,
		StatChangeResult statResult,
		String sceneTitle,
		String sceneDescription,
		Long sceneId,
		String userText,
		MiniGamePenalty miniGamePenalty,
		String predictedKey,
		double predictionConfidence,
		java.util.Map<String, Double> predictionScores,
		boolean usedFallback,
		String resolverType
) {
}
