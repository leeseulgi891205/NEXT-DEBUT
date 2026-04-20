package com.java.game.repository;

import com.java.game.entity.GameRunMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRunMemberRepository extends JpaRepository<GameRunMember, Long> {

    void deleteByRunRunId(Long runId);
    void deleteByTrainee_Id(Long traineeId);

    @Query(
        "select m " +
        "from GameRunMember m " +
        "join fetch m.trainee t " +
        "where m.run.runId = :runId " +
        "order by m.pickOrder asc"
    )
    List<GameRunMember> findRoster(@Param("runId") Long runId);
}
