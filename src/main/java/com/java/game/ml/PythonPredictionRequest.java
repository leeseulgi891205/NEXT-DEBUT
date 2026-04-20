package com.java.game.ml;

import java.util.List;

public record PythonPredictionRequest(
		String userText,
		Long sceneId,
		String phase,
		List<ChoicePayload> choices
) {
}
