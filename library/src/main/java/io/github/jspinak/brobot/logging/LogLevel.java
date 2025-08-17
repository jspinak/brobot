package io.github.jspinak.brobot.logging;

/**
 * Enumeration of log levels for the Brobot logging system.
 */
public enum LogLevel {
    /**
     * Trace level - most detailed logging
     */
    TRACE,
    
    /**
     * Debug level - debugging information
     */
    DEBUG,
    
    /**
     * Info level - informational messages
     */
    INFO,
    
    /**
     * Warning level - warning messages
     */
    WARN,
    
    /**
     * Error level - error messages
     */
    ERROR,
    
    /**
     * Fatal level - fatal error messages
     */
    FATAL,
    
    /**
     * Off - no logging
     */
    OFF
}