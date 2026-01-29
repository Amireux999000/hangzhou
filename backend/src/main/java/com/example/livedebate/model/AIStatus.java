package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIStatus {
    private String status; // stopped, running, paused
    private String aiSessionId;
    private String startTime;
    private Map<String, Object> settings;
    private Map<String, Object> statistics;
    private String stopTime;

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getStopTime() {
        return stopTime;
    }
}
