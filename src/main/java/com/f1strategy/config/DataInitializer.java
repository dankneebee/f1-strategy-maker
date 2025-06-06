package com.f1strategy.config;

import com.f1strategy.model.*;
import com.f1strategy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private RaceResultRepository raceResultRepository;

    @Autowired
    private TelemetryDataRepository telemetryDataRepository;

    @Override
    public void run(String... args) {
        // checks if drivers already exist
        if (driverRepository.count() == 0) {
            List<Driver> drivers = new ArrayList<>();
            
            // creates drivers in parallel
            List<Driver> driverList = Arrays.asList(
                createDriver("Lewis Hamilton", "Mercedes", 0.7, 0.9, 0.95),
                createDriver("Max Verstappen", "Red Bull", 0.8, 0.95, 0.9),
                createDriver("Charles Leclerc", "Ferrari", 0.75, 0.85, 0.9),
                createDriver("Carlos Sainz", "Ferrari", 0.65, 0.8, 0.95),
                createDriver("Sergio Perez", "Red Bull", 0.6, 0.85, 0.9),
                createDriver("George Russell", "Mercedes", 0.7, 0.85, 0.9),
                createDriver("Lando Norris", "McLaren", 0.75, 0.9, 0.85),
                createDriver("Oscar Piastri", "McLaren", 0.7, 0.85, 0.8),
                createDriver("Pierre Gasly", "Alpine", 0.8, 0.9, 0.85),
                createDriver("Esteban Ocon", "Alpine", 0.65, 0.8, 0.9),
                createDriver("Daniel Ricciardo", "AlphaTauri", 0.75, 0.85, 0.8),
                createDriver("Yuki Tsunoda", "AlphaTauri", 0.8, 0.8, 0.7),
                createDriver("Fernando Alonso", "Aston Martin", 0.7, 0.85, 0.9),
                createDriver("Lance Stroll", "Aston Martin", 0.65, 0.75, 0.8),
                createDriver("Valtteri Bottas", "Sauber", 0.6, 0.8, 0.95),
                createDriver("Zhou Guanyu", "Sauber", 0.7, 0.75, 0.8),
                createDriver("Kevin Magnussen", "Haas", 0.8, 0.8, 0.75),
                createDriver("Nico Huelkenberg", "Haas", 0.7, 0.75, 0.8),
                createDriver("Alex Albon", "Williams", 0.65, 0.8, 0.85),
                createDriver("Logan Sargeant", "Williams", 0.6, 0.7, 0.8)
            );
            
            // saves all drivers in a single batch
            driverRepository.saveAll(driverList);
        }

        // ge all drivers from repository
        List<Driver> drivers = driverRepository.findAll();

        // checks if races already exist
        if (raceRepository.count() == 0) {
            List<Race> races = new ArrayList<>();
            
            // creates races in parallel
            List<Race> raceList = Arrays.asList(
                createRace("Bahrain Grand Prix 2021", 57, 15, 22.0, "SUNNY"),
                createRace("Emilia Romagna Grand Prix 2021", 63, 19, 19.0, "RAINY"),
                createRace("Portuguese Grand Prix 2021", 66, 15, 22.0, "SUNNY"),
                createRace("Spanish Grand Prix 2021", 66, 16, 24.0, "SUNNY"),
                createRace("Monaco Grand Prix 2021", 78, 19, 23.0, "SUNNY"),
                createRace("Azerbaijan Grand Prix 2021", 51, 20, 23.0, "SUNNY"),
                createRace("French Grand Prix 2021", 53, 15, 27.0, "SUNNY"),
                createRace("Styrian Grand Prix 2021", 71, 10, 22.0, "SUNNY"),
                createRace("Austrian Grand Prix 2021", 71, 10, 23.0, "SUNNY"),
                createRace("British Grand Prix 2021", 52, 18, 20.0, "CLOUDY")
            );
            
            // saves all races in a single batch
            raceRepository.saveAll(raceList);
            races.addAll(raceList);

            // generates race results and telemetry data in parallel
            races.parallelStream().forEach(race -> {
                List<RaceResult> results = new ArrayList<>();
                List<TelemetryData> telemetryData = new ArrayList<>();
                
                // creates race results
                for (int i = 0; i < drivers.size(); i++) {
                    Driver driver = drivers.get(i);
                    int position = i + 1;
                    int points = calculatePoints(position);
                    int pitStops = determinePitStops(driver, race);
                    String strategy = pitStops == 1 ? "ONE_STOP" : "TWO_STOP";
                    double totalTime = calculateTotalTime(driver, race);
                    double fastestLap = calculateFastestLap(driver, race);
                    
                    results.add(createRaceResult(driver, race, position, points, pitStops, strategy, totalTime, fastestLap));
                }
                
                // saves race results in a single batch
                raceResultRepository.saveAll(results);

                // generates telemetry data
                for (Driver driver : drivers) {
                    for (int lap = 1; lap <= race.getTrackLength(); lap++) {
                        String tireCompound = determineTireCompound(lap, (int) race.getTrackLength(), driver);
                        double lapTime = calculateLapTime(driver, race, lap);
                        double tireWear = calculateTireWear(driver, lap);
                        
                        TelemetryData data = createTelemetryData(
                            driver, race, lap,
                            calculateSpeed(driver),
                            calculateFuelLevel(lap),
                            tireCompound,
                            lapTime / 3.0,
                            lapTime / 3.0,
                            lapTime / 3.0,
                            lapTime
                        );
                        
                        data.setTireWearFrontLeft(tireWear);
                        data.setTireWearFrontRight(tireWear);
                        data.setTireWearRearLeft(tireWear);
                        data.setTireWearRearRight(tireWear);
                        
                        telemetryData.add(data);
                    }
                }
                
                // saves telemetry data in batches of 1000
                int batchSize = 1000;
                for (int i = 0; i < telemetryData.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, telemetryData.size());
                    telemetryDataRepository.saveAll(telemetryData.subList(i, end));
                }
            });
        }
    }

    private int calculatePoints(int position) {
        switch (position) {
            case 1: return 25;
            case 2: return 18;
            case 3: return 15;
            case 4: return 12;
            case 5: return 10;
            case 6: return 8;
            case 7: return 6;
            case 8: return 4;
            case 9: return 2;
            case 10: return 1;
            default: return 0;
        }
    }

    private int determinePitStops(Driver driver, Race race) {
        // drivers with higher aggression and consistency tend to do more pit stops
        double aggressionFactor = driver.getAggressionIndex();
        double consistencyFactor = driver.getConsistency();
        
        // bases pit stops on track length and driver characteristics
        if (race.getTrackLength() > 60) {
            return aggressionFactor > 0.7 ? 2 : 1;
        } else {
            return aggressionFactor > 0.8 ? 2 : 1;
        }
    }

    private double calculateTotalTime(Driver driver, Race race) {
        // bases time per lap
        double baseLapTime = 80.0;
        
        // adjusts based on driver characteristics
        double aggressionImpact = (driver.getAggressionIndex() - 0.5) * 2.0;
        double consistencyImpact = (driver.getConsistency() - 0.5) * 2.0;
        
        // calculates total time
        return race.getTrackLength() * (baseLapTime - aggressionImpact + consistencyImpact);
    }

    private double calculateFastestLap(Driver driver, Race race) {
        // bases fastest lap time
        double baseTime = 80.0;
        
        // adjusts based on driver characteristics
        double aggressionImpact = (driver.getAggressionIndex() - 0.5) * 2.0;
        double consistencyImpact = (driver.getConsistency() - 0.5) * 2.0;
        
        // calculates and returns as double
        return baseTime - aggressionImpact + consistencyImpact;
    }

    private String determineTireCompound(int lap, int totalLaps, Driver driver) {
        // more aggressive drivers tend to use softer compounds
        double aggressionFactor = driver.getAggressionIndex();
        
        if (lap <= totalLaps * 0.3) {
            return aggressionFactor > 0.7 ? "SOFT" : "MEDIUM";
        } else if (lap <= totalLaps * 0.6) {
            return aggressionFactor > 0.5 ? "MEDIUM" : "HARD";
        } else {
            return aggressionFactor > 0.3 ? "HARD" : "MEDIUM";
        }
    }

    private double calculateLapTime(Driver driver, Race race, int lap) {
        // base lap time
        double baseTime = 80.0;
        
        // adjusts based on driver characteristics
        double aggressionImpact = (driver.getAggressionIndex() - 0.5) * 2.0;
        double consistencyImpact = (driver.getConsistency() - 0.5) * 2.0;
        
        // adds tire degradation effect
        double tireDegradation = lap * 0.02;
        
        // calculates and returns as double
        return baseTime - aggressionImpact + consistencyImpact + tireDegradation;
    }

    private double calculateTireWear(Driver driver, int lap) {
        // base tire wear
        double baseWear = lap * 0.01;
        
        // more aggressive drivers wear tires faster
        return baseWear * (1.0 + driver.getAggressionIndex() * 0.5);
    }

    private double calculateSpeed(Driver driver) {
        // base speed
        double baseSpeed = 280.0;
        
        // more aggressive drivers tend to have higher speeds
        return baseSpeed - (driver.getAggressionIndex() * 10.0);
    }

    private double calculateFuelLevel(int lap) {
        // fuel consumption is linear
        return 100.0 - (lap * 0.5);
    }

    private Driver createDriver(String name, String team, double aggressionIndex, double overtakingAbility, double consistency) {
        Driver driver = new Driver();
        driver.setDriverName(name);
        driver.setTeam(team);
        driver.setAggressionIndex(aggressionIndex);
        driver.setOvertakingAbility(overtakingAbility);
        driver.setConsistency(consistency);
        return driver;
    }

    private Race createRace(String name, int laps, int corners, double temperature, String weather) {
        Race race = new Race();
        race.setTrackName(name);
        race.setTrackLength(laps);
        race.setNumberOfCorners(corners);
        race.setTrackTemperature(temperature);
        race.setWeatherCondition(weather);
        return race;
    }

    private RaceResult createRaceResult(Driver driver, Race race, int position, int points, int pitStops, String strategy, double totalTime, double fastestLap) {
        RaceResult result = new RaceResult();
        result.setDriver(driver);
        result.setRace(race);
        result.setFinalPosition(position);
        result.setPoints(points);
        result.setPitStops(pitStops);
        result.setStrategyUsed(strategy);
        result.setTotalRaceTime(totalTime);
        result.setFastestLap(fastestLap);
        return result;
    }

    private TelemetryData createTelemetryData(Driver driver, Race race, int lapNumber, double speed, double fuelLevel, String tireCompound, double sector1Time, double sector2Time, double sector3Time, double lapTime) {
        TelemetryData telemetry = new TelemetryData();
        telemetry.setDriver(driver);
        telemetry.setRace(race);
        telemetry.setLapNumber(lapNumber);
        telemetry.setSpeed(speed);
        telemetry.setFuelLevel(fuelLevel);
        telemetry.setTireCompound(tireCompound);
        telemetry.setSector1Time(sector1Time);
        telemetry.setSector2Time(sector2Time);
        telemetry.setSector3Time(sector3Time);
        telemetry.setLapTime(lapTime);
        return telemetry;
    }
} 