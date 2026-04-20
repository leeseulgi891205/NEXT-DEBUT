package com.java.game.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.java.chat.GeminiClient;
import com.java.chat.GeminiClient.StructuredGenerateResult;
import com.java.game.NarrationFallbackDefaults;
import com.java.game.entity.Gender;
import com.java.game.entity.NarrationFallbackSituation;
import com.java.game.repository.NarrationFallbackSituationRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeType;

/**
 * 상황 지문·멤버 대사·턴 내레이션·(선택) 채팅→선택지 키 해석.
 * 외부 LLM은 Gemini만 사용한다. 키가 있으면 항상 API를 먼저 호출한다.
 * DB·앱 내 저장 지문 폴백은 Gemini가 할당량(쿼터) 초과로 모든 키가 막혔을 때만 쓴다.
 * 키 없음·파싱 실패·기타 오류는 짧은 최소 지문(저장 풀 미사용)으로 이어진다.
 */
@Service
public class GameAiNarrationService {

	private static final Logger log = LoggerFactory.getLogger(GameAiNarrationService.class);

	/** Gemini 프롬프트 공통: 중국어 등 비한글 출력 억제 */
	private static final String MODEL_KOREAN_OUTPUT_RULE = """
			모든 지문·멤버 대사·요약은 한국어(한글)로만 작성한다. 중국어·일본어·영어 대화문·번역체 본문 금지.""";

	/** 동일 run·씬·로스터(생존+제외)에서 새로고침 시 Gemini 인트로 재호출 방지 */
	private static final int INTRO_CACHE_MAX_ENTRIES = 512;
	private static final int CHOICE_KEY_CACHE_MAX_ENTRIES = 512;
	private static final int REACTION_CACHE_MAX_ENTRIES = 512;
	private final ConcurrentHashMap<String, IdolDialogueBlock> introDialogueCache = new ConcurrentHashMap<>();
	/** 동일 씬·선택지·프로듀서 입력에서 선택 키 해석 재호출 방지 */
	private final ConcurrentHashMap<String, String> choiceKeyCache = new ConcurrentHashMap<>();
	/** 동일 턴 맥락에서 반응 번들 재호출 방지(새로고침·중복 요청 등) */
	private final ConcurrentHashMap<String, ReactionNarrationBundle> reactionBundleCache = new ConcurrentHashMap<>();

	private final GeminiClient geminiClient;
	private final ObjectMapper objectMapper;
	private final NarrationFallbackSituationRepository narrationFallbackSituationRepository;

	/** false면 게임 인트로·채팅 반응에서 Gemini API를 호출하지 않고 DB 저장 지문만 사용 */
	@Value("${game.narration.use-gemini:false}")
	private boolean useGeminiForNarration;

	public GameAiNarrationService(GeminiClient geminiClient, ObjectMapper objectMapper,
			NarrationFallbackSituationRepository narrationFallbackSituationRepository) {
		this.geminiClient = geminiClient;
		this.objectMapper = objectMapper;
		this.narrationFallbackSituationRepository = narrationFallbackSituationRepository;
	}

	private static final String[] STAT_ORDER = { "보컬", "댄스", "멘탈", "스타", "팀웍" };

	/** 멤버 말풍선: (지문) "대사" 형식 파싱·확장용 */
	private static final Pattern FORMATTED_MEMBER_LINE = Pattern.compile("^\\(([^)]*)\\)\\s*\"([^\"]*)\"\\s*$");

	private static final String[] VOCAL_HINTS = {
			"보컬", "발성", "노래", "라이브", "음정", "호흡", "가성", "파트", "후렴", "보이스", "vocal", "싱잉"
	};
	private static final String[] DANCE_HINTS = {
			"댄스", "안무", "춤", "동선", "퍼포", "제스처", "포인트", "댄브", "dance", "킥", "스텝"
	};
	private static final String[] MENTAL_HINTS = {
			"쉬", "휴식", "멘탈", "컨디션", "회복", "스트레스", "번아웃", "잠", "쉼", "무리"
	};
	private static final String[] STAR_HINTS = {
			"스타", "카메라", "미디어", "노출", "존재감", "임팩트", "화제", "클립", "star", "비주얼", "한 컷"
	};
	private static final String[] TEAM_HINTS = {
			"팀", "팀워크", "협업", "화합", "역할", "소통", "합의", "분배", "조율"
	};

	/**
	 * API 비정상 응답 시 사용. DB 저장 지문 풀은 쓰지 않는다.
	 * 매 호출마다 무작위로 골라 같은 문장만 반복되지 않게 한다.
	 */
	private static final String[] MINIMAL_SITUATION_POOL = {
			"(연습실에서 호흡이 이어지고, 방금 지시가 팀에 스며든다.)",
			"(메트로놈이 한 박자 늦게 들리고, 누군가 숨을 고른다.)",
			"(거울 앞에서 시선이 잠깐 엇갈렸다 다시 맞춰진다.)",
			"(스피커에서 잔향이 남고, 바닥 테이프가 살짝 들뜬다.)",
			"(누군가 물병을 돌리고, 의자가 바닥을 긁는 소리가 난다.)",
			"(연습이 끊기지 않게 누군가 박자만 손끝으로 짚는다.)",
			"(창문 밖 소음이 잠깐 들어왔다 사라지고, 방 안이 다시 조용해진다.)",
			"(파트가 겹치는 구간에서 목소리가 한 톤 어긋났다 맞춰진다.)",
			"(누군가 작게 웃음을 터뜨렸다 입을 다물고, 다시 자세를 잡는다.)",
			"(조명이 미세하게 깜빡였다 안정되고, 그림자만 짧아진다.)",
			"(MR이 잠시 멈추고, 숨소리만 스피커에 실린다.)",
			"(안무지가 바닥에서 바스락이고, 누군가 발끝으로 선을 다시 긋는다.)",
			"(누군가 ‘한 번만’이라고 말하고, 모두가 같은 소절로 되돌아간다.)",
			"(에어컨 바람이 발목을 스치고, 땀 자국이 매트에 남는다.)",
			"(팀원 사이에 짧은 침묵이 있다가, 누군가 먼저 박자를 짚는다.)",
			"(프로듀서 노트가 테이블에 펼쳐져 있고, 펜이 한 줄을 더 긋는다.)",
			"(끝맺음을 맞추려다 한 박자 빠르게 가고, 누군가 고개를 끄덕인다.)",
			"(모니터 화면이 꺼졌다 켜지고, 녹음 표시등만 붉게 남는다.)",
			"(누군가 스트레칭을 멈추고, 다른 이는 그대로 동작을 이어간다.)",
			"(연습실 문 너머로 복도 발소리가 지나가고, 아무도 문을 열지 않는다.)",
			"(발바닥이 매트를 밀고, 누군가 셔츠 소매를 걷어 올린다.)",
			"(스피커에서 하울링이 잠깐 났다가, 누군가 게인을 낮춘다.)",
			"(거울에 비친 동작이 한 박자 어긋났다, 누군가 혼자 중얼거린다.)",
			"(연필이 스탠드에 딸깍이고, 프로듀서 쪽 시선이 잠깐 모였다 흩어진다.)",
			"(누군가 스트레칭을 이어가고, 다른 이는 제자리에서 호흡만 세다.)",
			"(바닥 테이프 끝이 들뜬 자국을 누군가 발로 눌러 고정한다.)",
			"(연습실 공기가 땀과 섬유유연제 냄새로 무겁다. 누군가 창문을 조금 열었다 닫고, 누군가는 그대로 호흡만 센다. 멀리서 지하철 진동이 올라온 듯 바닥이 미세하게 떨린다.)",
			"(메트로놈이 끊기지 않게 이어지고, 누군가는 박자에 맞춰 눈만 깜빡인다. 스피커에서 하울링이 났다가 누군가 게인을 만진다. 거울에 맺힌 김이 천천히 흘러내린다.)",
			"(누군가 가사지를 넘기며 ‘여기만’이라고 속삭인다. 다른 이들은 이미 같은 소절로 되돌아가 있고, 발끝 소리만 바닥에 고르게 떨어진다. 프로듀서 노트 위 펜이 한 줄을 더 긋는다.)",
			"(합창이 겹치는 구간에서 숨이 한 박자 엇갈린다. 누군가 손등으로 카운트를 짚고, 누군가는 입을 다문 채 목만 움직인다. 방 안이 잠깐 조용해졌다가 MR이 다시 차오른다.)",
			"(의자가 바닥을 긁는 소리와 물병이 부딪히는 소리가 겹친다. 누군가 스트레칭을 멈추고, 누군가는 그대로 포즈를 유지한다. 창밖 하늘이 잿빛으로 길어 보인다.)",
			"(누군가 셔츠 소매를 걷어 올리고, 누군가는 물을 따르려다 병만 돌린다. 연습이 끊기지 않게 누군가만 박자를 손끝으로 짚는다. 형광등이 한쪽만 깜빡였다 안정된다.)",
			"(복도 쪽에서 웃음 소리가 났다 사라지고, 방 안만 다시 고요해진다. 누군가 귀를 기울였다가 고개를 돌려 거울을 본다. 누군가 작게 기침하고, 팀의 호흡이 얕아진다.)",
			"(녹음 표시등이 붉게 켜진 채로, 누군가 실수한 구간을 되감지 않으려 애쓴다. 모니터 파형이 출렁이고, 누군가 ‘다시’라고 말한다. 바닥 테이프가 신발에 달라붙었다 떨어진다.)",
			"(누군가 무릎을 짚고 숨을 고르고, 누군가는 그대로 동작을 이어간다. 거울 속 그림자가 한 박자 늦게 따라오는 것 같다. 에어컨 바람이 땀을 식히지 못하고 지나간다.)",
			"(팀원 사이에 짧은 침묵이 있다가, 누군가 먼저 ‘한 번만’이라고 말한다. 모두가 같은 소절로 되돌아가고, 누군가 혼자 중얼거리다 입을 다문다. 스피커에서 잔향만 남는다.)",
			"(프로듀서 테이블 위 종이가 바스락이고, 누군가 그 소리에 맞춰 숨을 들이쉰다. 누군가 펜 뚜껑을 닫았다 열고, 누군가는 거울만 응시한다. 방 온도가 미세하게 오르는 듯하다.)",
			"(새벽에 가까운 시간인데도 불이 켜져 있다. 누군가 스탠드 조명만 끄고 형광등은 그대로 둔다. 바닥에 그림자가 겹치고, 누군가 그 위를 조심스레 피해 걷는다.)",
			"(연습실 문패에 적힌 시간이 무너져 있다. 누군가 손가락으로 글씨를 더듬다가 손을 거둔다. 복도 끝에서 멀리 음악 소리가 났다 사라진다.)",
			"(습기가 찬 바닥에 매트 자국이 남는다. 누군가 수건으로 이마를 닦고, 누군가는 아직도 같은 동작만 반복한다. 벽면 거울이 한 줄로 이어져 끝이 보이지 않는다.)",
			"(누군가 이어폰 한쪽만 귀에 꽂은 채로 고개를 끄덕인다. 다른 쪽에서는 메트로놈만 들린다. 두 박자가 미세하게 어긋나 누군가 볼륨을 낮춘다.)",
			"(프로듀서 자리가 비어 있고 의자만 돌아가 있다. 누군가 그 자리를 피해 지나가고, 누군가는 그 앞에서 잠시 멈춰 선다. 창밖 가로등이 하나씩 켜진다.)",
			"(악보 위에 커피 자국이 번져 있다. 누군가 그 부분을 가리고 연주하듯 손가락만 움직인다. 종이 냄새와 로스팅 냄새가 섞인다.)",
			"(누군가 무릎 보호대를 벗어 던지고 다시 주워 찬다. 누군가 웃으며 ‘괜찮다’고 말하지만 발끝은 아직 떨린다. 스피커가 잠시 침묵하고 다시 같은 소절이 흐른다.)",
			"(연습실 한쪽 구석에 쌓인 박스가 무너질 듯 흔들린다. 누군가 박스를 짚고 지나가고, 누군가는 그 자리에 앉아 호흡만 센다. 먼지 한 줄기가 햇빛에 떠오른다.)",
			"(누군가 손목을 돌리다 멈추고 시계를 본다. 누군가는 시계를 보지 않고도 박자를 세고 있다. 두 시선이 교차하고 다시 거울로 돌아간다.)",
			"(녹음 장비 표시등이 녹색에서 주황으로 바뀐다. 누군가 ‘컷’이라고 말하려다 삼키고, 누군가는 이미 다음 소절로 넘어간다. 방 안 공기가 팽팽하게 당겨진다.)",
			"(누군가 창문에 이마를 기대었다 떼고, 유리에 남은 김이 천천히 사라진다. 밖에서는 비가 그쳤는지 소리가 끊겼다. 누군가 ‘다시’라고 말하고 모두가 제자리로 돌아간다.)",
			"(연습복 지퍼 소리가 잇따라 난다. 누군가 옷을 여미고, 누군가는 소매를 걷어 올린다. 거울 앞 줄이 한 줄로 길어졌다 짧아진다. 누군가 앞으로 나오라는 손짓을 한다.)",
			"(누군가 스마트워치 알림을 끄고 다시 팔을 든다. 누군가는 알림이 울려도 무시하고 같은 구간을 세 번째 반복한다. 바닥이 미세하게 떨리는 것 같다가 멈춘다.)",
			"(누군가 콧노래를 흥얼거리다 멈추고, 팀 전체가 그 멜로디를 한 박자 늦게 따라 한다. 누군가 부끄러워하며 입을 다물고, 누군가는 그 멜로디로 MR을 맞춘다.)",
			"(프로젝터가 꺼진 벽에 빛 반점만 남는다. 누군가 리모컨을 책상에 올려두고, 누군가는 어둠 속에서 동작만 이어간다. 누군가 스탠드를 켜자 얼굴이 반쯤 드러난다.)",
			"(누군가 문 손잡이를 잡았다 놓는 소리가 난다. 아무도 나가지 않는다. 누군가 ‘조금만’이라고 말하고, 누군가가 ‘응’이라고 답한다. 그 직후 MR이 처음부터 재생된다.)",
			"(바닥에 떨어진 물방울이 신발 밑에서 미끄러진다. 누군가 걸레질을 하려다 멈추고, 누군가는 그 위를 피해 스텝을 바꾼다. 습기 냄새가 코끝에 닿는다.)",
			"(누군가 벽에 기대어 눈을 감고, 누군가는 바닥에 앉아 무릎을 끌어안는다. 둘 사이에 말 없이 박자만 오간다. 멀리서 엘리베이터가 도착하는 소리가 난다.)",
			"(연습실 시계가 정각을 가리키는데 아무도 끄지 않는다. 누군가 ‘한 곡만’이라고 말하고, 누군가가 ‘끝까지’라고 덧붙인다. 음악이 그 말을 덮고 지나간다.)",
			"(누군가 향수 냄새가 난다고 말하고, 누군가는 섬유유연제라고 고친다. 둘 다 웃다가 금세 입을 다문다. 그 짧은 웃음이 방 안에 잔향처럼 남는다.)"
	};

	private static String pickRandomMinimalSituation() {
		return MINIMAL_SITUATION_POOL[ThreadLocalRandom.current().nextInt(MINIMAL_SITUATION_POOL.length)];
	}

	/**
	 * 채팅 반응 지문: 연습실·공간(기본)과 프로듀서 입력에 대한 팀 반응을 한 블록으로 합친다.
	 */
	private static String mergeSceneAndProducerReaction(String sceneAmbient, String producerReaction) {
		String a = nz(sceneAmbient).trim();
		String b = nz(producerReaction).trim();
		if (a.isEmpty() && b.isEmpty()) {
			return "";
		}
		if (b.isEmpty()) {
			return a;
		}
		if (a.isEmpty()) {
			return "【프로듀서 말에 대한 반응】\n" + b;
		}
		return "【연습실·씬】\n" + a + "\n\n【프로듀서 말에 대한 반응】\n" + b;
	}

	/** 폴백·최소 경로용: 프로듀서 입력이 없을 때도 짧은 ‘반응’ 문단을 붙인다. */
	private static String pickRandomProducerReactionFallback(String userText) {
		String u = userText == null ? "" : userText.trim();
		String[] pool = {
				"누군가 프로듀서 쪽을 힐끗 본다. 말이 아니라 분위기로 전해진 무게가 남는다. 거울 너머로 누군가의 호흡이 얕아진 게 보인다.",
				"지시가 떨어진 직후, 숨을 고르는 소리만 잠깐 겹친다. MR이 한 박자 늦게 깔리고, 바닥 테이프가 발에 달라붙었다 떨어진다.",
				"팀 안에 짧은 정적이 있다가, 누군가 작게 고개를 끄덕인다. 그 움직임이 전염이라도 되듯 다른 이들도 미세하게 자세를 고친다.",
				"프로듀서의 한 마디가 연습실 공기를 살짝 무겁게 만든다. 누군가 물병을 돌리다 손이 멈추고, 창문 틈으로 바람이 스친다.",
				"누군가 발끝으로 박자를 짚으며, 방금 말을 곱씹는다. 스피커에서 잔향이 남고, 누군가 혼자 중얼거리다 입을 다문다.",
				"멤버들 사이에 시선이 스친다. 누가 먼저 움직일지 기다리는 순간이다. 에어컨 소리만 규칙적으로 이어지고, 형광등이 한 번 깜빡인다.",
				"MR이 잠시 낮아지고, 누군가 물을 마시지도 않은 채 병만 쥔다. 모니터 화면이 꺼졌다 켜지고, 녹음 표시등만 붉게 남는다.",
				"프로듀서 노트를 향해 시선이 모였다 흩어진다. 누군가 펜 소리를 내지 않으려다 한 줄만 더 긋는다.",
				"복도 쪽에서 웃음 소리가 났다 잦아들고, 방 안만 다시 조용해진다. 누군가 스트레칭을 멈추고 귀를 기울인다.",
				"누군가 셔츠 깃을 정돈하고, 다른 이는 그대로 포즈를 유지한다. 말보다 먼저 몸이 반응하는 순간이다.",
				"바닥에서 매트가 살짝 밀리는 소리가 나고, 누군가 발로 다시 고정한다. 누군가 ‘…알겠어’라고 말하려다 삼킨다.",
				"프로듀서 테이블 위 종이가 바스락이고, 누군가 그 소리에 맞춰 숨을 들이쉰다. 팀의 긴장이 실처럼 얽힌다.",
				"녹음부스 유리 너머로 누군가 손짓만 한다. 방 안에서는 그 제스처를 해석하느라 잠시 동작이 멈춘다. 누군가 고개를 끄덕이고, 누군가는 아직도 거울만 본다.",
				"메트로놈 클릭이 벽에 부딪혀 돌아온다. 누군가 박자를 앞당기려다 스스로 멈추고, 누군가는 발끝으로만 소절의 끝을 짚는다. 스피커에서 잔향이 길게 남는다.",
				"누군가 가사지를 넘기다 한 페이지가 찢어진 걸 발견하고 테이프로 붙인다. 누군가 그 틈에 혀를 찬다. 누군가는 이미 다음 구간을 입에 담고 있다.",
				"연습실 냉장고 문이 열렸다 닫히는 소리가 난다. 누군가 캔을 따려다 손이 미끄러지고, 누군가가 대신 받쳐 준다. 둘 사이에 짧은 웃음이 스친다.",
				"누군가 무릎을 짚고 일어나려다 다시 앉는다. 누군가는 그대로 스쿼트 자세를 유지한 채 숫자를 센다. 바닥이 땀으로 미끄럽다는 말이 오간다.",
				"프로듀서 쪽 시계가 딸깍이는 소리가 유난히 크다. 누군가 그 소리에 맞춰 눈만 깜빡이고, 누군가는 아예 귀를 막는 시늉을 한다. MR이 한 박자 늦게 이어진다.",
				"누군가 창문 틈으로 담배 냄새가 난다고 말하고, 누군가는 창을 닫는다. 밖 소음이 끊기자 방 안 호흡이 더 또렷해진다. 누군가 ‘집중’이라고 속삭인다.",
				"누군가 스마트폰을 집어 들었다가 화면만 끄고 내려놓는다. 누군가는 그걸 보고도 모른 척 같은 자세로 돌아간다. 형광등이 한 줄기로만 방을 가른다."
		};
		if (!u.isEmpty()) {
			String[] withUser = {
					"프로듀서가 내민 말이 아직 공기에 남아, 누군가 호흡부터 다시 맞춘다. 누군가 거울 속 자신의 눈썹만 움직인다.",
					"방금 들린 지시를 두고 누군가 입술을 깨문다. 팀의 리듬이 한 박자 늦게 따라붙고, 누군가 손등으로 카운트를 짚는다.",
					"말의 뉘앙스를 곱씹는 듯 누군가 고개를 숙였다 든다. 누군가는 이미 동작을 되짚기 시작하고, 다른 이는 잠시 멈춰 선다.",
					"프로듀서 말에 맞춰 누군가 동작을 되짚고, 다른 이는 거울만 응시한다. 누군가 작게 혀를 찬다.",
					"지시가 팀에 스며드는 동안, 누군가 작게 ‘알겠어’라고 중얼거린다. 그 말이 끝나기 전에 누군가 이미 발을 움직인다.",
					"말한 내용을 팀만의 방식으로 해석하려는 듯 시선이 엇갈린다. 누군가 웃음을 참다가 입꼬리만 올렸다 내린다.",
					"프로듀서의 말을 계기로 누군가 자리에서 살짝 움직인다. 의자가 바닥을 긁는 소리가 나고, 누군가 물을 따르려다 병만 돌린다.",
					"방금 말이 아직 귓가에 맴돌고, 누군가는 그 리듬에 맞춰 고개를 끄덕이다 멈춘다. 창밖이 잿빛으로 보인다.",
					"지시의 의도를 몸으로 확인하듯 누군가 어깨를 돌리고, 누군가는 손끝으로 가상의 선을 긋는다. MR이 잠시 멈춘다.",
					"프로듀서 쪽과 거울 사이에서 시선이 한 박자 늦게 오간다. 누군가 ‘한 번만’이라고 말하고 모두가 같은 소절로 되돌아간다.",
					"말이 아닌 무게가 먼저 와닿은 듯 누군가 발끝으로 선을 밟는다. 팀 안에 짧은 침묵이 있다가 누군가 먼저 숨을 내쉰다.",
					"프로듀서 말에 대한 해석이 갈리는 듯 누군가 눈을 마주쳤다 피한다. 누군가 테이블을 두드리다 손을 거둔다.",
					"방금 지시가 ‘맞다’와 ‘어렵다’ 사이에서 흔들린다. 누군가 입술을 깨물고, 누군가는 이미 동작을 반으로 나누어 연습한다. 거울 속에서 누군가만 한 박자 앞선다.",
					"프로듀서 말이 끝나기 전에 누군가 손을 들었다 내려놓는다. 질문이었는지 동의였는지 아무도 묻지 않는다. 누군가 ‘일단 해보자’라고 말하고 모두가 같은 소절로 되돌아간다.",
					"지시의 단어 하나가 팀마다 다른 색으로 들리는 듯하다. 누군가 고개를 갸우뚻하고, 누군가는 바로 메모에 밑줄을 긋는다. 누군가 펜 끝으로 테이블을 두드리다 멈춘다.",
					"프로듀서 쪽과 거울 사이에서 시선이 오간다. 누군가는 프로듀서를 보고, 누군가는 거울 속 동료만 본다. 누군가 ‘들었어’라고 말하자 둘 다 호흡을 맞춘다.",
					"말의 끝에 걸린 의도를 몸으로 확인하듯 누군가 어깨를 돌린다. 누군가는 그 움직임을 따라 하다 한 박자 늦는다. MR이 잠시 끊기고 같은 구간이 처음부터 재생된다.",
					"프로듀서 말이 아직 귓가에 남은 채로 누군가 물을 마시지도 않고 병만 돌린다. 누군가는 그 병을 받아 따 주려다 손이 스친다. 누군가 작게 ‘고마워’라고 말한다.",
					"지시를 ‘내 파트만’으로 좁혀 해석하는 이와 ‘전체’로 받는 이가 있다. 누군가 손등으로 파트 번호를 쓰고, 누군가는 고개만 끄덕인다. 방 안 공기가 잠깐 얇아졌다 두꺼워진다.",
					"프로듀서 말에 맞춰 누군가는 속도를 올리고, 누군가는 속도를 늦춘다. 둘이 스치자 누군가 ‘미안’이라고 말하고 누군가는 ‘괜찮아’라고 답한다. 그 직후 리더가 카운트를 세기 시작한다."
			};
			pool = withUser;
		}
		return pool[ThreadLocalRandom.current().nextInt(pool.length)];
	}

	/** DB·쿼터 폴백: 프로듀서 입력이 있으면 인용 + 반응 문단을 붙인다. */
	private static String buildProducerParagraphForFallback(String userText) {
		String u = userText == null ? "" : userText.trim();
		if (u.isEmpty()) {
			return pickRandomProducerReactionFallback("");
		}
		String snippet = sanitizeUserSnippetForNarration(u, 96);
		return "프로듀서가 내민 말(『" + snippet + "』)에 대한 반응이 공기에 남는다. "
				+ pickRandomProducerReactionFallback(u);
	}

	/** 폴백 대사: (지문)에 행동 클로즈업을 덧붙인다(과도한 장문 방지로 약 45%만). */
	private static String maybeExtendFallbackDialogueLine(String line) {
		if (line == null || line.isBlank()) {
			return line;
		}
		if (ThreadLocalRandom.current().nextInt(20) >= 11) {
			return line;
		}
		String[] tails = {
				"숨을 한 번 고르고 다시 거울을 본다",
				"누군가의 시선이 스쳐 지나간다",
				"손끝으로 박자만 짚어 본다",
				"말끝을 곱씹다가 작게 고개를 끄덕인다",
				"잠깐 눈을 감았다 뜨고 같은 구간을 되짚는다",
				"어깨에 힘이 들어갔다가 천천히 풀린다",
				"웃음을 참다가 입꼬리만 올렸다 내린다",
				"물병을 돌리다 손이 멈춘다",
				"발끝으로 바닥 선을 다시 긋는다",
				"프로듀서 쪽을 힐끗 보았다 시선을 거두고 호흡을 맞춘다",
				"작게 중얼거리다 말고 다시 자세를 잡는다",
				"귓가에 남은 말을 떠올리며 한 박자 늦게 움직인다"
		};
		String tail = tails[ThreadLocalRandom.current().nextInt(tails.length)];
		Matcher fm = FORMATTED_MEMBER_LINE.matcher(line.trim());
		if (fm.matches()) {
			String inner = fm.group(1).trim();
			String dialogue = fm.group(2);
			String merged = inner + ", " + tail;
			return "(" + merged + ")\n\"" + dialogue + "\"";
		}
		return formatStyledMemberLine(tail, line);
	}

	/** 큰따옴표 안 대사: 공백 포함 15~40자로 맞춤 */
	private static String clampDialogueLength(String dialogue) {
		if (dialogue == null) {
			return "……여기서 호흡만 맞추자.";
		}
		String t = dialogue.trim();
		if (t.isEmpty()) {
			return "……여기서 호흡만 맞추자.";
		}
		if (t.length() > 40) {
			return t.substring(0, 37) + "…";
		}
		if (t.length() < 15) {
			String[] pads = { " 같이 가자.", " 한 번 더.", " 여기서 맞추자.", " 끝까지 붙자." };
			for (String p : pads) {
				if (t.length() >= 15) {
					break;
				}
				if (t.length() + p.length() <= 40) {
					t = t + p;
				}
			}
			if (t.length() < 15) {
				t = t + " 여기서 맞추자.";
			}
			if (t.length() > 40) {
				return t.substring(0, 37) + "…";
			}
		}
		return t;
	}

	private static String formatStyledMemberLine(String actionInner, String dialogue) {
		String inner = actionInner == null || actionInner.isBlank() ? "잠시 숨을 고른다" : actionInner.trim();
		return "(" + inner + ")\n\"" + clampDialogueLength(dialogue) + "\"";
	}

	/**
	 * API·구버전 한 줄을 (행동) + 줄바꿈 + "대사" 로 통일. 평문만 오면 행동을 보강한다.
	 */
	private static String normalizeMemberLineToActionDialogue(String raw, List<RosterItem> alive, RosterItem m, int lineIndex) {
		String t = stripMemberNamesFromLine(raw, alive);
		if (t == null || t.isBlank()) {
			return fallbackDialogueLine(m, shuffledFallbackVariant(m, lineIndex));
		}
		t = t.replace('\r', ' ').replaceAll("\\)[ \\t]*\"", ")\n\"");
		Matcher fm = FORMATTED_MEMBER_LINE.matcher(t.trim());
		if (fm.matches()) {
			return "(" + fm.group(1).trim() + ")\n\"" + clampDialogueLength(fm.group(2).trim()) + "\"";
		}
		String trimmed = t.trim();
		if (trimmed.startsWith("\"")) {
			int end = trimmed.indexOf('"', 1);
			if (end > 1) {
				return formatStyledMemberLine("잠시 숨을 고른다", trimmed.substring(1, end).trim());
			}
		}
		if (trimmed.startsWith("(") && trimmed.contains(")") && !trimmed.contains("\"")) {
			String inner = trimmed.startsWith("(") && trimmed.endsWith(")")
					? trimmed.substring(1, trimmed.length() - 1).trim()
					: trimmed;
			return formatStyledMemberLine(inner, "……여기서 호흡만 맞추자.");
		}
		if (!trimmed.contains("(")) {
			return formatStyledMemberLine("잠시 숨을 고른다", trimmed);
		}
		return formatStyledMemberLine("잠시 숨을 고른다", trimmed);
	}

	private static boolean isMale(RosterItem m) {
		return m != null && m.gender() == Gender.MALE;
	}

	/** 멤버 이름·「이름」 를 본문에서 제거(헤더 라벨에만 표시). */
	private static String stripMemberNamesFromLine(String text, List<RosterItem> alive) {
		if (text == null) {
			return "";
		}
		String t = text;
		if (alive != null) {
			for (RosterItem m : alive) {
				if (m == null || m.name() == null) {
					continue;
				}
				String n = m.name().trim();
				if (n.isEmpty()) {
					continue;
				}
				t = t.replace("「" + n + "」", "");
				t = t.replace("『" + n + "』", "");
				t = t.replace(n + " —", "");
				t = t.replace(n + "—", "");
				t = t.replace("— " + n, "");
				t = t.replace(n + ":", "");
				t = t.replace(n + " -", "");
				t = t.replace(n + "-", "");
			}
		}
		/* 「임의이름」 - 대사 / 「」—대사 등 모델·구버전 패턴(이름이 로스터와 어긋나도 제거) */
		t = t.replaceAll("「[^」]{1,48}」\\s*[-—–]\\s*", "");
		t = t.replaceAll("\\s{2,}", " ").trim();
		t = t.replaceAll("^[-—–\\s]+", "");
		t = t.replaceAll("^—\\s*", "");
		return t;
	}

	/** 프로듀서 입력이 있을 때 첫 멤버 대사를 짧게 교체(15~40자). */
	private static String producerReactiveShortDialogue(String userText, String stat) {
		String u = userText.toLowerCase(Locale.ROOT);
		if (containsAnyHint(u, VOCAL_HINTS) || "보컬".equals(stat)) {
			return pickOneOf("보컬은 말한 그대로, 호흡부터 맞출게.", "목소리 라인, 말한 대로 끝까지 붙일게.",
					"발성 쪽 지시, 그대로 소리에 걸어볼게.", "노래 파트, 말한 흐름 그대로 갈게.");
		}
		if (containsAnyHint(u, DANCE_HINTS) || "댄스".equals(stat)) {
			return pickOneOf("안무는 말한 동선 그대로 몸에 걸게.", "댄스 쪽, 말한 리듬에 발부터 맞출게.",
					"춤 라인, 지시한 그대로 카운트 맞출게.", "동선은 말한 대로, 스텝만 다시 잡을게.");
		}
		if (containsAnyHint(u, MENTAL_HINTS) || "멘탈".equals(stat)) {
			return pickOneOf("멘탈은 말한 속도로, 숨부터 맞출게.", "컨디션 말한 대로, 무리 안 하게 갈게.",
					"쉬라는 뜻, 팀 호흡으로 받아볼게.", "마음 쪽 지시, 분위기로 이해했어.");
		}
		if (containsAnyHint(u, STAR_HINTS) || "스타".equals(stat)) {
			return pickOneOf("스타성 말한 대로, 시선이랑 표정 맞출게.", "존재감은 말한 대로, 한 컷 각도 잡을게.",
					"비주얼 쪽, 지시한 임팩트로 갈게.", "노출 말한 흐름, 그대로 연습에 걸게.");
		}
		if (containsAnyHint(u, TEAM_HINTS) || "팀웍".equals(stat)) {
			return pickOneOf("팀 호흡 말한 대로, 역할만 다시 맞출게.", "합 말한 쪽, 그대로 묶어서 갈게.",
					"협업 지시, 순서만 짚어서 따라갈게.", "역할 말한 대로, 라인 정리해볼게.");
		}
		return pickOneOf("방금 말한 흐름, 그대로 몸에 걸어볼게.", "프로듀서 말, 팀 리듬으로 받아볼게.",
				"지시 말한 톤, 여기서 호흡에 새길게.", "말한 순서대로, 지금 구간만 짚어볼게.");
	}

	/**
	 * 한글 위주가 아닌 응답(중국어 한자 위주·영문만 장문 등)을 걸러 폴백한다.
	 */
	private static boolean needsKoreanScriptFallback(String s) {
		if (s == null) {
			return true;
		}
		String t = s.trim();
		if (t.isEmpty()) {
			return true;
		}
		int hangul = 0;
		int han = 0;
		int latin = 0;
		for (int i = 0; i < t.length(); i++) {
			char c = t.charAt(i);
			if (c >= '\uAC00' && c <= '\uD7A3') {
				hangul++;
			} else if (c >= '\u4E00' && c <= '\u9FFF') {
				han++;
			} else if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
				latin++;
			}
		}
		if (hangul >= 2) {
			return false;
		}
		if (han >= 3 && hangul == 0) {
			return true;
		}
		return hangul == 0 && t.length() >= 18 && latin >= 14;
	}

	private IdolDialogueBlock sanitizeGeminiDialogueBlock(IdolDialogueBlock block, List<RosterItem> alive) {
		if (block == null || alive == null || alive.isEmpty()) {
			return block;
		}
		String sit = block.situation();
		if (needsKoreanScriptFallback(sit)) {
			sit = pickRandomMinimalSituation();
			log.warn("[GameAiNarration] situation script not Korean enough → minimal fallback");
		}
		List<IdolChatLine> lines = block.lines();
		List<IdolChatLine> out = new ArrayList<>();
		for (int i = 0; i < lines.size(); i++) {
			IdolChatLine ln = lines.get(i);
			RosterItem m = alive.get(Math.min(i, alive.size() - 1));
			String text = normalizeMemberLineToActionDialogue(ln.text(), alive, m, i);
			if (needsKoreanScriptFallback(text)) {
				text = fallbackDialogueLine(m, fallbackVariant(m, i));
				log.warn("[GameAiNarration] member line script not Korean enough → personality fallback (idx={})", i);
			}
			out.add(new IdolChatLine(ln.traineeId(), ln.name(), ln.personalityLabel(), text));
		}
		return new IdolDialogueBlock(ensureNonBlankSituation(sit), out);
	}

	private static String sanitizeGeminiTurnNarration(String narration) {
		if (narration == null || narration.isBlank()) {
			return "연습이 이어지고, 팀의 호흡이 조금씩 달라진다.";
		}
		if (needsKoreanScriptFallback(narration)) {
			return "연습이 이어지고, 팀의 호흡이 조금씩 달라진다.";
		}
		return narration.trim();
	}

	/** 파싱·캐시·API 어떤 경로에서도 상황 지문이 빈 문자열로 내려가지 않게 함 */
	private static String ensureNonBlankSituation(String situation) {
		if (situation != null && !situation.isBlank()) {
			return situation.trim();
		}
		return pickRandomMinimalSituation();
	}

	/** API JSON에 situation이 비었을 때: 괄호형 풀을 한 줄 본문으로 맞춤 */
	private static String stripOuterParensForPlainLine(String s) {
		if (s == null) {
			return "";
		}
		String t = s.trim();
		if (t.length() >= 2 && t.startsWith("(") && t.endsWith(")")) {
			return t.substring(1, t.length() - 1);
		}
		return t;
	}

	/** 최소 지문 경로에서 대사 variant를 흔들어 같은 멤버·성격 조합도 문장이 겹치지 않게 함 */
	private static int shuffledFallbackVariant(RosterItem m, int rosterIndex) {
		int base = fallbackVariant(m, rosterIndex);
		int salt = ThreadLocalRandom.current().nextInt(97);
		return Math.floorMod(base + salt, 6);
	}

	public boolean isEnabled() {
		return useGeminiForNarration && geminiClient != null && geminiClient.isConfigured();
	}

	/**
	 * @param sceneId 현재 씬 DB id. {@code null}이면 선택 키 캐시 안 함.
	 * @return 비어 있으면 호출 측에서 키워드/DB 폴백
	 */
	public Optional<String> resolveChoiceKeyWithAi(Long sceneId, String userText, List<SceneResult.ChoiceItem> choices) {
		if (!isEnabled() || choices == null || choices.isEmpty()) {
			return Optional.empty();
		}
		String ut = userText == null ? "" : userText.trim();
		if (ut.isBlank()) {
			return Optional.empty();
		}
		String choiceKey = null;
		if (sceneId != null) {
			choiceKey = buildChoiceKeyCacheKey(sceneId, ut, choices);
			String hit = choiceKeyCache.get(choiceKey);
			if (hit != null) {
				log.debug("[GameAiNarration] choice key cache hit sceneId={}", sceneId);
				return Optional.of(hit);
			}
		}
		StringBuilder sb = new StringBuilder();
		for (SceneResult.ChoiceItem c : choices) {
			if (c == null) {
				continue;
			}
			sb.append("- 키: ").append(c.getKey()).append(", 라벨: ").append(nz(c.getText()))
					.append(", 스탯: ").append(nz(c.getStatTarget())).append("\n");
		}
		String sys = """
				너는 아이돌 육성 시뮬 'NEXT DEBUT'의 게임 마스터다.
				프로듀서(플레이어)가 채팅으로 훈련 지시를 보냈다. 아래 선택지 중 의도에 가장 맞는 키 하나만 고른다.
				반드시 JSON 한 객체만 출력한다. 형식: {"key":"A"} 처럼 대문자 키.
				어느 것에도 안 맞으면 {"key":"NONE"}.
				설명 문장을 붙이지 마라.""";
		String user = "프로듀서 입력:\n" + ut + "\n\n선택지:\n" + sb;
		String raw = geminiClient.generateStructured(sys, user, 0.2, true, 384);
		String key = parseChoiceKeyJson(raw, choices);
		if (key == null) {
			return Optional.empty();
		}
		if (choiceKey != null) {
			putBoundedCache(choiceKeyCache, choiceKey, key, CHOICE_KEY_CACHE_MAX_ENTRIES);
		}
		return Optional.of(key);
	}

	private static String buildChoiceKeyCacheKey(Long sceneId, String userText, List<SceneResult.ChoiceItem> choices) {
		String fp = choices.stream()
				.filter(Objects::nonNull)
				.sorted(Comparator.comparing(c -> nz(c.getKey()), String.CASE_INSENSITIVE_ORDER))
				.map(c -> nz(c.getKey()) + "\u0001" + nz(c.getText()) + "\u0001" + nz(c.getStatTarget()))
				.collect(Collectors.joining("\u0002"));
		return sceneId + ":" + userText + ":" + fp;
	}

	/**
	 * 씬 인트로 상황·대사. 순서: (1) 인메모리 캐시 (2) Gemini API (3) 쿼터 초과 시에만 DB 저장 지문 폴백 (4) 그 외 실패 시 최소 지문.
	 *
	 * @param runId   캐시 키용 (같은 씬 재방문 시 API 생략). {@code null}이면 캐시 안 함.
	 * @param sceneId 현재 씬 DB id. {@code null}이면 캐시 안 함.
	 */
	public Optional<IdolDialogueBlock> tryIntroDialogue(
			Long runId,
			Long sceneId,
			List<RosterItem> roster,
			String sceneTitle,
			String sceneDescription,
			Set<Long> excludedTraineeIds) {
		if (roster == null || roster.isEmpty()) {
			return Optional.empty();
		}
		List<RosterItem> alive = orderedAlive(roster, excludedTraineeIds);
		if (alive.isEmpty()) {
			return Optional.empty();
		}
		String cacheKey = null;
		if (runId != null && sceneId != null) {
			cacheKey = buildIntroCacheKey(runId, sceneId, alive, excludedTraineeIds);
			IdolDialogueBlock hit = introDialogueCache.get(cacheKey);
			if (hit != null) {
				log.debug("[GameAiNarration] intro cache hit runId={} sceneId={}", runId, sceneId);
				return Optional.of(new IdolDialogueBlock(ensureNonBlankSituation(hit.situation()), hit.lines()));
			}
		}
		if (!isEnabled()) {
			log.debug("[GameAiNarration] intro: 외부 LLM 비활성 → DB 저장 지문 폴백");
			return Optional.of(buildFallbackIntroBlock(sceneTitle, sceneDescription, alive));
		}
		String sys = """
				너는 한국어 라이트노벨풍 아이돌 육성 게임 'NEXT DEBUT'의 작가다.
				출력은 반드시 JSON 한 개뿐이다. 키는 situation(문자열), memberLines(문자열 배열)만 사용한다.
				situation: 씬 인트로이므로 **연습실·공간·소리·빛·동선·온도·냄새·촉감**까지 **8~14문장**(현재형). 문장마다 디테일이 겹치지 않게 바꿔 쓴다. 호흡·박자·거울·MR·발소리·땀·공기·시선 등 감각을 촘촘히. 아직 프로듀서 채팅은 없다. 멤버 대사를 미리 요약하지 말 것. 이모지 없이.
				memberLines: 아래 멤버 순서와 동일한 개수. 각 원소는 반드시 이 형식: 첫 줄에 (무대/행동 지문) 만, **다음 줄**에 큰따옴표 "대사"
				- 괄호 () 안은 **행동·상태**만(현재형). **맨 앞이 ( 로 시작**해야 한다. 손·발·시선·호흡 등 구체적으로.
				- 큰따옴표 "" 안은 **말하는 내용만**. **15~40자(공백 포함)**.
				- 순서: **행동 줄 다음 줄에 대사**. 이름·「이름」·대시(—)로 화자를 적지 말 것(헤더에 표시됨). 성별(남/여)에 맞는 말버릇·호흡. 성격에 맞게.
				""" + MODEL_KOREAN_OUTPUT_RULE;
		String user = "씬 제목: " + nz(sceneTitle) + "\n씬 설명: " + nz(sceneDescription) + "\n\n멤버(순서 고정):\n"
				+ formatRosterForPrompt(alive);
		StructuredGenerateResult gen = geminiClient.generateStructuredResult(sys, user, 0.82, true, 4096);
		if (gen.kind() == StructuredGenerateResult.Kind.QUOTA_EXCEEDED) {
			log.warn("[GameAiNarration] intro: 쿼터 초과 → 저장 지문 폴백");
			return Optional.of(buildFallbackIntroBlock(sceneTitle, sceneDescription, alive));
		}
		String raw = gen.rawText();
		Optional<IdolDialogueBlock> parsed = parseSituationLinesBlock(raw, alive);
		if (parsed.isPresent()) {
			IdolDialogueBlock b = parsed.get();
			IdolDialogueBlock fixed = new IdolDialogueBlock(ensureNonBlankSituation(b.situation()), b.lines());
			fixed = sanitizeGeminiDialogueBlock(fixed, alive);
			if (cacheKey != null) {
				putIntroCache(cacheKey, fixed);
			}
			return Optional.of(fixed);
		}
		log.warn("[GameAiNarration] intro: API 응답 없음 또는 파싱 실패 → 최소 지문(저장 지문 미사용)");
		return Optional.of(buildMinimalPlaceholderIntroBlock(alive));
	}

	/**
	 * 채팅 턴에서만 사용. API 실패 시 {@link #tryReactionBundle} 내부에서 호출. 스탯/입력/씬 힌트로 카테고리·무작위 상황.
	 */
	public IdolDialogueBlock buildFallbackDialogueBlockForChatTurn(
			String statNameFromResult,
			String userText,
			String sceneTitle,
			String sceneDescription,
			List<RosterItem> alive) {
		if (alive == null || alive.isEmpty()) {
			return new IdolDialogueBlock("", List.of());
		}
		String stat = pickStatForFallback(statNameFromResult, userText, sceneTitle, sceneDescription);
		String situation = mergeSceneAndProducerReaction(
				pickRandomFallbackSituation(stat),
				buildProducerParagraphForFallback(userText));
		List<IdolChatLine> lines = new ArrayList<>();
		for (int i = 0; i < alive.size(); i++) {
			RosterItem m = alive.get(i);
			IdolPersonality p = resolvePersonality(m);
			String line = fallbackDialogueLine(m, fallbackVariant(m, i));
			line = withUserReactiveDialogueSuffix(line, userText, stat, i);
			line = maybeExtendFallbackDialogueLine(line);
			if (line == null || line.isBlank()) {
				line = fallbackDialogueLine(m, fallbackVariant(m, i));
			}
			lines.add(new IdolChatLine(m.traineeId(), m.name(), p.getShortLabel(), line));
		}
		return new IdolDialogueBlock(situation, lines);
	}

	private static int fallbackVariant(RosterItem m, int rosterIndex) {
		long tid = m != null && m.traineeId() != null ? m.traineeId().longValue() : rosterIndex;
		return (int) (Math.abs(tid * 31L + rosterIndex * 17L) % 6);
	}

	private static boolean isKnownStatName(String s) {
		if (s == null) {
			return false;
		}
		String t = s.trim();
		return "보컬".equals(t) || "댄스".equals(t) || "멘탈".equals(t) || "스타".equals(t) || "팀웍".equals(t);
	}

	private static String pickStatForFallback(String statNameFromResult, String userText, String sceneTitle,
			String sceneDescription) {
		if (isKnownStatName(statNameFromResult)) {
			return statNameFromResult.trim();
		}
		return inferStatFromHints(userText, sceneTitle, sceneDescription);
	}

	private static boolean containsAnyHint(String norm, String[] hints) {
		if (norm == null || norm.isEmpty() || hints == null) {
			return false;
		}
		for (String h : hints) {
			if (h != null && !h.isBlank() && norm.contains(h.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

	private static String inferStatFromHints(String userText, String sceneTitle, String sceneDescription) {
		String u = nz(userText).toLowerCase(Locale.ROOT);
		if (containsAnyHint(u, VOCAL_HINTS)) {
			return "보컬";
		}
		if (containsAnyHint(u, DANCE_HINTS)) {
			return "댄스";
		}
		if (containsAnyHint(u, MENTAL_HINTS)) {
			return "멘탈";
		}
		if (containsAnyHint(u, STAR_HINTS)) {
			return "스타";
		}
		if (containsAnyHint(u, TEAM_HINTS)) {
			return "팀웍";
		}
		String c = (nz(sceneTitle) + " " + nz(sceneDescription)).toLowerCase(Locale.ROOT);
		if (containsAnyHint(c, VOCAL_HINTS)) {
			return "보컬";
		}
		if (containsAnyHint(c, DANCE_HINTS)) {
			return "댄스";
		}
		if (containsAnyHint(c, MENTAL_HINTS)) {
			return "멘탈";
		}
		if (containsAnyHint(c, STAR_HINTS)) {
			return "스타";
		}
		if (containsAnyHint(c, TEAM_HINTS)) {
			return "팀웍";
		}
		return STAT_ORDER[ThreadLocalRandom.current().nextInt(STAT_ORDER.length)];
	}

	/**
	 * DB에 시드된 스탯별 상황 지문 중 서로 다른 2개를 이어 붙여 분량을 늘린다. 없거나 1개면 임베디드 풀과 혼합.
	 * 조회 실패 시 {@link NarrationFallbackDefaults}에서 2문단을 고른다.
	 */
	private String pickRandomFallbackSituation(String stat) {
		try {
			String fromDb = pickTwoSituationsFromDbOrNull(stat);
			if (fromDb != null) {
				return fromDb;
			}
			fromDb = pickTwoSituationsFromDbOrNull("보컬");
			if (fromDb != null) {
				return fromDb;
			}
		} catch (Exception e) {
			log.warn("[GameAiNarration] DB 상황 폴백 조회 실패: {}", e.getMessage());
		}
		return pickTwoEmbeddedSituations(stat);
	}

	private String pickTwoSituationsFromDbOrNull(String stat) {
		List<NarrationFallbackSituation> rows = narrationFallbackSituationRepository
				.findByStatCategoryOrderBySortOrderAsc(stat);
		if (rows == null || rows.isEmpty()) {
			return null;
		}
		if (rows.size() >= 2) {
			ThreadLocalRandom rnd = ThreadLocalRandom.current();
			int i = rnd.nextInt(rows.size());
			int j = (i + 1 + rnd.nextInt(rows.size() - 1)) % rows.size();
			String two = rows.get(i).getSituationText() + " " + rows.get(j).getSituationText();
			if (rows.size() >= 3 && rnd.nextInt(100) < 42) {
				int k = rnd.nextInt(rows.size());
				int guard = 0;
				while ((k == i || k == j) && guard++ < rows.size() * 2) {
					k = rnd.nextInt(rows.size());
				}
				if (k != i && k != j) {
					return two + " " + rows.get(k).getSituationText();
				}
			}
			return two;
		}
		return rows.get(0).getSituationText() + " " + pickRandomFallbackSituationEmbedded(stat);
	}

	private static String pickRandomFallbackSituationEmbedded(String stat) {
		String[] pool = NarrationFallbackDefaults.FALLBACK_SITUATIONS_BY_STAT.get(stat);
		if (pool == null || pool.length == 0) {
			pool = NarrationFallbackDefaults.FALLBACK_SITUATIONS_BY_STAT.get("보컬");
		}
		if (pool == null || pool.length == 0) {
			return "(연습실 안, 공기가 무겁게 가라앉는다.)";
		}
		return pool[ThreadLocalRandom.current().nextInt(pool.length)];
	}

	/** 임베디드 풀에서 서로 다른 문단 2개(가능하면)를 이어 붙인다. */
	private static String pickTwoEmbeddedSituations(String stat) {
		String[] pool = NarrationFallbackDefaults.FALLBACK_SITUATIONS_BY_STAT.get(stat);
		if (pool == null || pool.length == 0) {
			pool = NarrationFallbackDefaults.FALLBACK_SITUATIONS_BY_STAT.get("보컬");
		}
		if (pool == null || pool.length == 0) {
			return "(연습실 안, 공기가 무겁게 가라앉는다.)";
		}
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		if (pool.length == 1) {
			return pool[0];
		}
		int i = rnd.nextInt(pool.length);
		int j = (i + 1 + rnd.nextInt(pool.length - 1)) % pool.length;
		String two = pool[i] + " " + pool[j];
		if (pool.length >= 3 && rnd.nextInt(100) < 40) {
			int k = rnd.nextInt(pool.length);
			int guard = 0;
			while ((k == i || k == j) && guard++ < pool.length * 3) {
				k = rnd.nextInt(pool.length);
			}
			if (k != i && k != j) {
				return two + " " + pool[k];
			}
		}
		return two;
	}

	/** API 없을 때도 프로듀서 입력이 상황에 스며든다는 느낌을 한 문장으로 덧붙인다. 문장 틀은 무작위로 바꾼다. */
	private static String prefixSituationWithUserEcho(String userText, String situation) {
		String u = userText == null ? "" : userText.trim();
		if (u.isEmpty()) {
			return situation;
		}
		String snippet = sanitizeUserSnippetForNarration(u, 140);
		return switch (ThreadLocalRandom.current().nextInt(6)) {
			case 0 -> "(방금 전 프로듀서의 말——" + snippet + "——이 아직 이 공간의 공기에 남아 있다.) " + situation;
			case 1 -> "(프로듀서가 던진 말——" + snippet + "——이 연습실 한가운데에 걸려 있다.) " + situation;
			case 2 -> "(방금 들린 지시——" + snippet + "——가 아직 귓가에 맴돈다.) " + situation;
			case 3 -> "('" + snippet + "'라는 말이 떠오른 직후의 정적이, 잠깐 공기를 무겁게 한다.) " + situation;
			case 4 -> "(프로듀서의 한 마디——" + snippet + "——가 팀 사이를 스친다.) " + situation;
			default -> "(말로만이 아니라 분위기로도 전해진 지시——" + snippet + "——가 남아 있다.) " + situation;
		};
	}

	private static String sanitizeUserSnippetForNarration(String raw, int maxLen) {
		String s = raw.replace('\r', ' ').replace('\n', ' ').trim();
		if (s.length() > maxLen) {
			return s.substring(0, maxLen) + "…";
		}
		return s;
	}

	/**
	 * 첫 번째 멤버 대사에만, 프로듀서 입력에 맞춰 큰따옴표 안 대사를 짧게 교체한다.
	 */
	private static String withUserReactiveDialogueSuffix(String baseLine, String userText, String stat, int memberIndex) {
		if (memberIndex != 0) {
			return baseLine;
		}
		String u = userText == null ? "" : userText.trim();
		if (u.isEmpty()) {
			return baseLine;
		}
		Matcher fm = FORMATTED_MEMBER_LINE.matcher(String.valueOf(baseLine).trim());
		if (fm.matches()) {
			String inner = fm.group(1).trim();
			String newD = clampDialogueLength(producerReactiveShortDialogue(u, stat));
			return "(" + inner + ")\n\"" + newD + "\"";
		}
		return baseLine + " " + userReactiveLineSuffix(u, stat);
	}

	private static String userReactiveLineSuffix(String userText, String stat) {
		String u = userText.toLowerCase(Locale.ROOT);
		if (containsAnyHint(u, VOCAL_HINTS)) {
			return pickOneOf("말한 대로, 목소리 호흡부터 맞춰볼게.", "지금 말, 보컬 라인으로 받아볼게.", "발성 쪽으로 이해했어. 바로 짚어볼게.",
					"그 말, 음정·호흡 쪽으로 받았어.", "보컬 파트로 해석해서 갈게.", "라인 말한 그대로 목소리에 걸어볼게.");
		}
		if (containsAnyHint(u, DANCE_HINTS)) {
			return pickOneOf("안무·동선 말한 그대로, 몸에 걸어볼게.", "댄스 쪽 지시로 받았어. 한 번 더 맞춰볼게.", "말한 리듬에 맞춰 발부터 다시.",
					"동선 말한 흐름, 그대로 따라갈게.", "춤 쪽으로 해석해서 움직여볼게.", "카운트 말한 대로 몸에 새겨볼게.");
		}
		if (containsAnyHint(u, MENTAL_HINTS)) {
			return pickOneOf("멘탈·컨디션 말한 거, 팀 분위기로 느껴져.", "쉬고 회복하라는 뜻으로 받을게.", "말한 대로, 무리 안 하게 조절해볼게.",
					"컨디션 말한 거, 분위기로 읽었어.", "말한 속도로 호흡 맞춰볼게.", "지금 말, 마음 쪽으로 새겨둘게.");
		}
		if (containsAnyHint(u, STAR_HINTS)) {
			return pickOneOf("카메라·노출 말한 쪽으로, 표정부터 잡아볼게.", "스타성 말한 대로, 한 컷 각도 잡아볼게.", "말한 존재감, 연습에 걸어볼게.",
					"비주얼 말한 대로, 시선부터 맞출게.", "말한 임팩트, 톤에 실어볼게.", "노출 말한 쪽으로 연출해볼게.");
		}
		if (containsAnyHint(u, TEAM_HINTS)) {
			return pickOneOf("팀으로 말한 거, 호흡 맞추는 쪽으로 받았어.", "역할·합 말한 대로, 정리해볼게.", "말한 협업 방향, 그대로 따라가볼게.",
					"합 말한 흐름, 그대로 묶어볼게.", "역할 말한 대로, 순서만 짚어볼게.", "팀 호흡 말한 대로 맞춰볼게.");
		}
		if (isKnownStatName(stat)) {
			return switch (stat) {
				case "보컬" -> pickOneOf("이번엔 보컬 쪽 말로 이해했어.", "목소리 파트로 받아볼게.", "발성에 맞춰 볼게.", "보컬 라인으로 해석했어.",
						"노래 쪽 말한 그대로 갈게.");
				case "댄스" -> pickOneOf("댄스 쪽으로 받았어.", "몸에 걸어볼게.", "동선 맞춰볼게.", "춤 말한 흐름 이해했어.", "안무로 받아볼게.");
				case "멘탈" -> pickOneOf("멘탈 말한 흐름, 기억해둘게.", "컨디션 말로 받았어.", "말한 분위기로 맞춰볼게.", "마음 쪽으로 읽었어.",
						"컨디션 말한 대로 조절해볼게.");
				case "스타" -> pickOneOf("스타성 말한 대로 짚어볼게.", "비주얼·노출 쪽으로 받았어.", "말한 임팩트, 연습에 넣어볼게.", "존재감 말한 대로 갈게.",
						"한 컷 말한 느낌으로 잡아볼게.");
				case "팀웍" -> pickOneOf("팀으로 말한 거, 그대로 묶어볼게.", "호흡 말한 대로 맞춰볼게.", "역할 말한 대로 정리해볼게.", "합 말한 쪽으로 이해했어.",
						"협업 말한 대로 따라갈게.");
				default -> genericUserReactiveSuffix();
			};
		}
		return genericUserReactiveSuffix();
	}

	private static String genericUserReactiveSuffix() {
		return pickOneOf("방금 말한 흐름대로 해볼게.", "프로듀서 말, 그대로 따라가볼게.", "지금 말한 거, 팀에 전해진 느낌으로 받았어.",
				"말한 순서대로 짚어볼게.", "그 말, 그대로 몸에 걸어볼게.", "지시 말한 톤, 기억해둘게.");
	}

	private static String pickOneOf(String... options) {
		if (options == null || options.length == 0) {
			return "";
		}
		return options[ThreadLocalRandom.current().nextInt(options.length)];
	}

	/** 쿼터 초과 시에만 사용. DB·앱 내 저장 지문 풀. */
	private IdolDialogueBlock buildFallbackIntroBlock(
			String sceneTitle,
			String sceneDescription,
			List<RosterItem> alive) {
		String stat = inferStatFromHints("", sceneTitle, sceneDescription);
		String situation = prefixSituationWithUserEcho("", pickRandomFallbackSituation(stat));
		List<IdolChatLine> lines = new ArrayList<>();
		for (int i = 0; i < alive.size(); i++) {
			RosterItem m = alive.get(i);
			IdolPersonality p = resolvePersonality(m);
			lines.add(new IdolChatLine(m.traineeId(), m.name(), p.getShortLabel(),
					maybeExtendFallbackDialogueLine(fallbackDialogueLine(m, fallbackVariant(m, i)))));
		}
		return new IdolDialogueBlock(situation, lines);
	}

	/** 키 없음·파싱 실패 등 — 저장 지문 풀 없이 짧게 이어감 */
	private IdolDialogueBlock buildMinimalPlaceholderIntroBlock(List<RosterItem> alive) {
		List<IdolChatLine> lines = new ArrayList<>();
		for (int i = 0; i < alive.size(); i++) {
			RosterItem m = alive.get(i);
			IdolPersonality p = resolvePersonality(m);
			lines.add(new IdolChatLine(m.traineeId(), m.name(), p.getShortLabel(),
					maybeExtendFallbackDialogueLine(fallbackDialogueLine(m, shuffledFallbackVariant(m, i)))));
		}
		return new IdolDialogueBlock(pickRandomMinimalSituation(), lines);
	}

	/**
	 * 모델이 대사를 비우거나 …만 줄 때 대체. (행동) 먼저, 큰따옴표 대사(15~40자). 성별·성격별 지문.
	 */
	private static String fallbackDialogueLine(RosterItem rosterItem, int variant) {
		IdolPersonality p = resolvePersonality(rosterItem);
		if (p == null) {
			p = IdolPersonality.BUBBLY;
		}
		boolean male = isMale(rosterItem);
		int v = Math.floorMod(variant, 6);
		return switch (p) {
			case BUBBLY -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "주먹을 살짝 불끈 쥐고 고개를 끄덕인다"
						: "손끝으로 박자를 허공에 두드린다", "좋아, 조금만 더 맞춰보자! 끝까지 붙자!");
				case 1 -> formatStyledMemberLine(male ? "어깨를 으쓱이며 미소를 짓는다"
						: "발끝으로 바닥을 짚으며 웃는다", "텐션 올려서, 이번엔 한 번 더 가보자!");
				case 2 -> formatStyledMemberLine(male ? "거울 속 시선을 따라 가슴을 편다"
						: "머리카락을 귀 뒤로 넘기며 숨을 들이쉰다", "이 느낌 그대로 살리자, 같이 가자!");
				case 3 -> formatStyledMemberLine(male ? "메트로놈 소리에 맞춰 고개를 흔든다"
						: "클럽하듯 손뼉을 한 번 칠 뻔하다 멈춘다", "지금 박자 좋아, 끝까지 안 놓칠 거야!");
				case 4 -> formatStyledMemberLine(male ? "물을 벌컥 마시고 병을 내려놓는다"
						: "물병을 돌리다 웃음을 터뜨린다", "한 소절만 더, 숨 맞추고 끝까지 가자!");
				default -> formatStyledMemberLine(male ? "스텝을 한 번 밟고 엄지를 치켜든다"
						: "양 손을 머리 위로 뻗었다 내린다", "여기서 터뜨리면 돼, 같이 가자!");
			};
			case CALM -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "목을 천천히 굴리며 호흡을 세다"
						: "손바닥으로 가슴을 짚고 눈을 감는다", "호흡부터 다시 잡을게. 천천히 가자.");
				case 1 -> formatStyledMemberLine(male ? "시계를 보지 않고 박자만 귀로 듣는다"
						: "발끝을 일렬로 맞추며 자세를 고친다", "천천히, 지금은 박자만 맞추면 돼.");
				case 2 -> formatStyledMemberLine(male ? "노트에 펜을 대지 않고 선만 긋는다"
						: "머리끈을 다시 묶으며 숨을 내쉰다", "지금 흐름 그대로 유지하자. 흔들지 말자.");
				case 3 -> formatStyledMemberLine(male ? "어깨 힘을 빼라는 듯 손을 내린다"
						: "목소리를 한 톤 낮추며 동작을 늦춘다", "억지로 밀지 말고, 호흡만 정리하자.");
				case 4 -> formatStyledMemberLine(male ? "끝음을 손끝으로 공중에 그린다"
						: "거울과 눈을 맞추고 입꼬리를 폈다 낮춘다", "끝음까지 여유 두고, 같이 가 보자.");
				default -> formatStyledMemberLine(male ? "발박자를 세며 고개만 끄덕인다"
						: "손등으로 한 박자를 가늠한다", "지금은 속도보다 맞춤이 먼저야. 알지?");
			};
			case TSUNDERE -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "시선을 피했다가 다시 거울을 본다"
						: "입술을 삐죽이다 이마를 매만진다", "…봐. 한 번 더 해보자, 이번엔 제대로.");
				case 1 -> formatStyledMemberLine(male ? "혀를 차고 발끝으로 선을 긋는다"
						: "팔짱을 끼었다가 풀고 한숨을 쉰다", "실수한 거? …알아. 다시 하면 돼.");
				case 2 -> formatStyledMemberLine(male ? "귀를 붉히며 카운트를 외운다"
						: "눈을 굴리다가도 발은 제자리에 둔다", "딱히 듣는 건 아니지만… 해보자, 한 번 더.");
				case 3 -> formatStyledMemberLine(male ? "입꼬리를 올렸다가 금세 내린다"
						: "고개를 돌려 웃음을 참는다", "…별로 기대 안 했는데, 이번엔 나아졌네.");
				case 4 -> formatStyledMemberLine(male ? "쓴웃음을 지으며 스텝을 다시 밟는다"
						: "손가락으로 머리카락을 털어 낸다", "투덜대도 결국 하잖아. 한 번 더 가자.");
				default -> formatStyledMemberLine(male ? "주머니에 손을 넣었다 빼며 자세를 잡는다"
						: "볼을 꼬집고 눈앞을 바로 본다", "인정하기 싫지만, 여기는 고치자. 알겠지?");
			};
			case GENTLE -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "옆을 힐끗 보고 부드럽게 고개를 끄덕인다"
						: "손을 내밀었다가 움찔하고 거둔다", "괜찮아, 같이 맞추면 돼. 천천히 와.");
				case 1 -> formatStyledMemberLine(male ? "무릎을 꿇고 앉았다 일어나며 웃는다"
						: "물티슈로 손을 닦고 다시 손을 맞잡는다", "여기만 살짝 손 보면 돼. 내가 옆에 있어.");
				case 2 -> formatStyledMemberLine(male ? "등을 툭 치지 않고 어깨만 스친다"
						: "눈을 마주치며 작게 인사한다", "내가 옆에서 맞춰줄게. 무리하지 말고.");
				case 3 -> formatStyledMemberLine(male ? "목소리를 낮추고 한 박자 양보한다"
						: "머리를 쓰다듬듯 제 스카프를 만진다", "너무 무리하지 말고, 호흡부터 하자.");
				case 4 -> formatStyledMemberLine(male ? "물을 건네며 눈썹만 올린다"
						: "손가락으로 ‘천천히’를 쓴다", "천천히 해도 괜찮아, 같이 가면 돼.");
				default -> formatStyledMemberLine(male ? "바닥을 두드리며 ‘여기까지’를 그린다"
						: "눈을 감았다 뜨며 미소 짓는다", "여기까지 왔으면 충분해. 조금만 더 가자.");
			};
			case PRANKSTER -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "윙크를 했다가 바로 표정을 바꾼다"
						: "장난스럽게 엉덩이를 살짝 흔든다", "분위기 살려서 가볼까? 너무 딱딱해~");
				case 1 -> formatStyledMemberLine(male ? "하이파이브를 날리다 손바닥만 맞춘다"
						: "웃음을 터뜨리며 물을 뿜을 뻔한다", "너무 진지해~ 한 번 웃고 가자, 진짜로.");
				case 2 -> formatStyledMemberLine(male ? "물병을 돌리다 손이 멈춘다"
						: "거울에 하트를 그렸다 지운다", "장난 아니고, 이번엔 진짜 간다? 약속.");
				case 3 -> formatStyledMemberLine(male ? "입술을 비쭉이며 박자를 두드린다"
						: "볼을 부풀렸다 바람을 뱉는다", "표정 풀고, 그다음에 카운트 맞추자!");
				case 4 -> formatStyledMemberLine(male ? "장난감 마이크를 흉내 낸다"
						: "깔깔 웃다가 바로 입을 다문다", "웃음 참으면 더 어색해져~ 그냥 터뜨려.");
				default -> formatStyledMemberLine(male ? "엄지와 검지로 하트를 만든다"
						: "손끝으로 공중에 별을 그린다", "분위기 띄우는 것도 실력이야. 인정해줘.");
			};
			case SERIOUS -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "턱을 괴고 거울 속 선을 짚는다"
						: "노트에 체크 표시를 또 남긴다", "여기서 기준을 다시 맞추자. 한 번에.");
				case 1 -> formatStyledMemberLine(male ? "스톱 손짓을 하고 숨을 고른다"
						: "눈을 가늘게 뜨고 끝음만 따라 부른다", "한 번 더. 이번엔 끝까지 밀어붙이자.");
				case 2 -> formatStyledMemberLine(male ? "발끝으로 테이프 선을 따라 걷는다"
						: "머리핀을 바로 잡고 고개를 든다", "집중해. 지금 구간이 승부야, 알지?");
				case 3 -> formatStyledMemberLine(male ? "주먹을 탁 쳐서 박자를 고정한다"
						: "손목시계를 보지 않고 호흡만 센다", "지금 흐름 끊기면 안 돼. 이어가자.");
				case 4 -> formatStyledMemberLine(male ? "실수한 구간만 손가락으로 짚는다"
						: "연필로 동그라미를 두 번 그린다", "실수 포인트만 다시 짚고 간다. 끝.");
				default -> formatStyledMemberLine(male ? "입을 다물고 동작만 반복한다"
						: "입술을 깨물고 같은 구간을 되감는다", "말 줄이고, 동작으로 증명하자. 지금.");
			};
			case PERFECTIONIST -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "각도를 손으로 재듯 허공에 선을 긋는다"
						: "거울에 얼굴을 더 가까이 붙인다", "디테일 하나만 더 잡자. 여기, 딱 한 줄.");
				case 1 -> formatStyledMemberLine(male ? "지우개로 표시를 고쳐 다시 쓴다"
						: "속눈썹 끝까지 신경 쓰듯 눈을 깜빡인다", "여기 각도가 아직 아쉬워. 한 번 더.");
				case 2 -> formatStyledMemberLine(male ? "발끝이 일직선인지 바닥과 비교한다"
						: "머리카락 한 올까지 거울로 확인한다", "조금만 더 정확하게. 대충은 싫어.");
				case 3 -> formatStyledMemberLine(male ? "스탑워치를 누르지 않고 박자만 듣는다"
						: "손톱 끝까지 정렬했다 펼친다", "끝처리, 아직 살짝 어긋났어. 다시.");
				case 4 -> formatStyledMemberLine(male ? "세 번째 반복이라 혀를 찬다"
						: "노트에 같은 줄을 세 번 밑줄긋는다", "같은 구간 세 번째야. 이번엔 맞추자.");
				default -> formatStyledMemberLine(male ? "자를 대듯 손날로 공중을 잘랐다 붙인다"
						: "립 라인을 고치듯 입술만 다시 그린다", "대충 넘기지 말고, 여기부터 다시.");
			};
			case SENSITIVE -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "귀를 움찔이며 고개를 숙인다"
						: "어깨를 움츠렸다 펴며 숨을 고른다", "방금 그 떨림… 나도 느껴졌어. 괜찮아.");
				case 1 -> formatStyledMemberLine(male ? "창밖을 보았다가 다시 안으로 시선을 돌린다"
						: "손등으로 팔의 소름을 쓸어내린다", "공기가 바뀐 것 같아… 천천히 가자.");
				case 2 -> formatStyledMemberLine(male ? "눈을 마주치려다 피하고 웃는다"
						: "물컵을 쥔 손이 떨려 테이블을 탁 친다", "…괜찮아? 숨부터 맞추자, 같이.");
				case 3 -> formatStyledMemberLine(male ? "고개를 끄덕이며 말없이 박수를 친다"
						: "눈가를 손등으로 눌렀다 뗀다", "말 없이 힘 주는 거… 고마워. 느껴졌어.");
				case 4 -> formatStyledMemberLine(male ? "호흡이 얕아지자 입을 살짝 벌린다"
						: "이마에 손을 얹고 한 박자 늦춘다", "예민해지기 쉬운 구간이야. 천천히 가자.");
				default -> formatStyledMemberLine(male ? "목젖이 움직이는 걸 참으며 고개를 든다"
						: "속삭이듯 입모양만 맞춘다", "괜찮다고 말하기 전에, 호흡부터 맞추자.");
			};
			case OPTIMISTIC -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "엄지를 치켜세우며 스텝을 밟는다"
						: "양팔을 벌려 원을 그린다", "괜찮아, 다음 동작 가자! 쉽게 쉽게!");
				case 1 -> formatStyledMemberLine(male ? "어깨를 으쓱이며 휘파람을 불 뻔한다"
						: "깡충 뛰었다 착지하며 웃는다", "한 흐름만 더 붙여보자! 여기까지 왔잖아!");
				case 2 -> formatStyledMemberLine(male ? "물병을 탁 치고 리듬을 탄다"
						: "머리를 흔들며 머리끈을 다시 묶는다", "여기까지 잘 왔어, 이어가자! 끝까지!");
				case 3 -> formatStyledMemberLine(male ? "박수를 한 번 치고 바로 자세를 잡는다"
						: "손뼉을 마주치며 눈을 반짝인다", "지금까지 충분히 잘했어, 한 번만 더!");
				case 4 -> formatStyledMemberLine(male ? "넘어진 시늉을 했다 일어난다"
						: "무릎을 굽혔다 펴며 웃음을 터뜨린다", "넘어져도 다시 세우면 돼! 같이!");
				default -> formatStyledMemberLine(male ? "하늘을 가리키며 ‘할 수 있어’를 입모양으로 말한다"
						: "하트를 그렸다 주먹으로 감싼다", "분위기 좋아, 그대로 가자! 믿어!");
			};
			case SHY -> male ? switch (v) {
				case 0 -> formatStyledMemberLine("목덜미를 긁으며 시선을 깔고 앉는다",
						"그… 나도 다시 해볼게. 한 번만 봐줘.");
				case 1 -> formatStyledMemberLine("신발 끈을 만지작거리다 고개를 든다",
						"미안… 이번엔 더 맞출게. 진심이야.");
				case 2 -> formatStyledMemberLine("옆을 힐끗 보다가 금세 앞만 본다",
						"옆이랑… 호흡 맞춰볼게. 천천히.");
				case 3 -> formatStyledMemberLine("입술을 깨물고 한 마디만 더 내뱉는다",
						"부끄럽지만… 이번엔 말할게. 들어줘.");
				case 4 -> formatStyledMemberLine("악보만 보다가 눈을 들어 말한다",
						"내 파트… 조금만 기다려줘. 준비됐어.");
				default -> formatStyledMemberLine("손바닥에 땀이 배어 바지를 닦는다",
						"떨리는데… 그래도 해볼게. 같이.");
			} : switch (v) {
				case 0 -> formatStyledMemberLine("손가락을 꼼지락거리다 작게 고개를 든다",
						"저… 저도 다시 해볼게요. 조금만요.");
				case 1 -> formatStyledMemberLine("목소리가 잠겨 다시 숨을 고른다",
						"죄송해요… 이번엔 더 맞출게요. 정말로.");
				case 2 -> formatStyledMemberLine("옆 사람 소매를 살짝 잡았다 놓는다",
						"옆분이랑… 호흡 맞춰볼게요. 천천히.");
				case 3 -> formatStyledMemberLine("볼을 붉히고 눈만 마주친다",
						"부끄럽지만… 이번엔 말할게요. 들어주세요.");
				case 4 -> formatStyledMemberLine("악보 끝을 짚으며 입을 연다",
						"제 파트… 조금만 기다려 주세요. 할게요.");
				default -> formatStyledMemberLine("무릎을 모았다 펴며 떨림을 누른다",
						"떨리는데요… 그래도 해볼게요. 같이요.");
			};
			case LEADER -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "손뼉으로 박자를 켠다"
						: "손가락으로 하나 둘 셋을 허공에 찍는다", "정리하고 한 번 더 가자. 내가 앞에 설게.");
				case 1 -> formatStyledMemberLine(male ? "모두를 한 번 훑고 카운트를 외운다"
						: "손목을 스냅하며 템포를 알린다", "여기부터 다시. 내가 카운트 줄게. 따라와.");
				case 2 -> formatStyledMemberLine(male ? "동선을 손끝으로 가상으로 그린다"
						: "팀을 반원으로 모으는 시늉을 한다", "순서만 잡으면 돼. 내가 정리해줄게.");
				case 3 -> formatStyledMemberLine(male ? "문제 구간만 손가락으로 짚는다"
						: "보드에 동그라미를 치고 돌아선다", "지금 헷갈리는 부분만 딱 짚고 가자.");
				case 4 -> formatStyledMemberLine(male ? "앞에 서서 등으로 박자를 보여준다"
						: "손등으로 ‘내가 받을게’를 쓴다", "내가 앞에서 받을 테니까, 뒤는 나한테 맡겨.");
				default -> formatStyledMemberLine(male ? "목소리를 낮추고 한 줄로 요약한다"
						: "손바닥을 내려 치며 ‘여기’를 가리킨다", "한 마디로 정리하면, 여기서 맞추자. 끝.");
			};
			case FREE_SPIRIT -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "넥타이를 풀고 셔츠 단추를 하나 푼다"
						: "머리를 흩날리며 스핀을 한 바퀴 돈다", "틀에 안 맞춰도 돼, 우리만의 호흡으로!");
				case 1 -> formatStyledMemberLine(male ? "즉흥으로 박자를 두드리며 웃는다"
						: "안무를 살짝 바꿔 발끝으로 시험한다", "즉흥으로 한 번 가볼까? 느낌 온다.");
				case 2 -> formatStyledMemberLine(male ? "눈을 감고 몸이 이끄는 대로 스텝을 밟는다"
						: "스카프 끝을 흔들며 리듬을 탄다", "느낌 온다, 그대로 가자. 말 말고 몸으로.");
				case 3 -> formatStyledMemberLine(male ? "바닥에 누워 있다가 벌떡 일어난다"
						: "무릎 꿇었다 일어나며 웃음을 터뜨린다", "너무 맞추려 하지 말고, 몸이 이끄는 대로.");
				case 4 -> formatStyledMemberLine(male ? "정석을 접어 던지는 시늉을 한다"
						: "거울에 하트 대신 번개를 그린다", "틀 깨는 것도 연습이야. 부정하지 마.");
				default -> formatStyledMemberLine(male ? "발차기 시늉으로 공간을 잰다"
						: "원을 그리며 동선을 새로 깐다", "같은 길만 반복하긴 싫거든. 알지?");
			};
			case COOL -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "표정을 지우고 턱만 살짝 든다"
						: "머리카락 너머로 눈만 맞춘다", "감정 빼고 동작만 맞추자. 소음 끄자.");
				case 1 -> formatStyledMemberLine(male ? "입을 다물고 손짓만으로 박자를 짚는다"
						: "목소리를 거의 속삭이듯 낮춘다", "소리 줄이고, 동선만. 말은 최소로.");
				case 2 -> formatStyledMemberLine(male ? "고개를 옆으로 젖히고 한숨만 낸다"
						: "콧방귀만 끼고 엄지를 살짝 든다", "굳이 말 안 해도 알지? 한 번 더.");
				case 3 -> formatStyledMemberLine(male ? "어깨를 으쓱이지 않고 팔만 뻗는다"
						: "손목만 돌려 관절을 푼다", "불필요한 힘 빼. 거기만 딱 잡아.");
				case 4 -> formatStyledMemberLine(male ? "이어폰을 귀에서 빼고 고요를 듣는다"
						: "입술에 손가락을 대고 쉿 하는 시늉을 한다", "시끄러운 건 잠깐 끄자. 집중.");
				default -> formatStyledMemberLine(male ? "발끝만 움직이고 얼굴은 굳인다"
						: "거울 속 자신만 응시하고 고개를 끄덕인다", "말 대신, 박자로 답하자. 지금.");
			};
			case COMPETITIVE -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "이를 악물고 주먹을 꽉 쥔다"
						: "눈썹을 찌푸리고 거울과 눈싸움한다", "여기서 지면 안 돼. 한 번 더 밀어붙여.");
				case 1 -> formatStyledMemberLine(male ? "스쿼트 자세로 힘을 모은다"
						: "머리를 묶으며 끈을 세게 당긴다", "더 세게. 여기서 승부야. 빠지지 마.");
				case 2 -> formatStyledMemberLine(male ? "라인을 가르며 앞으로 한 걸음 내민다"
						: "손톱을 깨물다 말고 손등으로 박자를 친다", "끝까지 붙자, 빠지지 말고. 끝까지.");
				case 3 -> formatStyledMemberLine(male ? "바닥을 발로 쾅 치고 멈춘다"
						: "땀을 닦으며 눈을 부릅뜬다", "눈 감고 넘어가지 마. 여기서 끊어. 알지?");
				case 4 -> formatStyledMemberLine(male ? "심장 소리를 손으로 눌러 참는다"
						: "목덜미를 세게 비틀어 긴장을 푼다", "순위 생각하면 더 몸이 말을 들어. 진짜로.");
				default -> formatStyledMemberLine(male ? "상대를 힐끗 보고 시선을 거두지 않는다"
						: "입술을 깨물고 ‘끝’이라고 속삭인다", "한 명이라도 빠지면 끝이야. 집중해.");
			};
			case DEPENDENT -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "옆 팔꿈치를 살짝 찌른다"
						: "소매를 잡았다 놓으며 눈을 치켜뜬다", "옆에서 같이 맞춰줄게… 잠깐만 기다려.");
				case 1 -> formatStyledMemberLine(male ? "혼자 남은 시늉을 하며 손을 뻗는다"
						: "구석으로 한 발 물러섰다 다시 온다", "…나 혼자면 무서운데, 같이 해줘. 제발.");
				case 2 -> formatStyledMemberLine(male ? "물음표를 그리듯 손가락을 말았다 편다"
						: "고개를 갸웃하며 눈썹을 올린다", "이렇게 하면 돼? 알려줘. 한 번만.");
				case 3 -> formatStyledMemberLine(male ? "눈을 마주치자 안도의 한숨을 쉰다"
						: "손바닥을 마주 대며 호흡을 맞춘다", "눈만 마주쳐 줘도 힘이 나. 진짜로.");
				case 4 -> formatStyledMemberLine(male ? "등 뒤에서 옷자락을 잡는 시늉을 한다"
						: "무릎을 붙잡고 고개를 끄덕인다", "옆에 있어 주면… 할 수 있어. 믿어줘.");
				default -> formatStyledMemberLine(male ? "무대 끝을 바라보며 발을 떼지 못한다"
						: "손을 모으고 작게 빈다", "혼자 서 있기엔 너무 큰 무대 같아…");
			};
			case RELIABLE -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "앞에 서서 손을 내밀어 받친다"
						: "등을 토닥이며 앞을 가리킨다", "내가 받쳐줄 테니까. 뒤는 걱정 마.");
				case 1 -> formatStyledMemberLine(male ? "파트를 짚으며 고개를 끄덕인다"
						: "엄지와 검지로 체크 표시를 만든다", "네 파트, 내가 맞출게. 흔들리지 마.");
				case 2 -> formatStyledMemberLine(male ? "어깨를 두드리고 앞만 보라고 손짓한다"
						: "눈가를 눌러 주고 웃어 보인다", "뒤는 걱정 말고 앞만 봐. 내가 있어.");
				case 3 -> formatStyledMemberLine(male ? "틀어질 뻔한 동선을 손으로 바로잡는다"
						: "노트에 화살표를 그려 건넨다", "틀어질 뻔한 거 내가 살렸어. 계속 가.");
				case 4 -> formatStyledMemberLine(male ? "박자가 흔들릴 때 손뼉으로 고정한다"
						: "손등으로 리듬을 쳐 주며 눈을 맞춘다", "네가 흔들려도 내가 라인 잡을게. 믿어.");
				default -> formatStyledMemberLine(male ? "주먹을 가볍게 맞대며 약속한다"
						: "브이가 아니라 엄지를 건넨다", "믿고 한 번 더 밀어봐. 내가 받을게.");
			};
			case BLUNT -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "손가락으로 바닥을 쿡 찌른다"
						: "연필로 X자를 크게 그린다", "지금은 솔직히, 여기가 어긋났어. 인정해.");
				case 1 -> formatStyledMemberLine(male ? "팔짱을 끼고 턱으로 구간을 가리킨다"
						: "눈썹을 치켜올리며 한숨을 쉰다", "돌려 말하면 끝없어. 여기 고쳐. 끝.");
				case 2 -> formatStyledMemberLine(male ? "감으로 넘긴다는 말에 고개를 저었다 든다"
						: "노트를 탁 쳐서 페이지를 넘긴다", "감으로 넘기지 말고, 여기부터 다시.");
				case 3 -> formatStyledMemberLine(male ? "손목시계를 툭 두드리며 시간을 재촉한다"
						: "숫자를 손바닥에 쓴다", "느낌 좋다는 말 필요 없어. 여기 숫자로 와.");
				case 4 -> formatStyledMemberLine(male ? "모르는 척하는 표정을 지으며 입을 연다"
						: "손가락으로 탁탁 두드리며 멈춘다", "아는 척하지 말고, 다시. 한 번에.");
				default -> formatStyledMemberLine(male ? "변명하려는 입술을 손으로 막는 시늉을 한다"
						: "목소리 톤을 떨어뜨리고 단호히 말한다", "변명은 나중에. 지금은 동작으로 보여줘.");
			};
			case SENTIMENTAL -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "가슴에 손을 얹고 눈을 감는다"
						: "눈가를 손등으로 스치며 숨을 고른다", "마음까지 같이 맞춰보자. 여기서.");
				case 1 -> formatStyledMemberLine(male ? "창밖 하늘을 한참 바라본다"
						: "손끝으로 가슴 앞에 작은 하트를 그린다", "이 순간, 놓치기 아까운데… 천천히.");
				case 2 -> formatStyledMemberLine(male ? "목소리가 떨려 한 마디를 멈춘다"
						: "눈물을 참으며 웃어 보인다", "…조금만 더, 같이 가자. 끝까지.");
				case 3 -> formatStyledMemberLine(male ? "목젖이 움직이는 걸 참으며 고개를 든다"
						: "속눈썹이 떨리는 걸 손으로 가린다", "여기서 울먹이는 것도 연기야… 진짜로.");
				case 4 -> formatStyledMemberLine(male ? "말없이 손을 내밀어 악수를 청한다"
						: "손등 위에 손을 얹는 시늉을 한다", "말 안 해도 전해지게 해 보자. 지금.");
				default -> formatStyledMemberLine(male ? "조명을 올려다보며 한숨을 삼킨다"
						: "머리카락을 귀 뒤로 넘기며 눈을 반짝인다", "공기가 무거울 때가 제일 잘 맞을 때야.");
			};
			case LAID_BACK -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "하품을 참으며 어깨를 으쓱인다"
						: "바닥에 앉았다 일어나는 것도 귀찮은 시늉", "천천히, 그래도 리듬은 잡자. 급하지 않게.");
				case 1 -> formatStyledMemberLine(male ? "물병을 천천히 돌리며 리드를 넘긴다"
						: "스트레칭하듯 팔을 머리 위로 뻗는다", "급할 거 없어. 호흡만. 여유.");
				case 2 -> formatStyledMemberLine(male ? "박자를 반 박 늦춰 밟는다"
						: "눈을 반쯤 감고 고개를 흔든다", "느긋하게, 그래도 끊기지 않게 가자.");
				case 3 -> formatStyledMemberLine(male ? "소파처럼 의자에 몸을 맡긴다"
						: "발을 꼬고 앉아 리듬만 듣는다", "너무 당기지 말고, 길게 숨 쉬자.");
				case 4 -> formatStyledMemberLine(male ? "완벽보다 편안함이라고 혼잣말한다"
						: "턱을 괴고 거울을 빤히 본다", "오늘은 완벽보다 편안함. 알지?");
				default -> formatStyledMemberLine(male ? "어깨를 툭툭 털고 한 박자 늦게 움직인다"
						: "하품 대신 깊게 숨을 들이쉰다", "어깨 내려놓고, 그다음에 박자 맞추자.");
			};
			case REBELLIOUS -> switch (v) {
				case 0 -> formatStyledMemberLine(male ? "넥타이를 삐뚤게 매고 웃는다"
						: "스타킹을 걷어 올리듯 각오를 다진다", "규칙보다 느낌으로 한 번 더. 진짜로.");
				case 1 -> formatStyledMemberLine(male ? "지시문을 접어 주머니에 넣는 시늉을 한다"
						: "머리를 삐죽이며 거울에 혀를 내민다", "시키는 대로만 하기엔 아깝잖아. 그렇지?");
				case 2 -> formatStyledMemberLine(male ? "바닥 테이프를 살짝 찢었다 붙인다"
						: "안무지를 구겨 던졌다 주운다", "틀 깨고 가볼까, 여기서. 한 번만.");
				case 3 -> formatStyledMemberLine(male ? "정석이라는 말에 코웃음을 친다"
						: "체크무늬를 줄무늬로 바꾼 듯 스텝을 바꾼다", "정석만 따르긴 싫어. 우리 색으로.");
				case 4 -> formatStyledMemberLine(male ? "눈치를 보라는 말에 고개를 저었다 든다"
						: "손가락으로 귀를 막았다 뗀다", "눈치 보지 말고, 우리 색으로 가자.");
				default -> formatStyledMemberLine(male ? "답이 하나냐고 공중에 물음표를 그린다"
						: "하트 대신 번개를 손목에 그린다", "답은 하나가 아니라고 봐. 나는 그래.");
			};
		};
	}

	private static boolean isEllipsisOrBlankLine(String s) {
		if (s == null || s.isBlank()) {
			return true;
		}
		String t = s.replaceAll("[\\s·…\\.]+", "");
		return t.isEmpty();
	}

	private static String buildIntroCacheKey(Long runId, Long sceneId, List<RosterItem> alive, Set<Long> excluded) {
		StringBuilder sb = new StringBuilder();
		sb.append(runId).append(':').append(sceneId).append(':');
		if (excluded != null && !excluded.isEmpty()) {
			excluded.stream().sorted().forEach(id -> sb.append('x').append(id).append(','));
		}
		sb.append('|');
		for (RosterItem m : alive) {
			if (m != null && m.traineeId() != null) {
				sb.append(m.traineeId()).append(',');
			}
		}
		sb.append("|v2");
		return sb.toString();
	}

	private void putIntroCache(String key, IdolDialogueBlock block) {
		putBoundedCache(introDialogueCache, key, block, INTRO_CACHE_MAX_ENTRIES);
	}

	private static <K, V> void putBoundedCache(ConcurrentHashMap<K, V> map, K key, V value, int maxEntries) {
		while (map.size() >= maxEntries && !map.containsKey(key)) {
			var it = map.keySet().iterator();
			if (it.hasNext()) {
				map.remove(it.next());
			} else {
				break;
			}
		}
		map.put(key, value);
	}

	public record ReactionNarrationBundle(IdolDialogueBlock block, String resultNarration) {
	}

	/**
	 * 채팅 턴 반응 지문·대사. 순서: (1) 인메모리 캐시 (2) Gemini API (3) 쿼터 초과 시에만 DB 저장 지문 (4) 그 외 최소 지문.
	 *
	 * @param runId   캐시 키용. {@code null}이면 반응 번들 캐시 안 함.
	 * @param sceneId 현재 씬 DB id. {@code null}이면 캐시 안 함.
	 * @return 생존 멤버가 없을 때만 empty. 그 외에는 API 성공·쿼터 시 DB 폴백·그 외 최소 지문 번들.
	 */
	public Optional<ReactionNarrationBundle> tryReactionBundle(
			Long runId,
			Long sceneId,
			List<RosterItem> updatedRoster,
			String sceneTitle,
			String sceneDescription,
			String userText,
			String resolvedKey,
			String trainingCategory,
			StatChangeResult statResult,
			Set<Long> excludedTraineeIds) {
		if (updatedRoster == null || updatedRoster.isEmpty()) {
			return Optional.empty();
		}
		List<RosterItem> alive = orderedAlive(updatedRoster, excludedTraineeIds);
		if (alive.isEmpty()) {
			return Optional.empty();
		}
		if (!isEnabled()) {
			log.debug("[GameAiNarration] reaction: 외부 LLM 비활성 → DB 저장 지문 폴백");
			return Optional.of(buildFallbackReactionBundle(statResult, userText, sceneTitle, sceneDescription, alive));
		}
		String reactKey = null;
		if (runId != null && sceneId != null) {
			reactKey = buildReactionCacheKey(runId, sceneId, sceneTitle, sceneDescription, userText, resolvedKey,
					trainingCategory, statResult, excludedTraineeIds, alive);
			ReactionNarrationBundle hit = reactionBundleCache.get(reactKey);
			if (hit != null) {
				log.debug("[GameAiNarration] reaction bundle cache hit runId={} sceneId={}", runId, sceneId);
				return Optional.of(hit);
			}
		}
		String sys = """
				너는 'NEXT DEBUT' 게임의 작가다. 프로듀서 채팅 직후 연습생들의 반응을 쓴다.
				반드시 JSON 객체 하나만 출력한다. 키 이름(영문 camelCase): situation, sceneSituation, producerResponse, memberLines, resultNarration.
				sceneSituation: 연습실·공간·소리·움직임·빛·온도까지 **6~10문장**(현재형). 문장마다 다른 감각·디테일을 쓴다. 프로듀서 대사를 직접 인용하지 말 것.
				producerResponse: 프로듀서 채팅 입력에 팀이 어떻게 반응하는지 **6~10문장**(긴장·완화·집중·망설임·짧은 침묵 등). 입력의 의도가 몸으로 전해지게. 반복 표현을 피한다.
				situation: 위 둘을 합친 한 덩어리로만 써도 되고, 비우고 sceneSituation+producerResponse만 써도 된다. 둘 다 있으면 sceneSituation·producerResponse를 우선한다.
				memberLines: 문자열 배열. 아래 멤버 순서와 동일한 개수. 각 원소: 첫 줄 (행동 지문) 만, **다음 줄**에 "대사"
				괄호 안은 행동만, 큰따옴표 안은 말만. 대사 15~40자(공백 포함). **행동 줄 아래 줄에 대사**. 이름·「이름」을 본문에 넣지 말 것. 성별·성격에 맞게.
				resultNarration: 플레이어에게 보여 줄 턴 결과 요약 **3~5문장**(스탯/분위기 변화, 여운). 문장이 겹치지 않게.
				이모지 금지. 다른 키를 쓰지 마라.
				""" + MODEL_KOREAN_OUTPUT_RULE;
		StringBuilder ctx = new StringBuilder();
		ctx.append("씬: ").append(nz(sceneTitle)).append("\n배경: ").append(nz(sceneDescription)).append("\n");
		ctx.append("프로듀서 입력: ").append(nz(userText)).append("\n");
		ctx.append("적용된 훈련 키: ").append(nz(resolvedKey)).append(", 카테고리: ").append(nz(trainingCategory)).append("\n");
		if (statResult != null) {
			ctx.append("이번 턴 스탯: ").append(nz(statResult.statName())).append(" ")
					.append(statResult.delta() >= 0 ? "+" : "").append(statResult.delta())
					.append(" (").append(nz(statResult.traineeName())).append(")\n");
			ctx.append("팬 변화: ").append(statResult.fanDelta()).append("\n");
		}
		ctx.append("\n멤버(순서 고정):\n").append(formatRosterForPrompt(alive));
		StructuredGenerateResult gen = geminiClient.generateStructuredResult(sys, ctx.toString(), 0.82, true, 4096);
		if (gen.kind() == StructuredGenerateResult.Kind.QUOTA_EXCEEDED) {
			log.warn("[GameAiNarration] reaction: 쿼터 초과 → 저장 지문 폴백");
			ReactionNarrationBundle fb = buildFallbackReactionBundle(statResult, userText, sceneTitle, sceneDescription,
					alive);
			if (reactKey != null) {
				putBoundedCache(reactionBundleCache, reactKey, fb, REACTION_CACHE_MAX_ENTRIES);
			}
			return Optional.of(fb);
		}
		String raw = gen.rawText();
		Optional<IdolDialogueBlock> block = parseSituationLinesBlock(raw, alive);
		if (block.isEmpty()) {
			if (raw == null || raw.isBlank()) {
				log.warn("[GameAiNarration] reaction: Gemini 빈 응답 → 최소 지문 (key configured={})",
						geminiClient != null && geminiClient.isConfigured());
			} else {
				log.warn("[GameAiNarration] reaction: JSON 파싱 실패 → 최소 지문. First 500 chars: {}",
						raw.length() > 500 ? raw.substring(0, 500) + "…" : raw);
			}
			return Optional.of(buildMinimalPlaceholderReactionBundle(statResult, userText, sceneTitle, sceneDescription,
					alive));
		}
		String narration = extractResultNarration(raw);
		narration = sanitizeGeminiTurnNarration(narration == null ? "" : narration.trim());
		IdolDialogueBlock sanitizedBlock = sanitizeGeminiDialogueBlock(block.get(), alive);
		ReactionNarrationBundle bundle = new ReactionNarrationBundle(sanitizedBlock, narration);
		if (reactKey != null) {
			putBoundedCache(reactionBundleCache, reactKey, bundle, REACTION_CACHE_MAX_ENTRIES);
		}
		return Optional.of(bundle);
	}

	/** 쿼터 초과 시에만. DB·앱 내 저장 지문 + 턴 요약 */
	private ReactionNarrationBundle buildFallbackReactionBundle(
			StatChangeResult statResult,
			String userText,
			String sceneTitle,
			String sceneDescription,
			List<RosterItem> alive) {
		String statName = statResult != null ? statResult.statName() : null;
		IdolDialogueBlock block = buildFallbackDialogueBlockForChatTurn(
				statName, userText, sceneTitle, sceneDescription, alive);
		return new ReactionNarrationBundle(block, minimalTurnNarrationForStat(statResult, userText));
	}

	/** 키 없음·파싱 실패 등 — 저장 지문 풀 없이 짧게 이어감 */
	private ReactionNarrationBundle buildMinimalPlaceholderReactionBundle(
			StatChangeResult statResult,
			String userText,
			String sceneTitle,
			String sceneDescription,
			List<RosterItem> alive) {
		String statName = statResult != null ? statResult.statName() : null;
		String stat = pickStatForFallback(statName, userText, sceneTitle, sceneDescription);
		String situation = mergeSceneAndProducerReaction(
				stripOuterParensForPlainLine(pickRandomMinimalSituation()),
				buildProducerParagraphForFallback(userText));
		List<IdolChatLine> lines = new ArrayList<>();
		for (int i = 0; i < alive.size(); i++) {
			RosterItem m = alive.get(i);
			IdolPersonality p = resolvePersonality(m);
			String line = fallbackDialogueLine(m, shuffledFallbackVariant(m, i));
			line = withUserReactiveDialogueSuffix(line, userText, stat, i);
			line = maybeExtendFallbackDialogueLine(line);
			lines.add(new IdolChatLine(m.traineeId(), m.name(), p.getShortLabel(), line));
		}
		return new ReactionNarrationBundle(new IdolDialogueBlock(situation, lines),
				minimalTurnNarrationForStat(statResult, userText));
	}

	private static String minimalTurnNarrationForStat(StatChangeResult r, String userText) {
		String ut = userText == null ? "" : userText.trim();
		String snip = ut.isEmpty() ? "" : sanitizeUserSnippetForNarration(ut, 72);
		String echo = "";
		if (!ut.isEmpty()) {
			echo = switch (ThreadLocalRandom.current().nextInt(5)) {
				case 0 -> " 방금 『" + snip + "』에 맞춰 반응이 이어진다.";
				case 1 -> " 『" + snip + "』에 대한 기류가 팀에 남는다.";
				case 2 -> " 지시(『" + snip + "』)가 팀의 호흡에 섞인다.";
				case 3 -> " 『" + snip + "』를 두고 분위기가 한 번 흔들렸다 가라앉는다.";
				default -> " 프로듀서 말(『" + snip + "』)이 턴 결과에 스며든다.";
			};
		}
		if (r == null) {
			String base = ut.isEmpty() ? pickOneOf("훈련이 반영되었습니다.", "이번 턴이 팀에 스며들었다.", "연습 결과가 수치에 남았다.")
					: pickOneOf("프로듀서의 지시가 팀에 전해졌다.", "말한 대로 팀이 움직였다.", "지시가 연습실 공기에 남았다.");
			return base + echo;
		}
		if (r.traineeName() != null && r.statName() != null && !"-".equals(r.statName())) {
			String deltaStr = (r.delta() >= 0 ? "+" : "") + r.delta();
			String core = pickOneOf(
					r.traineeName() + "의 " + r.statName() + "이(가) " + deltaStr + " 변화했습니다.",
					r.traineeName() + "에게 " + r.statName() + " " + deltaStr + "의 변화가 감지된다.",
					r.statName() + "이(가) " + deltaStr + " — " + r.traineeName() + "에게 반영되었다.");
			return core + echo;
		}
		String tail = ut.isEmpty() ? pickOneOf("훈련이 반영되었습니다.", "턴이 마무리되었다.", "연습이 수치에 남았다.")
				: pickOneOf("훈련이 반영되었다.", "이번 입력이 팀에 스며들었다.", "지시가 결과에 반영되었다.");
		return tail + echo;
	}

	private static String buildReactionCacheKey(
			Long runId,
			Long sceneId,
			String sceneTitle,
			String sceneDescription,
			String userText,
			String resolvedKey,
			String trainingCategory,
			StatChangeResult stat,
			Set<Long> excluded,
			List<RosterItem> alive) {
		StringBuilder sb = new StringBuilder();
		sb.append(runId).append(':').append(sceneId).append(':');
		sb.append(nz(sceneTitle)).append('\u0001').append(nz(sceneDescription)).append('\u0001');
		sb.append(nz(userText).trim()).append('\u0001');
		sb.append(nz(resolvedKey)).append('\u0001').append(nz(trainingCategory)).append('\u0001');
		if (stat != null) {
			sb.append(stat.traineeId()).append(':').append(nz(stat.statName())).append(':').append(stat.delta())
					.append(':').append(stat.fanDelta());
		}
		sb.append('\u0001');
		if (excluded != null && !excluded.isEmpty()) {
			excluded.stream().sorted().forEach(id -> sb.append('x').append(id).append(','));
		}
		sb.append('\u0001');
		for (RosterItem m : alive) {
			if (m == null || m.traineeId() == null) {
				continue;
			}
			sb.append(m.traineeId()).append(':')
					.append(m.vocal()).append(',').append(m.dance()).append(',').append(m.star()).append(',')
					.append(m.mental()).append(',').append(m.teamwork()).append(':')
					.append(nz(m.statusCode())).append(',');
		}
		sb.append("|v2");
		return sb.toString();
	}

	private static String nz(String s) {
		return s == null ? "" : s;
	}

	private static List<RosterItem> orderedAlive(List<RosterItem> roster, Set<Long> ex) {
		Set<Long> excl = ex == null ? Set.of() : ex;
		return roster.stream()
				.filter(Objects::nonNull)
				.filter(m -> m.traineeId() == null || !excl.contains(m.traineeId()))
				.sorted(java.util.Comparator.comparingInt(RosterItem::pickOrder))
				.collect(Collectors.toList());
	}

	private static String formatRosterForPrompt(List<RosterItem> alive) {
		StringBuilder sb = new StringBuilder();
		for (RosterItem m : alive) {
			IdolPersonality p = resolvePersonality(m);
			String genderLabel = (m != null && m.gender() == Gender.MALE) ? "남" : "여";
			sb.append("- ").append(m.name())
					.append(" (성별: ").append(genderLabel)
					.append(", 성격: ").append(p.getShortLabel()).append(" — ").append(p.getStyleDescription()).append(")\n");
		}
		return sb.toString();
	}

	private static IdolPersonality resolvePersonality(RosterItem m) {
		if (m == null) {
			return IdolPersonality.BUBBLY;
		}
		IdolPersonality byCode = IdolPersonality.fromCodeOrNull(m.personalityCode());
		if (byCode != null) {
			return byCode;
		}
		return IdolPersonality.forPickOrder(m.pickOrder());
	}

	private String parseChoiceKeyJson(String raw, List<SceneResult.ChoiceItem> choices) {
		String json = stripJsonFence(raw);
		if (json.isBlank()) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			String k = root.path("key").asString("").trim().toUpperCase(Locale.ROOT);
			if ("NONE".equals(k)) {
				return "NONE";
			}
			for (SceneResult.ChoiceItem c : choices) {
				if (c != null && c.getKey() != null && c.getKey().equalsIgnoreCase(k)) {
					return c.getKey().trim().toUpperCase(Locale.ROOT);
				}
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private Optional<IdolDialogueBlock> parseSituationLinesBlock(String raw, List<RosterItem> alive) {
		if (alive == null || alive.isEmpty()) {
			return Optional.empty();
		}
		String json = normalizeModelJson(raw);
		if (json.isBlank()) {
			return Optional.empty();
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			String legacySit = firstNonBlank(
					root.path("situation").asString(""),
					root.path("scene").asString(""),
					root.path("situationText").asString(""),
					root.path("narration").asString("")).trim();
			String scenePart = firstNonBlank(
					root.path("sceneSituation").asString(""),
					root.path("sceneAmbience").asString(""),
					root.path("ambienceSituation").asString("")).trim();
			String prodPart = firstNonBlank(
					root.path("producerResponse").asString(""),
					root.path("producerReactionSituation").asString(""),
					root.path("producerEcho").asString("")).trim();

			String situation;
			if (!scenePart.isEmpty() || !prodPart.isEmpty()) {
				String amb = scenePart.isEmpty() ? legacySit : scenePart;
				situation = mergeSceneAndProducerReaction(amb, prodPart);
				if (situation.isBlank()) {
					situation = legacySit;
				}
			} else {
				situation = legacySit;
			}

			JsonNode arr = pickMemberLinesArray(root);
			if (situation.isBlank() && (arr == null || !arr.isArray() || arr.isEmpty())) {
				return Optional.empty();
			}
			if (situation.isBlank()) {
				situation = stripOuterParensForPlainLine(pickRandomMinimalSituation());
			}
			if (arr == null || !arr.isArray() || arr.isEmpty()) {
				List<IdolChatLine> placeholder = new ArrayList<>();
				for (int i = 0; i < alive.size(); i++) {
					RosterItem m = alive.get(i);
					IdolPersonality p = resolvePersonality(m);
					placeholder.add(new IdolChatLine(m.traineeId(), m.name(), p.getShortLabel(),
							maybeExtendFallbackDialogueLine(fallbackDialogueLine(m, shuffledFallbackVariant(m, i)))));
				}
				return Optional.of(new IdolDialogueBlock(ensureNonBlankSituation(situation), placeholder));
			}
			List<IdolChatLine> lines = new ArrayList<>();
			for (int i = 0; i < alive.size(); i++) {
				RosterItem m = alive.get(i);
				JsonNode cell = i < arr.size() ? arr.get(i) : null;
				String text = extractMemberLineText(cell);
				IdolPersonality p = resolvePersonality(m);
				if (isEllipsisOrBlankLine(text)) {
					text = maybeExtendFallbackDialogueLine(fallbackDialogueLine(m, shuffledFallbackVariant(m, i)));
				} else {
					text = normalizeMemberLineToActionDialogue(text, alive, m, i);
				}
				lines.add(new IdolChatLine(m.traineeId(), m.name(), p.getShortLabel(), text));
			}
			return Optional.of(new IdolDialogueBlock(ensureNonBlankSituation(situation), lines));
		} catch (Exception e) {
			log.debug("[GameAiNarration] parseSituationLinesBlock: {}", e.toString());
			return Optional.empty();
		}
	}

	private static String firstNonBlank(String... parts) {
		if (parts == null) {
			return "";
		}
		for (String p : parts) {
			if (p != null && !p.isBlank()) {
				return p;
			}
		}
		return "";
	}

	private static String extractMemberLineText(JsonNode elem) {
		if (elem == null || elem.isNull()) {
			return "";
		}
		if (elem.getNodeType() == JsonNodeType.STRING) {
			return elem.asString("").trim();
		}
		if (elem.isNumber()) {
			return elem.asString("").trim();
		}
		if (elem.isObject()) {
			String[] keys = { "text", "dialogue", "line", "content", "reply", "message", "value", "대사"};
			for (String k : keys) {
				String v = elem.path(k).asString("").trim();
				if (!v.isBlank()) {
					return v;
				}
			}
			for (JsonNode child : elem) {
				String v = extractMemberLineText(child);
				if (!v.isBlank()) {
					return v;
				}
			}
		}
		if (elem.isArray() && elem.size() > 0) {
			return extractMemberLineText(elem.get(0));
		}
		return "";
	}

	/** Gemini가 snake_case·별칭·객체 배열로 줄 때 대응 */
	private static JsonNode pickMemberLinesArray(JsonNode root) {
		if (root == null) {
			return null;
		}
		String[] keys = { "memberLines", "member_lines", "lines", "dialogues", "memberDialogues", "replies", "members" };
		for (String k : keys) {
			JsonNode n = root.path(k);
			if (n.isArray() && !n.isEmpty()) {
				return n;
			}
		}
		return null;
	}

	private String extractResultNarration(String raw) {
		String json = normalizeModelJson(raw);
		if (json.isBlank()) {
			return "";
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			String n = firstNonBlank(
					root.path("resultNarration").asString(""),
					root.path("result_narration").asString(""),
					root.path("summary").asString("")).trim();
			return n;
		} catch (Exception e) {
			return "";
		}
	}

	/** 코드펜스 제거 + 앞뒤 잡담 속 `{...}` 추출 */
	private static String normalizeModelJson(String raw) {
		String s = stripJsonFence(raw);
		if (s.isBlank()) {
			return "";
		}
		s = s.trim();
		if (s.startsWith("{")) {
			return s;
		}
		return extractBalancedJsonObject(s);
	}

	private static String stripJsonFence(String raw) {
		if (raw == null) {
			return "";
		}
		String s = raw.trim();
		if (s.startsWith("```")) {
			int nl = s.indexOf('\n');
			if (nl > 0) {
				s = s.substring(nl + 1);
			}
			int end = s.lastIndexOf("```");
			if (end >= 0) {
				s = s.substring(0, end);
			}
		}
		return s.trim();
	}

	/** 모델이 JSON 앞에 문장을 붙인 경우 */
	private static String extractBalancedJsonObject(String s) {
		int start = s.indexOf('{');
		if (start < 0) {
			return "";
		}
		int depth = 0;
		boolean inStr = false;
		boolean esc = false;
		for (int i = start; i < s.length(); i++) {
			char c = s.charAt(i);
			if (inStr) {
				if (esc) {
					esc = false;
				} else if (c == '\\') {
					esc = true;
				} else if (c == '"') {
					inStr = false;
				}
				continue;
			}
			if (c == '"') {
				inStr = true;
				continue;
			}
			if (c == '{') {
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0) {
					return s.substring(start, i + 1);
				}
			}
		}
		return "";
	}
}
