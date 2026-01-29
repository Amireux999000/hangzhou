package com.example.livedebate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
    }

    public void broadcast(String type, Object data) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", type, "data", data, "timestamp", System.currentTimeMillis()));
            TextMessage message = new TextMessage(json);
            sessions.values().forEach(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
