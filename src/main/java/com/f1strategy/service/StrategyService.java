package com.f1strategy.service;

import com.f1strategy.model.Race;
import com.f1strategy.model.Driver;
import java.util.Map;
import java.util.List;

public interface StrategyService {
    Map<String, Object> calculateStrategy(Map<String, Object> request);
    Map<String, Object> compareStrategies(Map<String, Object> request);
    Map<String, Object> optimizePitStop(Map<String, Object> request);
    double predictLapTime(Race race, Driver driver, int lapNumber, String tireCompound);
    double calculateTireDegradation(Race race, Driver driver, String tireCompound);
    Map<String, Object> optimizePitStopStrategy(Race race, Driver driver);
    List<Map<String, Object>> compareStrategies(Race race, Driver driver);
} 