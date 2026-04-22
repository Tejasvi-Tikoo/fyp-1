package com.wildroutes.repository;

import com.wildroutes.model.Follower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    List<Follower> findByFollowerId(Long followerId);
    List<Follower> findByFollowingId(Long followingId);
    Optional<Follower> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}

