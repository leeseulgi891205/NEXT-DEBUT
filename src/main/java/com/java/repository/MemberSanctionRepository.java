package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.MemberSanction;

public interface MemberSanctionRepository extends JpaRepository<MemberSanction, Long> {
	List<MemberSanction> findTop10ByMemberMnoOrderByCreatedAtDesc(Long memberMno);
}
