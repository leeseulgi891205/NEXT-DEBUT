package com.java.photocard.entity;

import java.time.LocalDateTime;

import com.java.game.entity.Trainee;

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
@Table(name = "EQUIPPED_PHOTO_CARD", uniqueConstraints = {
		@UniqueConstraint(name = "UK_EPC_MEMBER_TRAINEE", columnNames = { "MEMBER_ID", "TRAINEE_ID" }) })
public class EquippedPhotoCard {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name = "MEMBER_ID", nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TRAINEE_ID", nullable = false)
	private Trainee trainee;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PHOTO_CARD_MASTER_ID", nullable = false)
	private PhotoCardMaster photoCardMaster;

	@Column(name = "EQUIPPED_AT", nullable = false)
	private LocalDateTime equippedAt = LocalDateTime.now();

	protected EquippedPhotoCard() {
	}

	public EquippedPhotoCard(Long memberId, Trainee trainee, PhotoCardMaster photoCardMaster) {
		this.memberId = memberId;
		this.trainee = trainee;
		this.photoCardMaster = photoCardMaster;
		this.equippedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public Trainee getTrainee() {
		return trainee;
	}

	public PhotoCardMaster getPhotoCardMaster() {
		return photoCardMaster;
	}

	public LocalDateTime getEquippedAt() {
		return equippedAt;
	}

	public void setPhotoCardMaster(PhotoCardMaster photoCardMaster) {
		this.photoCardMaster = photoCardMaster;
		this.equippedAt = LocalDateTime.now();
	}
}
