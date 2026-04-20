package com.java.game.ml;

import java.util.List;

import com.java.game.service.SceneResult;

public interface ChatChoicePredictor {

	PredictionResult predict(String userText, List<SceneResult.ChoiceItem> choices, Long sceneId, String phase);
}
