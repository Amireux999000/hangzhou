package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveSchedule {
    private String scheduledStartTime;
    private String scheduledEndTime;
    private String streamId;
    private boolean isScheduled;
}
