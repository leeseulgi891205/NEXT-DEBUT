package com.java.game.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.entity.Member;
import com.java.game.repository.GameRunMemberRepository;
import com.java.game.repository.GameRunRepository;
import com.java.game.service.GameService;
import com.java.repository.MemberRepository;

/**
 * 기존 데이터 보정:
 * 1) FINISHED 런의 빈 로스터를 복구(재추첨)
 * 2) SCORE_CACHE가 비어 있으면 점수 캐시를 채운다.
 */
@Component
@Order(110)
public class RankingScoreCacheBackfillRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(RankingScoreCacheBackfillRunner.class);

	private final GameRunRepository gameRunRepository;
	private final GameRunMemberRepository gameRunMemberRepository;
	private final MemberRepository memberRepository;
	private final GameService gameService;

	public RankingScoreCacheBackfillRunner(
			GameRunRepository gameRunRepository,
			GameRunMemberRepository gameRunMemberRepository,
			MemberRepository memberRepository,
			GameService gameService) {
		this.gameRunRepository = gameRunRepository;
		this.gameRunMemberRepository = gameRunMemberRepository;
		this.memberRepository = memberRepository;
		this.gameService = gameService;
	}

	@Override
	public void run(String... args) {
		int repairedRoster = 0;
		int updatedCache = 0;
		int reassignedOwner = 0;
		try {
			List<Member> members = memberRepository.findAll();
			Map<Long, Member> memberById = members.stream()
					.filter(m -> m.getMno() != null)
					.collect(Collectors.toMap(Member::getMno, Function.identity(), (a, b) -> a));
			List<Long> candidateMnos = members.stream()
					.filter(m -> m.getMno() != null)
					.filter(m -> !isAdminLikeMember(m))
					.map(Member::getMno)
					.toList();

			for (var run : gameRunRepository.findAll()) {
				if (!"FINISHED".equals(run.getPhase()) || run.getRunId() == null) {
					continue;
				}
				Long runId = run.getRunId();
				if (gameRunMemberRepository.findRoster(runId).isEmpty()) {
					try {
						gameService.rerollRun(runId);
						repairedRoster++;
					} catch (Exception e) {
						log.warn("[ranking-score-cache] runId={} 로스터 복구 실패: {}", runId, e.getMessage());
					}
				}

				// admin 소유로 쏠린 랭킹 데이터를 실제 회원 풀로 분산한다.
				if (!candidateMnos.isEmpty() && shouldReassignOwner(run.getPlayerMno(), memberById)) {
					Long randomMno = candidateMnos.get(ThreadLocalRandom.current().nextInt(candidateMnos.size()));
					run.setPlayerMno(randomMno);
					gameRunRepository.save(run);
					reassignedOwner++;
				}

				Integer cache = run.getScoreCache();
				boolean needsCacheBackfill = (cache == null)
						|| (cache <= 0 && !gameRunMemberRepository.findRoster(runId).isEmpty());
				if (needsCacheBackfill) {
					int score = gameService.getRankingScore(runId);
					run.setScoreCache(score);
					gameRunRepository.save(run);
					updatedCache++;
				}
			}
		} catch (Exception e) {
			log.warn("[ranking-score-cache] backfill 실패: {}", e.getMessage());
			return;
		}
		if (repairedRoster > 0 || updatedCache > 0 || reassignedOwner > 0) {
			log.info("[ranking-score-cache] 로스터 복구 {}건, 소유자 재할당 {}건, SCORE_CACHE backfill {}건 완료",
					repairedRoster, reassignedOwner, updatedCache);
		}
	}

	private static boolean shouldReassignOwner(Long mno, Map<Long, Member> memberById) {
		if (mno == null) {
			return true;
		}
		Member owner = memberById.get(mno);
		if (owner == null) {
			return true;
		}
		return isAdminLikeMember(owner);
	}

	private static boolean isAdminLikeMember(Member member) {
		if (member == null) {
			return false;
		}
		String mid = member.getMid() == null ? "" : member.getMid().trim().toLowerCase();
		String role = member.getRole() == null ? "" : member.getRole().trim().toUpperCase();
		return "admin".equals(mid) || "ADMIN".equals(role);
	}
}
