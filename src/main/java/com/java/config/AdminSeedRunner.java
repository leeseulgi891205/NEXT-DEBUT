package com.java.config;

import com.java.entity.Member;
import com.java.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 포트폴리오/과제 환경용: 기본 관리자 계정 시드.
 * - mid: admin
 * - pw : 1111 (BCrypt 해시로 저장)
 *
 * 이미 admin 계정이 있으면 role=ADMIN으로 승격하고 비밀번호를 1111로 재설정한다.
 */
@Component
@Order(1)
public class AdminSeedRunner implements CommandLineRunner {

    private final MemberRepository memberRepository;

    public AdminSeedRunner(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(String... args) {
        try {
            String mid = "admin";
            String rawPw = "1111";
            BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

            Member m = memberRepository.findByMid(mid).orElseGet(() -> {
                Member nm = new Member();
                nm.setMid(mid);
                nm.setMname("관리자");
                nm.setNickname("ADMIN");
                nm.setEmail("admin@unitx.local");
                nm.setPhone("010-0000-0000");
                nm.setAddress("ADMIN");
                nm.setAddressDetail("SYSTEM");
                nm.setJumin(null);
                return nm;
            });

            m.setMpw(enc.encode(rawPw));
            m.setRole("ADMIN");
            memberRepository.save(m);
        } catch (Exception ignored) {
            // 시드/스키마 환경 차이로 실패해도 앱 실행을 막지 않는다.
        }
    }
}

