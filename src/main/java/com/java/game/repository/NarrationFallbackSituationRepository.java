package com.java.game.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.game.entity.NarrationFallbackSituation;

public interface NarrationFallbackSituationRepository extends JpaRepository<NarrationFallbackSituation, Long> {

	List<NarrationFallbackSituation> findByStatCategoryOrderBySortOrderAsc(String statCategory);
}
