package com.f1strategy.ml;

import com.f1strategy.model.Race;
import com.f1strategy.model.Driver;
import com.f1strategy.model.TelemetryData;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Component
public class StrategyPredictor {
    
    private static final Logger logger = LoggerFactory.getLogger(StrategyPredictor.class);
    
    private final ModelTrainer modelTrainer;
    private MultiLayerNetwork lapTimeModel;
    private MultiLayerNetwork tireDegradationModel;
    private MultiLayerNetwork pitStopModel;
    private boolean modelsInitialized = false;
    
    @Autowired
    public StrategyPredictor(ModelTrainer modelTrainer) {
        this.modelTrainer = modelTrainer;
    }
    
    @PostConstruct
    public void init() {
        logger.info("Initializing StrategyPredictor...");
        loadOrTrainModels();
    }
    
    private synchronized void loadOrTrainModels() {
        if (modelsInitialized) {
            return;
        }
        
        try {
            File modelsDir = new File("models");
            if (!modelsDir.exists()) {
                logger.info("Creating models directory...");
                modelsDir.mkdirs();
            }
            
            // tries to load existing models
            File lapTimeModelFile = new File(modelsDir, "lap_time_model.zip");
            File tireDegradationModelFile = new File(modelsDir, "tire_degradation_model.zip");
            File pitStopModelFile = new File(modelsDir, "pit_stop_model.zip");
            
            if (lapTimeModelFile.exists() && tireDegradationModelFile.exists() && pitStopModelFile.exists()) {
                logger.info("Loading existing models...");
                try {
                    lapTimeModel = ModelSerializer.restoreMultiLayerNetwork(lapTimeModelFile);
                    logger.info("Lap time model loaded successfully");
                    tireDegradationModel = ModelSerializer.restoreMultiLayerNetwork(tireDegradationModelFile);
                    logger.info("Tire degradation model loaded successfully");
                    pitStopModel = ModelSerializer.restoreMultiLayerNetwork(pitStopModelFile);
                    logger.info("Pit stop model loaded successfully");
                } catch (Exception e) {
                    logger.error("Error loading models: {}", e.getMessage());
                    throw e;
                }
            } else {
                logger.info("Models not found, training new models...");
                modelTrainer.trainModels();
                loadOrTrainModels(); // Try loading again after training
            }
            
            modelsInitialized = true;
            logger.info("All models initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing models: {}", e.getMessage(), e);
            throw new RuntimeException("Error initializing models: " + e.getMessage(), e);
        }
    }
    
    public double predictLapTime(Race race, Driver driver, int lapNumber, String tireCompound) {
        try {
            logger.info("Predicting lap time for lap {} with tire compound {}", lapNumber, tireCompound);
            if (lapTimeModel == null) {
                throw new IllegalStateException("Lap time model not initialized");
            }
            
            // creates input array
            float[] input = new float[] {
                (float) race.getTrackTemperature(),
                (float) race.getNumberOfCorners(),
                (float) race.getTrackLength(),
                (float) driver.getAggressionIndex(),
                (float) driver.getConsistency(),
                (float) lapNumber,
                getTireCompoundValue(tireCompound)
            };
            
            logger.info("Input array: {}", Arrays.toString(input));
            
            // converts to INDArray
            INDArray inputArray = Nd4j.create(input).reshape(1, 7);
            
            // makes prediction
            INDArray output = lapTimeModel.output(inputArray);
            double prediction = output.getDouble(0);
            logger.info("Predicted lap time: {}", prediction);
            
            return prediction;
            
        } catch (Exception e) {
            logger.error("Error predicting lap time: {}", e.getMessage(), e);
            throw new RuntimeException("Error predicting lap time: " + e.getMessage(), e);
        }
    }
    
    public double predictTireDegradation(Race race, Driver driver, String tireCompound) {
        try {
            logger.info("Predicting tire degradation for tire compound {}", tireCompound);
            if (tireDegradationModel == null) {
                throw new IllegalStateException("Tire degradation model not initialized");
            }
            
            // creates input array
            float[] input = new float[] {
                (float) race.getTrackTemperature(),
                (float) race.getNumberOfCorners(),
                (float) race.getTrackLength(),
                (float) driver.getAggressionIndex(),
                (float) driver.getConsistency(),
                getTireCompoundValue(tireCompound)
            };
            
            logger.info("Input array: {}", Arrays.toString(input));
            
            // converts to INDArray
            INDArray inputArray = Nd4j.create(input).reshape(1, 6);
            
            // makes prediction
            INDArray output = tireDegradationModel.output(inputArray);
            double prediction = output.getDouble(0);
            logger.info("Predicted tire degradation: {}", prediction);
            
            return prediction;
            
        } catch (Exception e) {
            logger.error("Error predicting tire degradation: {}", e.getMessage(), e);
            throw new RuntimeException("Error predicting tire degradation: " + e.getMessage(), e);
        }
    }
    
    public int predictOptimalPitLap(Race race, Driver driver, int numberOfLaps) {
        try {
            logger.info("Predicting optimal pit lap for {} laps", numberOfLaps);
            if (pitStopModel == null) {
                throw new IllegalStateException("Pit stop model not initialized");
            }
            
            // creates input array
            float[] input = new float[] {
                (float) race.getTrackTemperature(),
                (float) race.getNumberOfCorners(),
                (float) race.getTrackLength(),
                (float) driver.getAggressionIndex(),
                (float) driver.getConsistency(),
                (float) numberOfLaps
            };
            
            logger.info("Input array: {}", Arrays.toString(input));
            
            // converts to INDArray
            INDArray inputArray = Nd4j.create(input).reshape(1, 6);
            
            // makes prediction
            INDArray output = pitStopModel.output(inputArray);
            int prediction = (int) Math.round(output.getDouble(0));
            logger.info("Predicted optimal pit lap: {}", prediction);
            
            return prediction;
            
        } catch (Exception e) {
            logger.error("Error predicting optimal pit lap: {}", e.getMessage(), e);
            throw new RuntimeException("Error predicting optimal pit lap: " + e.getMessage(), e);
        }
    }
    
    private float getTireCompoundValue(String tireCompound) {
        switch (tireCompound) {
            case "SOFT": return 0.0f;
            case "MEDIUM": return 0.5f;
            case "HARD": return 1.0f;
            default: return 0.5f;
        }
    }
} 