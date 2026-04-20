package com.java.dto;

public record AdminPhotoCardDto(
		Long id,
		Long traineeId,
		String grade,
		String imageUrl,
		boolean configured) {
}
