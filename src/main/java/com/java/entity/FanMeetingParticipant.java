package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "FANMEETING_PARTICIPANT")
public class FanMeetingParticipant {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "POST_ID", nullable = false)
	private Long postId;

	@Column(name = "USER_ID", nullable = false)
	private Long userId;

	@Column(name = "USER_NICK", length = 60, nullable = false)
	private String userNick;

	/** APPLIED / PICKED */
	@Column(name = "STATUS", length = 20, nullable = false)
	private String status = "APPLIED";

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	protected FanMeetingParticipant() {
	}

	public FanMeetingParticipant(Long postId, Long userId, String userNick, String status) {
		this.postId = postId;
		this.userId = userId;
		this.userNick = userNick;
		this.status = status == null ? "APPLIED" : status;
	}

	public Long getId() {
		return id;
	}

	public Long getPostId() {
		return postId;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUserNick() {
		return userNick;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public String getStatusLabel() {
		if ("APPROVED".equalsIgnoreCase(status)) {
			return "승인";
		}
		if ("WAITING".equalsIgnoreCase(status)) {
			return "대기";
		}
		if ("PICKED".equalsIgnoreCase(status)) {
			return "선정";
		}
		return "신청";
	}
}

