package io.github.jspinak.brobot.action.basic.find.presets;

import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import lombok.Getter;

/**
 * Preset configuration for quick pattern matching operations optimized for speed.
 *
 * <p>This preset is ideal for scenarios where:
 *
 * <ul>
 *   <li>You need to quickly verify if an element exists on screen
 *   <li>Performance is more important than precision
 *   <li>The target element has distinct visual characteristics
 *   <li>You only need the first occurrence of an element
 * </ul>
 *
 * <p>Default settings:
 *
 * <ul>
 *   <li>Strategy: FIRST (stops after finding one match)
 *   <li>Similarity: 0.7 (allows more variation for faster matching)
 *   <li>Capture Image: false (skips screenshot capture for performance)
 *   <li>Max Matches: 1 (only processes the first match)
 * </ul>
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using the preset directly
 * PatternFindOptions quickOptions = QuickFindOptions.create();
 *
 * // Customizing the preset
 * PatternFindOptions customQuick = QuickFindOptions.builder()
 *     .setSimilarity(0.8) // Override default similarity
 *     .build();
 * }</pre>
 *
 * @since 1.1.0
 * @see PatternFindOptions
 * @see PreciseFindOptions
 */
@Getter
public final class QuickFindOptions {

    /**
     * The default similarity threshold for quick finds. Lower than standard to prioritize speed
     * over precision.
     */
    public static final double DEFAULT_SIMILARITY = 0.7;

    /**
     * Creates a QuickFindOptions instance with default settings.
     *
     * @return A PatternFindOptions configured for quick pattern matching
     */
    public static PatternFindOptions create() {
        return builder().build();
    }

    /**
     * Creates a pre-configured builder for QuickFindOptions.
     *
     * <p>The builder starts with optimized defaults but allows customization of any setting if
     * needed.
     *
     * @return A PatternFindOptions.Builder with quick find defaults
     */
    public static PatternFindOptions.Builder builder() {
        return new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(DEFAULT_SIMILARITY)
                .setCaptureImage(false)
                .setMaxMatchesToActOn(1);
    }

    /**
     * Private constructor to prevent instantiation. This class provides only static factory
     * methods.
     */
    private QuickFindOptions() {
        throw new UnsupportedOperationException("Utility class");
    }
}
