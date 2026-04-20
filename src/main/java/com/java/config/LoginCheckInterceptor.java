package com.java.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            return true;
        }

        String uri = request.getRequestURI();
        String qs = request.getQueryString();
        String redirect = uri + (qs == null ? "" : "?" + qs);
        response.sendRedirect(request.getContextPath() + "/login?redirect=" + java.net.URLEncoder.encode(redirect, java.nio.charset.StandardCharsets.UTF_8));
        return false;
    }
}
