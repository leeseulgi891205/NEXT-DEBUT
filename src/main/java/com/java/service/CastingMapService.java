package com.java.service;

import java.util.Map;

public interface CastingMapService {

	/** 탐색 실행 — 실패·일반·희귀, 코인·일일 횟수 반영 */
	Map<String, Object> explore(Long memberId, String regionId);

	/** 맵 페이지용 오늘 남은 무료 횟수 등 */
	Map<String, Object> status(Long memberId);
}
