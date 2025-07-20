package io.github.jspinak.brobot.runner.common.diagnostics;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

/**
 * Contains diagnostic information about a component's state.
 * 
 * This class provides a snapshot of a component's state at a specific point in time,
 * useful for debugging and monitoring.
 * 
 * @since 1.0.0
 */
@Getter
@Builder
public class DiagnosticInfo {
    
    /**
     * The name of the component providing this diagnostic information
     */
    private final String component;
    
    /**
     * Current state information as key-value pairs
     */
    @Builder.Default
    private final Map<String, Object> states = new HashMap<>();
    
    /**
     * Timestamp when this diagnostic information was captured
     */
    @Builder.Default
    private final Instant timestamp = Instant.now();
    
    /**
     * Correlation ID for tracing execution across components
     */
    private final String correlationId;
    
    /**
     * List of warning messages
     */
    @Builder.Default
    private final List<String> warnings = new ArrayList<>();
    
    /**
     * List of error messages
     */
    @Builder.Default
    private final List<String> errors = new ArrayList<>();
    
    /**
     * Creates a diagnostic info for an error condition
     */
    public static DiagnosticInfo error(String component, Throwable error) {
        List<String> errorList = new ArrayList<>();
        errorList.add(error.getClass().getSimpleName() + ": " + error.getMessage());
        
        return DiagnosticInfo.builder()
                .component(component)
                .timestamp(Instant.now())
                .errors(errorList)
                .build();
    }
}