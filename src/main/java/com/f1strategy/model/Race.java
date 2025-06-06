package com.f1strategy.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "races")
public class Race {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long raceId;

    private String trackName;
    private LocalDate raceDate;
    private double trackTemperature;
    private String weatherCondition;
    private double humidity;
    private double trackLength;
    private double numberOfCorners;

    // getters and setters
    public Long getRaceId() {
        return raceId;
    }

    public void setRaceId(Long raceId) {
        this.raceId = raceId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public LocalDate getRaceDate() {
        return raceDate;
    }

    public void setRaceDate(LocalDate raceDate) {
        this.raceDate = raceDate;
    }

    public double getTrackTemperature() {
        return trackTemperature;
    }

    public void setTrackTemperature(double trackTemperature) {
        this.trackTemperature = trackTemperature;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getTrackLength() {
        return trackLength;
    }

    public void setTrackLength(double trackLength) {
        this.trackLength = trackLength;
    }

    public double getNumberOfCorners() {
        return numberOfCorners;
    }

    public void setNumberOfCorners(double numberOfCorners) {
        this.numberOfCorners = numberOfCorners;
    }
} 