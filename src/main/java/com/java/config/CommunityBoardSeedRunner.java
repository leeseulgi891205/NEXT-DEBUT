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
import com.java.repository.BoardRepository;

/**
 * 자유게시판(free)·공략(guide) 데모 글 50+50. 제목이 [UX-DEMO] 로 시작하면 이미 시드된 것으로 보고 스킵.
 */
@Component
@Order(11)
public class CommunityBoardSeedRunner implements CommandLineRunner {

	private static final String TITLE_PREFIX = "[UX-DEMO] ";

	private final BoardRepository boardRepository;

	@Value("${app.seed.community-demo:false}")
	private boolean enabled;

	public CommunityBoardSeedRunner(BoardRepository boardRepository) {
		this.boardRepository = boardRepository;
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
			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			LocalDateTime base = LocalDateTime.now().minusDays(95);
			List<Board> batch = new ArrayList<>(100);

			int i = 0;
			for (PostSeed p : FREE_SEEDS) {
				Board b = new Board("free", null, TITLE_PREFIX + p.title(), p.body(), null, null, false,
						pickNick(i, rnd), null, false);
				b.setCreatedAt(base.plusHours(i * 37L + rnd.nextInt(0, 12)).plusMinutes(rnd.nextInt(0, 59)));
				b.setViewCount(rnd.nextInt(1, 101));
				batch.add(b);
				i++;
			}
			for (PostSeed p : GUIDE_SEEDS) {
				Board b = new Board("guide", null, TITLE_PREFIX + p.title(), p.body(), null, null, false,
						pickNick(i, rnd), null, false);
				b.setCreatedAt(base.plusHours(i * 41L + rnd.nextInt(0, 12)).plusMinutes(rnd.nextInt(0, 59)));
				b.setViewCount(rnd.nextInt(1, 101));
				batch.add(b);
				i++;
			}
			boardRepository.saveAll(batch);
		} catch (Exception ignored) {
		}
	}

	private static String pickNick(int index, ThreadLocalRandom rnd) {
		return NICKS[(index + rnd.nextInt(0, NICKS.length)) % NICKS.length];
	}

	private static final String[] NICKS = { "라이트유저", "스테이지러버", "캐스팅마스터", "뽑기요정", "연습생맘",
			"야식파더", "커피한잔", "새벽러", "KST정예", "유닛덕후", "포지션고민", "엔딩수집가", "팬미팅러", "챗봇친구",
			"코인아껴쓰기", "SSR기원", "데일리출석", "맵탐험가", "가이드봇", "커뮤니티인" };

	private record PostSeed(String title, String body) {
	}

	private static final PostSeed[] FREE_SEEDS = new PostSeed[] {
			new PostSeed("오늘 점심 추천 좀", "회사 근처 맛집 다 갔는데 뭐가 제일 나을까요. 매운 거 잘 먹어요."),
			new PostSeed("비 오는 날 노래 플레이리스트", "UNIT-X 플레이하면서 듣기 좋은 잔잔한 팝 있으면 추천 부탁드려요."),
			new PostSeed("주말에 뭐 하세요?", "집콕 vs 나들이 고민 중이에요. 게임은 할 건데 밤에만…"),
			new PostSeed("커피 vs 녹차", "카페인 줄이려는데 게임할 땐 커피가 당기네요. 다들 어떻게 해요?"),
			new PostSeed("고양이 집사 인증", "제 털뭉치가 키보드 앞에 앉아서 라이브 판정 방해함 ㅋㅋ"),
			new PostSeed("이어폰 추천", "장시간 게임·영상 볼 때 귀 안 아픈 거 있을까요? 예산 10만 원대."),
			new PostSeed("야식 먹고 후회 중", "치킨 시켰는데 내일 후회할 거 알면서도 시켰네요. 공감?"),
			new PostSeed("운동 루틴 공유", "하루 30분만 걷기 시작했어요. 장좌 자세 깨는 중."),
			new PostSeed("넷플 뭐 보세요?", "요즘 재밌는 드라마 있으면 추천. 스포는 X"),
			new PostSeed("새벽에 눈 떠지는 사람", "새벽에만 집중되는 타입인데 건강 괜찮을까요…"),
			new PostSeed("책상 정리했어요", "케이블 타이로 정리하니까 기분이 살짝 좋아짐."),
			new PostSeed("MBTI 얘기 (가볍게)", "그냥 가벼운 잡담이에요. 게임할 때 성향도 궁금."),
			new PostSeed("오늘 기분 좋은 일", "길에서 좋은 노래 나와서 하루 종일 씐남."),
			new PostSeed("스트레스 풀 때", "산책? 게임? 음악? 저는 라이브 스테이지 돌리면서 풀어요."),
			new PostSeed("여행 가고 싶다", "국내 당일치기 코스 추천 받아요. 사람 많은 곳은 좀 피하고 싶어요."),
			new PostSeed("비건은 아닌데 채소 늘리기", "밀프렙 도전 중. 레시피 있으면 공유 부탁."),
			new PostSeed("게임 말고 취미", "요즘 퍼즐이랑 브이로그 편집 배우는 중이에요."),
			new PostSeed("수면 패턴 고치는 법", "새벽에만 게임이 되는데 낮에도 살고 싶어요."),
			new PostSeed("오늘의 TMI", "양치하다가 휴대폰 떨어뜨림. 액정 무사."),
			new PostSeed("친구랑 연락 줄었을 때", "바빠서 그런 건지 멀어진 건지 애매할 때 어떻게 하세요?"),
			new PostSeed("집에서 입는 옷", "잠옷 vs 트레이닝복 전쟁. 편한 게 최고."),
			new PostSeed("물 마시기 챌린지", "하루 2L 도전 중. 화장실 자주 가는 게 힘듦."),
			new PostSeed("유튜브 알고리즘 빠져나오기", "한 영상만 보려다 새벽 됨. 공감하시나요."),
			new PostSeed("오늘의 할 일 정리", "포스트잇으로만 살아남는 중. 디지털도 쓰긴 하는데."),
			new PostSeed("강아지 vs 고양이", "둘 다 좋아하는데 키울 수는 없음. 그냥 잡담."),
			new PostSeed("버스 vs 지하철", "이어폰 끼고 멍 때리기 좋은 쪽은 전 지하철이에요."),
			new PostSeed("요리 초보 질문", "계란말이가 왜 안 돼요? 팬 온도?"),
			new PostSeed("오늘 하늘 사진", "구름 예쁘게 떠서 찍었어요. 날씨 좋은 날 산책 가세요."),
			new PostSeed("밤에 배고프면", "라면 끓일까 말까 30분 고민하는 타입."),
			new PostSeed("일상 소소한 행복", "편의점에서 좋아하는 음료 1+1 걸린 날."),
			new PostSeed("운동화 추천", "걷기 많이 하는데 발 편한 브랜드 있을까요?"),
			new PostSeed("집중 안 될 때", "플레이리스트 바꾸고 창문 열고 다시 시도."),
			new PostSeed("오타 자랑 아님", "제목 쓰다가 오타 냈는데 수정 귀찮아서 그냥 올림."),
			new PostSeed("주말 아침 루틴", "늦잠 vs 일찍 일어나서 여유. 주로 전자."),
			new PostSeed("취미 사진 찍기", "핸드폰으로만 찍는데 감성 사진 배우고 싶어요."),
			new PostSeed("혼밥 자주 하는 사람", "혼밥할 때 뭐 보세요? 유튜브? 드라마?"),
			new PostSeed("오늘의 기분 노래 한 곡", "신나는 팝 추천 받아요. 운전할 때 듣게."),
			new PostSeed("장마철 빨래 걱정", "제습기 틀어도 냄새 날 때 어떻게 하세요?"),
			new PostSeed("책 읽기 습관", "전자책 vs 종이책. 전 둘 다 하는데 집중은 종이가."),
			new PostSeed("가벼운 잡담 방", "아무 말 대잔치. 오늘 뭐 드셨어요?"),
			new PostSeed("밤샘 후 회복", "물 많이 마시고 낮잠 20분만. 이게 맞나요?"),
			new PostSeed("집순이의 외출", "한 달에 한 번 나가면 성공. 이번 달은 성공."),
			new PostSeed("취미로 배우는 것", "영어 단어 앱이랑 기타 코드만 살짝."),
			new PostSeed("오늘의 작은 목표", "물 1L, 스트레칭 5분. 작게라도."),
			new PostSeed("게임 말고 영화", "최근에 본 영화 중 추천 한 편만."),
			new PostSeed("커피 줄이기 2일차", "헤롱거림. 이게 금단증인가요."),
			new PostSeed("방 청소 언제 하지", "주말에 미루고 미루다 월요일이 됨."),
			new PostSeed("산책 코스", "집 근처 한 바퀴 40분 코스 고정. 음악 필수."),
			new PostSeed("오늘의 한 줄 일기", "날씨 흐림. 기분은 보통. 저녁은 맛있었음."),
			new PostSeed("수다 떨어요", "가볍게 댓글 달아주세요. 주제 없음."),
	};

	private static final PostSeed[] GUIDE_SEEDS = new PostSeed[] {
			new PostSeed("초반 스태미너 배분 팁", "튜토리얼 끝나고 나서는 라이브 전에 소모품 챙기는 습관이 중요해요. 한 번에 몰아쓰기보다 일일 퀘스트랑 맞춰 쓰면 덜 아깝습니다."),
			new PostSeed("포지션별 스탯 우선순위 (보컬)", "보컬 메인으로 키우려면 보컬·멘탈 위주로 올리고, 팀워크는 서브로 맞추면 라이브 판정이 안정적이었어요. 장르마다 가중치 다른 구간 있으니 스토리 선택지도 같이 보세요."),
			new PostSeed("댄스 포지션 육성 노트", "댄스는 스타성이 같이 올라가는 선택지가 많아서 밸런스 잡기 쉬워요. 멘탈이 바닥나면 연습 이벤트에서 회복시키는 루트 추천."),
			new PostSeed("스타성 위주 클리어 후기", "스타성만 밀면 중간에 팀워크 이벤트에서 살짝 고비가 올 수 있어요. 주 1회는 팀워크 보충하는 식으로 조정했습니다."),
			new PostSeed("엔딩 분기 체크리스트", "챕터 후반 선택지에서 호감도·스탯 둘 다 영향 받는 구간이 있어요. 저장 슬롯 나눠서 진짜 엔딩만 따로 보는 것도 방법."),
			new PostSeed("라이브 스테이지 판정 이해하기", "같은 곡이라도 연습생 컨디션·스탯 합이 들어가요. 연속 실패하면 장비·소모품부터 점검해 보세요."),
			new PostSeed("캐스팅 뽑기: 리롤 타이밍", "이벤트 픽업 때만 가중치가 붙는 경우가 많아요. 일반 기간에 쓰기 아까우면 모아두었다가 배너 확인 후 돌리는 걸 추천."),
			new PostSeed("SR→SSR 승급 재료 모으는 법", "주간 미션·출석이 기본 베이스예요. 길거리 캐스팅 맵 탐색 보너스 주간이면 우선순위 올려도 좋아요."),
			new PostSeed("연습생별 숨은 컷 신경 쓰는 분", "특정 호감 구간 지나면 프로필 대사가 바뀌는 케이스가 있어요. 스킵 안 하고 보는 걸 추천."),
			new PostSeed("멘탈 관리 루트 (스토리 스포 주의)", "멘탈이 낮을 때 뜨는 선택지가 엔딩에 영향 줄 수 있어요. 최악 엔딩 피하려면 중간에 회복 이벤트 챙기기."),
			new PostSeed("팀워크 올리기 좋은 이벤트", "합숙·단체 미션 챕터에서 팀워크 상승 폭이 큽니다. 다른 스탯은 그때만 살짝 양보해도 괜찮았어요."),
			new PostSeed("가챠 확률 화면 읽는 법", "표시 확률은 당시 이벤트 배율 반영이에요. 픽업 종료 전에 뽑을지 결정하세요."),
			new PostSeed("특정 연습생 픽업 때 코인 계산", "10연 기준 예상 기대값보다 코인이 부족하면 일일·주간부터 채우고 가세요. 충동 뽑기는 지갑이 아파요."),
			new PostSeed("스토리 챗봇: 질문 잘 묻는 법", "세계관 안에서 구체적으로 물을수록 대사 품질이 좋아요. 한 번에 한 주제만."),
			new PostSeed("라이브 곡 난이도 해금 순서", "스토리 진행도에 따라 곡이 풀려요. 안 보이면 이전 챕터 클리어 조건 확인."),
			new PostSeed("데일리 미션 효율 정리", "5분 컷 가능한 것부터 체크하고, 라이브 1회는 저난이도로 끝내기."),
			new PostSeed("길거리 캐스팅 맵 버프 이해", "지역 이벤트마다 포지션 가중치가 달라요. 뽑기 전에 맵 버프부터 확인."),
			new PostSeed("팬미팅 일정 겹칠 때", "캘린더에서 같은 날 여러 건이면 거리·시간대 먼저 보고 신청하세요. 선착순은 빠르게."),
			new PostSeed("MY 연습생 성장 곡선", "MY에 넣어둔 연습생은 성장치가 따로 쌓여요. 메인과 서브 나눠 두면 자원 분배 편해요."),
			new PostSeed("포지션 밸런스 팀 구성", "보컬2 댄스2 스타1 같은 식으로 가면 스토리 요구 스탯 맞추기 쉬웠어요. 상황마다 조정."),
			new PostSeed("엔딩 수집용 세이브 슬롯", "챕터 8 전후로 슬롯 하나 남겨두면 분기 따라가기 편해요."),
			new PostSeed("스태미너 과금 vs 무료", "무료만으로도 주간 루틴 맞추면 충분한 편이에요. 과금은 스킨·시간 절약 쪽이 체감 큼."),
			new PostSeed("연습생 호감 이벤트 놓치지 않기", "특정 주차에만 뜨는 이벤트 있어요. 공지·게임 내 알림 켜두는 걸 추천."),
			new PostSeed("라이브 풀콤보 노하우", "노트 속도 설정 바꿔보세요. 기기마다 최적값이 달라요."),
			new PostSeed("신규 유저 7일 체크리스트", "튜토리얼→데일리→첫 라이브→캐스팅 한 번→챗봇 체험 순으로 하면 보상 놓치기 적어요."),
			new PostSeed("연습생 스토리 스포: A 루트 후기", "중간 보스 같은 이벤트 전에 아이템 챙기면 선택지 여유 생겨요. (세부 스포는 댓글로)"),
			new PostSeed("뽑기 천장 시스템 정리", "시즌마다 규칙이 조금씩 다를 수 있어요. 이벤트 페이지 숫자 꼭 읽기."),
			new PostSeed("팀워크 부족으로 막힌 분", "이전 챕터로 돌아갈 수 있으면 단체 연습 선택지만 따로 모아보세요."),
			new PostSeed("보컬+댄스 하이브리드 육성", "양쪽 다 중간 이상으로 올리면 특정 엔딩 조건 만족하기 쉬워요. 극단만 피하기."),
			new PostSeed("AI 멘토 채팅 금칙어 피하기", "욕설·개인정보 요구는 필터 걸려요. 게임 설정 질문이 답 잘 나와요."),
			new PostSeed("캐스팅 맵 탐험 쿨타임", "일일 무료 횟수는 새벽 초기화예요. 시간대만 맞춰도 효율 좋아요."),
			new PostSeed("라이브 스코어 올리는 장비 세팅", "소모품은 공격 버프 vs 방어 버프 곡에 맞춰 바꾸면 점수 차이 납니다."),
			new PostSeed("연습생별 추천 스토리 순서", "호감도 쌓이는 쪽부터 보면 후반 선택지 이해가 빨라요."),
			new PostSeed("엔딩 후 재플레이 포인트", "2주차는 스탯 분배만 바꿔도 다른 엔딩 쉽게 볼 수 있어요."),
			new PostSeed("SSR 기대값 vs 마음", "확률은 확률이에요. 예산 정해두고 넘기지 않기."),
			new PostSeed("멘탈 회복 아이템 언제 쓰나", "라이브 직전에만 쓰기 아까울 때가 많아요. 스토리 강제 전투 전에 쓰는 게 안전."),
			new PostSeed("댄스 라이브 곡 추천 (중급)", "중간 BPM 곡으로 패턴 익히고 고난이도 도전하세요."),
			new PostSeed("스타성 올릴 때 팀워크 버는 법", "스타성 선택지가 팀워크도 살짝 올려주는 분기를 골랐어요. 공략 표 참고."),
			new PostSeed("주간 랭킹 보상 효율", "상위권 아니어도 주간 보상은 챙기는 게 장기적으로 이득."),
			new PostSeed("신규 패치 후 밸런스 느낌", "특정 포지션 가중치 조정됐다면 공지 숫자 꼭 확인. 예전 공략이랑 다를 수 있어요."),
			new PostSeed("연습생 서브 스토리 보너스", "서브 스토리 클리어 시 소모품 주는 구간 있어요. 놓치지 말기."),
			new PostSeed("가챠 실패 후 멘탈 관리 (현실)", "과금 각오 없으면 무료만으로도 충분히 즐길 수 있어요. 쉬었다가기."),
			new PostSeed("프로듀서 레벨(있다면) 우선순위", "계정 성장 보상이 스토리 잠금에 영향 주면 우선 미션부터."),
			new PostSeed("라이브 네트워크 지연 시", "공지에 나온 대로 재시도하면 스태미너 이중 소모 안 된다고 해요. 패치 확인."),
			new PostSeed("포지션별 숨겨진 타이틀", "특정 스탯 합과 엔딩 조합으로 타이틀이 풀려요. 수집 목록에서 힌트 확인."),
			new PostSeed("챗봇으로 세계관 질문 리스트", "소속사 구조, 연습생 일과 같은 질문이 답 잘 나와요."),
			new PostSeed("연습생 키우기 밸런스 (주간 플레이)", "하루 30분이면 데일리+라이브 1~2회 가능. 무리하지 않기."),
			new PostSeed("공략: 최종 챕터 저장 시점", "최종 선택 직전에 세이브하면 모든 엔딩 빠르게 도장 찍기 좋아요."),
			new PostSeed("캐스팅 이벤트 기간 계산", "종료 시각이 KST 기준이에요. 해외 거주면 시차만 주의."),
			new PostSeed("MY 슬롯 교체 타이밍", "이벤트 기간 한정 연습생 키울 때만 슬롯 바꿔도 됨."),
	};

	static {
		if (FREE_SEEDS.length != 50 || GUIDE_SEEDS.length != 50) {
			throw new IllegalStateException("FREE/GUIDE 시드는 각 50개여야 합니다.");
		}
	}
}
