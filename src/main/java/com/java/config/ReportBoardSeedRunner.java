package com.java.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.entity.Board;
import com.java.entity.Member;
import com.java.repository.BoardRepository;
import com.java.repository.MemberRepository;

/**
 * 버그/신고 게시판(boardType=report) 데모 글. 제목이 [UX-DEMO-RPT] 로 시작하면 스킵.
 */
@Component
@Order(12)
public class ReportBoardSeedRunner implements CommandLineRunner {

	private static final String TITLE_PREFIX = "[UX-DEMO-RPT] ";

	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;

	@Value("${app.seed.report-demo:false}")
	private boolean enabled;

	public ReportBoardSeedRunner(BoardRepository boardRepository, MemberRepository memberRepository) {
		this.boardRepository = boardRepository;
		this.memberRepository = memberRepository;
	}

	@Override
	public void run(String... args) {
		if (!enabled) {
			return;
		}
		try {
			if (boardRepository.countByTitleStartingWith(TITLE_PREFIX.trim()) > 0) {
				return;
			}
			Member author = memberRepository.findByMid("admin").orElse(null);
			String nick = author != null && author.getNickname() != null ? author.getNickname() : "데모유저";
			Long mno = author != null ? author.getMno() : null;

			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			LocalDateTime base = LocalDateTime.now().minusDays(60);
			List<Board> batch = new ArrayList<>(SEEDS.length);

			for (int i = 0; i < SEEDS.length; i++) {
				ReportSeed s = SEEDS[i];
				Board b = new Board("report", s.category(), TITLE_PREFIX + s.title(), s.body(), null, null, false, nick,
						mno, false);
				b.setCreatedAt(base.plusHours(i * 71L + rnd.nextInt(0, 12)).plusMinutes(rnd.nextInt(0, 59)));
				b.setViewCount(rnd.nextInt(1, 101));
				batch.add(b);
			}
			boardRepository.saveAll(batch);
		} catch (Exception ignored) {
		}
	}

	private record ReportSeed(String category, String title, String body) {
	}

	private static final ReportSeed[] SEEDS = new ReportSeed[] {
			new ReportSeed("bug", "라이브 중 화면이 잠깐 멈춤", "크롬 최신, 윈도우 11입니다. 곡 중간 쯤에서 0.5초 정도 프레임이 멈췄다가 돌아와요. 재현은 가끔입니다."),
			new ReportSeed("bug", "가챠 연출 후 결과창이 빈 화면", "모바일 사파리에서 5연 뽑기 후 결과 카드만 안 뜨고 회색 화면이에요. 새로고침하면 인벤에는 들어와 있습니다."),
			new ReportSeed("report", "특정 글에 욕설 댓글 다는 유저", "자유게시판 최신 글에 비속어가 연속으로 달려 있어요. 링크는 상세에서 확인 부탁드립니다."),
			new ReportSeed("bug", "챗봇 입력창 한글 조합 깨짐", "IME 켜진 상태에서 엔터 치면 마지막 글자가 두 번 찍혀요. 엣지 브라우저입니다."),
			new ReportSeed("bug", "출석 팝업이 두 번 뜸", "데일리 출석 누르니까 보상 받았다는 모달이 두 번 연속으로 나왔어요."),
			new ReportSeed("report", "스토리 스포일러 제목 도배", "공략판이 아닌 곳에 엔딩 제목만 대문에 올라와 있어요. 신고합니다."),
			new ReportSeed("bug", "결제 완료인데 코인 미지급", "카카오페이 승인 문자는 왔는데 게임 내 코인이 안 들어왔어요. 18:20경 결제했습니다."),
			new ReportSeed("bug", "맵 페이지에서 지도 마커 클릭 시 오류", "콘솔에 500 뜨고 팝업이 안 열려요. 특정 글 ID만 그런 것 같아요."),
			new ReportSeed("report", "프로필에 타인 사진 도용 의심", "검색으로 찾은 이미지와 동일해 보입니다. 확인 부탁드려요."),
			new ReportSeed("bug", "라이브 스코어와 결과 화면 점수 불일치", "플레이 중 콤보 수는 맞는데 결과 화면 점수가 더 낮게 나왔어요. 스샷 저장해 두었습니다."),
			new ReportSeed("report", "댓글에서 개인정보 요구", "연락처 묻는 댓글이 달렸어요. 삭제 요청합니다."),
			new ReportSeed("bug", "알림 음성이 계속 재생됨", "설정에서 끈 뒤에도 메뉴 들어갈 때마다 짧게 재생돼요."),
			new ReportSeed("bug", "MY 연습생 카드가 회색으로만 보임", "캐시 삭제 후에도 동일. 다른 기기에서는 정상입니다."),
			new ReportSeed("report", "게시글에 불법 사이트 링크", "본문에 단축 URL로 광고성 링크가 있습니다."),
			new ReportSeed("bug", "팬미팅 일정 캘린더 한 달 앞으로만 보임", "다음 달 버튼이 안 먹혀요. 모바일 크롬입니다."),
			new ReportSeed("report", "혐오 표현 닉네임", "리더보드에 보이는 닉이 불쾌합니다. 조치 부탁드립니다."),
			new ReportSeed("bug", "로그아웃 후에도 로그인으로 보임", "헤더만 로그인 상태인 것처럼 보이고 실제 액션은 실패해요."),
			new ReportSeed("bug", "채팅 전송 버튼 연타 시 중복 전송", "한 번만 눌렀는데 같은 메시지가 두 번 올라갔어요."),
			new ReportSeed("report", "도배성 동일 댓글", "여러 글에 같은 문장을 반복해서 달고 있어요."),
			new ReportSeed("bug", "다크모드에서 글씨 대비 부족", "자유게시판 목록에서 부제가 거의 안 보입니다."),
	};

	static {
		if (SEEDS.length != 20) {
			throw new IllegalStateException("신고 시드는 20개여야 합니다.");
		}
	}
}
