package com.java.game.config;

import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.entity.GameChoice;
import com.java.game.entity.GameScene;
import com.java.game.repository.GameChoiceRepository;
import com.java.game.repository.GameRunRepository;
import com.java.game.repository.GameSceneRepository;

/**
 * 서버 시작 시 게임 씬 + 선택지를 DB에 자동 삽입.
	 * - 기존 {@code game_scene} 행은 삭제 후 재삽입하여 지문 변경이 항상 반영된다.
	 * - 재시드로 씬 ID가 바뀌므로 {@link com.java.game.entity.GameRun#currentSceneId}는 null로 초기화된다.
	 * - 모든 지문은 랜덤 출력(버킷별 풀)
	 * - 첫날(튜토리얼): 아침/저녁 × 그룹타입(혼성/남/여) 별도 풀(각 3개)
	 * - 1~3개월: 월차 × 전반/후반(2주) × 아침/저녁 별도 풀(각 12개 기본)
	 * - 최종 데뷔 평가: 등급별 풀
 */
@Component
public class SceneSeedRunner {

	private final GameSceneRepository sceneRepo;
	private final GameChoiceRepository choiceRepo;
	private final GameRunRepository gameRunRepository;

	private boolean seeded = false;

	public SceneSeedRunner(GameSceneRepository sceneRepo, GameChoiceRepository choiceRepo,
			GameRunRepository gameRunRepository) {
		this.sceneRepo = sceneRepo;
		this.choiceRepo = choiceRepo;
		this.gameRunRepository = gameRunRepository;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Order(1)
	@Transactional
	public void onApplicationReady() {
		if (seeded) return;
		seeded = true;
		seedScenes();
		seedChoices();
		System.out.println("[SceneSeedRunner] 씬 " + sceneRepo.count() + "개 / 선택지 " + choiceRepo.count() + "개 준비 완료");
	}

	private void seedScenes() {
		sceneRepo.deleteAll();
		gameRunRepository.clearCurrentSceneIds();
		// ══ 첫날(튜토리얼) — 아침/저녁 × 그룹타입(혼성/남/여) 각 3개 풀 ══
		String[] times = {"MORNING", "EVENING"};
		String[] groups = {"MIXED", "MALE", "FEMALE"};
		for (String time : times) {
			for (String g : groups) {
				String bucket = "TUTORIAL_" + time + "_" + g;
				String timeKo = "MORNING".equals(time) ? "아침" : "저녁";
				String gKo = switch (g) {
				case "MALE" -> "남성";
				case "FEMALE" -> "여성";
				default -> "혼성";
				};
				savePool(bucket, "📘 TUTORIAL", "첫날 " + timeKo + " — 연습실 첫 걸음 (" + gKo + ")",
						"문이 열리자 차가운 공기가 얼굴을 스친다. 거울 너머로 실루엣만 겹쳐 보인다. "
								+ "누군가의 발끝이 먼저 움직이기 시작한다.", 3);
				savePool(bucket, "📘 TUTORIAL", "첫날 " + timeKo + " — 첫 루틴 (" + gKo + ")",
						"타이머가 돌아갈수록 땀이 바닥에 떨어졌다 증발한다. 숨이 겹치기 시작하고 리듬이 천천히 하나로 맞는다.", 3);
				savePool(bucket, "📘 TUTORIAL", "첫날 " + timeKo + " — 멤버와의 첫 호흡 (" + gKo + ")",
						"연습이 잠시 멈추고 긴장이 조금씩 풀린다. 멤버들의 표정이 한결 부드러워지며 분위기가 가벼워진다.", 3);
			}
		}

		// ══ 1~3개월 — 월차 × 전반/후반(2주) × 아침/저녁 풀 (각 버킷 12개 이상) ══
		for (int month = 1; month <= 3; month++) {
			for (String half : new String[]{"H1", "H2"}) {
				for (String time : times) {
					String bucket = "M" + month + "_" + half + "_" + time;
					String timeKo = "MORNING".equals(time) ? "아침" : "저녁";
					String halfKo = "H1".equals(half) ? "전반" : "후반";

					seedMonthlyBucket(bucket, month, half, timeKo, halfKo);
				}
			}
		}

		// ══ 중간 평가 ══
		seedMidEval();

		// ══ 최종 데뷔 평가 ══
		seedDebutEval();
	}

	private void seedMidEval() {
		savePool("MID_EVAL_S", "🧾 MID REVIEW", "중간 평가 — 상위권 진입",
				"평가가 끝난 복도는 조용하다. 누군가의 발걸음만 리듬처럼 길게 이어진다. 등줄기에 남은 열기가 천천히 식는다.", 3);
		savePool("MID_EVAL_S", "🧾 MID REVIEW", "중간 평가 — 성장 곡선",
				"점수표를 접는 소리가 난다. 누군가가 주먹을 쥐었다 편다를 반복한다. 연습실 문 손잡이가 아직 따뜻하다.", 3);
		savePool("MID_EVAL_S", "🧾 MID REVIEW", "중간 평가 — 기대주",
				"스크린에 올라온 글자가 시선을 붙잡는다. 숨을 고르는 어깨가 여럿이다. 아무도 자리에서 일어나지 않는다.", 3);

		savePool("MID_EVAL_A", "🧾 MID REVIEW", "중간 평가 — 좋은 페이스",
				"웃음이 크지 않아도 공기가 가볍다. 그러다 한 줄 적힌 코멘트에서 시선이 잠깐 멈춘다. 누군가가 물병 뚜껑을 조인다.", 3);
		savePool("MID_EVAL_A", "🧾 MID REVIEW", "중간 평가 — 경쟁 시작",
				"복도에 발소리가 잦아진다. 시선이 스쳐 지나갈 때마다 어깨가 아주 짧게 굳었다 풀린다. 창밖 하늘이 회색빛으로 내려앉는다.", 3);
		savePool("MID_EVAL_A", "🧾 MID REVIEW", "중간 평가 — 한 단계 위로",
				"순위표 맨 위쪽이 눈에 들어온다. 누군가가 고개를 끄덕이다 멈춘다. 손끝이 테이블 가장자리를 짚는다.", 3);

		savePool("MID_EVAL_B", "🧾 MID REVIEW", "중간 평가 — 무난한 결과",
				"칭찬도 질책도 짧게 끝난다. 메모장 페이지가 넘어가는 속도만 유난히 빠르다. 누군가가 한숨을 삼킨다.", 3);
		savePool("MID_EVAL_B", "🧾 MID REVIEW", "중간 평가 — 불안한 균형",
				"점수 칸막이가 비슷한 높이로 늘어선다. 누군가가 펜 끝으로 같은 줄을 여러 번 긋는다. 형광등이 지글거린다.", 3);
		savePool("MID_EVAL_B", "🧾 MID REVIEW", "중간 평가 — 아직은 가능성",
				"끝난 줄 알았던 평가지가 다시 펼쳐진다. 누군가의 무릎이 의자를 두드린다. 창문 너머 빛이 기울어진다.", 3);

		savePool("MID_EVAL_C", "🧾 MID REVIEW", "중간 평가 — 경고등",
				"붉은 표시가 한 군데 박혀 있다. 누군가가 시선을 바닥에 고정한다. 공기가 순간 얇아진다.", 3);
		savePool("MID_EVAL_C", "🧾 MID REVIEW", "중간 평가 — 재정비 필요",
				"종이 가장자리가 구겨진다. 물티슈 한 장이 조용히 건네진다. 아무도 전화 화면을 켜지 않는다.", 3);
		savePool("MID_EVAL_C", "🧾 MID REVIEW", "중간 평가 — 멘탈 흔들림",
				"손등에 맺힌 땀이 한 방울 떨어진다. 누군가가 목덜미를 문지른다. 침묵이 길어질수록 숨소리만 커진다.", 3);

		savePool("MID_EVAL_D", "🧾 MID REVIEW", "중간 평가 — 탈락권",
				"차가운 숫자가 시야에 박힌다. 누군가의 입술이 하얗게 질린다. 의자 다리가 삐걱인다.", 3);
		savePool("MID_EVAL_D", "🧾 MID REVIEW", "중간 평가 — 벼랑 끝",
				"문이 닫히는 소리가 유난히 크게 들린다. 누군가가 벽에 등을 기댄다. 시계 초침만 끊임없이 돈다.", 3);
		savePool("MID_EVAL_D", "🧾 MID REVIEW", "중간 평가 — 역전 과제",
				"새로 인쇄된 과제지가 테이블 위에 올라온다. 누군가의 손끝이 제목 줄을 따라 움직인다. 창문 밖 바람 소리가 난다.", 3);
	}

	private void seedDebutEval() {
		savePool("DEBUT_EVAL_S", "🏁 FINAL EVALUATION", "최종 평가 — 압도적 무대",
				"조명이 켜지는 순간 호흡이 동시에 고른다. 관객석에서 웅성거림이 한 번에 잦아든다. 무대 끝까지 열기가 끊기지 않는다.", 3);
		savePool("DEBUT_EVAL_S", "🏁 FINAL EVALUATION", "최종 평가 — 완벽한 합",
				"엇박 없이 박자가 맞아떨어진다. 누군가의 눈물이 라이트에 반짝인다. 앵콜 함성이 발끝까지 울려 퍼진다.", 3);
		savePool("DEBUT_EVAL_S", "🏁 FINAL EVALUATION", "최종 평가 — 데뷔 확정 분위기",
				"마지막 포즈에서 어깨가 동시에 내려앉는다. 심사석 쪽에서 짧은 박수가 터진다. 무대 먼지가 천천히 가라앉는다.", 3);

		savePool("DEBUT_EVAL_A", "🏁 FINAL EVALUATION", "최종 평가 — 강한 인상",
				"첫 소절에서 목소리가 살짝 떨렸다 곧바로 잡힌다. 관객의 시선이 한 줄로 모였다 흩어진다. 막바지에서 박수가 밀려온다.", 3);
		savePool("DEBUT_EVAL_A", "🏁 FINAL EVALUATION", "최종 평가 — 승부수 성공",
				"무대 중앙에서 한 박이 길어진다. 누군가의 손끝이 떨렸다 멈춘다. 곡이 끝날 때 환호가 뒤늦게 터진다.", 3);
		savePool("DEBUT_EVAL_A", "🏁 FINAL EVALUATION", "최종 평가 — 끝까지 밀어붙이다",
				"뺨에 땀이 굵게 흐른다. 무릎이 살짝 후들거렸다 다시 세워진다. 커튼이 내리자 숨 쉬는 소리만 크게 들린다.", 3);

		savePool("DEBUT_EVAL_B", "🏁 FINAL EVALUATION", "최종 평가 — 아쉬움과 가능성",
				"중간에 동선이 반 박 어긋난다. 누군가가 웃음으로 틈을 메운다. 끝나고도 관객석이 완전히 가라앉지 않는다.", 3);
		savePool("DEBUT_EVAL_B", "🏁 FINAL EVALUATION", "최종 평가 — 흔들리다 다잡다",
				"후렴에서 호흡이 한 번 엇갈렸다 맞춰진다. 손을 잡는 듯한 제스처가 스쳐 지나간다. 박수가 박자보다 늦게 이어진다.", 3);
		savePool("DEBUT_EVAL_B", "🏁 FINAL EVALUATION", "최종 평가 — 마지막에 빛나다",
				"초반은 조용했던 객석이 후반에 들썩인다. 누군가의 하이톤이 떴다 안정된다. 엔딩 라이트가 천천히 내려앉는다.", 3);

		savePool("DEBUT_EVAL_C", "🏁 FINAL EVALUATION", "최종 평가 — 컨디션 변수",
				"목소리 끝이 갈라진다. 누군가가 물을 삼키는 목이 보인다. 그래도 마지막 동작까지 버티어 낸다.", 3);
		savePool("DEBUT_EVAL_C", "🏁 FINAL EVALUATION", "최종 평가 — 팀워크가 갈린다",
				"동선이 겹칠 뻔하다 스쳐 지나간다. 누군가의 표정이 순간 굳었다 풀린다. 곡이 끝나자 어깨가 동시에 처진다.", 3);
		savePool("DEBUT_EVAL_C", "🏁 FINAL EVALUATION", "최종 평가 — 재도전의 무대",
				"실수가 한 번 더 보인다. 누군가가 고개를 숙였다 들었다. 커튼 뒤 발걸음이 무겁게 이어진다.", 3);

		savePool("DEBUT_EVAL_D", "🏁 FINAL EVALUATION", "최종 평가 — 흔들리는 무대",
				"마이크 스탠드가 살짝 흔들린다. 박자가 두 번 틀어진다. 객석의 기류가 차갑게 식는다.", 3);
		savePool("DEBUT_EVAL_D", "🏁 FINAL EVALUATION", "최종 평가 — 벼랑 끝",
				"라이트가 눈을 찌른다. 실수가 연달아 겹친다. 끝난 뒤에도 무대 위 공기가 무겁게 남는다.", 3);
		savePool("DEBUT_EVAL_D", "🏁 FINAL EVALUATION", "최종 평가 — 역전은 가능한가",
				"마지막 한 소절만 남았다. 누군가의 손이 마이크를 꽉 쥔다. 조명이 꺼지기 직전 숨이 멎는 듯한 정적이 드리운다.", 3);
	}

	/**
	 * 버킷당 12개 기본 풀. 지문은 2~3문장, 분위기·신체 감각 중심(설명/메타 문장 없음).
	 */
	private void seedMonthlyBucket(String bucket, int month, String half, String timeKo, String halfKo) {
		String prefix = month + "개월차 " + halfKo + " — ";

		add(bucket, "⚡ TRAINING EVENT", prefix + "기본 훈련 (1) · " + timeKo, monthlyTrain1(month), 12);
		add(bucket, "⚡ TRAINING EVENT", prefix + "기본 훈련 (2) · " + timeKo, monthlyTrain2(month), 12);

		add(bucket, "😓 CONDITION ALERT", prefix + "컨디션 이슈 (1) · " + timeKo, monthlyCondition1(month), 12);
		add(bucket, "😓 CONDITION ALERT", prefix + "컨디션 이슈 (2) · " + timeKo, monthlyCondition2(month), 12);

		add(bucket, "🧑‍🏫 TRAINER FEEDBACK", prefix + "트레이너 피드백 (1) · " + timeKo, monthlyTrainer1(month), 12);
		add(bucket, "🧑‍🏫 TRAINER FEEDBACK", prefix + "트레이너 피드백 (2) · " + timeKo, monthlyTrainer2(month, half), 12);

		add(bucket, "🔀 SUDDEN CHANGE", prefix + "변수 발생 (1) · " + timeKo, monthlySudden1(month), 12);
		add(bucket, "🔀 SUDDEN CHANGE", prefix + "변수 발생 (2) · " + timeKo, monthlySudden2(month, half), 12);

		add(bucket, "📷 MEDIA EVENT", prefix + "미디어/노출 (1) · " + timeKo, monthlyMedia1(month), 12);
		add(bucket, "📷 MEDIA EVENT", prefix + "미디어/노출 (2) · " + timeKo, monthlyMedia2(month), 12);

		add(bucket, "⚠️ CHOREOGRAPHY FIX", prefix + "수정 지시 (1) · " + timeKo, monthlyChoreo1(month), 12);
		add(bucket, "⚠️ CHOREOGRAPHY FIX", prefix + "수정 지시 (2) · " + timeKo, monthlyChoreo2(month, half), 12);
	}

	private String monthlyTrain1(int month) {
		return switch (month) {
		case 1 -> "거울 앞에 선 호흡이 겹친다. 어깨가 아주 짧게 떨렸다 가라앉는다. 바닥이 미세하게 미끄럽게 느껴진다.";
		case 2 -> "같은 파트를 두 번째 돌자 눈빛이 곧게 세워진다. 땀이 목덜미를 타고 흐른다. 연습실 공기가 한결 건조해진다.";
		default -> "카운트 소리만 남고 말은 줄었다. 무릎이 굽혀졌다 펴진다를 반복한다. 타이머 불빛이 시야 끝에서 깜빡인다.";
		};
	}

	private String monthlyTrain2(int month) {
		return switch (month) {
		case 1 -> "워밍업 후 숨이 아직 고르게 잡히지 않는다. 누군가는 벽에 기댄 채 물병을 돌린다. 누군가는 이미 포인트 자리에 선다.";
		case 2 -> "엇박이 한 번 섞이자 모두가 동시에 멈춘다. 잠시 침묵 뒤 첫 박이 다시 울린다. 누군가의 손끝이 먼저 박자를 짚는다.";
		default -> "리듬이 빨라질수록 표정은 굳고 눈만 살아 있다. 손끝이 제스처 끝에서 미세하게 떨린다. 쉰 숨이 한 줄 섞인다.";
		};
	}

	private String monthlyCondition1(int month) {
		return switch (month) {
		case 1 -> "눈동자가 반사등에 자꾸 튄다. 동작이 반 박 늦게 따라붙는다. 누군가가 바닥을 길게 숨을 내쉰다.";
		case 2 -> "작은 실수에도 몸이 움찔한다. 누군가가 목을 돌리며 안경을 고쳐 쓴다. 연습실 공기가 순간 얇아진다.";
		default -> "무릎을 짚은 채 고개를 숙인 사람이 있다. 물티슈 한 장이 조용히 건네진다. 아무도 말을 꺼내지 않는다.";
		};
	}

	private String monthlyCondition2(int month) {
		return switch (month) {
		case 1 -> "같은 구간이 세 번째 틀어진다. 누군가가 미간을 누른다. 거울 속 시선이 제각각 엇갈린다.";
		case 2 -> "평소엔 넘어갔을 동작이 오늘은 거칠게 보인다. 발끝이 의자 다리에 부딪친다. 잠깐의 정적이 흐른다.";
		default -> "타이머를 끄자 숨소리만 남는다. 누군가가 벽에 등을 기댄다. 창문 너머 빛이 기울어진다.";
		};
	}

	private String monthlyTrainer1(int month) {
		return switch (month) {
		case 1 -> "영상이 멈춘다. 화면 속 한 프레임이 반복된다. 포인트에서 손끝이 어긋난 것이 보인다.";
		case 2 -> "모니터 밝기가 낮아진다. 손가락이 타임라인 위를 짚는다. 누군가의 목젖이 움직이다 삼켜진다.";
		default -> "정지 화면 속 표정들이 차갑게 박혀 있다. 누군가가 입술을 깨문다. 연필이 종이에 굵게 선을 긋는 소리가 난다.";
		};
	}

	private String monthlyTrainer2(int month, String half) {
		if (month <= 1) {
			return "메모지가 한 장 넘어온다. 체크 표시가 몇 줄 적혀 있다. 누군가가 고개를 끄덕이다 멈춘다.";
		}
		if (month == 2) {
			return "피드백 동안 누구도 전화를 보지 않는다. 발끝이 바닥 무늬를 따라 천천히 움직인다. 창문이 안개를 머금은 것 같다.";
		}
		return "H1".equals(half)
				? "종이 가장자리가 구겨진다. 누군가가 손톱으로 테이블을 긋는다. 시계 초침 소리만 유난히 크게 들린다."
				: "마지막 줄에 밑줄이 세 번 겹쳐져 있다. 누군가의 손이 컵을 짚는다. 연습실 문이 살짝 열렸다 닫힌다.";
	}

	private String monthlySudden1(int month) {
		return switch (month) {
		case 1 -> "메시지 알림이 연달아 울린다. 케이블이 바닥에서 끌린다. 연습실 문이 한 번 더 열렸다 닫힌다.";
		case 2 -> "일정표가 새로 출력되어 테이블 위에 올라간다. 가방이 복도로 밀려난다. 공기가 순간 바뀐다.";
		default -> "열쇠 소리가 유난히 가깝게 들린다. 램프가 한 번 깜빡이고 안정된다. 누군가가 시계를 두 번 본다.";
		};
	}

	private String monthlySudden2(int month, String half) {
		if (month <= 1) {
			return "익숙한 거울 각도가 바뀌었다. 발바닥 감각이 낯설다. 누군가가 창틀을 짚고 선다.";
		}
		if (month == 2) {
			return "새 연습실 냄새가 코끝에 닿는다. 말수가 줄고 움직임만 커진다. 문 밖 발소리가 잦아진다.";
		}
		return "H1".equals(half)
				? "박자를 세던 손이 한 박 늦는다. 물을 한 모금만 마시고 뚜껑이 닫힌다. 형광등이 지글거린다."
				: "의자가 한 줄 밀려 바닥이 드러난다. 먼지가 햇빛에 떠다닌다. 누군가의 발이 새 자리를 찾는다.";
	}

	private String monthlyMedia1(int month) {
		return switch (month) {
		case 1 -> "휴대폰 화면에 짧은 일정 알림이 떠 있다. 조명 테스트 불빛이 번쩍인다. 누군가가 옷깃을 정돈한다.";
		case 2 -> "카메라 렌즈 뚜껑이 열린다. 반사판이 각도를 찾는다. 누군가의 뺨이 살짝 붉어진다.";
		default -> "셔터음이 드물게 섞인다. 시선이 렌즈를 피했다가 다시 붙는다. 공기가 얇아진 듯 숨이 짧아진다.";
		};
	}

	private String monthlyMedia2(int month) {
		return switch (month) {
		case 1 -> "짧은 리허설 컷이 반복된다. 피사체 표시등이 붉게 깜빡인다. 누군가가 손등으로 이마를 쓸어내린다.";
		case 2 -> "모니터 속 자신들이 낯설다. 누군가가 웃음을 참는다 티가 난다. 바닥 마커 테이프가 반쯤 떨어져 있다.";
		default -> "조명 열기가 볼에 닿는다. 스태프의 손짓이 빨라진다. 연습복 소매가 젖어 겉돈다.";
		};
	}

	private String monthlyChoreo1(int month) {
		return switch (month) {
		case 1 -> "안무지가 새로 펼쳐진다. 형광펜 표시가 여러 겹이다. 발끝이 이전 동선에서 멈춘다.";
		case 2 -> "동선이 바뀌자 맨 앞 자리가 비었다 메워진다. 거울 속 정렬이 한 박 어긋난다. 누군가가 허리를 굽혔다 편다.";
		default -> "테이프 자국이 바닥에 겹친다. 무릎 보호대 끈을 다시 묶는 손이 보인다. 숨이 끊기지 않게 삼키는 소리가 난다.";
		};
	}

	private String monthlyChoreo2(int month, String half) {
		if (month <= 1) {
			return "수정 구간만 세 번 연속 돌린다. 종아리가 떨린다. 누군가가 벽에 손바닥을 짚는다.";
		}
		if (month == 2) {
			return "시간 부족 알림이 한 번 울린다. 표정이 굳기 전에 다시 웃음을 짓는다. 손등에 땀이 맺혀 흘러내린다.";
		}
		return "H1".equals(half)
				? "마지막 블록만 남았는데 아무도 자리를 비우지 않는다. 시계를 본 눈이 넷이다. 문 손잡이에 손이 얹혀 있다 멈춘다."
				: "커튼 너머로 발소리가 지나간다. 누군가가 물을 한 모금 삼킨다. 마지막 동선만 바닥에 분필로 다시 그려진다.";
	}

	private void add(String phase, String eventType, String title, String desc, int maxPerPhase) {
		// 동일 버킷에서 max까지 찰 때까지, 호출 순서대로 차곡차곡 채움
		savePool(phase, eventType, title, desc, maxPerPhase);
	}

	private void seedChoices() {
		// 모든 버킷에서 공통 선택지(스탯 선택 구조 고정)
		java.util.List<String> buckets = new java.util.ArrayList<>();

		// tutorial buckets
		for (String time : new String[]{"MORNING", "EVENING"}) {
			for (String g : new String[]{"MIXED", "MALE", "FEMALE"}) {
				buckets.add("TUTORIAL_" + time + "_" + g);
			}
		}
		// month buckets
		for (int month = 1; month <= 3; month++) {
			for (String half : new String[]{"H1", "H2"}) {
				buckets.add("M" + month + "_" + half + "_MORNING");
				buckets.add("M" + month + "_" + half + "_EVENING");
			}
		}
		// mid (등급 버킷)
		buckets.add("MID_EVAL_S");
		buckets.add("MID_EVAL_A");
		buckets.add("MID_EVAL_B");
		buckets.add("MID_EVAL_C");
		buckets.add("MID_EVAL_D");

		// final (등급 버킷)
		buckets.add("DEBUT_EVAL_S");
		buckets.add("DEBUT_EVAL_A");
		buckets.add("DEBUT_EVAL_B");
		buckets.add("DEBUT_EVAL_C");
		buckets.add("DEBUT_EVAL_D");

		for (String bucket : buckets) {
			ChoiceSet cs = buildChoiceSet(bucket);
			saveChoice(bucket, "A", cs.aText, "VOCAL", 1);
			saveChoice(bucket, "B", cs.bText, "DANCE", 2);
			saveChoice(bucket, "C", cs.cText, "TEAMWORK", 3);
			saveChoice(bucket, "D", cs.dText, "MENTAL", 4);
			saveChoice(bucket, "SPECIAL", cs.sText, "STAR", 5);
		}
	}

	private ChoiceSet buildChoiceSet(String bucket) {
		// 버킷별로 A/B/C/D 문구가 체감상 다르게 보이도록 라벨/톤을 섞는다.
		String flavor = bucketFlavor(bucket);
		String suffix = bucketSuffix(bucket);

		// 기본 5개는 “역할이 명확히 다른 액션”으로 고정하되, 문구는 버킷마다 달라지게 구성
		String a = flavor + " 보컬 발성 교정 — 호흡과 음정을 안정시킨다" + suffix;
		String b = flavor + " 퍼포먼스 집중 — 동선과 표정을 맞춘다" + suffix;
		String c = flavor + " 팀 합 맞추기 — 파트 분배와 호흡을 정리한다" + suffix;
		String d = flavor + " 멘탈 케어 — 긴장과 컨디션을 회복한다" + suffix;
		String s = flavor + " 카메라 테스트 — 존재감을 끌어올린다" + suffix;

		// 힌트(예상 효과)를 문구에 노출
		a += " (안정 +1~2)";
		b += " (안정 +1~2)";
		c += " (안정 +1~2)";
		d += " (안정 +1~2)";
		s += " (도전 0~4 / 실패 -2)";

		// 최종평가는 “결정적 선택” 느낌
		if (bucket.startsWith("DEBUT_EVAL_")) {
			String tier = bucket.substring("DEBUT_EVAL_".length());
			a = "최종 평가(" + tier + ") · 라이브 보컬 — 흔들림을 잡고 완주한다";
			b = "최종 평가(" + tier + ") · 퍼포먼스 — 포인트를 정확히 찍는다";
			c = "최종 평가(" + tier + ") · 팀 합 — 호흡으로 무대를 지배한다";
			d = "최종 평가(" + tier + ") · 멘탈 — 압박을 이겨낸다";
			s = "최종 평가(" + tier + ") · 엔딩 컷 — 카메라를 장악한다";
		}

		return new ChoiceSet(a, b, c, d, s);
	}

	private String bucketFlavor(String bucket) {
		// 월차/전반후반/아침저녁이 선택지 문구에 은근히 묻어나게
		if (bucket.startsWith("TUTORIAL_")) return "첫날";
		if (bucket.startsWith("M1_")) return "초반";
		if (bucket.startsWith("M2_")) return "중반";
		if (bucket.startsWith("M3_")) return "후반";
		return "오늘";
	}

	private String bucketSuffix(String bucket) {
		if (bucket.endsWith("_MORNING")) return " (아침)";
		if (bucket.endsWith("_EVENING")) return " (저녁)";
		return "";
	}

	private static class ChoiceSet {
		final String aText, bText, cText, dText, sText;
		ChoiceSet(String aText, String bText, String cText, String dText, String sText) {
			this.aText = aText;
			this.bText = bText;
			this.cText = cText;
			this.dText = dText;
			this.sText = sText;
		}
	}

	/** DAY2+ 랜덤 풀 씬 — maxPerPhase 개수 미만일 때만 추가 */
	private void savePool(String phase, String eventType, String title, String desc, int maxPerPhase) {
		if (sceneRepo.countByPhase(phase) < maxPerPhase) {
			sceneRepo.save(new GameScene(phase, eventType, title, desc));
		}
	}

	private void saveChoice(String phase, String key, String text, String stat, int order) {
		choiceRepo.findByPhaseAndChoiceKey(phase, key).ifPresentOrElse(existing -> {
			// 이미 있으면 텍스트/타겟/정렬을 업데이트해서 즉시 반영
			existing.setChoiceText(text);
			existing.setStatTarget(stat);
			existing.setSortOrder(order);
		}, () -> choiceRepo.save(new GameChoice(phase, key, text, stat, order)));
	}
}
