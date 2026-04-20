package com.java.service;

import java.util.List;

import com.java.dto.TraineeCreateRequest;
import com.java.dto.TraineeStatsUpdateRequest;
import com.java.dto.TraineeUpdateRequest;
import com.java.game.entity.Gender;
import com.java.game.entity.Grade;
import com.java.game.entity.Trainee;

public interface TraineeService {

	Trainee createTrainee(TraineeCreateRequest request);

	Trainee updateTrainee(Long traineeId, TraineeUpdateRequest request);

	Trainee updateStats(Long traineeId, TraineeStatsUpdateRequest request);

	Trainee updateImage(Long traineeId, String imagePath);

	void deleteTrainee(Long traineeId);

	List<Trainee> searchForAdmin(String keyword, Gender gender, Grade grade, String sort);
}
