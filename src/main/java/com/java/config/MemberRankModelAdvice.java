package com.java.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.java.dto.LoginMember;
import com.java.entity.MemberRank;
import com.java.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;

/**
 * 로그인 사용자의 회원 등급을 모든 뷰 모델에 주입 (상단 네비 표시용).
 */
@ControllerAdvice
public class MemberRankModelAdvice {

	private final MemberRepository memberRepository;

	public MemberRankModelAdvice(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	@ModelAttribute
	public void addMemberRankForNav(HttpSession session, org.springframework.ui.Model model) {
		if (session == null) {
			return;
		}
		Object raw = session.getAttribute(SessionConst.LOGIN_MEMBER);
		if (!(raw instanceof LoginMember lm) || lm.mno() == null) {
			return;
		}
		memberRepository.findById(lm.mno()).ifPresent(m -> {
			MemberRank rank = MemberRank.getRankByExp(m.getRankExp());
			MemberRank.NextTierProgress prog = MemberRank.nextTierProgress(m.getRankExp());
			model.addAttribute("memberRankCode", rank.name());
			model.addAttribute("memberRankLabel", rank.displayName());
			model.addAttribute("memberRankExp", m.getRankExp());
			model.addAttribute("memberRankBarPercent", prog.barPercent());
			model.addAttribute("memberRankExpUntilNext", prog.expUntilNext());
			model.addAttribute("memberRankMaxTier", prog.maxTier());
			model.addAttribute("memberRankNextLabel", prog.nextTierLabel());
		});
	}
}
