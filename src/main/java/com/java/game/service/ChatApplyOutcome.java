package com.java.game.service;

import java.util.List;

/**
 * 채팅 턴 결과: 키워드 분류, 스탯(서버), 캐릭터 대사(AI 또는 폴백).
 */
public record ChatApplyOutcome(
		String resolvedKey,
		String trainingCategory,
		StatChangeResult result,
		String dialogueSituation,
		String resultNarration,
		List<IdolChatLine> characterResponses,
		MiniGamePenalty miniGamePenalty,
		String predictedKey,
		double predictionConfidence,
		java.util.Map<String, Double> predictionScores,
		boolean usedFallback,
		String resolverType
) {
}
