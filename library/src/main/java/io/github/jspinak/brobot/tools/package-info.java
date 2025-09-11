/**
 * Utility tools for building state structures and visualizing execution history.
 *
 * <p>This package provides essential tools that support the construction of State Structures (Ω)
 * and the visualization of automation execution. These tools implement the practical aspects of
 * Brobot's model-based approach, making it easier to build explicit GUI representations and
 * understand automation behavior through visual history.
 *
 * <h2>Subpackages</h2>
 *
 * <h3>builder</h3>
 *
 * <p>State structure construction utilities:
 *
 * <ul>
 *   <li>Fluent API for building states and transitions
 *   <li>Simplified state structure assembly
 *   <li>Testing and bootstrapping support
 * </ul>
 *
 * <h3>history</h3>
 *
 * <p>Execution visualization and history tracking:
 *
 * <ul>
 *   <li>Action result visualization
 *   <li>State recognition illustrations
 *   <li>Visual debugging capabilities
 *   <li>History file management
 * </ul>
 *
 * <h2>Design Philosophy</h2>
 *
 * <p>The tools package embodies the principle of making implicit GUI knowledge explicit through:
 *
 * <ul>
 *   <li><b>Declarative Construction</b> - Build state structures declaratively
 *   <li><b>Visual Representation</b> - Show what the automation sees and does
 *   <li><b>Historical Tracking</b> - Maintain visual records of execution
 *   <li><b>Debugging Support</b> - Enable understanding through visualization
 * </ul>
 *
 * <h2>Integration with Core Framework</h2>
 *
 * <p>These tools support the core Brobot components:
 *
 * <ul>
 *   <li>Builder tools create State Structure (Ω) components
 *   <li>History tools visualize Action execution results
 *   <li>Both support the transition from implicit to explicit modeling
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.action
 */
package io.github.jspinak.brobot.tools;
