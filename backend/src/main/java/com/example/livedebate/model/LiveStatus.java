package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveStatus {
    private boolean isLive;
    private String streamUrl;
    private String scheduledStartTime;
    private String scheduledEndTime;
    private String streamId;
    private boolean isScheduled;
    private String liveId;
    private String startTime;
    private String stopTime;
    
    // Additional fields for dashboard
    private String activeStreamUrl;
    private String activeStreamId;
    private String activeStreamName;
    private LiveSchedule schedule;
}
