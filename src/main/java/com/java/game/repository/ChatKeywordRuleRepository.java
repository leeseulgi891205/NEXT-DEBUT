package com.java.game.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.game.entity.ChatKeywordRule;

public interface ChatKeywordRuleRepository extends JpaRepository<ChatKeywordRule, Long> {
	List<ChatKeywordRule> findByActiveTrueOrderByPriorityDescIdAsc();
}

