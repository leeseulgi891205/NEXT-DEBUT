package com.java.game.service;

/**
 * 채팅 UI용 한 줄 대사 (연습생별).
 */
public record IdolChatLine(Long traineeId, String name, String personalityLabel, String text) {
}
