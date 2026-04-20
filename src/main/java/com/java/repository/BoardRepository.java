package com.java.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {

	long countByBoardType(String boardType);

	/** 데모 시드 중복 방지용 (제목 접두어) */
	long countByTitleStartingWith(String prefix);

	/** 게시판 타입별 목록 — 최신순 */
	List<Board> findByBoardTypeOrderByCreatedAtDesc(String boardType);

	/** 메인 위젯용: 타입별 최신 N개 */
	List<Board> findTop3ByBoardTypeOrderByCreatedAtDesc(String boardType);

	/** 메인 피드용: 노출 가능한 글 중 조회수 높은 글 */
	List<Board> findTop4ByVisibleTrueOrderByViewCountDescCreatedAtDesc();

	/** 메인 피드 TOP5 */
	List<Board> findTop5ByVisibleTrueOrderByViewCountDescCreatedAtDesc();

	/** 메인 실시간 피드용 — 노출 중인 글 중 조회수 높은 순 */
	@Query("""
			SELECT b FROM Board b
			WHERE b.visible = true
			ORDER BY b.viewCount DESC, b.createdAt DESC
			""")
	List<Board> findMainFeedTopViewed(Pageable pageable);

	/** 메인 우측 하단: 인기글 3개(조회수순) */
	List<Board> findTop3ByVisibleTrueOrderByViewCountDescCreatedAtDesc();

	/** 마이페이지: 내가 쓴 글(노출 중) 최신순 */
	List<Board> findTop5ByAuthorNickAndVisibleTrueOrderByCreatedAtDesc(String authorNick);

	/** 마이페이지: 내가 쓴 글(노출/비노출 포함) 최신순 */
	List<Board> findTop5ByAuthorNickOrderByCreatedAtDesc(String authorNick);

	/** 팬미팅(map) 공개 목록 — 일정 가까운 순 (일시 없음은 뒤로) */
	@Query("""
			SELECT b FROM Board b
			WHERE b.boardType = :type AND b.visible = true AND b.fanMeetApproved = true
			ORDER BY CASE WHEN b.eventAt IS NULL THEN 1 ELSE 0 END, b.eventAt ASC, b.createdAt DESC
			""")
	List<Board> findPublicMapBoards(@Param("type") String type);

	/** 팬미팅(fanmeeting) 전용 — 길거리 캐스팅(map)과 분리 */
	@Query("""
			SELECT b FROM Board b
			WHERE b.boardType = :boardType AND b.visible = true AND b.fanMeetApproved = true
			AND (:traineeId IS NULL OR b.traineeId = :traineeId)
			ORDER BY
			  CASE WHEN :sort = 'popular' THEN b.likeCount END DESC,
			  CASE WHEN :sort = 'popular' THEN b.viewCount END DESC,
			  b.createdAt DESC
			""")
	List<Board> findLocationBoardsForUi(@Param("boardType") String boardType, @Param("traineeId") Long traineeId,
			@Param("sort") String sort);

	@Query("""
			SELECT b FROM Board b
			WHERE b.boardType = 'map' AND b.visible = true AND b.fanMeetApproved = true
				AND b.eventAt >= :start AND b.eventAt < :end
			ORDER BY b.eventAt ASC
			""")
	List<Board> findMapEventsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	List<Board> findByBoardTypeAndFanMeetApprovedFalseOrderByCreatedAtDesc(String boardType);

	/** 마이페이지: 최신 공지(노출 중) */
	List<Board> findTop3ByBoardTypeAndVisibleTrueOrderByCreatedAtDesc(String boardType);

	Page<Board> findByBoardTypeOrderByCreatedAtDesc(String boardType, Pageable pageable);

	/** 자유게시판 통합(free + lounge + guide), 어드민 */
	Page<Board> findByBoardTypeInOrderByCreatedAtDesc(Collection<String> boardTypes, Pageable pageable);

	Page<Board> findByBoardTypeAndSecretFalseOrderByCreatedAtDesc(String boardType, Pageable pageable);

	/** 자유게시판 통합, 비로그인 */
	Page<Board> findByBoardTypeInAndSecretFalseOrderByCreatedAtDesc(Collection<String> boardTypes,
			Pageable pageable);

	@Query("SELECT b FROM Board b WHERE b.boardType = :boardType "
			+ "AND (b.secret = false OR b.authorMno = :mno) ORDER BY b.createdAt DESC")
	Page<Board> findVisibleByBoardType(@Param("boardType") String boardType, @Param("mno") Long mno,
			Pageable pageable);

	/** 자유게시판 통합, 로그인 */
	@Query("SELECT b FROM Board b WHERE b.boardType IN :types "
			+ "AND (b.secret = false OR b.authorMno = :mno) ORDER BY b.createdAt DESC")
	Page<Board> findVisibleByBoardTypesIn(@Param("types") Collection<String> types, @Param("mno") Long mno,
			Pageable pageable);

	/**
	 * 통합/게시판 검색 — H2 CLOB 본문은 JPQL LIKE 가 불안정하여 네이티브 SQL 사용.
	 * (BOARD.TITLE / BOARD.CONTENT, MODE=Oracle 호환)
	 */
	@Query(value = """
			SELECT * FROM BOARD
			WHERE BOARD_TYPE IN (:types)
			AND (
				LOWER(TITLE) LIKE LOWER('%' || :q || '%')
				OR LOWER(CAST(CONTENT AS VARCHAR(100000))) LIKE LOWER('%' || :q || '%')
			)
			ORDER BY CREATED_AT DESC
			""",
			countQuery = """
			SELECT COUNT(*) FROM BOARD
			WHERE BOARD_TYPE IN (:types)
			AND (
				LOWER(TITLE) LIKE LOWER('%' || :q || '%')
				OR LOWER(CAST(CONTENT AS VARCHAR(100000))) LIKE LOWER('%' || :q || '%')
			)
			""",
			nativeQuery = true)
	Page<Board> searchBoardsAdmin(@Param("types") Collection<String> types, @Param("q") String q, Pageable pageable);

	@Query(value = """
			SELECT * FROM BOARD
			WHERE BOARD_TYPE IN (:types)
			AND (
				LOWER(TITLE) LIKE LOWER('%' || :q || '%')
				OR LOWER(CAST(CONTENT AS VARCHAR(100000))) LIKE LOWER('%' || :q || '%')
			)
			AND (SECRET = FALSE OR AUTHOR_MNO = :mno)
			ORDER BY CREATED_AT DESC
			""",
			countQuery = """
			SELECT COUNT(*) FROM BOARD
			WHERE BOARD_TYPE IN (:types)
			AND (
				LOWER(TITLE) LIKE LOWER('%' || :q || '%')
				OR LOWER(CAST(CONTENT AS VARCHAR(100000))) LIKE LOWER('%' || :q || '%')
			)
			AND (SECRET = FALSE OR AUTHOR_MNO = :mno)
			""",
			nativeQuery = true)
	Page<Board> searchBoardsForMember(@Param("types") Collection<String> types, @Param("q") String q,
			@Param("mno") Long mno, Pageable pageable);

	@Query(value = """
			SELECT * FROM BOARD
			WHERE BOARD_TYPE IN (:types)
			AND (
				LOWER(TITLE) LIKE LOWER('%' || :q || '%')
				OR LOWER(CAST(CONTENT AS VARCHAR(100000))) LIKE LOWER('%' || :q || '%')
			)
			AND SECRET = FALSE
			ORDER BY CREATED_AT DESC
			""",
			countQuery = """
			SELECT COUNT(*) FROM BOARD
			WHERE BOARD_TYPE IN (:types)
			AND (
				LOWER(TITLE) LIKE LOWER('%' || :q || '%')
				OR LOWER(CAST(CONTENT AS VARCHAR(100000))) LIKE LOWER('%' || :q || '%')
			)
			AND SECRET = FALSE
			""",
			nativeQuery = true)
	Page<Board> searchBoardsPublic(@Param("types") Collection<String> types, @Param("q") String q, Pageable pageable);
}
