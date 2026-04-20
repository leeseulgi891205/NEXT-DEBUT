package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 실시간 채팅방 부적절 키워드 — 관리자가 추가·삭제 (기본 키워드는 코드에도 유지).
 */
@Entity
@Table(name = "CHAT_ROOM_KEYWORD")
public class ChatRoomKeyword {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(nullable = false, length = 100)
	private String keyword;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	protected ChatRoomKeyword() {
	}

	public ChatRoomKeyword(String keyword) {
		this.keyword = keyword;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
