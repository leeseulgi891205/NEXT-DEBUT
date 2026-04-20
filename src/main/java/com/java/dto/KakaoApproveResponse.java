package com.java.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 카카오페이 결제 승인 응답(JSON). 역직렬화만 하고 필드는 참조하지 않을 수 있음.
 */
public record KakaoApproveResponse(
		@JsonProperty("aid") String aid,
		@JsonProperty("tid") String tid,
		@JsonProperty("cid") String cid,
		@JsonProperty("partner_order_id") String partnerOrderId,
		@JsonProperty("partner_user_id") String partnerUserId,
		@JsonProperty("item_name") String itemName,
		@JsonProperty("created_at") String createdAt,
		@JsonProperty("approved_at") String approvedAt) {
}
