package com.f1strategy.controller;

import com.f1strategy.ml.ModelTrainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelTrainer modelTrainer;

    @Autowired
    public ModelController(ModelTrainer modelTrainer) {
        this.modelTrainer = modelTrainer;
    }

    @PostMapping("/retrain")
    public ResponseEntity<String> retrainModels() {
        try {
            modelTrainer.retrainModels();
            return ResponseEntity.ok("Models retrained successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error retraining models: " + e.getMessage());
        }
    }
} 