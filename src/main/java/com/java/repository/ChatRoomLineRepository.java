package com.java.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.ChatRoomLineEntity;

public interface ChatRoomLineRepository extends JpaRepository<ChatRoomLineEntity, Long> {

	Page<ChatRoomLineEntity> findByRoomIdOrderBySentAtDesc(String roomId, Pageable pageable);

	void deleteByRoomId(String roomId);
}
