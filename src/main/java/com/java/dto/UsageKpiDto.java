package com.java.dto;

public record UsageKpiDto(
		long totalUsage,
		long usageCount,
		long userCount,
		double avgUsage,
		double shopRatio,
		double gachaRatio) {
}
