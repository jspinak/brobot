package io.github.jspinak.brobot.runner.session.context;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

/**
 * Configuration options for session management.
 * 
 * This class encapsulates all configurable parameters for a session,
 * following the principle of explicit configuration over implicit defaults.
 * 
 * @since 1.0.0
 */
@Getter
@Builder
@ToString
public class SessionOptions {
    
    /**
     * Whether automatic saving is enabled for the session
     */
    @Builder.Default
    private final boolean autosaveEnabled = true;
    
    /**
     * Interval between automatic saves
     */
    @Builder.Default
    private final Duration autosaveInterval = Duration.ofMinutes(5);
    
    /**
     * Maximum number of session history entries to keep
     */
    @Builder.Default
    private final int maxSessionHistory = 10;
    
    /**
     * Whether to capture screenshots during session
     */
    @Builder.Default
    private final boolean captureScreenshots = true;
    
    /**
     * Whether to save execution results in session
     */
    @Builder.Default
    private final boolean saveExecutionResults = true;
    
    /**
     * Maximum size for session data (in MB)
     */
    @Builder.Default
    private final int maxSessionSizeMB = 100;
    
    /**
     * Whether to compress session files
     */
    @Builder.Default
    private final boolean compressSessionFiles = false;
    
    /**
     * Session retention period (how long to keep old sessions)
     */
    @Builder.Default
    private final Duration retentionPeriod = Duration.ofDays(30);
    
    /**
     * Creates default session options
     */
    public static SessionOptions defaultOptions() {
        return SessionOptions.builder().build();
    }
    
    /**
     * Creates options for a minimal session (no autosave, no screenshots)
     */
    public static SessionOptions minimalSession() {
        return SessionOptions.builder()
                .autosaveEnabled(false)
                .captureScreenshots(false)
                .saveExecutionResults(false)
                .build();
    }
    
    /**
     * Creates options for a development session (frequent saves, all features)
     */
    public static SessionOptions developmentSession() {
        return SessionOptions.builder()
                .autosaveInterval(Duration.ofMinutes(1))
                .captureScreenshots(true)
                .saveExecutionResults(true)
                .maxSessionHistory(50)
                .build();
    }
}