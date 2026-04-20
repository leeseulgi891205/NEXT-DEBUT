package com.java.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "MARKET_TXN")
public class MarketTxn {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	/** BUY / ADMIN_ADJUST */
	@Column(name = "TXN_TYPE", nullable = false, length = 30)
	private String txnType;

	@Column(name = "ITEM_NAME", length = 100)
	private String itemName;

	/** 코인 변화량(+/-). 구매는 음수 */
	@Column(name = "COIN_DELTA", nullable = false)
	private int coinDelta = 0;

	@Column(name = "NOTE", length = 255)
	private String note;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	protected MarketTxn() {
	}

	public MarketTxn(Long memberId, String txnType, String itemName, int coinDelta, String note) {
		this.memberId = memberId;
		this.txnType = txnType;
		this.itemName = itemName;
		this.coinDelta = coinDelta;
		this.note = note;
	}

	@PrePersist
	void onCreate() {
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

	public String getTxnType() {
		return txnType;
	}

	public String getItemName() {
		return itemName;
	}

	public int getCoinDelta() {
		return coinDelta;
	}

	public String getNote() {
		return note;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}

