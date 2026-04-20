package com.java.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.Member;
import com.java.photocard.repository.EquippedPhotoCardRepository;
import com.java.photocard.repository.UserPhotoCardRepository;
import com.java.repository.MemberRepository;
import com.java.repository.MyTraineeRepository;

@Service
public class MemberProgressMigrationService {

	private static final int TARGET_PROGRESS_VERSION = 2;

	private final MemberRepository memberRepository;
	private final MyTraineeRepository myTraineeRepository;
	private final UserPhotoCardRepository userPhotoCardRepository;
	private final EquippedPhotoCardRepository equippedPhotoCardRepository;
	private final StarterTraineeGrantService starterTraineeGrantService;

	public MemberProgressMigrationService(MemberRepository memberRepository,
			MyTraineeRepository myTraineeRepository,
			UserPhotoCardRepository userPhotoCardRepository,
			EquippedPhotoCardRepository equippedPhotoCardRepository,
			StarterTraineeGrantService starterTraineeGrantService) {
		this.memberRepository = memberRepository;
		this.myTraineeRepository = myTraineeRepository;
		this.userPhotoCardRepository = userPhotoCardRepository;
		this.equippedPhotoCardRepository = equippedPhotoCardRepository;
		this.starterTraineeGrantService = starterTraineeGrantService;
	}

	@Transactional
	public void migrateToV2IfNeeded() {
		List<Member> targets = memberRepository.findByProgressVersionLessThan(TARGET_PROGRESS_VERSION);
		for (Member member : targets) {
			Long mno = member.getMno();
			if (mno == null) {
				continue;
			}
			equippedPhotoCardRepository.deleteByMemberId(mno);
			userPhotoCardRepository.deleteByMemberId(mno);
			myTraineeRepository.deleteByMemberId(mno);
			member.setGroupUnlockMask(TraineeGroupService.DEFAULT_UNLOCK_MASK);
			member.setProgressVersion(TARGET_PROGRESS_VERSION);
			memberRepository.save(member);
			starterTraineeGrantService.grantStarterGroupsForMember(mno);
		}
	}
}
