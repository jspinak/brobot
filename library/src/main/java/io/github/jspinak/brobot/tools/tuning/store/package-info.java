/**
 * Provides storage and analysis capabilities for parameter tuning experiments.
 * 
 * <p>This package contains the repository component that stores tuning experiment
 * results and provides analysis capabilities. It serves as the data persistence
 * layer for the tuning framework, enabling collection, aggregation, and analysis
 * of parameter effectiveness across multiple test runs.
 * 
 * <h2>Core Component</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.tuning.store.TuningExperimentStore} - 
 *       Repository for storing and analyzing tuning experiments</li>
 * </ul>
 * 
 * <h2>Storage Capabilities</h2>
 * <p>The experiment store provides:
 * <ul>
 *   <li><strong>In-Memory Storage</strong>: Maintains experiments during the tuning
 *       session for fast access and analysis</li>
 *   <li><strong>Experiment Aggregation</strong>: Collects multiple experiments for
 *       comparative analysis</li>
 *   <li><strong>Filtering</strong>: Access experiments by success status or other
 *       criteria</li>
 *   <li><strong>Reporting</strong>: Generates analysis reports showing parameter
 *       effectiveness</li>
 * </ul>
 * 
 * <h2>Analysis Features</h2>
 * <p>The store enables various analyses:
 * <ul>
 *   <li><strong>Success Rate Analysis</strong>: Identify which parameter combinations
 *       yield the highest success rates</li>
 *   <li><strong>Performance Analysis</strong>: Compare timing measurements across
 *       different parameter sets</li>
 *   <li><strong>Parameter Correlation</strong>: Understand relationships between
 *       parameters and outcomes</li>
 *   <li><strong>Optimal Parameter Identification</strong>: Find the best-performing
 *       parameter combinations</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Get the experiment store
 * TuningExperimentStore store = context.getBean(TuningExperimentStore.class);
 * 
 * // Run multiple tuning experiments
 * for (int i = 0; i < 100; i++) {
 *     TuningExperiment experiment = new TuningExperiment();
 *     experiment.generateRandomParameters(constraints);
 *     
 *     ActionResult result = runExperiment(experiment);
 *     experiment.setSuccess(result.isSuccess());
 *     experiment.setTimeToAppear(result.getAppearTime());
 *     
 *     store.addExperiment(experiment);
 * }
 * 
 * // Analyze results
 * List<TuningExperiment> successful = store.getSuccessfulExperiments();
 * System.out.println("Success rate: " + 
 *     (successful.size() * 100.0 / store.getAllExperiments().size()) + "%");
 * 
 * // Generate detailed report
 * store.generateReport();
 * 
 * // Find optimal parameters
 * TuningExperiment best = store.getBestPerformingExperiment();
 * ActionOptions optimal = best.toActionOptions();
 * }</pre>
 * 
 * <h2>Report Generation</h2>
 * <p>The generated reports include:
 * <ul>
 *   <li>Summary statistics (total experiments, success rate)</li>
 *   <li>Parameter distributions for successful experiments</li>
 *   <li>Average timing measurements</li>
 *   <li>Recommended parameter values based on analysis</li>
 * </ul>
 * 
 * <h2>Future Enhancements</h2>
 * <p>Potential improvements to the storage layer:
 * <ul>
 *   <li><strong>Persistence</strong>: Save experiments to database or file system
 *       for long-term analysis</li>
 *   <li><strong>Advanced Analytics</strong>: Statistical analysis, machine learning
 *       integration</li>
 *   <li><strong>Visualization</strong>: Graphical reports showing parameter
 *       effectiveness</li>
 *   <li><strong>Export Capabilities</strong>: Export data for external analysis
 *       tools</li>
 *   <li><strong>Context Grouping</strong>: Group experiments by application state
 *       or action type</li>
 * </ul>
 * 
 * <h2>Integration Points</h2>
 * <p>The store integrates with:
 * <ul>
 *   <li>Spring framework as a singleton component</li>
 *   <li>Logging system for detailed experiment tracking</li>
 *   <li>Action framework for parameter application</li>
 *   <li>Testing framework for automated optimization</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * <ul>
 *   <li>Run sufficient experiments (50-100+) for statistical significance</li>
 *   <li>Test across different application states for robust parameters</li>
 *   <li>Consider time of day and system load when tuning</li>
 *   <li>Periodically re-tune as applications update</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.tuning.model.TuningExperiment
 * @see io.github.jspinak.brobot.tools.tuning.model.TuningConstraints
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.tuning.store;