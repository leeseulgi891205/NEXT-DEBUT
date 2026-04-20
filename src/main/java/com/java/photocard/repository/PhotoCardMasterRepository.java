package com.java.photocard.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.photocard.entity.PhotoCardGrade;
import com.java.photocard.entity.PhotoCardMaster;

public interface PhotoCardMasterRepository extends JpaRepository<PhotoCardMaster, Long> {

	Optional<PhotoCardMaster> findByTrainee_IdAndGrade(Long traineeId, PhotoCardGrade grade);

	List<PhotoCardMaster> findByGrade(PhotoCardGrade grade);

	List<PhotoCardMaster> findByTrainee_Id(Long traineeId);

	void deleteByTrainee_Id(Long traineeId);

	void deleteByIdIn(Collection<Long> ids);

	@Query("SELECT p FROM PhotoCardMaster p WHERE p.trainee.id IN :traineeIds")
	List<PhotoCardMaster> findByTraineeIds(@Param("traineeIds") List<Long> traineeIds);

	long count();
}
