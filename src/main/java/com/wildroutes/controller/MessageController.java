package com.wildroutes.controller;

import com.wildroutes.model.GroupMessage;
import com.wildroutes.model.Message;
import com.wildroutes.repository.GroupMessageRepository;
import com.wildroutes.repository.MessageRepository;
import com.wildroutes.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final GroupMessageRepository groupMessageRepository;

    public MessageController(MessageRepository messageRepository,
                             GroupMessageRepository groupMessageRepository) {
        this.messageRepository = messageRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @GetMapping("/direct/{peerId}")
    public List<Map<String, Object>> directHistory(
            @AuthenticationPrincipal CustomUserDetails current,
            @PathVariable Long peerId) {

        Long me = current.getId();

        // Fetch messages in both directions
        List<Message> sent = messageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(me, peerId);
        List<Message> received = messageRepository.findByReceiverIdAndSenderIdOrderByTimestampAsc(me, peerId);

        List<Message> all = new ArrayList<>();
        all.addAll(sent);
        all.addAll(received);
        all.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

        // Convert to DTO (Map)
        List<Map<String, Object>> dto = new ArrayList<>();
        for (Message m : all) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSender().getId());
            map.put("senderUsername", m.getSender().getUsername());
            map.put("receiverId", m.getReceiver().getId());
            map.put("receiverUsername", m.getReceiver().getUsername());
            map.put("content", m.getContent());
            map.put("timestamp", m.getTimestamp());
            dto.add(map);
        }

        return dto;
    }

    @GetMapping("/groups/{groupId}")
    public List<Map<String, Object>> groupHistory(@PathVariable Long groupId) {

        List<GroupMessage> messages =
                groupMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (GroupMessage gm : messages) {
            Map<String, Object> m = new HashMap<>();
            m.put("groupId", gm.getGroup().getId());
            m.put("senderId", gm.getSender().getId());
            m.put("content", gm.getContent());
            m.put("timestamp", gm.getTimestamp());

            result.add(m);
        }

        return result;
    }
}

