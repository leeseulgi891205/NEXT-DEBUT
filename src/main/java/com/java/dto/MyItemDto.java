package com.java.dto;

import java.io.Serializable;

public class MyItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long memberId;
    private String itemName;
    private int quantity;
    private String itemEffect;
    private String imagePath;
    private int totalDays;
    private int elapsedDays;
    private int remainingDays;
    private int progressPercent;
    private boolean expired;
    /** 게임 내 적용한 일차(DAY N). 세션에만 저장 */
    private int appliedOnGameDay;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getItemEffect() {
        return itemEffect;
    }

    public void setItemEffect(String itemEffect) {
        this.itemEffect = itemEffect;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(int totalDays) {
        this.totalDays = totalDays;
    }

    public int getElapsedDays() {
        return elapsedDays;
    }

    public void setElapsedDays(int elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    public int getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(int remainingDays) {
        this.remainingDays = remainingDays;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public int getAppliedOnGameDay() {
        return appliedOnGameDay;
    }

    public void setAppliedOnGameDay(int appliedOnGameDay) {
        this.appliedOnGameDay = appliedOnGameDay;
    }
}

