package com.java.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.entity.Board;
import com.java.entity.FanMeetingParticipant;
import com.java.entity.Member;
import com.java.repository.BoardRepository;
import com.java.repository.FanMeetingParticipantRepository;
import com.java.repository.MemberRepository;

/**
 * 기존 팬미팅 게시글의 PARTICIPANTS가 비어 있을 때 랜덤 참여자를 채워 넣는 백필 러너.
 */
@Component
@Order(14)
public class FanMeetingParticipantBackfillRunner implements CommandLineRunner {

	private final BoardRepository boardRepository;
	private final FanMeetingParticipantRepository participantRepository;
	private final MemberRepository memberRepository;

	@Value("${app.seed.fanmeeting-participants-demo:false}")
	private boolean enabled;

	public FanMeetingParticipantBackfillRunner(BoardRepository boardRepository,
			FanMeetingParticipantRepository participantRepository,
			MemberRepository memberRepository) {
		this.boardRepository = boardRepository;
		this.participantRepository = participantRepository;
		this.memberRepository = memberRepository;
	}

	@Override
	public void run(String... args) {
		if (!enabled) {
			return;
		}
		try {
			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			List<Member> members = memberRepository.findAll().stream()
					.filter(m -> m.getMno() != null && m.getNickname() != null && !m.getNickname().isBlank())
					.toList();
			List<Board> fanMeetings = boardRepository.findByBoardTypeOrderByCreatedAtDesc("fanmeeting");
			for (Board post : fanMeetings) {
				if (post.getId() == null) {
					continue;
				}
				if (participantRepository.countByPostId(post.getId()) > 0) {
					continue;
				}
				List<FanMeetingParticipant> batch = buildParticipants(post, members, rnd);
				if (!batch.isEmpty()) {
					participantRepository.saveAll(batch);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<FanMeetingParticipant> buildParticipants(Board post, List<Member> members, ThreadLocalRandom rnd) {
		int capacity = post.getMaxCapacity() != null ? post.getMaxCapacity().intValue() : 40;
		int targetCount = "DONE".equalsIgnoreCase(post.getRecruitStatus()) ? rnd.nextInt(8, 21) : rnd.nextInt(3, 13);
		targetCount = Math.min(targetCount, Math.max(1, capacity));
		List<FanMeetingParticipant> out = new ArrayList<>(targetCount);
		List<Long> usedMemberIds = new ArrayList<>(targetCount);
		for (int i = 0; i < targetCount; i++) {
			Member picked = pickUnusedMember(members, usedMemberIds, rnd);
			long userId = picked != null ? picked.getMno().longValue() : 950000L + rnd.nextInt(100000);
			String nick = picked != null ? picked.getNickname() : "백필참여자" + (i + 1);
			String status = resolveParticipantStatus(post.getParticipationType(), i, targetCount, rnd);
			out.add(new FanMeetingParticipant(post.getId(), userId, nick, status));
		}
		return out;
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
}
