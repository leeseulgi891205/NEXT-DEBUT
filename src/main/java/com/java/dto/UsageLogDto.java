package com.java.dto;

public record UsageLogDto(
		String time,
		String user,
		String type,
		String itemName,
		long amount,
		String note) {
}
