package com.java.dto;

/**
 * 도감 연습생별 포토카드 보유·장착 요약.
 */
public record TraineePhotoCardSummaryDto(
		boolean ownedR,
		boolean ownedSr,
		boolean ownedSsr,
		String equippedGrade,
		int equippedBonusPercent) {
}
