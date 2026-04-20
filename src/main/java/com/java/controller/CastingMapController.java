package com.java.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 예전 URL 호환: /casting/map → 캐스팅 이벤트 게시판(/boards/map)으로 이동.
 */
@Controller
@RequestMapping("/casting")
public class CastingMapController {

	@GetMapping("/map")
	public String legacyCastingMap() {
		return "redirect:/boards/map";
	}
}
