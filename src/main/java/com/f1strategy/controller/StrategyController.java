package com.f1strategy.controller;

import com.f1strategy.model.Race;
import com.f1strategy.model.Driver;
import com.f1strategy.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/strategy")
public class StrategyController {

    @Autowired
    private StrategyService strategyService;

    @PostMapping("/calculate")
    public ResponseEntity<?> calculateStrategy(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = strategyService.calculateStrategy(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/compare")
    public ResponseEntity<?> compareStrategies(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = strategyService.compareStrategies(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/predict-lap-time")
    public ResponseEntity<Double> predictLapTime(
            @RequestBody Map<String, Object> request) {
        Race race = new Race();
        Driver driver = new Driver();
        
        // sets race parameters
        race.setTrackName((String) request.get("trackName"));
        race.setTrackTemperature((Double) request.get("trackTemperature"));
        race.setWeatherCondition((String) request.get("weatherCondition"));
        
        // sets driver parameters
        driver.setDriverName((String) request.get("driverName"));
        driver.setTeam((String) request.get("team"));
        
        int lapNumber = (Integer) request.get("lapNumber");
        String tireCompound = (String) request.get("tireCompound");
        
        double predictedTime = strategyService.predictLapTime(race, driver, lapNumber, tireCompound);
        return ResponseEntity.ok(predictedTime);
    }

    @PostMapping("/tire-degradation")
    public ResponseEntity<Double> calculateTireDegradation(
            @RequestBody Map<String, Object> request) {
        Race race = new Race();
        Driver driver = new Driver();
        
        // sets race parameters
        race.setTrackName((String) request.get("trackName"));
        race.setTrackTemperature((Double) request.get("trackTemperature"));
        race.setWeatherCondition((String) request.get("weatherCondition"));
        race.setNumberOfCorners((Integer) request.get("numberOfCorners"));
        
        // sets driver parameters
        driver.setDriverName((String) request.get("driverName"));
        driver.setTeam((String) request.get("team"));
        driver.setAggressionIndex((Double) request.get("aggressionIndex"));
        
        String tireCompound = (String) request.get("tireCompound");
        
        double degradation = strategyService.calculateTireDegradation(race, driver, tireCompound);
        return ResponseEntity.ok(degradation);
    }

    @PostMapping("/optimize-pit-stop")
    public ResponseEntity<?> optimizePitStop(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = strategyService.optimizePitStop(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void validateRequest(Map<String, Object> request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        // validates required fields
        String[] requiredFields = {
            "trackName", "trackTemperature", "weatherCondition", "trackLength",
            "numberOfCorners", "numberOfLaps", "driverName", "team", "aggressionIndex", "consistency"
        };

        for (String field : requiredFields) {
            if (!request.containsKey(field)) {
                throw new IllegalArgumentException("Missing required field: " + field);
            }
        }

        // validates numeric fields
        try {
            double trackTemperature = Double.parseDouble(request.get("trackTemperature").toString());
            double trackLength = Double.parseDouble(request.get("trackLength").toString());
            int numberOfCorners = Integer.parseInt(request.get("numberOfCorners").toString());
            int numberOfLaps = Integer.parseInt(request.get("numberOfLaps").toString());
            double aggressionIndex = Double.parseDouble(request.get("aggressionIndex").toString());
            double consistency = Double.parseDouble(request.get("consistency").toString());

            if (trackTemperature < 0 || trackTemperature > 100) {
                throw new IllegalArgumentException("Track temperature must be between 0 and 100");
            }
            if (trackLength <= 0) {
                throw new IllegalArgumentException("Track length must be positive");
            }
            if (numberOfCorners <= 0) {
                throw new IllegalArgumentException("Number of corners must be positive");
            }
            if (numberOfLaps <= 0) {
                throw new IllegalArgumentException("Number of laps must be positive");
            }
            if (aggressionIndex < 0 || aggressionIndex > 1) {
                throw new IllegalArgumentException("Aggression index must be between 0 and 1");
            }
            if (consistency < 0 || consistency > 1) {
                throw new IllegalArgumentException("Consistency must be between 0 and 1");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value in request");
        }
    }
} 