package io.github.jspinak.brobot.action;

/**
 * Defines conditions for text-based action termination.
 *
 * <p>Used in conjunction with find operations to determine when to stop searching based on text
 * presence or absence.
 *
 * @since 2.0
 */
public enum GetTextUntil {
    /** Text is not used as an exit condition. Default for non-text operations. */
    NONE,

    /** Continue searching until any text appears in the search region. */
    TEXT_APPEARS,

    /** Continue searching until text disappears from the search region. */
    TEXT_VANISHES
}
