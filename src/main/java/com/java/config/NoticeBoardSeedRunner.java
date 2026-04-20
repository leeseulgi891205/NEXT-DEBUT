package com.java.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.entity.Board;
import com.java.entity.Member;
import com.java.repository.BoardRepository;
import com.java.repository.MemberRepository;

/**
 * UNIT-X 공지사항(boardType=notice) 데모 데이터. 관리자 계정 시드 이후 실행.
 * 공지가 하나라도 있으면 건너뜀(수동 작성분과 중복 삽입 방지).
 */
@Component
@Order(10)
public class NoticeBoardSeedRunner implements CommandLineRunner {

	private final BoardRepository boardRepository;
	private final MemberRepository memberRepository;

	public NoticeBoardSeedRunner(BoardRepository boardRepository, MemberRepository memberRepository) {
		this.boardRepository = boardRepository;
		this.memberRepository = memberRepository;
	}

	@Override
	public void run(String... args) {
		try {
			if (boardRepository.countByBoardType("notice") > 0) {
				return;
			}
			Member admin = memberRepository.findByMid("admin").orElse(null);
			if (admin == null) {
				return;
			}
			Long mno = admin.getMno();
			String nick = admin.getNickname() != null ? admin.getNickname() : "ADMIN";

			LocalDateTime base = LocalDateTime.now().minusDays(120);
			List<Board> batch = new ArrayList<>();
			int i = 0;
			for (NoticeSeed n : SEEDS) {
				Board b = new Board("notice", null, n.title(), n.body(), null, null, false, nick, mno, false);
				b.setCreatedAt(base.plusDays(i * 4L).withHour(10 + (i % 8)).withMinute((i * 7) % 60));
				batch.add(b);
				i++;
			}
			boardRepository.saveAll(batch);
		} catch (Exception ignored) {
		}
	}

	private record NoticeSeed(String title, String body) {
	}

	private static final NoticeSeed[] SEEDS = new NoticeSeed[] {
			new NoticeSeed("[안내] UNIT-X 서비스 이용약관 개정 안내 (v1.2)",
					"안녕하세요, UNIT-X 운영팀입니다.\n\n회원 권리·의무 및 유료 결제 관련 조항을 명확히 하기 위해 이용약관을 개정했습니다. 주요 변경: 환불 절차, 이벤트 참여 자격, 부정 이용 제재 기준입니다.\n\n시행일은 공지 상단 기준일이며, 서비스 계속 이용 시 개정 약관에 동의한 것으로 봅니다. 전문은 사이트 하단 링크에서 확인할 수 있습니다."),
			new NoticeSeed("[점검] 정기 서버 점검 안내 (매주 수요일 새벽)",
					"보다 안정적인 서비스를 위해 매주 수요일 03:00~05:00(KST) 구간에 짧은 점검이 있을 수 있습니다.\n\n해당 시간에는 로그인·결제·게임 진행이 일시 중단될 수 있으니 미리 참고해 주세요. 긴급 점검 시에는 별도 공지로 안내드립니다."),
			new NoticeSeed("[이벤트] 신규 프로듀서 환영 미션 보상 안내",
					"가입 후 7일 이내에 튜토리얼을 완료하고 첫 라이브 스테이지를 클리어하면 한정 칭호와 소모품 패키지를 지급합니다.\n\n보상은 우편함(인벤토리) 형태로 지급되며, 수령 기한은 14일입니다. 이미 수령한 계정은 중복 지급되지 않습니다."),
			new NoticeSeed("[가챠] 캐스팅 뽑기 확률 표기 방식 안내",
					"UNIT-X의 연습생 뽑기는 등급별 기본 확률에, 시즌 이벤트·캐스팅 맵 버프가 가중될 수 있습니다.\n\n화면에 표시되는 확률은 당시 적용 중인 배율을 반영한 수치이며, 이벤트 종료 시 자동으로 원래 확률로 돌아갑니다."),
			new NoticeSeed("[업데이트] 스토리 챗봇(라이브 대화) 안정화 패치",
					"일부 구간에서 대사가 끊기거나 동일 반복이 발생하던 현상을 줄이기 위해 프롬프트·컨텍스트 길이를 조정했습니다.\n\n여전히 이상한 응답이 보이면 버그 게시판에 재현 순서와 함께 남겨 주시면 감사하겠습니다."),
			new NoticeSeed("[안내] 길거리 캐스팅 맵 이용 수칙",
					"실제 장소 모임을 연결하는 커뮤니티 기능입니다. 허위 장소·타인 사칭·불법 행위를 유도하는 글은 삭제 및 계정 제재 대상입니다.\n\n모임 전 참가자 간 연락처 공유는 개인 책임이며, 안전한 장소에서 만나 주세요."),
			new NoticeSeed("[이벤트] 시즌 랭킹 집계 시간대 안내",
					"주간·월간 랭킹은 KST 기준으로 집계되며, 매주 월요일 00:00에 전주 결과가 확정됩니다.\n\n집계 직전 10분간 급격한 순위 변동이 있을 수 있으니, 마지막 플레이는 여유 있게 마무리해 주세요."),
			new NoticeSeed("[상점] 코인·티켓 결제 수단 및 영수증",
					"카카오페이 등 외부 PG를 통해 결제 시 영수증은 해당 서비스에서 발급됩니다.\n\n미성년자 결제는 법정대리인 동의가 필요할 수 있으며, 이의가 있는 경우 고객센터로 문의해 주세요."),
			new NoticeSeed("[정책] 부정 프로그램·매크로 이용 제재",
					"자동 조작, 메모리 변조, 비정상 API 호출이 적발될 경우 사전 통지 없이 이용 제한 또는 영구 정지될 수 있습니다.\n\n오탐 의심 시 본인 플레이 기록을 근거로 이의 신청 절차를 안내드립니다."),
			new NoticeSeed("[업데이트] 연습생 스탯·엔딩 분기 밸런스 조정",
					"특정 포지션 위주로 엔딩 도달이 지나치게 쏠리던 부분을 조정했습니다. 전체 난이도는 유지하되 선택지 가중치를 고르게 맞췄습니다.\n\n자세한 수치는 공략 게시판에서 커뮤니티 가이드를 참고해 주세요."),
			new NoticeSeed("[안내] 계정 연동·닉네임 변경 제한",
					"닉네임은 30일에 한 번 변경 가능하며, 타인에게 혐오·불쾌감을 주는 이름은 임의 변경될 수 있습니다.\n\n계정 공유·양도는 약관 위반이며, 분실 시 본인 인증 절차 후 복구를 도와드립니다."),
			new NoticeSeed("[이벤트] 출석 체크 보상 테이블 변경",
					"연속 출석 보너스에 소량의 뽑기권이 추가되었습니다. 월말 주차에는 추가 보상이 붙는 날이 있으니 캘린더를 확인해 주세요.\n\n출석은 자정(KST) 기준으로 초기화됩니다."),
			new NoticeSeed("[채팅] AI 멘토 채팅 예절·금칙어 안내",
					"욕설·혐오·개인정보 요구·불법 정보는 자동 필터 및 신고 시스템에 의해 제재됩니다.\n\n게임 세계관에 맞는 질문일수록 더 풍부한 답변을 받을 수 있습니다."),
			new NoticeSeed("[점검] 결제 모듈 긴급 점검 (완료)",
					"특정 카드사 승인 지연이 발생하여 당일 01:20~02:10 결제가 일시 실패했습니다. 현재는 정상화되었습니다.\n\n중복 결제가 의심될 경우 결제 내역 캡처와 함께 문의해 주시면 확인 후 조치해 드립니다."),
			new NoticeSeed("[안내] 버그 제보 포상제 운영",
					"재현 가능한 중대 버그를 최초로 제보해 주신 분께는 소정의 코인 보상을 드립니다. 동일 이슈는 선제보자 기준입니다.\n\n버그 게시판에 OS, 브라우저, 재현 순서를 적어 주세요."),
			new NoticeSeed("[가챠] 픽업 이벤트: 포지션별 출현 가중",
					"기간 한정으로 특정 포지션 연습생의 출현 가중치가 상승합니다. 이벤트 배너의 기간·대상을 반드시 확인해 주세요.\n\n가중치는 SSR/SR 확률에 각각 다른 방식으로 적용될 수 있습니다."),
			new NoticeSeed("[업데이트] 팬미팅 일정 캘린더 UI 개선",
					"길거리 캐스팅 맵의 월별 캘린더에서 일정이 겹칠 때 구분이 잘 되도록 색상·툴팁을 손봤습니다.\n\n모바일에서도 스크롤 영역이 넓어졌습니다."),
			new NoticeSeed("[안내] 개인정보 처리방침: 쿠키·로그",
					"서비스 개선을 위해 접속 로그·기기 정보를 수집하며, 마케팅 수신은 설정에서 언제든 거부할 수 있습니다.\n\n필수 항목 외 동의 철회 시 일부 기능이 제한될 수 있습니다."),
			new NoticeSeed("[이벤트] 커뮤니티 자유게시판 주간 화제상",
					"매주 금요일 운영진이 추천 수와 내용을 참고해 우수 글을 선정, 소량의 코인을 지급합니다.\n\n타인 비방·저작권 침해 글은 선정에서 제외됩니다."),
			new NoticeSeed("[게임] 라이브 스테이지 판정 지연 시",
					"네트워크 불안정 시 판정 결과가 늦게 뜰 수 있습니다. 이중으로 스태미너가 소모되지 않도록 서버에서 한 번만 처리합니다.\n\n10초 이상 멈추면 페이지 새로고침 후에도 동일하면 버그 제보 부탁드립니다."),
			new NoticeSeed("[안내] 멀티 디바이스 로그인",
					"동일 계정으로 여러 기기에서 동시에 플레이할 수 있으나, 동시에 같은 세션 액션을 수행하면 나중 요청이 거절될 수 있습니다.\n\n계정 보안을 위해 공용 PC에서는 반드시 로그아웃해 주세요."),
			new NoticeSeed("[상점] 환불·취소 가능 조건",
					"디지털 콘텐츠가 사용되지 않은 경우 등 관련 법령에 따라 청약철회가 제한될 수 있습니다.\n\n결제 오류로 중복 청구된 경우 전액 취소를 우선 처리합니다."),
			new NoticeSeed("[업데이트] 연습생 프로필 이미지 업로드 용량",
					"프로필 이미지는 파일당 20MB 이하, JPG/PNG/WebP 권장입니다. 부적절 이미지는 별도 통지 없이 제거될 수 있습니다.\n\n저작권이 있는 이미지 업로드는 금지입니다."),
			new NoticeSeed("[이벤트] 캐스팅 맵 탐색 보너스 주간",
					"지역 이벤트를 탐색하면 추가 탐험 포인트가 지급되는 주간이 있습니다. 이벤트 탭의 배너를 확인해 주세요.\n\n포인트는 시즌이 바뀌면 일부 초기화될 수 있습니다."),
			new NoticeSeed("[안내] 고객센터·문의 응답 시간",
					"평일 10:00~18:00 순차 답변이며, 주말·공휴일은 지연이 있을 수 있습니다.\n\n긴급 장애는 공지 상단에 [장애] 태그로 먼저 안내드립니다."),
			new NoticeSeed("[정책] 커뮤니티 신고 처리 기준 강화",
					"반복적인 악성 댓글·도배는 경고 없이 활동 제한될 수 있습니다. 신고 시 구체적 사유를 적어 주시면 검토에 도움이 됩니다.\n\n허위 신고 역시 제재 대상입니다."),
			new NoticeSeed("[게임] 엔딩 수집·갤러리 동기화",
					"엔딩 해금 정보는 서버에 저장됩니다. 캐시 삭제 후에도 동일 계정으로 로그인하면 유지됩니다.\n\n일부 로컬 설정(그래픽 옵션)만 기기별로 다를 수 있습니다."),
			new NoticeSeed("[이벤트] 스페셜 라이브: 한정 곡 패키지",
					"기간 한정으로 특정 곡 세트가 상점에 등장합니다. 종료 후에는 일반 상점 목록에서 사라질 수 있으니 놓치지 마세요.\n\n곡 난이도는 스토리 진행도에 따라 잠금 해제됩니다."),
			new NoticeSeed("[가챠] 다시뽑기(리롤) 충전 규칙 안내",
					"계정당 리롤 횟수는 시간 경과에 따라 서서히 충전됩니다. 이벤트로 추가 슬롯이 열릴 수 있습니다.\n\n악용 방지를 위해 짧은 시간에 과도한 요청은 일시 차단될 수 있습니다."),
			new NoticeSeed("[업데이트] 모바일 웹 터치 영역·하단 네비 개선",
					"작은 화면에서 버튼이 겹치던 현상을 수정했습니다. iOS Safari·Android Chrome 최신 버전을 권장합니다.\n\n구형 브라우저는 일부 애니메이션이 생략될 수 있습니다."),
	};

}
