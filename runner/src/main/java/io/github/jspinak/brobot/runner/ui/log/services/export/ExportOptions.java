package io.github.jspinak.brobot.runner.ui.log.services.export;

import lombok.Builder;
import lombok.Getter;

/**
 * Export options configuration.
 */
@Getter
@Builder
public class ExportOptions {
    @Builder.Default
    private boolean includeHeaders = true;
    
    @Builder.Default
    private boolean includeTimestamps = true;
    
    @Builder.Default
    private boolean includeStackTraces = true;
    
    @Builder.Default
    private boolean includeMetadata = false;
    
    @Builder.Default
    private String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    
    @Builder.Default
    private int maxEntries = Integer.MAX_VALUE;
    
    /**
     * Creates default export options.
     */
    public static ExportOptions defaultOptions() {
        return ExportOptions.builder().build();
    }
    
    /**
     * Creates minimal export options (no headers, stack traces, or metadata).
     */
    public static ExportOptions minimalOptions() {
        return ExportOptions.builder()
            .includeHeaders(false)
            .includeStackTraces(false)
            .includeMetadata(false)
            .build();
    }
}