package com.java.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.entity.ChatModerationLogEntity;
import com.java.repository.ChatModerationLogRepository;

/**
 * 부적절/스팸 의심 메시지 — DB(H2)에 저장해 서버 재시작 후에도 관리자 화면에 남김.
 */
@Service
public class ChatModerationService {

	private final ChatModerationLogRepository logRepo;

	public ChatModerationService(ChatModerationLogRepository logRepo) {
		this.logRepo = logRepo;
	}

	@Transactional
	public void flag(String roomId, String roomName, String nickname, String content, String reason) {
		ChatModerationLogEntity e = new ChatModerationLogEntity();
		e.setRoomId(roomId != null ? roomId : "");
		e.setRoomName(roomName != null ? roomName : "");
		e.setNickname(nickname != null ? nickname : "");
		e.setContent(content != null ? content : "");
		e.setReason(reason != null ? reason : "FILTER");
		e.setCreatedAt(LocalDateTime.now());
		logRepo.save(e);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRecent() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ChatModerationLogEntity e : logRepo.findTop200ByOrderByIdDesc()) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("at", e.getCreatedAt() != null ? e.getCreatedAt().toString() : "");
			m.put("roomId", e.getRoomId());
			m.put("roomName", e.getRoomName());
			m.put("nickname", e.getNickname());
			m.put("content", e.getContent());
			m.put("reason", e.getReason());
			list.add(m);
		}
		return list;
	}
}
