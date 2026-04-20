package com.java.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Gemini 미응답 시 사용하는 스탯별 상황 지문(운영에서 H2/DB로 수정 가능).
 */
@Entity
@Table(name = "NARRATION_FALLBACK_SITUATION")
public class NarrationFallbackSituation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "STAT_CATEGORY", nullable = false, length = 32)
	private String statCategory;

	@Column(name = "SITUATION_TEXT", nullable = false, length = 2048)
	private String situationText;

	@Column(name = "SORT_ORDER", nullable = false)
	private int sortOrder;

	protected NarrationFallbackSituation() {
		// JPA 기본 생성자
	}

	public NarrationFallbackSituation(String statCategory, String situationText, int sortOrder) {
		this.statCategory = statCategory;
		this.situationText = situationText;
		this.sortOrder = sortOrder;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatCategory() {
		return statCategory;
	}

	public void setStatCategory(String statCategory) {
		this.statCategory = statCategory;
	}

	public String getSituationText() {
		return situationText;
	}

	public void setSituationText(String situationText) {
		this.situationText = situationText;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
