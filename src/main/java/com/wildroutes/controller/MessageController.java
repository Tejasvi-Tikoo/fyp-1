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
import java.util.List;

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
    public List<Message> directHistory(@AuthenticationPrincipal CustomUserDetails current,
                                       @PathVariable Long peerId) {
        Long me = current.getId();
        List<Message> a = messageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(me, peerId);
        List<Message> b = messageRepository.findByReceiverIdAndSenderIdOrderByTimestampAsc(me, peerId);
        List<Message> all = new ArrayList<>();
        all.addAll(a);
        all.addAll(b);
        all.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
        return all;
    }

    @GetMapping("/groups/{groupId}")
    public List<GroupMessage> groupHistory(@PathVariable Long groupId) {
        return groupMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);
    }
}

