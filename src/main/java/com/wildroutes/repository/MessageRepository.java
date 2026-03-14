package com.wildroutes.repository;

import com.wildroutes.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderIdAndReceiverIdOrderByTimestampAsc(Long senderId, Long receiverId);
    List<Message> findByReceiverIdAndSenderIdOrderByTimestampAsc(Long receiverId, Long senderId);
}

