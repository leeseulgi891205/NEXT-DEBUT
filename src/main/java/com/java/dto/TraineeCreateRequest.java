package com.java.dto;

import com.java.game.entity.Gender;

public class TraineeCreateRequest {

	private String name;
	private Gender gender;
	private Integer vocal;
	private Integer dance;
	private Integer star;
	private Integer mental;
	private Integer teamwork;
	private String imagePath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Integer getVocal() {
		return vocal;
	}

	public void setVocal(Integer vocal) {
		this.vocal = vocal;
	}

	public Integer getDance() {
		return dance;
	}

	public void setDance(Integer dance) {
		this.dance = dance;
	}

	public Integer getStar() {
		return star;
	}

	public void setStar(Integer star) {
		this.star = star;
	}

	public Integer getMental() {
		return mental;
	}

	public void setMental(Integer mental) {
		this.mental = mental;
	}

	public Integer getTeamwork() {
		return teamwork;
	}

	public void setTeamwork(Integer teamwork) {
		this.teamwork = teamwork;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
}
