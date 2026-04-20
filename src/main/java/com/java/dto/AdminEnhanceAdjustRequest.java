package com.java.dto;

public class AdminEnhanceAdjustRequest {
	private Long traineeId;
	private Integer enhanceLevel;
	private Integer quantity;

	public Long getTraineeId() {
		return traineeId;
	}

	public void setTraineeId(Long traineeId) {
		this.traineeId = traineeId;
	}

	public Integer getEnhanceLevel() {
		return enhanceLevel;
	}

	public void setEnhanceLevel(Integer enhanceLevel) {
		this.enhanceLevel = enhanceLevel;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}
