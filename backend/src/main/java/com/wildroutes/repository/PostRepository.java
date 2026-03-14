package com.wildroutes.repository;

import com.wildroutes.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}

