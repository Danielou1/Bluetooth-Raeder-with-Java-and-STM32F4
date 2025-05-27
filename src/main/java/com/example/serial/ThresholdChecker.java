package com.example.serial;

import java.util.HashMap;
import java.util.Map;

public class ThresholdChecker {

    public static class Threshold {
        private float min;
        private float max;

        public Threshold(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public float getMin() {
            return min;
        }

        public float getMax() {
            return max;
        }

        public void setMin(float min) {
            this.min = min;
        }

        public void setMax(float max) {
            this.max = max;
        }

        public boolean isOutOfRange(float value) {
            return value < min || value > max;
        }
    }

    private final Map<String, Threshold> thresholds;

    public ThresholdChecker() {
        thresholds = new HashMap<>();
        // Valeurs par défaut
        thresholds.put("temperature", new Threshold(0, 50));
        thresholds.put("humidity", new Threshold(20, 80));
        thresholds.put("pressure", new Threshold(950, 1050));
    }

    public void setThreshold(String sensorType, float min, float max) {
        thresholds.put(sensorType, new Threshold(min, max));
    }

    public Threshold getThreshold(String sensorType) {
        return thresholds.get(sensorType);
    }

    public boolean isValueOutOfThreshold(String sensorType, float value) {
        Threshold threshold = thresholds.get(sensorType);
        if (threshold != null) {
            return threshold.isOutOfRange(value);
        }
        return false;
    }
}
