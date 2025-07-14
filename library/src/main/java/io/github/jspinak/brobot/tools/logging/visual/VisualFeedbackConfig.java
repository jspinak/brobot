package io.github.jspinak.brobot.tools.logging.visual;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.*;

/**
 * Configuration for visual feedback during automation execution.
 * Controls highlighting of found images and search regions.
 * 
 * <p>This configuration can be set via properties files using the prefix
 * "brobot.highlight". For example:</p>
 * <pre>
 * brobot.highlight.enabled=true
 * brobot.highlight.auto-highlight-finds=true
 * brobot.highlight.find.color=#00FF00
 * brobot.highlight.find.duration=2.0
 * </pre>
 * 
 * @see HighlightManager for the implementation that uses this config
 */
@Data
@Component
@ConfigurationProperties(prefix = "brobot.highlight")
public class VisualFeedbackConfig {
    
    /**
     * Whether visual highlighting is enabled globally.
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Whether to automatically highlight successful finds.
     * Default: true
     */
    private boolean autoHighlightFinds = true;
    
    /**
     * Whether to automatically highlight search regions before searching.
     * Default: false
     */
    private boolean autoHighlightSearchRegions = false;
    
    /**
     * Configuration for find highlighting.
     */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.find")
    public static class FindHighlightConfig {
        /**
         * Color for highlighting found images.
         * Default: Green (#00FF00)
         */
        private String color = "#00FF00";
        
        /**
         * Duration in seconds to show the highlight.
         * Default: 2.0 seconds
         */
        private double duration = 2.0;
        
        /**
         * Border width in pixels.
         * Default: 3
         */
        private int borderWidth = 3;
        
        /**
         * Whether to flash the highlight.
         * Default: false
         */
        private boolean flash = false;
        
        /**
         * Number of times to flash if enabled.
         * Default: 2
         */
        private int flashCount = 2;
        
        /**
         * Flash interval in milliseconds.
         * Default: 300ms
         */
        private long flashInterval = 300;
        
        /**
         * Converts the color string to a Color object.
         */
        public Color getColorObject() {
            try {
                return Color.decode(color);
            } catch (NumberFormatException e) {
                // Default to green if color parsing fails
                return Color.GREEN;
            }
        }
    }
    
    /**
     * Configuration for search region highlighting.
     */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.search-region")
    public static class SearchRegionHighlightConfig {
        /**
         * Color for highlighting search regions.
         * Default: Blue (#0000FF)
         */
        private String color = "#0000FF";
        
        /**
         * Duration in seconds to show the highlight.
         * Default: 1.0 seconds
         */
        private double duration = 1.0;
        
        /**
         * Border width in pixels.
         * Default: 2
         */
        private int borderWidth = 2;
        
        /**
         * Opacity for filled highlights (0.0 to 1.0).
         * Default: 0.3
         */
        private double opacity = 0.3;
        
        /**
         * Whether to fill the region or just show border.
         * Default: false
         */
        private boolean filled = false;
        
        /**
         * Whether to show region dimensions as text.
         * Default: false
         */
        private boolean showDimensions = false;
        
        /**
         * Converts the color string to a Color object.
         */
        public Color getColorObject() {
            try {
                Color base = Color.decode(color);
                if (filled && opacity < 1.0) {
                    // Apply opacity for filled regions
                    int alpha = (int) (opacity * 255);
                    return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
                }
                return base;
            } catch (NumberFormatException e) {
                // Default to blue if color parsing fails
                return Color.BLUE;
            }
        }
    }
    
    /**
     * Configuration for error highlighting.
     */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.error")
    public static class ErrorHighlightConfig {
        /**
         * Whether to highlight areas where finds failed.
         * Default: false
         */
        private boolean enabled = false;
        
        /**
         * Color for error highlights.
         * Default: Red (#FF0000)
         */
        private String color = "#FF0000";
        
        /**
         * Duration in seconds to show the error highlight.
         * Default: 3.0 seconds
         */
        private double duration = 3.0;
        
        /**
         * Whether to show a cross mark on failed areas.
         * Default: true
         */
        private boolean showCrossMark = true;
        
        /**
         * Converts the color string to a Color object.
         */
        public Color getColorObject() {
            try {
                return Color.decode(color);
            } catch (NumberFormatException e) {
                // Default to red if color parsing fails
                return Color.RED;
            }
        }
    }
    
    /**
     * Configuration for click highlighting.
     */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.click")
    public static class ClickHighlightConfig {
        /**
         * Whether to highlight click locations.
         * Default: true
         */
        private boolean enabled = true;
        
        /**
         * Color for click highlights.
         * Default: Yellow (#FFFF00)
         */
        private String color = "#FFFF00";
        
        /**
         * Duration in seconds to show the click highlight.
         * Default: 0.5 seconds
         */
        private double duration = 0.5;
        
        /**
         * Radius of the click indicator circle.
         * Default: 20 pixels
         */
        private int radius = 20;
        
        /**
         * Whether to show ripple effect.
         * Default: true
         */
        private boolean rippleEffect = true;
        
        /**
         * Converts the color string to a Color object.
         */
        public Color getColorObject() {
            try {
                return Color.decode(color);
            } catch (NumberFormatException e) {
                // Default to yellow if color parsing fails
                return Color.YELLOW;
            }
        }
    }
    
    private final FindHighlightConfig find = new FindHighlightConfig();
    private final SearchRegionHighlightConfig searchRegion = new SearchRegionHighlightConfig();
    private final ErrorHighlightConfig error = new ErrorHighlightConfig();
    private final ClickHighlightConfig click = new ClickHighlightConfig();
}