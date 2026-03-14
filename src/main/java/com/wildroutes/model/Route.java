package com.wildroutes.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String activityType; // hiking, cycling, etc.

    private Double distanceKm;

    private Double elevationGainM;

    private Double terrainVariance;

    private Double difficultyScore;

    private String difficultyCategory; // Easy, Moderate, Strenuous, Expert

    private String gpxFilePath;

    private String location;

    // Optional start/end coordinates for map display
    private Double startLat;
    private Double startLng;
    private Double endLat;
    private Double endLng;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

