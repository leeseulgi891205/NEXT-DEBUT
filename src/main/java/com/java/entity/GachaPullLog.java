package com.java.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "GACHA_PULL_LOG")
public class GachaPullLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	@Column(name = "TRAINEE_ID", nullable = false)
	private Long traineeId;

	@Column(length = 10)
	private String grade;

	@Column(name = "POOL_ID", nullable = false, length = 32)
	private String poolId = "DEFAULT";

	@Column(name = "CREATED_AT", nullable = false)
	private Instant createdAt;

	public GachaPullLog() {
	}

	public GachaPullLog(Long memberId, Long traineeId, String grade, String poolId) {
		this.memberId = memberId;
		this.traineeId = traineeId;
		this.grade = grade;
		this.poolId = poolId;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Long getTraineeId() {
		return traineeId;
	}

	public String getGrade() {
		return grade;
	}

	public String getPoolId() {
		return poolId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
