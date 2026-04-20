package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.FanMeetingParticipant;

public interface FanMeetingParticipantRepository extends JpaRepository<FanMeetingParticipant, Long> {

	List<FanMeetingParticipant> findByPostIdOrderByCreatedAtAsc(Long postId);

	boolean existsByPostIdAndUserId(Long postId, Long userId);

	long countByPostId(Long postId);

	long countByPostIdAndStatus(Long postId, String status);
}

