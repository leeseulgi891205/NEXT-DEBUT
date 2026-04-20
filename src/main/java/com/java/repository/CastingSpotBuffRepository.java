package com.java.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.entity.CastingSpotBuff;

public interface CastingSpotBuffRepository extends JpaRepository<CastingSpotBuff, Long> {

	Optional<CastingSpotBuff> findFirstByMemberIdAndExpireAtAfterOrderByExpireAtDesc(Long memberId,
			LocalDateTime now);

	@Modifying
	@Query("DELETE FROM CastingSpotBuff b WHERE b.memberId = :memberId")
	int deleteByMemberId(@Param("memberId") Long memberId);
}
