package com.f1strategy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/connection")
    public Map<String, Object> testConnection() {
        try {
            // tries to execute a simple query
            String result = jdbcTemplate.queryForObject("SELECT 1", String.class);
            return Map.of(
                "status", "success",
                "message", "Database connection successful",
                "result", result
            );
        } catch (Exception e) {
            return Map.of(
                "status", "error",
                "message", "Database connection failed",
                "error", e.getMessage()
            );
        }
    }
} 