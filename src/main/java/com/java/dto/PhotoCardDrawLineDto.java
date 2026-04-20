package com.java.dto;

/**
 * 포토카드 1줄 뽑기 결과 (다회 뽑기·배치 응답용).
 */
public record PhotoCardDrawLineDto(
		String result,
		String message,
		String grade,
		String displayName,
		Long traineeId,
		String imagePath,
		String traineeName) {
}
