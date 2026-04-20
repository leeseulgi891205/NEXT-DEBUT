package com.java.game.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.entity.ChatKeywordRule;
import com.java.game.repository.ChatKeywordRuleRepository;

/**
 * 채팅 키워드→선택 키 매핑 룰을 DB에 시딩한다.
 * - 운영 중에는 DB에서 추가/수정 가능
 * - Resolver는 DB룰 우선 + 폴백(하드코딩 키워드) 유지
 */
@Component
public class ChatKeywordRuleSeedRunner {

	private final ChatKeywordRuleRepository repo;
	private boolean seeded;

	public ChatKeywordRuleSeedRunner(ChatKeywordRuleRepository repo) {
		this.repo = repo;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Order(3)
	@Transactional
	public void onApplicationReady() {
		if (seeded) return;
		seeded = true;
		repo.deleteAll();

		// D(멘탈/휴식)
		add("쉬어", "D", 100);
		add("휴식", "D", 95);
		add("회복", "D", 90);
		add("컨디션", "D", 80);
		add("멘탈", "D", 70);
		add("잠", "D", 60);

		// A(보컬)
		add("보컬", "A", 100);
		add("발성", "A", 95);
		add("노래", "A", 90);
		add("음정", "A", 85);
		add("호흡", "A", 80);

		// B(댄스)
		add("댄스", "B", 100);
		add("안무", "B", 95);
		add("춤", "B", 90);
		add("동선", "B", 85);
		add("퍼포", "B", 80);

		// C(팀웍)
		add("팀워크", "C", 100);
		add("팀", "C", 70);
		add("협업", "C", 90);
		add("화합", "C", 85);
		add("조율", "C", 80);

		// SPECIAL(도전/승부수)
		add("승부수", "SPECIAL", 110);
		add("도전", "SPECIAL", 100);
		add("올인", "SPECIAL", 95);
		add("리스크", "SPECIAL", 90);
		add("카메라", "SPECIAL", 85);
	}

	private void add(String keyword, String key, int priority) {
		repo.save(new ChatKeywordRule(keyword, key, priority, true));
	}
}

