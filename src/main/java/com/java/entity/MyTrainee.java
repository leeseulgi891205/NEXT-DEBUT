package com.java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "MY_TRAINEE", uniqueConstraints = {
		@UniqueConstraint(name = "UK_MY_TRAINEE_MEMBER_TRAINEE", columnNames = { "MEMBER_ID", "TRAINEE_ID" }) })
public class MyTrainee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	@Column(name = "TRAINEE_ID", nullable = false)
	private Long traineeId;

	@Column(nullable = false)
	private int quantity = 1;

	@Column(name = "ENHANCE_LEVEL", nullable = false)
	private int enhanceLevel = 0;

	protected MyTrainee() {
	}

	public MyTrainee(Long memberId, Long traineeId, int quantity) {
		this.memberId = memberId;
		this.traineeId = traineeId;
		this.quantity = quantity;
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

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getEnhanceLevel() {
		return enhanceLevel;
	}

	public void setEnhanceLevel(int enhanceLevel) {
		this.enhanceLevel = enhanceLevel;
	}
}
