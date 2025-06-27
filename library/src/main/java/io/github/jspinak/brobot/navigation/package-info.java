/**
 * State-based navigation and path traversal framework.
 * 
 * <p>This package implements the Path Traversal Model (§) and navigation components of
 * Brobot's formal model. It provides intelligent navigation capabilities that enable
 * automated traversal of GUI state spaces, automatic path discovery, and adaptive
 * recovery from failures. This is the strategic knowledge layer that separates
 * model-based from script-based automation.</p>
 * 
 * <h2>Theoretical Foundation</h2>
 * 
 * <p>Based on the formal model where GUI automation is represented as <b>Ω = (E, S, T)</b>,
 * the navigation system provides:</p>
 * <ul>
 *   <li><b>Pathfinding</b>: f_pathfind: (Ω, S_Ξ, s_target) → P(ρ)</li>
 *   <li><b>Path Traversal</b>: f_§: (Ω, S_Ξ, s_target, H) → (Ξ')</li>
 *   <li><b>Dynamic Adaptation</b>: Continuous re-evaluation and alternative path discovery</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>Path Management</h3>
 * <p>The path subpackage handles route discovery and execution:</p>
 * <ul>
 *   <li>Automatic path discovery from current states to targets</li>
 *   <li>Path scoring and optimization</li>
 *   <li>Sequential transition execution</li>
 *   <li>Failure point tracking for recovery</li>
 * </ul>
 * 
 * <h3>Transition Execution</h3>
 * <p>The transition subpackage manages state changes:</p>
 * <ul>
 *   <li>Complex transition orchestration</li>
 *   <li>Cascading state activation</li>
 *   <li>Hidden state management</li>
 *   <li>High-level navigation API</li>
 * </ul>
 * 
 * <h3>State Services</h3>
 * <p>The service subpackage provides state management:</p>
 * <ul>
 *   <li>State repository access</li>
 *   <li>Transition relationship management</li>
 *   <li>State query and persistence</li>
 * </ul>
 * 
 * <h3>Reactive Monitoring</h3>
 * <p>The monitoring subpackage enables event-driven automation:</p>
 * <ul>
 *   <li>Continuous state monitoring</li>
 *   <li>Event-loop based automation</li>
 *   <li>Reactive response to GUI changes</li>
 * </ul>
 * 
 * <h2>Navigation Architecture</h2>
 * 
 * <pre>
 * User Request: "Go to State X"
 *         |
 *         v
 *   StateNavigator
 *         |
 *         v
 *    PathFinder -----> Discovers all paths
 *         |
 *         v
 *   PathTraverser ----> Executes transitions
 *         |
 *         v
 * TransitionExecutor --> Manages state changes
 *         |
 *         v
 *   Target Reached
 * </pre>
 * 
 * <h2>Key Concepts</h2>
 * 
 * <h3>Dynamic Pathfinding</h3>
 * <p>Unlike static scripts, the navigation system discovers paths at runtime:</p>
 * <ul>
 *   <li>No hardcoded navigation sequences</li>
 *   <li>Adapts to current GUI state</li>
 *   <li>Finds alternative routes automatically</li>
 *   <li>Handles unexpected states gracefully</li>
 * </ul>
 * 
 * <h3>Failure Recovery</h3>
 * <p>Built-in resilience through intelligent recovery:</p>
 * <ul>
 *   <li>Failed transitions trigger path recalculation</li>
 *   <li>Blacklisting prevents repeated failures</li>
 *   <li>Partial progress is preserved</li>
 *   <li>Multiple alternative paths provide redundancy</li>
 * </ul>
 * 
 * <h3>State Management</h3>
 * <p>Sophisticated tracking of GUI state:</p>
 * <ul>
 *   <li>Multiple concurrent active states</li>
 *   <li>Hidden vs. active state distinction</li>
 *   <li>Cascading state relationships</li>
 *   <li>State existence probabilities</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>Basic Navigation</h3>
 * <pre>{@code
 * // Navigate to a specific state
 * StateNavigator navigator = context.getBean(StateNavigator.class);
 * boolean success = navigator.goToState(targetState);
 * 
 * // The framework automatically:
 * // 1. Finds current active states
 * // 2. Discovers all paths to target
 * // 3. Executes transitions
 * // 4. Recovers from failures
 * }</pre>
 * 
 * <h3>Reactive Automation</h3>
 * <pre>{@code
 * // Set up continuous monitoring
 * ReactiveAutomator automator = new ReactiveAutomator(...);
 * automator.start();
 * 
 * // System continuously:
 * // - Monitors for active states
 * // - Executes appropriate transitions
 * // - Responds to GUI events
 * }</pre>
 * 
 * <h3>Custom Path Selection</h3>
 * <pre>{@code
 * // Find paths with custom scoring
 * PathFinder finder = context.getBean(PathFinder.class);
 * Paths allPaths = finder.getPathsToState(currentStates, targetState);
 * 
 * // Apply custom heuristics
 * Path optimal = allPaths.getOptimalPath(customHeuristic);
 * PathTraverser.traverse(optimal);
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ol>
 *   <li><b>Separation of Concerns</b> - Navigation logic separate from GUI specifics</li>
 *   <li><b>Fail-Safe Design</b> - Multiple paths provide redundancy</li>
 *   <li><b>Dynamic Adaptation</b> - Runtime path discovery over static scripts</li>
 *   <li><b>State-Centric</b> - Navigation based on states, not screens</li>
 *   <li><b>Pluggable Strategies</b> - Customizable heuristics and handlers</li>
 * </ol>
 * 
 * <h2>Integration Points</h2>
 * 
 * <p>The navigation package integrates with:</p>
 * <ul>
 *   <li><b>State Model</b> - Uses states and transitions from model package</li>
 *   <li><b>Actions</b> - Executes actions during transitions</li>
 *   <li><b>State Management</b> - Updates active state tracking</li>
 *   <li><b>Logging</b> - Records navigation history for analysis</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.model.transition
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.statemanagement
 */
package io.github.jspinak.brobot.navigation;