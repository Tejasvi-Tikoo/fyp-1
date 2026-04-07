package com.wildroutes.websocket;

import com.wildroutes.model.*;
import com.wildroutes.repository.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final GroupRepository groupRepository;
    private final GroupMessageRepository groupMessageRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository,
            MessageRepository messageRepository,
            GroupRepository groupRepository,
            GroupMessageRepository groupMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.groupRepository = groupRepository;
        this.groupMessageRepository = groupMessageRepository;
    }

    @MessageMapping("/chat.send")
    public void send(ChatMessage message) {

        message.setTimestamp(Instant.now());

        // =========================
        // ✅ DIRECT MESSAGE
        // =========================
        if ("DIRECT".equalsIgnoreCase(message.getType())) {

            User sender = userRepository.findById(message.getSenderId()).orElseThrow();
            User receiver = userRepository.findById(message.getReceiverId()).orElseThrow();

            // Save to DB
            Message entity = Message.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .build();

            messageRepository.save(entity);

            // ✅ Payload for frontend
            Map<String, Object> payload = new HashMap<>();
            payload.put("senderId", sender.getId());
            payload.put("receiverId", receiver.getId());
            payload.put("content", message.getContent());
            payload.put("timestamp", message.getTimestamp());

            // ✅ Send to receiver
            messagingTemplate.convertAndSendToUser(
                    receiver.getId().toString(),
                    "/queue/messages",
                    payload);

            // ✅ Send to sender (for sync)
            messagingTemplate.convertAndSendToUser(
                    sender.getId().toString(),
                    "/queue/messages",
                    payload);

            System.out.println("DIRECT sent: " + sender.getId() + " -> " + receiver.getId());
        }

        // =========================
        // ✅ GROUP MESSAGE
        // =========================
        else if ("GROUP".equalsIgnoreCase(message.getType())) {

            Group group = groupRepository.findById(message.getGroupId()).orElseThrow();
            User sender = userRepository.findById(message.getSenderId()).orElseThrow();

            // Save to DB
            GroupMessage gm = GroupMessage.builder()
                    .group(group)
                    .sender(sender)
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .build();

            groupMessageRepository.save(gm);

            // ✅ Payload for frontend
            Map<String, Object> payload = new HashMap<>();
            payload.put("groupId", group.getId());
            payload.put("senderId", sender.getId());
            payload.put("content", message.getContent());
            payload.put("timestamp", message.getTimestamp());

            // ✅ Broadcast to group
            messagingTemplate.convertAndSend(
                    "/topic/groups/" + group.getId(),
                    payload);

            System.out.println("GROUP sent: group " + group.getId());
        }
    }

    // =========================
    // OPTIONAL: typing indicator
    // =========================
    @MessageMapping("/chat.typing")
    public void typing(ChatMessage message) {

        if (message.getReceiverId() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId().toString(),
                    "/queue/typing",
                    message);
        }

        else if (message.getGroupId() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/groups/" + message.getGroupId() + "/typing",
                    message);
        }
    }
}