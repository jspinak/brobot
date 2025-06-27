package io.github.jspinak.brobot.tools.logging.model;

/**
 * Enumerates the different types of log entries in the Brobot automation framework.
 * Each LogEventType represents a specific category of event that can occur during
 * automated testing, allowing for structured logging and efficient filtering
 * of log data.
 * 
 * <p>The LogEventType determines which optional fields in {@link LogData} are relevant
 * and should be populated. This design allows a single, flexible log model to
 * handle diverse logging requirements without creating multiple specialized models.</p>
 * 
 * @see LogData for the log entry model that uses this enum
 * @see io.github.jspinak.brobot.tools.logging.dto.LogDataDTO for the DTO representation
 */
public enum LogEventType {
    /**
     * Logs for user interface actions such as FIND, CLICK, TYPE, DRAG, etc.
     * These entries typically include actionType, actionPerformed, and may include
     * screenshot paths for visual verification.
     */
    ACTION,
    
    /**
     * Logs for initial state detection attempts during application startup or navigation.
     * Failures in state detection don't necessarily indicate problems with the
     * Application Under Test (AUT) but rather that the expected state wasn't found.
     * This is normal during state exploration and transition discovery.
     */
    STATE_DETECTION,
    
    /**
     * Logs for state transitions within the application.
     * These entries track movement between application states and include
     * from/to state information, before/after state snapshots, and transition timing.
     */
    TRANSITION,
    
    /**
     * Logs specifically for state image detection results.
     * Records which state images were searched for and whether they were found,
     * providing detailed visibility into the pattern matching process.
     */
    STATE_IMAGE,
    
    /**
     * Logs for error conditions and exceptions.
     * These entries always include an errorMessage and typically have success=false.
     * May include screenshots or video clips to aid in debugging.
     */
    ERROR,
    
    /**
     * Logs for session lifecycle events (start/end).
     * Marks the boundaries of test sessions and may include session-level
     * metrics and configuration information.
     */
    SESSION,
    
    /**
     * Logs related to video recording operations.
     * Includes start/stop of recordings and references to saved video files
     * via the videoClipPath field.
     */
    VIDEO,
    
    /**
     * Logs for observations and general comments about test execution.
     * Used for non-structured information that doesn't fit other categories,
     * such as test notes, warnings, or contextual information.
     */
    OBSERVATION,
    
    /**
     * Logs for performance metrics and measurements.
     * These entries include the performance field with detailed timing data
     * for various operations, supporting performance analysis and optimization.
     */
    METRICS,
    
    /**
     * Logs for general system information and framework events.
     * Includes initialization messages, configuration changes, and other
     * system-level events that aren't directly related to test execution.
     */
    SYSTEM
}
