package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vote {
    private int leftVotes;
    private int rightVotes;
    private int totalVotes;
    private int leftPercentage;
    private int rightPercentage;
}
