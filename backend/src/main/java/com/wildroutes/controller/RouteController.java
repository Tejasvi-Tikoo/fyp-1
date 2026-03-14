package com.wildroutes.controller;

import com.wildroutes.model.Route;
import com.wildroutes.repository.RouteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteRepository routeRepository;

    public RouteController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @GetMapping("/search")
    public List<Route> searchRoutes(@RequestParam(required = false) String activityType,
                                    @RequestParam(required = false) String difficulty,
                                    @RequestParam(required = false) String location) {
        // Simplified: combine filters on client or apply basic server-side filtering
        List<Route> routes = routeRepository.findAll();
        return routes.stream()
                .filter(r -> activityType == null || (r.getActivityType() != null &&
                        r.getActivityType().toLowerCase().contains(activityType.toLowerCase())))
                .filter(r -> difficulty == null || difficulty.equalsIgnoreCase(r.getDifficultyCategory()))
                .filter(r -> location == null || (r.getLocation() != null &&
                        r.getLocation().toLowerCase().contains(location.toLowerCase())))
                .toList();
    }
}

