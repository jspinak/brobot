package io.github.jspinak.brobot.debug;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for image finding debugging features.
 *
 * <p>Provides comprehensive control over debugging output including:
 *
 * <ul>
 *   <li>Visual annotations on screenshots
 *   <li>Detailed logging with similarity scores
 *   <li>Comparison image generation
 *   <li>HTML/JSON report generation
 * </ul>
 *
 * @since 1.0
 */
@ConfigurationProperties(prefix = "brobot.debug.image")
@Data
public class ImageDebugConfig {

    /** Master switch for image debugging. */
    private boolean enabled = false;

    /** Debug level: OFF, BASIC, DETAILED, VISUAL, FULL */
    private DebugLevel level = DebugLevel.BASIC;

    /** Save debug screenshots to disk. */
    private boolean saveScreenshots = true;

    /** Save pattern images to disk. */
    private boolean savePatterns = true;

    /** Save comparison images showing pattern vs found region. */
    private boolean saveComparisons = true;

    /** Output directory for debug files. */
    private String outputDir = "debug/image-finding";

    /** Visual debugging settings. */
    private VisualSettings visual = new VisualSettings();

    /** Logging settings. */
    private LogSettings log = new LogSettings();

    /** Real-time monitoring settings. */
    private RealTimeSettings realtime = new RealTimeSettings();

    /** Console output settings. */
    private ConsoleSettings console = new ConsoleSettings();

    @Data
    public static class VisualSettings {
        private boolean enabled = true;
        private boolean showSearchRegions = true;
        private boolean showMatchScores = true;
        private boolean showFailedRegions = true;
        private boolean highlightBestMatch = true;
        private boolean createHeatmap = false;
        private boolean createComparisonGrid = true;
    }

    @Data
    public static class LogSettings {
        private boolean similarityScores = true;
        private boolean searchTime = true;
        private boolean patternDetails = true;
        private boolean dpiInfo = true;
        private boolean searchPath = false;
        private boolean memoryUsage = false;
    }

    @Data
    public static class RealTimeSettings {
        private boolean enabled = false;
        private int port = 8888;
        private boolean autoOpen = false;
    }

    @Data
    public static class ConsoleSettings {
        private boolean useColors = true;
        private boolean showBox = true;
        private boolean showTimestamp = true;
        private boolean showStackTrace = false;
        private boolean compactMode = false;
    }

    public enum DebugLevel {
        OFF(0),
        BASIC(1), // Just success/failure
        DETAILED(2), // Include scores and timing
        VISUAL(3), // Add visual output
        FULL(4); // Everything including memory/performance

        private final int value;

        DebugLevel(int value) {
            this.value = value;
        }

        public boolean isAtLeast(DebugLevel other) {
            return this.value >= other.value;
        }
    }

    /** Check if a specific debug level is active. */
    public boolean isLevelEnabled(DebugLevel requiredLevel) {
        return enabled && level.isAtLeast(requiredLevel);
    }

    /** Check if visual debugging is enabled. */
    public boolean isVisualEnabled() {
        return enabled && visual.isEnabled() && level.isAtLeast(DebugLevel.VISUAL);
    }

    /** Check if file saving is enabled. */
    public boolean shouldSaveFiles() {
        return enabled && (saveScreenshots || savePatterns || saveComparisons);
    }
}
