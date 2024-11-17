package io.github.jspinak.brobot.app.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.log.entities.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogSender {

    private static final Logger logger = LoggerFactory.getLogger(LogSender.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public LogSender(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendLog(LogEntry logEntry) {
        try {
            logger.debug("Attempting to send log entry: {}", logEntry);
            String payload = objectMapper.writeValueAsString(logEntry);
            logger.debug("Serialized payload: {}", payload);
            messagingTemplate.convertAndSend("/topic/logs", payload);
            logger.info("Log entry sent successfully");
        } catch (Exception e) {
            logger.error("Error sending log entry", e);
        }
    }
}