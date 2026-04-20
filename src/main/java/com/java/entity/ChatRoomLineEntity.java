package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "CHAT_ROOM_LINE", indexes = @Index(name = "IX_CHAT_ROOM_LINE_ROOM_SENT", columnList = "ROOM_ID,SENT_AT"))
public class ChatRoomLineEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ROOM_ID", nullable = false, length = 16)
	private String roomId;

	@Column(name = "NICKNAME", nullable = false, length = 80)
	private String nickname;

	@Column(name = "CONTENT", nullable = false, length = 2000)
	private String content;

	@Column(name = "SENT_AT", nullable = false)
	private LocalDateTime sentAt;

	public ChatRoomLineEntity() {
	}

	public Long getId() {
		return id;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
}
