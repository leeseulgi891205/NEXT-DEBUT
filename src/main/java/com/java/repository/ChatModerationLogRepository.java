package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.ChatModerationLogEntity;

public interface ChatModerationLogRepository extends JpaRepository<ChatModerationLogEntity, Long> {

	List<ChatModerationLogEntity> findTop200ByOrderByIdDesc();
}
