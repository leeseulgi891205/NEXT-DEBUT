package com.java.game.service;

import java.util.List;

public record GameRunResult(Long runId, String groupType, List<RosterItem> roster, boolean confirmed, String phase // 현재
																													// 진행
																													// 단계
																													// (WEEK1~WEEK6 등)
) {
}
