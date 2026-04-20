package com.java.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.Member;
import com.java.entity.MyTrainee;
import com.java.game.entity.Trainee;
import com.java.game.repository.TraineeRepository;
import com.java.repository.MemberRepository;
import com.java.repository.MyTraineeRepository;

/** 회원별 시작 그룹(라이즈/하츠투하츠) 연습생 기본 지급. */
@Service
public class StarterTraineeGrantService {

	private final TraineeRepository traineeRepository;
	private final MyTraineeRepository myTraineeRepository;
	private final MemberRepository memberRepository;
	private final TraineeGroupService traineeGroupService;

	public StarterTraineeGrantService(TraineeRepository traineeRepository, MyTraineeRepository myTraineeRepository,
			MemberRepository memberRepository, TraineeGroupService traineeGroupService) {
		this.traineeRepository = traineeRepository;
		this.myTraineeRepository = myTraineeRepository;
		this.memberRepository = memberRepository;
		this.traineeGroupService = traineeGroupService;
	}

	@Transactional
	public void grantStarterGroupsForMember(Long memberId) {
		if (memberId == null) {
			return;
		}
		List<Trainee> all = traineeRepository.findAll();
		for (Trainee t : all) {
			Long tid = t.getId();
			if (tid == null) {
				continue;
			}
			String groupCode = traineeGroupService.resolveTraineeGroup(t.getName());
			if (!TraineeGroupService.GROUP_RIIZE.equals(groupCode)
					&& !TraineeGroupService.GROUP_HEARTS2HEARTS.equals(groupCode)) {
				continue;
			}
			if (myTraineeRepository.findByMemberIdAndTraineeId(memberId, tid).isEmpty()) {
				myTraineeRepository.save(new MyTrainee(memberId, tid, 1));
			}
		}
	}

	/** 기존 전체 회원에 대해 시작 그룹만 채움(멱등). */
	@Transactional
	public void grantStarterGroupsForAllMembers() {
		List<Member> members = memberRepository.findAll();
		for (Member m : members) {
			if (m.getMno() != null) {
				grantStarterGroupsForMember(m.getMno());
			}
		}
	}
}
