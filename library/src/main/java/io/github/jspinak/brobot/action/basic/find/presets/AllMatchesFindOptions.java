package io.github.jspinak.brobot.action.basic.find.presets;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.MatchFusionOptions;
import lombok.Getter;

/**
 * Preset configuration for finding all occurrences of a pattern on screen.
 * 
 * <p>This preset is ideal for scenarios where:
 * <ul>
 *   <li>You need to find all instances of repeating elements (lists, grids, toolbars)</li>
 *   <li>You're counting occurrences of an element</li>
 *   <li>You need to process multiple similar items</li>
 *   <li>You're working with dynamic content where element count varies</li>
 * </ul>
 * </p>
 * 
 * <p>Default settings:
 * <ul>
 *   <li>Strategy: ALL (finds all matches across the screen)</li>
 *   <li>Similarity: 0.8 (balanced threshold for reliable detection)</li>
 *   <li>Capture Image: false (disabled by default for performance with many matches)</li>
 *   <li>Match Fusion: enabled to combine adjacent matches</li>
 *   <li>Max Matches: unlimited (-1)</li>
 * </ul>
 * </p>
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Find all buttons in a toolbar
 * PatternFindOptions allButtons = AllMatchesFindOptions.create();
 * 
 * // Find all items in a list with limited results
 * PatternFindOptions limitedMatches = AllMatchesFindOptions.builder()
 *     .setMaxMatchesToActOn(10) // Process only first 10 matches
 *     .build();
 * 
 * // Find all checkboxes with higher precision
 * PatternFindOptions preciseAll = AllMatchesFindOptions.builder()
 *     .setSimilarity(0.9)
 *     .setCaptureImage(true) // Enable for debugging
 *     .build();
 * }</pre>
 * </p>
 * 
 * @since 1.1.0
 * @see PatternFindOptions
 * @see QuickFindOptions
 * @see PreciseFindOptions
 */
@Getter
public final class AllMatchesFindOptions {
    
    /**
     * The default similarity threshold for finding all matches.
     * Balanced to avoid false positives while catching variations.
     */
    public static final double DEFAULT_SIMILARITY = 0.8;
    
    /**
     * The default maximum distance for match fusion.
     * Appropriate for combining adjacent UI elements.
     */
    public static final int DEFAULT_FUSION_DISTANCE = 20;
    
    /**
     * Creates an AllMatchesFindOptions instance with default settings.
     * 
     * @return A PatternFindOptions configured for finding all matches
     */
    public static PatternFindOptions create() {
        return builder().build();
    }
    
    /**
     * Creates a pre-configured builder for AllMatchesFindOptions.
     * 
     * <p>The builder starts with optimized defaults for finding multiple matches
     * but allows customization of any setting if needed.</p>
     * 
     * @return A PatternFindOptions.Builder with all matches defaults
     */
    public static PatternFindOptions.Builder builder() {
        return new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .setSimilarity(DEFAULT_SIMILARITY)
            .setCaptureImage(false)
            .setMaxMatchesToActOn(-1) // No limit
            .setMatchFusion(MatchFusionOptions.builder()
                .fusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .maxFusionDistanceX(DEFAULT_FUSION_DISTANCE)
                .maxFusionDistanceY(DEFAULT_FUSION_DISTANCE)
                .build());
    }
    
    /**
     * Private constructor to prevent instantiation.
     * This class provides only static factory methods.
     */
    private AllMatchesFindOptions() {
        throw new UnsupportedOperationException("Utility class");
    }
}