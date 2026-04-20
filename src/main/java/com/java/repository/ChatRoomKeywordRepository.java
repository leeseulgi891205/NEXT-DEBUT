package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.ChatRoomKeyword;

public interface ChatRoomKeywordRepository extends JpaRepository<ChatRoomKeyword, Long> {

	List<ChatRoomKeyword> findByActiveTrueOrderByKeywordAsc();

	boolean existsByKeywordIgnoreCaseAndActiveTrue(String keyword);
}
