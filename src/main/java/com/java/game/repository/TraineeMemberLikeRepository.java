package com.java.game.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.game.entity.TraineeMemberLike;

public interface TraineeMemberLikeRepository extends JpaRepository<TraineeMemberLike, Long> {

	/** 데모/시드용 합성 좋아요(MEMBER_MNO &lt; 0)만 삭제 */
	@Modifying
	@Query("DELETE FROM TraineeMemberLike l WHERE l.memberMno < 0")
	int deleteSyntheticByMemberMnoNegative();

	long countByTraineeId(Long traineeId);

	boolean existsByMemberMnoAndTraineeIdAndRunId(Long memberMno, Long traineeId, Long runId);

	void deleteByTraineeId(Long traineeId);

	@Query("SELECT l.traineeId, COUNT(l) FROM TraineeMemberLike l WHERE l.traineeId IN :ids GROUP BY l.traineeId")
	List<Object[]> countGroupedByTraineeId(@Param("ids") Collection<Long> ids);

	@Query("SELECT DISTINCT l.traineeId FROM TraineeMemberLike l WHERE l.memberMno = :mno AND l.traineeId IN :ids")
	List<Long> findTraineeIdsLikedEver(@Param("mno") Long mno, @Param("ids") Collection<Long> ids);

	@Query("SELECT l.traineeId FROM TraineeMemberLike l WHERE l.memberMno = :mno AND l.runId = :runId AND l.traineeId IN :ids")
	List<Long> findTraineeIdsLikedInRun(@Param("mno") Long mno, @Param("runId") Long runId,
			@Param("ids") Collection<Long> ids);
}
