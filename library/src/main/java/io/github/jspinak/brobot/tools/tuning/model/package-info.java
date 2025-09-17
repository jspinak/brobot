/**
 * Contains data models for parameter tuning experiments and constraints.
 *
 * <p>This package defines the core data structures used in the parameter tuning framework. These
 * models represent tuning experiments with their parameters and results, as well as the constraints
 * that define reasonable bounds for parameter exploration.
 *
 * <h2>Core Models</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.tuning.model.TuningExperiment} - Represents a single
 *       parameter tuning test run with timing parameters and results
 *   <li>{@link io.github.jspinak.brobot.tools.tuning.model.TuningConstraints} - Defines maximum
 *       allowable values for timing parameters
 * </ul>
 *
 * <h2>TuningExperiment Structure</h2>
 *
 * <p>Each tuning experiment captures:
 *
 * <ul>
 *   <li><strong>Input Parameters</strong>: All timing-related settings used for the test
 *       <ul>
 *         <li>Mouse action delays (pauseBeforeMouseDown, pauseAfterMouseUp, etc.)
 *         <li>Movement delays (delayBeforeMouseDown, delayAfterMouseUp, etc.)
 *         <li>Mouse movement speed (moveMouseDelay)
 *         <li>Maximum wait times (maxWait)
 *       </ul>
 *   <li><strong>Execution Results</strong>: Outcomes and measurements from the test
 *       <ul>
 *         <li>Success/failure status
 *         <li>Time for element to appear
 *         <li>Time for element to vanish
 *         <li>Timestamp of execution
 *       </ul>
 * </ul>
 *
 * <h2>Parameter Generation Strategies</h2>
 *
 * <p>TuningExperiment supports multiple parameter generation approaches:
 *
 * <ul>
 *   <li><strong>Random Generation</strong>: Creates random values within constraint bounds for
 *       exploring the parameter space
 *   <li><strong>From Existing Configuration</strong>: Captures current ActionConfig settings as a
 *       baseline
 *   <li><strong>Manual Setting</strong>: Direct parameter assignment for specific tests
 * </ul>
 *
 * <h2>Constraint System</h2>
 *
 * <p>TuningConstraints provides:
 *
 * <ul>
 *   <li><strong>Upper Bounds</strong>: Maximum values for each timing parameter to prevent
 *       excessively long delays
 *   <li><strong>Default Values</strong>: Reasonable defaults based on typical GUI automation needs
 *   <li><strong>Validation</strong>: Ensures generated parameters stay within acceptable ranges
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create constraints with custom limits
 * TuningConstraints constraints = new TuningConstraints();
 * constraints.setMaxPauseBeforeMouseDown(300);  // 300ms max
 * constraints.setMaxWait(10000);                // 10 second max wait
 *
 * // Generate experiment with random parameters
 * TuningExperiment experiment = new TuningExperiment();
 * experiment.generateRandomParameters(constraints);
 *
 * // Or create from existing configuration
 * ActionConfig currentOptions = getDefaultActionConfig();
 * TuningExperiment baseline = TuningExperiment.fromActionConfig(currentOptions);
 *
 * // Execute and record results
 * ActionResult result = executeWithParameters(experiment.toActionConfig());
 * experiment.setSuccess(result.isSuccess());
 * experiment.setTimeToAppear(result.getAppearTime());
 * experiment.setTimeStamp(System.currentTimeMillis());
 * }</pre>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><strong>Immutability</strong>: Models use defensive copying where appropriate
 *   <li><strong>Completeness</strong>: Captures all relevant timing parameters
 *   <li><strong>Flexibility</strong>: Supports various generation strategies
 *   <li><strong>Type Safety</strong>: Strong typing prevents parameter confusion
 * </ul>
 *
 * <h2>Parameter Relationships</h2>
 *
 * <p>The timing parameters work together to control action execution:
 *
 * <ol>
 *   <li><strong>Pause Parameters</strong>: Brief delays for UI stabilization
 *   <li><strong>Delay Parameters</strong>: Longer waits between major actions
 *   <li><strong>Movement Parameters</strong>: Control mouse movement smoothness
 *   <li><strong>Wait Parameters</strong>: Maximum time to wait for elements
 * </ol>
 *
 * <h2>Typical Constraint Values</h2>
 *
 * <p>Default constraints are based on empirical testing:
 *
 * <ul>
 *   <li>Mouse pauses: 0-1000ms (typically 50-200ms works well)
 *   <li>Action delays: 0-2000ms (application-dependent)
 *   <li>Movement delay: 0-500ms (for smooth mouse movement)
 *   <li>Max wait: 0-30000ms (30 seconds for slow-loading elements)
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.ActionConfig
 * @see io.github.jspinak.brobot.tools.tuning.store.TuningExperimentStore
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.tuning.model;
