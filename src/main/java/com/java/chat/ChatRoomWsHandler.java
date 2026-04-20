package com.java.chat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class ChatRoomWsHandler extends TextWebSocketHandler {

    private final ChatRoomService roomService;
    private final ChatModerationService moderationService;
    private final ChatRoomKeywordService keywordService;
    private final ObjectMapper mapper;

    private final Map<String, WebSocketSession> allSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionNickname = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoom = new ConcurrentHashMap<>();

    public ChatRoomWsHandler(ChatRoomService roomService, ChatModerationService moderationService,
            ChatRoomKeywordService keywordService, ObjectMapper mapper) {
        this.roomService = roomService;
        this.moderationService = moderationService;
        this.keywordService = keywordService;
        this.mapper = mapper;
    }

    /** 관리자: 방 삭제 — 접속자 전원에게 ROOM_GONE */
    public void deleteRoomByAdmin(String roomId) {
        ChatRoom room = roomService.getRoom(roomId);
        if (room == null) {
            return;
        }
        for (WebSocketSession s : new ArrayList<>(room.getSessions().values())) {
            sessionRoom.remove(s.getId());
            Map<String, Object> gone = new LinkedHashMap<>();
            gone.put("type", "ROOM_GONE");
            gone.put("roomId", roomId);
            roomService.sendTo(s, gone);
        }
        roomService.removeRoom(roomId);
        broadcastRoomList();
    }

    /** 관리자: 특정 닉네임 추방 */
    public void kickFromAdmin(String roomId, String targetNickname) {
        ChatRoom room = roomService.getRoom(roomId);
        if (room == null || targetNickname == null || targetNickname.isBlank()) {
            return;
        }
        WebSocketSession victim = room.getSessions().get(targetNickname);
        if (victim == null) {
            return;
        }
        room.removeSession(targetNickname);
        sessionRoom.remove(victim.getId());
        Map<String, Object> kicked = new LinkedHashMap<>();
        kicked.put("type", "KICKED");
        kicked.put("roomId", roomId);
        kicked.put("reason", "ADMIN");
        roomService.sendTo(victim, kicked);

        Map<String, Object> sys = new LinkedHashMap<>();
        sys.put("type", "SYSTEM");
        sys.put("content", targetNickname + "님이 관리자에 의해 추방되었습니다.");
        sys.put("userCount", room.getUserCount());
        sys.put("participants", new ArrayList<>(room.getSessions().keySet()));
        roomService.broadcast(room, sys);
        broadcastRoomList();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        allSessions.put(session.getId(), session);
        Map<String, Object> msg = mapper.readValue(message.getPayload(), new TypeReference<Map<String, Object>>() {});
        String type = (String) msg.get("type");
        String nickname = sessionNickname.getOrDefault(session.getId(), "익명");

        switch (type) {
            case "INIT" -> {
                String nick = (String) msg.get("nickname");
                sessionNickname.put(session.getId(), nick != null ? nick : "익명");
                sendRoomList(session);
            }
            case "CREATE_ROOM" -> {
                String roomName = (String) msg.get("roomName");
                if (roomName == null || roomName.isBlank()) {
                    return;
                }
                Boolean secretObj = (Boolean) msg.get("secret");
                boolean secret = Boolean.TRUE.equals(secretObj);
                String password = (String) msg.get("password");
                if (secret && (password == null || password.isBlank())) {
                    return;
                }
                roomService.createRoom(roomName.trim(), nickname, secret, password);
                broadcastRoomList();
                sendRoomList(session);
            }
            case "JOIN_ROOM" -> {
                String roomId = (String) msg.get("roomId");
                ChatRoom room = roomService.getRoom(roomId);
                if (room == null) {
                    return;
                }
                String roomPassword = (String) msg.get("password");
                if (!room.matchesPassword(roomPassword)) {
                    sendJoinDenied(session, "PASSWORD");
                    return;
                }

                leaveCurrentRoom(session, nickname);

                room.addSession(nickname, session);
                sessionRoom.put(session.getId(), roomId);
                roomService.saveVisitorIfNew(roomId, nickname);

                Map<String, Object> joinMsg = new LinkedHashMap<>();
                joinMsg.put("type", "SYSTEM");
                joinMsg.put("content", nickname + "님이 입장했습니다.");
                joinMsg.put("userCount", room.getUserCount());
                joinMsg.put("participants", new ArrayList<>(room.getSessions().keySet()));
                roomService.broadcast(room, joinMsg);

                broadcastRoomList();
                sendJoinOk(session, room);
            }
            case "SEND_MSG" -> {
                String roomId = sessionRoom.get(session.getId());
                if (roomId == null) {
                    return;
                }
                ChatRoom room = roomService.getRoom(roomId);
                if (room == null) {
                    return;
                }
                String content = String.valueOf(msg.get("content"));
                if (keywordService.shouldFlag(content)) {
                    moderationService.flag(roomId, room.getRoomName(), nickname, content, "KEYWORD");
                }

                roomService.saveChatLine(roomId, nickname, content);

                Map<String, Object> chatMsg = new LinkedHashMap<>();
                chatMsg.put("type", "CHAT");
                chatMsg.put("nickname", nickname);
                chatMsg.put("content", msg.get("content"));
                chatMsg.put("time", new java.text.SimpleDateFormat("HH:mm").format(new Date()));
                roomService.broadcast(room, chatMsg);
            }
            case "LEAVE_ROOM" -> {
                leaveCurrentRoom(session, nickname);
                sendRoomList(session);
            }
            case "KICK_USER" -> {
                String roomId = (String) msg.get("roomId");
                String target = (String) msg.get("targetNickname");
                if (roomId == null || target == null || target.isBlank()) {
                    return;
                }
                ChatRoom room = roomService.getRoom(roomId);
                if (room == null) {
                    return;
                }
                if (!nickname.equals(room.getCreatorNickname())) {
                    return;
                }
                if (nickname.equals(target)) {
                    return;
                }
                WebSocketSession victim = room.getSessions().get(target);
                if (victim == null) {
                    return;
                }
                room.removeSession(target);
                sessionRoom.remove(victim.getId());

                Map<String, Object> kicked = new LinkedHashMap<>();
                kicked.put("type", "KICKED");
                kicked.put("roomId", roomId);
                kicked.put("reason", "HOST");
                roomService.sendTo(victim, kicked);

                Map<String, Object> sys = new LinkedHashMap<>();
                sys.put("type", "SYSTEM");
                sys.put("content", target + "님이 추방되었습니다.");
                sys.put("userCount", room.getUserCount());
                sys.put("participants", new ArrayList<>(room.getSessions().keySet()));
                roomService.broadcast(room, sys);
                broadcastRoomList();
            }
            case "REJOIN_ROOM" -> {
                String roomId = (String) msg.get("roomId");
                ChatRoom room = roomService.getRoom(roomId);
                if (room == null) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("type", "ROOM_GONE");
                    err.put("roomId", roomId);
                    roomService.sendTo(session, err);
                    return;
                }
                String roomPassword = (String) msg.get("password");
                if (!room.matchesPassword(roomPassword)) {
                    sendJoinDenied(session, "PASSWORD");
                    return;
                }
                leaveCurrentRoom(session, nickname);
                room.addSession(nickname, session);
                sessionRoom.put(session.getId(), roomId);
                roomService.saveVisitorIfNew(roomId, nickname);

                Map<String, Object> rejoinMsg = new LinkedHashMap<>();
                rejoinMsg.put("type", "SYSTEM");
                rejoinMsg.put("content", "");
                rejoinMsg.put("userCount", room.getUserCount());
                rejoinMsg.put("participants", new ArrayList<>(room.getSessions().keySet()));
                roomService.sendTo(session, rejoinMsg);

                broadcastRoomList();
                sendJoinOk(session, room);
            }
            default -> {
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        allSessions.remove(session.getId());
        String nickname = sessionNickname.getOrDefault(session.getId(), "익명");
        leaveCurrentRoom(session, nickname);
        sessionNickname.remove(session.getId());
        sessionRoom.remove(session.getId());
    }

    private void leaveCurrentRoom(WebSocketSession session, String nickname) {
        String roomId = sessionRoom.get(session.getId());
        if (roomId == null) {
            return;
        }
        ChatRoom room = roomService.getRoom(roomId);
        if (room != null) {
            room.removeSession(nickname);
            sessionRoom.remove(session.getId());

            Map<String, Object> leaveMsg = new LinkedHashMap<>();
            leaveMsg.put("type", "SYSTEM");
            leaveMsg.put("content", nickname + "님이 퇴장했습니다.");
            leaveMsg.put("userCount", room.getUserCount());
            leaveMsg.put("participants", new ArrayList<>(room.getSessions().keySet()));
            roomService.broadcast(room, leaveMsg);

            broadcastRoomList();
        }
    }

    private void sendJoinDenied(WebSocketSession session, String reason) {
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("type", "JOIN_DENIED");
        err.put("reason", reason);
        roomService.sendTo(session, err);
    }

    private void sendJoinOk(WebSocketSession session, ChatRoom room) {
        Map<String, Object> ok = new LinkedHashMap<>();
        ok.put("type", "JOIN_OK");
        ok.put("roomId", room.getRoomId());
        ok.put("roomName", room.getRoomName());
        ok.put("creatorNickname", room.getCreatorNickname());
        ok.put("participants", new ArrayList<>(room.getSessions().keySet()));
        ok.put("history", roomService.getRecentChatHistory(room.getRoomId(), 300));
        roomService.sendTo(session, ok);
    }

    private void sendRoomList(WebSocketSession session) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", "ROOM_LIST");
        m.put("rooms", roomService.getRoomListJson());
        roomService.sendTo(session, m);
    }

    private void broadcastRoomList() {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("type", "ROOM_LIST");
        msg.put("rooms", roomService.getRoomListJson());
        try {
            String json = mapper.writeValueAsString(msg);
            for (String sid : sessionNickname.keySet()) {
                WebSocketSession s = allSessions.get(sid);
                if (s != null && s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
