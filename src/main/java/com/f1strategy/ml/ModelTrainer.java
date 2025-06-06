package com.f1strategy.ml;

import com.f1strategy.model.Race;
import com.f1strategy.model.Driver;
import com.f1strategy.model.TelemetryData;
import com.f1strategy.repository.TelemetryDataRepository;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

@Component
public class ModelTrainer {
    
    private static final Logger logger = LoggerFactory.getLogger(ModelTrainer.class);
    private static final Random random = new Random(123);
    
    private final TelemetryDataRepository telemetryDataRepository;
    
    @Autowired
    public ModelTrainer(TelemetryDataRepository telemetryDataRepository) {
        this.telemetryDataRepository = telemetryDataRepository;
    }
    
    @PostConstruct
    public void init() {
        logger.info("Initializing ModelTrainer...");
        trainModels();
    }
    
    public void trainModels() {
        try {
            logger.info("Starting model training...");
            
            // loads or generates training data
            List<TelemetryData> trainingData = loadOrGenerateTrainingData();
            if (trainingData.isEmpty()) {
                throw new RuntimeException("No training data available");
            }
            
            // trains each model
            trainLapTimePredictor(trainingData);
            trainTireDegradationPredictor(trainingData);
            trainOptimalPitStopPredictor(trainingData);
            
            logger.info("All models trained successfully");
        } catch (Exception e) {
            logger.error("Error training models: {}", e.getMessage(), e);
            throw new RuntimeException("Error training models: " + e.getMessage(), e);
        }
    }
    
    public void retrainModels() {
        try {
            logger.info("Starting model retraining...");
            
            // deletes existing model files
            File modelsDir = new File("models");
            if (modelsDir.exists()) {
                File[] modelFiles = modelsDir.listFiles();
                if (modelFiles != null) {
                    for (File file : modelFiles) {
                        if (file.getName().endsWith(".zip")) {
                            file.delete();
                        }
                    }
                }
            }
            
            // trains models from scratch
            trainModels();
            
            logger.info("All models retrained successfully");
        } catch (Exception e) {
            logger.error("Error retraining models: {}", e.getMessage(), e);
            throw new RuntimeException("Error retraining models: " + e.getMessage(), e);
        }
    }
    
    private List<TelemetryData> generateSyntheticData() {
        logger.info("Generating synthetic training data...");
        
        // creates synthetic race
        Race race = new Race();
        race.setTrackName("Synthetic Circuit");
        race.setTrackTemperature(25.0);
        race.setNumberOfCorners(20);
        race.setTrackLength(5.0);
        
        // creates synthetic drivers with different characteristics
        List<Driver> drivers = new ArrayList<>();
        
        // aggressive drivers have high aggression and lower consistency
        Driver aggressiveDriver = new Driver();
        aggressiveDriver.setDriverName("Aggressive Driver");
        aggressiveDriver.setAggressionIndex(0.9);
        aggressiveDriver.setConsistency(0.4);
        drivers.add(aggressiveDriver);
        
        // consistent drivers have low aggression and high consistency
        Driver consistentDriver = new Driver();
        consistentDriver.setDriverName("Consistent Driver");
        consistentDriver.setAggressionIndex(0.3);
        consistentDriver.setConsistency(0.9);
        drivers.add(consistentDriver);
        
        // balanced drivers have medium aggression and consistency
        Driver balancedDriver = new Driver();
        balancedDriver.setDriverName("Balanced Driver");
        balancedDriver.setAggressionIndex(0.6);
        balancedDriver.setConsistency(0.6);
        drivers.add(balancedDriver);
        
        // generates synthetic data points for each driver
        List<TelemetryData> syntheticData = new ArrayList<>();
        for (Driver driver : drivers) {
            // generates data for each driver
            for (int lap = 1; lap <= 50; lap++) {
                // adjusts lap time based on driver characteristics
                double baseTime = 80.0;
                double aggressionImpact = (driver.getAggressionIndex() - 0.5) * 2.0; // -1.0 to +1.0
                double consistencyImpact = (driver.getConsistency() - 0.5) * 2.0; // -1.0 to +1.0
                
                // more aggressive drivers are faster but less consistent
                double lapTime = baseTime - aggressionImpact + (1.0 - driver.getConsistency()) * random.nextDouble() * 3.0;
                
                // creates telemetry data
                TelemetryData data = new TelemetryData();
                data.setRace(race);
                data.setDriver(driver);
                data.setLapNumber(lap);
                data.setTireCompound(lap <= 25 ? "SOFT" : "MEDIUM");
                data.setLapTime(lapTime);
                
                // adds tire wear based on driver characteristics
                double baseTireWear = lap * 0.01;
                double aggressiveTireWear = baseTireWear * (1.0 + driver.getAggressionIndex() * 0.5);
                data.setTireWearFrontLeft(aggressiveTireWear);
                data.setTireWearFrontRight(aggressiveTireWear);
                data.setTireWearRearLeft(aggressiveTireWear);
                data.setTireWearRearRight(aggressiveTireWear);
                
                syntheticData.add(data);
            }
        }
        
        logger.info("Generated {} synthetic data points", syntheticData.size());
        return syntheticData;
    }
    
    private List<TelemetryData> loadOrGenerateTrainingData() {
        try {
            // tries to load existing data
            List<TelemetryData> data = telemetryDataRepository.findAll();
            
            if (data.isEmpty()) {
                logger.warn("No training data available. Generating synthetic data...");
                data = generateSyntheticData();
            }
            
            return data;
        } catch (Exception e) {
            logger.error("Error loading or generating training data: {}", e.getMessage(), e);
            throw new RuntimeException("Error loading or generating training data: " + e.getMessage(), e);
        }
    }
    
    private void trainLapTimePredictor(List<TelemetryData> data) throws Exception {
        logger.info("Training lap time predictor with {} data points...", data.size());
        
        // creates model configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(7)
                .nOut(64)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(64)
                .nOut(32)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(32)
                .nOut(1)
                .activation(Activation.IDENTITY)
                .build())
            .build();
        
        // creates and trains model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        
        // creates input and output arrays
        float[][] inputs = new float[data.size()][7];
        float[] outputs = new float[data.size()];
        
        for (int i = 0; i < data.size(); i++) {
            TelemetryData td = data.get(i);
            inputs[i] = new float[] {
                (float) td.getRace().getTrackTemperature(),
                (float) td.getRace().getNumberOfCorners(),
                (float) td.getRace().getTrackLength(),
                (float) td.getDriver().getAggressionIndex(),
                (float) td.getDriver().getConsistency(),
                (float) td.getLapNumber(),
                getTireCompoundValue(td.getTireCompound())
            };
            outputs[i] = (float) td.getLapTime();
        }
        
        // creates training dataset
        INDArray inputArray = Nd4j.create(inputs);
        INDArray outputArray = Nd4j.create(outputs).reshape(-1, 1);
        DataSet dataset = new DataSet(inputArray, outputArray);
        
        // trains model
        for (int i = 0; i < 100; i++) {
            model.fit(dataset);
        }
        
        // saves model
        File modelsDir = new File("models");
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
        }
        File modelFile = new File(modelsDir, "lap_time_model.zip");
        ModelSerializer.writeModel(model, modelFile, true);
        logger.info("Lap time predictor trained and saved successfully");
    }
    
    private void trainTireDegradationPredictor(List<TelemetryData> data) throws Exception {
        logger.info("Training tire degradation predictor with {} data points...", data.size());
        
        // creates model configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(6)
                .nOut(64)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(64)
                .nOut(32)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(32)
                .nOut(1)
                .activation(Activation.IDENTITY)
                .build())
            .build();
        
        // creates and trains model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        
        // creates input and output arrays
        float[][] inputs = new float[data.size()][6];
        float[] outputs = new float[data.size()];
        
        for (int i = 0; i < data.size(); i++) {
            TelemetryData td = data.get(i);
            inputs[i] = new float[] {
                (float) td.getRace().getTrackTemperature(),
                (float) td.getRace().getNumberOfCorners(),
                (float) td.getRace().getTrackLength(),
                (float) td.getDriver().getAggressionIndex(),
                (float) td.getDriver().getConsistency(),
                getTireCompoundValue(td.getTireCompound())
            };
            outputs[i] = (float) calculateDegradation(td);
        }
        
        // creates training dataset
        INDArray inputArray = Nd4j.create(inputs);
        INDArray outputArray = Nd4j.create(outputs).reshape(-1, 1);
        DataSet dataset = new DataSet(inputArray, outputArray);
        
        // trains model
        for (int i = 0; i < 100; i++) {
            model.fit(dataset);
        }
        
        // saves model
        File modelsDir = new File("models");
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
        }
        File modelFile = new File(modelsDir, "tire_degradation_model.zip");
        ModelSerializer.writeModel(model, modelFile, true);
        logger.info("Tire degradation predictor trained and saved successfully");
    }
    
    private void trainOptimalPitStopPredictor(List<TelemetryData> data) throws Exception {
        logger.info("Training optimal pit stop predictor with {} data points...", data.size());
        
        // creates model configuration
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(123)
            .weightInit(WeightInit.XAVIER)
            .updater(new Adam(0.001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(6)
                .nOut(64)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(64)
                .nOut(32)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(32)
                .nOut(1)
                .activation(Activation.IDENTITY)
                .build())
            .build();
        
        // creates and trains model
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        
        // creates input and output arrays
        float[][] inputs = new float[data.size()][6];
        float[] outputs = new float[data.size()];
        
        for (int i = 0; i < data.size(); i++) {
            TelemetryData td = data.get(i);
            inputs[i] = new float[] {
                (float) td.getRace().getTrackTemperature(),
                (float) td.getRace().getNumberOfCorners(),
                (float) td.getRace().getTrackLength(),
                (float) td.getDriver().getAggressionIndex(),
                (float) td.getDriver().getConsistency(),
                (float) td.getLapNumber()
            };
            outputs[i] = (float) findOptimalPitLap(td);
        }
        
        // creates training dataset
        INDArray inputArray = Nd4j.create(inputs);
        INDArray outputArray = Nd4j.create(outputs).reshape(-1, 1);
        DataSet dataset = new DataSet(inputArray, outputArray);
        
        // trains model
        for (int i = 0; i < 100; i++) {
            model.fit(dataset);
        }
        
        // saves model
        File modelsDir = new File("models");
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
        }
        File modelFile = new File(modelsDir, "pit_stop_model.zip");
        ModelSerializer.writeModel(model, modelFile, true);
        logger.info("Optimal pit stop predictor trained and saved successfully");
    }
    
    private float getTireCompoundValue(String tireCompound) {
        switch (tireCompound) {
            case "SOFT": return 0.0f;
            case "MEDIUM": return 0.5f;
            case "HARD": return 1.0f;
            default: return 0.5f;
        }
    }
    
    private double calculateDegradation(TelemetryData telemetry) {
        // gets driver characteristics
        double aggressionIndex = telemetry.getDriver().getAggressionIndex();
        double consistency = telemetry.getDriver().getConsistency();
        
        // calculates base degradation from tire wear
        double totalTireWear = (
            telemetry.getTireWearFrontLeft() +
            telemetry.getTireWearFrontRight() +
            telemetry.getTireWearRearLeft() +
            telemetry.getTireWearRearRight()
        ) / 4.0;
        
        // aggressive drivers experience more degradation
        double aggressionFactor = 1.0 + (aggressionIndex * 0.5);
        
        // consistent drivers manage their tires better
        double consistencyFactor = 1.0 - (consistency * 0.3);
        
        // calculates final degradation
        return totalTireWear * aggressionFactor * consistencyFactor;
    }
    
    private int findOptimalPitLap(TelemetryData telemetry) {
        // calculates optimal pit lap based on tire degradation, lap time trends, and driver characteristics
        double baseLapTime = telemetry.getLapTime();
        double currentLapTime = telemetry.getLapTime();
        double degradation = (currentLapTime - baseLapTime) / baseLapTime;
        
        // gets driver characteristics
        double aggressionIndex = telemetry.getDriver().getAggressionIndex();
        double consistency = telemetry.getDriver().getConsistency();
        
        // adjusts degradation thresholds based on driver characteristics
        double aggressiveThreshold = 0.02 * (1.0 - aggressionIndex * 0.3); // more aggressive drivers can handle more degradation
        double conservativeThreshold = 0.01 * (1.0 + consistency * 0.3); // more consistent drivers pit earlier
        
        // calculates base pit lap (60% of race length)
        int basePitLap = (int) (telemetry.getLapNumber() * 0.6);
        
        // adjusts pit lap based on driver characteristics
        int adjustedPitLap = basePitLap;
        
        // aggressive drivers tend to pit later
        adjustedPitLap += (int) (aggressionIndex * 5);
        
        // consistent drivers tend to pit earlier
        adjustedPitLap -= (int) (consistency * 3);
        
        // if degradation is significant, consider immediate pit
        if (degradation > aggressiveThreshold) {
            return telemetry.getLapNumber();
        }
        
        // if degradation is moderate, consider pitting soon
        if (degradation > conservativeThreshold) {
            return Math.min(telemetry.getLapNumber() + 2, adjustedPitLap);
        }
        
        // otherwise use the adjusted pit lap
        return Math.min(adjustedPitLap, telemetry.getLapNumber());
    }
} 