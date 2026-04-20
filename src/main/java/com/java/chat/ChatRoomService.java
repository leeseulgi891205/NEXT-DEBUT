package com.java.chat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.java.entity.ChatRoomEntity;
import com.java.entity.ChatRoomLineEntity;
import com.java.entity.ChatRoomVisitorEntity;
import com.java.repository.ChatRoomEntityRepository;
import com.java.repository.ChatRoomLineRepository;
import com.java.repository.ChatRoomVisitorEntityRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class ChatRoomService {

	private static final int MAX_CHAT_HISTORY = 300;

	private final Map<String, ChatRoom> rooms = new ConcurrentHashMap<>();
	private final ObjectMapper mapper;
	private final ChatRoomEntityRepository roomEntityRepo;
	private final ChatRoomVisitorEntityRepository visitorEntityRepo;
	private final ChatRoomLineRepository lineRepo;

	public ChatRoomService(ObjectMapper mapper, ChatRoomEntityRepository roomEntityRepo,
			ChatRoomVisitorEntityRepository visitorEntityRepo, ChatRoomLineRepository lineRepo) {
		this.mapper = mapper;
		this.roomEntityRepo = roomEntityRepo;
		this.visitorEntityRepo = visitorEntityRepo;
		this.lineRepo = lineRepo;
	}

	/**
	 * 서버 기동 시 H2에서 방·방문자 복원 (WebSocket 세션은 비어 있음).
	 */
	@Transactional(readOnly = true)
	public void loadFromDatabase() {
		List<ChatRoomEntity> rows = roomEntityRepo.findAll();
		for (ChatRoomEntity row : rows) {
			ChatRoom room = new ChatRoom(row.getRoomId(), row.getRoomName(), row.getCreatorNickname(), row.isSecret(),
					row.getPassword());
			visitorEntityRepo.findByRoomIdOrderByFirstSeenAsc(row.getRoomId())
					.forEach(v -> room.recordVisitor(v.getNickname()));
			rooms.put(row.getRoomId(), room);
		}
	}

	@Transactional
	public ChatRoom createRoom(String roomName, String creatorNickname, boolean secret, String passwordPlain) {
		String roomId = UUID.randomUUID().toString().substring(0, 8);
		boolean isSecret = secret && passwordPlain != null && !passwordPlain.isBlank();
		String pwd = isSecret && passwordPlain != null ? passwordPlain.trim() : null;
		ChatRoom room = new ChatRoom(roomId, roomName, creatorNickname, isSecret, pwd);
		room.recordVisitor(creatorNickname);
		rooms.put(roomId, room);

		ChatRoomEntity ent = new ChatRoomEntity();
		ent.setRoomId(roomId);
		ent.setRoomName(roomName);
		ent.setCreatorNickname(creatorNickname);
		ent.setSecret(isSecret);
		ent.setPassword(pwd);
		ent.setCreatedAt(LocalDateTime.now());
		roomEntityRepo.save(ent);
		saveVisitorIfNew(roomId, creatorNickname);
		return room;
	}

	public ChatRoom getRoom(String roomId) {
		return rooms.get(roomId);
	}

	/** 입장 기록 DB 저장 (중복 시 스킵). WS 핸들러에서 입장 성공 후 호출 */
	@Transactional
	public void saveVisitorIfNew(String roomId, String nickname) {
		if (roomId == null || nickname == null || nickname.isBlank()) {
			return;
		}
		String n = nickname.trim();
		if (visitorEntityRepo.existsByRoomIdAndNickname(roomId, n)) {
			return;
		}
		ChatRoomVisitorEntity v = new ChatRoomVisitorEntity();
		v.setRoomId(roomId);
		v.setNickname(n);
		v.setFirstSeen(LocalDateTime.now());
		visitorEntityRepo.save(v);
	}

	@Transactional
	public void removeRoom(String roomId) {
		rooms.remove(roomId);
		lineRepo.deleteByRoomId(roomId);
		visitorEntityRepo.deleteByRoomId(roomId);
		roomEntityRepo.deleteById(roomId);
	}

	@Transactional
	public void saveChatLine(String roomId, String nickname, String rawContent) {
		if (roomId == null || nickname == null || rawContent == null) {
			return;
		}
		String nick = nickname.strip();
		if (nick.isEmpty()) {
			return;
		}
		String c = rawContent.trim();
		if (c.isEmpty()) {
			return;
		}
		if (c.length() > 2000) {
			c = c.substring(0, 2000);
		}
		ChatRoomLineEntity line = new ChatRoomLineEntity();
		line.setRoomId(roomId);
		line.setNickname(nick);
		line.setContent(c);
		line.setSentAt(LocalDateTime.now());
		lineRepo.save(line);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRecentChatHistory(String roomId, int maxLines) {
		if (roomId == null || maxLines <= 0) {
			return List.of();
		}
		int n = Math.min(maxLines, MAX_CHAT_HISTORY);
		Pageable p = PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "sentAt"));
		Page<ChatRoomLineEntity> page = lineRepo.findByRoomIdOrderBySentAtDesc(roomId, p);
		List<ChatRoomLineEntity> rows = new ArrayList<>(page.getContent());
		Collections.reverse(rows);
		DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
		List<Map<String, Object>> out = new ArrayList<>();
		for (ChatRoomLineEntity e : rows) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("type", "CHAT");
			m.put("nickname", e.getNickname());
			m.put("content", e.getContent());
			m.put("time", e.getSentAt() != null ? tf.format(e.getSentAt()) : "");
			out.add(m);
		}
		return out;
	}

	public Collection<ChatRoom> getAllRooms() {
		return rooms.values();
	}

	public void removeRoomIfEmpty(String roomId) {
		ChatRoom room = rooms.get(roomId);
		if (room != null && room.getUserCount() == 0) {
			rooms.remove(roomId);
		}
	}

	public void broadcast(ChatRoom room, Map<String, Object> message) {
		try {
			String json = mapper.writeValueAsString(message);
			for (WebSocketSession session : room.getSessions().values()) {
				if (session.isOpen()) {
					session.sendMessage(new TextMessage(json));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendTo(WebSocketSession session, Map<String, Object> message) {
		try {
			String json = mapper.writeValueAsString(message);
			if (session.isOpen()) {
				session.sendMessage(new TextMessage(json));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Map<String, Object>> getRoomListJson() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ChatRoom room : rooms.values()) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("roomId", room.getRoomId());
			m.put("roomName", room.getRoomName());
			m.put("creatorNickname", room.getCreatorNickname());
			m.put("userCount", room.getUserCount());
			m.put("secret", room.isSecret());
			list.add(m);
		}
		return list;
	}

	public List<Map<String, Object>> getRoomsAdminDetail() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ChatRoom room : rooms.values()) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("roomId", room.getRoomId());
			m.put("roomName", room.getRoomName());
			m.put("creatorNickname", room.getCreatorNickname());
			m.put("secret", room.isSecret());
			m.put("userCount", room.getUserCount());
			m.put("users", new ArrayList<>(room.getSessions().keySet()));
			m.put("visitorHistory", room.getVisitorHistory());
			list.add(m);
		}
		return list;
	}
}
