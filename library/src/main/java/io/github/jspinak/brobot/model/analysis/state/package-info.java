/**
 * State analysis and discovery models.
 * 
 * <p>This package provides models for analyzing application behavior to
 * discover states and their relationships. It enables automated learning
 * of application structure through observation and analysis of GUI elements
 * and transitions.</p>
 * 
 * <h2>Core Purpose</h2>
 * 
 * <p>State discovery addresses the challenge of building automation for
 * applications without predefined models. By observing application behavior,
 * the system can:</p>
 * <ul>
 *   <li>Identify unique application states</li>
 *   <li>Discover state-defining elements</li>
 *   <li>Learn transition patterns</li>
 *   <li>Build state models dynamically</li>
 * </ul>
 * 
 * <h2>Discovery Process</h2>
 * 
 * <ol>
 *   <li><b>Exploration</b> - Navigate application systematically</li>
 *   <li><b>Observation</b> - Capture screens and detect elements</li>
 *   <li><b>Classification</b> - Group similar screens into states</li>
 *   <li><b>Validation</b> - Confirm state uniqueness and stability</li>
 *   <li><b>Model Building</b> - Create formal state definitions</li>
 * </ol>
 * 
 * <h2>Analysis Capabilities</h2>
 * 
 * <h3>State Identification</h3>
 * <p>Determine when the application is in a new vs known state:</p>
 * <ul>
 *   <li>Compare visual elements with known states</li>
 *   <li>Calculate similarity scores</li>
 *   <li>Identify unique state indicators</li>
 *   <li>Handle dynamic content appropriately</li>
 * </ul>
 * 
 * <h3>Element Discovery</h3>
 * <p>Find interactive and identifying elements:</p>
 * <ul>
 *   <li>Detect buttons, fields, and controls</li>
 *   <li>Identify static markers and logos</li>
 *   <li>Discover dynamic content regions</li>
 *   <li>Learn element relationships</li>
 * </ul>
 * 
 * <h3>Transition Learning</h3>
 * <p>Understand how states connect:</p>
 * <ul>
 *   <li>Track user interactions</li>
 *   <li>Map actions to state changes</li>
 *   <li>Identify transition triggers</li>
 *   <li>Build navigation paths</li>
 * </ul>
 * 
 * <h2>Integration</h2>
 * 
 * <p>State analysis integrates with:</p>
 * <ul>
 *   <li>Scene analysis for visual comparison</li>
 *   <li>Action execution for exploration</li>
 *   <li>State store for model persistence</li>
 *   <li>Transition tracking for path learning</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Start with manual state definition when possible</li>
 *   <li>Use discovery to find missed states and transitions</li>
 *   <li>Validate discovered states before automation</li>
 *   <li>Combine automated discovery with human review</li>
 *   <li>Update models as applications evolve</li>
 * </ol>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.model.analysis.scene
 * @see io.github.jspinak.brobot.state.stateDiscovery
 */
package io.github.jspinak.brobot.model.analysis.state;