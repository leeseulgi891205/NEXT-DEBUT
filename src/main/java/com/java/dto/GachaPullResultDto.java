package com.java.dto;

import java.util.List;

public record GachaPullResultDto(
		String result,
		int currentCoin,
		List<GachaPullLineDto> pulls,
		String message) {

	public static GachaPullResultDto success(int currentCoin, List<GachaPullLineDto> pulls) {
		return new GachaPullResultDto("success", currentCoin, pulls, null);
	}

	public static GachaPullResultDto lack(int currentCoin) {
		return new GachaPullResultDto("lack", currentCoin, List.of(), "코인이 부족합니다.");
	}

	public static GachaPullResultDto fail(String message) {
		return new GachaPullResultDto("error", 0, List.of(), message);
	}
}
