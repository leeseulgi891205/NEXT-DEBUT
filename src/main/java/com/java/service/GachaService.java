package com.java.service;

import java.util.List;
import java.util.Map;

import com.java.dto.GachaPullResultDto;
import com.java.dto.MyTraineeSummaryDto;

public interface GachaService {

	/**
	 * 가챠 실행. 코인 차감·보유 반영·로그는 단일 트랜잭션.
	 *
	 * @param pulls 1, 5, 또는 10
	 * @param eventId 캐스팅 이벤트 게시글 ID(map), 없으면 null
	 */
	GachaPullResultDto pull(Long memberId, int pulls, String poolId, Long eventId);

	List<MyTraineeSummaryDto> listOwnedTrainees(Long memberId);

	/** 확률·가격 등 공개 설정 (UI) */
	default Map<String, Object> getPublicSettings() {
		return getPublicSettings(null, null);
	}

	/** 이벤트 ID가 있으면 할인·표시 확률 등 반영 */
	default Map<String, Object> getPublicSettings(Long eventId) {
		return getPublicSettings(eventId, null);
	}

	/**
	 * 이벤트 + 캐스팅 맵 스팟 버프 반영 (로그인 시 memberId).
	 */
	Map<String, Object> getPublicSettings(Long eventId, Long memberId);
}
