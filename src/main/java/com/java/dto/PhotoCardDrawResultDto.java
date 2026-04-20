package com.java.dto;

/**
 * 포토카드 1회 뽑기 결과.
 */
public record PhotoCardDrawResultDto(
		String result,
		String message,
		String grade,
		String displayName,
		Long traineeId,
		Integer currentCoin,
		String imagePath,
		String traineeName) {
}
