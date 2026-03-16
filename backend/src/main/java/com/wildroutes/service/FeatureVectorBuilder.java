package com.wildroutes.service;

import com.wildroutes.model.Post;
import com.wildroutes.model.Route;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Builds feature vectors for posts based on route attributes.
 * Features: activityType, difficulty, distance, elevationGain, tags
 */
@Component
public class FeatureVectorBuilder {

    /**
     * Builds a feature vector for a post.
     * The vector is a map of feature names to their normalized values.
     */
    public Map<String, Double> buildFeatureVector(Post post) {
        Map<String, Double> vector = new HashMap<>();
        Route route = post.getRoute();

        if (route != null) {
            // Activity type - one-hot encoded, but for simplicity, use hash code normalized
            vector.put("activityType", normalizeActivityType(route.getActivityType()));

            // Difficulty - normalize difficulty score or category
            vector.put("difficulty", normalizeDifficulty(route.getDifficultyScore(), route.getDifficultyCategory()));

            // Distance - normalize to 0-1 range (assuming max 100km)
            vector.put("distance", route.getDistanceKm() != null ? Math.min(route.getDistanceKm() / 100.0, 1.0) : 0.0);

            // Elevation gain - normalize to 0-1 range (assuming max 5000m)
            vector.put("elevationGain", route.getElevationGainM() != null ? Math.min(route.getElevationGainM() / 5000.0, 1.0) : 0.0);
        }

        // Tags - count of tags as a feature
        vector.put("tags", normalizeTags(post.getTags()));

        return vector;
    }

    private double normalizeActivityType(String activityType) {
        if (activityType == null) return 0.0;
        // Simple normalization: hash code mod 100 / 100.0
        return (Math.abs(activityType.hashCode()) % 100) / 100.0;
    }

    private double normalizeDifficulty(Double score, String category) {
        if (score != null) {
            // Normalize score to 0-1 (assuming score is 0-10)
            return Math.min(score / 10.0, 1.0);
        } else if (category != null) {
            // Map categories to values
            switch (category.toLowerCase()) {
                case "easy": return 0.2;
                case "moderate": return 0.5;
                case "strenuous": return 0.7;
                case "expert": return 1.0;
                default: return 0.5;
            }
        }
        return 0.5;
    }

    private double normalizeTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) return 0.0;
        // Number of tags normalized (assuming max 10 tags)
        String[] tagArray = tags.split(",");
        return Math.min(tagArray.length / 10.0, 1.0);
    }
}