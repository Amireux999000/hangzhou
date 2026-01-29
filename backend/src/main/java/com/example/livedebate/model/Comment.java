package com.example.livedebate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String id;
    private String commentId; // Alias
    private String user; // Nickname
    private String userId;
    private String text;
    private String content; // Alias
    private String time; // Display time string
    private long timestamp; // Real timestamp
    private String avatar;
    private int likes;
    private String nickname; // Alias for user

    public Comment(String id, String user, String content, long timestamp) {
        this.id = id;
        this.commentId = id;
        this.user = user;
        this.nickname = user;
        this.userId = "user-" + id;
        this.content = content;
        this.text = content;
        this.timestamp = timestamp;
        this.time = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(timestamp));
        this.avatar = "/static/avatar/default.png";
        this.likes = 0;
    }
}
