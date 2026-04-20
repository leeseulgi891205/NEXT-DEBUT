package com.java.game.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "TRAINEE_MEMBER_LIKE", uniqueConstraints = @UniqueConstraint(name = "UK_TRAINEE_MEMBER_LIKE_RUN", columnNames = {
		"MEMBER_MNO", "TRAINEE_ID", "RUN_ID" }))
public class TraineeMemberLike {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "MEMBER_MNO", nullable = false)
	private Long memberMno;

	@Column(name = "TRAINEE_ID", nullable = false)
	private Long traineeId;

	/** 게임 런 ID. 같은 런에서 동일 연습생에 대해 1회만 좋아요(누적은 여러 런에서 가능). */
	@Column(name = "RUN_ID", nullable = false)
	private Long runId;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	protected TraineeMemberLike() {
	}

	public TraineeMemberLike(Long memberMno, Long traineeId, Long runId) {
		this.memberMno = memberMno;
		this.traineeId = traineeId;
		this.runId = runId;
		this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Long getMemberMno() {
		return memberMno;
	}

	public Long getTraineeId() {
		return traineeId;
	}

	public Long getRunId() {
		return runId;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
