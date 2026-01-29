package com.example.livedebate.controller;

import com.example.livedebate.model.*;
import com.example.livedebate.service.MockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private MockDataService mockService;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard() {
        Map<String, Object> data = new HashMap<>();
        Vote votes = mockService.getVotes();
        LiveStatus status = mockService.getLiveStatus();
        
        data.put("totalVotes", votes.getTotalVotes());
        data.put("leftVotes", votes.getLeftVotes());
        data.put("rightVotes", votes.getRightVotes());
        data.put("isLive", status.isLive());
        data.put("liveStreamUrl", status.getStreamUrl());
        data.put("debateTopic", mockService.getDebateTopic());
        
        return ApiResponse.success(data);
    }

    @GetMapping("/streams")
    public ApiResponse<Map<String, Object>> getStreams() {
        Map<String, Object> data = new HashMap<>();
        data.put("streams", mockService.getStreams().values());
        data.put("total", mockService.getStreams().size());
        return ApiResponse.success(data);
    }
    
    @PostMapping("/streams")
    public ApiResponse<Stream> addStream(@RequestBody Stream stream) {
        stream.setId(UUID.randomUUID().toString());
        mockService.getStreams().put(stream.getId(), stream);
        return ApiResponse.success(stream);
    }
    
    @PutMapping("/streams/{id}")
    public ApiResponse<Stream> updateStream(@PathVariable String id, @RequestBody Stream stream) {
        Stream existing = mockService.getStreams().get(id);
        if (existing != null) {
            existing.setName(stream.getName());
            existing.setUrl(stream.getUrl());
            existing.setEnabled(stream.isEnabled());
            return ApiResponse.success(existing);
        }
        return ApiResponse.error("Stream not found");
    }
    
    @DeleteMapping("/streams/{id}")
    public ApiResponse<String> deleteStream(@PathVariable String id) {
        mockService.getStreams().remove(id);
        return ApiResponse.success("Deleted", null);
    }

    @PostMapping("/live/start")
    public ApiResponse<LiveStatus> startLive(@RequestBody Map<String, Object> payload) {
        String streamId = (String) payload.get("streamId");
        Stream stream = mockService.getStreams().get(streamId);
        
        if (stream == null) {
            // Pick first active
            stream = mockService.getStreams().values().stream().filter(Stream::isEnabled).findFirst().orElse(null);
        }
        
        if (stream != null) {
            LiveStatus status = new LiveStatus();
            status.setLive(true);
            status.setStreamUrl(stream.getUrl());
            status.setStreamId(stream.getId());
            mockService.updateLiveStatus(status);
            return ApiResponse.success("Live started", mockService.getLiveStatus());
        }
        
        return ApiResponse.error("No stream available");
    }

    @PostMapping("/live/stop")
    public ApiResponse<LiveStatus> stopLive() {
        LiveStatus status = new LiveStatus();
        status.setLive(false);
        mockService.updateLiveStatus(status);
        return ApiResponse.success("Live stopped", mockService.getLiveStatus());
    }

    @PostMapping("/live/control")
    public ApiResponse<LiveStatus> controlLive(@RequestBody Map<String, Object> payload) {
        String action = (String) payload.get("action");
        String streamId = (String) payload.get("streamId");
        
        if ("start".equals(action)) {
             Stream stream = streamId != null ? mockService.getStreams().get(streamId) : null;
             if (stream == null) {
                  stream = mockService.getStreams().values().stream().filter(Stream::isEnabled).findFirst().orElse(null);
             }
             if (stream != null) {
                 LiveStatus status = new LiveStatus();
                 status.setLive(true);
                 status.setStreamUrl(stream.getUrl());
                 status.setStreamId(stream.getId());
                 mockService.updateLiveStatus(status);
                 return ApiResponse.success("Live started", mockService.getLiveStatus());
             }
             return ApiResponse.error("No stream available");
        } else if ("stop".equals(action)) {
             LiveStatus status = new LiveStatus();
             status.setLive(false);
             mockService.updateLiveStatus(status);
             return ApiResponse.success("Live stopped", mockService.getLiveStatus());
        }
        return ApiResponse.error("Invalid action");
    }

    @GetMapping("/votes/statistics")
    public ApiResponse<Map<String, Object>> getVotesStatistics(@RequestParam(required=false) String streamId) {
        Map<String, Object> data = new HashMap<>();
        Vote votes = mockService.getVotes();
        data.put("totalVotes", votes.getTotalVotes());
        data.put("leftVotes", votes.getLeftVotes());
        data.put("rightVotes", votes.getRightVotes());
        data.put("leftPercentage", votes.getLeftPercentage());
        data.put("rightPercentage", votes.getRightPercentage());
        return ApiResponse.success(data);
    }

    
    @PostMapping("/ai/start")
    public ApiResponse<AIStatus> startAI() {
        mockService.getAIStatus().setStatus("running");
        return ApiResponse.success("AI started", mockService.getAIStatus());
    }
    
    @PostMapping("/ai/stop")
    public ApiResponse<AIStatus> stopAI() {
        mockService.getAIStatus().setStatus("stopped");
        return ApiResponse.success("AI stopped", mockService.getAIStatus());
    }

    @PostMapping("/live/update-votes")
    public ApiResponse<Vote> updateVotes(@RequestBody Map<String, Integer> payload) {
        int left = payload.getOrDefault("left", 0);
        int right = payload.getOrDefault("right", 0);
        mockService.updateVotes(left, right);
        return ApiResponse.success("Votes updated", mockService.getVotes());
    }

    @PostMapping("/live/reset-votes")
    public ApiResponse<Vote> resetVotes() {
        mockService.resetVotes();
        return ApiResponse.success("Votes reset", mockService.getVotes());
    }
    
    @PostMapping("/ai/toggle")
    public ApiResponse<AIStatus> toggleAI(@RequestBody Map<String, Boolean> payload) {
        boolean running = payload.getOrDefault("running", false);
        mockService.setAIStatus(running);
        return ApiResponse.success(mockService.getAIStatus());
    }

    @GetMapping("/ai-content/list")
    public ApiResponse<Object> getAIContentList(@RequestParam(required=false) String debate_id) {
        Map<String, Object> result = new HashMap<>();
        result.put("items", mockService.getAiDebateContent());
        result.put("total", mockService.getAiDebateContent().size());
        return ApiResponse.success(result);
    }

    @GetMapping("/ai-content/{contentId}/comments")
    public ApiResponse<Object> getAIContentComments(@PathVariable String contentId) {
         Map<String, Object> result = new HashMap<>();
         result.put("comments", mockService.getCommentsForContent(contentId));
         return ApiResponse.success(result);
    }

    @GetMapping("/live/viewers")
    public ApiResponse<Map<String, Integer>> getViewers() {
         Map<String, Integer> result = new HashMap<>();
         result.put("count", mockService.getViewers());
         return ApiResponse.success(result);
    }
    
    @PostMapping("/live/broadcast-viewers")
    public ApiResponse<String> broadcastViewers() {
        return ApiResponse.success("Broadcasted", null);
    }

    @GetMapping("/users")
    public ApiResponse<Object> getUsers() {
        return ApiResponse.success(mockService.getUsers());
    }
    
    @GetMapping("/debates")
    public ApiResponse<Object> getDebates() {
         return ApiResponse.success(Collections.singletonList(mockService.getDebateTopic()));
    }

    @GetMapping("/debates/{id}")
    public ApiResponse<DebateTopic> getDebateById(@PathVariable String id) {
        return ApiResponse.success(mockService.getDebateTopic());
    }
    
    @GetMapping("/streams/{streamId}/debate")
    public ApiResponse<DebateTopic> getStreamDebate(@PathVariable String streamId) {
        return ApiResponse.success(mockService.getDebateTopic());
    }
    
    @PutMapping("/streams/{streamId}/debate")
    public ApiResponse<DebateTopic> updateStreamDebate(@PathVariable String streamId, @RequestBody DebateTopic topic) {
        mockService.updateDebate(mockService.getDebateTopic().getId(), topic);
        return ApiResponse.success(topic);
    }
}
