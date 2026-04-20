package com.java.game.repository;

import com.java.game.entity.Trainee;
import com.java.game.entity.Gender;
import com.java.game.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TraineeRepository extends JpaRepository<Trainee,Long>{
    /**
     * 등록된 전체 연습생 기준 능력치 평균(각 0~20). 게임 유저 전체와 비슷한 베이스라인으로 차트 비교용.
     */
    @Query("SELECT AVG(t.vocal), AVG(t.dance), AVG(t.star), AVG(t.mental), AVG(t.teamwork) FROM Trainee t")
    Object[] averageAbilityStats();

    List<Trainee> findByGender(Gender gender);

    List<Trainee> findByGrade(Grade grade);

    List<Trainee> findByGenderAndGrade(Gender gender, Grade grade);

    @Query("""
            SELECT t
            FROM Trainee t
            WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:gender IS NULL OR t.gender = :gender)
              AND (:grade IS NULL OR t.grade = :grade)
            """)
    List<Trainee> searchForAdmin(@Param("keyword") String keyword, @Param("gender") Gender gender, @Param("grade") Grade grade);
}
