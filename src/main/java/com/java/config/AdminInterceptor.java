package com.java.config;

import com.java.dto.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        LoginMember lm = session == null ? null : (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (lm == null) {
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            String redirect = uri + (qs == null ? "" : "?" + qs);
            response.sendRedirect(request.getContextPath() + "/login?redirect=" + java.net.URLEncoder.encode(redirect, java.nio.charset.StandardCharsets.UTF_8));
            return false;
        }
        String role = lm.role();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            response.setStatus(403);
            response.sendRedirect(request.getContextPath() + "/main?err=forbidden");
            return false;
        }
        return true;
    }
}

