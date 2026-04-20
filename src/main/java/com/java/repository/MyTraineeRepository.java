package com.java.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.MyTrainee;

public interface MyTraineeRepository extends JpaRepository<MyTrainee, Long> {

	Optional<MyTrainee> findByMemberIdAndTraineeId(Long memberId, Long traineeId);

	List<MyTrainee> findByMemberIdOrderByIdDesc(Long memberId);

	void deleteByTraineeId(Long traineeId);

	void deleteByMemberId(Long memberId);
}
