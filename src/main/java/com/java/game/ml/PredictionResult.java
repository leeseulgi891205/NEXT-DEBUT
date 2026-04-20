package com.java.game.ml;

import java.util.Map;

public record PredictionResult(
		String predictedKey,
		double confidence,
		Map<String, Double> scoreByKey,
		boolean fallbackRecommended,
		String resolverType
) {
}
