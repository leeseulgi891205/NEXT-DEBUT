package com.java.dto;

import java.util.List;

public record MemberOpsDetailDto(
		long coin,
		long traineeCount,
		long photoCardCount,
		List<String> traineeNames,
		List<String> photoCards
) {
}
