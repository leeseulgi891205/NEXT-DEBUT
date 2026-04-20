package com.java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.entity.BoardComment;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {
	/** 특정 게시글의 댓글 목록 (등록순) */
	List<BoardComment> findByBoardIdOrderByCreatedAtAsc(Long boardId);

	/** 특정 게시글 댓글 수 */
	long countByBoardId(Long boardId);

	/** 게시글 삭제 시 댓글도 삭제 */
	void deleteByBoardId(Long boardId);
}