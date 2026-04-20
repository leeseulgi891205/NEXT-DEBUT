package com.java.game.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.game.entity.TraineeMemberLike;
import com.java.game.repository.TraineeMemberLikeRepository;
import com.java.game.repository.TraineeRepository;

@Service
public class TraineeLikeService {

	private final TraineeMemberLikeRepository likeRepository;
	private final TraineeRepository traineeRepository;

	public TraineeLikeService(TraineeMemberLikeRepository likeRepository, TraineeRepository traineeRepository) {
		this.likeRepository = likeRepository;
		this.traineeRepository = traineeRepository;
	}

	public Map<Long, Long> countByTraineeIds(Collection<Long> traineeIds) {
		Map<Long, Long> out = new HashMap<>();
		if (traineeIds == null || traineeIds.isEmpty()) {
			return out;
		}
		for (Object[] row : likeRepository.countGroupedByTraineeId(traineeIds)) {
			if (row != null && row.length >= 2 && row[0] != null) {
				long tid = ((Number) row[0]).longValue();
				long cnt = ((Number) row[1]).longValue();
				out.put(tid, cnt);
			}
		}
		return out;
	}

	/** 도감 등: 한 번이라도 좋아요 누른 연습생 ID */
	public Set<Long> likedTraineeIdsEver(Long memberMno, Collection<Long> traineeIds) {
		if (memberMno == null || traineeIds == null || traineeIds.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(likeRepository.findTraineeIdsLikedEver(memberMno, traineeIds));
	}

	/** 엔딩 화면: 해당 런에서 이미 좋아요한 연습생 */
	public Set<Long> likedTraineeIdsInRun(Long memberMno, Collection<Long> traineeIds, Long runId) {
		if (memberMno == null || runId == null || traineeIds == null || traineeIds.isEmpty()) {
			return Set.of();
		}
		return new HashSet<>(likeRepository.findTraineeIdsLikedInRun(memberMno, runId, traineeIds));
	}

	public record LikeAddResult(boolean added, boolean alreadyLikedThisRun, long totalLikes) {
	}

	/**
	 * 런당 연습생 1회만 추가(누적). 같은 런에서 중복이면 added=false, alreadyLikedThisRun=true.
	 */
	@Transactional
	public LikeAddResult addLike(Long memberMno, Long traineeId, Long runId) {
		if (memberMno == null || traineeId == null || runId == null) {
			throw new IllegalArgumentException("memberMno, traineeId, runId required");
		}
		traineeRepository.findById(traineeId)
				.orElseThrow(() -> new IllegalArgumentException("trainee not found: " + traineeId));

		if (likeRepository.existsByMemberMnoAndTraineeIdAndRunId(memberMno, traineeId, runId)) {
			return new LikeAddResult(false, true, likeRepository.countByTraineeId(traineeId));
		}
		likeRepository.save(new TraineeMemberLike(memberMno, traineeId, runId));
		return new LikeAddResult(true, false, likeRepository.countByTraineeId(traineeId));
	}
}
