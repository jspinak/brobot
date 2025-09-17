package io.github.jspinak.brobot.action.basic.find;

/**
 * Defines the various strategies available for find operations in Brobot.
 *
 * <p>This enum consolidates all find strategies that were previously scattered across different
 * options classes. It provides a unified type system for find operations while maintaining backward
 * compatibility with the original ActionConfig.Find enum.
 *
 * <p>Each strategy represents a different approach to finding elements on the screen, from basic
 * pattern matching to advanced motion detection and color analysis.
 *
 * @since 2.0
 */
public enum FindStrategy {

    // Pattern-based strategies (used by PatternFindOptions)
    /**
     * Returns the first match found. Stops searching once any Pattern finds a match, making it
     * efficient for existence checks.
     */
    FIRST,

    /**
     * Returns one match per Image object. The DoOnEach option in PatternFindOptions determines
     * whether to return the first or best match per Image.
     */
    EACH,

    /**
     * Returns all matches for all Patterns across all Images. Useful for counting or processing
     * multiple instances of an element.
     */
    ALL,

    /** Performs an ALL search then returns only the match with the highest similarity score. */
    BEST,

    // Special strategies
    /**
     * Used for mocking. Initializing an Image with a UNIVERSAL Find allows it to be accessed by any
     * find operation type.
     */
    UNIVERSAL,

    /** User-defined find strategy. Must be registered with FindStrategyRegistry before use. */
    CUSTOM,

    // Color-based strategies (used by ColorFindOptions)
    /**
     * Finds regions based on color analysis using k-means clustering, mean color statistics, or
     * classification.
     */
    COLOR,

    // Histogram-based strategies (used by HistogramFindOptions)
    /** Matches regions based on histogram similarity from the input images. */
    HISTOGRAM,

    // Motion-based strategies (used by MotionFindOptions)
    /** Finds the locations of a moving object across consecutive screens. */
    MOTION,

    /** Finds all dynamic pixel regions from a series of screens. */
    REGIONS_OF_MOTION,

    /**
     * Returns a mask of all pixels that remain unchanged and a corresponding Match list from the
     * contours.
     */
    FIXED_PIXELS,

    /**
     * Returns a mask of all pixels that have changed and a corresponding Match list from the
     * contours.
     */
    DYNAMIC_PIXELS,

    // Text-based strategies
    /**
     * Finds all words and their regions. Each word is returned as a separate Match object. For
     * finding all text in a specific region as one Match, use a normal Find operation.
     */
    ALL_WORDS,

    // Image comparison strategies
    /**
     * Finds images in the second ObjectCollection that are above a similarity threshold to images
     * in the first ObjectCollection.
     */
    SIMILAR_IMAGES,

    // State analysis strategies
    /**
     * Analyzes ObjectCollections containing screen images and screenshots to produce states with
     * StateImage objects. Returns Match objects holding the state owner's name and Pattern.
     */
    STATES
}
