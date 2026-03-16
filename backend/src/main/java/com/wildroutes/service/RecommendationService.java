package com.wildroutes.service;

import com.wildroutes.model.*;
import com.wildroutes.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating content-based recommendations for users.
 * Recommendations are based on user's liked posts, viewed posts, and comments.
 */
@Service
public class RecommendationService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostViewRepository postViewRepository;
    private final FeatureVectorBuilder featureVectorBuilder;
    private final SimilarityCalculator similarityCalculator;

    public RecommendationService(PostRepository postRepository,
                                 LikeRepository likeRepository,
                                 CommentRepository commentRepository,
                                 PostViewRepository postViewRepository,
                                 FeatureVectorBuilder featureVectorBuilder,
                                 SimilarityCalculator similarityCalculator) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postViewRepository = postViewRepository;
        this.featureVectorBuilder = featureVectorBuilder;
        this.similarityCalculator = similarityCalculator;
    }

    /**
     * Generates recommendations for a user based on their interaction history.
     * Returns top 10 most similar posts sorted by similarity score.
     */
    public List<Post> getRecommendations(Long userId) {
        // Step 1: Get user's interaction history
        List<Post> interactedPosts = getUserInteractedPosts(userId);

        if (interactedPosts.isEmpty()) {
            // If no interactions, return popular posts or random
            return getDefaultRecommendations();
        }

        // Step 2: Build user preference profile
        Map<String, Double> userProfile = buildUserProfile(interactedPosts);

        // Step 3: Get all posts except user's own and already interacted
        List<Post> candidatePosts = getCandidatePosts(userId, interactedPosts);

        // Step 4: Calculate similarity scores
        List<PostSimilarity> similarities = candidatePosts.stream()
                .map(post -> {
                    Map<String, Double> postVector = featureVectorBuilder.buildFeatureVector(post);
                    double similarity = similarityCalculator.calculateCosineSimilarity(userProfile, postVector);
                    return new PostSimilarity(post, similarity);
                })
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity)) // Sort descending
                .limit(10)
                .collect(Collectors.toList());

        // Step 5: Return top 10 posts
        return similarities.stream()
                .map(ps -> ps.post)
                .collect(Collectors.toList());
    }

    /**
     * Gets posts the user has interacted with (liked, viewed, commented).
     */
    private List<Post> getUserInteractedPosts(Long userId) {
        Set<Post> interactedPosts = new HashSet<>();

        // Liked posts
        List<Like> likes = likeRepository.findByUserId(userId);
        likes.forEach(like -> interactedPosts.add(like.getPost()));

        // Viewed posts
        List<PostView> views = postViewRepository.findByUserId(userId);
        views.forEach(view -> interactedPosts.add(view.getPost()));

        // Commented posts
        List<Comment> comments = commentRepository.findByUserId(userId);
        comments.forEach(comment -> interactedPosts.add(comment.getPost()));

        return new ArrayList<>(interactedPosts);
    }

    /**
     * Builds a user preference profile by averaging feature vectors of interacted posts.
     */
    private Map<String, Double> buildUserProfile(List<Post> interactedPosts) {
        Map<String, Double> profile = new HashMap<>();
        Map<String, Integer> featureCounts = new HashMap<>();

        for (Post post : interactedPosts) {
            Map<String, Double> vector = featureVectorBuilder.buildFeatureVector(post);
            for (Map.Entry<String, Double> entry : vector.entrySet()) {
                String feature = entry.getKey();
                double value = entry.getValue();

                profile.put(feature, profile.getOrDefault(feature, 0.0) + value);
                featureCounts.put(feature, featureCounts.getOrDefault(feature, 0) + 1);
            }
        }

        // Average the values
        for (String feature : profile.keySet()) {
            profile.put(feature, profile.get(feature) / featureCounts.get(feature));
        }

        return profile;
    }

    /**
     * Gets candidate posts for recommendation (all posts except user's own and interacted).
     */
    private List<Post> getCandidatePosts(Long userId, List<Post> interactedPosts) {
        List<Post> allPosts = postRepository.findAll();
        Set<Long> interactedPostIds = interactedPosts.stream()
                .map(Post::getId)
                .collect(Collectors.toSet());

        return allPosts.stream()
                .filter(post -> !post.getUser().getId().equals(userId)) // Exclude user's own posts
                .filter(post -> !interactedPostIds.contains(post.getId())) // Exclude interacted posts
                .collect(Collectors.toList());
    }

    /**
     * Returns default recommendations when user has no interaction history.
     * For now, returns recent posts.
     */
    private List<Post> getDefaultRecommendations() {
        // Return top 10 recent posts
        return postRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Helper class to hold post and its similarity score.
     */
    private static class PostSimilarity {
        Post post;
        double similarity;

        PostSimilarity(Post post, double similarity) {
            this.post = post;
            this.similarity = similarity;
        }
    }
}