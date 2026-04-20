package com.java.dto;

public record GachaPullLineDto(
		long traineeId,
		String name,
		String grade,
		String imagePath,
		/** 이미 보유 중이었던 경우 true (중복 허용·보유 수 증가) */
		boolean duplicate,
		int ownedTotalAfter) {
}
