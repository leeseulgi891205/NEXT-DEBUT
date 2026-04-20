package com.java.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.GachaPullLog;
import com.java.entity.MarketTxn;
import com.java.entity.Member;
import com.java.entity.MemberRank;
import com.java.entity.MyTrainee;
import com.java.game.entity.GameRun;
import com.java.game.entity.GroupType;
import com.java.game.entity.Trainee;
import com.java.game.repository.GameRunMemberRepository;
import com.java.game.repository.GameRunRepository;
import com.java.game.repository.GameTurnLogRepository;
import com.java.game.repository.TraineeRepository;
import com.java.game.service.GameService;
import com.java.photocard.entity.PhotoCardMaster;
import com.java.photocard.entity.UserPhotoCard;
import com.java.photocard.repository.EquippedPhotoCardRepository;
import com.java.photocard.repository.PhotoCardMasterRepository;
import com.java.photocard.repository.UserPhotoCardRepository;
import com.java.repository.CastingMapDailyExploreRepository;
import com.java.repository.CastingSpotBuffRepository;
import com.java.repository.GachaPullLogRepository;
import com.java.repository.MarketTxnRepository;
import com.java.repository.MemberRepository;
import com.java.repository.MyTraineeRepository;

/**
 * 데모 회원 일괄 생성 (런처에서만 호출).
 */
@Service
public class BulkDemoMemberSeedService {

	private static final int COUNT = 100;
	private static final String MID_PREFIX = "seedmem";
	private static final String RAW_PASSWORD = "Seed1234";
	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final Set<String> PRESERVED_MIDS = Set.of("admin", "aaa1111");

	private static final String[] NICK_PREFIX = {
			"별빛", "달빛", "노을", "하늘", "은하", "봄날", "여름", "가을", "겨울", "바다",
			"구름", "숲속", "초롱", "새벽", "한울", "찬란", "고운", "고요", "맑은", "반짝",
			"두근", "설레", "햇살", "바람", "꿈꾸는", "웃는", "따뜻", "푸른", "빛나는", "행복"
	};
	private static final String[] NICK_SUFFIX = {
			"고양이", "토끼", "사슴", "다람쥐", "강아지", "판다", "여우", "햄스터", "펭귄", "참새",
			"요정", "천사", "별", "꽃", "하트", "리본", "멜로디", "소나타", "캔디", "라떼",
			"쿠키", "모찌", "젤리", "루비", "산호", "파도", "단비", "바람", "미소", "꿈"
	};

	private static final String[] NAME_POOL = {
			"김민서", "이서윤", "박지우", "최서연", "정도윤", "조하린", "윤지민", "한시우", "임지훈", "오유진",
			"신현우", "장하율", "권채원", "홍예린", "송서준", "문다은", "배지호", "고유나", "백지안", "노태윤"
	};

	private static final String[] ADDRESS_POOL = {
			"서울 마포구 월드컵북로", "서울 강남구 테헤란로", "서울 송파구 올림픽로", "경기 성남시 분당구 판교역로",
			"인천 연수구 센트럴로", "대전 유성구 대학로", "부산 해운대구 센텀동로", "광주 서구 상무중앙로",
			"대구 수성구 달구벌대로", "울산 남구 삼산로"
	};

	private static final String[] ITEM_BUY_NAMES = {
			"아이템 뽑기", "연습생 스카우트 뽑기", "포토카드 뽑기"
	};

	private static final MemberRank[] RANK_POOL = {
			MemberRank.ROOKIE,
			MemberRank.TRAINEE,
			MemberRank.RISING_STAR,
			MemberRank.IDOL,
			MemberRank.SUPERSTAR
	};

	private final MemberRepository memberRepository;
	private final GameService gameService;
	private final GameRunRepository gameRunRepository;
	private final GameRunMemberRepository gameRunMemberRepository;
	private final GameTurnLogRepository gameTurnLogRepository;
	private final MyTraineeRepository myTraineeRepository;
	private final MarketTxnRepository marketTxnRepository;
	private final GachaPullLogRepository gachaPullLogRepository;
	private final CastingSpotBuffRepository castingSpotBuffRepository;
	private final CastingMapDailyExploreRepository castingMapDailyExploreRepository;
	private final UserPhotoCardRepository userPhotoCardRepository;
	private final EquippedPhotoCardRepository equippedPhotoCardRepository;
	private final PhotoCardMasterRepository photoCardMasterRepository;
	private final TraineeRepository traineeRepository;
	private final JuminCryptoService juminCryptoService;
	@PersistenceContext
	private EntityManager em;

	public BulkDemoMemberSeedService(MemberRepository memberRepository,
			GameService gameService,
			GameRunRepository gameRunRepository,
			GameRunMemberRepository gameRunMemberRepository,
			GameTurnLogRepository gameTurnLogRepository,
			MyTraineeRepository myTraineeRepository,
			MarketTxnRepository marketTxnRepository,
			GachaPullLogRepository gachaPullLogRepository,
			CastingSpotBuffRepository castingSpotBuffRepository,
			CastingMapDailyExploreRepository castingMapDailyExploreRepository,
			UserPhotoCardRepository userPhotoCardRepository,
			EquippedPhotoCardRepository equippedPhotoCardRepository,
			PhotoCardMasterRepository photoCardMasterRepository,
			TraineeRepository traineeRepository,
			JuminCryptoService juminCryptoService) {
		this.memberRepository = memberRepository;
		this.gameService = gameService;
		this.gameRunRepository = gameRunRepository;
		this.gameRunMemberRepository = gameRunMemberRepository;
		this.gameTurnLogRepository = gameTurnLogRepository;
		this.myTraineeRepository = myTraineeRepository;
		this.marketTxnRepository = marketTxnRepository;
		this.gachaPullLogRepository = gachaPullLogRepository;
		this.castingSpotBuffRepository = castingSpotBuffRepository;
		this.castingMapDailyExploreRepository = castingMapDailyExploreRepository;
		this.userPhotoCardRepository = userPhotoCardRepository;
		this.equippedPhotoCardRepository = equippedPhotoCardRepository;
		this.photoCardMasterRepository = photoCardMasterRepository;
		this.traineeRepository = traineeRepository;
		this.juminCryptoService = juminCryptoService;
	}

	public boolean shouldSkip() {
		return memberRepository.existsByMid(MID_PREFIX + "001");
	}

	/**
	 * admin/aaa1111 계정만 남기고 기존 회원과 연관 데이터를 정리한다.
	 */
	@Transactional
	public void removeExistingDemoSeedMembers() {
		List<Member> members = memberRepository.findAll();
		for (Member m : members) {
			if (m.getMid() == null || PRESERVED_MIDS.contains(m.getMid().trim().toLowerCase())) {
				continue;
			}
			Long mno = m.getMno();
			if (mno == null) {
				continue;
			}
			deleteMemberRelations(mno);
			memberRepository.deleteById(mno);
		}
	}

	@Transactional
	public void seedDemoMembers() {
		Random rnd = ThreadLocalRandom.current();
		BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
		List<Trainee> allTrainees = traineeRepository.findAll();
		List<PhotoCardMaster> allPhotoCards = photoCardMasterRepository.findAll();
		Set<String> usedNicks = new HashSet<>();

		for (int i = 1; i <= COUNT; i++) {
			String mid = MID_PREFIX + String.format("%03d", i);
			if (memberRepository.existsByMid(mid)) {
				continue;
			}

			LocalDateTime joinedAt = randomDateTimeSinceFebruary();
			String nick = pickUniqueKoreanNickname(rnd, usedNicks);
			MemberRank rank = RANK_POOL[rnd.nextInt(RANK_POOL.length)];

			Member m = new Member();
			m.setMid(mid);
			m.setMpw(enc.encode(RAW_PASSWORD));
			m.setMname(NAME_POOL[rnd.nextInt(NAME_POOL.length)]);
			m.setNickname(nick);
			m.setEmail(mid + "@seed.local");
			m.setPhone("010-" + randomDigits(rnd, 4) + "-" + randomDigits(rnd, 4));
			m.setAddress(ADDRESS_POOL[rnd.nextInt(ADDRESS_POOL.length)] + " " + (1 + rnd.nextInt(300)));
			m.setAddressDetail((1 + rnd.nextInt(60)) + "층 " + (1 + rnd.nextInt(20)) + "호");
			m.setJumin(juminCryptoService.encrypt(randomJumin(rnd)));
			m.setCreatedAt(joinedAt);
			m.setRerollLastAt(joinedAt);
			m.setRankExp(rank.minExp() + rnd.nextInt(120));
			m.setMemberRankCode(rank.name());

			Member saved = memberRepository.save(m);
			if (saved.getMno() == null) {
				continue;
			}
			Long memberId = saved.getMno();

			seedGameRuns(memberId, joinedAt, rnd);
			seedCoinHistory(memberId, joinedAt, rnd);
			seedOwnedTrainees(memberId, allTrainees, rnd);
			seedOwnedPhotoCards(memberId, allPhotoCards, rnd);
		}
	}

	private void deleteMemberRelations(Long mno) {
		List<GameRun> runs = gameRunRepository.findByPlayerMnoOrderByCreatedAtDesc(mno);
		for (GameRun run : runs) {
			Long runId = run.getRunId();
			if (runId == null) {
				continue;
			}
			gameTurnLogRepository.deleteByRunId(runId);
			gameRunMemberRepository.deleteByRunRunId(runId);
			gameRunRepository.deleteById(runId);
		}

		equippedPhotoCardRepository.deleteByMemberId(mno);
		userPhotoCardRepository.deleteByMemberId(mno);
		myTraineeRepository.deleteByMemberId(mno);
		gachaPullLogRepository.deleteByMemberId(mno);
		castingSpotBuffRepository.deleteByMemberId(mno);
		castingMapDailyExploreRepository.deleteByMemberId(mno);
		marketTxnRepository.deleteByMemberId(mno);
	}

	private void seedGameRuns(Long memberId, LocalDateTime joinedAt, Random rnd) {
		List<LocalDateTime> points = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			points.add(randomDateTimeBetween(joinedAt, LocalDateTime.now(SEOUL)));
		}
		points.sort(Comparator.naturalOrder());

		Long earlyRun = gameService.createRunAndPickRoster(GroupType.MIXED, null);
		gameService.setPlayerMno(earlyRun, memberId);
		gameRunRepository.findById(earlyRun).ifPresent(run -> {
			run.setCreatedAt(points.get(0));
			run.setPhase("DAY24_EVENING");
			gameRunRepository.save(run);
		});

		Long midRun = gameService.createRunAndPickRoster(GroupType.MIXED, null);
		gameService.setPlayerMno(midRun, memberId);
		gameRunRepository.findById(midRun).ifPresent(run -> {
			run.setCreatedAt(points.get(1));
			run.setPhase("MID_EVAL");
			gameRunRepository.save(run);
		});

		Long finishedRun = gameService.createRunAndPickRoster(GroupType.MIXED, null);
		gameService.setPlayerMno(finishedRun, memberId);
		gameService.clampDemoSeedRunScoreBelowPerfect(finishedRun);
		LocalDateTime finishedAt = points.get(2).plusHours(1 + rnd.nextInt(36));
		if (finishedAt.isAfter(LocalDateTime.now(SEOUL))) {
			finishedAt = LocalDateTime.now(SEOUL).minusMinutes(rnd.nextInt(20));
		}
		gameService.applyDemoSeedFinishedRun(finishedRun, points.get(2), finishedAt);
	}

	private void seedCoinHistory(Long memberId, LocalDateTime joinedAt, Random rnd) {
		int balance = 1000;
		int chargeCount = 2 + rnd.nextInt(4);
		for (int i = 0; i < chargeCount; i++) {
			int amount = (5 + rnd.nextInt(196)) * 100;
			balance += amount;
			MarketTxn charge = new MarketTxn(memberId, "CHARGE", null, amount, "데모 충전");
			marketTxnRepository.save(charge);
		}

		for (String buyName : ITEM_BUY_NAMES) {
			int price = (5 + rnd.nextInt(70)) * 100;
			if (balance - price < 100) {
				price = Math.max(100, balance - 100);
			}
			if (price <= 0) {
				continue;
			}
			balance -= price;
			MarketTxn buy = new MarketTxn(memberId, "BUY", buyName, -price, "데모 사용");
			marketTxnRepository.save(buy);
		}

		memberRepository.findById(memberId).ifPresent(m -> {
			// COIN 필드는 네이티브 쿼리로 별도 관리되어 저장 시점에 기본값 보정을 유도한다.
			m.setRerollLastAt(joinedAt.plusMinutes(rnd.nextInt(120)));
			memberRepository.save(m);
		});
		em.createNativeQuery("UPDATE MEMBER SET COIN = :coin WHERE MNO = :mno")
				.setParameter("coin", balance)
				.setParameter("mno", memberId)
				.executeUpdate();
	}

	private void seedOwnedTrainees(Long memberId, List<Trainee> trainees, Random rnd) {
		if (trainees.isEmpty()) {
			return;
		}
		List<Trainee> shuffled = new ArrayList<>(trainees);
		java.util.Collections.shuffle(shuffled, rnd);
		int maxCount = Math.min(8, shuffled.size());
		int minCount = Math.min(3, maxCount);
		int target = minCount + rnd.nextInt(maxCount - minCount + 1);
		for (int i = 0; i < target; i++) {
			Trainee t = shuffled.get(i);
			int qty = 1 + rnd.nextInt(3);
			myTraineeRepository.save(new MyTrainee(memberId, t.getId(), qty));
		}
	}

	private void seedOwnedPhotoCards(Long memberId, List<PhotoCardMaster> cards, Random rnd) {
		if (cards.isEmpty()) {
			return;
		}
		List<PhotoCardMaster> shuffled = new ArrayList<>(cards);
		java.util.Collections.shuffle(shuffled, rnd);
		int maxCount = Math.min(10, shuffled.size());
		int minCount = Math.min(3, maxCount);
		int target = minCount + rnd.nextInt(maxCount - minCount + 1);
		for (int i = 0; i < target; i++) {
			PhotoCardMaster card = shuffled.get(i);
			userPhotoCardRepository.save(new UserPhotoCard(memberId, card));
			gachaPullLogRepository.save(new GachaPullLog(memberId, card.getTrainee().getId(), card.getGrade().name(), "DEFAULT"));
		}
	}

	private static LocalDateTime randomDateTimeSinceFebruary() {
		LocalDateTime now = LocalDateTime.now(SEOUL);
		LocalDateTime from = LocalDate.of(now.getYear(), 2, 1).atStartOfDay();
		return randomDateTimeBetween(from, now);
	}

	private static LocalDateTime randomDateTimeBetween(LocalDateTime from, LocalDateTime to) {
		LocalDateTime start = from.isAfter(to) ? to.minusMinutes(5) : from;
		long minMs = start.atZone(SEOUL).toInstant().toEpochMilli();
		long maxMs = to.atZone(SEOUL).toInstant().toEpochMilli();
		if (maxMs <= minMs) {
			return to;
		}
		long picked = ThreadLocalRandom.current().nextLong(minMs, maxMs + 1L);
		return Instant.ofEpochMilli(picked).atZone(SEOUL).toLocalDateTime();
	}

	private String pickUniqueKoreanNickname(Random rnd, Set<String> usedNicks) {
		for (int i = 0; i < 300; i++) {
			String base = NICK_PREFIX[rnd.nextInt(NICK_PREFIX.length)] + NICK_SUFFIX[rnd.nextInt(NICK_SUFFIX.length)];
			String nick = base;
			if (nick.length() > 6) {
				nick = nick.substring(0, 6);
			}
			if (nick.length() < 3) {
				nick = nick + "별";
			}
			if (rnd.nextInt(10) == 0 && nick.length() <= 5) {
				nick = nick + (1 + rnd.nextInt(9));
			}
			if (!usedNicks.contains(nick) && !memberRepository.existsByNickname(nick)) {
				usedNicks.add(nick);
				return nick;
			}
		}
		return "한글닉" + randomDigits(rnd, 3);
	}

	private static String randomDigits(Random rnd, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			sb.append(rnd.nextInt(10));
		}
		return sb.toString();
	}

	private static String randomJumin(Random rnd) {
		int year = 1970 + rnd.nextInt(46); // 1970~2015
		int month = 1 + rnd.nextInt(12);
		int day = 1 + rnd.nextInt(28);
		int genderCode;
		if (year < 2000) {
			genderCode = rnd.nextBoolean() ? 1 : 2;
		} else {
			genderCode = rnd.nextBoolean() ? 3 : 4;
		}
		String front = String.format("%02d%02d%02d", year % 100, month, day);
		String back = genderCode + randomDigits(rnd, 6);
		return front + "-" + back;
	}
}
