package com.java.dto;

import java.util.List;

public record PhotoCardBatchResultDto(
		String result,
		String message,
		List<PhotoCardDrawLineDto> lines,
		Integer currentCoin) {
}
