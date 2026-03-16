package com.wildroutes.controller;

import com.wildroutes.model.Post;
import com.wildroutes.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for recommendation endpoints.
 */
@RestController
@RequestMapping("/api")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * Gets personalized recommendations for a user.
     * Returns top 10 recommended posts based on user's interaction history.
     *
     * @param userId the ID of the user to get recommendations for
     * @return list of recommended posts sorted by similarity score
     */
    @GetMapping("/recommendations/{userId}")
    public List<Post> getRecommendations(@PathVariable Long userId) {
        return recommendationService.getRecommendations(userId);
    }
}