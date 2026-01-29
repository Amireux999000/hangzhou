package com.example.livedebate.controller;

import com.example.livedebate.model.*;
import com.example.livedebate.service.MockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private MockDataService mockService;

    @GetMapping("/votes")
    public ApiResponse<Vote> getVotes() {
        return ApiResponse.success(mockService.getVotes());
    }

    @GetMapping("/debate-topic")
    public ApiResponse<DebateTopic> getDebateTopic() {
        return ApiResponse.success(mockService.getDebateTopic());
    }

    @GetMapping("/ai-content")
    public ApiResponse<Object> getAIContent() {
        return ApiResponse.success(mockService.getAiDebateContent());
    }

    @PostMapping("/comment")
    public ApiResponse<Comment> addComment(@RequestBody Map<String, String> payload) {
        String contentId = payload.get("contentId");
        String text = payload.get("text");
        String user = payload.get("user");
        String avatar = payload.get("avatar");

        if (contentId == null || text == null) {
            return ApiResponse.error("Missing parameters");
        }

        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setText(text);
        comment.setUser(user != null ? user : "Anonymous");
        comment.setAvatar(avatar != null ? avatar : "👤");
        comment.setTime("Just now");
        comment.setTimestamp(System.currentTimeMillis());
        
        // Find content and add comment
        mockService.getAiDebateContent().stream()
                .filter(c -> c.getId().equals(contentId))
                .findFirst()
                .ifPresent(c -> c.getComments().add(comment));

        return ApiResponse.success(comment);
    }

    @PostMapping("/like")
    public ApiResponse<Map<String, Integer>> like(@RequestBody Map<String, String> payload) {
        String contentId = payload.get("contentId");
        String commentId = payload.get("commentId");
        
        Map<String, Integer> result = new HashMap<>();
        
        mockService.getAiDebateContent().stream()
                .filter(c -> c.getId().equals(contentId))
                .findFirst()
                .ifPresent(c -> {
                    if (commentId != null) {
                        c.getComments().stream()
                            .filter(cmt -> cmt.getId().equals(commentId))
                            .findFirst()
                            .ifPresent(cmt -> {
                                cmt.setLikes(cmt.getLikes() + 1);
                                result.put("likes", cmt.getLikes());
                            });
                    } else {
                        c.setLikes(c.getLikes() + 1);
                        result.put("likes", c.getLikes());
                    }
                });
                
        return ApiResponse.success(result);
    }
    
    @PostMapping("/user-vote")
    public ApiResponse<Vote> userVote(@RequestBody Map<String, Object> payload) {
        String side = (String) payload.get("side");
        Integer votes = (Integer) payload.get("votes");
        
        // Handle 100-vote distribution
        if (payload.containsKey("leftVotes") && payload.containsKey("rightVotes")) {
            int left = Integer.parseInt(payload.get("leftVotes").toString());
            int right = Integer.parseInt(payload.get("rightVotes").toString());
            mockService.vote100(left, right);
        } else if (side != null) {
            mockService.vote(side, votes != null ? votes : 10);
        }
        
        return ApiResponse.success("Vote success", mockService.getVotes());
    }

    @PostMapping("/wechat-login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody Map<String, Object> payload) {
        // Mock Login
        Map<String, Object> data = new HashMap<>();
        data.put("openid", "mock_openid_" + System.currentTimeMillis());
        data.put("session_key", "mock_session_key");
        data.put("userInfo", payload.get("userInfo"));
        data.put("isMock", true);
        return ApiResponse.success(data);
    }
}
