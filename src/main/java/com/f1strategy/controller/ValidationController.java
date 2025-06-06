package com.f1strategy.controller;

import com.f1strategy.model.Driver;
import com.f1strategy.model.Race;
import com.f1strategy.model.RaceResult;
import com.f1strategy.model.TelemetryData;
import com.f1strategy.repository.DriverRepository;
import com.f1strategy.repository.RaceRepository;
import com.f1strategy.repository.RaceResultRepository;
import com.f1strategy.repository.TelemetryDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/validate")
public class ValidationController {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private RaceResultRepository raceResultRepository;

    @Autowired
    private TelemetryDataRepository telemetryDataRepository;

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> validateData() {
        Map<String, Object> validationResults = new HashMap<>();
        
        // checks drivers
        long driverCount = driverRepository.count();
        validationResults.put("drivers", driverCount);
        validationResults.put("driverList", driverRepository.findAll());
        
        // checks races
        long raceCount = raceRepository.count();
        validationResults.put("races", raceCount);
        validationResults.put("raceList", raceRepository.findAll());
        
        // checks race results
        long resultCount = raceResultRepository.count();
        validationResults.put("raceResults", resultCount);
        validationResults.put("resultList", raceResultRepository.findAll());
        
        // checks telemetry data
        long telemetryCount = telemetryDataRepository.count();
        validationResults.put("telemetryData", telemetryCount);
        validationResults.put("telemetryList", telemetryDataRepository.findAll());
        
        return ResponseEntity.ok(validationResults);
    }
} 