package com.java.game.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.game.entity.GameScene;

public interface GameSceneRepository extends JpaRepository<GameScene, Long> {
	java.util.List<GameScene> findAllByOrderByPhaseAscIdAsc();

	/** phase 이름으로 씬 1개 조회 (DAY1 고정 씬용) */
	Optional<GameScene> findByPhase(String phase);

	/** 해당 phase의 씬이 이미 존재하는지 확인 */
	boolean existsByPhase(String phase);

	/** 해당 phase의 씬 전체 목록 (랜덤 풀용) */
	java.util.List<GameScene> findAllByPhase(String phase);

	/** 해당 phase의 씬 개수 */
	long countByPhase(String phase);
}
