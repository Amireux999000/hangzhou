package com.example.livedebate.config;

import com.example.livedebate.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class LiveWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private WebSocketService webSocketService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        webSocketService.addSession(session);
        System.out.println("New WebSocket connection: " + session.getId());
        
        // Send initial connected message
        String msg = objectMapper.writeValueAsString(Map.of("type", "connected", "message", "Connected to Java Backend"));
        session.sendMessage(new TextMessage(msg));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle ping or other messages
        String payload = message.getPayload();
        // System.out.println("Received: " + payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketService.removeSession(session);
        System.out.println("WebSocket closed: " + session.getId());
    }
}
