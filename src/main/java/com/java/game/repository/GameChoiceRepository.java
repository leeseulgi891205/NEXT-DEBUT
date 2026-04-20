package com.java.game.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.game.entity.GameChoice;

public interface GameChoiceRepository extends JpaRepository<GameChoice, Long> {
	List<GameChoice> findAllByOrderByPhaseAscSortOrderAsc();

	/** phase에 해당하는 선택지 목록 (정렬 순서대로) */
	List<GameChoice> findByPhaseOrderBySortOrder(String phase);

	/** 중복 삽입 방지 체크 */
	boolean existsByPhaseAndChoiceKey(String phase, String choiceKey);

	/** 기존 선택지 업데이트용 조회 */
	Optional<GameChoice> findByPhaseAndChoiceKey(String phase, String choiceKey);
}
