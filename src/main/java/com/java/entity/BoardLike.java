package com.java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 게시글 좋아요 - 회원당 1번만 허용
 */
@Entity
@Table(name = "BOARD_LIKE", uniqueConstraints = @UniqueConstraint(columnNames = { "BOARD_ID", "MNO" }))
public class BoardLike {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "BOARD_ID", nullable = false)
	private Long boardId;

	@Column(name = "MNO", nullable = false)
	private Long mno;

	protected BoardLike() {
	}

	public BoardLike(Long boardId, Long mno) {
		this.boardId = boardId;
		this.mno = mno;
	}

	public Long getId() {
		return id;
	}

	public Long getBoardId() {
		return boardId;
	}

	public Long getMno() {
		return mno;
	}
}