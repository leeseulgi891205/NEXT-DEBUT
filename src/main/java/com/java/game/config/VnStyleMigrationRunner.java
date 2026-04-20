package com.java.game.config;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.entity.GameScene;
import com.java.game.repository.GameSceneRepository;

/**
 * DB 씬 설명을 연출형(상황 지문) 스타일로 정규화. 지문 대사는 Gemini 전용으로 DB 풀은 사용하지 않음.
 */
@Component
public class VnStyleMigrationRunner {

	private final GameSceneRepository gameSceneRepository;

	private boolean migrated;

	public VnStyleMigrationRunner(GameSceneRepository gameSceneRepository) {
		this.gameSceneRepository = gameSceneRepository;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Order(4)
	@Transactional
	public void onReady() {
		if (migrated) {
			return;
		}
		migrated = true;

		int n1 = migrateScenes();
		System.out.println("[VnStyleMigrationRunner] migrated scene descriptions=" + n1);
	}

	private int migrateScenes() {
		List<GameScene> all = gameSceneRepository.findAll();
		int changed = 0;
		for (GameScene s : all) {
			if (s == null) {
				continue;
			}
			String before = s.getDescription();
			String after = toSituationOnly(before, s.getEventType());
			if (after != null && !after.equals(before)) {
				s.setDescription(after);
				changed++;
			}
		}
		return changed;
	}

	private static boolean looksSituationOnly(String text) {
		if (text == null) {
			return false;
		}
		String t = text.trim();
		if (t.isEmpty()) {
			return false;
		}
		if (t.contains("\"")) {
			return false;
		}
		String[] lines = t.split("\\R+");
		int ok = 0;
		for (String line : lines) {
			String x = line == null ? "" : line.trim();
			if (x.isEmpty()) {
				continue;
			}
			ok++;
		}
		return ok >= 1;
	}

	private static String toSituationOnly(String original, String eventType) {
		String src = original == null ? "" : original.trim();
		if (src.isEmpty()) {
			return original;
		}
		if (looksSituationOnly(src)) {
			return original;
		}

		String[] beats = situationBeatsByEvent(eventType);

		java.util.List<String> out = new java.util.ArrayList<>();
		for (String part : src.replace("\"", "").split("(?<=[.!?。…])\\s+")) {
			if (out.size() >= 3) {
				break;
			}
			String p = sanitizeSituationPart(part);
			if (p.isBlank()) {
				continue;
			}
			out.add(ensurePeriod(p));
		}
		if (out.size() < 2) {
			for (String b : beats) {
				if (out.size() >= 3) {
					break;
				}
				out.add(ensurePeriod(b));
			}
		}
		if (out.isEmpty()) {
			out.add(ensurePeriod(beats[0]));
		}
		return String.join("\n", out);
	}

	private static String sanitizeSituationPart(String part) {
		String p = part == null ? "" : part.trim();
		if (p.isEmpty()) {
			return "";
		}
		p = p.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();

		String[] ban = { "긴장", "무겁", "가라앉", "분위기", "감정", "느낌", "생각", "불안", "기대", "압박" };
		for (String b : ban) {
			p = p.replace(b, "");
		}
		p = p.replace("한다.", "한다").replace("된다.", "된다").replace("보인다.", "보인다");
		p = p.replaceAll("[.。]+$", "");
		p = p.replaceAll("\\s+", " ").trim();

		if (p.endsWith("흐른다")) {
			p = "말이 나오기 전 숨이 길어진다";
		}
		if (p.endsWith("느껴진다")) {
			p = "손끝이 가볍게 떨린다";
		}
		if (p.length() > 45) {
			p = p.substring(0, 45).trim();
		}
		return p;
	}

	private static String ensurePeriod(String s) {
		String t = s == null ? "" : s.trim();
		if (t.isEmpty()) {
			return t;
		}
		if (t.endsWith(".")) {
			return t;
		}
		if (t.endsWith("…")) {
			return t + ".";
		}
		if (t.endsWith("。") || t.endsWith("!") || t.endsWith("?")) {
			return t;
		}
		return t + ".";
	}

	private static String[] situationBeatsByEvent(String eventType) {
		String et = eventType == null ? "" : eventType.toUpperCase(Locale.ROOT);
		if (et.contains("CONDITION")) {
			return new String[] {
					"누군가 물병 뚜껑을 조용히 닫는다",
					"어깨가 한 번 들렸다가 천천히 내려간다",
					"발끝이 자리를 찾듯 바닥을 두 번 짚는다"
			};
		}
		if (et.contains("MEDIA")) {
			return new String[] {
					"조명이 켜지자 시선이 렌즈를 피했다가 다시 붙는다",
					"옷깃을 정돈하는 손이 잠깐 멈춘다",
					"셔터음이 한 번 섞이고 숨이 동시에 고른다"
			};
		}
		if (et.contains("CHOREOGRAPHY")) {
			return new String[] {
					"바닥 테이프 자국 위로 발이 다시 정렬된다",
					"손끝이 포인트에서 한 박 늦게 멈춘다",
					"누군가 고개를 끄덕이며 카운트를 다시 세기 시작한다"
			};
		}
		if (et.contains("TRAINER")) {
			return new String[] {
					"메모지 한 장이 조용히 넘어간다",
					"펜 끝이 같은 구간을 두 번 짚는다",
					"거울 쪽을 보던 눈이 바닥으로 떨어졌다가 다시 올라간다"
			};
		}
		return new String[] {
				"문이 닫히는 소리 뒤로 숨소리만 남는다",
				"시선이 잠깐 엇갈렸다가 같은 곳으로 모인다",
				"누군가 발끝으로 박자를 한 번 더 찍는다"
		};
	}
}
