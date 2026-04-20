package com.java.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.service.MemberProgressMigrationService;

@Component
@Order(60)
public class MemberProgressMigrationRunner implements CommandLineRunner {

	private final MemberProgressMigrationService memberProgressMigrationService;

	public MemberProgressMigrationRunner(MemberProgressMigrationService memberProgressMigrationService) {
		this.memberProgressMigrationService = memberProgressMigrationService;
	}

	@Override
	public void run(String... args) {
		memberProgressMigrationService.migrateToV2IfNeeded();
	}
}
