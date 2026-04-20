package com.java.game.service;

import java.util.List;

/**
 * 현재 phase의 씬 정보 + 선택지 목록을 담는 DTO GameController → gamestart.jsp 로 전달됨 JSP EL
 * 호환을 위해 record 대신 일반 클래스 사용
 */
public class SceneResult {

	private final String phase;
	private final Long sceneId;
	private final String eventType;
	private final String title;
	private final String description;
	private final List<ChoiceItem> choices;

	public SceneResult(String phase, Long sceneId, String eventType, String title, String description,
			List<ChoiceItem> choices) {
		this.phase = phase;
		this.sceneId = sceneId;
		this.eventType = eventType;
		this.title = title;
		this.description = description;
		this.choices = choices;
	}

	public String getPhase() {
		return phase;
	}

	public Long getSceneId() {
		return sceneId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public List<ChoiceItem> getChoices() {
		return choices;
	}

	/** 선택지 단건 - JSP EL 호환 getter 방식 */
	public static class ChoiceItem {
		private final String key;
		private final String text;
		private final String statTarget;

		public ChoiceItem(String key, String text, String statTarget) {
			this.key = key;
			this.text = text;
			this.statTarget = statTarget;
		}

		public String getKey() {
			return key;
		}

		public String getText() {
			return text;
		}

		public String getStatTarget() {
			return statTarget;
		}
	}
}
