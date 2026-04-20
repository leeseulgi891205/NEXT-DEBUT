package com.java.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 유저 채팅 텍스트의 키워드를 choiceKey(A/B/C/D/SPECIAL)로 매핑하는 DB 룰.
 */
@Entity
@Table(name = "CHAT_KEYWORD_RULE")
public class ChatKeywordRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "KEYWORD", nullable = false, length = 40)
	private String keyword;

	@Column(name = "CHOICE_KEY", nullable = false, length = 16)
	private String choiceKey;

	@Column(name = "PRIORITY", nullable = false)
	private int priority;

	@Column(name = "IS_ACTIVE", nullable = false)
	private boolean active = true;

	protected ChatKeywordRule() {
		// JPA 기본 생성자
	}

	public ChatKeywordRule(String keyword, String choiceKey, int priority, boolean active) {
		this.keyword = keyword;
		this.choiceKey = choiceKey;
		this.priority = priority;
		this.active = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getChoiceKey() {
		return choiceKey;
	}

	public void setChoiceKey(String choiceKey) {
		this.choiceKey = choiceKey;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}

