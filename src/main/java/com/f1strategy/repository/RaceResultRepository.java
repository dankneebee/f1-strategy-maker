package com.f1strategy.repository;

import com.f1strategy.model.RaceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {
    List<RaceResult> findByRace_RaceId(Long raceId);
    List<RaceResult> findByDriver_DriverId(Long driverId);
    RaceResult findByRace_RaceIdAndDriver_DriverId(Long raceId, Long driverId);
} 