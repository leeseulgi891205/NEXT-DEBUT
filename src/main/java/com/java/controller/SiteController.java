package com.java.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.java.config.SessionConst;

import jakarta.servlet.http.HttpSession;

@Controller
public class SiteController {


    @GetMapping("/notice")
    public String notice() {
        return "redirect:/boards/notice";
    }

    @GetMapping("/guide")
    public String guide() {
        return "pages/guide";
    }

    @GetMapping("/board")
    public String board() {
        return "redirect:/boards/free";
    }

    @GetMapping("/report")
    public String report() {
        return "redirect:/boards/report";
    }

    /**
     * 이미 로그인된 상태에서 /login 으로 오면(새로고침·북마크 등) 상단 네비는 로그아웃으로 보이는데
     * 본문만 로그인 폼이 나오는 불일치가 생기므로, 세션이 있으면 원래 가려던 경로 또는 메인으로 보낸다.
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "redirect", required = false) String redirect,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "message", required = false) String message,
            HttpSession session,
            Model model) {
        if (session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")) {
                return "redirect:" + redirect;
            }
            return "redirect:/main";
        }
        if (error != null) {
            model.addAttribute("loginError", message != null && !message.isBlank()
                    ? "소셜 로그인 실패: " + message
                    : "소셜 로그인에 실패했습니다. 설정값을 다시 확인해주세요.");
        }
        return "auth/login";
    }
}
