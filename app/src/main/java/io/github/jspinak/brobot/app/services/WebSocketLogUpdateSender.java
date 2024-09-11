package io.github.jspinak.brobot.app.services;

import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.logging.LogUpdateSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebSocketLogUpdateSender implements LogUpdateSender {
    private final WebSocketService webSocketService;

    @Autowired
    public WebSocketLogUpdateSender(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void sendLogUpdate(List<LogEntry> logEntries) {
        webSocketService.sendLogUpdate(logEntries);
    }
}