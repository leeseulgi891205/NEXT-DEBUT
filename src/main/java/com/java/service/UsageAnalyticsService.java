package com.java.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.java.dto.UsageFlowDto;
import com.java.dto.UsageKpiDto;
import com.java.dto.UsageLogDto;
import com.java.dto.UsageRatioDto;
import com.java.dto.UsageTopDto;

public interface UsageAnalyticsService {

	long getTodayUsageCoin();

	long getTodayUsageCount();

	long getUsageUserCount();

	double getAverageUsage();

	List<UsageFlowDto> getUsageFlow(LocalDate from, LocalDate to);

	UsageRatioDto getUsageRatio(LocalDateTime from, LocalDateTime to);

	List<UsageTopDto> getShopUsageTop(LocalDateTime from, LocalDateTime to, int limit);

	List<UsageTopDto> getGachaUsageTop(LocalDateTime from, LocalDateTime to, int limit);

	List<UsageLogDto> getRecentUsageLogs(LocalDateTime from, LocalDateTime to, int limit);

	/** MARKET_TXN(소비) + 연습생 뽑기 로그 병합, 최신순 페이지 */
	List<UsageLogDto> getUsageLogsPage(LocalDateTime from, LocalDateTime to, int page, int pageSize);

	long countUsageLogs(LocalDateTime from, LocalDateTime to);

	UsageKpiDto getTodayKpi();
}
