package com.java.dto;

/**
 * 코인 흐름 차트 1포인트.
 */
public record CoinFlowDto(
		String label,
		long chargeCoins,
		long usedCoins,
		long netIncreaseCoins) {
}
