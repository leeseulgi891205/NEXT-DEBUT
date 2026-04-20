package com.java.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.ChatRoomEntity;

public interface ChatRoomEntityRepository extends JpaRepository<ChatRoomEntity, String> {
}
