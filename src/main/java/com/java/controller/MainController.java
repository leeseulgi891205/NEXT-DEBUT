package com.java.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.java.config.SessionConst;
import com.java.dto.LoginMember;
import com.java.dto.MyItemDto;
import com.java.entity.Board;
import com.java.entity.Member;
import com.java.game.entity.GameRun;
import com.java.game.entity.GameRunMember;
import com.java.game.entity.Grade;
import com.java.game.entity.Trainee;
import com.java.game.repository.GameRunMemberRepository;
import com.java.game.repository.GameRunRepository;
import com.java.game.repository.TraineeRepository;
import com.java.repository.BoardRepository;
import com.java.repository.MemberRepository;
import com.java.game.service.GameService;
import com.java.game.service.RankingPeriod;
import com.java.game.service.TraineeLikeService;
import com.java.game.util.LikeCountFormat;
import com.java.service.MarketService;
import com.java.service.RankingNavService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

	/** 상점과 동일 가격 기준 — 메인 인기 아이템 9개(앵커는 shop.jsp 의 id 와 일치) */
	private static final List<ShopSpotlightItem> SHOP_SPOTLIGHT_TOP9 = List.of(
			new ShopSpotlightItem("올라운드 패키지 박스", 900, "/images/items/allpass.png", "5종 패키지", "shop-item-allpass"),
			new ShopSpotlightItem("보컬 워터", 180, "/images/items/water.png", "보컬 +10", "shop-item-water"),
			new ShopSpotlightItem("호흡 컨트롤 북", 320, "/images/items/breathe control.jpg", "보컬 +20", "shop-item-breathe"),
			new ShopSpotlightItem("댄스 슈즈", 180, "/images/items/shoes.png", "댄스 +10", "shop-item-shoes"),
			new ShopSpotlightItem("퍼포먼스 밴드", 320, "/images/items/band.png", "댄스 +20", "shop-item-band"),
			new ShopSpotlightItem("팬레터", 180, "/images/items/letter.png", "스타성 +10", "shop-item-letter"),
			new ShopSpotlightItem("라이브 방송 세트", 320, "/images/items/live.png", "스타성 +20", "shop-item-live"),
			new ShopSpotlightItem("릴렉스 캔디", 180, "/images/items/candy.png", "멘탈 +10", "shop-item-candy"),
			new ShopSpotlightItem("명상 키트", 320, "/images/items/meditation.png", "멘탈 +20", "shop-item-meditation"));

	private final GameRunRepository gameRunRepository;
	private final GameRunMemberRepository gameRunMemberRepository;
	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;
	private final TraineeRepository traineeRepository;
	private final TraineeLikeService traineeLikeService;
	private final MarketService marketService;
	private final RankingNavService rankingNavService;
	private final GameService gameService;

	public MainController(GameRunRepository gameRunRepository, GameRunMemberRepository gameRunMemberRepository,
			BoardRepository boardRepository, MemberRepository memberRepository, TraineeRepository traineeRepository,
			TraineeLikeService traineeLikeService, MarketService marketService, RankingNavService rankingNavService,
			GameService gameService) {
		this.gameRunRepository = gameRunRepository;
		this.gameRunMemberRepository = gameRunMemberRepository;
		this.boardRepository = boardRepository;
		this.memberRepository = memberRepository;
		this.traineeRepository = traineeRepository;
		this.traineeLikeService = traineeLikeService;
		this.marketService = marketService;
		this.rankingNavService = rankingNavService;
		this.gameService = gameService;
	}

	@GetMapping("/")
	public String index() {
		return "redirect:/main";
	}

	@GetMapping("/main")
	public String mainPage(Model model, HttpSession session) {
		long totalGames = safeCount(() -> gameRunRepository.count());
		long totalMembers = safeCount(() -> memberRepository.count());
		long totalPosts = safeCount(() -> boardRepository.count());

		model.addAttribute("sGradeCount", 0);
		model.addAttribute("totalGames", totalGames);
		model.addAttribute("totalMembers", totalMembers);
		model.addAttribute("totalPosts", totalPosts);
		model.addAttribute("weeklyTop3", computeTopRunsForPeriod(RankingPeriod.WEEK, 3));
		List<MainRunRow> liveTop9 = computeTopRunsForPeriod(RankingPeriod.ALL, 9);
		model.addAttribute("liveTop9", liveTop9);
		model.addAttribute("rankDashboardSlideCount", ceilDivide(liveTop9.size(), 3));
		model.addAttribute("recentNotices", safeRecent("notice"));
		model.addAttribute("recentFreePosts", recentCommunityPosts());
		List<Board> topViewedPosts = topViewedPosts();
		model.addAttribute("topViewedPosts", topViewedPosts);
		model.addAttribute("boardDashboardSlideCount", ceilDivide(topViewedPosts.size(), 3));
		model.addAttribute("shopSpotlightItems", SHOP_SPOTLIGHT_TOP9);
		model.addAttribute("livePickTopByLikes", livePickTopByLikes());

		LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (loginMember != null && loginMember.mno() != null) {
			Long mno = loginMember.mno();
			model.addAttribute("myRanking", computeMyRanking(mno));
			model.addAttribute("recentPlay", computeRecentPlay(mno));
			model.addAttribute("bestCombo", computeBestCombo(mno));

			marketService.ensureMinimumCoin(mno, MarketService.DEFAULT_MIN_COIN);
			model.addAttribute("mainCoin", marketService.getCurrentCoin(mno));
			model.addAttribute("mainItemQty", sumItemQuantities(marketService.getMyItems(mno)));
			memberRepository.findById(mno).ifPresentOrElse(m -> {
				String nick = m.getNickname();
				model.addAttribute("mainNickname",
						nick != null && !nick.isBlank() ? nick : loginMember.nickname());
				model.addAttribute("mainProfileImage", m.getProfileImage());
			}, () -> {
				model.addAttribute("mainNickname", loginMember.nickname());
				model.addAttribute("mainProfileImage", null);
			});
		}

		return "main";
	}

	/**
	 * runId 없이 /game/run/ranking 으로 들어올 때(메인·우측 네비 등).
	 * 전체 경로로 매핑해 정적 리소스 핸들러에 먹히지 않게 하고, 최신 RUN으로 리다이렉트한다.
	 */
	@GetMapping("/game/run/ranking")
	public String rankingEntryRedirect(@RequestParam(name = "period", required = false) String period,
			HttpSession session) {
		Long runId = rankingNavService.resolveDefaultRunIdForRanking(session);
		if (runId == null) {
			return "redirect:/main";
		}
		session.setAttribute(SessionConst.RANKING_HUB_PENDING_RUN_ID, runId);
		StringBuilder q = new StringBuilder("?");
		if (period != null && !period.isBlank()) {
			q.append("period=").append(URLEncoder.encode(period, StandardCharsets.UTF_8)).append("&");
		}
		q.append("from=main");
		return "redirect:/game/run/" + runId + "/ranking" + q;
	}

	/** 좋아요 누적 기준 상위 5명 — LIVE PICK 캐러셀 */
	private List<LivePickByLikesRow> livePickTopByLikes() {
		try {
			List<Trainee> all = traineeRepository.findAll();
			if (all == null || all.isEmpty()) {
				return List.of();
			}
			List<Long> ids = all.stream().map(Trainee::getId).filter(Objects::nonNull).toList();
			Map<Long, Long> counts = traineeLikeService.countByTraineeIds(ids);
			return all.stream()
					.filter(t -> t.getId() != null)
					.filter(t -> t.getGrade() != Grade.HIDDEN)
					.sorted(Comparator
							.<Trainee>comparingLong(t -> counts.getOrDefault(t.getId(), 0L)).reversed()
							.thenComparing(Trainee::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
					.limit(5)
					.map(t -> toLivePickByLikesRow(t, counts.getOrDefault(t.getId(), 0L)))
					.collect(Collectors.toList());
		} catch (Exception e) {
			return List.of();
		}
	}

	private LivePickByLikesRow toLivePickByLikesRow(Trainee trainee, long likeCount) {
		String grade = trainee.getGrade() == null ? "ROOKIE" : trainee.getGrade().name();
		String imagePath = trainee.getImagePath();
		if (imagePath == null || imagePath.isBlank()) {
			imagePath = "/images/char.png";
		}
		return new LivePickByLikesRow(
				trainee.getId(),
				trainee.getName(),
				grade,
				trainee.getAverageAbilityScore(),
				trainee.getStar(),
				trainee.getDance(),
				imagePath,
				likeCount,
				LikeCountFormat.compact(likeCount));
	}

	private long safeCount(LongSupplier supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			return 0L;
		}
	}

	/** 대시보드 캐러셀: 한 슬라이드에 3개씩일 때 슬라이드 개수 */
	private static int ceilDivide(int n, int d) {
		if (d <= 0 || n <= 0) {
			return 0;
		}
		return (n + d - 1) / d;
	}

	private List<Board> safeRecent(String type) {
		try {
			return boardRepository.findTop3ByBoardTypeOrderByCreatedAtDesc(type);
		} catch (Exception e) {
			return List.of();
		}
	}

	/** free + lounge + 공략(guide) 최신글 합쳐 상위 3개 */
	private List<Board> recentCommunityPosts() {
		try {
			List<Board> a = boardRepository.findTop3ByBoardTypeOrderByCreatedAtDesc("free");
			List<Board> b = boardRepository.findTop3ByBoardTypeOrderByCreatedAtDesc("lounge");
			List<Board> c = boardRepository.findTop3ByBoardTypeOrderByCreatedAtDesc("guide");
			return Stream.of(a.stream(), b.stream(), c.stream()).flatMap(s -> s)
					.filter(Objects::nonNull)
					.sorted(Comparator.comparing(Board::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
					.limit(3)
					.collect(Collectors.toList());
		} catch (Exception e) {
			return List.of();
		}
	}

	private static int sumItemQuantities(List<MyItemDto> items) {
		if (items == null || items.isEmpty()) {
			return 0;
		}
		return items.stream().mapToInt(MyItemDto::getQuantity).sum();
	}

	private List<Board> topViewedPosts() {
		try {
			return boardRepository.findMainFeedTopViewed(PageRequest.of(0, 9));
		} catch (Exception e) {
			return List.of();
		}
	}

	/**
	 * 랭킹 페이지와 동일: FINISHED 런 + {@link GameService#calculateRosterScore(Long)}(표시 점수 0~1000) + 닉네임 라벨.
	 */
	private List<MainRunRow> computeTopRunsForPeriod(RankingPeriod period, int limit) {
		try {
			List<GameRun> finished = gameService.getFinishedRunsForRankingPeriod(period);
			var scored = finished.stream()
					.map(run -> Map.entry(run, gameService.getRankingScore(run.getRunId())))
					.sorted(Comparator.<Map.Entry<GameRun, Integer>, Integer>comparing(Map.Entry::getValue).reversed())
					.limit(limit)
					.collect(Collectors.toList());
			if (scored.isEmpty()) {
				return List.of();
			}
			Map<Long, String> nickByMno = loadNicknameMapForRuns(
					scored.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
			return scored.stream()
					.map(e -> new MainRunRow(e.getKey().getRunId(), buildRankingStyleLabel(e.getKey(), nickByMno),
							e.getValue(), groupTypeDisplayKo(e.getKey().getGroupType())))
					.collect(Collectors.toList());
		} catch (Exception e) {
			return List.of();
		}
	}

	private Map<Long, String> loadNicknameMapForRuns(List<GameRun> runs) {
		Set<Long> mnos = new HashSet<>();
		for (GameRun run : runs) {
			if (run.getPlayerMno() != null) {
				mnos.add(run.getPlayerMno());
			}
		}
		Map<Long, String> out = new HashMap<>();
		if (mnos.isEmpty()) {
			return out;
		}
		for (Member m : memberRepository.findAllById(mnos)) {
			out.put(m.getMno(), displayNameForMember(m));
		}
		return out;
	}

	/** 닉네임 → 이름 → 로그인 아이디 순 (랭킹 페이지와 동일하게 사람 이름이 보이도록) */
	private static String displayNameForMember(Member m) {
		if (m == null) {
			return "";
		}
		if (m.getNickname() != null && !m.getNickname().isBlank()) {
			return m.getNickname().trim();
		}
		if (m.getMname() != null && !m.getMname().isBlank()) {
			return m.getMname().trim();
		}
		if (m.getMid() != null && !m.getMid().isBlank()) {
			return m.getMid().trim();
		}
		return "USER-" + m.getMno();
	}

	private static String buildRankingStyleLabel(GameRun run, Map<Long, String> nameByMno) {
		Long mno = run.getPlayerMno();
		if (mno != null) {
			String name = nameByMno.get(mno);
			return (name != null && !name.isBlank()) ? name : ("USER-" + mno);
		}
		return "RUN-" + run.getRunId();
	}

	/** 메인 TOP 랭킹: MALE/FEMALE/MIXED → 한글 짧은 라벨 */
	private static String groupTypeDisplayKo(String groupType) {
		if (groupType == null || groupType.isBlank()) {
			return "";
		}
		return switch (groupType.trim().toUpperCase()) {
			case "MALE" -> "남자";
			case "FEMALE" -> "여자";
			case "MIXED" -> "혼성";
			default -> "";
		};
	}

	private MyRankingRow computeMyRanking(Long mno) {
		try {
			List<GameRun> myFinished = gameRunRepository.findByPlayerMnoOrderByCreatedAtDesc(mno).stream()
					.filter(r -> "FINISHED".equals(r.getPhase()))
					.collect(Collectors.toList());
			if (myFinished.isEmpty()) {
				return null;
			}
			int myBest = 0;
			Long bestRunId = null;
			for (GameRun r : myFinished) {
				int sc = gameService.getRankingScore(r.getRunId());
				if (bestRunId == null || sc > myBest) {
					myBest = sc;
					bestRunId = r.getRunId();
				}
			}
			final int bestScore = myBest;
			List<GameRun> allFinished = gameService.getFinishedRunsForRankingPeriod(RankingPeriod.ALL);
			if (allFinished.isEmpty()) {
				return null;
			}
			long strictlyBetter = allFinished.stream()
					.mapToLong(r -> gameService.getRankingScore(r.getRunId()) > bestScore ? 1L : 0L)
					.sum();
			int rank = (int) strictlyBetter + 1;
			return new MyRankingRow(rank, bestScore, bestRunId);
		} catch (Exception e) {
			return null;
		}
	}

	private RecentPlayRow computeRecentPlay(Long mno) {
		try {
			return gameRunRepository.findByPlayerMnoOrderByCreatedAtDesc(mno).stream()
				.filter(r -> "FINISHED".equals(r.getPhase()))
				.findFirst()
				.map(run -> new RecentPlayRow(run.getRunId(), shortGroupLabel(run.getGroupType()),
						gameService.getRankingScore(run.getRunId())))
				.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	private BestComboRow computeBestCombo(Long mno) {
		try {
			List<GameRun> myRuns = gameRunRepository.findByPlayerMnoOrderByCreatedAtDesc(mno);
			if (myRuns == null || myRuns.isEmpty()) {
				return null;
			}
			return myRuns.stream()
				.filter(r -> "FINISHED".equals(r.getPhase()))
				.map(run -> new ScoredRun(run, gameService.getRankingScore(run.getRunId())))
				.max(Comparator.comparingInt(ScoredRun::score))
				.map(scored -> new BestComboRow(comboLabel(scored.run().getRunId()), scored.score()))
				.orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	private String shortGroupLabel(String groupType) {
		String value = groupType == null ? "" : groupType.trim().toUpperCase();
		return switch (value) {
			case "FEMALE" -> "FEMALE";
			case "MALE" -> "MALE";
			case "MIXED" -> "MIXED";
			default -> "";
		};
	}

	private String comboLabel(Long runId) {
		try {
			List<GameRunMember> roster = gameRunMemberRepository.findRoster(runId);
			long vocalCount = 0;
			long danceCount = 0;
			long starCount = 0;
			long teamworkCount = 0;
			for (GameRunMember member : roster) {
				Trainee trainee = member == null ? null : member.getTrainee();
				if (trainee == null) {
					continue;
				}
				int max = Math.max(Math.max(trainee.getVocal(), trainee.getDance()), Math.max(trainee.getStar(), trainee.getTeamwork()));
				if (trainee.getVocal() == max) vocalCount++;
				if (trainee.getDance() == max) danceCount++;
				if (trainee.getStar() == max) starCount++;
				if (trainee.getTeamwork() == max) teamworkCount++;
			}

			long maxCount = Math.max(Math.max(vocalCount, danceCount), Math.max(starCount, teamworkCount));
			if (maxCount == 0) {
				return "기록 없음";
			}
			if (vocalCount == maxCount) return "보컬 중심 팀";
			if (danceCount == maxCount) return "퍼포먼스형";
			if (starCount == maxCount) return "스타성 중심";
			return "밸런스 그룹";
		} catch (Exception e) {
			return "기록 없음";
		}
	}

	@FunctionalInterface
	private interface LongSupplier {
		long get();
	}

	private record ScoredRun(GameRun run, int score) {}

	public record ShopSpotlightItem(String itemName, int priceCoin, String imagePath, String effectShort,
			String shopAnchorId) {}

	/**
	 * JSP EL은 Java record 접근자({@code label()})보다 JavaBean 스타일 getter에 안정적으로 매핑된다.
	 */
	public static final class MainRunRow {
		private final Long runId;
		private final String label;
		private final int score;
		private final String groupLabel;

		public MainRunRow(Long runId, String label, int score, String groupLabel) {
			this.runId = runId;
			this.label = label == null ? "" : label;
			this.score = score;
			this.groupLabel = groupLabel == null ? "" : groupLabel;
		}

		public Long getRunId() {
			return runId;
		}

		public String getLabel() {
			return label;
		}

		public int getScore() {
			return score;
		}

		public String getGroupLabel() {
			return groupLabel;
		}
	}

	/** LIVE PICK: 좋아요 상위 연습생 슬라이드용 */
	public static final class LivePickByLikesRow {
		private final Long id;
		private final String name;
		private final String grade;
		private final int totalScore;
		private final int star;
		private final int dance;
		private final String imagePath;
		private final long likeCount;
		private final String likeLabel;

		public LivePickByLikesRow(Long id, String name, String grade, int totalScore, int star, int dance,
				String imagePath, long likeCount, String likeLabel) {
			this.id = id;
			this.name = name == null ? "" : name;
			this.grade = grade == null ? "" : grade;
			this.totalScore = totalScore;
			this.star = star;
			this.dance = dance;
			this.imagePath = imagePath == null ? "" : imagePath;
			this.likeCount = likeCount;
			this.likeLabel = likeLabel == null ? "0" : likeLabel;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getGrade() {
			return grade;
		}

		public int getTotalScore() {
			return totalScore;
		}

		public int getStar() {
			return star;
		}

		public int getDance() {
			return dance;
		}

		public String getImagePath() {
			return imagePath;
		}

		public long getLikeCount() {
			return likeCount;
		}

		public String getLikeLabel() {
			return likeLabel;
		}
	}

	public record MyRankingRow(int rank, int score, Long runId) {}
	public record RecentPlayRow(Long runId, String groupLabel, int score) {}
	public record BestComboRow(String label, int score) {}
}
