package io.github.jspinak.brobot.runner.ui.services.logs;

import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * View model for log entries.
 * Provides formatted data for UI display.
 */
@Getter
public class LogEntryViewModel {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final String time;
    private final String fullTime;
    private final String level;
    private final String type;
    private final String message;
    private final String details;
    private final LocalDateTime timestamp;
    
    /**
     * Creates a view model from LogData.
     */
    public LogEntryViewModel(LogData logData) {
        this.timestamp = logData.getTimestamp() != null ? 
            LocalDateTime.ofInstant(logData.getTimestamp(), ZoneId.systemDefault()) : 
            LocalDateTime.now();
        
        this.time = timestamp.format(TIME_FORMATTER);
        this.fullTime = timestamp.format(FULL_TIME_FORMATTER);
        
        // Determine level from success/type
        if (!logData.isSuccess()) {
            this.level = "ERROR";
        } else if (logData.getType() == LogEventType.ERROR) {
            this.level = "ERROR";
        } else {
            this.level = "INFO";
        }
        
        this.type = logData.getType() != null ? logData.getType().name() : "UNKNOWN";
        this.message = logData.getDescription() != null ? logData.getDescription() : "";
        
        // Build details
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Time: ").append(fullTime).append("\n");
        detailsBuilder.append("Level: ").append(level).append("\n");
        detailsBuilder.append("Type: ").append(type).append("\n");
        detailsBuilder.append("Session: ").append(logData.getSessionId()).append("\n");
        detailsBuilder.append("Success: ").append(logData.isSuccess()).append("\n");
        detailsBuilder.append("Duration: ").append(logData.getDuration()).append(" ms\n");
        detailsBuilder.append("Message: ").append(message);
        
        if (logData.getActionType() != null) {
            detailsBuilder.append("\nAction Type: ").append(logData.getActionType());
        }
        
        this.details = detailsBuilder.toString();
    }
    
    /**
     * Creates a view model from LogEvent.
     */
    public LogEntryViewModel(LogEvent logEvent) {
        this.timestamp = LocalDateTime.now();
        this.time = timestamp.format(TIME_FORMATTER);
        this.fullTime = timestamp.format(FULL_TIME_FORMATTER);
        this.level = logEvent.getLevel() != null ? logEvent.getLevel().name() : "INFO";
        this.type = logEvent.getEventType() != null ? logEvent.getEventType().name() : "UNKNOWN";
        this.message = logEvent.getMessage() != null ? logEvent.getMessage() : "";
        
        // Build details
        StringBuilder detailsBuilder = new StringBuilder();
        detailsBuilder.append("Time: ").append(fullTime).append("\n");
        detailsBuilder.append("Level: ").append(level).append("\n");
        detailsBuilder.append("Type: ").append(type).append("\n");
        detailsBuilder.append("Category: ").append(logEvent.getCategory()).append("\n");
        detailsBuilder.append("Message: ").append(message);
        
        if (logEvent.getException() != null) {
            detailsBuilder.append("\n\nException:\n");
            detailsBuilder.append(logEvent.getException().toString());
        }
        
        this.details = detailsBuilder.toString();
    }
    
    /**
     * Checks if this entry matches a search term.
     */
    public boolean matchesSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return true;
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        return message.toLowerCase().contains(lowerSearch) ||
               level.toLowerCase().contains(lowerSearch) ||
               type.toLowerCase().contains(lowerSearch);
    }
}