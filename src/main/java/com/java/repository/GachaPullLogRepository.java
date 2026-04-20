package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.java.entity.GachaPullLog;

public interface GachaPullLogRepository extends JpaRepository<GachaPullLog, Long> {

	void deleteByMemberId(Long memberId);

	@Query("SELECT COUNT(DISTINCT g.memberId) FROM GachaPullLog g")
	long countDistinctMembers();

	@Query("SELECT g.grade, COUNT(g) FROM GachaPullLog g GROUP BY g.grade ORDER BY COUNT(g) DESC")
	List<Object[]> countGroupedByGrade();

	List<GachaPullLog> findTop100ByOrderByCreatedAtDesc();
}
