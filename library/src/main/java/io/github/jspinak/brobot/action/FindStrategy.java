package io.github.jspinak.brobot.action;

/**
 * Defines strategies for finding patterns, text, and other elements on screen.
 *
 * <p>Each strategy determines how matches are selected and returned:
 *
 * <ul>
 *   <li>Pattern matching strategies (FIRST, ALL, BEST) for image-based searches
 *   <li>Text recognition strategies (ALL_WORDS) for OCR operations
 *   <li>Analysis strategies (HISTOGRAM, COLOR, MOTION) for advanced detection
 *   <li>State detection strategies for model-based automation
 * </ul>
 *
 * @since 2.0
 */
public enum FindStrategy {
    /** Returns the first match found. Stops searching after finding one match per pattern. */
    FIRST,

    /** Returns one match per image. The DoOnEach option determines specifics. */
    EACH,

    /** Returns all matches for all patterns during the entire search duration. */
    ALL,

    /** Returns the single match with the highest similarity score. */
    BEST,

    /** Mock strategy that allows universal matching for testing. */
    UNIVERSAL,

    /**
     * User-defined custom find strategy. Must provide a BiConsumer<ActionResult,
     * List<ObjectCollection>>.
     */
    CUSTOM,

    /** Matches based on color histogram similarity. */
    HISTOGRAM,

    /** Matches based on color properties. */
    COLOR,

    /** Detects motion by finding moving objects across frames. */
    MOTION,

    /** Identifies all dynamic pixel regions from a series of screenshots. */
    REGIONS_OF_MOTION,

    /** Performs OCR to find all words and their bounding regions. */
    ALL_WORDS,

    /** Finds images in the second collection similar to those in the first. */
    SIMILAR_IMAGES,

    /** Returns a mask of all unchanged pixels and corresponding matches. */
    FIXED_PIXELS,

    /** Returns a mask of all changed pixels and corresponding matches. */
    DYNAMIC_PIXELS,

    /** Analyzes screenshots to identify and construct state objects. */
    STATES
}
