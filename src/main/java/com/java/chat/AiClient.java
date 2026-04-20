package com.java.chat;

import java.util.List;

public interface AiClient {

    String chat(List<ChatMessage> messages);
}
