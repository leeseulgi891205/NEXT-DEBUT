package com.java.game.service;

import com.java.game.entity.Gender;

public record RosterItem(
        Long traineeId,
        String name,
        Gender gender,
        String grade,
        int vocal,
        int dance,
        int star,
        int mental,
        int teamwork,
        String imagePath,
        int pickOrder,
        String personalityCode,
        Integer age,
        String statusCode,
        String statusLabel,
        String statusDesc,
        Integer statusTurnsLeft,
        /** 연습생 카드 강화 단계(0~5) */
        int enhanceLevel,
        /** 장착 포토카드 등급(R/SR/SSR), 없으면 null */
        String photoCardGrade,
        /** 포토카드 퍼센트 보너스(0,5,10,15) — 표시·글로우용 */
        int photoCardBonusPct
) {}
