package com.java.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.dto.UsageFlowDto;
import com.java.dto.UsageKpiDto;
import com.java.dto.UsageLogDto;
import com.java.dto.UsageRatioDto;
import com.java.dto.UsageTopDto;
import com.java.game.config.GachaConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class UsageAnalyticsServiceImpl implements UsageAnalyticsService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = true)
	public long getTodayUsageCoin() {
		LocalDateTime dayStart = LocalDate.now(KST).atStartOfDay();
		return queryLong(
				"SELECT COALESCE(SUM(CASE WHEN COIN_DELTA < 0 THEN -COIN_DELTA ELSE 0 END), 0) FROM MARKET_TXN WHERE CREATED_AT >= :from",
				dayStart);
	}

	@Override
	@Transactional(readOnly = true)
	public long getTodayUsageCount() {
		LocalDateTime dayStart = LocalDate.now(KST).atStartOfDay();
		return queryLong("SELECT COUNT(*) FROM MARKET_TXN WHERE CREATED_AT >= :from AND COIN_DELTA < 0", dayStart);
	}

	@Override
	@Transactional(readOnly = true)
	public long getUsageUserCount() {
		LocalDateTime dayStart = LocalDate.now(KST).atStartOfDay();
		return queryLong("SELECT COUNT(DISTINCT MEMBER_ID) FROM MARKET_TXN WHERE CREATED_AT >= :from AND COIN_DELTA < 0", dayStart);
	}

	@Override
	@Transactional(readOnly = true)
	public double getAverageUsage() {
		long usageCoin = getTodayUsageCoin();
		long usageCount = getTodayUsageCount();
		if (usageCount <= 0L) {
			return 0.0;
		}
		return (double) usageCoin / (double) usageCount;
	}

	@Override
	@Transactional(readOnly = true)
	public UsageKpiDto getTodayKpi() {
		long total = getTodayUsageCoin();
		long count = getTodayUsageCount();
		long users = getUsageUserCount();
		double avg = count > 0 ? (double) total / (double) count : 0.0;
		UsageRatioDto ratio = getUsageRatio(LocalDate.now(KST).atStartOfDay(), LocalDateTime.now(KST));
		long ratioTotal = ratio.shop() + ratio.gacha() + ratio.etc();
		double shopRatio = ratioTotal > 0 ? (double) ratio.shop() * 100.0 / (double) ratioTotal : 0.0;
		double gachaRatio = ratioTotal > 0 ? (double) ratio.gacha() * 100.0 / (double) ratioTotal : 0.0;
		return new UsageKpiDto(total, count, users, avg, shopRatio, gachaRatio);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UsageFlowDto> getUsageFlow(LocalDate from, LocalDate to) {
		if (from == null || to == null || from.isAfter(to)) {
			return List.of();
		}

		Map<LocalDate, long[]> buckets = new LinkedHashMap<>();
		for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
			buckets.put(d, new long[] { 0L, 0L, 0L });
		}

		LocalDateTime fromDt = from.atStartOfDay();
		LocalDateTime toDt = to.plusDays(1).atStartOfDay();
		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(
				"SELECT CREATED_AT, TXN_TYPE, COIN_DELTA FROM MARKET_TXN WHERE CREATED_AT >= :from AND CREATED_AT < :to AND COIN_DELTA < 0 ORDER BY CREATED_AT ASC")
				.setParameter("from", fromDt).setParameter("to", toDt).getResultList();

		for (Object[] row : rows) {
			LocalDateTime at = toLocalDateTime(row[0]);
			if (at == null) {
				continue;
			}
			LocalDate date = at.toLocalDate();
			long[] vals = buckets.get(date);
			if (vals == null) {
				continue;
			}
			String type = str(row[1]).toUpperCase();
			long amount = Math.abs(numToLong(row[2]));
			vals[0] += amount;
			if ("BUY".equals(type)) {
				vals[1] += amount;
			} else if ("GACHA".equals(type)) {
				vals[2] += amount;
			}
		}

		// GACHA가 MARKET_TXN에 누락된 환경 대비: pull 로그로 일별 보정
		applyGachaFallbackToDailyFlow(buckets, fromDt, toDt);

		List<UsageFlowDto> out = new ArrayList<>(buckets.size());
		DateTimeFormatter labelFmt = DateTimeFormatter.ofPattern("MM/dd");
		for (Map.Entry<LocalDate, long[]> e : buckets.entrySet()) {
			long total = e.getValue()[0];
			long shop = e.getValue()[1];
			long gacha = e.getValue()[2];
			if (total < shop + gacha) {
				total = shop + gacha;
			}
			out.add(new UsageFlowDto(e.getKey().format(labelFmt), total, shop, gacha));
		}
		return out;
	}

	@Override
	@Transactional(readOnly = true)
	public UsageRatioDto getUsageRatio(LocalDateTime from, LocalDateTime to) {
		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(
				"SELECT TXN_TYPE, COALESCE(SUM(-COIN_DELTA),0) FROM MARKET_TXN "
						+ "WHERE CREATED_AT >= :from AND CREATED_AT < :to AND COIN_DELTA < 0 "
						+ "GROUP BY TXN_TYPE")
				.setParameter("from", from).setParameter("to", to).getResultList();
		long shop = 0L;
		long gacha = 0L;
		long etc = 0L;
		for (Object[] row : rows) {
			String type = str(row[0]).toUpperCase();
			long amount = numToLong(row[1]);
			if ("BUY".equals(type)) {
				shop += amount;
			} else if ("GACHA".equals(type)) {
				gacha += amount;
			} else {
				etc += amount;
			}
		}

		if (gacha == 0L) {
			gacha = estimateGachaUsageFromPullLog(from, to);
		}
		return new UsageRatioDto(shop, gacha, etc);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UsageTopDto> getShopUsageTop(LocalDateTime from, LocalDateTime to, int limit) {
		int size = Math.max(1, Math.min(limit, 20));
		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(
				"SELECT COALESCE(ITEM_NAME, '(미분류)'), COUNT(*), COALESCE(SUM(-COIN_DELTA),0), COUNT(DISTINCT MEMBER_ID) "
						+ "FROM MARKET_TXN WHERE CREATED_AT >= :from AND CREATED_AT < :to "
						+ "AND COIN_DELTA < 0 AND TXN_TYPE = 'BUY' "
						+ "AND (ITEM_NAME IS NULL OR ITEM_NAME NOT LIKE '포토카드 뽑기%') "
						+ "GROUP BY ITEM_NAME ORDER BY SUM(-COIN_DELTA) DESC, COUNT(*) DESC")
				.setParameter("from", from).setParameter("to", to).setMaxResults(size).getResultList();

		List<UsageTopDto> out = new ArrayList<>();
		for (Object[] row : rows) {
			out.add(new UsageTopDto(str(row[0]), numToLong(row[1]), numToLong(row[2]), numToLong(row[3])));
		}
		return out;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UsageTopDto> getGachaUsageTop(LocalDateTime from, LocalDateTime to, int limit) {
		int size = Math.max(1, Math.min(limit, 20));
		List<UsageTopDto> candidates = new ArrayList<>();

		// 포토카드 뽑기: MARKET_TXN BUY + 상품명(실제 로그: "포토카드 뽑기", "포토카드 뽑기 5회" 등)
		@SuppressWarnings("unchecked")
		List<Object[]> photoRows = em.createNativeQuery(
				"SELECT COUNT(*), COALESCE(SUM(-COIN_DELTA),0), COUNT(DISTINCT MEMBER_ID) "
						+ "FROM MARKET_TXN WHERE CREATED_AT >= :from AND CREATED_AT < :to "
						+ "AND COIN_DELTA < 0 AND TXN_TYPE = 'BUY' AND ITEM_NAME LIKE '포토카드 뽑기%'")
				.setParameter("from", from).setParameter("to", to).getResultList();
		if (!photoRows.isEmpty() && photoRows.get(0) != null) {
			Object[] pr = photoRows.get(0);
			long cnt = numToLong(pr[0]);
			if (cnt > 0L) {
				candidates.add(new UsageTopDto("포토카드 뽑기", cnt, numToLong(pr[1]), numToLong(pr[2])));
			}
		}

		// 연습생 뽑기: GACHA_PULL_LOG (건당 코인은 번들 가격 미저장 → 1회 가격 기준 추정)
		long pullCount = queryLong(
				"SELECT COUNT(*) FROM GACHA_PULL_LOG WHERE CREATED_AT >= :fromInstant AND CREATED_AT < :toInstant",
				from.atZone(KST).toInstant(), to.atZone(KST).toInstant());
		if (pullCount > 0L) {
			long pullUsers = queryLong(
					"SELECT COUNT(DISTINCT MEMBER_ID) FROM GACHA_PULL_LOG WHERE CREATED_AT >= :fromInstant AND CREATED_AT < :toInstant",
					from.atZone(KST).toInstant(), to.atZone(KST).toInstant());
			long estCoin = pullCount * GachaConfig.COIN_PRICE_SINGLE;
			candidates.add(new UsageTopDto("연습생 뽑기", pullCount, estCoin, pullUsers));
		}

		candidates.sort(Comparator.comparingLong(UsageTopDto::totalUsage).reversed());
		return candidates.size() > size ? candidates.subList(0, size) : candidates;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UsageLogDto> getRecentUsageLogs(LocalDateTime from, LocalDateTime to, int limit) {
		return getUsageLogsPage(from, to, 1, limit);
	}

	@Override
	@Transactional(readOnly = true)
	public long countUsageLogs(LocalDateTime from, LocalDateTime to) {
		long nTxn = countMarketTxnSpend(from, to);
		long nPull = queryLong(
				"SELECT COUNT(*) FROM GACHA_PULL_LOG WHERE CREATED_AT >= :fromInstant AND CREATED_AT < :toInstant",
				from.atZone(KST).toInstant(), to.atZone(KST).toInstant());
		return nTxn + nPull;
	}

	@Override
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<UsageLogDto> getUsageLogsPage(LocalDateTime from, LocalDateTime to, int page, int pageSize) {
		int size = Math.max(1, Math.min(pageSize, 50));
		int safePage = Math.max(1, page);
		int offset = (safePage - 1) * size;

		String sql = "SELECT merged.evt_at, merged.u_nick, merged.t_type, merged.i_name, merged.c_delta, merged.n_note FROM ("
				+ "SELECT CAST(t.CREATED_AT AS TIMESTAMP) AS evt_at, "
				+ "COALESCE(m.NICKNAME, CONCAT('#', CAST(t.MEMBER_ID AS VARCHAR))) AS u_nick, "
				+ "t.TXN_TYPE AS t_type, COALESCE(t.ITEM_NAME, '-') AS i_name, t.COIN_DELTA AS c_delta, COALESCE(t.NOTE, '') AS n_note "
				+ "FROM MARKET_TXN t LEFT JOIN MEMBER m ON m.MNO = t.MEMBER_ID "
				+ "WHERE t.CREATED_AT >= :from AND t.CREATED_AT < :to AND t.COIN_DELTA < 0 "
				+ "UNION ALL "
				+ "SELECT CAST(g.CREATED_AT AS TIMESTAMP) AS evt_at, "
				+ "COALESCE(m2.NICKNAME, CONCAT('#', CAST(g.MEMBER_ID AS VARCHAR))) AS u_nick, "
				+ "'GACHA' AS t_type, '연습생 뽑기' AS i_name, CAST(:gachaPullCoinDelta AS INTEGER) AS c_delta, '' AS n_note "
				+ "FROM GACHA_PULL_LOG g LEFT JOIN MEMBER m2 ON m2.MNO = g.MEMBER_ID "
				+ "WHERE g.CREATED_AT >= :fromInstant AND g.CREATED_AT < :toInstant"
				+ ") merged ORDER BY merged.evt_at DESC";

		List<Object[]> rows = em.createNativeQuery(sql).setParameter("from", from).setParameter("to", to)
				.setParameter("fromInstant", from.atZone(KST).toInstant())
				.setParameter("toInstant", to.atZone(KST).toInstant())
				.setParameter("gachaPullCoinDelta", -GachaConfig.COIN_PRICE_SINGLE).setFirstResult(offset)
				.setMaxResults(size).getResultList();

		List<UsageLogDto> out = new ArrayList<>();
		for (Object[] row : rows) {
			LocalDateTime at = toLocalDateTime(row[0]);
			String txnType = str(row[2]);
			String itemName = str(row[3]);
			out.add(new UsageLogDto(at == null ? "" : at.format(TS_FMT), str(row[1]), mapUsageCategory(txnType, itemName),
					itemName, numToLong(row[4]), str(row[5])));
		}
		return out;
	}

	private long countMarketTxnSpend(LocalDateTime from, LocalDateTime to) {
		Number n = (Number) em
				.createNativeQuery(
						"SELECT COUNT(*) FROM MARKET_TXN WHERE CREATED_AT >= :from AND CREATED_AT < :to AND COIN_DELTA < 0")
				.setParameter("from", from).setParameter("to", to).getSingleResult();
		return n == null ? 0L : n.longValue();
	}

	private void applyGachaFallbackToDailyFlow(Map<LocalDate, long[]> buckets, LocalDateTime from, LocalDateTime to) {
		long gachaFromTxn = 0L;
		for (long[] v : buckets.values()) {
			gachaFromTxn += v[2];
		}
		if (gachaFromTxn > 0L) {
			return;
		}

		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(
				"SELECT CREATED_AT, COUNT(*) FROM GACHA_PULL_LOG WHERE CREATED_AT >= :fromInstant AND CREATED_AT < :toInstant GROUP BY CREATED_AT")
				.setParameter("fromInstant", from.atZone(KST).toInstant())
				.setParameter("toInstant", to.atZone(KST).toInstant())
				.getResultList();
		long singlePrice = GachaConfig.COIN_PRICE_SINGLE;
		for (Object[] row : rows) {
			Instant at = toInstant(row[0]);
			if (at == null) {
				continue;
			}
			LocalDate d = LocalDateTime.ofInstant(at, KST).toLocalDate();
			long[] vals = buckets.get(d);
			if (vals == null) {
				continue;
			}
			long amount = numToLong(row[1]) * singlePrice;
			vals[2] += amount;
			vals[0] += amount;
		}
	}

	private long estimateGachaUsageFromPullLog(LocalDateTime from, LocalDateTime to) {
		long count = queryLong(
				"SELECT COUNT(*) FROM GACHA_PULL_LOG WHERE CREATED_AT >= :fromInstant AND CREATED_AT < :toInstant",
				from.atZone(KST).toInstant(), to.atZone(KST).toInstant());
		return count * GachaConfig.COIN_PRICE_SINGLE;
	}

	/** 관리자 로그 구분: 포토카드 뽑기(BUY)는 GACHA로 표시 */
	private static String mapUsageCategory(String txnType, String itemName) {
		String t = txnType == null ? "" : txnType.trim().toUpperCase();
		String item = itemName == null ? "" : itemName;
		if ("BUY".equals(t) && item.contains("포토카드")) {
			return "GACHA";
		}
		if ("BUY".equals(t)) {
			return "SHOP";
		}
		if ("GACHA".equals(t)) {
			return "GACHA";
		}
		return "ADMIN";
	}

	private long queryLong(String sql, Object param) {
		Number n = (Number) em.createNativeQuery(sql).setParameter("from", param).getSingleResult();
		return n == null ? 0L : n.longValue();
	}

	private long queryLong(String sql, Object from, Object to) {
		Number n = (Number) em.createNativeQuery(sql)
				.setParameter("fromInstant", from)
				.setParameter("toInstant", to)
				.getSingleResult();
		return n == null ? 0L : n.longValue();
	}

	private static long numToLong(Object o) {
		return o instanceof Number n ? n.longValue() : 0L;
	}

	private static String str(Object o) {
		return o == null ? "" : String.valueOf(o);
	}

	private static Instant toInstant(Object o) {
		if (o instanceof Instant i) {
			return i;
		}
		if (o instanceof Timestamp ts) {
			return ts.toInstant();
		}
		return null;
	}

	private static LocalDateTime toLocalDateTime(Object o) {
		if (o instanceof LocalDateTime dt) {
			return dt;
		}
		if (o instanceof Timestamp ts) {
			return ts.toLocalDateTime();
		}
		if (o instanceof Instant i) {
			return LocalDateTime.ofInstant(i, KST);
		}
		return null;
	}
}
