package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String nickName;
    private String avatarUrl;
    private String createdAt;
    private String role;
    private Map<String, Object> statistics;

    public User(String id, String nickName, String avatarUrl, String role) {
        this.id = id;
        this.nickName = nickName;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.statistics = new java.util.HashMap<>();
    }
}
