package com.java.photocard.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.photocard.entity.UserPhotoCard;

public interface UserPhotoCardRepository extends JpaRepository<UserPhotoCard, Long> {

	boolean existsByMemberIdAndPhotoCardMaster_Id(Long memberId, Long photoCardMasterId);

	List<UserPhotoCard> findByMemberId(Long memberId);

	void deleteByPhotoCardMaster_IdIn(Collection<Long> photoCardMasterIds);

	void deleteByMemberId(Long memberId);
}
