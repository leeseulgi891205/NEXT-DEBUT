package com.java.photocard.entity;

/**
 * 포토카드 등급 (연습생 "티어" Grade 와 별개).
 */
public enum PhotoCardGrade {
	R(5),
	SR(10),
	SSR(15);

	private final int bonusPercent;

	PhotoCardGrade(int bonusPercent) {
		this.bonusPercent = bonusPercent;
	}

	public int getBonusPercent() {
		return bonusPercent;
	}
}
