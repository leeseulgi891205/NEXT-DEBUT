package com.java.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.java.Pro01Application;
import com.java.entity.Board;

@SpringBootTest(classes = Pro01Application.class)
@ActiveProfiles("test")
@Transactional
class BoardRepositorySearchTest {

	@Autowired
	private BoardRepository boardRepository;

	@Test
	void searchBoardsPublic_findsByTitleAndContent_nativeQuery() {
		boardRepository.save(new Board("free", null, "고유제목XYZ123", "본문에다른단어", null, null, false, "작성자", null, false));
		boardRepository.flush();

		Page<Board> byTitle = boardRepository.searchBoardsPublic(List.of("free", "notice", "lounge", "guide", "map", "report"),
				"고유제목XYZ123", PageRequest.of(0, 10));
		assertThat(byTitle.getTotalElements()).isGreaterThanOrEqualTo(1);

		Page<Board> byBody = boardRepository.searchBoardsPublic(List.of("free", "notice", "lounge", "guide", "map", "report"),
				"다른단어", PageRequest.of(0, 10));
		assertThat(byBody.getTotalElements()).isGreaterThanOrEqualTo(1);
	}
}
