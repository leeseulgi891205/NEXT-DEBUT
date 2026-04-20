package com.java.chat;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * H2에 저장된 채팅방 메타·방문 기록을 기동 시 메모리(ChatRoom)로 복원.
 */
@Component
@Order(50)
public class ChatRoomPersistenceBootstrap implements ApplicationRunner {

	private final ChatRoomService chatRoomService;

	public ChatRoomPersistenceBootstrap(ChatRoomService chatRoomService) {
		this.chatRoomService = chatRoomService;
	}

	@Override
	public void run(ApplicationArguments args) {
		chatRoomService.loadFromDatabase();
	}
}
