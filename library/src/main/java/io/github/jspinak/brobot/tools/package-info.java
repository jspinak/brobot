/**
 * Utility tools for building state structures and visualizing execution history.
 * 
 * <p>This package provides essential tools that support the construction of
 * State Structures (Ω) and the visualization of automation execution. These
 * tools implement the practical aspects of Brobot's model-based approach,
 * making it easier to build explicit GUI representations and understand
 * automation behavior through visual history.</p>
 * 
 * <h2>Subpackages</h2>
 * 
 * <h3>builder</h3>
 * <p>State structure construction utilities:</p>
 * <ul>
 *   <li>Fluent API for building states and transitions</li>
 *   <li>Simplified state structure assembly</li>
 *   <li>Testing and bootstrapping support</li>
 * </ul>
 * 
 * <h3>history</h3>
 * <p>Execution visualization and history tracking:</p>
 * <ul>
 *   <li>Action result visualization</li>
 *   <li>State recognition illustrations</li>
 *   <li>Visual debugging capabilities</li>
 *   <li>History file management</li>
 * </ul>
 * 
 * <h2>Design Philosophy</h2>
 * 
 * <p>The tools package embodies the principle of making implicit GUI
 * knowledge explicit through:</p>
 * <ul>
 *   <li><b>Declarative Construction</b> - Build state structures declaratively</li>
 *   <li><b>Visual Representation</b> - Show what the automation sees and does</li>
 *   <li><b>Historical Tracking</b> - Maintain visual records of execution</li>
 *   <li><b>Debugging Support</b> - Enable understanding through visualization</li>
 * </ul>
 * 
 * <h2>Integration with Core Framework</h2>
 * 
 * <p>These tools support the core Brobot components:</p>
 * <ul>
 *   <li>Builder tools create State Structure (Ω) components</li>
 *   <li>History tools visualize Action execution results</li>
 *   <li>Both support the transition from implicit to explicit modeling</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.action
 */
package io.github.jspinak.brobot.tools;