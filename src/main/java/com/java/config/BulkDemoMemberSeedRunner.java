package com.java.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.service.BulkDemoMemberSeedService;

/**
 * 데모/테스트용 신규 회원 일괄 생성 (기본 비활성화).
 * <p>
 * {@code app.seed.demo-members=true} 일 때만 실행된다.
 * {@code seedmem001} 이 이미 있으면 기본적으로 스킵하며,
 * {@code app.seed.demo-members.force=true} 이면 기존 일반 회원을 정리하고 다시 만든다.
 * </p>
 * 비밀번호는 모든 시드 계정 동일: {@code Seed1234}
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "app.seed.demo-members", havingValue = "true")
public class BulkDemoMemberSeedRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(BulkDemoMemberSeedRunner.class);

	private final BulkDemoMemberSeedService bulkDemoMemberSeedService;

	@Value("${app.seed.demo-members.force:false}")
	private boolean forceReseed;

	public BulkDemoMemberSeedRunner(BulkDemoMemberSeedService bulkDemoMemberSeedService) {
		this.bulkDemoMemberSeedService = bulkDemoMemberSeedService;
	}

	@Override
	public void run(String... args) {
		try {
			if (forceReseed) {
				log.info("[demo-seed] force=true: admin/aaa1111 제외 기존 회원을 정리한 뒤 다시 생성합니다.");
				bulkDemoMemberSeedService.removeExistingDemoSeedMembers();
			} else if (bulkDemoMemberSeedService.shouldSkip()) {
				log.info("[demo-seed] seedmem001 이(가) 이미 있어 시드를 건너뜁니다. 다시 넣으려면 application.properties 에 "
						+ "app.seed.demo-members.force=true 를 추가한 뒤 서버를 재시작하세요.");
				return;
			}
			bulkDemoMemberSeedService.seedDemoMembers();
			log.info("[demo-seed] 데모 회원 {}명 생성 완료 (비밀번호 Seed1234).", 100);
		} catch (Exception e) {
			log.warn("[demo-seed] 시드 실패: {}", e.getMessage());
		}
	}
}
