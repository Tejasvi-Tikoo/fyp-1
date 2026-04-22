package com.wildroutes.controller;

//import com.wildroutes.model.Follower;
import com.wildroutes.model.Post;
import com.wildroutes.model.User;
//import com.wildroutes.repository.FollowerRepository;
import com.wildroutes.repository.PostRepository;
import com.wildroutes.repository.UserRepository;
import com.wildroutes.security.CustomUserDetails;

import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    //private final FollowerRepository followerRepository;

    public UserController(UserRepository userRepository,
                          PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        //this.followerRepository = followerRepository;
    }

    private Map<String, Object> mapPost(Post p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("title", p.getTitle());
        m.put("story", p.getStory());
        m.put("imageUrl", p.getImageUrl());
        m.put("location", p.getLocation());
        m.put("createdAt", p.getCreatedAt());
        m.put("username", p.getUser().getUsername());
        m.put("userId", p.getUser().getId());
        m.put("likeCount", p.getLikes().size());
        m.put("liked", false); // simple for now
        m.put("comments", new java.util.ArrayList<>());
        return m;
    }

    @GetMapping
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long id) {

        return userRepository.findById(id)
                .map(user -> {

                    List<Post> posts = postRepository
                            .findByUserIdOrderByCreatedAtDesc(user.getId());

                    List<Map<String, Object>> postResponses = posts.stream()
                            .map(this::mapPost)
                            .toList();

                    Map<String, Object> body = new HashMap<>();
                    body.put("user", user);
                    body.put("posts", postResponses);
                    //body.put("followersCount", 0); // removed
                    //body.put("followingCount", 0); // removed

                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal CustomUserDetails current,
            @RequestPart(value = "fullName", required = false) String fullName,
            @RequestPart(value = "mobileNumber", required = false) String mobileNumber,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "bio", required = false) String bio,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {

        User user = userRepository.findById(current.getId()).orElseThrow();

        if (fullName != null) user.setFullName(fullName);
        if (mobileNumber != null) user.setMobileNumber(mobileNumber);
        if (email != null) user.setEmail(email);
        if (bio != null) user.setBio(bio);

        // ✅ HANDLE IMAGE UPLOAD
        if (image != null && !image.isEmpty()) {
            String uploadDir = "C:\\Users\\tejas";
            new File(uploadDir).mkdirs();

            String ext = image.getOriginalFilename()
                    .substring(image.getOriginalFilename().lastIndexOf('.') + 1);

            String filename = "profile_" + System.currentTimeMillis() + "." + ext;

            File dest = new File(uploadDir, filename);
            image.transferTo(dest);

            user.setProfilePhotoUrl("/uploads/" + filename);
        }

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
}

