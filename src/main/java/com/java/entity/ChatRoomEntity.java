package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CHAT_ROOM")
public class ChatRoomEntity {

	@Id
	@Column(name = "ROOM_ID", nullable = false, length = 16)
	private String roomId;

	@Column(name = "ROOM_NAME", nullable = false, length = 80)
	private String roomName;

	@Column(name = "CREATOR_NICKNAME", nullable = false, length = 80)
	private String creatorNickname;

	@Column(name = "SECRET", nullable = false)
	private boolean secret;

	/** 비밀방 비밀번호 (평문, 기존 메모리 방식과 동일) */
	@Column(name = "PASSWORD", length = 120)
	private String password;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getCreatorNickname() {
		return creatorNickname;
	}

	public void setCreatorNickname(String creatorNickname) {
		this.creatorNickname = creatorNickname;
	}

	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
