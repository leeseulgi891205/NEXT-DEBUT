package com.java.game.service;

import com.java.game.entity.Gender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 그룹 케미스트리(시너지) 분석 서비스
 */
@Service
public class ChemistryService {

    private static final int MEMBER_COUNT = 4;
    private static final int HIGH_STAT = 14;
    private static final int TOP_STAT = 15;
    private static final int SOLID_STAT = 12;
    private static final int BALANCED_STAT = 10;
    private static final int ACE_TOTAL = 58;

    public ChemistryResult analyze(List<RosterItem> roster) {
        if (roster == null || roster.isEmpty()) {
            return empty();
        }

        double avgVocal = avg(roster, RosterItem::vocal);
        double avgDance = avg(roster, RosterItem::dance);
        double avgStar = avg(roster, RosterItem::star);
        double avgMental = avg(roster, RosterItem::mental);
        double avgTeamwork = avg(roster, RosterItem::teamwork);
        double minStat = Math.min(Math.min(Math.min(Math.min(avgVocal, avgDance), avgStar), avgMental), avgTeamwork);

        long maleCount = roster.stream().filter(r -> r.gender() == Gender.MALE).count();
        long femaleCount = roster.stream().filter(r -> r.gender() == Gender.FEMALE).count();
        long highVocalCount = countAtLeast(roster, RosterItem::vocal, TOP_STAT);
        long highDanceCount = countAtLeast(roster, RosterItem::dance, TOP_STAT);
        long highMentalCount = countAtLeast(roster, RosterItem::mental, HIGH_STAT);
        long highTeamworkCount = countAtLeast(roster, RosterItem::teamwork, HIGH_STAT);
        long highStarCount = countAtLeast(roster, RosterItem::star, HIGH_STAT);
        long aceCount = roster.stream().filter(item -> totalScore(item) >= ACE_TOTAL).count();

        List<ChemistryResult.Synergy> synergies = new ArrayList<>();

        if (avgVocal >= HIGH_STAT) {
            synergies.add(new ChemistryResult.Synergy(
                "하모니 라인",
                String.format("보컬 평균 %.1f — 팀 보컬 안정감이 높습니다.", avgVocal),
                "🎤", "vocal", 4,
                topMembers(roster, RosterItem::vocal, 2)
            ));
        }

        if (avgDance >= HIGH_STAT) {
            synergies.add(new ChemistryResult.Synergy(
                "퍼포먼스 라인",
                String.format("댄스 평균 %.1f — 퍼포먼스 합이 강합니다.", avgDance),
                "✦", "dance", 4,
                topMembers(roster, RosterItem::dance, 2)
            ));
        }

        if (avgMental >= HIGH_STAT && avgTeamwork >= HIGH_STAT) {
            synergies.add(new ChemistryResult.Synergy(
                "안정된 팀워크",
                String.format("멘탈 %.1f / 팀워크 %.1f — 흔들림이 적은 조합입니다.", avgMental, avgTeamwork),
                "🛡", "teamwork", 6,
                uniqueNames(topMembers(roster, RosterItem::mental, 2), topMembers(roster, RosterItem::teamwork, 2))
            ));
        }

        if (maleCount == 2 && femaleCount == 2) {
            synergies.add(new ChemistryResult.Synergy(
                "완벽한 조화",
                "남녀 2:2 밸런스 — 혼성 무대 시너지가 크게 살아납니다.",
                "✧", "star", 6,
                allNames(roster)
            ));
        } else if (maleCount == MEMBER_COUNT || femaleCount == MEMBER_COUNT) {
            synergies.add(new ChemistryResult.Synergy(
                "동일 성별 결속",
                "동일 성별 4인 조합 — 팀 호흡이 빠르게 맞춰집니다.",
                "◆", "teamwork", 6,
                allNames(roster)
            ));
        }

        if (highVocalCount >= 2) {
            synergies.add(new ChemistryResult.Synergy(
                "꿀보이스 조합",
                String.format("고보컬 멤버 %d명 — 파트 분배가 안정적입니다.", highVocalCount),
                "♫", "vocal", 4,
                membersAtLeast(roster, RosterItem::vocal, TOP_STAT)
            ));
        }

        if (highDanceCount >= 2) {
            synergies.add(new ChemistryResult.Synergy(
                "칼군무 라인",
                String.format("고댄스 멤버 %d명 — 퍼포먼스 완성도가 올라갑니다.", highDanceCount),
                "⬢", "dance", 4,
                membersAtLeast(roster, RosterItem::dance, TOP_STAT)
            ));
        }

        Integer sameAge = mostCommonAge(roster);
        if (sameAge != null) {
            synergies.add(new ChemistryResult.Synergy(
                "친구 사이",
                String.format("동갑 멤버 포함 (%d세) — 친밀도와 멘탈 케어가 좋아집니다.", sameAge),
                "♥", "mental", 3,
                membersByAge(roster, sameAge)
            ));
        }

        if (highMentalCount >= 2 && highTeamworkCount >= 2) {
            synergies.add(new ChemistryResult.Synergy(
                "분위기 메이커",
                "멘탈과 팀워크 중심 멤버가 팀 텐션을 안정시킵니다.",
                "☀", "mental", 4,
                uniqueNames(membersAtLeast(roster, RosterItem::mental, HIGH_STAT), membersAtLeast(roster, RosterItem::teamwork, HIGH_STAT))
            ));
        }

        if (highStarCount >= 3) {
            synergies.add(new ChemistryResult.Synergy(
                "무대 장악",
                String.format("스타성 높은 멤버 %d명 — 시선 집중 효과가 매우 큽니다.", highStarCount),
                "★", "star", 6,
                membersAtLeast(roster, RosterItem::star, HIGH_STAT)
            ));
        } else if (highStarCount >= 2) {
            synergies.add(new ChemistryResult.Synergy(
                "시선 캐치",
                String.format("스타성 높은 멤버 %d명 — 무대 집중도가 올라갑니다.", highStarCount),
                "✷", "star", 3,
                membersAtLeast(roster, RosterItem::star, HIGH_STAT)
            ));
        }

        if (avgVocal >= SOLID_STAT && avgDance >= SOLID_STAT && avgStar >= SOLID_STAT) {
            synergies.add(new ChemistryResult.Synergy(
                "스타 포지션 밸런스",
                String.format("보컬 %.1f / 댄스 %.1f / 스타 %.1f — 무대 핵심 축이 고르게 강합니다.", avgVocal, avgDance, avgStar),
                "◉", "all", 4,
                uniqueNames(topMembers(roster, RosterItem::vocal, 2), topMembers(roster, RosterItem::dance, 2), topMembers(roster, RosterItem::star, 2))
            ));
        }

        if (highMentalCount >= 3) {
            synergies.add(new ChemistryResult.Synergy(
                "멘탈 버팀목",
                String.format("고멘탈 멤버 %d명 — 장기전에서 흔들림이 적습니다.", highMentalCount),
                "☘", "mental", 3,
                membersAtLeast(roster, RosterItem::mental, HIGH_STAT)
            ));
        }

        if (highTeamworkCount >= 3) {
            synergies.add(new ChemistryResult.Synergy(
                "팀워크 코어",
                String.format("고팀워크 멤버 %d명 — 협업 완성도가 높습니다.", highTeamworkCount),
                "⛶", "teamwork", 3,
                membersAtLeast(roster, RosterItem::teamwork, HIGH_STAT)
            ));
        }

        if (aceCount >= 2) {
            synergies.add(new ChemistryResult.Synergy(
                "에이스 듀오",
                String.format("총합 높은 멤버 %d명 — 팀의 중심축이 단단합니다.", aceCount),
                "✪", "all", 6,
                roster.stream().filter(item -> totalScore(item) >= ACE_TOTAL).map(RosterItem::name).toList()
            ));
        }

        if (minStat >= BALANCED_STAT) {
            synergies.add(new ChemistryResult.Synergy(
                "올라운더 밸런스",
                "전 스탯 평균이 무너지지 않아 전체 밸런스가 안정적입니다.",
                "◈", "all", 3,
                allNames(roster)
            ));
        }

        List<ChemistryResult.Synergy> selected = synergies.stream()
            .sorted(Comparator.comparingInt(ChemistryResult.Synergy::getBonusPct).reversed()
                .thenComparing(ChemistryResult.Synergy::getName))
            .limit(4)
            .collect(Collectors.toCollection(ArrayList::new));

        List<ChemistryResult.Synergy> halvedSelected = selected.stream()
            .map(s -> new ChemistryResult.Synergy(
                s.getName(),
                s.getDescription(),
                s.getIcon(),
                s.getType(),
                s.getBonusPct() / 2,
                s.getInvolvedMembers()))
            .collect(Collectors.toCollection(ArrayList::new));

        int synergyCount = halvedSelected.size();
        int baseBonus = halvedSelected.stream().mapToInt(ChemistryResult.Synergy::getBonusPct).sum();
        int gradeBonus = gradeBonus(synergyCount) / 2;
        int totalBonus = baseBonus + gradeBonus;

        String chemGrade;
        String chemLabel;
        if (synergyCount >= 4) {
            chemGrade = "S";
            chemLabel = "환상의 케미";
        } else if (synergyCount == 3) {
            chemGrade = "A";
            chemLabel = "찰떡 케미";
        } else if (synergyCount == 2) {
            chemGrade = "B";
            chemLabel = "좋은 케미";
        } else if (synergyCount == 1) {
            chemGrade = "C";
            chemLabel = "가능성 있는 조합";
        } else {
            chemGrade = "D";
            chemLabel = "케미 없음";
        }

        return new ChemistryResult(halvedSelected, baseBonus, gradeBonus, totalBonus, chemGrade, chemLabel);
    }

    private int gradeBonus(int synergyCount) {
        if (synergyCount >= 4) return 10;
        if (synergyCount == 3) return 5;
        if (synergyCount == 2) return 3;
        if (synergyCount == 1) return 1;
        return 0;
    }

    private int totalScore(RosterItem item) {
        return item.vocal() + item.dance() + item.star() + item.mental() + item.teamwork();
    }

    private double avg(List<RosterItem> roster, ToIntFunction<RosterItem> getter) {
        return roster.stream().mapToInt(getter).average().orElse(0);
    }

    private long countAtLeast(List<RosterItem> roster, ToIntFunction<RosterItem> getter, int threshold) {
        return roster.stream().filter(item -> getter.applyAsInt(item) >= threshold).count();
    }

    private Integer mostCommonAge(List<RosterItem> roster) {
        Map<Integer, Long> ageCount = roster.stream()
            .map(RosterItem::age)
            .filter(age -> age != null && age > 0)
            .collect(Collectors.groupingBy(age -> age, Collectors.counting()));

        return ageCount.entrySet().stream()
            .filter(entry -> entry.getValue() >= 2)
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    }

    private List<String> topMembers(List<RosterItem> roster, ToIntFunction<RosterItem> getter, int limit) {
        return roster.stream()
            .sorted(Comparator.comparingInt((RosterItem item) -> getter.applyAsInt(item)).reversed())
            .limit(limit)
            .map(RosterItem::name)
            .toList();
    }

    private List<String> membersAtLeast(List<RosterItem> roster, ToIntFunction<RosterItem> getter, int threshold) {
        return roster.stream()
            .filter(item -> getter.applyAsInt(item) >= threshold)
            .sorted(Comparator.comparingInt((RosterItem item) -> getter.applyAsInt(item)).reversed())
            .map(RosterItem::name)
            .toList();
    }

    private List<String> membersByAge(List<RosterItem> roster, int age) {
        return roster.stream()
            .filter(item -> item.age() != null && item.age() == age)
            .map(RosterItem::name)
            .toList();
    }

    @SafeVarargs
    private List<String> uniqueNames(List<String>... groups) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (List<String> group : groups) {
            if (group != null) names.addAll(group);
        }
        return new ArrayList<>(names);
    }

    private List<String> allNames(List<RosterItem> roster) {
        return roster.stream().map(RosterItem::name).toList();
    }

    public ChemistryResult empty() {
        return new ChemistryResult(List.of(), 0, 0, 0, "D", "케미 없음");
    }
}
