package io.github.jspinak.brobot.tools.tuning.model;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.tools.tuning.store.TuningExperimentStore;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.Random;

/**
 * Represents a collection of timing parameters and their execution results.
 * <p>
 * TuningExperiment encapsulates both input parameters for action timing and
 * the results obtained when executing actions with those parameters. This dual
 * nature makes it ideal for parameter tuning, where different parameter combinations
 * are tested and their outcomes recorded for optimization.
 * <p>
 * <strong>Key components:</strong>
 * <ul>
 * <li><strong>Input parameters:</strong> Timing values that control action execution</li>
 * <li><strong>Result metrics:</strong> Success status and timing measurements</li>
 * </ul>
 * <p>
 * <strong>Usage scenarios:</strong>
 * <ol>
 * <li><strong>Parameter tuning:</strong> Generate random parameters within thresholds,
 *     test them, and record results</li>
 * <li><strong>Configuration capture:</strong> Extract current parameters from
 *     ActionOptions for analysis</li>
 * <li><strong>Performance analysis:</strong> Compare different parameter sets
 *     based on success rates and timing</li>
 * </ol>
 * <p>
 * <strong>Design rationale:</strong>
 * <p>
 * Combining parameters with results in a single class facilitates machine learning
 * and optimization algorithms that need to correlate inputs with outcomes. The
 * random initialization constructor supports exploration-based tuning strategies.
 *
 * @see TuningConstraints
 * @see TuningExperimentStore
 * @see ActionOptions
 */
@Getter
@Setter
public class TuningExperiment {

    // Results variables - outcomes from executing actions with these parameters
    
    /**
     * Indicates whether the action succeeded with these parameters.
     * Used to identify optimal parameter combinations.
     */
    private boolean success;
    
    /**
     * Time taken for UI elements to appear after action execution.
     * Helps optimize wait times for element visibility.
     */
    private Duration timeToAppear;
    
    /**
     * Time taken for UI elements to disappear after action execution.
     * Useful for animations and transition timing optimization.
     */
    private Duration timeToVanish;

    // Click parameters - timing values that control mouse actions
    
    /**
     * Pause duration in seconds before mouse down event.
     * Allows UI to stabilize before interaction.
     */
    private double pauseBeforeMouseDown;
    
    /**
     * Pause duration in seconds after mouse down event.
     * Ensures proper press registration before release.
     */
    private double pauseAfterMouseDown;
    
    /**
     * Pause duration in seconds after mouse up event.
     * Accommodates UI response time after click completion.
     */
    private double pauseAfterMouseUp;
    
    /**
     * Delay factor for mouse movement speed.
     * Controls how fast the mouse moves between positions.
     */
    private float moveMouseDelay;

    // Wait parameters - timeout configuration
    
    /**
     * Maximum time in seconds to wait for element detection.
     * Balances thorough searching with reasonable timeouts.
     */
    private double maxWait;

    /**
     * Creates a TuningExperiment with randomly generated parameters.
     * <p>
     * Initializes all timing parameters with random values between 0 and their
     * respective thresholds defined in {@link TuningConstraints}. This constructor
     * is primarily used for parameter space exploration during automated tuning.
     * <p>
     * The random distribution is uniform, giving equal probability to all values
     * within the valid range. This ensures comprehensive exploration of the
     * parameter space during optimization.
     * <p>
     * Result fields (success, timeToAppear, timeToVanish) are left uninitialized
     * and should be populated after action execution.
     */
    public TuningExperiment() {
        //randomly selects parameters given thresholds
        Random rand = new Random();
        pauseBeforeMouseDown = rand.nextDouble() * TuningConstraints.pauseBeforeMouseDown;
        pauseAfterMouseDown = rand.nextDouble() * TuningConstraints.pauseAfterMouseDown;
        pauseAfterMouseUp = rand.nextDouble() * TuningConstraints.pauseAfterMouseUp;
        moveMouseDelay = rand.nextFloat() * TuningConstraints.moveMouseDelay;
        maxWait = rand.nextDouble() * TuningConstraints.maxWait;
    }

    /**
     * Creates a TuningExperiment from existing ActionOptions.
     * <p>
     * Extracts timing parameters from the provided ActionOptions to create a
     * collection that represents the current configuration. This constructor is
     * useful for:
     * <ul>
     * <li>Capturing baseline parameters before tuning</li>
     * <li>Analyzing the performance of current settings</li>
     * <li>Creating variations of existing configurations</li>
     * </ul>
     * <p>
     * Result fields are left uninitialized and should be populated based on
     * actual execution outcomes.
     *
     * @param actionOptions The ActionOptions containing timing parameters to extract
     */
    public TuningExperiment(ActionOptions actionOptions) {
        //sets parameters to those in actionOptions
        pauseBeforeMouseDown = actionOptions.getPauseBeforeMouseDown();
        pauseAfterMouseDown = actionOptions.getPauseAfterMouseDown();
        pauseAfterMouseUp = actionOptions.getPauseAfterMouseUp();
        moveMouseDelay = actionOptions.getMoveMouseDelay();
        maxWait = actionOptions.getMaxWait();
    }

}
