package com.java.game.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.game.entity.GameMiniQuiz;

public interface GameMiniQuizRepository extends JpaRepository<GameMiniQuiz, Long> {
	List<GameMiniQuiz> findAllByOrderBySortOrderAscIdAsc();

	List<GameMiniQuiz> findByEnabledTrueOrderBySortOrderAscIdAsc();
}
