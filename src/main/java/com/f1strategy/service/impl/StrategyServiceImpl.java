package com.f1strategy.service.impl;

import com.f1strategy.model.Race;
import com.f1strategy.model.Driver;
import com.f1strategy.model.TelemetryData;
import com.f1strategy.repository.TelemetryDataRepository;
import com.f1strategy.service.StrategyService;
import com.f1strategy.ml.StrategyPredictor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
public class StrategyServiceImpl implements StrategyService {

    private static final Logger logger = LoggerFactory.getLogger(StrategyServiceImpl.class);
    
    @Autowired
    private TelemetryDataRepository telemetryDataRepository;
    
    @Autowired
    private StrategyPredictor strategyPredictor;

    @Override
    public Map<String, Object> calculateStrategy(Map<String, Object> request) {
        try {
            logger.info("Calculating strategy with request: {}", request);
            
            Race race = new Race();
            Driver driver = new Driver();

            // sets race parameters
            race.setTrackName((String) request.get("trackName"));
            race.setTrackTemperature(Double.parseDouble(request.get("trackTemperature").toString()));
            race.setWeatherCondition((String) request.get("weatherCondition"));
            race.setTrackLength(Double.parseDouble(request.get("trackLength").toString()));
            race.setNumberOfCorners(Double.parseDouble(request.get("numberOfCorners").toString()));
            int numberOfLaps = (int) Math.round(Double.parseDouble(request.get("numberOfLaps").toString()));

            logger.info("Race parameters set: trackName={}, temperature={}, weather={}, length={}, corners={}, laps={}",
                race.getTrackName(), race.getTrackTemperature(), race.getWeatherCondition(),
                race.getTrackLength(), race.getNumberOfCorners(), numberOfLaps);

            // sets driver parameters
            driver.setDriverName((String) request.get("driverName"));
            driver.setTeam((String) request.get("team"));
            driver.setAggressionIndex(Double.parseDouble(request.get("aggressionIndex").toString()));
            driver.setConsistency(Double.parseDouble(request.get("consistency").toString()));

            logger.info("Driver parameters set: name={}, team={}, aggression={}, consistency={}",
                driver.getDriverName(), driver.getTeam(), driver.getAggressionIndex(), driver.getConsistency());

            // calculates base degradation using machine learning
            logger.info("Calculating base tire degradation...");
            double baseDegradation = strategyPredictor.predictTireDegradation(race, driver, "MEDIUM");
            logger.info("Base tire degradation calculated: {}", baseDegradation);
            
            // calculates optimal pit windows using machine learning
            logger.info("Calculating optimal pit windows...");
            List<Integer> optimalPitWindows = new ArrayList<>();
            int optimalLap = strategyPredictor.predictOptimalPitLap(race, driver, numberOfLaps);
            
            // ensures optimal pit lap is within race length
            if (optimalLap >= numberOfLaps) {
                // adjusts optimal pit lap to stay within race length
                optimalLap = numberOfLaps - 1; 
                logger.info("Adjusted optimal pit lap to {} to stay within race length", optimalLap);
            }
            
            optimalPitWindows.add(optimalLap);
            logger.info("Optimal pit lap calculated: {}", optimalLap);
            
            // calculates actual lap times based on tire compounds used
            List<Double> actualLapTimes = new ArrayList<>();
            List<String> tireCompoundsUsed = new ArrayList<>();
            
            // determines optimal tire compounds based on track temperature
            String firstCompound = calculateOptimalTireCompound(race, driver);
            String secondCompound = firstCompound.equals("HARD") ? "MEDIUM" : 
                                  firstCompound.equals("MEDIUM") ? "SOFT" : "MEDIUM";
            
            logger.info("Selected tire compounds: first={}, second={}", firstCompound, secondCompound);
            
            // calculates first stint lap times
            logger.info("Calculating first stint lap times...");
            for (int i = 1; i <= optimalLap; i++) {
                double lapTime = strategyPredictor.predictLapTime(race, driver, i, firstCompound);
                actualLapTimes.add(lapTime);
                tireCompoundsUsed.add(firstCompound);
            }
            
            // calculates second stint lap times
            logger.info("Calculating second stint lap times...");
            for (int i = optimalLap + 1; i <= numberOfLaps; i++) {
                double lapTime = strategyPredictor.predictLapTime(race, driver, i, secondCompound);
                actualLapTimes.add(lapTime);
                tireCompoundsUsed.add(secondCompound);
            }

            // ensures we have the correct number of laps
            if (actualLapTimes.size() != numberOfLaps) {
                throw new RuntimeException("Incorrect number of lap times generated. Expected: " + 
                                        numberOfLaps + ", Got: " + actualLapTimes.size());
            }

            Map<String, Object> result = new HashMap<>();
            result.put("baseDegradation", baseDegradation);
            result.put("optimalPitWindows", optimalPitWindows);
            result.put("actualLapTimes", actualLapTimes);
            result.put("tireCompoundsUsed", tireCompoundsUsed);
            result.put("tireCompounds", List.of(firstCompound, secondCompound));
            
            logger.info("Strategy calculation completed successfully");
            return result;
        } catch (Exception e) {
            logger.error("Error calculating strategy: {}", e.getMessage(), e);
            throw new RuntimeException("Error calculating strategy: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> compareStrategies(Map<String, Object> request) {
        try {
            Race race = new Race();
            Driver driver = new Driver();

            // sets race parameters
            race.setTrackName((String) request.get("trackName"));
            race.setTrackTemperature(Double.parseDouble(request.get("trackTemperature").toString()));
            race.setWeatherCondition((String) request.get("weatherCondition"));
            race.setTrackLength(Double.parseDouble(request.get("trackLength").toString()));
            race.setNumberOfCorners(Double.parseDouble(request.get("numberOfCorners").toString()));
            int numberOfLaps = (int) Math.round(Double.parseDouble(request.get("numberOfLaps").toString()));

            // sets driver parameters
            driver.setDriverName((String) request.get("driverName"));
            driver.setTeam((String) request.get("team"));
            driver.setAggressionIndex(Double.parseDouble(request.get("aggressionIndex").toString()));
            driver.setConsistency(Double.parseDouble(request.get("consistency").toString()));

            List<Map<String, Object>> strategies = new ArrayList<>();
            
            // one-stop strategies
            String optimalCompound = calculateOptimalTireCompound(race, driver);
            int optimalLap = calculateOptimalPitLap(numberOfLaps, race, driver);
            
            // strategy 1: optimal -> next softer (balanced)
            Map<String, Object> strategy1 = new HashMap<>();
            strategy1.put("strategyType", "ONE_STOP");
            strategy1.put("totalRaceTime", calculateTotalRaceTime(race, driver, 1));
            strategy1.put("pitLaps", List.of(optimalLap));
            strategy1.put("tireCompounds", List.of(optimalCompound, 
                optimalCompound.equals("HARD") ? "MEDIUM" : 
                optimalCompound.equals("MEDIUM") ? "SOFT" : "MEDIUM"));
            strategies.add(strategy1);
            
            // strategy 2: HARD -> MEDIUM (conservative)
            Map<String, Object> strategy2 = new HashMap<>();
            strategy2.put("strategyType", "ONE_STOP");
            strategy2.put("totalRaceTime", calculateTotalRaceTime(race, driver, 1));
            // later pit stop
            strategy2.put("pitLaps", List.of(optimalLap + 5)); 
            strategy2.put("tireCompounds", List.of("HARD", "MEDIUM"));
            strategies.add(strategy2);
            
            // strategy 3: MEDIUM -> SOFT (aggressive)
            Map<String, Object> strategy3 = new HashMap<>();
            strategy3.put("strategyType", "ONE_STOP");
            strategy3.put("totalRaceTime", calculateTotalRaceTime(race, driver, 1));
            // earlier pit stop
            strategy3.put("pitLaps", List.of(optimalLap - 5)); 
            strategy3.put("tireCompounds", List.of("MEDIUM", "SOFT"));
            strategies.add(strategy3);
            
            // strategy 4: SOFT -> MEDIUM (reverse strategy)
            Map<String, Object> strategy4 = new HashMap<>();
            strategy4.put("strategyType", "ONE_STOP");
            strategy4.put("totalRaceTime", calculateTotalRaceTime(race, driver, 1));
            strategy4.put("pitLaps", List.of(optimalLap));
            strategy4.put("tireCompounds", List.of("SOFT", "MEDIUM"));
            strategies.add(strategy4);
            
            // two-stop strategies
            int firstPitLap = optimalLap / 2;
            int secondPitLap = optimalLap;
            
            // strategy 5: SOFT -> MEDIUM -> SOFT (aggressive two-stop)
            Map<String, Object> strategy5 = new HashMap<>();
            strategy5.put("strategyType", "TWO_STOP");
            strategy5.put("totalRaceTime", calculateTotalRaceTime(race, driver, 2));
            strategy5.put("pitLaps", List.of(firstPitLap, secondPitLap));
            strategy5.put("tireCompounds", List.of("SOFT", "MEDIUM", "SOFT"));
            strategies.add(strategy5);
            
            // strategy 6: MEDIUM -> SOFT -> MEDIUM (balanced two-stop)
            Map<String, Object> strategy6 = new HashMap<>();
            strategy6.put("strategyType", "TWO_STOP");
            strategy6.put("totalRaceTime", calculateTotalRaceTime(race, driver, 2));
            strategy6.put("pitLaps", List.of(firstPitLap, secondPitLap));
            strategy6.put("tireCompounds", List.of("MEDIUM", "SOFT", "MEDIUM"));
            strategies.add(strategy6);
            
            // strategy 7: HARD -> MEDIUM -> SOFT (progressive two-stop)
            Map<String, Object> strategy7 = new HashMap<>();
            strategy7.put("strategyType", "TWO_STOP");
            strategy7.put("totalRaceTime", calculateTotalRaceTime(race, driver, 2));
            strategy7.put("pitLaps", List.of(firstPitLap, secondPitLap));
            strategy7.put("tireCompounds", List.of("HARD", "MEDIUM", "SOFT"));
            strategies.add(strategy7);
            
            // calculates actual lap times and tire compounds used for each strategy
            for (Map<String, Object> strategy : strategies) {
                List<Double> actualLapTimes = new ArrayList<>();
                List<String> tireCompoundsUsed = new ArrayList<>();
                List<String> compounds = (List<String>) strategy.get("tireCompounds");
                List<Integer> pitLaps = (List<Integer>) strategy.get("pitLaps");
                
                if (strategy.get("strategyType").equals("ONE_STOP")) {
                    // first stint
                    for (int i = 1; i <= pitLaps.get(0); i++) {
                        double lapTime = predictLapTime(race, driver, i, compounds.get(0));
                        actualLapTimes.add(lapTime);
                        tireCompoundsUsed.add(compounds.get(0));
                    }
                    // second stint
                    for (int i = pitLaps.get(0) + 1; i <= numberOfLaps; i++) {
                        double lapTime = predictLapTime(race, driver, i, compounds.get(1));
                        actualLapTimes.add(lapTime);
                        tireCompoundsUsed.add(compounds.get(1));
                    }
                } else {
                    // first stint
                    for (int i = 1; i <= pitLaps.get(0); i++) {
                        double lapTime = predictLapTime(race, driver, i, compounds.get(0));
                        actualLapTimes.add(lapTime);
                        tireCompoundsUsed.add(compounds.get(0));
                    }
                    // second stint
                    for (int i = pitLaps.get(0) + 1; i <= pitLaps.get(1); i++) {
                        double lapTime = predictLapTime(race, driver, i, compounds.get(1));
                        actualLapTimes.add(lapTime);
                        tireCompoundsUsed.add(compounds.get(1));
                    }
                    // third stint
                    for (int i = pitLaps.get(1) + 1; i <= numberOfLaps; i++) {
                        double lapTime = predictLapTime(race, driver, i, compounds.get(2));
                        actualLapTimes.add(lapTime);
                        tireCompoundsUsed.add(compounds.get(2));
                    }
                }
                
                strategy.put("actualLapTimes", actualLapTimes);
                strategy.put("tireCompoundsUsed", tireCompoundsUsed);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("strategies", strategies);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error comparing strategies: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> optimizePitStop(Map<String, Object> request) {
        try {
            Race race = new Race();
            Driver driver = new Driver();

            // sets race parameters
            race.setTrackName((String) request.get("trackName"));
            race.setTrackTemperature(Double.parseDouble(request.get("trackTemperature").toString()));
            race.setWeatherCondition((String) request.get("weatherCondition"));
            race.setTrackLength(Double.parseDouble(request.get("trackLength").toString()));
            race.setNumberOfCorners(Double.parseDouble(request.get("numberOfCorners").toString()));
            int numberOfLaps = (int) Math.round(Double.parseDouble(request.get("numberOfLaps").toString()));

            // sets driver parameters
            driver.setDriverName((String) request.get("driverName"));
            driver.setTeam((String) request.get("team"));
            driver.setAggressionIndex(Double.parseDouble(request.get("aggressionIndex").toString()));
            driver.setConsistency(Double.parseDouble(request.get("consistency").toString()));

            // determines optimal tire compounds based on track temperature
            String firstCompound = calculateOptimalTireCompound(race, driver);
            String secondCompound = firstCompound.equals("HARD") ? "MEDIUM" : 
                                  firstCompound.equals("MEDIUM") ? "SOFT" : "MEDIUM";

            // calculates optimal pit lap (ensure it's not too early or late)
            int optimalLap = Math.max(6, Math.min(numberOfLaps - 6, calculateOptimalPitLap(numberOfLaps, race, driver)));
            
            // calculates pit stop time loss
            double pitStopTimeLoss = calculatePitStopTimeLoss(race, driver);
            
            // analyzes different pit stop windows around the optimal lap (-5 to +5)
            List<Map<String, Object>> pitStopAnalysis = new ArrayList<>();
            
            // first add laps before optimal (5 laps)
            for (int i = 5; i > 0; i--) {
                int testLap = optimalLap - i;
                Map<String, Object> analysis = addPitStopAnalysis(testLap, race, driver, firstCompound, secondCompound, 
                                 numberOfLaps, pitStopTimeLoss, optimalLap);
                pitStopAnalysis.add(analysis);
            }
            
            // add optimal lap
            Map<String, Object> optimalAnalysis = addPitStopAnalysis(optimalLap, race, driver, firstCompound, secondCompound, 
                             numberOfLaps, pitStopTimeLoss, optimalLap);
            pitStopAnalysis.add(optimalAnalysis);
            
            // then add laps after optimal (5 laps)
            for (int i = 1; i <= 5; i++) {
                int testLap = optimalLap + i;
                Map<String, Object> analysis = addPitStopAnalysis(testLap, race, driver, firstCompound, secondCompound, 
                                 numberOfLaps, pitStopTimeLoss, optimalLap);
                pitStopAnalysis.add(analysis);
            }

            // sorts pit stop analysis by pit lap
            pitStopAnalysis.sort(Comparator.comparingInt(a -> (int) a.get("pitLap")));

            // finds the optimal pit stop window
            Map<String, Object> bestAnalysis = pitStopAnalysis.stream()
                .min(Comparator.comparingDouble(a -> (double) a.get("totalRaceTime")))
                .orElse(optimalAnalysis);

            Map<String, Object> result = new HashMap<>();
            result.put("strategyType", "ONE_STOP");
            result.put("optimalPitLap", bestAnalysis.get("pitLap"));
            result.put("totalRaceTime", bestAnalysis.get("totalRaceTime"));
            result.put("tireDegradationAtPit", bestAnalysis.get("tireDegradationAtPit"));
            result.put("positionImpact", bestAnalysis.get("positionImpact"));
            result.put("pitStopAnalysis", pitStopAnalysis);
            result.put("tireCompounds", List.of(firstCompound, secondCompound));
            result.put("pitStopTimeLoss", pitStopTimeLoss);
            result.put("actualLapTimes", bestAnalysis.get("lapTimes"));
            result.put("tireCompoundsUsed", bestAnalysis.get("compoundsUsed"));
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error optimizing pit stop: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> addPitStopAnalysis(int testLap, Race race, Driver driver, 
                                                 String firstCompound, String secondCompound,
                                                 int numberOfLaps, double pitStopTimeLoss, int optimalLap) {
        // calculates lap times for this pit stop window
        List<Double> lapTimes = new ArrayList<>();
        List<String> compoundsUsed = new ArrayList<>();
        
        // first stint
        for (int i = 1; i <= testLap; i++) {
            double lapTime = predictLapTime(race, driver, i, firstCompound);
            lapTimes.add(lapTime);
            compoundsUsed.add(firstCompound);
        }
        
        // second stint
        for (int i = testLap + 1; i <= numberOfLaps; i++) {
            double lapTime = predictLapTime(race, driver, i, secondCompound);
            lapTimes.add(lapTime);
            compoundsUsed.add(secondCompound);
        }
        
        // calculates total race time including pit stop
        double totalRaceTime = lapTimes.stream().mapToDouble(Double::doubleValue).sum() + pitStopTimeLoss;
        
        // calculates tire degradation at pit stop
        double degradationAtPit = calculateTireDegradation(race, driver, firstCompound) * testLap;
        
        // calculates position impact (estimated)
        int offset = testLap - optimalLap;
        double positionImpact = calculatePositionImpact(offset, race, driver);
        
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("pitLap", testLap);
        analysis.put("totalRaceTime", totalRaceTime);
        analysis.put("tireDegradationAtPit", degradationAtPit);
        analysis.put("positionImpact", positionImpact);
        analysis.put("lapTimes", lapTimes);
        analysis.put("compoundsUsed", compoundsUsed);
        analysis.put("timeDifference", totalRaceTime - calculateBaseRaceTime(race, driver, numberOfLaps));
        analysis.put("offset", offset);
        
        return analysis;
    }

    @Override
    public double predictLapTime(Race race, Driver driver, int lapNumber, String tireCompound) {
        return strategyPredictor.predictLapTime(race, driver, lapNumber, tireCompound);
    }

    @Override
    public double calculateTireDegradation(Race race, Driver driver, String tireCompound) {
        return strategyPredictor.predictTireDegradation(race, driver, tireCompound);
    }

    @Override
    public Map<String, Object> optimizePitStopStrategy(Race race, Driver driver) {
        Map<String, Object> strategy = calculateOptimalStrategy(race, driver);
        List<Map<String, Object>> comparisons = compareStrategies(race, driver);
        
        // finds the strategy with the lowest total race time
        Map<String, Object> optimalStrategy = comparisons.stream()
            .min(Comparator.comparingDouble(s -> (double) s.get("totalRaceTime")))
            .orElse(strategy);
        
        return optimalStrategy;
    }

    @Override
    public List<Map<String, Object>> compareStrategies(Race race, Driver driver) {
        List<Map<String, Object>> strategies = new ArrayList<>();
        
        // one-stop strategy
        Map<String, Object> oneStop = calculateOneStopStrategy(race, driver);
        strategies.add(oneStop);
        
        // two-stop strategy
        Map<String, Object> twoStop = calculateTwoStopStrategy(race, driver);
        strategies.add(twoStop);
        
        return strategies;
    }

    private double calculateBaseLapTime(Race race, Driver driver) {
        return race.getTrackLength() * 0.1 + race.getNumberOfCorners() * 0.5;
    }

    private double calculateFuelEffect(Race race, Driver driver) {
        return race.getTrackLength() * 0.001 * (1 - driver.getConsistency());
    }

    private String calculateOptimalTireCompound(Race race, Driver driver) {
        if (race.getTrackTemperature() > 35) {
            return "SOFT";
        } else if (race.getTrackTemperature() > 25) {
            return "MEDIUM";
        } else {
            return "HARD";
        }
    }

    private int calculateOptimalPitLap(int numberOfLaps, Race race, Driver driver) {
        return strategyPredictor.predictOptimalPitLap(race, driver, numberOfLaps);
    }

    private double calculatePitStopTimeLoss(Race race, Driver driver) {
        return 20 + race.getNumberOfCorners() * 0.5;
    }

    private Map<String, Object> calculateOptimalStrategy(Race race, Driver driver) {
        Map<String, Object> strategy = new HashMap<>();
        
        // calculates base tire degradation
        double baseDegradation = calculateTireDegradation(race, driver, "MEDIUM");
        
        // predicts lap times for different tire compounds
        List<Double> mediumTireLapTimes = new ArrayList<>();
        List<Double> softTireLapTimes = new ArrayList<>();
        List<Double> hardTireLapTimes = new ArrayList<>();
        
        for (int i = 1; i <= race.getTrackLength(); i++) {
            mediumTireLapTimes.add(predictLapTime(race, driver, i, "MEDIUM"));
            softTireLapTimes.add(predictLapTime(race, driver, i, "SOFT"));
            hardTireLapTimes.add(predictLapTime(race, driver, i, "HARD"));
        }
        
        // calculates optimal pit stop windows
        List<Integer> optimalPitWindows = calculatePitStopWindows(mediumTireLapTimes, softTireLapTimes, hardTireLapTimes);
        
        strategy.put("optimalPitWindows", optimalPitWindows);
        strategy.put("baseDegradation", baseDegradation);
        strategy.put("predictedLapTimes", Map.of(
            "MEDIUM", mediumTireLapTimes,
            "SOFT", softTireLapTimes,
            "HARD", hardTireLapTimes
        ));
        
        return strategy;
    }

    private List<Integer> calculatePitStopWindows(List<Double> mediumTireLapTimes, 
                                                List<Double> softTireLapTimes, 
                                                List<Double> hardTireLapTimes) {
        List<Integer> windows = new ArrayList<>();
        // simple implementation - find points where lap times start to degrade significantly
        for (int i = 1; i < mediumTireLapTimes.size(); i++) {
            if (mediumTireLapTimes.get(i) - mediumTireLapTimes.get(i-1) > 0.5) {
                windows.add(i);
            }
        }
        return windows;
    }

    private Map<String, Object> calculateOneStopStrategy(Race race, Driver driver) {
        Map<String, Object> strategy = new HashMap<>();
        strategy.put("strategyType", "ONE_STOP");
        strategy.put("totalRaceTime", calculateTotalRaceTime(race, driver, 1));
        int numberOfLaps = (int) Math.round(305 / race.getTrackLength());
        int optimalLap = calculateOptimalPitLap(numberOfLaps, race, driver);
        strategy.put("pitLaps", List.of(optimalLap));
        
        // determines optimal tire compounds based on track temperature
        String firstCompound = calculateOptimalTireCompound(race, driver);
        String secondCompound = firstCompound.equals("HARD") ? "MEDIUM" : 
                              firstCompound.equals("MEDIUM") ? "SOFT" : "MEDIUM";
        
        strategy.put("tireCompounds", List.of(firstCompound, secondCompound));
        
        // calculates actual lap times
        List<Double> actualLapTimes = new ArrayList<>();
        List<String> tireCompoundsUsed = new ArrayList<>();
        
        // first stint
        for (int i = 1; i <= optimalLap; i++) {
            double lapTime = predictLapTime(race, driver, i, firstCompound);
            actualLapTimes.add(lapTime);
            tireCompoundsUsed.add(firstCompound);
        }
        
        // second stint
        for (int i = optimalLap + 1; i <= numberOfLaps; i++) {
            double lapTime = predictLapTime(race, driver, i, secondCompound);
            actualLapTimes.add(lapTime);
            tireCompoundsUsed.add(secondCompound);
        }
        
        strategy.put("actualLapTimes", actualLapTimes);
        strategy.put("tireCompoundsUsed", tireCompoundsUsed);
        
        return strategy;
    }

    private Map<String, Object> calculateTwoStopStrategy(Race race, Driver driver) {
        Map<String, Object> strategy = new HashMap<>();
        strategy.put("strategyType", "TWO_STOP");
        strategy.put("totalRaceTime", calculateTotalRaceTime(race, driver, 2));
        int numberOfLaps = (int) Math.round(305 / race.getTrackLength());
        
        // calculates optimal pit laps
        int firstPitLap = calculateOptimalPitLap(numberOfLaps, race, driver) / 2;
        int secondPitLap = calculateOptimalPitLap(numberOfLaps, race, driver);
        strategy.put("pitLaps", List.of(firstPitLap, secondPitLap));
        
        // determines optimal tire compounds based on track temperature
        String firstCompound = calculateOptimalTireCompound(race, driver);
        String secondCompound = firstCompound.equals("HARD") ? "MEDIUM" : 
                              firstCompound.equals("MEDIUM") ? "SOFT" : "MEDIUM";
        String thirdCompound = secondCompound.equals("HARD") ? "MEDIUM" : 
                             secondCompound.equals("MEDIUM") ? "SOFT" : "MEDIUM";
        
        strategy.put("tireCompounds", List.of(firstCompound, secondCompound, thirdCompound));
        
        // calculates actual lap times
        List<Double> actualLapTimes = new ArrayList<>();
        List<String> tireCompoundsUsed = new ArrayList<>();
        
        // first stint
        for (int i = 1; i <= firstPitLap; i++) {
            double lapTime = predictLapTime(race, driver, i, firstCompound);
            actualLapTimes.add(lapTime);
            tireCompoundsUsed.add(firstCompound);
        }
        
        // second stint
        for (int i = firstPitLap + 1; i <= secondPitLap; i++) {
            double lapTime = predictLapTime(race, driver, i, secondCompound);
            actualLapTimes.add(lapTime);
            tireCompoundsUsed.add(secondCompound);
        }
        
        // third stint
        for (int i = secondPitLap + 1; i <= numberOfLaps; i++) {
            double lapTime = predictLapTime(race, driver, i, thirdCompound);
            actualLapTimes.add(lapTime);
            tireCompoundsUsed.add(thirdCompound);
        }
        
        strategy.put("actualLapTimes", actualLapTimes);
        strategy.put("tireCompoundsUsed", tireCompoundsUsed);
        
        return strategy;
    }

    private double calculateTotalRaceTime(Race race, Driver driver, int numberOfStops) {
        double baseTime = 0;
        for (int i = 1; i <= race.getTrackLength(); i++) {
            baseTime += predictLapTime(race, driver, i, "MEDIUM");
        }
        return baseTime + (numberOfStops * calculatePitStopTimeLoss(race, driver));
    }

    private double calculatePositionImpact(int offset, Race race, Driver driver) {
        // calculates how much the pit stop timing might affect position
        // earlier pit stops (negative offset) might lose more positions
        // later pit stops (positive offset) might gain positions
        double baseImpact = 0.5; // base position impact
        double aggressionFactor = driver.getAggressionIndex();
        double consistencyFactor = driver.getConsistency();
        
        // more aggressive drivers can handle earlier pit stops better
        // more consistent drivers can handle later pit stops better
        double adjustedImpact = baseImpact * (1 - aggressionFactor * 0.3 + consistencyFactor * 0.2);
        
        // earlier pit stops have more negative impact
        if (offset < 0) {
            return adjustedImpact * (1 + Math.abs(offset) * 0.1);
        }
        // later pit stops have more positive impact
        else {
            return -adjustedImpact * (1 + offset * 0.1);
        }
    }

    private double calculateBaseRaceTime(Race race, Driver driver, int numberOfLaps) {
        // calculates base race time without pit stops
        double totalTime = 0;
        String baseCompound = calculateOptimalTireCompound(race, driver);
        for (int i = 1; i <= numberOfLaps; i++) {
            totalTime += predictLapTime(race, driver, i, baseCompound);
        }
        return totalTime;
    }
} 