package com.wildroutes.controller;

import com.wildroutes.model.*;
import com.wildroutes.repository.*;
import com.wildroutes.dto.PostResponse;
import com.wildroutes.exception.ResourceNotFoundException;
import com.wildroutes.security.CustomUserDetails;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    //private final FollowerRepository followerRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;
    private final String uploadDir;

    private PostResponse mapToResponse(Post post, Long currentUserId) {

        boolean liked = false;

        if (currentUserId != null) {
            liked = likeRepository
                    .findByUserIdAndPostId(currentUserId, post.getId())
                    .isPresent();
        }

        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .story(post.getStory())
                .imageUrl(post.getImageUrl())
                .location(post.getLocation())
                .createdAt(post.getCreatedAt())
                .username(post.getUser().getUsername())
                .userId(post.getUser().getId())
                .liked(liked)
                .likeCount(0) // 🔥 TEMP FIX
                .comments(new ArrayList<>()) // 🔥 TEMP FIX
                .build();
    }
    public PostController(PostRepository postRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            PostViewRepository postViewRepository,
            @Value("${wildroutes.storage.upload-dir}") String uploadDir) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        //this.followerRepository = followerRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postViewRepository = postViewRepository;
        this.uploadDir = uploadDir;
    }

    // =========================
    // FEED
    // =========================
    @GetMapping("/feed")
    @Transactional(readOnly = true)
    public List<PostResponse> getFeed(@AuthenticationPrincipal CustomUserDetails current) {

        Long userId = current != null ? current.getId() : null;

        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(); // 🔥 ONLY THIS

        return posts.stream()
                .map(p -> mapToResponse(p, userId))
                .toList();
    }



    // =========================
    // CREATE POST
    // =========================
    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(
            @AuthenticationPrincipal CustomUserDetails current,
            @RequestPart("title") String title,
            @RequestPart("story") String story,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "location", required = false) String location,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        User user = userRepository.findById(current.getId()).orElseThrow();

        new File(uploadDir).mkdirs();

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            String ext = FilenameUtils.getExtension(image.getOriginalFilename());
            String filename = "img_" + System.currentTimeMillis() + "." + ext;

            File dest = new File(uploadDir, filename);
            image.transferTo(dest);

            imageUrl = "/uploads/" + filename;
        }

        Post post = Post.builder()
                .title(title)
                .story(story)
                .tags(tags)
                .location(location)
                .imageUrl(imageUrl)
                .user(user)
                .createdAt(Instant.now())
                .build();

        postRepository.save(post);

        return ResponseEntity.ok(post);
    }

    // =========================
    // GET SINGLE POST
    // =========================
    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails current) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (current != null) {
            Long userId = current.getId();

            postViewRepository.findByUserIdAndPostId(userId, id)
                    .orElseGet(() -> postViewRepository.save(
                            PostView.builder()
                                    .user(userRepository.findById(userId).orElse(null))
                                    .post(post)
                                    .viewedAt(Instant.now())
                                    .build()));
        }

        return ResponseEntity.ok(post);
    }

    // =========================
    // LIKE POST
    // =========================
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails current) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Long userId = current.getId();

        return likeRepository.findByUserIdAndPostId(userId, id)
                .map(existing -> {
                    likeRepository.delete(existing);
                    return ResponseEntity.ok("unliked");
                })
                .orElseGet(() -> {
                    Like like = Like.builder()
                            .user(userRepository.findById(userId).orElse(null))
                            .post(post)
                            .build();

                    likeRepository.save(like);
                    return ResponseEntity.ok("liked");
                });
    }

    // =========================
    // COMMENT
    // =========================
    @PostMapping("/posts/{id}/comment")
    public ResponseEntity<Comment> comment(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails current,
            @RequestBody Comment payload) {

        Post post = postRepository.findById(id).orElseThrow();
        User user = userRepository.findById(current.getId()).orElseThrow();

        Comment comment = Comment.builder()
                .content(payload.getContent())
                .createdAt(Instant.now())
                .user(user)
                .post(post)
                .build();

        commentRepository.save(comment);

        return ResponseEntity.ok(
                        Comment.builder()
                                        .id(comment.getId())
                                        .content(comment.getContent())
                                        .createdAt(comment.getCreatedAt())
                                        .user(user) // safe
                                        .build());
    }

    @GetMapping("/posts/{id}/comments")
    public List<Comment> getComments(@PathVariable Long id) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(id);
    }
}