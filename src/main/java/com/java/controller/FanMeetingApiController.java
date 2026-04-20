package com.java.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import com.java.config.SessionConst;
import com.java.dto.LoginMember;
import com.java.entity.Board;
import com.java.entity.BoardComment;
import com.java.repository.BoardCommentRepository;
import com.java.repository.BoardLikeRepository;
import com.java.repository.BoardRepository;
import com.java.service.BoardService;

import jakarta.servlet.http.HttpSession;

/**
 * 팬미팅 게시판 REST API — 길거리 캐스팅(/boards/map)과 분리된 boardType=fanmeeting 전용.
 */
@RestController
@RequestMapping("/api/fanmeetings")
public class FanMeetingApiController {

	private final BoardRepository boardRepository;
	private final BoardCommentRepository commentRepository;
	private final BoardLikeRepository likeRepository;
	private final BoardService boardService;

	public FanMeetingApiController(BoardRepository boardRepository, BoardCommentRepository commentRepository,
			BoardLikeRepository likeRepository, BoardService boardService) {
		this.boardRepository = boardRepository;
		this.commentRepository = commentRepository;
		this.likeRepository = likeRepository;
		this.boardService = boardService;
	}

	@GetMapping
	public ResponseEntity<Map<String, Object>> list(
			@RequestParam(value = "traineeId", required = false) Long traineeId,
			@RequestParam(value = "sort", defaultValue = "latest") String sort) {
		String sortKey = "popular".equalsIgnoreCase(sort) ? "popular" : "latest";
		List<Board> posts = boardRepository.findLocationBoardsForUi("fanmeeting", traineeId, sortKey);
		List<Map<String, Object>> items = posts.stream().map(this::toSummaryDto).collect(Collectors.toList());
		return ResponseEntity.ok(Map.of("items", items, "sort", sortKey));
	}

	@GetMapping("/{id:\\d+}")
	public ResponseEntity<Map<String, Object>> detail(@PathVariable("id") Long id) {
		Board b = boardRepository.findById(id).orElse(null);
		if (b == null || !"fanmeeting".equals(b.getBoardType())) {
			return ResponseEntity.notFound().build();
		}
		if (!b.isVisible() || !b.isFanMeetApproved()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "비공개 글입니다."));
		}
		return ResponseEntity.ok(toDetailDto(b));
	}

	@PostMapping
	@Transactional
	public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body, HttpSession session) {
		LoginMember lm = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (lm == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
		}
		String title = str(body.get("title"));
		String content = str(body.get("content"));
		if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
			return ResponseEntity.badRequest().body(Map.of("error", "제목과 내용을 입력해주세요."));
		}
		Long traineeId = longOrNull(body.get("trainee_id"));
		if (traineeId == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "연습생(trainee_id)이 필요합니다."));
		}
		String eventAtRaw = str(body.get("event_date"));
		LocalDateTime ev = boardService.parseEventAtParam(eventAtRaw);
		if (ev == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "팬미팅 일시(event_date, ISO 로컬 형식)가 필요합니다."));
		}
		String latStr = str(body.get("lat"));
		String lngStr = str(body.get("lng"));
		Board board = new Board("fanmeeting", title, content, null, null, false, lm.nickname());
		board.setAuthorMno(lm.mno());
		board.setTraineeId(traineeId);
		boardService.applyOptionalLocation(board, str(body.get("place_name")), str(body.get("address")), latStr,
				lngStr);
		if (!board.isHasMapLocation()) {
			return ResponseEntity.badRequest().body(Map.of("error", "유효한 위치(lat, lng)가 필요합니다."));
		}
		String recruit = str(body.get("status"));
		String maxCap = body.get("max_capacity") != null ? String.valueOf(body.get("max_capacity")) : null;
		String part = str(body.get("participation_type"));
		boardService.applyFanMeetFields(board, ev, recruit, maxCap, part);
		board.setFanMeetApproved(true);
		boardRepository.save(board);
		return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(board));
	}

	@GetMapping("/{postId:\\d+}/comments")
	public ResponseEntity<Map<String, Object>> listComments(@PathVariable("postId") Long postId) {
		Board b = boardRepository.findById(postId).orElse(null);
		if (b == null || !"fanmeeting".equals(b.getBoardType())) {
			return ResponseEntity.notFound().build();
		}
		if (!b.isVisible() || !b.isFanMeetApproved()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "비공개 글입니다."));
		}
		List<BoardComment> list = commentRepository.findByBoardIdOrderByCreatedAtAsc(postId);
		List<Map<String, Object>> out = list.stream().map(c -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("id", c.getId());
			m.put("user_id", c.getAuthorMno());
			m.put("author_nick", c.getAuthorNick());
			m.put("content", c.getContent());
			m.put("created_at", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
			return m;
		}).collect(Collectors.toList());
		return ResponseEntity.ok(Map.of("comments", out));
	}

	@PostMapping("/{postId:\\d+}/comments")
	@Transactional
	public ResponseEntity<Map<String, Object>> addComment(@PathVariable("postId") Long postId,
			@RequestBody Map<String, Object> body, HttpSession session) {
		LoginMember lm = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (lm == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
		}
		Board b = boardRepository.findById(postId).orElse(null);
		if (b == null || !"fanmeeting".equals(b.getBoardType())) {
			return ResponseEntity.notFound().build();
		}
		if (!b.isVisible() || !b.isFanMeetApproved()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "댓글을 작성할 수 없습니다."));
		}
		String content = str(body.get("content"));
		if (!StringUtils.hasText(content) || content.length() > 500) {
			return ResponseEntity.badRequest().body(Map.of("error", "댓글은 1~500자로 입력해주세요."));
		}
		BoardComment saved = commentRepository.save(new BoardComment(b, content, lm.nickname(), lm.mno()));
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("id", saved.getId(), "created_at",
						saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : ""));
	}

	@DeleteMapping("/comments/{commentId:\\d+}")
	@Transactional
	public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable("commentId") Long commentId,
			HttpSession session) {
		LoginMember lm = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (lm == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
		}
		BoardComment c = commentRepository.findById(commentId).orElse(null);
		if (c == null || c.getBoard() == null) {
			return ResponseEntity.notFound().build();
		}
		if (!"fanmeeting".equals(c.getBoard().getBoardType())) {
			return ResponseEntity.notFound().build();
		}
		if (!lm.mno().equals(c.getAuthorMno()) && !"ADMIN".equals(lm.role())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "삭제 권한이 없습니다."));
		}
		commentRepository.delete(c);
		return ResponseEntity.ok(Map.of("success", true));
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Map<String, Object>> delete(@PathVariable("id") Long id, HttpSession session) {
		LoginMember lm = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (lm == null || !"ADMIN".equals(lm.role())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "관리자만 삭제할 수 있습니다."));
		}
		Board b = boardRepository.findById(id).orElse(null);
		if (b == null || !"fanmeeting".equals(b.getBoardType())) {
			return ResponseEntity.notFound().build();
		}
		commentRepository.deleteByBoardId(id);
		likeRepository.deleteByBoardId(id);
		boardRepository.delete(b);
		return ResponseEntity.ok(Map.of("success", true));
	}

	private static String str(Object o) {
		return o == null ? null : String.valueOf(o).trim();
	}

	private static Long longOrNull(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Number n) {
			return n.longValue();
		}
		try {
			return Long.parseLong(String.valueOf(o).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Map<String, Object> toSummaryDto(Board b) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", b.getId());
		m.put("trainee_id", b.getTraineeId());
		m.put("title", b.getTitle());
		m.put("place_name", b.getPlaceName());
		m.put("address", b.getAddress());
		m.put("lat", b.getLat());
		m.put("lng", b.getLng());
		m.put("event_date", b.getEventAt() != null ? b.getEventAt().toString() : null);
		m.put("status", b.getRecruitStatus());
		m.put("views", b.getViewCount());
		m.put("likes_count", b.getLikeCount());
		return m;
	}

	private Map<String, Object> toDetailDto(Board b) {
		Map<String, Object> m = new LinkedHashMap<>(toSummaryDto(b));
		m.put("content", b.getContent());
		m.put("author_nick", b.getAuthorNick());
		m.put("created_at", b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
		m.put("fan_meet_approved", b.isFanMeetApproved());
		return m;
	}
}
