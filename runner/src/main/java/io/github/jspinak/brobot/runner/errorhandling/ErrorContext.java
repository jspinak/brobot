package io.github.jspinak.brobot.runner.errorhandling;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Context information for errors to aid in debugging and recovery.
 */
@Getter
@Builder
public class ErrorContext {
    
    @Builder.Default
    private final String errorId = UUID.randomUUID().toString();
    
    @Builder.Default
    private final Instant timestamp = Instant.now();
    
    private final String operation;
    private final String component;
    private final String userId;
    private final String sessionId;
    
    private final Map<String, Object> additionalData;
    
    private final ErrorCategory category;
    private final ErrorSeverity severity;
    
    private final boolean recoverable;
    private final String recoveryHint;
    
    // System state at time of error
    private final long memoryUsed;
    private final int activeThreads;
    private final double cpuUsage;
    
    public enum ErrorCategory {
        CONFIGURATION("Configuration Error"),
        FILE_IO("File I/O Error"),
        NETWORK("Network Error"),
        DATABASE("Database Error"),
        VALIDATION("Validation Error"),
        AUTHORIZATION("Authorization Error"),
        AUTOMATION("Automation Error"),
        SYSTEM("System Error"),
        UI("UI Error"),
        UNKNOWN("Unknown Error");
        
        private final String displayName;
        
        ErrorCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum ErrorSeverity {
        LOW(1, "Low"),
        MEDIUM(2, "Medium"),
        HIGH(3, "High"),
        CRITICAL(4, "Critical");
        
        private final int level;
        private final String displayName;
        
        ErrorSeverity(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Create a minimal error context with just the essentials.
     */
    public static ErrorContext minimal(String operation, ErrorCategory category) {
        return ErrorContext.builder()
            .operation(operation)
            .category(category)
            .severity(ErrorSeverity.MEDIUM)
            .build();
    }
    
    /**
     * Create a context for a recoverable error.
     */
    public static ErrorContext recoverable(String operation, ErrorCategory category, 
                                         String recoveryHint) {
        return ErrorContext.builder()
            .operation(operation)
            .category(category)
            .severity(ErrorSeverity.LOW)
            .recoverable(true)
            .recoveryHint(recoveryHint)
            .build();
    }
    
    /**
     * Create a context for a critical error.
     */
    public static ErrorContext critical(String operation, ErrorCategory category) {
        return ErrorContext.builder()
            .operation(operation)
            .category(category)
            .severity(ErrorSeverity.CRITICAL)
            .recoverable(false)
            .build();
    }
}