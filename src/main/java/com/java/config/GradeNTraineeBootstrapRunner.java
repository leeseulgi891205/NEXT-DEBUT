package com.java.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.java.service.StarterTraineeGrantService;

/** 기존 회원에게 시작 그룹(라이즈/하츠투하츠) 연습생이 없으면 채움. */
@Component
@Order(50)
public class GradeNTraineeBootstrapRunner implements CommandLineRunner {

	private final StarterTraineeGrantService starterTraineeGrantService;

	public GradeNTraineeBootstrapRunner(StarterTraineeGrantService starterTraineeGrantService) {
		this.starterTraineeGrantService = starterTraineeGrantService;
	}

	@Override
	public void run(String... args) {
		starterTraineeGrantService.grantStarterGroupsForAllMembers();
	}
}
