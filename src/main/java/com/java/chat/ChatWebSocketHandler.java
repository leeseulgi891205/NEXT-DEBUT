package com.java.chat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;

    public ChatWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userMsg = message.getPayload().trim();
        if (userMsg.isBlank()) {
            return;
        }

        if ("__clear__".equals(userMsg)) {
            chatService.clearSession(session.getId());
            session.sendMessage(new TextMessage("__cleared__"));
            return;
        }

        String reply = chatService.ask(session.getId(), userMsg);
        session.sendMessage(new TextMessage(reply));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        chatService.clearSession(session.getId());
    }
}
