package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "CHAT_ROOM_VISITOR", uniqueConstraints = {
		@UniqueConstraint(name = "UK_CHAT_ROOM_VISITOR", columnNames = { "ROOM_ID", "NICKNAME" }) })
public class ChatRoomVisitorEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ROOM_ID", nullable = false, length = 16)
	private String roomId;

	@Column(name = "NICKNAME", nullable = false, length = 80)
	private String nickname;

	@Column(name = "FIRST_SEEN", nullable = false)
	private LocalDateTime firstSeen;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public LocalDateTime getFirstSeen() {
		return firstSeen;
	}

	public void setFirstSeen(LocalDateTime firstSeen) {
		this.firstSeen = firstSeen;
	}
}
