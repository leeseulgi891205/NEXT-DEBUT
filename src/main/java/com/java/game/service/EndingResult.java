package com.java.game.service;

import java.util.List;

import com.java.dto.MemberRankReward;

/**
 * 데뷔 엔딩 결과 DTO
 * 게임 FINISHED 시 스탯·케미 반영 최종 점수(0~1000)로 데뷔 등급 계산
 */
public class EndingResult {

    private final Long runId;
    private final String groupType;
    private final List<RosterItem> roster;

    private final int totalScore;    // 최종 점수(만점 1000)
    private final String grade;      // S / A / B / C / D
    private final String gradeLabel; // 등급 라벨
    private final String gradeDesc;  // 등급 설명
    private final String gradeColor; // CSS 컬러

    // 멀티 엔딩 라우트
    private final String endingRoute;    // 예: WORLD_TOUR / VIRAL_HIT / RESTART / FAIL
    private final String endingTitle;    // 화면용 타이틀
    private final String endingReason;   // 왜 이 엔딩인지 한 줄 근거

    private final MemberRankReward memberRankReward;

    public EndingResult(Long runId, String groupType, List<RosterItem> roster,
                        int totalScore, String grade, String gradeLabel,
                        String gradeDesc, String gradeColor,
                        String endingRoute, String endingTitle, String endingReason,
                        MemberRankReward memberRankReward) {
        this.runId      = runId;
        this.groupType  = groupType;
        this.roster     = roster;
        this.totalScore = totalScore;
        this.grade      = grade;
        this.gradeLabel = gradeLabel;
        this.gradeDesc  = gradeDesc;
        this.gradeColor = gradeColor;
        this.endingRoute = endingRoute;
        this.endingTitle = endingTitle;
        this.endingReason = endingReason;
        this.memberRankReward = memberRankReward != null ? memberRankReward : MemberRankReward.notFinishedRun();
    }

    public Long getRunId()        { return runId; }
    public String getGroupType()  { return groupType; }
    public List<RosterItem> getRoster() { return roster; }
    public int getTotalScore()    { return totalScore; }
    public String getGrade()      { return grade; }
    public String getGradeLabel() { return gradeLabel; }
    public String getGradeDesc()  { return gradeDesc; }
    public String getGradeColor() { return gradeColor; }
    public String getEndingRoute() { return endingRoute; }
    public String getEndingTitle() { return endingTitle; }
    public String getEndingReason() { return endingReason; }
    public MemberRankReward getMemberRankReward() { return memberRankReward; }
}
