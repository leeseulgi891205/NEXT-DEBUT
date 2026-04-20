package com.java.photocard.repository;

import java.util.Optional;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.photocard.entity.EquippedPhotoCard;

public interface EquippedPhotoCardRepository extends JpaRepository<EquippedPhotoCard, Long> {

	Optional<EquippedPhotoCard> findByMemberIdAndTrainee_Id(Long memberId, Long traineeId);

	void deleteByTrainee_Id(Long traineeId);

	void deleteByPhotoCardMaster_IdIn(Collection<Long> photoCardMasterIds);

	void deleteByMemberId(Long memberId);
}
