package com.wildroutes.controller;

import com.wildroutes.model.Follower;
import com.wildroutes.model.User;
import com.wildroutes.repository.FollowerRepository;
import com.wildroutes.repository.UserRepository;
import com.wildroutes.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;

    public FollowController(UserRepository userRepository,
                            FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.followerRepository = followerRepository;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> follow(@AuthenticationPrincipal CustomUserDetails me,
                                       @PathVariable Long id) {
        if (me.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }
        User follower = userRepository.findById(me.getId()).orElseThrow();
        User following = userRepository.findById(id).orElseThrow();
        followerRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId())
                .orElseGet(() -> followerRepository.save(
                        Follower.builder().follower(follower).following(following).build()
                ));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unfollow(@AuthenticationPrincipal CustomUserDetails me,
                                         @PathVariable Long id) {
        followerRepository.findByFollowerIdAndFollowingId(me.getId(), id)
                .ifPresent(followerRepository::delete);
        return ResponseEntity.ok().build();
    }
}

