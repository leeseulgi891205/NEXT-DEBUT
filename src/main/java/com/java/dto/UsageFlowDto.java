package com.java.dto;

public record UsageFlowDto(
		String label,
		long total,
		long shop,
		long gacha) {
}
