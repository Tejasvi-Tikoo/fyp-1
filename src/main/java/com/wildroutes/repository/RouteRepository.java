package com.wildroutes.repository;

import com.wildroutes.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByActivityTypeContainingIgnoreCase(String activityType);
    List<Route> findByDifficultyCategory(String difficultyCategory);
    List<Route> findByLocationContainingIgnoreCase(String location);
}

