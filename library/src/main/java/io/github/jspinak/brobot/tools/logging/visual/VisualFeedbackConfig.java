package io.github.jspinak.brobot.tools.logging.visual;

import java.awt.*;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * Configuration for visual feedback during automation execution. Controls highlighting of found
 * images and search regions.
 *
 * <p>This configuration can be set via properties files using the prefix "brobot.highlight". For
 * example:
 *
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
@ConfigurationProperties(prefix = "brobot.highlight")
public class VisualFeedbackConfig {

    /** Whether visual highlighting is enabled globally. */
    private boolean enabled;

    /** Whether to automatically highlight successful finds. */
    private boolean autoHighlightFinds;

    /** Whether to automatically highlight search regions before searching. */
    private boolean autoHighlightSearchRegions;

    /** Configuration for find highlighting. */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.find")
    public static class FindHighlightConfig {
        /** Color for highlighting found images. */
        private String color;

        /** Duration in seconds to show the highlight. */
        private double duration;

        /** Border width in pixels. */
        private int borderWidth;

        /** Whether to flash the highlight. */
        private boolean flash;

        /** Number of times to flash if enabled. */
        private int flashCount;

        /** Flash interval in milliseconds. */
        private long flashInterval;

        /** Converts the color string to a Color object. */
        public Color getColorObject() {
            try {
                return Color.decode(color);
            } catch (NumberFormatException e) {
                // Default to green if color parsing fails
                return Color.GREEN;
            }
        }
    }

    /** Configuration for search region highlighting. */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.search-region")
    public static class SearchRegionHighlightConfig {
        /** Color for highlighting search regions. */
        private String color;

        /** Duration in seconds to show the highlight. */
        private double duration;

        /** Border width in pixels. */
        private int borderWidth;

        /** Opacity for filled highlights (0.0 to 1.0). */
        private double opacity;

        /** Whether to fill the region or just show border. */
        private boolean filled;

        /** Whether to show region dimensions as text. */
        private boolean showDimensions;

        /** Converts the color string to a Color object. */
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

    /** Configuration for error highlighting. */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.error")
    public static class ErrorHighlightConfig {
        /** Whether to highlight areas where finds failed. */
        private boolean enabled;

        /** Color for error highlights. */
        private String color;

        /** Duration in seconds to show the error highlight. */
        private double duration;

        /** Whether to show a cross mark on failed areas. */
        private boolean showCrossMark;

        /** Converts the color string to a Color object. */
        public Color getColorObject() {
            try {
                return Color.decode(color);
            } catch (NumberFormatException e) {
                // Default to red if color parsing fails
                return Color.RED;
            }
        }
    }

    /** Configuration for click highlighting. */
    @Data
    @ConfigurationProperties(prefix = "brobot.highlight.click")
    public static class ClickHighlightConfig {
        /** Whether to highlight click locations. */
        private boolean enabled;

        /** Color for click highlights. */
        private String color;

        /** Duration in seconds to show the click highlight. */
        private double duration;

        /** Radius of the click indicator circle. */
        private int radius;

        /** Whether to show ripple effect. */
        private boolean rippleEffect;

        /** Converts the color string to a Color object. */
        public Color getColorObject() {
            try {
                return Color.decode(color);
            } catch (NumberFormatException e) {
                // Default to yellow if color parsing fails
                return Color.YELLOW;
            }
        }
    }

    private FindHighlightConfig find = new FindHighlightConfig();
    private SearchRegionHighlightConfig searchRegion = new SearchRegionHighlightConfig();
    private ErrorHighlightConfig error = new ErrorHighlightConfig();
    private ClickHighlightConfig click = new ClickHighlightConfig();
}
