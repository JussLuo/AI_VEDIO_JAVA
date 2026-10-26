package com.ai168.vedio.service;

import com.ai168.vedio.entity.Shot;

public interface ReplicateVideoClient {
    String submit(Shot shot, byte[] image, String mimeType);

    Prediction getPrediction(String predictionId);

    record Prediction(String status, String outputUrl) {
    }
}
