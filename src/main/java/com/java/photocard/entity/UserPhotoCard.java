package com.java.photocard.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "USER_PHOTO_CARD", uniqueConstraints = {
		@UniqueConstraint(name = "UK_UPC_MEMBER_MASTER", columnNames = { "MEMBER_ID", "PHOTO_CARD_MASTER_ID" }) })
public class UserPhotoCard {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PHOTO_CARD_MASTER_ID", nullable = false)
	private PhotoCardMaster photoCardMaster;

	@Column(name = "OWNED_AT", nullable = false)
	private LocalDateTime ownedAt = LocalDateTime.now();

	protected UserPhotoCard() {
	}

	public UserPhotoCard(Long memberId, PhotoCardMaster photoCardMaster) {
		this.memberId = memberId;
		this.photoCardMaster = photoCardMaster;
		this.ownedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public PhotoCardMaster getPhotoCardMaster() {
		return photoCardMaster;
	}

	public LocalDateTime getOwnedAt() {
		return ownedAt;
	}
}
