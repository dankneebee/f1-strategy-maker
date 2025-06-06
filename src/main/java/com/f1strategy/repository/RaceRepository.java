package com.f1strategy.repository;

import com.f1strategy.model.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RaceRepository extends JpaRepository<Race, Long> {
    List<Race> findByTrackName(String trackName);
    List<Race> findByRaceDateBetween(LocalDate startDate, LocalDate endDate);
} 