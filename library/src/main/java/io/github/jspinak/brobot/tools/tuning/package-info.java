/**
 * Provides automated parameter tuning capabilities for optimizing GUI automation timing.
 *
 * <p>This package implements an empirical, data-driven approach to finding optimal timing
 * parameters for GUI automation actions. By systematically testing different parameter combinations
 * and analyzing their success rates, the tuning framework helps create more reliable and efficient
 * automation scripts that adapt to varying system conditions and application response times.
 *
 * <h2>Tuning Philosophy</h2>
 *
 * <p>The framework recognizes that optimal timing parameters vary based on:
 *
 * <ul>
 *   <li><strong>Application Characteristics</strong>: Different applications have different
 *       response times and animation speeds
 *   <li><strong>System Performance</strong>: CPU load, memory usage, and other factors affect GUI
 *       responsiveness
 *   <li><strong>Action Types</strong>: Click, drag, type, and other actions may require different
 *       timing strategies
 *   <li><strong>UI Context</strong>: Different screens or states within an application may respond
 *       differently
 * </ul>
 *
 * <h2>Core Components</h2>
 *
 * <p>The tuning framework is organized into specialized subpackages:
 *
 * <ul>
 *   <li><strong>model</strong> - Data structures representing tuning experiments and constraints
 *   <li><strong>store</strong> - Repository for storing and analyzing tuning results
 * </ul>
 *
 * <h2>Tuning Process</h2>
 *
 * <ol>
 *   <li><strong>Parameter Space Definition</strong>: Define reasonable bounds for each timing
 *       parameter using constraints
 *   <li><strong>Experimentation</strong>: Execute actions with various parameter combinations,
 *       either randomly generated or systematically varied
 *   <li><strong>Result Collection</strong>: Record success/failure status and actual timing
 *       measurements for each experiment
 *   <li><strong>Analysis</strong>: Identify parameter combinations that yield the best success
 *       rates and performance
 *   <li><strong>Optimization</strong>: Apply the optimal parameters to improve automation
 *       reliability
 * </ol>
 *
 * <h2>Parameters Under Tuning</h2>
 *
 * <p>The framework tunes various timing-related parameters:
 *
 * <ul>
 *   <li><strong>Mouse Movement Delays</strong>: pauseBeforeMouseDown, pauseAfterMouseDown,
 *       pauseBeforeMouseUp, pauseAfterMouseUp
 *   <li><strong>Action Delays</strong>: delayBeforeMouseDown, delayAfterMouseDown,
 *       delayBeforeMouseUp, delayAfterMouseUp
 *   <li><strong>Wait Times</strong>: maxWait for element appearance/disappearance
 *   <li><strong>Movement Speed</strong>: moveMouseDelay for smooth mouse movements
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Define constraints for parameter tuning
 * TuningConstraints constraints = new TuningConstraints();
 * constraints.setMaxPauseBeforeMouseDown(500); // Max 500ms pause
 *
 * // Create and run a tuning experiment
 * TuningExperiment experiment = new TuningExperiment();
 * experiment.generateRandomParameters(constraints);
 *
 * // Execute action with experimental parameters
 * ActionOptions options = experiment.toActionOptions();
 * ActionResult result = action.perform(options);
 *
 * // Record results
 * experiment.setSuccess(result.isSuccess());
 * experiment.setTimeToAppear(result.getDuration());
 *
 * // Store for analysis
 * TuningExperimentStore store = context.getBean(TuningExperimentStore.class);
 * store.addExperiment(experiment);
 *
 * // Analyze results
 * store.generateReport();
 * }</pre>
 *
 * <h2>Benefits</h2>
 *
 * <ul>
 *   <li><strong>Improved Reliability</strong>: Find timing parameters that work consistently across
 *       different conditions
 *   <li><strong>Performance Optimization</strong>: Minimize unnecessary delays while maintaining
 *       reliability
 *   <li><strong>Adaptability</strong>: Easily tune parameters for new applications or environments
 *   <li><strong>Data-Driven</strong>: Make decisions based on empirical evidence rather than
 *       guesswork
 * </ul>
 *
 * <h2>Integration Points</h2>
 *
 * <p>The tuning framework integrates with:
 *
 * <ul>
 *   <li>Action framework for parameter application
 *   <li>State management for context-specific tuning
 *   <li>Logging system for detailed experiment tracking
 *   <li>Testing framework for automated parameter optimization
 * </ul>
 *
 * <h2>Future Enhancements</h2>
 *
 * <p>Potential improvements to the tuning framework:
 *
 * <ul>
 *   <li>Machine learning algorithms for parameter optimization
 *   <li>Context-aware tuning based on application state
 *   <li>Real-time parameter adjustment during execution
 *   <li>Cross-platform parameter profiles
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.tools.tuning.model
 * @see io.github.jspinak.brobot.tools.tuning.store
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.tuning;
