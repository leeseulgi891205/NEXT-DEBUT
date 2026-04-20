package com.java.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 게임 선택지(Choice) 엔티티 씬 1개당 선택지 4개 (A/B/C/D) → 6씬 × 4 = 24개 + 스페셜 이벤트 선택지 12개 =
 * 총 36개
 */
@Entity
@Table(name = "GAME_CHOICE")
public class GameChoice {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	/** 어느 phase에 속한 선택지인지 */
	@Column(name = "PHASE", nullable = false, length = 30)
	private String phase;

	/** A / B / C / D / SPECIAL */
	@Column(name = "CHOICE_KEY", nullable = false, length = 10)
	private String choiceKey;

	/** 선택지 버튼에 표시되는 텍스트 */
	@Column(name = "CHOICE_TEXT", nullable = false, length = 200)
	private String choiceText;

	/**
	 * 이 선택지가 영향주는 스탯 VOCAL / DANCE / TEAMWORK / MENTAL / STAR
	 */
	@Column(name = "STAT_TARGET", nullable = false, length = 20)
	private String statTarget;

	/** 정렬 순서 (같은 phase 내에서 A=1, B=2, C=3, D=4) */
	@Column(name = "SORT_ORDER", nullable = false)
	private int sortOrder;

	protected GameChoice() {
	}

	public GameChoice(String phase, String choiceKey, String choiceText, String statTarget, int sortOrder) {
		this.phase = phase;
		this.choiceKey = choiceKey;
		this.choiceText = choiceText;
		this.statTarget = statTarget;
		this.sortOrder = sortOrder;
	}

	public Long getId() {
		return id;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public String getChoiceKey() {
		return choiceKey;
	}

	public void setChoiceKey(String choiceKey) {
		this.choiceKey = choiceKey;
	}

	public String getChoiceText() {
		return choiceText;
	}

	public String getStatTarget() {
		return statTarget;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setChoiceText(String choiceText) {
		this.choiceText = choiceText;
	}

	public void setStatTarget(String statTarget) {
		this.statTarget = statTarget;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
