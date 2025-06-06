package com.f1strategy.controller;

import com.f1strategy.model.Driver;
import com.f1strategy.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private DriverRepository driverRepository;

    @GetMapping
    public ResponseEntity<List<Driver>> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        return ResponseEntity.ok(drivers);
    }

    @PostMapping("/custom")
    public ResponseEntity<Driver> createCustomDriver(@RequestBody Map<String, Object> driverData) {
        try {
            String name = (String) driverData.get("name");
            String team = (String) driverData.get("team");
            double aggressionIndex = Double.parseDouble(driverData.get("aggressionIndex").toString());
            double overtakingAbility = Double.parseDouble(driverData.get("overtakingAbility").toString());
            double consistency = Double.parseDouble(driverData.get("consistency").toString());

            Driver driver = new Driver();
            driver.setDriverName(name);
            driver.setTeam(team);
            driver.setAggressionIndex(aggressionIndex);
            driver.setOvertakingAbility(overtakingAbility);
            driver.setConsistency(consistency);

            Driver savedDriver = driverRepository.save(driver);
            return ResponseEntity.ok(savedDriver);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 