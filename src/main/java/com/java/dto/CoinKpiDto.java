package com.java.dto;

/**
 * 관리자 코인 운영 KPI 요약.
 */
public record CoinKpiDto(
		long todayChargeCoins,
		long todayUsedCoins,
		long totalCirculatingCoins,
		long todayNetIncrease) {
}
