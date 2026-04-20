package com.java.dto;

public record LoginMember(
        Long mno,
        String mid,
        String mname,
        String nickname,
        String role
) {}
