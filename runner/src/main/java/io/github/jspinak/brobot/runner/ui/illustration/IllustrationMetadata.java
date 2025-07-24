package io.github.jspinak.brobot.runner.ui.illustration;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Metadata for an illustration containing information about the action and its visual elements.
 * <p>
 * This class bridges the gap between the library's illustration data and the
 * Desktop Runner's enhanced visualization capabilities.
 *
 * @see IllustrationViewer
 */
@Data
@Builder
public class IllustrationMetadata {
    
    /**
     * Unique identifier for this illustration.
     */
    private final String id;
    
    /**
     * Timestamp when the illustration was created.
     */
    private final LocalDateTime timestamp;
    
    /**
     * Type of action being illustrated (FIND, CLICK, DRAG, etc.).
     */
    private final String actionType;
    
    /**
     * Name of the state where the action occurred.
     */
    private final String stateName;
    
    /**
     * Search regions used in the action.
     */
    @Singular
    private final List<Region> searchRegions;
    
    /**
     * Matches found during the action.
     */
    @Singular
    private final List<Match> matches;
    
    /**
     * Points involved in the action (click points, drag path, etc.).
     */
    @Singular
    private final List<Point> actionPoints;
    
    /**
     * Additional properties and metadata.
     */
    @Singular
    private final Map<String, Object> properties;
    
    /**
     * Success/failure status of the action.
     */
    private final boolean success;
    
    /**
     * Error message if the action failed.
     */
    private final String errorMessage;
    
    /**
     * Performance metrics for the action.
     */
    private final PerformanceData performanceData;
    
    /**
     * Represents a rectangular region.
     */
    @Data
    @Builder
    public static class Region {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final String label;
    }
    
    /**
     * Represents a match result.
     */
    @Data
    @Builder
    public static class Match {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        private final double similarity;
        private final String objectName;
    }
    
    /**
     * Represents a point in 2D space.
     */
    @Data
    @Builder
    public static class Point {
        private final double x;
        private final double y;
        private final long timestamp; // For animation timing
    }
    
    /**
     * Performance data for the illustrated action.
     */
    @Data
    @Builder
    public static class PerformanceData {
        private final long executionTimeMs;
        private final long imageProcessingTimeMs;
        private final int matchesFound;
        private final double averageSimilarity;
    }
}