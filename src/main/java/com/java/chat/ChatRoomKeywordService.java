package com.java.chat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.ChatRoomKeyword;
import com.java.repository.ChatRoomKeywordRepository;

/**
 * 채팅 키워드: 코드 내장 기본 목록 + DB에 등록된 추가 키워드를 합쳐 검사합니다.
 */
@Service
public class ChatRoomKeywordService {

	private static final String[] BUILT_IN = {
			"씨발", "시발", "ㅅㅂ", "병신", "좆", "개새", "지랄", "니미",
			"fuck", "shit", "bitch", "sex", "spam", "스팸"
	};

	private final ChatRoomKeywordRepository repository;

	private volatile List<String> mergedCache;

	public ChatRoomKeywordService(ChatRoomKeywordRepository repository) {
		this.repository = repository;
	}

	public boolean shouldFlag(String text) {
		if (text == null || text.isBlank()) {
			return false;
		}
		String lower = text.toLowerCase();
		for (String p : mergedKeywords()) {
			if (p == null || p.isBlank()) {
				continue;
			}
			if (lower.contains(p.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private List<String> mergedKeywords() {
		List<String> c = mergedCache;
		if (c != null) {
			return c;
		}
		return rebuildCache();
	}

	@Transactional(readOnly = true)
	public synchronized List<String> rebuildCache() {
		LinkedHashSet<String> set = new LinkedHashSet<>();
		for (String b : BUILT_IN) {
			set.add(b);
		}
		for (ChatRoomKeyword e : repository.findByActiveTrueOrderByKeywordAsc()) {
			if (e.getKeyword() != null && !e.getKeyword().isBlank()) {
				set.add(e.getKeyword().trim());
			}
		}
		List<String> list = List.copyOf(set);
		mergedCache = list;
		return list;
	}

	public void invalidateCache() {
		mergedCache = null;
	}

	public List<String> getBuiltInKeywords() {
		return List.of(BUILT_IN);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> listCustomKeywords() {
		return repository.findByActiveTrueOrderByKeywordAsc().stream().map(e -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("id", e.getId());
			m.put("keyword", e.getKeyword());
			return m;
		}).collect(Collectors.toCollection(ArrayList::new));
	}

	@Transactional
	public String addKeyword(String raw) {
		if (raw == null) {
			return "empty";
		}
		String kw = raw.trim();
		if (kw.isEmpty() || kw.length() > 100) {
			return "invalid";
		}
		if (repository.existsByKeywordIgnoreCaseAndActiveTrue(kw)) {
			return "duplicate";
		}
		for (String b : BUILT_IN) {
			if (b.equalsIgnoreCase(kw)) {
				return "builtin";
			}
		}
		repository.save(new ChatRoomKeyword(kw));
		invalidateCache();
		return "ok";
	}

	@Transactional
	public boolean deleteKeyword(Long id) {
		if (id == null) {
			return false;
		}
		return repository.findById(id).map(e -> {
			repository.delete(e);
			invalidateCache();
			return true;
		}).orElse(false);
	}
}
