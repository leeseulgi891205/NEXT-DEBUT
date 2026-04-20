package com.java.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.dto.MyItemDto;
import com.java.dto.CoinDistributionDto;
import com.java.dto.CoinFlowDto;
import com.java.dto.CoinKpiDto;
import com.java.dto.CoinTxnLogDto;
import com.java.entity.MarketTxn;
import com.java.repository.MarketTxnRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class MarketServiceImpl implements MarketService {
    private static final DateTimeFormatter COIN_TXN_TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PersistenceContext
    private EntityManager em;

    private final MarketTxnRepository marketTxnRepository;

    public MarketServiceImpl(MarketTxnRepository marketTxnRepository) {
        this.marketTxnRepository = marketTxnRepository;
    }

    @Override
    @Transactional
    public void ensureMinimumCoin(Long memberId, int minimum) {
        if (memberId == null || minimum < 0) {
            return;
        }
        em.createNativeQuery(
                "UPDATE MEMBER SET COIN = :min WHERE MNO = :memberId AND COIN IS NULL")
                .setParameter("min", minimum)
                .setParameter("memberId", memberId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public String buyItem(Long memberId, String itemName, int price) {
        if (memberId == null || itemName == null || itemName.isBlank() || price < 0) {
            return "fail";
        }

        int updatedCoin = em.createNativeQuery(
                "UPDATE MEMBER SET COIN = COIN - :price WHERE MNO = :memberId AND COIN >= :price")
                .setParameter("price", price)
                .setParameter("memberId", memberId)
                .executeUpdate();

        if (updatedCoin <= 0) {
            return "lack";
        }

        List<String> purchaseItems = resolvePurchaseItems(itemName);
        for (String purchaseItem : purchaseItems) {
            addOrIncreaseItem(memberId, purchaseItem, 1);
        }

        logPurchase(memberId, itemName, price);
        return "success";
    }

    private List<String> resolvePurchaseItems(String itemName) {
        if ("올라운드 패키지 박스".equals(itemName)) {
            return List.of(
                    "보컬 워터",
                    "댄스 슈즈",
                    "팬레터",
                    "릴렉스 캔디",
                    "팀 스낵 박스");
        }
        return List.of(itemName);
    }

    private void addOrIncreaseItem(Long memberId, String itemName, int quantity) {
        Number exists = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM MY_ITEM WHERE MEMBER_ID = :memberId AND ITEM_NAME = :itemName")
                .setParameter("memberId", memberId)
                .setParameter("itemName", itemName)
                .getSingleResult();

        int count = exists == null ? 0 : exists.intValue();
        if (count > 0) {
            em.createNativeQuery(
                    "UPDATE MY_ITEM SET QUANTITY = COALESCE(QUANTITY, 0) + :qty WHERE MEMBER_ID = :memberId AND ITEM_NAME = :itemName")
                    .setParameter("qty", quantity)
                    .setParameter("memberId", memberId)
                    .setParameter("itemName", itemName)
                    .executeUpdate();
            return;
        }

        em.createNativeQuery(
                "INSERT INTO MY_ITEM (MEMBER_ID, ITEM_NAME, QUANTITY) VALUES (:memberId, :itemName, :qty)")
                .setParameter("memberId", memberId)
                .setParameter("itemName", itemName)
                .setParameter("qty", quantity)
                .executeUpdate();
    }

    @Override
    public List<MyItemDto> getMyItems(Long memberId) {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT id, member_id, item_name, quantity FROM my_item WHERE member_id = :memberId ORDER BY id DESC")
                    .setParameter("memberId", memberId)
                    .getResultList();
            List<MyItemDto> items = new ArrayList<>();
            for (Object[] row : rows) {
                MyItemDto dto = new MyItemDto();
                dto.setId(((Number) row[0]).longValue());
                dto.setMemberId(((Number) row[1]).longValue());
                dto.setItemName((String) row[2]);
                dto.setQuantity(((Number) row[3]).intValue());
                items.add(dto);
            }
            for (MyItemDto item : items) {
                item.setItemEffect(getItemEffect(item.getItemName()));
                item.setImagePath(getItemImage(item.getItemName()));
            }
            return items;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getItemEffect(String itemName) {
        switch (itemName) {
            case "보컬 워터": return "보컬 +10";
            case "호흡 컨트롤 북": return "보컬 +20";
            case "댄스 슈즈": return "댄스 +10";
            case "퍼포먼스 밴드": return "댄스 +20";
            case "팬레터": return "스타성 +10";
            case "라이브 방송 세트": return "스타성 +20";
            case "릴렉스 캔디": return "멘탈 +10";
            case "명상 키트": return "멘탈 +20";
            case "팀 스낵 박스": return "팀워크 +10";
            case "유닛 워크북": return "팀워크 +20";
            case "올라운드 패키지 박스": return "보컬 워터, 댄스 슈즈, 팬레터, 릴렉스 캔디, 팀 스낵 박스 각 1개 지급";
            default: return "효과 정보 없음";
        }
    }

    private String getItemImage(String itemName) {
        switch (itemName) {
            case "보컬 워터": return "/images/items/water.png";
            case "호흡 컨트롤 북": return "/images/items/breathe control.jpg";
            case "댄스 슈즈": return "/images/items/shoes.png";
            case "퍼포먼스 밴드": return "/images/items/band.png";
            case "팬레터": return "/images/items/letter.png";
            case "라이브 방송 세트": return "/images/items/live.png";
            case "릴렉스 캔디": return "/images/items/candy.png";
            case "명상 키트": return "/images/items/meditation.png";
            case "팀 스낵 박스": return "/images/items/snack-box.png";
            case "유닛 워크북": return "/images/items/workbook.png";
            case "올라운드 패키지 박스": return "/images/items/allpass.png";
            default: return "/images/items/star.png";
        }
    }

    @Override
    public int getCurrentCoin(Long memberId) {
        try {
            Number coin = (Number) em.createNativeQuery("SELECT COIN FROM MEMBER WHERE MNO = :memberId")
                    .setParameter("memberId", memberId)
                    .getSingleResult();
            return coin == null ? 0 : coin.intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public List<MyItemDto> getMyItemsByIds(Long memberId, List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        String in = itemIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT id, member_id, item_name, quantity FROM my_item WHERE member_id = :memberId AND id IN (" + in + ")")
                .setParameter("memberId", memberId)
                .getResultList();
        List<MyItemDto> items = new ArrayList<>();
        for (Object[] row : rows) {
            MyItemDto dto = new MyItemDto();
            dto.setId(((Number) row[0]).longValue());
            dto.setMemberId(((Number) row[1]).longValue());
            dto.setItemName((String) row[2]);
            dto.setQuantity(((Number) row[3]).intValue());
            items.add(dto);
        }
        for (MyItemDto item : items) {
            item.setItemEffect(getItemEffect(item.getItemName()));
            item.setImagePath(getItemImage(item.getItemName()));
        }
        return items;
    }

    @Override
    @Transactional
    public void useItems(Long memberId, List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) return;
        for (Long itemId : itemIds) {
            em.createNativeQuery(
                    "UPDATE my_item SET quantity = quantity - 1 WHERE member_id = :memberId AND id = :itemId AND quantity > 0")
                    .setParameter("memberId", memberId)
                    .setParameter("itemId", itemId)
                    .executeUpdate();
            em.createNativeQuery(
                    "DELETE FROM my_item WHERE member_id = :memberId AND id = :itemId AND quantity <= 0")
                    .setParameter("memberId", memberId)
                    .setParameter("itemId", itemId)
                    .executeUpdate();
        }
    }

    @Override
    @Transactional
    public void addCoin(Long memberId, int amount) {
        int currentCoin = getCurrentCoin(memberId);

        em.createNativeQuery("UPDATE MEMBER SET COIN = :coin WHERE MNO = :memberId")
          .setParameter("coin", currentCoin + amount)
          .setParameter("memberId", memberId)
          .executeUpdate();
    }

    @Override
    @Transactional
    public void logCharge(Long memberId, int amount, String note) {
        if (memberId == null || amount <= 0) {
            return;
        }
        marketTxnRepository.save(new MarketTxn(memberId, "CHARGE", null, amount, note != null ? note : ""));
    }

    @Override
    @Transactional
    public void logPurchase(Long memberId, String itemName, int coinSpent) {
        if (memberId == null || itemName == null || itemName.isBlank() || coinSpent <= 0) {
            return;
        }
        marketTxnRepository.save(new MarketTxn(memberId, "BUY", itemName.trim(), -coinSpent, null));
    }

    @Override
    @Transactional(readOnly = true)
    public long sumChargedCoinsSince(LocalDateTime fromInclusive) {
        if (fromInclusive == null) {
            return 0L;
        }
        return marketTxnRepository.sumCoinDeltaSince("CHARGE", fromInclusive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> topPurchaseCountsByItem(int limit) {
        int lim = Math.max(1, Math.min(limit, 50));
        List<Object[]> rows = marketTxnRepository.countPurchasesByItem(PageRequest.of(0, lim));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("itemName", row[0] != null ? row[0].toString() : "");
            m.put("purchaseCount", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            out.add(m);
        }
        return out;
    }

    @Override
    public long sumAllMemberCoins() {
        try {
            Number n = (Number) em.createNativeQuery("SELECT COALESCE(SUM(COIN), 0) FROM MEMBER").getSingleResult();
            return n == null ? 0L : n.longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public long sumAllItemQuantities() {
        try {
            Number n = (Number) em.createNativeQuery("SELECT COALESCE(SUM(QUANTITY), 0) FROM MY_ITEM").getSingleResult();
            return n == null ? 0L : n.longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public long countMembersWithMyItems() {
        try {
            Number n = (Number) em.createNativeQuery("SELECT COUNT(DISTINCT MEMBER_ID) FROM MY_ITEM").getSingleResult();
            return n == null ? 0L : n.longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> topItemsByTotalQuantity(int limit) {
        int lim = Math.max(1, Math.min(limit, 50));
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT ITEM_NAME, COALESCE(SUM(QUANTITY), 0) FROM MY_ITEM GROUP BY ITEM_NAME ORDER BY SUM(QUANTITY) DESC")
                    .setMaxResults(lim)
                    .getResultList();
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("itemName", row[0] != null ? row[0].toString() : "");
                m.put("totalQty", row[1] != null ? ((Number) row[1]).longValue() : 0L);
                out.add(m);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> topMembersByCoin(int limit) {
        int lim = Math.max(1, Math.min(limit, 50));
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT MNO, NICKNAME, COIN FROM MEMBER ORDER BY COIN DESC, MNO ASC")
                    .setMaxResults(lim)
                    .getResultList();
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object[] row : rows) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("mno", row[0] != null ? ((Number) row[0]).longValue() : 0L);
                m.put("nickname", row[1] != null ? row[1].toString() : "");
                m.put("coin", row[2] != null ? ((Number) row[2]).longValue() : 0L);
                out.add(m);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CoinKpiDto getCoinOpsKpi(LocalDateTime dayStartInclusive) {
        LocalDateTime dayStart = dayStartInclusive != null ? dayStartInclusive : LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        long todayCharge = 0L;
        long todayUsed = 0L;
        long totalCirculating = sumAllMemberCoins();
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT COALESCE(SUM(CASE WHEN COIN_DELTA > 0 THEN COIN_DELTA ELSE 0 END), 0) AS CHARGE_SUM, "
                            + "COALESCE(SUM(CASE WHEN COIN_DELTA < 0 THEN -COIN_DELTA ELSE 0 END), 0) AS USED_SUM "
                            + "FROM MARKET_TXN WHERE CREATED_AT >= :dayStart")
                    .setParameter("dayStart", dayStart)
                    .getResultList();
            if (!rows.isEmpty() && rows.get(0) != null) {
                Object[] row = rows.get(0);
                todayCharge = row[0] != null ? ((Number) row[0]).longValue() : 0L;
                todayUsed = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            }
        } catch (Exception ignored) {
            todayCharge = 0L;
            todayUsed = 0L;
        }
        return new CoinKpiDto(todayCharge, todayUsed, totalCirculating, todayCharge - todayUsed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinFlowDto> getCoinFlowDaily(int days) {
        int bucket = Math.max(1, Math.min(60, days));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(bucket - 1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        Map<String, long[]> agg = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = bucket - 1; i >= 0; i--) {
            String key = now.minusDays(i).format(fmt);
            agg.put(key, new long[] { 0L, 0L });
        }
        for (MarketTxn tx : marketTxnRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(from)) {
            if (tx.getCreatedAt() == null) {
                continue;
            }
            String key = tx.getCreatedAt().format(fmt);
            long[] v = agg.get(key);
            if (v == null) {
                continue;
            }
            applyDelta(v, tx.getCoinDelta());
        }
        return toFlowList(agg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinFlowDto> getCoinFlowWeekly(int weeks) {
        int bucket = Math.max(1, Math.min(30, weeks));
        LocalDateTime now = LocalDateTime.now();
        java.time.LocalDate currentWeekStart = now.toLocalDate().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        java.time.LocalDate fromWeekStart = currentWeekStart.minusWeeks(bucket - 1);
        LocalDateTime from = fromWeekStart.atStartOfDay();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        Map<String, long[]> agg = new LinkedHashMap<>();
        for (int i = bucket - 1; i >= 0; i--) {
            java.time.LocalDate ws = currentWeekStart.minusWeeks(i);
            agg.put(ws.format(fmt) + "~", new long[] { 0L, 0L });
        }
        for (MarketTxn tx : marketTxnRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(from)) {
            if (tx.getCreatedAt() == null) {
                continue;
            }
            java.time.LocalDate ws = tx.getCreatedAt().toLocalDate().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            String key = ws.format(fmt) + "~";
            long[] v = agg.get(key);
            if (v == null) {
                continue;
            }
            applyDelta(v, tx.getCoinDelta());
        }
        return toFlowList(agg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinFlowDto> getCoinFlowMonthly(int months) {
        int bucket = Math.max(1, Math.min(24, months));
        LocalDateTime now = LocalDateTime.now();
        YearMonth current = YearMonth.from(now);
        YearMonth fromMonth = current.minusMonths(bucket - 1);
        LocalDateTime from = fromMonth.atDay(1).atStartOfDay();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, long[]> agg = new LinkedHashMap<>();
        for (int i = bucket - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            agg.put(ym.format(fmt), new long[] { 0L, 0L });
        }
        for (MarketTxn tx : marketTxnRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(from)) {
            if (tx.getCreatedAt() == null) {
                continue;
            }
            String key = YearMonth.from(tx.getCreatedAt()).format(fmt);
            long[] v = agg.get(key);
            if (v == null) {
                continue;
            }
            applyDelta(v, tx.getCoinDelta());
        }
        return toFlowList(agg);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinDistributionDto> getCoinDistribution() {
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT "
                            + "COALESCE(SUM(CASE WHEN COIN BETWEEN 0 AND 1000 THEN 1 ELSE 0 END),0) AS B1, "
                            + "COALESCE(SUM(CASE WHEN COIN BETWEEN 1001 AND 5000 THEN 1 ELSE 0 END),0) AS B2, "
                            + "COALESCE(SUM(CASE WHEN COIN BETWEEN 5001 AND 10000 THEN 1 ELSE 0 END),0) AS B3, "
                            + "COALESCE(SUM(CASE WHEN COIN >= 10001 THEN 1 ELSE 0 END),0) AS B4 "
                            + "FROM MEMBER")
                    .getResultList();
            if (rows.isEmpty() || rows.get(0) == null) {
                return List.of(
                        new CoinDistributionDto("0 ~ 1,000", 0L),
                        new CoinDistributionDto("1,001 ~ 5,000", 0L),
                        new CoinDistributionDto("5,001 ~ 10,000", 0L),
                        new CoinDistributionDto("10,001 이상", 0L));
            }
            Object[] r = rows.get(0);
            return List.of(
                    new CoinDistributionDto("0 ~ 1,000", numToLong(r[0])),
                    new CoinDistributionDto("1,001 ~ 5,000", numToLong(r[1])),
                    new CoinDistributionDto("5,001 ~ 10,000", numToLong(r[2])),
                    new CoinDistributionDto("10,001 이상", numToLong(r[3])));
        } catch (Exception e) {
            return List.of(
                    new CoinDistributionDto("0 ~ 1,000", 0L),
                    new CoinDistributionDto("1,001 ~ 5,000", 0L),
                    new CoinDistributionDto("5,001 ~ 10,000", 0L),
                    new CoinDistributionDto("10,001 이상", 0L));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoinTxnLogDto> getRecentCoinTxnLogs(LocalDateTime fromInclusive, int page, int pageSize) {
        LocalDateTime from = fromInclusive != null ? fromInclusive : LocalDateTime.now().minusMonths(1);
        int safePage = Math.max(1, page);
        int size = Math.max(1, Math.min(pageSize, 50));
        int offset = (safePage - 1) * size;
        try {
            @SuppressWarnings("unchecked")
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT t.CREATED_AT, t.MEMBER_ID, COALESCE(m.NICKNAME, ''), t.TXN_TYPE, t.COIN_DELTA, COALESCE(t.NOTE, '') "
                            + "FROM MARKET_TXN t LEFT JOIN MEMBER m ON m.MNO = t.MEMBER_ID "
                            + "WHERE t.CREATED_AT >= :from "
                            + "ORDER BY t.CREATED_AT DESC")
                    .setParameter("from", from)
                    .setFirstResult(offset)
                    .setMaxResults(size)
                    .getResultList();
            List<CoinTxnLogDto> out = new ArrayList<>();
            for (Object[] row : rows) {
                LocalDateTime at = row[0] instanceof LocalDateTime dt ? dt : null;
                out.add(new CoinTxnLogDto(
                        at != null ? at.format(COIN_TXN_TS_FMT) : "",
                        numToLong(row[1]),
                        row[2] != null ? String.valueOf(row[2]) : "",
                        row[3] != null ? String.valueOf(row[3]) : "",
                        row[4] != null ? ((Number) row[4]).intValue() : 0,
                        row[5] != null ? String.valueOf(row[5]) : ""));
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecentCoinTxnLogs(LocalDateTime fromInclusive) {
        LocalDateTime from = fromInclusive != null ? fromInclusive : LocalDateTime.now().minusMonths(1);
        try {
            Number n = (Number) em.createNativeQuery("SELECT COUNT(*) FROM MARKET_TXN WHERE CREATED_AT >= :from")
                    .setParameter("from", from)
                    .getSingleResult();
            return n == null ? 0L : n.longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    private static void applyDelta(long[] bucket, int coinDelta) {
        if (coinDelta >= 0) {
            bucket[0] += coinDelta;
        } else {
            bucket[1] += Math.abs((long) coinDelta);
        }
    }

    private static List<CoinFlowDto> toFlowList(Map<String, long[]> agg) {
        List<CoinFlowDto> out = new ArrayList<>();
        for (Map.Entry<String, long[]> e : agg.entrySet()) {
            long charge = e.getValue()[0];
            long used = e.getValue()[1];
            out.add(new CoinFlowDto(e.getKey(), charge, used, charge - used));
        }
        return out;
    }

    private static long numToLong(Object o) {
        return (o instanceof Number n) ? n.longValue() : 0L;
    }
}
