package com.f1strategy.model;

import jakarta.persistence.*;

@Entity
@Table(name = "telemetry_data")
public class TelemetryData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long telemetryId;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "race_id")
    private Race race;

    private int lapNumber;
    private double speed;
    private double fuelLevel;
    private double tireWearFrontLeft;
    private double tireWearFrontRight;
    private double tireWearRearLeft;
    private double tireWearRearRight;
    private String tireCompound;
    private double sector1Time;
    private double sector2Time;
    private double sector3Time;
    private double lapTime;

    // getters and setters
    public Long getTelemetryId() {
        return telemetryId;
    }

    public void setTelemetryId(Long telemetryId) {
        this.telemetryId = telemetryId;
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

    public int getLapNumber() {
        return lapNumber;
    }

    public void setLapNumber(int lapNumber) {
        this.lapNumber = lapNumber;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public double getTireWearFrontLeft() {
        return tireWearFrontLeft;
    }

    public void setTireWearFrontLeft(double tireWearFrontLeft) {
        this.tireWearFrontLeft = tireWearFrontLeft;
    }

    public double getTireWearFrontRight() {
        return tireWearFrontRight;
    }

    public void setTireWearFrontRight(double tireWearFrontRight) {
        this.tireWearFrontRight = tireWearFrontRight;
    }

    public double getTireWearRearLeft() {
        return tireWearRearLeft;
    }

    public void setTireWearRearLeft(double tireWearRearLeft) {
        this.tireWearRearLeft = tireWearRearLeft;
    }

    public double getTireWearRearRight() {
        return tireWearRearRight;
    }

    public void setTireWearRearRight(double tireWearRearRight) {
        this.tireWearRearRight = tireWearRearRight;
    }

    public String getTireCompound() {
        return tireCompound;
    }

    public void setTireCompound(String tireCompound) {
        this.tireCompound = tireCompound;
    }

    public double getSector1Time() {
        return sector1Time;
    }

    public void setSector1Time(double sector1Time) {
        this.sector1Time = sector1Time;
    }

    public double getSector2Time() {
        return sector2Time;
    }

    public void setSector2Time(double sector2Time) {
        this.sector2Time = sector2Time;
    }

    public double getSector3Time() {
        return sector3Time;
    }

    public void setSector3Time(double sector3Time) {
        this.sector3Time = sector3Time;
    }

    public double getLapTime() {
        return lapTime;
    }

    public void setLapTime(double lapTime) {
        this.lapTime = lapTime;
    }
} 