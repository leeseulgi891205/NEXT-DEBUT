package com.java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.BoardLike;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
	Optional<BoardLike> findByBoardIdAndMno(Long boardId, Long mno);

	boolean existsByBoardIdAndMno(Long boardId, Long mno);

	void deleteByBoardId(Long boardId);
}