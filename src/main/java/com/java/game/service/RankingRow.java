package com.java.game.service;

public record RankingRow(int rank, Long runId, String playerLabel, int score, int gapFromTop, int gapFromPrev, boolean me,
		String playedAtLabel, RosterStatSums statSums, int rosterMemberCount) {}
