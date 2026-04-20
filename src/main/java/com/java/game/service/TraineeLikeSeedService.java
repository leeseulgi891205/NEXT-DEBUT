package com.java.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.entity.Trainee;
import com.java.game.entity.TraineeMemberLike;
import com.java.game.repository.TraineeMemberLikeRepository;
import com.java.game.repository.TraineeRepository;

/**
 * 연습생별 누적 좋아요를 데모용으로 채움. MEMBER_MNO &lt; 0 인 행은 합성 데이터이며 실제 회원과 겹치지 않게 둔다.
 */
@Service
public class TraineeLikeSeedService {

	private static final long SYNTHETIC_RUN_ID = 0L;
	private static final int BATCH = 500;

	private final TraineeRepository traineeRepository;
	private final TraineeMemberLikeRepository likeRepository;

	public TraineeLikeSeedService(TraineeRepository traineeRepository, TraineeMemberLikeRepository likeRepository) {
		this.traineeRepository = traineeRepository;
		this.likeRepository = likeRepository;
	}

	@Transactional
	public void seedRandomSyntheticLikes() {
		likeRepository.deleteSyntheticByMemberMnoNegative();
		Random r = new Random();
		List<TraineeMemberLike> buffer = new ArrayList<>();
		for (Trainee t : traineeRepository.findAll()) {
			Long tid = t.getId();
			if (tid == null) {
				continue;
			}
			int n = 1 + r.nextInt(2000);
			for (int i = 1; i <= n; i++) {
				long mno = -(tid * 10000L + i);
				buffer.add(new TraineeMemberLike(mno, tid, SYNTHETIC_RUN_ID));
				if (buffer.size() >= BATCH) {
					likeRepository.saveAll(buffer);
					buffer.clear();
				}
			}
		}
		if (!buffer.isEmpty()) {
			likeRepository.saveAll(buffer);
		}
	}
}
