package io.github.jspinak.brobot.logging;

/**
 * Categories for Brobot logging events.
 *
 * <p>Each category can be configured independently or controlled by global setting. This allows
 * fine-grained control over what types of events are logged and at what level.
 *
 * <p>Categories follow the main functional areas of the Brobot framework:
 *
 * <ul>
 *   <li>ACTIONS - User actions (click, type, find)
 *   <li>TRANSITIONS - State transitions and navigation
 *   <li>MATCHING - Pattern matching details
 *   <li>PERFORMANCE - Timing and performance metrics
 *   <li>STATE - State management events
 *   <li>LIFECYCLE - Application lifecycle events
 *   <li>VALIDATION - Input validation and checks
 *   <li>SYSTEM - System-level events
 * </ul>
 */
public enum LogCategory {
    /** User actions (click, type, find) */
    ACTIONS,

    /** State transitions and navigation */
    TRANSITIONS,

    /** Pattern matching details and results */
    MATCHING,

    /** Timing and performance metrics */
    PERFORMANCE,

    /** State management events */
    STATE,

    /** Application lifecycle events */
    LIFECYCLE,

    /** Input validation and checks */
    VALIDATION,

    /** System-level events */
    SYSTEM,

    /** Execution control events (pause, resume, stop) */
    EXECUTION
}
