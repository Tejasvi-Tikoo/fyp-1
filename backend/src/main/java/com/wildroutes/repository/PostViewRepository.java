package com.wildroutes.repository;

import com.wildroutes.model.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    Optional<PostView> findByUserIdAndPostId(Long userId, Long postId);
    List<PostView> findByUserId(Long userId);
}