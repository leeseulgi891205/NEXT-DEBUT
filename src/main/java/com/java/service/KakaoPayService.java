package com.java.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.java.dto.KakaoApproveResponse;
import com.java.dto.KakaoReadyResponse;

@Service
public class KakaoPayService {

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.ready-url}")
    private String readyUrl;

    @Value("${kakaopay.approve-url}")
    private String approveUrl;

    @Value("${app.base-url}")
    private String baseUrl;

    public Map<String, String> ready(Long memberId, int amount) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        String partnerOrderId = "order_" + UUID.randomUUID();
        String partnerUserId = String.valueOf(memberId);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("partner_order_id", partnerOrderId);
        body.put("partner_user_id", partnerUserId);
        body.put("item_name", amount + "코인 충전");
        body.put("quantity", 1);
        body.put("total_amount", amount);
        body.put("vat_amount", 0);
        body.put("tax_free_amount", 0);
        body.put("approval_url", baseUrl + "/kakao/success");
        body.put("cancel_url", baseUrl + "/kakao/cancel");
        body.put("fail_url", baseUrl + "/kakao/fail");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<KakaoReadyResponse> response = restTemplate.exchange(
                readyUrl,
                HttpMethod.POST,
                entity,
                KakaoReadyResponse.class
        );

        KakaoReadyResponse ready = response.getBody();

        Map<String, String> result = new HashMap<>();
        result.put("tid", ready.tid());
        result.put("partnerOrderId", partnerOrderId);
        result.put("partnerUserId", partnerUserId);
        result.put("nextRedirectPcUrl", ready.nextRedirectPcUrl());

        return result;
    }

    public KakaoApproveResponse approve(String tid, String partnerOrderId, String partnerUserId, String pgToken) {
        RestTemplate restTemplate = new RestTemplate();
    
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);
    
        Map<String, Object> body = new HashMap<>();
        body.put("cid", cid);
        body.put("tid", tid);
        body.put("partner_order_id", partnerOrderId);
        body.put("partner_user_id", partnerUserId);
        body.put("pg_token", pgToken);
    
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
    
        try {
            ResponseEntity<KakaoApproveResponse> response = restTemplate.exchange(
                    approveUrl,
                    HttpMethod.POST,
                    entity,
                    KakaoApproveResponse.class
            );
    
            System.out.println("approve status = " + response.getStatusCode());
            System.out.println("approve body = " + response.getBody());
    
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}