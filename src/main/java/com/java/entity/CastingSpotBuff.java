package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * 캐스팅 맵에서 발견한 스팟 버프 (가챠에 반영, 만료 시 무효).
 */
@Entity
@Table(name = "CASTING_SPOT_BUFF", indexes = {
		@Index(name = "IX_CASTING_SPOT_BUFF_MEMBER_EXPIRE", columnList = "MEMBER_ID, EXPIRE_AT") })
public class CastingSpotBuff {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	@Column(name = "REGION_CODE", length = 40)
	private String regionCode;

	/** UI용 (예: 홍대 캐스팅 스팟) */
	@Column(name = "SPOT_LABEL", nullable = false, length = 120)
	private String spotLabel;

	@Column(name = "EFFECT_TYPE", nullable = false, length = 40)
	private String effectType;

	@Column(name = "EFFECT_VALUE", length = 80)
	private String effectValue;

	@Column(name = "EXPIRE_AT", nullable = false)
	private LocalDateTime expireAt;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	protected CastingSpotBuff() {
	}

	public CastingSpotBuff(Long memberId, String regionCode, String spotLabel, String effectType, String effectValue,
			LocalDateTime expireAt) {
		this.memberId = memberId;
		this.regionCode = regionCode;
		this.spotLabel = spotLabel;
		this.effectType = effectType;
		this.effectValue = effectValue;
		this.expireAt = expireAt;
	}

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public String getSpotLabel() {
		return spotLabel;
	}

	public String getEffectType() {
		return effectType;
	}

	public String getEffectValue() {
		return effectValue;
	}

	public LocalDateTime getExpireAt() {
		return expireAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public CastingEventEffectType getEffectTypeEnum() {
		return CastingEventEffectType.fromDb(effectType);
	}

	public int parseEffectBasisPoints() {
		CastingEventEffectType t = getEffectTypeEnum();
		if (t != CastingEventEffectType.SSR_UP && t != CastingEventEffectType.SR_UP) {
			return 0;
		}
		if (effectValue == null || effectValue.isBlank()) {
			return 200;
		}
		try {
			return Math.max(0, Integer.parseInt(effectValue.trim()));
		} catch (NumberFormatException e) {
			return 200;
		}
	}

	public int parseDiscountPercent() {
		if (getEffectTypeEnum() != CastingEventEffectType.DISCOUNT_PULL) {
			return 0;
		}
		try {
			if (effectValue == null || effectValue.isBlank()) {
				return 10;
			}
			int p = Integer.parseInt(effectValue.trim());
			return Math.max(0, Math.min(90, p));
		} catch (NumberFormatException e) {
			return 10;
		}
	}

	/** 가챠 배너·맵 결과용 한 줄 */
	public String getEffectSummaryLine() {
		CastingEventEffectType t = getEffectTypeEnum();
		if (t == null) {
			return "";
		}
		int bp = parseEffectBasisPoints();
		switch (t) {
		case SSR_UP:
			return "SSR 확률 +" + (bp / 100.0) + "% 증가";
		case SR_UP:
			return "SR 확률 +" + (bp / 100.0) + "% 증가";
		case DISCOUNT_PULL:
			return "뽑기 코인 " + parseDiscountPercent() + "% 할인";
		case BONUS_PULL:
			return "5회 뽑기 시 1회 추가 지급";
		default:
			return "";
		}
	}
}
