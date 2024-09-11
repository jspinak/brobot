package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.log.entities.LogEntry;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendLogUpdate(List<LogEntry> logEntries) {
        messagingTemplate.convertAndSend("/topic/logs", logEntries);
    }

    public void sendAutomationStatus(String status) {
        messagingTemplate.convertAndSend("/topic/automation-status", status);
    }
}
