package com.java.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오페이 결제 준비 응답 (JSON 필드명 스네이크 케이스).
 */
public record KakaoReadyResponse(
		@JsonProperty("tid") String tid,
		@JsonProperty("next_redirect_pc_url") String nextRedirectPcUrl,
		@JsonProperty("created_at") String createdAt) {
}
