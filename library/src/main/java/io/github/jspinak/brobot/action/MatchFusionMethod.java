package io.github.jspinak.brobot.action;

/**
 * Defines methods for combining matches based on spatial proximity.
 *
 * <p>Match fusion is primarily used to combine text elements found through OCR operations, allowing
 * separate words to be merged into phrases or sentences based on their relative positions.
 *
 * @since 2.0
 */
public enum MatchFusionMethod {
    /** No match fusion is performed. Default behavior. */
    NONE,

    /** Matches are fused based on absolute pixel distances. */
    ABSOLUTE,

    /** Matches are fused based on relative size and spacing. */
    RELATIVE
}
