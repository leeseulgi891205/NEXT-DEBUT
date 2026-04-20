package com.java.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.java.config.SessionConst;
import com.java.dto.KakaoApproveResponse;
import com.java.dto.LoginMember;
import com.java.service.KakaoPayService;
import com.java.service.MarketService;

import jakarta.servlet.http.HttpSession;

@Controller
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final MarketService marketService;

    public KakaoPayController(KakaoPayService kakaoPayService, MarketService marketService) {
        this.kakaoPayService = kakaoPayService;
        this.marketService = marketService;
    }

    @PostMapping("/kakao/ready")
    @ResponseBody
    public Map<String, Object> kakaoReady(@RequestBody Map<String, Object> request, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            result.put("result", "logout");
            return result;
        }

        int amount = Integer.parseInt(String.valueOf(request.get("amount")));

        Map<String, String> ready = kakaoPayService.ready(loginMember.mno(), amount);

        session.setAttribute("kakao_tid", ready.get("tid"));
        session.setAttribute("kakao_partner_order_id", ready.get("partnerOrderId"));
        session.setAttribute("kakao_partner_user_id", ready.get("partnerUserId"));
        session.setAttribute("kakao_charge_amount", amount);

        result.put("result", "success");
        result.put("redirectUrl", ready.get("nextRedirectPcUrl"));
        return result;
    }

    @GetMapping("/kakao/success")
    public String kakaoSuccess(@RequestParam("pg_token") String pgToken, HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            return "redirect:/login";
        }

        String tid = (String) session.getAttribute("kakao_tid");
        String partnerOrderId = (String) session.getAttribute("kakao_partner_order_id");
        String partnerUserId = (String) session.getAttribute("kakao_partner_user_id");
        Integer amount = (Integer) session.getAttribute("kakao_charge_amount");

        System.out.println("=== kakao success ===");
        System.out.println("pgToken = " + pgToken);
        System.out.println("tid = " + tid);
        System.out.println("partnerOrderId = " + partnerOrderId);
        System.out.println("partnerUserId = " + partnerUserId);
        System.out.println("amount = " + amount);

        if (tid == null || partnerOrderId == null || partnerUserId == null || amount == null) {
            return "redirect:/market/shop?pay=invalid";
        }

        KakaoApproveResponse approve = kakaoPayService.approve(tid, partnerOrderId, partnerUserId, pgToken);

        if (approve != null) {
            marketService.addCoin(loginMember.mno(), amount);
            marketService.logCharge(loginMember.mno(), amount, "kakao_pay");
        }

        session.removeAttribute("kakao_tid");
        session.removeAttribute("kakao_partner_order_id");
        session.removeAttribute("kakao_partner_user_id");
        session.removeAttribute("kakao_charge_amount");

        return "redirect:/market/shop?pay=success";
    }

    @GetMapping("/kakao/cancel")
    public String kakaoCancel() {
        return "redirect:/market/shop?pay=cancel";
    }

    @GetMapping("/kakao/fail")
    public String kakaoFail() {
        return "redirect:/market/shop?pay=fail";
    }
}