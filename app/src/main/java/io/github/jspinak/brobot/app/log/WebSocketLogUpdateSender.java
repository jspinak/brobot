package io.github.jspinak.brobot.app.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jspinak.brobot.log.entities.LogEntry;
import io.github.jspinak.brobot.log.entities.LogEntryDTO;
import io.github.jspinak.brobot.log.entities.LogEntryMapper;
import io.github.jspinak.brobot.logging.LogUpdateSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class WebSocketLogUpdateSender implements LogUpdateSender {
    private static final Logger log = LoggerFactory.getLogger(WebSocketLogUpdateSender.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final LogEntryMapper logEntryMapper;

    public WebSocketLogUpdateSender(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper,
                                    LogEntryMapper logEntryMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.logEntryMapper = logEntryMapper;
    }

    @Override
    public void sendLogUpdate(List<LogEntry> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            log.warn("Received null or empty log entries list");
            return;
        }

        try {
            for (LogEntry logEntry : logEntries) {
                if (logEntry == null) {
                    log.warn("Skipping null log entry");
                    continue;
                }

                LogEntryDTO dto = logEntryMapper.toDTO(logEntry);
                log.debug("Converting DTO to JSON: {}", dto);
                String jsonPayload = objectMapper.writeValueAsString(dto);
                log.debug("Sending JSON payload: {}", jsonPayload);

                messagingTemplate.convertAndSend("/topic/logs", jsonPayload);
                log.debug("Successfully sent log entry with ID: {}", dto.getId());
            }
        } catch (Exception e) {
            log.error("Error sending log update via WebSocket", e);
        }
    }

    // Convenience method for single log entries
    public void sendSingleLogUpdate(LogEntry logEntry) {
        sendLogUpdate(Collections.singletonList(logEntry));
    }
}