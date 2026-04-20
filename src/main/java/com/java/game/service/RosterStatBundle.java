package com.java.game.service;

/** 로스터 능력치 합과 인원(합산 상한 ≈ 인원×100, 인당 다섯 스탯 합 최대 100). */
public record RosterStatBundle(RosterStatSums sums, int rosterMemberCount) {
}
