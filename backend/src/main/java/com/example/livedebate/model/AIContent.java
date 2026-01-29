package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIContent {
    private String id;
    private String debate_id;
    private String text;
    private String content; // Alias for text
    private String side;
    private long timestamp;
    private List<Comment> comments = new ArrayList<>();
    private int likes;
    private Map<String, Object> statistics; // views, etc.
}
