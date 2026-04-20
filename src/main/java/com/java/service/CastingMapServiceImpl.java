package com.java.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.casting.CastingMapRegions;
import com.java.casting.CastingMapRegions.Region;
import com.java.entity.CastingEventEffectType;
import com.java.entity.CastingMapDailyExplore;
import com.java.entity.CastingSpotBuff;
import com.java.repository.CastingMapDailyExploreRepository;
import com.java.repository.CastingSpotBuffRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class CastingMapServiceImpl implements CastingMapService {

	public static final int FREE_EXPLORES_PER_DAY = 3;
	public static final int PAID_EXPLORE_COIN = 50;
	public static final int SPOT_DURATION_MINUTES = 30;

	private static final int ROLL_FAIL_MAX = 5000;
	private static final int ROLL_NORMAL_MAX = 8500;
	private static final DateTimeFormatter UI_TIME = DateTimeFormatter.ofPattern("MM.dd HH:mm");

	private final CastingSpotBuffRepository castingSpotBuffRepository;
	private final CastingMapDailyExploreRepository dailyExploreRepository;
	private final MarketService marketService;

	@PersistenceContext
	private EntityManager em;

	public CastingMapServiceImpl(CastingSpotBuffRepository castingSpotBuffRepository,
			CastingMapDailyExploreRepository dailyExploreRepository, MarketService marketService) {
		this.castingSpotBuffRepository = castingSpotBuffRepository;
		this.dailyExploreRepository = dailyExploreRepository;
		this.marketService = marketService;
	}

	@Override
	@Transactional
	public Map<String, Object> explore(Long memberId, String regionId) {
		Map<String, Object> out = new LinkedHashMap<>();
		if (memberId == null) {
			out.put("result", "logout");
			return out;
		}

		Region region = CastingMapRegions.find(regionId);
		if (region == null) {
			out.put("result", "error");
			out.put("message", "\uD0D0\uC0C9\uD560 \uAD6C\uC5ED\uC744 \uBA3C\uC800 \uC120\uD0DD\uD574 \uC8FC\uC138\uC694.");
			return out;
		}

		LocalDate today = LocalDate.now();
		CastingMapDailyExplore daily = dailyExploreRepository.findByMemberIdAndExploreDate(memberId, today)
				.orElseGet(() -> dailyExploreRepository.save(new CastingMapDailyExplore(memberId, today)));

		int countBefore = daily.getExploreCount();
		int cost = countBefore < FREE_EXPLORES_PER_DAY ? 0 : PAID_EXPLORE_COIN;

		if (cost > 0) {
			int updated = em.createNativeQuery(
					"UPDATE MEMBER SET COIN = COIN - :cost WHERE MNO = :mid AND COIN >= :cost")
					.setParameter("cost", cost)
					.setParameter("mid", memberId)
					.executeUpdate();
			if (updated <= 0) {
				out.put("result", "lack");
				out.put("message",
						"\uCF54\uC778\uC774 \uBD80\uC871\uD569\uB2C8\uB2E4. \uC720\uB8CC \uD0D0\uC0C9 1\uD68C\uC5D0\uB294 "
								+ PAID_EXPLORE_COIN + "\uCF54\uC778\uC774 \uD544\uC694\uD569\uB2C8\uB2E4.");
				out.put("currentCoin", marketService.getCurrentCoin(memberId));
				appendDailyHints(out, daily);
				appendActiveBuff(out, memberId);
				return out;
			}
		}

		daily.setExploreCount(countBefore + 1);
		dailyExploreRepository.save(daily);

		int roll = ThreadLocalRandom.current().nextInt(10_000);
		out.put("result", "ok");
		out.put("currentCoin", marketService.getCurrentCoin(memberId));
		appendDailyHints(out, daily);

		if (roll < ROLL_FAIL_MAX) {
			out.put("discovery", "none");
			out.put("message",
					"\uC774\uBC88 \uB77C\uC6B4\uB4DC\uC5D0\uC11C\uB294 \uB208\uC5D0 \uB744\uB294 \uC5F0\uC2B5\uC0DD\uC744 \uCC3E\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uB2E4\uC74C \uD0C0\uC774\uBC0D\uC744 \uB178\uB824\uBCF4\uC138\uC694.");
			appendActiveBuff(out, memberId);
			return out;
		}

		boolean rare = roll >= ROLL_NORMAL_MAX;
		out.put("discovery", rare ? "rare" : "normal");
		out.put("message",
				rare
						? "\uB808\uC5B4 \uC2A4\uCE74\uC6B0\uD2B8\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uAC15\uD55C \uC7A0\uC7AC\uB825\uC744 \uC9C0\uB2CC \uD754\uC801\uC744 \uD3EC\uCC29\uD588\uC5B4\uC694."
						: "\uAC70\uB9AC \uCE90\uC2A4\uD305\uC5D0 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4. \uBC14\uB85C \uD65C\uC6A9 \uAC00\uB2A5\uD55C \uC2A4\uD31F \uC815\uBCF4\uB97C \uD655\uBCF4\uD588\uC5B4\uC694.");

		castingSpotBuffRepository.deleteByMemberId(memberId);
		castingSpotBuffRepository.flush();

		CastingSpotBuff buff = rollSpotBuff(memberId, region, rare);
		castingSpotBuffRepository.save(buff);

		Map<String, Object> buffMap = new LinkedHashMap<>();
		buffMap.put("spotLabel", buff.getSpotLabel());
		buffMap.put("effectLine", buff.getEffectSummaryLine());
		buffMap.put("expireAt", formatDateTime(buff.getExpireAt()));
		buffMap.put("discoveryLabel", rare ? "RARE DISCOVERY" : "SCOUT LOCKED");
		buffMap.put("rarityClass", rare ? "rare" : "normal");
		buffMap.put("regionLabel", region.label());
		out.put("buff", buffMap);
		appendActiveBuff(out, memberId);

		return out;
	}

	private static void appendDailyHints(Map<String, Object> out, CastingMapDailyExplore daily) {
		int c = daily.getExploreCount();
		out.put("exploresUsedToday", c);
		out.put("freeExploresLeftToday", Math.max(0, FREE_EXPLORES_PER_DAY - c));
		out.put("nextExploreCost", c < FREE_EXPLORES_PER_DAY ? 0 : PAID_EXPLORE_COIN);
	}

	private void appendActiveBuff(Map<String, Object> out, Long memberId) {
		if (memberId == null) {
			return;
		}
		castingSpotBuffRepository.findFirstByMemberIdAndExpireAtAfterOrderByExpireAtDesc(memberId, LocalDateTime.now())
				.ifPresent(buff -> {
					Map<String, Object> buffMap = new LinkedHashMap<>();
					buffMap.put("spotLabel", buff.getSpotLabel());
					buffMap.put("effectLine", buff.getEffectSummaryLine());
					buffMap.put("expireAt", formatDateTime(buff.getExpireAt()));
					buffMap.put("regionCode", buff.getRegionCode());
					out.put("activeBuff", buffMap);
				});
	}

	private static CastingSpotBuff rollSpotBuff(Long memberId, Region region, boolean rare) {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		CastingEventEffectType[] pool = { CastingEventEffectType.SSR_UP, CastingEventEffectType.SR_UP,
				CastingEventEffectType.DISCOUNT_PULL, CastingEventEffectType.BONUS_PULL };
		CastingEventEffectType et = pool[r.nextInt(pool.length)];
		String value;
		switch (et) {
		case SSR_UP:
			value = String.valueOf(rare ? r.nextInt(151) + 250 : r.nextInt(101) + 100);
			break;
		case SR_UP:
			value = String.valueOf(rare ? r.nextInt(151) + 200 : r.nextInt(151) + 100);
			break;
		case DISCOUNT_PULL:
			value = String.valueOf(rare ? r.nextInt(11) + 15 : r.nextInt(8) + 8);
			break;
		case BONUS_PULL:
		default:
			value = "";
			break;
		}
		LocalDateTime expire = LocalDateTime.now().plusMinutes(SPOT_DURATION_MINUTES);
		String label = region.label() + " \uCE90\uC2A4\uD305 \uC2A4\uD31F";
		return new CastingSpotBuff(memberId, region.id(), label, et.name(), value, expire);
	}

	private static String formatDateTime(LocalDateTime value) {
		return value == null ? "" : value.format(UI_TIME);
	}

	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> status(Long memberId) {
		Map<String, Object> out = new LinkedHashMap<>();
		if (memberId == null) {
			out.put("loggedIn", false);
			return out;
		}
		out.put("loggedIn", true);
		LocalDate today = LocalDate.now();
		int used = dailyExploreRepository.findByMemberIdAndExploreDate(memberId, today)
				.map(CastingMapDailyExplore::getExploreCount)
				.orElse(0);
		out.put("exploresUsedToday", used);
		out.put("freeExploresLeftToday", Math.max(0, FREE_EXPLORES_PER_DAY - used));
		out.put("nextExploreCost", used < FREE_EXPLORES_PER_DAY ? 0 : PAID_EXPLORE_COIN);
		out.put("currentCoin", marketService.getCurrentCoin(memberId));
		appendActiveBuff(out, memberId);
		return out;
	}
}
