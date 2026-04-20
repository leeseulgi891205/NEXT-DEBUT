package com.java.dto;

/**
 * 관리자 코인 거래 로그 행.
 */
public record CoinTxnLogDto(
		String createdAtStr,
		long memberId,
		String memberNickname,
		String txnType,
		int coinDelta,
		String note) {
}
