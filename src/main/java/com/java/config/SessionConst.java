package com.java.config;

public final class SessionConst {
    private SessionConst() {}

    public static final String LOGIN_MEMBER = "LOGIN_MEMBER";

    /**
     * /game/run/ranking 진입 시 리다이렉트 대상 runId(Long). 랭킹 페이지에서 일치할 때만
     * 「메인에서 온」 버튼으로 인정하고 제거한다.
     */
    public static final String RANKING_HUB_PENDING_RUN_ID = "RANKING_HUB_PENDING_RUN_ID";
}
