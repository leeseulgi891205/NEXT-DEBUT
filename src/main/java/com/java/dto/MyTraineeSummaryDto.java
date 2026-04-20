package com.java.dto;

public record MyTraineeSummaryDto(
		long traineeId,
		String name,
		String grade,
		String imagePath,
		int quantity,
		int enhanceLevel) {
}
