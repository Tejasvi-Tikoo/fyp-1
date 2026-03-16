package com.wildroutes.controller;

import com.wildroutes.model.*;
import com.wildroutes.repository.*;
import com.wildroutes.exception.ResourceNotFoundException;
import com.wildroutes.security.CustomUserDetails;
import com.wildroutes.util.RouteDifficultyUtil;
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

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final FollowerRepository followerRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;
    private final String uploadDir;

    public PostController(PostRepository postRepository,
                          UserRepository userRepository,
                          RouteRepository routeRepository,
                          FollowerRepository followerRepository,
                          LikeRepository likeRepository,
                          CommentRepository commentRepository,
                          PostViewRepository postViewRepository,
                          @Value("${wildroutes.storage.upload-dir}") String uploadDir) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.followerRepository = followerRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postViewRepository = postViewRepository;
        this.uploadDir = uploadDir;
    }

    @GetMapping("/feed")
    public List<Post> getFeed(@AuthenticationPrincipal CustomUserDetails current) {
        Long userId = current.getId();
        List<Follower> following = followerRepository.findByFollowerId(userId);
        List<Long> ids = new ArrayList<>();
        for (Follower f : following) {
            ids.add(f.getFollowing().getId());
        }
        ids.add(userId);
        return postRepository.findByUserIdInOrderByCreatedAtDesc(ids);
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(
            @AuthenticationPrincipal CustomUserDetails current,
            @RequestPart("title") String title,
            @RequestPart("story") String story,
            @RequestPart(value = "tags", required = false) String tags,
            @RequestPart(value = "location", required = false) String location,
            @RequestPart(value = "activityType", required = false) String activityType,
            @RequestPart(value = "distanceKm", required = false) Double distanceKm,
            @RequestPart(value = "elevationGainM", required = false) Double elevationGainM,
            @RequestPart(value = "terrainVariance", required = false) Double terrainVariance,
            @RequestPart(value = "startLat", required = false) Double startLat,
            @RequestPart(value = "startLng", required = false) Double startLng,
            @RequestPart(value = "endLat", required = false) Double endLat,
            @RequestPart(value = "endLng", required = false) Double endLng,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "routeFile", required = false) MultipartFile routeFile
    ) throws IOException {
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

        String gpxPath = null;
        if (routeFile != null && !routeFile.isEmpty()) {
            String ext = FilenameUtils.getExtension(routeFile.getOriginalFilename());
            String filename = "route_" + System.currentTimeMillis() + "." + ext;
            File dest = new File(uploadDir, filename);
            routeFile.transferTo(dest);
            gpxPath = "/uploads/" + filename;
        }

        Route route = null;
        if (distanceKm != null && elevationGainM != null && terrainVariance != null) {
            double score = RouteDifficultyUtil.difficultyScore(distanceKm, elevationGainM, terrainVariance);
            String category = RouteDifficultyUtil.difficultyCategory(score);
            route = Route.builder()
                    .activityType(activityType)
                    .distanceKm(distanceKm)
                    .elevationGainM(elevationGainM)
                    .terrainVariance(terrainVariance)
                    .difficultyScore(score)
                    .difficultyCategory(category)
                    .gpxFilePath(gpxPath)
                    .location(location)
                    .startLat(startLat)
                    .startLng(startLng)
                    .endLat(endLat)
                    .endLng(endLng)
                    .user(user)
                    .build();
            routeRepository.save(route);
        }

        Post post = Post.builder()
                .title(title)
                .story(story)
                .tags(tags)
                .location(location)
                .imageUrl(imageUrl)
                .user(user)
                .route(route)
                .createdAt(Instant.now())
                .build();
        postRepository.save(post);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<Post> getPost(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails current) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        // Record view if user is authenticated
        if (current != null) {
            Long userId = current.getId();
            postViewRepository.findByUserIdAndPostId(userId, id)
                    .orElseGet(() -> postViewRepository.save(
                            PostView.builder()
                                    .user(userRepository.findById(userId).orElse(null))
                                    .post(post)
                                    .viewedAt(Instant.now())
                                    .build()
                    ));
        }

        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails current) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        Long userId = current.getId();
        likeRepository.findByUserIdAndPostId(userId, post.getId())
                .orElseGet(() -> likeRepository.save(
                        Like.builder().user(userRepository.findById(userId).orElse(null)).post(post).build()
                ));
        return ResponseEntity.ok().build();
    }

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
        return ResponseEntity.ok(comment);
    }
}

