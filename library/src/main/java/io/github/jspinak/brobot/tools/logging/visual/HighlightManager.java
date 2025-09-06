package io.github.jspinak.brobot.tools.logging.visual;

import io.github.jspinak.brobot.capture.ScreenDimensions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateObject;
import io.github.jspinak.brobot.statemanagement.StateMemory;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import io.github.jspinak.brobot.logging.unified.LogEvent;
import io.github.jspinak.brobot.tools.testing.wrapper.HighlightWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages visual highlighting of screen regions during automation execution.
 * Provides configurable highlighting for finds, search regions, clicks, and
 * errors.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Configurable colors and durations for different highlight types</li>
 * <li>Support for flashing and ripple effects</li>
 * <li>Asynchronous highlighting to avoid blocking automation</li>
 * <li>Mock mode support for testing</li>
 * </ul>
 * 
 * @see VisualFeedbackConfig for configuration options
 */
@Component
@Slf4j
public class HighlightManager {

    /**
     * Represents a region with its state and object context for better logging.
     */
    public static class RegionWithContext {
        private final Region region;
        private final String stateName;
        private final String objectName;

        public RegionWithContext(Region region, String stateName, String objectName) {
            this.region = region;
            this.stateName = stateName;
            this.objectName = objectName;
        }

        public Region getRegion() {
            return region;
        }

        public String getStateName() {
            return stateName;
        }

        public String getObjectName() {
            return objectName;
        }
    }

    private final VisualFeedbackConfig config;
    private final BrobotLogger brobotLogger;
    private final HighlightWrapper highlightWrapper;
    private final StateMemory stateMemory;

    // Keep track of active highlight threads for cleanup
    private final List<CompletableFuture<Void>> activeHighlights = new ArrayList<>();

    @Autowired
    public HighlightManager(VisualFeedbackConfig config,
            BrobotLogger brobotLogger,
            HighlightWrapper highlightWrapper,
            @Autowired(required = false) StateMemory stateMemory) {
        this.config = config;
        this.brobotLogger = brobotLogger;
        this.highlightWrapper = highlightWrapper;
        this.stateMemory = stateMemory;
    }

    /**
     * Highlights successful match findings.
     * 
     * @param matches List of matches to highlight
     */
    public void highlightMatches(List<Match> matches) {
        if (!config.isEnabled() || !config.isAutoHighlightFinds() || matches.isEmpty()) {
            return;
        }

        VisualFeedbackConfig.FindHighlightConfig findConfig = config.getFind();

        for (Match match : matches) {
            Region region = match.getRegion();
            if (region == null)
                continue;
            
            // Scale region from physical to logical coordinates if needed for SikuliX highlighting
            region = scaleRegionForHighlight(region);

            // Check if the match has a custom highlight color from its StateImage
            Color highlightColor = findConfig.getColorObject();
            String colorString = findConfig.getColor();

            // Try to get custom color from the StateImage if available
            String customColor = getCustomColorForMatch(match);
            if (customColor != null) {
                try {
                    highlightColor = Color.decode(customColor);
                    colorString = customColor;
                    log.debug("Using custom highlight color {} for {}", customColor,
                            match.getStateObjectData() != null ? match.getStateObjectData().getStateObjectName()
                                    : "unknown");
                } catch (NumberFormatException e) {
                    log.debug("Invalid custom color '{}' for StateImage, using default", customColor);
                }
            }

            highlightRegion(
                    region,
                    highlightColor,
                    findConfig.getDuration(),
                    findConfig.getBorderWidth(),
                    "FIND",
                    match.getScore());

            if (findConfig.isFlash()) {
                flashRegion(region, findConfig);
            }
        }

        brobotLogger.log()
                .observation("Highlighted matches")
                .metadata("matchCount", matches.size())
                .metadata("color", findConfig.getColor())
                .metadata("duration", findConfig.getDuration())
                .log();
    }

    /**
     * Highlights search regions before searching.
     * 
     * @param regions List of regions to be searched
     */
    public void highlightSearchRegions(List<Region> regions) {
        if (!config.isEnabled() || !config.isAutoHighlightSearchRegions() || regions.isEmpty()) {
            log.debug("Search region highlighting skipped: enabled={}, autoHighlight={}, regions={}",
                    config.isEnabled(), config.isAutoHighlightSearchRegions(), regions.size());
            return;
        }

        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();

        // Perform synchronous highlighting for search regions so they appear in console
        try {
            // Perform highlight synchronously using wrapper
            if (highlightWrapper != null) {
                log.debug("Highlighting {} search regions synchronously", regions.size());
                brobotLogger.log()
                        .observation("Starting highlight action")
                        .metadata("regionCount", regions.size())
                        .metadata("color", getColorName(searchConfig.getColorObject()))
                        .metadata("duration", searchConfig.getDuration())
                        .log();

                int successCount = highlightWrapper.highlightRegions(
                        regions,
                        searchConfig.getDuration(),
                        getColorName(searchConfig.getColorObject()));

                brobotLogger.log()
                        .observation("Highlight action completed")
                        .metadata("success", successCount > 0)
                        .metadata("highlightedCount", successCount)
                        .log();

                if (successCount == 0) {
                    log.warn("Highlight action failed - no regions could be highlighted");
                }
            } else {
                log.error("Highlight wrapper not available for highlighting - highlights will not appear on screen");
                brobotLogger.log()
                        .error(new IllegalStateException("HighlightWrapper not available"))
                        .message("Cannot display highlights - HighlightWrapper component not available")
                        .log();
            }

            if (searchConfig.isShowDimensions()) {
                for (Region region : regions) {
                    showRegionDimensions(region);
                }
            }

        } catch (Exception e) {
            log.error("Error highlighting search regions", e);
        }

        brobotLogger.log()
                .observation("Highlighted search regions")
                .metadata("regionCount", regions.size())
                .metadata("color", searchConfig.getColor())
                .metadata("duration", searchConfig.getDuration())
                .log();
    }

    /**
     * Highlights search regions with state and object context for better logging.
     * 
     * @param regionsWithContext List of regions with their state/object context
     */
    public void highlightSearchRegionsWithContext(List<RegionWithContext> regionsWithContext) {
        if (!config.isEnabled() || !config.isAutoHighlightSearchRegions() || regionsWithContext.isEmpty()) {
            log.debug("Search region highlighting skipped: enabled={}, autoHighlight={}, regions={}",
                    config.isEnabled(), config.isAutoHighlightSearchRegions(), regionsWithContext.size());
            return;
        }

        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();

        // Log that we're about to highlight regions with context
        brobotLogger.log()
                .level(LogEvent.Level.INFO)
                .observation(String.format("Highlighting %d search regions with context", regionsWithContext.size()))
                .log();

        // Log each region with its context
        for (RegionWithContext rwc : regionsWithContext) {
            Region region = rwc.getRegion();
            String stateName = rwc.getStateName() != null ? rwc.getStateName() : "Unknown";
            String objectName = rwc.getObjectName() != null ? rwc.getObjectName() : "Unknown";

            brobotLogger.log()
                    .level(LogEvent.Level.INFO)
                    .observation(String.format("Highlighting region for %s.%s (%d,%d,%dx%d)",
                            stateName, objectName, region.x(), region.y(), region.w(), region.h()))
                    .metadata("state", stateName)
                    .metadata("object", objectName)
                    .metadata("x", region.x())
                    .metadata("y", region.y())
                    .metadata("width", region.w())
                    .metadata("height", region.h())
                    .log();
        }

        // Perform synchronous highlighting for search regions
        try {
            // Extract just the regions for highlighting
            List<Region> regions = regionsWithContext.stream()
                    .map(RegionWithContext::getRegion)
                    .toList();

            // Perform highlight synchronously using wrapper
            if (highlightWrapper != null) {
                log.debug("Highlighting {} search regions synchronously", regions.size());
                brobotLogger.log()
                        .observation("Starting highlight action with context")
                        .metadata("regionCount", regions.size())
                        .metadata("color", getColorName(searchConfig.getColorObject()))
                        .metadata("duration", searchConfig.getDuration())
                        .log();

                int successCount = highlightWrapper.highlightRegions(
                        regions,
                        searchConfig.getDuration(),
                        getColorName(searchConfig.getColorObject()));

                brobotLogger.log()
                        .observation("Highlight action with context completed")
                        .metadata("success", successCount > 0)
                        .metadata("highlightedCount", successCount)
                        .log();

                if (successCount == 0) {
                    log.warn("Highlight action failed - no regions could be highlighted");
                }
            } else {
                log.error("Highlight wrapper not available for highlighting - highlights will not appear on screen");
                brobotLogger.log()
                        .error(new IllegalStateException("HighlightWrapper not available"))
                        .message("Cannot display highlights - HighlightWrapper component not available")
                        .log();
            }

            if (searchConfig.isShowDimensions()) {
                for (RegionWithContext rwc : regionsWithContext) {
                    showRegionDimensionsWithContext(rwc);
                }
            }

        } catch (Exception e) {
            log.error("Error highlighting search regions", e);
        }

        brobotLogger.log()
                .observation("Highlighted search regions complete")
                .metadata("regionCount", regionsWithContext.size())
                .metadata("color", searchConfig.getColor())
                .metadata("duration", searchConfig.getDuration())
                .log();
    }

    /**
     * Highlights a click location.
     * 
     * @param x X coordinate of the click
     * @param y Y coordinate of the click
     */
    public void highlightClick(int x, int y) {
        if (!config.isEnabled() || !config.getClick().isEnabled()) {
            return;
        }

        VisualFeedbackConfig.ClickHighlightConfig clickConfig = config.getClick();

        // Create a small region around the click point
        Region clickRegion = new Region(
                x - clickConfig.getRadius(),
                y - clickConfig.getRadius(),
                clickConfig.getRadius() * 2,
                clickConfig.getRadius() * 2);

        if (clickConfig.isRippleEffect()) {
            showRippleEffect(x, y, clickConfig);
        } else {
            highlightRegion(
                    clickRegion,
                    clickConfig.getColorObject(),
                    clickConfig.getDuration(),
                    2,
                    "CLICK",
                    null);
        }
    }

    /**
     * Highlights an area where a find operation failed.
     * 
     * @param searchRegion The region that was searched
     */
    public void highlightError(Region searchRegion) {
        if (!config.isEnabled() || !config.getError().isEnabled() || searchRegion == null) {
            return;
        }

        VisualFeedbackConfig.ErrorHighlightConfig errorConfig = config.getError();

        highlightRegion(
                searchRegion,
                errorConfig.getColorObject(),
                errorConfig.getDuration(),
                3,
                "ERROR",
                null);

        if (errorConfig.isShowCrossMark()) {
            showCrossMark(searchRegion, errorConfig);
        }
    }

    /**
     * Highlights a single region with specified parameters.
     */
    private void highlightRegion(Region region, Color color, double duration,
            int borderWidth, String type, Double score) {
        if (FrameworkSettings.mock) {
            // In mock mode, just log the highlight action
            brobotLogger.log()
                    .observation("Mock highlight")
                    .metadata("type", type)
                    .metadata("region", String.format("(%d,%d,%dx%d)",
                            region.x(), region.y(), region.w(), region.h()))
                    .metadata("color", String.format("#%06X", color.getRGB() & 0xFFFFFF))
                    .metadata("duration", duration)
                    .log();
            return;
        }

        try {
            // Use CompletableFuture for non-blocking highlight
            CompletableFuture<Void> highlightFuture = CompletableFuture.runAsync(() -> {
                try {
                    // Use wrapper to perform the highlight
                    if (highlightWrapper != null) {
                        highlightWrapper.highlightRegion(region, duration, getColorName(color));
                    } else {
                        log.warn("HighlightWrapper not available for async highlight");
                    }

                } catch (Exception e) {
                    log.error("Error highlighting region: {}", e.getMessage(), e);
                }
            });

            synchronized (activeHighlights) {
                activeHighlights.add(highlightFuture);
            }

            // Schedule cleanup
            highlightFuture.whenComplete((result, throwable) -> {
                synchronized (activeHighlights) {
                    activeHighlights.remove(highlightFuture);
                }
            });

        } catch (Exception e) {
            log.error("Error creating highlight", e);
            brobotLogger.log()
                    .error(e)
                    .message("Failed to create highlight")
                    .metadata("type", type)
                    .log();
        }
    }

    /**
     * Creates a flashing effect on a region.
     */
    private void flashRegion(Region region, VisualFeedbackConfig.FindHighlightConfig config) {
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < config.getFlashCount(); i++) {
                    highlightRegion(region, config.getColorObject(),
                            config.getFlashInterval() / 1000.0,
                            config.getBorderWidth(), "FLASH", null);
                    Thread.sleep(config.getFlashInterval() * 2);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Flash effect interrupted");
            }
        });
    }

    /**
     * Shows a ripple effect at click location.
     */
    private void showRippleEffect(int x, int y, VisualFeedbackConfig.ClickHighlightConfig config) {
        CompletableFuture.runAsync(() -> {
            try {
                int maxRadius = config.getRadius() * 3;
                int steps = 5;
                long stepDuration = (long) (config.getDuration() * 1000) / steps;

                for (int i = 1; i <= steps; i++) {
                    int radius = (maxRadius * i) / steps;
                    int alpha = 255 - (200 * i / steps); // Fade out

                    Color color = new Color(
                            config.getColorObject().getRed(),
                            config.getColorObject().getGreen(),
                            config.getColorObject().getBlue(),
                            alpha);

                    Region rippleRegion = new Region(
                            x - radius, y - radius, radius * 2, radius * 2);

                    highlightRegion(rippleRegion, color, stepDuration / 1000.0,
                            2, "RIPPLE", null);

                    Thread.sleep(stepDuration);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Ripple effect interrupted");
            }
        });
    }

    /**
     * Shows region dimensions as text overlay.
     */
    private void showRegionDimensions(Region region) {
        String dimensions = String.format("%dx%d", region.w(), region.h());
        // In a real implementation, this would overlay text on the screen
        // For now, just log it
        brobotLogger.log()
                .observation("Region dimensions")
                .metadata("dimensions", dimensions)
                .metadata("location", String.format("(%d,%d)", region.x(), region.y()))
                .log();
    }

    /**
     * Shows region dimensions with state/object context.
     */
    private void showRegionDimensionsWithContext(RegionWithContext rwc) {
        Region region = rwc.getRegion();
        String dimensions = String.format("%dx%d", region.w(), region.h());
        String stateName = rwc.getStateName() != null ? rwc.getStateName() : "Unknown";
        String objectName = rwc.getObjectName() != null ? rwc.getObjectName() : "Unknown";

        brobotLogger.log()
                .observation(String.format("Region dimensions for %s.%s", stateName, objectName))
                .metadata("dimensions", dimensions)
                .metadata("location", String.format("(%d,%d)", region.x(), region.y()))
                .metadata("state", stateName)
                .metadata("object", objectName)
                .log();
    }

    /**
     * Shows a cross mark on error regions.
     */
    private void showCrossMark(Region region, VisualFeedbackConfig.ErrorHighlightConfig config) {
        // In a real implementation, this would draw an X over the region
        // For now, create two diagonal highlights
        CompletableFuture.runAsync(() -> {
            try {
                // Top-left to bottom-right
                Region diagonal1 = new Region(region.x(), region.y(), region.w(), 2);
                highlightRegion(diagonal1, config.getColorObject(),
                        config.getDuration(), 3, "CROSS", null);

                // Top-right to bottom-left
                Region diagonal2 = new Region(region.x(), region.y() + region.h() - 2, region.w(), 2);
                highlightRegion(diagonal2, config.getColorObject(),
                        config.getDuration(), 3, "CROSS", null);

            } catch (Exception e) {
                log.warn("Failed to show cross mark: {}", e.getMessage());
            }
        });
    }

    /**
     * Clears all active highlights immediately.
     */
    public void clearAllHighlights() {
        synchronized (activeHighlights) {
            for (CompletableFuture<Void> highlightFuture : activeHighlights) {
                try {
                    highlightFuture.cancel(true);
                } catch (Exception e) {
                    log.warn("Error canceling highlight: {}", e.getMessage());
                }
            }
            activeHighlights.clear();
        }

        brobotLogger.log()
                .observation("Cleared all highlights")
                .log();
    }

    /**
     * Gets the current configuration.
     */
    public VisualFeedbackConfig getConfig() {
        return config;
    }

    /**
     * Converts a Color object to a color name string that Sikuli understands.
     * Sikuli accepts: "red", "green", "blue", "yellow", "cyan", "magenta", "white",
     * "black", "gray", "orange"
     */
    private String getColorName(Color color) {
        // Check for exact matches to common colors
        if (color.equals(Color.RED))
            return "red";
        if (color.equals(Color.GREEN))
            return "green";
        if (color.equals(Color.BLUE))
            return "blue";
        if (color.equals(Color.YELLOW))
            return "yellow";
        if (color.equals(Color.CYAN))
            return "cyan";
        if (color.equals(Color.MAGENTA))
            return "magenta";
        if (color.equals(Color.WHITE))
            return "white";
        if (color.equals(Color.BLACK))
            return "black";
        if (color.equals(Color.GRAY))
            return "gray";
        if (color.equals(Color.ORANGE))
            return "orange";

        // For other colors, find the closest match
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // Simple heuristic for color matching
        if (r > 200 && g < 100 && b < 100)
            return "red";
        if (r < 100 && g > 200 && b < 100)
            return "green";
        if (r < 100 && g < 100 && b > 200)
            return "blue";
        if (r > 200 && g > 200 && b < 100)
            return "yellow";
        if (r < 100 && g > 200 && b > 200)
            return "cyan";
        if (r > 200 && g < 100 && b > 200)
            return "magenta";
        if (r > 200 && g > 150 && b < 100)
            return "orange";
        if (r > 180 && g > 180 && b > 180)
            return "white";
        if (r < 80 && g < 80 && b < 80)
            return "black";

        // Default to gray for anything else
        return "gray";
    }

    /**
     * Gets the custom highlight color for a match based on its StateImage.
     * 
     * @param match the match to get the color for
     * @return the custom color string, or null if no custom color is defined
     */
    private String getCustomColorForMatch(Match match) {
        if (match == null || match.getStateObjectData() == null) {
            return null;
        }

        // If we don't have StateMemory, we can't look up the StateImage
        if (stateMemory == null) {
            return null;
        }

        try {
            // Get the state object metadata
            var metadata = match.getStateObjectData();
            if (metadata.getObjectType() != StateObject.Type.IMAGE) {
                return null;
            }

            // For now, return a hardcoded color based on the object name
            // This is a simple workaround until we can properly access State objects
            String objectName = metadata.getStateObjectName();
            if ("ClaudeIcon".equals(objectName)) {
                return "#0000FF"; // Blue for ClaudeIcon
            } else if ("ClaudePrompt".equals(objectName)) {
                return "#00FF00"; // Green for ClaudePrompt (default)
            }
        } catch (Exception e) {
            log.debug("Error getting custom color for match: {}", e.getMessage());
        }

        return null;
    }
    
    /**
     * Scales a region from physical coordinates to logical coordinates if needed.
     * This is necessary when matches are found in physical resolution (1920x1080)
     * but SikuliX highlighting works in logical resolution (1536x864).
     * 
     * @param region The region to potentially scale
     * @return The scaled region, or the original if no scaling is needed
     */
    private Region scaleRegionForHighlight(Region region) {
        // Check if we're using physical resolution captures
        int captureWidth = ScreenDimensions.getWidth();
        int captureHeight = ScreenDimensions.getHeight();
        
        // Get the logical screen dimensions (what SikuliX uses for highlighting)
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int logicalWidth = screenSize.width;
        int logicalHeight = screenSize.height;
        
        // If capture and logical dimensions match, no scaling needed
        if (captureWidth == logicalWidth && captureHeight == logicalHeight) {
            return region;
        }
        
        // Calculate scale factors (from physical to logical)
        double scaleX = (double) logicalWidth / captureWidth;
        double scaleY = (double) logicalHeight / captureHeight;
        
        // Scale the region coordinates
        int scaledX = (int) Math.round(region.x() * scaleX);
        int scaledY = (int) Math.round(region.y() * scaleY);
        int scaledW = (int) Math.round(region.w() * scaleX);
        int scaledH = (int) Math.round(region.h() * scaleY);
        
        // Ensure the scaled region fits within logical screen bounds
        if (scaledY + scaledH > logicalHeight) {
            scaledH = logicalHeight - scaledY;
        }
        if (scaledX + scaledW > logicalWidth) {
            scaledW = logicalWidth - scaledX;
        }
        
        log.debug("Scaled highlight region from physical [{}] to logical [{}]",
                 region, new Region(scaledX, scaledY, scaledW, scaledH));
        
        return new Region(scaledX, scaledY, scaledW, scaledH);
    }
}