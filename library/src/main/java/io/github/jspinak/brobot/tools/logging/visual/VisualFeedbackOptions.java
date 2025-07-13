package io.github.jspinak.brobot.tools.logging.visual;

import lombok.Builder;
import lombok.Data;

import java.awt.Color;

/**
 * Options for visual feedback during action execution.
 * Allows fine-grained control over highlighting behavior for individual actions.
 * 
 * <p>This class provides a builder pattern for creating custom visual feedback
 * configurations that can override the global settings for specific actions.</p>
 * 
 * @see HighlightManager for the implementation that uses these options
 * @see VisualFeedbackConfig for global configuration
 */
@Data
@Builder
public class VisualFeedbackOptions {
    
    /**
     * Whether highlighting is enabled for this action.
     * Default: true
     */
    @Builder.Default
    private boolean highlightEnabled = true;
    
    /**
     * Whether to highlight successful finds.
     * Default: true
     */
    @Builder.Default
    private boolean highlightFinds = true;
    
    /**
     * Whether to highlight search regions.
     * Default: false
     */
    @Builder.Default
    private boolean highlightSearchRegions = false;
    
    /**
     * Whether to highlight errors/failures.
     * Default: false
     */
    @Builder.Default
    private boolean highlightErrors = false;
    
    /**
     * Custom color for find highlights (null = use default).
     */
    private Color findHighlightColor;
    
    /**
     * Custom duration for find highlights in seconds (null = use default).
     */
    private Double findHighlightDuration;
    
    /**
     * Custom color for search region highlights (null = use default).
     */
    private Color searchRegionHighlightColor;
    
    /**
     * Custom duration for search region highlights in seconds (null = use default).
     */
    private Double searchRegionHighlightDuration;
    
    /**
     * Whether to use flashing effect for highlights.
     * Default: false
     */
    @Builder.Default
    private boolean flashHighlight = false;
    
    /**
     * Number of times to flash if enabled.
     * Default: 2
     */
    @Builder.Default
    private int flashCount = 2;
    
    /**
     * Whether to show match score as overlay text.
     * Default: false
     */
    @Builder.Default
    private boolean showMatchScore = false;
    
    /**
     * Whether to persist highlights until manually cleared.
     * Default: false
     */
    @Builder.Default
    private boolean persistHighlight = false;
    
    /**
     * Label to show with the highlight (optional).
     */
    private String highlightLabel;
    
    /**
     * Creates default visual feedback options.
     */
    public static VisualFeedbackOptions defaults() {
        return VisualFeedbackOptions.builder().build();
    }
    
    /**
     * Creates visual feedback options with no highlighting.
     */
    public static VisualFeedbackOptions none() {
        return VisualFeedbackOptions.builder()
            .highlightEnabled(false)
            .build();
    }
    
    /**
     * Creates visual feedback options for development/debugging.
     */
    public static VisualFeedbackOptions debug() {
        return VisualFeedbackOptions.builder()
            .highlightEnabled(true)
            .highlightFinds(true)
            .highlightSearchRegions(true)
            .highlightErrors(true)
            .showMatchScore(true)
            .findHighlightDuration(3.0)
            .build();
    }
    
    /**
     * Creates visual feedback options for find operations only.
     */
    public static VisualFeedbackOptions findsOnly() {
        return VisualFeedbackOptions.builder()
            .highlightEnabled(true)
            .highlightFinds(true)
            .highlightSearchRegions(false)
            .highlightErrors(false)
            .build();
    }
    
    /**
     * Merges these options with global configuration.
     * Custom values in this object override global settings.
     * 
     * @param globalConfig The global visual feedback configuration
     * @return Merged configuration with custom overrides
     */
    public VisualFeedbackOptions mergeWithGlobal(VisualFeedbackConfig globalConfig) {
        VisualFeedbackOptions merged = VisualFeedbackOptions.builder()
            .highlightEnabled(this.highlightEnabled && globalConfig.isEnabled())
            .highlightFinds(this.highlightFinds)
            .highlightSearchRegions(this.highlightSearchRegions)
            .highlightErrors(this.highlightErrors)
            .flashHighlight(this.flashHighlight)
            .flashCount(this.flashCount)
            .showMatchScore(this.showMatchScore)
            .persistHighlight(this.persistHighlight)
            .highlightLabel(this.highlightLabel)
            .build();
            
        // Use custom colors/durations if specified, otherwise use global
        if (this.findHighlightColor != null) {
            merged.setFindHighlightColor(this.findHighlightColor);
        } else {
            merged.setFindHighlightColor(globalConfig.getFind().getColorObject());
        }
        
        if (this.findHighlightDuration != null) {
            merged.setFindHighlightDuration(this.findHighlightDuration);
        } else {
            merged.setFindHighlightDuration(globalConfig.getFind().getDuration());
        }
        
        if (this.searchRegionHighlightColor != null) {
            merged.setSearchRegionHighlightColor(this.searchRegionHighlightColor);
        } else {
            merged.setSearchRegionHighlightColor(globalConfig.getSearchRegion().getColorObject());
        }
        
        if (this.searchRegionHighlightDuration != null) {
            merged.setSearchRegionHighlightDuration(this.searchRegionHighlightDuration);
        } else {
            merged.setSearchRegionHighlightDuration(globalConfig.getSearchRegion().getDuration());
        }
        
        return merged;
    }
}