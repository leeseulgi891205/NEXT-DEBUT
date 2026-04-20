package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.ChatRoomVisitorEntity;

public interface ChatRoomVisitorEntityRepository extends JpaRepository<ChatRoomVisitorEntity, Long> {

	List<ChatRoomVisitorEntity> findByRoomIdOrderByFirstSeenAsc(String roomId);

	boolean existsByRoomIdAndNickname(String roomId, String nickname);

	void deleteByRoomId(String roomId);
}
