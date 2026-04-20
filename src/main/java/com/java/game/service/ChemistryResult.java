package com.java.game.service;

import java.util.List;

/**
 * 케미스트리 분석 결과
 */
public class ChemistryResult {

    public static class Synergy {
        private final String name;
        private final String description;
        private final String icon;
        private final String type;
        private final int bonusPct;
        private final List<String> involvedMembers;

        public Synergy(String name, String description, String icon, String type, int bonusPct, List<String> involvedMembers) {
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.type = type;
            this.bonusPct = bonusPct;
            this.involvedMembers = involvedMembers == null ? List.of() : List.copyOf(involvedMembers);
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public String getType() { return type; }
        public int getBonusPct() { return bonusPct; }
        public List<String> getInvolvedMembers() { return involvedMembers; }
        public boolean hasMembers() { return !involvedMembers.isEmpty(); }
        public String getMemberSummary() {
            return involvedMembers.isEmpty() ? "" : String.join(", ", involvedMembers);
        }
    }

    private final List<Synergy> synergies;
    private final int baseBonus;
    private final int gradeBonus;
    private final int totalBonus;
    private final String chemGrade;
    private final String chemLabel;

    public ChemistryResult(List<Synergy> synergies, int baseBonus, int gradeBonus, int totalBonus, String chemGrade, String chemLabel) {
        this.synergies = synergies == null ? List.of() : List.copyOf(synergies);
        this.baseBonus = baseBonus;
        this.gradeBonus = gradeBonus;
        this.totalBonus = totalBonus;
        this.chemGrade = chemGrade;
        this.chemLabel = chemLabel;
    }

    public List<Synergy> getSynergies() { return synergies; }
    public int getBaseBonus() { return baseBonus; }
    public int getGradeBonus() { return gradeBonus; }
    public int getTotalBonus() { return totalBonus; }
    public String getChemGrade() { return chemGrade; }
    public String getChemLabel() { return chemLabel; }
    public boolean hasSynergy() { return !synergies.isEmpty(); }
}
