package com.java.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.Board;
import com.java.entity.FanMeetingParticipant;
import com.java.entity.Member;
import com.java.game.entity.Trainee;
import com.java.game.repository.TraineeRepository;
import com.java.repository.BoardCommentRepository;
import com.java.repository.BoardLikeRepository;
import com.java.repository.BoardReportRepository;
import com.java.repository.BoardRepository;
import com.java.repository.FanMeetingParticipantRepository;
import com.java.repository.MemberRepository;

/**
 * 팬미팅(boardType=fanmeeting) 데모 글 시드.
 * 실행 시 기존 팬미팅 게시글/참여자 데이터를 비우고 다시 생성한다.
 */
@Component
@Order(13)
public class FanMeetingBoardSeedRunner implements CommandLineRunner {

	private static final String[] PARTICIPATION_TYPES = { "FIRST_COME", "LOTTERY", "CONTACT", "FREE" };

	private final BoardRepository boardRepository;
	private final BoardCommentRepository boardCommentRepository;
	private final BoardLikeRepository boardLikeRepository;
	private final BoardReportRepository boardReportRepository;
	private final FanMeetingParticipantRepository participantRepository;
	private final MemberRepository memberRepository;
	private final TraineeRepository traineeRepository;

	@Value("${app.seed.fanmeeting-demo:false}")
	private boolean enabled;

	public FanMeetingBoardSeedRunner(BoardRepository boardRepository, BoardCommentRepository boardCommentRepository,
			BoardLikeRepository boardLikeRepository, BoardReportRepository boardReportRepository,
			FanMeetingParticipantRepository participantRepository, MemberRepository memberRepository,
			TraineeRepository traineeRepository) {
		this.boardRepository = boardRepository;
		this.boardCommentRepository = boardCommentRepository;
		this.boardLikeRepository = boardLikeRepository;
		this.boardReportRepository = boardReportRepository;
		this.participantRepository = participantRepository;
		this.memberRepository = memberRepository;
		this.traineeRepository = traineeRepository;
	}

	@Override
	@Transactional
	public void run(String... args) {
		System.out.println("[FanMeetingBoardSeedRunner] start, enabled=" + enabled);
		try {
			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			LocalDateTime now = LocalDateTime.now();
			List<Board> batch = new ArrayList<>(30);
			List<Member> members = memberRepository.findAll().stream()
					.filter(m -> m.getMno() != null && m.getNickname() != null && !m.getNickname().isBlank())
					.collect(Collectors.toList());
			List<Trainee> trainees = traineeRepository.findAll().stream()
					.filter(t -> t.getId() != null && t.getName() != null && !t.getName().isBlank())
					.collect(Collectors.toList());
			if (trainees.isEmpty()) {
				return;
			}

			replaceExistingFanMeetingData();

			for (int i = 0; i < 10; i++) {
				batch.add(buildClosedPost(i, now, rnd, trainees.get(i % trainees.size())));
			}
			for (int i = 0; i < 20; i++) {
				batch.add(buildRecruitingPost(i, now, rnd, trainees.get(i % trainees.size())));
			}
			List<Board> savedBoards = boardRepository.saveAll(batch);
			seedParticipants(savedBoards, members, rnd);
			System.out.println("[FanMeetingBoardSeedRunner] reseeded fanmeeting posts=" + savedBoards.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void replaceExistingFanMeetingData() {
		List<Board> existing = boardRepository.findByBoardTypeOrderByCreatedAtDesc("fanmeeting");
		participantRepository.deleteAll();
		for (Board post : existing) {
			Long postId = post.getId();
			if (postId == null) {
				continue;
			}
			boardCommentRepository.deleteByBoardId(postId);
			boardLikeRepository.deleteByBoardId(postId);
			boardReportRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc("board", postId)
					.forEach(boardReportRepository::delete);
		}
		if (!existing.isEmpty()) {
			boardRepository.deleteAll(existing);
		}
	}

	private void seedParticipants(List<Board> posts, List<Member> members, ThreadLocalRandom rnd) {
		for (Board post : posts) {
			if (post.getId() == null) {
				continue;
			}
			int capacity = post.getMaxCapacity() != null ? post.getMaxCapacity().intValue() : 40;
			int targetCount = "DONE".equalsIgnoreCase(post.getRecruitStatus()) ? rnd.nextInt(8, 21) : rnd.nextInt(3, 13);
			targetCount = Math.min(targetCount, Math.max(1, capacity));
			List<FanMeetingParticipant> batch = new ArrayList<>(targetCount);
			List<Long> usedMemberIds = new ArrayList<>(targetCount);
			for (int i = 0; i < targetCount; i++) {
				Member picked = pickUnusedMember(members, usedMemberIds, rnd);
				long userId = picked != null ? picked.getMno().longValue() : 900000L + rnd.nextInt(100000);
				String nick = picked != null ? picked.getNickname() : "테스트참여자" + (i + 1);
				String status = resolveParticipantStatus(post.getParticipationType(), i, targetCount, rnd);
				batch.add(new FanMeetingParticipant(post.getId(), userId, nick, status));
			}
			participantRepository.saveAll(batch);
		}
	}

	private static String resolveParticipantStatus(String participationType, int idx, int total, ThreadLocalRandom rnd) {
		String type = participationType == null ? "FREE" : participationType.toUpperCase();
		return switch (type) {
		case "LOTTERY" -> idx < Math.max(1, total / 4) ? "PICKED" : "APPLIED";
		case "FIRST_COME" -> idx < Math.max(1, total / 2) ? "APPROVED" : "WAITING";
		case "CONTACT" -> rnd.nextInt(100) < 35 ? "APPROVED" : "APPLIED";
		default -> "APPLIED";
		};
	}

	private static Member pickUnusedMember(List<Member> members, List<Long> usedMemberIds, ThreadLocalRandom rnd) {
		if (members == null || members.isEmpty()) {
			return null;
		}
		for (int attempt = 0; attempt < members.size() * 2; attempt++) {
			Member m = members.get(rnd.nextInt(members.size()));
			Long id = m.getMno();
			if (id != null && !usedMemberIds.contains(id)) {
				usedMemberIds.add(id);
				return m;
			}
		}
		Member fallback = members.get(rnd.nextInt(members.size()));
		if (fallback.getMno() != null) {
			usedMemberIds.add(fallback.getMno());
		}
		return fallback;
	}

	private Board buildClosedPost(int index, LocalDateTime now, ThreadLocalRandom rnd, Trainee trainee) {
		LocationSeed loc = pickLocation(rnd);
		String traineeName = trainee.getName().trim();
		String title = traineeName + " 팬미팅 후기 모임 안내 #" + (index + 1);
		String content = buildClosedContent(traineeName, loc, index);
		Board b = new Board("fanmeeting", title, content, null, null, false, pickNick(index));
		b.setCreatedAt(now.minusDays(35L - index).withHour(11 + (index % 6)).withMinute((index * 7) % 60));
		b.setEventAt(now.minusDays(20L - index).withHour(19).withMinute((index * 5) % 60));
		b.setRecruitStatus("DONE");
		b.setParticipationType(PARTICIPATION_TYPES[index % PARTICIPATION_TYPES.length]);
		b.setMaxCapacity(20 + rnd.nextInt(31));
		b.setTraineeId(trainee.getId());
		b.setPlaceName(loc.name());
		b.setAddress(loc.address());
		b.setLat(randomized(loc.lat(), rnd));
		b.setLng(randomized(loc.lng(), rnd));
		b.setViewCount(40 + rnd.nextInt(320));
		b.setFanMeetApproved(true);
		return b;
	}

	private Board buildRecruitingPost(int index, LocalDateTime now, ThreadLocalRandom rnd, Trainee trainee) {
		LocationSeed loc = pickLocation(rnd);
		String participationType = PARTICIPATION_TYPES[index / 5];
		String traineeName = trainee.getName().trim();
		String title = buildRecruitingTitle(traineeName, participationType, index);
		String content = buildRecruitingContent(traineeName, participationType, loc, index);
		Board b = new Board("fanmeeting", title, content, null, null, false, pickNick(index + 10));
		b.setCreatedAt(now.minusDays(9L - (index % 10)).withHour(10 + (index % 9)).withMinute((index * 11) % 60));
		b.setEventAt(now.plusDays(2L + (index % 18)).withHour(18 + (index % 4)).withMinute((index * 3) % 60));
		b.setRecruitStatus("RECRUITING");
		b.setParticipationType(participationType);
		b.setMaxCapacity(30 + rnd.nextInt(71));
		b.setTraineeId(trainee.getId());
		b.setPlaceName(loc.name());
		b.setAddress(loc.address());
		b.setLat(randomized(loc.lat(), rnd));
		b.setLng(randomized(loc.lng(), rnd));
		b.setViewCount(20 + rnd.nextInt(260));
		b.setFanMeetApproved(true);
		return b;
	}

	private static double randomized(double base, ThreadLocalRandom rnd) {
		return base + rnd.nextDouble(-0.008, 0.008);
	}

	private static String pickNick(int index) {
		return NICKS[index % NICKS.length];
	}

	private static String buildRecruitingTitle(String traineeName, String participationType, int index) {
		return switch (participationType) {
		case "FIRST_COME" -> traineeName + " 팬미팅 선착순 모집 (" + (index + 1) + "차)";
		case "LOTTERY" -> traineeName + " 팬미팅 추첨 신청 안내 (" + (index + 1) + ")";
		case "CONTACT" -> traineeName + " 팬미팅 문의 후 참여 공지 (" + (index + 1) + ")";
		default -> traineeName + " 팬미팅 자유 참여 안내 (" + (index + 1) + ")";
		};
	}

	private static String buildRecruitingContent(String traineeName, String participationType, LocationSeed loc, int index) {
		String joinGuide = switch (participationType) {
		case "FIRST_COME" -> "참여 방식: 선착순입니다. 신청 순서대로 확정되며 정원 초과 시 대기 처리됩니다.";
		case "LOTTERY" -> "참여 방식: 추첨입니다. 모집 마감 후 운영진이 일괄 추첨하여 선정자를 안내합니다.";
		case "CONTACT" -> "참여 방식: 문의 후 참여입니다. 신청 후 상태가 대기로 표시되며, 확인 뒤 승인됩니다.";
		default -> "참여 방식: 자유 참여입니다. 기본 예절만 지켜주시면 누구나 편하게 참여 가능합니다.";
		};
		return traineeName + " 팬미팅 모집 공지입니다.\n\n"
				+ "집결 장소: " + loc.name() + " (" + loc.address() + ")\n"
				+ "운영 안내: 현장 인증 후 입장, 굿즈 교환은 자유 진행입니다.\n"
				+ joinGuide + "\n"
				+ "유의사항: 과도한 자리 점유, 무단 촬영, 타 팬 비방은 즉시 퇴장 조치됩니다.\n"
				+ "추가 안내: 일정 변동 시 본 게시글 댓글로 가장 먼저 공지드리겠습니다. #" + (index + 1);
	}

	private static String buildClosedContent(String traineeName, LocationSeed loc, int index) {
		return traineeName + " 팬미팅이 마감되어 후속 안내만 남겨둡니다.\n\n"
				+ "진행 장소: " + loc.name() + " (" + loc.address() + ")\n"
				+ "진행 내용: 단체 인사, 응원 구호 맞추기, 포토타임 순으로 진행 완료되었습니다.\n"
				+ "공지 사항: 분실물 문의는 게시글 댓글로 남겨주시면 순차 답변드립니다.\n"
				+ "다음 오프라인 모임 일정은 재공지 예정입니다. 참여해주신 분들 감사합니다. #" + (index + 1);
	}

	private static LocationSeed pickLocation(ThreadLocalRandom rnd) {
		return LOCATIONS[rnd.nextInt(LOCATIONS.length)];
	}

	private record LocationSeed(String name, String address, double lat, double lng) {
	}

	private static final String[] NICKS = { "팬미팅매니아", "현장요정", "응원단장", "스테이지픽", "덕질기록러", "현생탈출러", "콘서트러버",
			"연습생응원단", "굿즈수집가", "심야스트리머", "오프라인출동", "팬사랑", "맵체크봇", "최애직관러", "티켓헌터" };

	private static final LocationSeed[] LOCATIONS = {
			new LocationSeed("홍대입구역 9번 출구 앞", "서울 마포구 양화로 160", 37.557192, 126.924430),
			new LocationSeed("강남역 11번 출구 광장", "서울 강남구 강남대로 396", 37.498095, 127.027610),
			new LocationSeed("잠실 롯데월드몰 아레나광장", "서울 송파구 올림픽로 300", 37.513068, 127.102744),
			new LocationSeed("성수 서울숲역 3번 출구", "서울 성동구 아차산로 100", 37.544566, 127.044412),
			new LocationSeed("합정 메세나폴리스 정문", "서울 마포구 양화로 45", 37.550887, 126.913300),
			new LocationSeed("광화문 교보문고 광장", "서울 종로구 종로 1", 37.571533, 126.977963),
			new LocationSeed("부산 서면역 2번 출구", "부산 부산진구 중앙대로 730", 35.157530, 129.059120),
			new LocationSeed("대구 동성로 중앙무대 앞", "대구 중구 동성로2가 88-22", 35.869404, 128.594812),
			new LocationSeed("인천 송도 센트럴파크 광장", "인천 연수구 컨벤시아대로 160", 37.392559, 126.639200),
			new LocationSeed("대전 둔산 갤러리아 타임월드 앞", "대전 서구 대덕대로 211", 36.351012, 127.378344) };
}
