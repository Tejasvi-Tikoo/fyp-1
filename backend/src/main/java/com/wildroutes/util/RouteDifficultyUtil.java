package com.wildroutes.util;

public class RouteDifficultyUtil {

    private static final double ALPHA = 0.4; // distance weight
    private static final double BETA = 0.4;  // elevation weight
    private static final double GAMMA = 0.2; // terrain variance weight

    public static double difficultyScore(double distanceKm, double elevationGainM, double terrainVariance) {
        double L = distanceKm;
        double E = elevationGainM / 100.0;
        double V = terrainVariance;
        return ALPHA * L + BETA * E + GAMMA * V;
    }

    public static String difficultyCategory(double score) {
        if (score < 10) return "Easy";
        if (score < 20) return "Moderate";
        if (score < 35) return "Strenuous";
        return "Expert";
    }
}

