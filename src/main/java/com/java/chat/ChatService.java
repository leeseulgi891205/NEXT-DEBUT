package com.java.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final AiClient aiClient;

    private final Map<String, List<ChatMessage>> sessions = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT =
            "당신은 NEXT DEBUT 아이돌 육성 시뮬레이션 사이트의 친절한 안내 AI입니다. 한국어로 짧고 명확하게 답해주세요.";

    public ChatService(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    public String ask(String sessionId, String userMessage) {
        List<ChatMessage> history = sessions.computeIfAbsent(sessionId, k -> {
            List<ChatMessage> list = new ArrayList<>();
            list.add(new ChatMessage("system", SYSTEM_PROMPT));
            return list;
        });

        history.add(new ChatMessage("user", userMessage));

        if (history.size() > 21) {
            history.subList(1, history.size() - 20).clear();
        }

        String reply = aiClient.chat(history);
        history.add(new ChatMessage("assistant", reply));
        return reply;
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
