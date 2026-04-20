package com.java.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

public class ChatRoom {
    private final String roomId;
    private final String roomName;
    private final String creatorNickname;
    /** 비밀방일 때만 사용, 메모리 내 평문(게임용 실시간 채팅) */
    private final boolean secret;
    private final String password;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    /** 입장(세션 연결)한 적이 있는 닉네임 누적 (관리자 상세용) */
    private final Set<String> visitorHistory = Collections.synchronizedSet(new LinkedHashSet<>());
    private final long createdAt = System.currentTimeMillis();

    public ChatRoom(String roomId, String roomName, String creatorNickname, boolean secret, String password) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.creatorNickname = creatorNickname;
        this.secret = secret;
        this.password = password;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getCreatorNickname() {
        return creatorNickname;
    }

    public int getUserCount() {
        return sessions.size();
    }

    public Map<String, WebSocketSession> getSessions() {
        return sessions;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public boolean isSecret() {
        return secret;
    }

    /** 비밀방이 아니면 항상 true, 비밀방이면 비밀번호 일치 여부 */
    public boolean matchesPassword(String input) {
        if (!secret) {
            return true;
        }
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.equals(input != null ? input : "");
    }

    public void addSession(String nickname, WebSocketSession session) {
        recordVisitor(nickname);
        sessions.put(nickname, session);
    }

    public void recordVisitor(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            visitorHistory.add(nickname.trim());
        }
    }

    public List<String> getVisitorHistory() {
        synchronized (visitorHistory) {
            return new ArrayList<>(visitorHistory);
        }
    }

    public void removeSession(String nickname) {
        sessions.remove(nickname);
    }
}
