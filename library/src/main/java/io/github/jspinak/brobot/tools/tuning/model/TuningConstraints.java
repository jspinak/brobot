package io.github.jspinak.brobot.tools.tuning.model;

/**
 * Defines maximum threshold values for action timing parameters.
 * <p>
 * TuningConstraints establishes upper bounds for various timing parameters used
 * throughout the Brobot framework. These thresholds ensure that automated parameter
 * tuning and user configurations remain within reasonable limits, preventing
 * excessively long delays that could make automation impractical.
 * <p>
 * <strong>Purpose:</strong>
 * <ul>
 * <li>Constrains automatic parameter tuning to practical values</li>
 * <li>Prevents configuration errors that could cause indefinite waits</li>
 * <li>Provides sensible defaults for parameter optimization algorithms</li>
 * <li>Ensures consistent timing boundaries across the framework</li>
 * </ul>
 * <p>
 * <strong>Usage context:</strong>
 * <p>
 * These thresholds are primarily used by:
 * <ul>
 * <li>Parameter tuning algorithms that automatically adjust timing values</li>
 * <li>Configuration validation to reject invalid user inputs</li>
 * <li>Testing frameworks that need reasonable upper bounds</li>
 * </ul>
 * <p>
 * The values are chosen based on practical GUI automation experience where
 * delays beyond these thresholds typically indicate problems rather than
 * legitimate timing requirements.
 * <p>
 * <strong>Design rationale:</strong>
 * <p>
 * Static fields allow easy access throughout the framework while maintaining
 * a single source of truth for threshold values. The values can be adjusted
 * based on specific application requirements, though defaults are suitable
 * for most GUI automation scenarios.
 *
 * @see TuningExperiment
 * @see io.github.jspinak.brobot.config.FrameworkSettings
 */
public class TuningConstraints {

    /**
     * Maximum pause duration in seconds before mouse down events.
     * <p>
     * Limits how long the framework will wait before pressing the mouse button.
     * 3.0 seconds accommodates slow-loading UI elements while preventing
     * excessive delays.
     */
    public static double pauseBeforeMouseDown = 3.0;
    
    /**
     * Maximum pause duration in seconds after mouse down events.
     * <p>
     * Constrains the delay after pressing but before releasing the mouse.
     * 1.0 second is sufficient for most drag initiation scenarios.
     */
    public static double pauseAfterMouseDown = 1.0;
    
    /**
     * Maximum pause duration in seconds after mouse up events.
     * <p>
     * Limits the wait time after releasing the mouse button.
     * 2.5 seconds allows for UI animations and state transitions.
     */
    public static double pauseAfterMouseUp = 2.5;
    
    /**
     * Maximum delay in seconds for mouse movement operations.
     * <p>
     * Controls the upper bound for mouse movement speed settings.
     * 1.0 second per movement ensures visible, human-like motion.
     */
    public static float moveMouseDelay = 1.0F;
    
    /**
     * Maximum wait time in seconds for find operations.
     * <p>
     * Defines the longest duration to search for UI elements.
     * 5 seconds balances thorough searching with practical timeout limits.
     */
    public static double maxWait = 5;
}
