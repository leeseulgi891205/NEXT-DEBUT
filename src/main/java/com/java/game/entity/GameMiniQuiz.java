package com.java.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "GAME_MINI_QUIZ")
public class GameMiniQuiz {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "HINT", nullable = false, length = 300)
	private String hint;

	@Column(name = "ANSWER", nullable = false, length = 120)
	private String answer;

	@Column(name = "SORT_ORDER", nullable = false)
	private int sortOrder;

	@Column(name = "ENABLED", nullable = false)
	private boolean enabled;

	protected GameMiniQuiz() {
	}

	public GameMiniQuiz(String hint, String answer, int sortOrder, boolean enabled) {
		this.hint = hint;
		this.answer = answer;
		this.sortOrder = sortOrder;
		this.enabled = enabled;
	}

	public Long getId() {
		return id;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
