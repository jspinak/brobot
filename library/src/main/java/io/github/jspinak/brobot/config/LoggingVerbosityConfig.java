package io.github.jspinak.brobot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for logging verbosity across the Brobot framework.
 * 
 * <p>This configuration controls the amount of detail included in log output.
 * Normal mode provides concise logging with essential information only,
 * while verbose mode includes all available details for debugging.</p>
 * 
 * <p>Configuration can be set via properties:</p>
 * <pre>
 * brobot.logging.verbosity=NORMAL
 * brobot.logging.normal.show-timing=true
 * brobot.logging.normal.show-match-coordinates=true
 * </pre>
 * 
 * @since 2.0
 */
@Data
@ConfigurationProperties(prefix = "brobot.logging")
public class LoggingVerbosityConfig {
    
    /**
     * Verbosity levels for logging output.
     */
    public enum VerbosityLevel {
        /**
         * Quiet logging - minimal single-line output:
         * - Single line per action with result
         * - Format: ✓/✗ Action State.Object • timing
         * - No START/COMPLETE separation
         */
        QUIET,
        
        /**
         * Normal logging - shows only essential information:
         * - Action type (CLICK, FIND, TYPE, etc.)
         * - State and object acted on
         * - Results (success/failure)
         * - Match coordinates (if configured)
         */
        NORMAL,
        
        /**
         * Verbose logging - shows all available information:
         * - Everything from NORMAL
         * - Detailed timing information
         * - Search regions
         * - Match scores
         * - Performance metrics
         * - Additional metadata
         */
        VERBOSE
    }
    
    /**
     * The verbosity level for logging.
     * Default: NORMAL
     */
    private VerbosityLevel verbosity = VerbosityLevel.NORMAL;
    
    /**
     * Configuration for normal logging mode.
     */
    @Data
    @ConfigurationProperties(prefix = "brobot.logging.normal")
    public static class NormalModeConfig {
        
        /**
         * Whether to show timing information in normal mode.
         * Default: false
         */
        private boolean showTiming = false;
        
        /**
         * Whether to show match coordinates in normal mode.
         * Default: true
         */
        private boolean showMatchCoordinates = true;
        
        /**
         * Whether to show match count in normal mode.
         * Default: true
         */
        private boolean showMatchCount = true;
        
        /**
         * Whether to use compact formatting in normal mode.
         * Default: true
         */
        private boolean useCompactFormat = true;
        
        /**
         * Maximum length for object names before truncation.
         * Default: 30
         */
        private int maxObjectNameLength = 30;
    }
    
    private NormalModeConfig normal = new NormalModeConfig();
    
    /**
     * Configuration for verbose logging mode.
     */
    @Data
    @ConfigurationProperties(prefix = "brobot.logging.verbose")
    public static class VerboseModeConfig {
        
        /**
         * Whether to show search regions in verbose mode.
         * Default: true
         */
        private boolean showSearchRegions = true;
        
        /**
         * Whether to show match scores in verbose mode.
         * Default: true
         */
        private boolean showMatchScores = true;
        
        /**
         * Whether to show action options in verbose mode.
         * Default: true
         */
        private boolean showActionOptions = true;
        
        /**
         * Whether to show performance breakdown in verbose mode.
         * Default: true
         */
        private boolean showPerformanceBreakdown = true;
        
        /**
         * Whether to show metadata in verbose mode.
         * Default: true
         */
        private boolean showMetadata = true;
        
        /**
         * Whether to show stack traces for errors in verbose mode.
         * Default: true
         */
        private boolean showStackTraces = true;
    }
    
    private VerboseModeConfig verbose = new VerboseModeConfig();
    
    /**
     * Helper method to check if running in normal mode.
     */
    public boolean isNormalMode() {
        return verbosity == VerbosityLevel.NORMAL;
    }
    
    /**
     * Helper method to check if running in verbose mode.
     */
    public boolean isVerboseMode() {
        return verbosity == VerbosityLevel.VERBOSE;
    }
}