package com.java.game.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.game.service.TraineeLikeSeedService;

/**
 * 기동마다 연습생별 합성 좋아요(MEMBER_MNO&lt;0)를 삭제 후 1~2000 사이 난수로 다시 채움.
 */
@Component
@Order(10)
public class TraineeLikeSeedRunner implements CommandLineRunner {

	private final TraineeLikeSeedService traineeLikeSeedService;

	public TraineeLikeSeedRunner(TraineeLikeSeedService traineeLikeSeedService) {
		this.traineeLikeSeedService = traineeLikeSeedService;
	}

	@Override
	public void run(String... args) {
		try {
			traineeLikeSeedService.seedRandomSyntheticLikes();
		} catch (Exception ignored) {
		}
	}
}
