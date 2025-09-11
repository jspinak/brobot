/**
 * State analysis and discovery models.
 *
 * <p>This package provides models for analyzing application behavior to discover states and their
 * relationships. It enables automated learning of application structure through observation and
 * analysis of GUI elements and transitions.
 *
 * <h2>Core Purpose</h2>
 *
 * <p>State discovery addresses the challenge of building automation for applications without
 * predefined models. By observing application behavior, the system can:
 *
 * <ul>
 *   <li>Identify unique application states
 *   <li>Discover state-defining elements
 *   <li>Learn transition patterns
 *   <li>Build state models dynamically
 * </ul>
 *
 * <h2>Discovery Process</h2>
 *
 * <ol>
 *   <li><b>Exploration</b> - Navigate application systematically
 *   <li><b>Observation</b> - Capture screens and detect elements
 *   <li><b>Classification</b> - Group similar screens into states
 *   <li><b>Validation</b> - Confirm state uniqueness and stability
 *   <li><b>Model Building</b> - Create formal state definitions
 * </ol>
 *
 * <h2>Analysis Capabilities</h2>
 *
 * <h3>State Identification</h3>
 *
 * <p>Determine when the application is in a new vs known state:
 *
 * <ul>
 *   <li>Compare visual elements with known states
 *   <li>Calculate similarity scores
 *   <li>Identify unique state indicators
 *   <li>Handle dynamic content appropriately
 * </ul>
 *
 * <h3>Element Discovery</h3>
 *
 * <p>Find interactive and identifying elements:
 *
 * <ul>
 *   <li>Detect buttons, fields, and controls
 *   <li>Identify static markers and logos
 *   <li>Discover dynamic content regions
 *   <li>Learn element relationships
 * </ul>
 *
 * <h3>Transition Learning</h3>
 *
 * <p>Understand how states connect:
 *
 * <ul>
 *   <li>Track user interactions
 *   <li>Map actions to state changes
 *   <li>Identify transition triggers
 *   <li>Build navigation paths
 * </ul>
 *
 * <h2>Integration</h2>
 *
 * <p>State analysis integrates with:
 *
 * <ul>
 *   <li>Scene analysis for visual comparison
 *   <li>Action execution for exploration
 *   <li>State store for model persistence
 *   <li>Transition tracking for path learning
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ol>
 *   <li>Start with manual state definition when possible
 *   <li>Use discovery to find missed states and transitions
 *   <li>Validate discovered states before automation
 *   <li>Combine automated discovery with human review
 *   <li>Update models as applications evolve
 * </ol>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.model.analysis.scene
 * @see io.github.jspinak.brobot.state.stateDiscovery
 */
package io.github.jspinak.brobot.model.analysis.state;
