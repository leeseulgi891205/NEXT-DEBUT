package com.java.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TraineeGroupService {

	public static final String GROUP_RIIZE = "RIIZE";
	public static final String GROUP_EXO = "EXO";
	public static final String GROUP_HEARTS2HEARTS = "HEARTS2HEARTS";
	public static final String GROUP_AESPA = "AESPA";
	public static final String GROUP_REDVELVET = "REDVELVET";
	public static final String GROUP_HIDDEN = "HIDDEN";
	public static final String GROUP_OTHER = "OTHER";
	public static final String GROUP_ALL = "ALL";

	private static final int BIT_RIIZE = 1 << 0;
	private static final int BIT_EXO = 1 << 1;
	private static final int BIT_HEARTS2HEARTS = 1 << 2;
	private static final int BIT_AESPA = 1 << 3;
	private static final int BIT_REDVELVET = 1 << 4;
	private static final int BIT_HIDDEN = 1 << 5;

	public static final int DEFAULT_UNLOCK_MASK = BIT_RIIZE | BIT_HEARTS2HEARTS;

	public static final int REQUIRE_REDVELVET_AVG = 70;
	public static final int REQUIRE_EXO_AVG = 80;
	public static final int REQUIRE_AESPA_AVG = 90;

	private static final Set<String> RIIZE_NAMES = Set.of("쇼타로", "은석", "성찬", "원빈", "소희", "앤톤");
	private static final Set<String> EXO_NAMES = Set.of("수호", "찬열", "디오", "카이", "세훈", "레이");
	private static final Set<String> HEARTS2HEARTS_NAMES = Set.of("예온", "에이나", "지우", "카르멘", "주은", "이안", "유하", "스텔라");
	private static final Set<String> AESPA_NAMES = Set.of("카리나", "윈터", "닝닝", "지젤");
	private static final Set<String> REDVELVET_NAMES = Set.of("아이린", "웬디", "조이", "예리", "슬기");
	private static final Set<String> HIDDEN_NAMES = Set.of("문상훈");

	public String normalizeGroupFilter(String group) {
		if (!StringUtils.hasText(group)) {
			return GROUP_ALL;
		}
		String g = group.strip().toUpperCase(Locale.ROOT);
		return switch (g) {
		case GROUP_RIIZE, GROUP_EXO, GROUP_HEARTS2HEARTS, GROUP_AESPA, GROUP_REDVELVET, GROUP_HIDDEN -> g;
		default -> GROUP_ALL;
		};
	}

	public String resolveTraineeGroup(String name) {
		if (!StringUtils.hasText(name)) {
			return GROUP_OTHER;
		}
		String n = name.strip();
		if (RIIZE_NAMES.contains(n)) {
			return GROUP_RIIZE;
		}
		if (EXO_NAMES.contains(n)) {
			return GROUP_EXO;
		}
		if (HEARTS2HEARTS_NAMES.contains(n)) {
			return GROUP_HEARTS2HEARTS;
		}
		if (AESPA_NAMES.contains(n)) {
			return GROUP_AESPA;
		}
		if (REDVELVET_NAMES.contains(n)) {
			return GROUP_REDVELVET;
		}
		if (HIDDEN_NAMES.contains(n)) {
			return GROUP_HIDDEN;
		}
		return GROUP_OTHER;
	}

	public boolean isUnlocked(int mask, String groupCode) {
		String g = normalizeGroupCode(groupCode);
		return switch (g) {
		case GROUP_RIIZE -> (mask & BIT_RIIZE) != 0;
		case GROUP_EXO -> (mask & BIT_EXO) != 0;
		case GROUP_HEARTS2HEARTS -> (mask & BIT_HEARTS2HEARTS) != 0;
		case GROUP_AESPA -> (mask & BIT_AESPA) != 0;
		case GROUP_REDVELVET -> (mask & BIT_REDVELVET) != 0;
		case GROUP_HIDDEN -> (mask & BIT_HIDDEN) != 0;
		default -> true;
		};
	}

	public int applyUnlockByAverageScore(int currentMask, double rosterAverageScore) {
		return applyUnlockByProgress(currentMask, rosterAverageScore, false);
	}

	public int applyUnlockByProgress(int currentMask, double rosterAverageScore, boolean hasPerfectMember) {
		int next = currentMask;
		if (rosterAverageScore >= REQUIRE_REDVELVET_AVG) {
			next |= BIT_REDVELVET;
		}
		if (rosterAverageScore >= REQUIRE_EXO_AVG) {
			next |= BIT_EXO;
		}
		if (rosterAverageScore >= REQUIRE_AESPA_AVG) {
			next |= BIT_AESPA;
		}
		if (hasPerfectMember) {
			next |= BIT_HIDDEN;
		}
		return next;
	}

	public Map<String, List<String>> unlockRequirements() {
		Map<String, List<String>> out = new LinkedHashMap<>();
		out.put(GROUP_REDVELVET, List.of("팀 평균 70점 이상"));
		out.put(GROUP_EXO, List.of("팀 평균 80점 이상"));
		out.put(GROUP_AESPA, List.of("팀 평균 90점 이상"));
		out.put(GROUP_HIDDEN, List.of("게임 플레이 중 멤버 1명이라도 모든 능력치 100 달성"));
		return out;
	}

	private String normalizeGroupCode(String groupCode) {
		if (!StringUtils.hasText(groupCode)) {
			return GROUP_OTHER;
		}
		return groupCode.trim().toUpperCase(Locale.ROOT);
	}
}
