package com.java.dto;

import java.util.List;

public class PhotoCardGrantRequest {
	private Long memberId;
	private Long traineeId;
	private List<Long> traineeIds;
	private String grade;

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public Long getTraineeId() {
		return traineeId;
	}

	public void setTraineeId(Long traineeId) {
		this.traineeId = traineeId;
	}

	public List<Long> getTraineeIds() {
		return traineeIds;
	}

	public void setTraineeIds(List<Long> traineeIds) {
		this.traineeIds = traineeIds;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}
}
