package com.java.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.CastingMapDailyExplore;

public interface CastingMapDailyExploreRepository extends JpaRepository<CastingMapDailyExplore, Long> {

	Optional<CastingMapDailyExplore> findByMemberIdAndExploreDate(Long memberId, LocalDate exploreDate);

	void deleteByMemberId(Long memberId);
}
