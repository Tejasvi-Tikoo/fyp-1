package com.wildroutes.service;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Calculates similarity between feature vectors using cosine similarity.
 */
@Component
public class SimilarityCalculator {

    /**
     * Calculates cosine similarity between two feature vectors.
     * Cosine similarity = (A • B) / (|A| * |B|)
     */
    public double calculateCosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        // Get all unique features from both vectors
        Set<String> allFeatures = new HashSet<>(vectorA.keySet());
        allFeatures.addAll(vectorB.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String feature : allFeatures) {
            double valueA = vectorA.getOrDefault(feature, 0.0);
            double valueB = vectorB.getOrDefault(feature, 0.0);

            dotProduct += valueA * valueB;
            normA += valueA * valueA;
            normB += valueB * valueB;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0; // Avoid division by zero
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}