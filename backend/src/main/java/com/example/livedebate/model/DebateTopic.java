package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebateTopic {
    private String id;
    private String title;
    private String description;
    private String leftPosition; // Added based on gateway usage
    private String rightPosition; // Added based on gateway usage
}
