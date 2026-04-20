package com.java.photocard.entity;

import com.java.game.entity.Trainee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "PHOTO_CARD_MASTER", uniqueConstraints = {
		@UniqueConstraint(name = "UK_PCM_TRAINEE_GRADE", columnNames = { "TRAINEE_ID", "CARD_GRADE" }) })
public class PhotoCardMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "TRAINEE_ID", nullable = false)
	private Trainee trainee;

	@Enumerated(EnumType.STRING)
	@Column(name = "CARD_GRADE", nullable = false, length = 8)
	private PhotoCardGrade grade;

	@Column(name = "DISPLAY_NAME", nullable = false, length = 120)
	private String displayName;

	@Column(name = "IMAGE_URL", length = 300)
	private String imageUrl;

	protected PhotoCardMaster() {
	}

	public PhotoCardMaster(Trainee trainee, PhotoCardGrade grade, String displayName) {
		this.trainee = trainee;
		this.grade = grade;
		this.displayName = displayName;
	}

	public Long getId() {
		return id;
	}

	public Trainee getTrainee() {
		return trainee;
	}

	public PhotoCardGrade getGrade() {
		return grade;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
