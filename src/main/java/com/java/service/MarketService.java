package com.java.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.java.dto.CoinDistributionDto;
import com.java.dto.CoinFlowDto;
import com.java.dto.CoinKpiDto;
import com.java.dto.CoinTxnLogDto;
import com.java.dto.MyItemDto;

public interface MarketService {

    /** 상점·로그인 시 계정당 최소 보유 코인 */
    int DEFAULT_MIN_COIN = 1000;

    /** 보유 코인이 {@code minimum} 미만이면 DB를 {@code minimum}으로 맞춤 (상점/로그인 기본 지급) */
    void ensureMinimumCoin(Long memberId, int minimum);

    String buyItem(Long memberId, String itemName, int price);
    List<MyItemDto> getMyItems(Long memberId);
    int getCurrentCoin(Long memberId);
    List<MyItemDto> getMyItemsByIds(Long memberId, List<Long> itemIds);
    void useItems(Long memberId, List<Long> itemIds);
    void addCoin(Long memberId, int amount);

    /** 관리자: 전체 회원 보유 코인 합계 */
    long sumAllMemberCoins();

    /** 관리자: MY_ITEM 수량 합계(누적 보유 개수) */
    long sumAllItemQuantities();

    /** 관리자: 인벤토리가 있는 회원 수(중복 제외) */
    long countMembersWithMyItems();

    /** 관리자: 아이템별 전체 수량 상위 N (itemName, totalQty) */
    List<Map<String, Object>> topItemsByTotalQuantity(int limit);

    /** 관리자: 코인 보유 상위 회원 (mno, nickname, coin) */
    List<Map<String, Object>> topMembersByCoin(int limit);

    /** 카카오 등 충전 로그 (양수 코인) */
    void logCharge(Long memberId, int amount, String note);

    /** 상점 구매 1건 로그 (상품명·차감 코인) */
    void logPurchase(Long memberId, String itemName, int coinSpent);

    /** CHARGE 유형: 충전 코인 합계 (해당 시각 이후) */
    long sumChargedCoinsSince(LocalDateTime fromInclusive);

    /** BUY 유형: 상품별 구매 건수 상위 (itemName, purchaseCount) */
    List<Map<String, Object>> topPurchaseCountsByItem(int limit);

    /** 코인 운영 KPI (오늘 충전/사용/순증가 + 현재 총 유통 코인) */
    CoinKpiDto getCoinOpsKpi(LocalDateTime dayStartInclusive);

    /** 코인 흐름(일간:최근 N일) */
    List<CoinFlowDto> getCoinFlowDaily(int days);

    /** 코인 흐름(주간:최근 N주) */
    List<CoinFlowDto> getCoinFlowWeekly(int weeks);

    /** 코인 흐름(월간:최근 N개월) */
    List<CoinFlowDto> getCoinFlowMonthly(int months);

    /** 회원 코인 보유 분포 */
    List<CoinDistributionDto> getCoinDistribution();

    /** 최근 코인 거래 로그 (기간 필터 + 페이지네이션) */
    List<CoinTxnLogDto> getRecentCoinTxnLogs(LocalDateTime fromInclusive, int page, int pageSize);

    /** 최근 코인 거래 로그 총 개수 (기간 필터) */
    long countRecentCoinTxnLogs(LocalDateTime fromInclusive);
}
