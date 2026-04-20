package com.java.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConceptController {

	/**
	 * 연습생 선택 완료 후 콘셉트 설정 페이지로 이동
	 * @param trainees - 선택된 연습생 이름 배열
	 * @param model
	 * @return
	 */
	@PostMapping("/concept/selection")
	public String handleSelection(
			@RequestParam("trainees[]") String[] trainees,
			Model model) {
		
		// 선택된 연습생 로그 출력
		System.out.println("=== 선택된 연습생 ===");
		Arrays.stream(trainees).forEach(name -> System.out.println("✅ " + name));
		System.out.println("총 " + trainees.length + "명 선택됨");
		
		// 배열을 List로 변환하여 모델에 추가
		List<String> traineeList = Arrays.asList(trainees);
		model.addAttribute("selectedTrainees", traineeList);
		
		// 콘셉트 설정 페이지로 이동 (아직 없으면 임시 메시지)
		return "concept";
	}
}
