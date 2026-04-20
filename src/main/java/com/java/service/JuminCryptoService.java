package com.java.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JuminCryptoService {
    private static final String PREFIX = "ENC:";
    private static final int GCM_TAG_BITS = 128;
    private static final int NONCE_BYTES = 12;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public JuminCryptoService(@Value("${app.security.jumin-key:projectx-local-jumin-key-change-me}") String rawKey) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] key = sha256.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            this.keySpec = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("주민번호 암호화 키 초기화 실패", e);
        }
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isBlank()) {
            return "";
        }
        if (plain.startsWith(PREFIX)) {
            return plain;
        }
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[nonce.length + encrypted.length];
            System.arraycopy(nonce, 0, payload, 0, nonce.length);
            System.arraycopy(encrypted, 0, payload, nonce.length, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("주민번호 암호화 실패", e);
        }
    }

    public String decrypt(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (!value.startsWith(PREFIX)) {
            // 기존 평문 데이터와 호환
            return value;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (payload.length <= NONCE_BYTES) {
                return "";
            }
            byte[] nonce = new byte[NONCE_BYTES];
            byte[] encrypted = new byte[payload.length - NONCE_BYTES];
            System.arraycopy(payload, 0, nonce, 0, NONCE_BYTES);
            System.arraycopy(payload, NONCE_BYTES, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    public String mask(String plain) {
        if (plain == null || plain.isBlank()) {
            return "";
        }
        String digits = plain.replaceAll("[^0-9]", "");
        if (digits.length() != 13) {
            return plain;
        }
        return digits.substring(0, 6) + "-*******";
    }
}
