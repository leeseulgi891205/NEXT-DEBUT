package com.java.chat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin/chatroom")
public class AdminChatRoomController {

    private final ChatRoomService roomService;
    private final ChatRoomWsHandler chatRoomWsHandler;
    private final ChatModerationService moderationService;
    private final ChatRoomKeywordService keywordService;

    public AdminChatRoomController(ChatRoomService roomService, ChatRoomWsHandler chatRoomWsHandler,
            ChatModerationService moderationService, ChatRoomKeywordService keywordService) {
        this.roomService = roomService;
        this.chatRoomWsHandler = chatRoomWsHandler;
        this.moderationService = moderationService;
        this.keywordService = keywordService;
    }

    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> data() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rooms", roomService.getRoomsAdminDetail());
        m.put("flags", moderationService.getRecent());
        return m;
    }

    @GetMapping("/{roomId}/detail")
    @ResponseBody
    public Map<String, Object> roomDetail(@PathVariable("roomId") String roomId) {
        ChatRoom room = roomService.getRoom(roomId);
        Map<String, Object> out = new LinkedHashMap<>();
        if (room == null) {
            out.put("error", "NOT_FOUND");
            return out;
        }
        out.put("roomId", room.getRoomId());
        out.put("roomName", room.getRoomName());
        out.put("creatorNickname", room.getCreatorNickname());
        out.put("secret", room.isSecret());
        out.put("users", room.getSessions().keySet());
        out.put("visitorHistory", room.getVisitorHistory());
        return out;
    }

    @PostMapping("/{roomId}/delete")
    public String delete(@PathVariable("roomId") String roomId) {
        chatRoomWsHandler.deleteRoomByAdmin(roomId);
        return "redirect:/admin#chatroom-admin";
    }

    @PostMapping("/{roomId}/kick")
    public String kick(@PathVariable("roomId") String roomId, @RequestParam("nickname") String nickname) {
        chatRoomWsHandler.kickFromAdmin(roomId, nickname);
        return "redirect:/admin#chatroom-admin";
    }

    @GetMapping("/keywords/data")
    @ResponseBody
    public Map<String, Object> keywordsData() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("builtIn", keywordService.getBuiltInKeywords());
        m.put("custom", keywordService.listCustomKeywords());
        return m;
    }

    @PostMapping("/keywords/add")
    @ResponseBody
    public Map<String, Object> keywordsAdd(@RequestParam("keyword") String keyword) {
        String r = keywordService.addKeyword(keyword);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("result", r);
        return out;
    }

    @PostMapping("/keywords/{id}/delete")
    @ResponseBody
    public Map<String, Object> keywordsDelete(@PathVariable("id") Long id) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", keywordService.deleteKeyword(id));
        return out;
    }
}
