package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.BoardReport;

public interface BoardReportRepository extends JpaRepository<BoardReport, Long> {

	boolean existsByTargetTypeAndTargetIdAndReporterMno(String targetType, Long targetId, Long reporterMno);

	List<BoardReport> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);

	List<BoardReport> findByStatusOrderByCreatedAtDesc(String status);

	List<BoardReport> findAllByOrderByCreatedAtDesc();
}
