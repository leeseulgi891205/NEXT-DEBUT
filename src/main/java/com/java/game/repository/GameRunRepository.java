package com.java.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.java.game.entity.GameRun;

public interface GameRunRepository extends JpaRepository<GameRun, Long> {
	/** 회원별 게임 기록 (최신순) */
	java.util.List<GameRun> findByPlayerMnoOrderByCreatedAtDesc(Long playerMno);

	/** 메인 페이지 요약/랭킹용 (최신순 일부) */
	java.util.List<GameRun> findTop50ByOrderByCreatedAtDesc();

	/** game_scene 재시드 시 이전 씬 ID가 무효해지므로 진행 중 런의 포인터만 초기화 */
	@Modifying
	@Query("update GameRun r set r.currentSceneId = null where r.currentSceneId is not null")
	int clearCurrentSceneIds();
}