package com.f1strategy.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "race_results")
public class RaceResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @Column(nullable = false)
    private Integer finalPosition;

    @Column
    private Integer points;

    @Column
    private Integer pitStops;

    @Column
    private String strategyUsed;

    @Column
    private Double totalRaceTime;

    @Column
    private Double fastestLap;

    // getters and setters
    public Long getResultId() {
        return resultId;
    }

    public void setResultId(Long resultId) {
        this.resultId = resultId;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Race getRace() {
        return race;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public Integer getFinalPosition() {
        return finalPosition;
    }

    public void setFinalPosition(Integer finalPosition) {
        this.finalPosition = finalPosition;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getPitStops() {
        return pitStops;
    }

    public void setPitStops(Integer pitStops) {
        this.pitStops = pitStops;
    }

    public String getStrategyUsed() {
        return strategyUsed;
    }

    public void setStrategyUsed(String strategyUsed) {
        this.strategyUsed = strategyUsed;
    }

    public Double getTotalRaceTime() {
        return totalRaceTime;
    }

    public void setTotalRaceTime(Double totalRaceTime) {
        this.totalRaceTime = totalRaceTime;
    }

    public Double getFastestLap() {
        return fastestLap;
    }

    public void setFastestLap(Double fastestLap) {
        this.fastestLap = fastestLap;
    }

    // toString method
    @Override
    public String toString() {
        return "RaceResult{" +
                "resultId=" + resultId +
                ", driver=" + driver +
                ", race=" + race +
                ", finalPosition=" + finalPosition +
                ", points=" + points +
                ", pitStops=" + pitStops +
                ", strategyUsed='" + strategyUsed + '\'' +
                ", totalRaceTime=" + totalRaceTime +
                ", fastestLap=" + fastestLap +
                '}';
    }

    // equals method
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaceResult that = (RaceResult) o;
        return Objects.equals(resultId, that.resultId) &&
                Objects.equals(driver, that.driver) &&
                Objects.equals(race, that.race) &&
                Objects.equals(finalPosition, that.finalPosition) &&
                Objects.equals(points, that.points) &&
                Objects.equals(pitStops, that.pitStops) &&
                Objects.equals(strategyUsed, that.strategyUsed) &&
                Objects.equals(totalRaceTime, that.totalRaceTime) &&
                Objects.equals(fastestLap, that.fastestLap);
    }

    // hashCode method
    @Override
    public int hashCode() {
        return Objects.hash(resultId, driver, race, finalPosition, points, pitStops, strategyUsed, totalRaceTime, fastestLap);
    }
} 