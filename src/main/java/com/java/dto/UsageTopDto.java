package com.java.dto;

public record UsageTopDto(
		String name,
		long usageCount,
		long totalUsage,
		long userCount) {
}
