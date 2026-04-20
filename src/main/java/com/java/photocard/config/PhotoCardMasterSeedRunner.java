package com.java.photocard.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.photocard.service.PhotoCardService;

/**
 * 연습생별 포토카드 마스터(R/SR/SSR) 자동 생성.
 */
@Component
@Order(50)
public class PhotoCardMasterSeedRunner implements ApplicationRunner {

	private final PhotoCardService photoCardService;

	public PhotoCardMasterSeedRunner(PhotoCardService photoCardService) {
		this.photoCardService = photoCardService;
	}

	@Override
	public void run(ApplicationArguments args) {
		photoCardService.ensureMastersInitialized();
	}
}
