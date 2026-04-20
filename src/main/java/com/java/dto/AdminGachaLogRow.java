package com.java.dto;

/**
 * 관리자 뽑기 로그 테이블 표시용.
 */
public record AdminGachaLogRow(
		long id,
		long memberId,
		String memberNickname,
		long traineeId,
		String traineeName,
		String grade,
		String poolId,
		String createdAtStr) {
}
