package com.java.dto;

/**
 * 회원 코인 보유 구간 분포.
 */
public record CoinDistributionDto(
		String rangeLabel,
		long memberCount) {
}
