package com.java.service;

import org.springframework.stereotype.Service;

import com.java.config.SessionConst;
import com.java.dto.LoginMember;
import com.java.game.repository.GameRunRepository;

import jakarta.servlet.http.HttpSession;

/**
 * 메인·우측 네비 등에서 랭킹 진입 시 사용할 기본 RUN ID (최신순).
 */
@Service
public class RankingNavService {

	private final GameRunRepository gameRunRepository;

	public RankingNavService(GameRunRepository gameRunRepository) {
		this.gameRunRepository = gameRunRepository;
	}

	public Long resolveDefaultRunIdForRanking(HttpSession session) {
		Long runId = null;
		LoginMember lm = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
		try {
			if (lm != null && lm.mno() != null) {
				var myRuns = gameRunRepository.findByPlayerMnoOrderByCreatedAtDesc(lm.mno());
				if (myRuns != null && !myRuns.isEmpty()) {
					runId = myRuns.get(0).getRunId();
				}
			}
			if (runId == null) {
				var runs = gameRunRepository.findTop50ByOrderByCreatedAtDesc();
				if (runs != null && !runs.isEmpty()) {
					runId = runs.get(0).getRunId();
				}
			}
		} catch (Exception ignored) {
		}
		return runId;
	}
}
