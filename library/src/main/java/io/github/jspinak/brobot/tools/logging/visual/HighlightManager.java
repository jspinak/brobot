package io.github.jspinak.brobot.tools.logging.visual;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.model.element.basic.match.Match;
import io.github.jspinak.brobot.model.element.basic.region.Region;
import io.github.jspinak.brobot.logging.unified.BrobotLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sikuli.script.support.ScreenHighlighter;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages visual highlighting of screen regions during automation execution.
 * Provides configurable highlighting for finds, search regions, clicks, and errors.
 * 
 * <p>Features:</p>
 * <ul>
 *   <li>Configurable colors and durations for different highlight types</li>
 *   <li>Support for flashing and ripple effects</li>
 *   <li>Asynchronous highlighting to avoid blocking automation</li>
 *   <li>Mock mode support for testing</li>
 * </ul>
 * 
 * @see VisualFeedbackConfig for configuration options
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HighlightManager {
    
    private final VisualFeedbackConfig config;
    private final BrobotLogger brobotLogger;
    
    // Keep track of active highlights for cleanup
    private final List<ScreenHighlighter> activeHighlights = new ArrayList<>();
    
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
            if (region == null) continue;
            
            highlightRegion(
                region,
                findConfig.getColorObject(),
                findConfig.getDuration(),
                findConfig.getBorderWidth(),
                "FIND",
                match.getScore()
            );
            
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
            return;
        }
        
        VisualFeedbackConfig.SearchRegionHighlightConfig searchConfig = config.getSearchRegion();
        
        for (Region region : regions) {
            if (region == null) continue;
            
            highlightRegion(
                region,
                searchConfig.getColorObject(),
                searchConfig.getDuration(),
                searchConfig.getBorderWidth(),
                "SEARCH_REGION",
                null
            );
            
            if (searchConfig.isShowDimensions()) {
                showRegionDimensions(region);
            }
        }
        
        brobotLogger.log()
            .observation("Highlighted search regions")
            .metadata("regionCount", regions.size())
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
            clickConfig.getRadius() * 2
        );
        
        if (clickConfig.isRippleEffect()) {
            showRippleEffect(x, y, clickConfig);
        } else {
            highlightRegion(
                clickRegion,
                clickConfig.getColorObject(),
                clickConfig.getDuration(),
                2,
                "CLICK",
                null
            );
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
            null
        );
        
        if (errorConfig.isShowCrossMark()) {
            showCrossMark(searchRegion, errorConfig);
        }
    }
    
    /**
     * Highlights a single region with specified parameters.
     */
    private void highlightRegion(Region region, Color color, double duration, 
                                int borderWidth, String type, Double score) {
        if (FrameworkSettings.isMockBuild()) {
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
            CompletableFuture.runAsync(() -> {
                try {
                    // Convert Brobot Region to Sikuli Region
                    org.sikuli.script.Region sikuliRegion = convertToSikuliRegion(region);
                    
                    // Create and show highlight
                    ScreenHighlighter highlighter = new ScreenHighlighter();
                    highlighter.highlight(sikuliRegion, (float) duration, color.toString());
                    
                    synchronized (activeHighlights) {
                        activeHighlights.add(highlighter);
                    }
                    
                    // Schedule cleanup
                    CompletableFuture.delayedExecutor((long)(duration * 1000), TimeUnit.MILLISECONDS)
                        .execute(() -> {
                            synchronized (activeHighlights) {
                                activeHighlights.remove(highlighter);
                            }
                        });
                        
                } catch (Exception e) {
                    log.warn("Failed to highlight region: {}", e.getMessage());
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
                long stepDuration = (long)(config.getDuration() * 1000) / steps;
                
                for (int i = 1; i <= steps; i++) {
                    int radius = (maxRadius * i) / steps;
                    int alpha = 255 - (200 * i / steps); // Fade out
                    
                    Color color = new Color(
                        config.getColorObject().getRed(),
                        config.getColorObject().getGreen(),
                        config.getColorObject().getBlue(),
                        alpha
                    );
                    
                    Region rippleRegion = new Region(
                        x - radius, y - radius, radius * 2, radius * 2
                    );
                    
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
     * Converts a Brobot Region to a Sikuli Region.
     */
    private org.sikuli.script.Region convertToSikuliRegion(Region brobotRegion) {
        return new org.sikuli.script.Region(
            brobotRegion.x(),
            brobotRegion.y(),
            brobotRegion.w(),
            brobotRegion.h()
        );
    }
    
    /**
     * Clears all active highlights immediately.
     */
    public void clearAllHighlights() {
        synchronized (activeHighlights) {
            for (ScreenHighlighter highlighter : activeHighlights) {
                try {
                    highlighter.close();
                } catch (Exception e) {
                    log.warn("Error closing highlighter: {}", e.getMessage());
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
}