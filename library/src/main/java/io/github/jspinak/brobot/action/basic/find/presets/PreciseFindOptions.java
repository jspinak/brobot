package io.github.jspinak.brobot.action.basic.find.presets;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.MatchFusionOptions;
import lombok.Getter;

/**
 * Preset configuration for precise pattern matching operations optimized for accuracy.
 * 
 * <p>This preset is ideal for scenarios where:
 * <ul>
 *   <li>Accuracy is more important than speed</li>
 *   <li>You need to find the best match among similar elements</li>
 *   <li>The target element has subtle visual differences from similar elements</li>
 *   <li>False positives must be minimized</li>
 * </ul>
 * </p>
 * 
 * <p>Default settings:
 * <ul>
 *   <li>Strategy: BEST (searches all possibilities and returns highest scoring match)</li>
 *   <li>Similarity: 0.9 (high threshold to ensure accurate matches)</li>
 *   <li>Capture Image: true (captures screenshots for verification and debugging)</li>
 *   <li>Match Fusion: enabled with conservative settings</li>
 * </ul>
 * </p>
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Using the preset directly
 * PatternFindOptions preciseOptions = PreciseFindOptions.create();
 * 
 * // Customizing the preset
 * PatternFindOptions customPrecise = PreciseFindOptions.builder()
 *     .setSimilarity(0.95) // Even higher precision
 *     .build();
 * }</pre>
 * </p>
 * 
 * @since 1.1.0
 * @see PatternFindOptions
 * @see QuickFindOptions
 */
@Getter
public final class PreciseFindOptions {
    
    /**
     * The default similarity threshold for precise finds.
     * Higher than standard to ensure accurate matches.
     */
    public static final double DEFAULT_SIMILARITY = 0.9;
    
    /**
     * The default maximum distance for match fusion.
     * Conservative value to avoid merging unrelated matches.
     */
    public static final int DEFAULT_FUSION_DISTANCE = 10;
    
    /**
     * Creates a PreciseFindOptions instance with default settings.
     * 
     * @return A PatternFindOptions configured for precise pattern matching
     */
    public static PatternFindOptions create() {
        return builder().build();
    }
    
    /**
     * Creates a pre-configured builder for PreciseFindOptions.
     * 
     * <p>The builder starts with optimized defaults for precision but allows 
     * customization of any setting if needed.</p>
     * 
     * @return A PatternFindOptions.Builder with precise find defaults
     */
    public static PatternFindOptions.Builder builder() {
        return new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .setSimilarity(DEFAULT_SIMILARITY)
            .setCaptureImage(true)
            .setMatchFusion(MatchFusionOptions.builder()
                .setFusionMethod(MatchFusionOptions.FusionMethod.ABSOLUTE)
                .setMaxFusionDistanceX(DEFAULT_FUSION_DISTANCE)
                .setMaxFusionDistanceY(DEFAULT_FUSION_DISTANCE)
                .build());
    }
    
    /**
     * Private constructor to prevent instantiation.
     * This class provides only static factory methods.
     */
    private PreciseFindOptions() {
        throw new UnsupportedOperationException("Utility class");
    }
}