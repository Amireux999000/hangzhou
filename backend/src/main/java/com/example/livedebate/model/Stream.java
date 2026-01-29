package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stream {
    private String id;
    private String name;
    private String url;
    private String type;
    private String description;
    private boolean enabled;
    private String createdAt;
    private String updatedAt;
    private Map<String, String> playUrls;
    private LiveStatus liveStatus;
}
