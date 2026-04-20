package com.java.game.service;

import java.util.List;

/**
 * AI 출력 형식: [상황] 블록 + [대사] 멤버별 한 줄씩.
 */
public record IdolDialogueBlock(String situation, List<IdolChatLine> lines) {
}
