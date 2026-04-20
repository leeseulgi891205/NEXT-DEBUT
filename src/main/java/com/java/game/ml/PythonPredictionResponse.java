package com.java.game.ml;

import java.util.Map;

public record PythonPredictionResponse(
		String predictedKey,
		double confidence,
		Map<String, Double> scoreByKey
) {
}
