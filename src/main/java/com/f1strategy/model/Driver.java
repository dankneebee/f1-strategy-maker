package com.f1strategy.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;

    private String driverName;
    private String team;
    private double aggressionIndex;
    private double overtakingAbility;
    private double consistency;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<RaceResult> raceResults;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
    private List<TelemetryData> telemetryData;

    // getters and setters
    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public double getAggressionIndex() {
        return aggressionIndex;
    }

    public void setAggressionIndex(double aggressionIndex) {
        this.aggressionIndex = aggressionIndex;
    }

    public double getOvertakingAbility() {
        return overtakingAbility;
    }

    public void setOvertakingAbility(double overtakingAbility) {
        this.overtakingAbility = overtakingAbility;
    }

    public double getConsistency() {
        return consistency;
    }

    public void setConsistency(double consistency) {
        this.consistency = consistency;
    }
} 