package com.java.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 회원별 일일 탐색 횟수 (무료 3회 초과 시 유료).
 */
@Entity
@Table(name = "CASTING_MAP_DAILY_EXPLORE", uniqueConstraints = @UniqueConstraint(name = "UK_CASTING_MAP_DAILY_MEMBER_DAY", columnNames = {
		"MEMBER_ID", "EXPLORE_DATE" }))
public class CastingMapDailyExplore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	@Column(name = "EXPLORE_DATE", nullable = false)
	private LocalDate exploreDate;

	@Column(name = "EXPLORE_COUNT", nullable = false)
	private int exploreCount = 0;

	protected CastingMapDailyExplore() {
	}

	public CastingMapDailyExplore(Long memberId, LocalDate exploreDate) {
		this.memberId = memberId;
		this.exploreDate = exploreDate;
	}

	public Long getId() {
		return id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public LocalDate getExploreDate() {
		return exploreDate;
	}

	public int getExploreCount() {
		return exploreCount;
	}

	public void setExploreCount(int exploreCount) {
		this.exploreCount = exploreCount;
	}
}
