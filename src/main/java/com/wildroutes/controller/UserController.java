package com.wildroutes.controller;

import com.wildroutes.model.Follower;
import com.wildroutes.model.Post;
import com.wildroutes.model.User;
import com.wildroutes.repository.FollowerRepository;
import com.wildroutes.repository.PostRepository;
import com.wildroutes.repository.UserRepository;
import com.wildroutes.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FollowerRepository followerRepository;

    public UserController(UserRepository userRepository,
                          PostRepository postRepository,
                          FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.followerRepository = followerRepository;
    }

    @GetMapping
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
                    long followers = followerRepository.findByFollowingId(user.getId()).size();
                    long following = followerRepository.findByFollowerId(user.getId()).size();
                    Map<String, Object> body = new HashMap<>();
                    body.put("user", user);
                    body.put("posts", posts);
                    body.put("followersCount", followers);
                    body.put("followingCount", following);
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal CustomUserDetails current,
                                              @RequestBody User update) {
        User user = userRepository.findById(current.getId()).orElseThrow();
        user.setBio(update.getBio());
        user.setTravelInterests(update.getTravelInterests());
        user.setProfilePhotoUrl(update.getProfilePhotoUrl());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}

