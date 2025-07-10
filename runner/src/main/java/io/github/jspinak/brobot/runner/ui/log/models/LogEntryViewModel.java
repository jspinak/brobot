package io.github.jspinak.brobot.runner.ui.log.models;

import io.github.jspinak.brobot.runner.events.LogEvent;
import io.github.jspinak.brobot.tools.logging.model.LogData;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * View model for log entries in the UI.
 * Provides JavaFX properties for data binding.
 */
public class LogEntryViewModel {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private final SimpleStringProperty time = new SimpleStringProperty();
    private final SimpleStringProperty level = new SimpleStringProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();
    private final SimpleStringProperty message = new SimpleStringProperty();
    private final SimpleBooleanProperty success = new SimpleBooleanProperty();
    
    @Getter @Setter
    private LogData rawLogData;
    
    /**
     * Default constructor for JavaFX.
     */
    public LogEntryViewModel() {}
    
    /**
     * Creates a view model from LogData.
     */
    public LogEntryViewModel(LogData logData) {
        this.rawLogData = logData;
        
        LocalDateTime timestamp = LocalDateTime.ofInstant(
            logData.getTimestamp(), 
            ZoneId.systemDefault()
        );
        
        this.time.set(timestamp.format(TIME_FORMATTER));
        this.type.set(logData.getType() != null ? logData.getType().toString() : "UNKNOWN");
        this.message.set(logData.getDescription());
        this.success.set(logData.isSuccess());
        
        // Determine level based on type and success
        if (logData.getType() == LogEventType.ERROR) {
            this.level.set("ERROR");
        } else if (!logData.isSuccess()) {
            this.level.set("WARNING");
        } else {
            this.level.set("INFO");
        }
    }
    
    /**
     * Creates a view model from LogEvent.
     */
    public LogEntryViewModel(LogEvent logEvent) {
        this.level.set(logEvent.getLevel().name());
        this.success.set(
            logEvent.getLevel() == LogEvent.LogLevel.INFO || 
            logEvent.getLevel() == LogEvent.LogLevel.DEBUG
        );
        this.message.set(logEvent.getMessage());
        
        LocalDateTime timestamp = LocalDateTime.ofInstant(
            logEvent.getTimestamp(), 
            ZoneId.systemDefault()
        );
        this.time.set(timestamp.format(TIME_FORMATTER));
        
        // Create minimal LogData for consistency
        LogData tempLogData = new LogData();
        tempLogData.setSuccess(isSuccess());
        tempLogData.setDescription(getMessage());
        tempLogData.setTimestamp(logEvent.getTimestamp());
        
        if (logEvent.getException() != null) {
            tempLogData.setErrorMessage(logEvent.getException().toString());
        }
        
        this.rawLogData = tempLogData;
        
        // Try to determine type from category
        try {
            this.type.set(LogEventType.valueOf(logEvent.getCategory().toUpperCase()).toString());
        } catch (Exception e) {
            this.type.set(LogEventType.SYSTEM.toString());
        }
    }
    
    // Property accessors
    public String getTime() { return time.get(); }
    public void setTime(String value) { time.set(value); }
    public SimpleStringProperty timeProperty() { return time; }
    
    public String getLevel() { return level.get(); }
    public void setLevel(String value) { level.set(value); }
    public SimpleStringProperty levelProperty() { return level; }
    
    public String getType() { return type.get(); }
    public void setType(String value) { type.set(value); }
    public SimpleStringProperty typeProperty() { return type; }
    
    public String getMessage() { return message.get(); }
    public void setMessage(String value) { message.set(value); }
    public SimpleStringProperty messageProperty() { return message; }
    
    public boolean isSuccess() { return success.get(); }
    public void setSuccess(boolean value) { success.set(value); }
    public SimpleBooleanProperty successProperty() { return success; }
    
    /**
     * Gets detailed text representation of the log entry.
     */
    public String getDetailedText() {
        if (rawLogData == null) {
            return "No raw data available.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Time: ").append(getTime()).append("\n");
        sb.append("Level: ").append(getLevel()).append("\n");
        sb.append("Type: ").append(getType()).append("\n");
        sb.append("Success: ").append(isSuccess() ? "Yes" : "No").append("\n\n");
        sb.append("Message: ").append(getMessage()).append("\n\n");
        
        // Add additional details if available
        if (rawLogData.getActionType() != null) {
            sb.append("Action Type: ").append(rawLogData.getActionType()).append("\n");
        }
        
        if (rawLogData.getErrorMessage() != null) {
            sb.append("Error: ").append(rawLogData.getErrorMessage()).append("\n");
        }
        
        if (rawLogData.getCurrentStateName() != null) {
            sb.append("Current State: ").append(rawLogData.getCurrentStateName()).append("\n");
        }
        
        if (rawLogData.getFromStates() != null) {
            sb.append("From States: ").append(rawLogData.getFromStates()).append("\n");
        }
        
        if (rawLogData.getToStateNames() != null && !rawLogData.getToStateNames().isEmpty()) {
            sb.append("To States: ").append(String.join(", ", rawLogData.getToStateNames())).append("\n");
        }
        
        if (rawLogData.getPerformance() != null && rawLogData.getPerformance().getActionDuration() > 0) {
            sb.append("Action Duration: ").append(rawLogData.getPerformance().getActionDuration()).append(" ms\n");
        }
        
        return sb.toString();
    }
}