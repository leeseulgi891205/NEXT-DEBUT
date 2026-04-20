package com.java.game.config;

import java.util.Map;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.NarrationFallbackDefaults;
import com.java.game.entity.NarrationFallbackSituation;
import com.java.game.repository.NarrationFallbackSituationRepository;

/**
 * 스탯별 상황 폴백 지문을 DB에 최초 1회 시드한다. 이후에는 H2 콘솔·JPA로만 수정(재시작 시 유지).
 */
@Component
public class NarrationFallbackSituationSeedRunner {

	private final NarrationFallbackSituationRepository repo;
	private boolean seeded;

	public NarrationFallbackSituationSeedRunner(NarrationFallbackSituationRepository repo) {
		this.repo = repo;
	}

	@EventListener(ApplicationReadyEvent.class)
	@Order(4)
	@Transactional
	public void onApplicationReady() {
		if (seeded) {
			return;
		}
		seeded = true;
		if (repo.count() > 0) {
			return;
		}
		for (Map.Entry<String, String[]> e : NarrationFallbackDefaults.FALLBACK_SITUATIONS_BY_STAT.entrySet()) {
			String stat = e.getKey();
			String[] arr = e.getValue();
			for (int i = 0; i < arr.length; i++) {
				repo.save(new NarrationFallbackSituation(stat, arr[i], i));
			}
		}
	}
}
