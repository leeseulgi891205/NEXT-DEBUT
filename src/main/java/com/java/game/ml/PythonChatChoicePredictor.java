package com.java.game.ml;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.java.game.service.SceneResult;

@Component
public class PythonChatChoicePredictor implements ChatChoicePredictor {

	private static final Logger log = LoggerFactory.getLogger(PythonChatChoicePredictor.class);
	private static final PredictionResult DISABLED_RESULT = new PredictionResult(null, 0.0, Map.of(), true, "RULE");

	private final boolean enabled;
	private final String predictUrl;
	private final RestTemplate restTemplate;

	public PythonChatChoicePredictor(
			@Value("${app.ml.enabled:true}") boolean enabled,
			@Value("${app.ml.predict-url:http://localhost:8000/predict-choice}") String predictUrl,
			@Value("${app.ml.timeout-ms:2000}") long timeoutMs) {
		this.enabled = enabled;
		this.predictUrl = predictUrl;
		long safeTimeout = timeoutMs > 0 ? timeoutMs : 2000;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout((int) Duration.ofMillis(safeTimeout).toMillis());
		requestFactory.setReadTimeout((int) Duration.ofMillis(safeTimeout).toMillis());
		this.restTemplate = new RestTemplate(requestFactory);
	}

	@Override
	public PredictionResult predict(String userText, List<SceneResult.ChoiceItem> choices, Long sceneId, String phase) {
		if (!enabled) {
			return DISABLED_RESULT;
		}
		if (predictUrl == null || predictUrl.isBlank()) {
			log.warn("ML predict-url 미설정으로 RULE fallback 사용");
			return DISABLED_RESULT;
		}
		try {
			List<ChoicePayload> payloadChoices = choices == null ? List.of()
					: choices.stream()
							.filter(Objects::nonNull)
							.map(c -> new ChoicePayload(
									normalizeKey(c.getKey()),
									safeText(c.getText()),
									safeText(c.getStatTarget())))
							.toList();
			PythonPredictionRequest request = new PythonPredictionRequest(
					safeText(userText),
					sceneId,
					safeText(phase),
					payloadChoices);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<PythonPredictionResponse> response = restTemplate.postForEntity(
					predictUrl,
					new HttpEntity<>(request, headers),
					PythonPredictionResponse.class);
			PythonPredictionResponse body = response.getBody();
			if (body == null) {
				log.warn("ML 응답 본문이 비어 있어 RULE fallback 사용");
				return DISABLED_RESULT;
			}
			String predictedKey = normalizeKey(body.predictedKey());
			Map<String, Double> normalizedScores = normalizeScores(body.scoreByKey());
			double confidence = sanitizeConfidence(body.confidence());
			boolean invalidPrediction = predictedKey == null || predictedKey.isBlank();
			return new PredictionResult(predictedKey, confidence, normalizedScores, invalidPrediction, "ML");
		} catch (Exception ex) {
			log.warn("ML 예측 호출 실패. RULE fallback 사용: {}", ex.getMessage());
			return DISABLED_RESULT;
		}
	}

	private static String safeText(String value) {
		return value == null ? "" : value.trim();
	}

	private static String normalizeKey(String key) {
		if (key == null) {
			return null;
		}
		String normalized = key.trim().toUpperCase(Locale.ROOT);
		return normalized.isBlank() ? null : normalized;
	}

	private static double sanitizeConfidence(double confidence) {
		if (!Double.isFinite(confidence)) {
			return 0.0;
		}
		if (confidence < 0.0) {
			return 0.0;
		}
		if (confidence > 1.0) {
			return 1.0;
		}
		return confidence;
	}

	private static Map<String, Double> normalizeScores(Map<String, Double> scoreByKey) {
		if (scoreByKey == null || scoreByKey.isEmpty()) {
			return Map.of();
		}
		Map<String, Double> normalized = new LinkedHashMap<>();
		for (Map.Entry<String, Double> entry : scoreByKey.entrySet()) {
			String key = normalizeKey(entry.getKey());
			Double value = entry.getValue();
			if (key == null || value == null || !Double.isFinite(value)) {
				continue;
			}
			normalized.put(key, value);
		}
		return normalized;
	}
}
