package com.f1strategy.repository;

import com.f1strategy.model.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TelemetryDataRepository extends JpaRepository<TelemetryData, Long> {
    List<TelemetryData> findByRace_RaceIdAndDriver_DriverId(Long raceId, Long driverId);
    List<TelemetryData> findByRace_RaceIdAndLapNumber(Long raceId, Integer lapNumber);
} 