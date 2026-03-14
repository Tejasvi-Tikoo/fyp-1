package com.wildroutes.websocket;

import com.wildroutes.model.Group;
import com.wildroutes.model.GroupMessage;
import com.wildroutes.model.Message;
import com.wildroutes.model.User;
import com.wildroutes.repository.GroupMessageRepository;
import com.wildroutes.repository.GroupRepository;
import com.wildroutes.repository.MessageRepository;
import com.wildroutes.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

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

        if ("DIRECT".equalsIgnoreCase(message.getType())) {
            User sender = userRepository.findById(message.getSenderId()).orElseThrow();
            User receiver = userRepository.findById(message.getReceiverId()).orElseThrow();
            Message entity = Message.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .build();
            messageRepository.save(entity);
            messagingTemplate.convertAndSend("/queue/messages/" + message.getReceiverId(), message);
            messagingTemplate.convertAndSend("/queue/messages/" + message.getSenderId(), message);
        } else if ("GROUP".equalsIgnoreCase(message.getType())) {
            Group group = groupRepository.findById(message.getGroupId()).orElseThrow();
            User sender = userRepository.findById(message.getSenderId()).orElseThrow();
            GroupMessage gm = GroupMessage.builder()
                    .group(group)
                    .sender(sender)
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .build();
            groupMessageRepository.save(gm);
            messagingTemplate.convertAndSend("/topic/groups/" + message.getGroupId(), message);
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(ChatMessage message) {
        if (message.getReceiverId() != null) {
            messagingTemplate.convertAndSend("/queue/typing/" + message.getReceiverId(), message);
        } else if (message.getGroupId() != null) {
            messagingTemplate.convertAndSend("/topic/groups/" + message.getGroupId() + "/typing", message);
        }
    }
}

