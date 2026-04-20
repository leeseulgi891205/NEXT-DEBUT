package com.java.game.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.game.entity.GameTurnLog;

public interface GameTurnLogRepository extends JpaRepository<GameTurnLog, Long> {
	List<GameTurnLog> findByRunIdOrderByTurnIndexAsc(Long runId);

	@Query("SELECT COALESCE(MAX(t.turnIndex), 0) FROM GameTurnLog t WHERE t.runId = :runId")
	int findMaxTurnIndexByRunId(@Param("runId") Long runId);

	/** 최근 로그(반복 방지/요약용) */
	List<GameTurnLog> findTop8ByRunIdOrderByTurnIndexDesc(Long runId);

	void deleteByRunId(Long runId);
}

