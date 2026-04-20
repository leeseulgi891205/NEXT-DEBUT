package com.java.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.java.chat.ChatRoomWsHandler;
import com.java.chat.ChatWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final ChatRoomWsHandler chatRoomHandler;

    public WebSocketConfig(ChatWebSocketHandler chatHandler, ChatRoomWsHandler chatRoomHandler) {
        this.chatHandler = chatHandler;
        this.chatRoomHandler = chatRoomHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat").setAllowedOriginPatterns("*");
        registry.addHandler(chatRoomHandler, "/ws/chatroom").setAllowedOriginPatterns("*");
    }
}
