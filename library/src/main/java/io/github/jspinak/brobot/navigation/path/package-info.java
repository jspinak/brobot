/**
 * Path discovery and traversal for state-based navigation.
 * 
 * <p>This package implements the pathfinding and path execution components of Brobot's
 * navigation system. It provides algorithms for discovering routes through the state
 * graph and mechanisms for executing those routes by triggering state transitions.
 * This is the core implementation of the Path Traversal Model (§) from the formal
 * framework.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.navigation.path.Path} - 
 *       Represents a route through states as tuple ρ = (S_ρ, T_ρ)</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.path.Paths} - 
 *       Collection of paths with scoring and selection capabilities</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.path.PathFinder} - 
 *       Implements f_pathfind to discover all routes to target states</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.path.PathTraverser} - 
 *       Executes paths by performing transitions in sequence</li>
 *   <li>{@link io.github.jspinak.brobot.navigation.path.PathManager} - 
 *       Manages path collections, cleanup, and adaptation</li>
 * </ul>
 * 
 * <h2>Path Model</h2>
 * 
 * <p>A path consists of:</p>
 * <ul>
 *   <li><b>State Sequence</b>: S_ρ = [s₀, s₁, ..., sₙ] from start to target</li>
 *   <li><b>Transition Sequence</b>: T_ρ = [t₀, t₁, ..., tₙ₋₁] connecting states</li>
 *   <li><b>Score</b>: Calculated based on state weights and transition costs</li>
 *   <li><b>Validity</b>: Paths must connect adjacent states via transitions</li>
 * </ul>
 * 
 * <h2>Pathfinding Algorithm</h2>
 * 
 * <p>The PathFinder uses recursive graph traversal:</p>
 * <ol>
 *   <li>Start from target state (backward search)</li>
 *   <li>Find all states with transitions TO current state</li>
 *   <li>Recursively explore each predecessor</li>
 *   <li>Terminate when reaching any start state</li>
 *   <li>Prevent cycles by tracking visited states</li>
 * </ol>
 * 
 * <h2>Path Execution</h2>
 * 
 * <p>PathTraverser executes discovered paths:</p>
 * <ol>
 *   <li>Iterate through consecutive state pairs</li>
 *   <li>Execute transition between each pair</li>
 *   <li>Monitor success/failure of each transition</li>
 *   <li>Record failure points for recovery</li>
 *   <li>Return success only if target reached</li>
 * </ol>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Basic Pathfinding</h3>
 * <pre>{@code
 * PathFinder finder = context.getBean(PathFinder.class);
 * 
 * // Find all paths from current states to target
 * Set<State> currentStates = stateMemory.getActiveStates();
 * Paths allPaths = finder.getPathsToState(currentStates, targetState);
 * 
 * // Paths are automatically scored and sorted
 * Path bestPath = allPaths.getBestPath();
 * System.out.println("Best path score: " + bestPath.getScore());
 * }</pre>
 * 
 * <h3>Path Traversal</h3>
 * <pre>{@code
 * PathTraverser traverser = context.getBean(PathTraverser.class);
 * 
 * // Execute the best path
 * boolean success = traverser.traverse(bestPath);
 * 
 * if (!success) {
 *     // Get failure information
 *     Long failedFromState = traverser.getFailedFromState();
 *     System.out.println("Failed at state: " + failedFromState);
 * }
 * }</pre>
 * 
 * <h3>Path Management</h3>
 * <pre>{@code
 * PathManager manager = new PathManager(paths);
 * 
 * // Remove paths that use a failed transition
 * manager.cleanFailedTransition(failedFromState, failedToState);
 * 
 * // Adjust paths after partial progress
 * manager.adjustForProgress(reachedState);
 * 
 * // Get remaining viable paths
 * Paths remainingPaths = manager.getPaths();
 * }</pre>
 * 
 * <h2>Path Scoring</h2>
 * 
 * <p>Paths are scored using:</p>
 * <ul>
 *   <li><b>State Weights</b>: Lower weight = more desirable to traverse</li>
 *   <li><b>Path Length</b>: Shorter paths generally preferred</li>
 *   <li><b>Transition Reliability</b>: Historical success rates</li>
 *   <li><b>Custom Heuristics</b>: Application-specific scoring</li>
 * </ul>
 * 
 * <pre>{@code
 * // Path cost calculation
 * score = Σ(stateWeight[i]) for all states in path
 * 
 * // Lower scores are better
 * paths.sort(); // Sorts by ascending score
 * }</pre>
 * 
 * <h2>Failure Handling</h2>
 * 
 * <p>The package provides robust failure recovery:</p>
 * <ul>
 *   <li><b>Failure Detection</b>: Identifies exact transition that failed</li>
 *   <li><b>Path Filtering</b>: Removes paths using failed transitions</li>
 *   <li><b>Progress Preservation</b>: Adjusts paths based on partial success</li>
 *   <li><b>Alternative Routes</b>: Automatically tries next best path</li>
 * </ul>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <h3>Multi-Start Pathfinding</h3>
 * <pre>{@code
 * // Find paths from multiple possible starting points
 * Set<State> possibleStarts = Set.of(stateA, stateB, stateC);
 * Paths paths = finder.getPathsToState(possibleStarts, targetState);
 * 
 * // PathFinder finds optimal paths from any start
 * }</pre>
 * 
 * <h3>Path Analysis</h3>
 * <pre>{@code
 * // Analyze path characteristics
 * Path path = paths.getBestPath();
 * 
 * List<Long> stateSequence = path.getStates();
 * int pathLength = path.size();
 * boolean visitState = path.contains(specificStateId);
 * 
 * // Get human-readable representation
 * String pathDescription = path.getStatesAsString();
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ol>
 *   <li><b>Completeness</b> - Finds ALL valid paths, not just one</li>
 *   <li><b>Efficiency</b> - Cycle detection prevents infinite recursion</li>
 *   <li><b>Flexibility</b> - Supports multiple start and end states</li>
 *   <li><b>Robustness</b> - Graceful handling of failures</li>
 *   <li><b>Transparency</b> - Clear tracking of execution state</li>
 * </ol>
 * 
 * <h2>Integration</h2>
 * 
 * <p>Path components integrate with:</p>
 * <ul>
 *   <li>StateTransitions for discovering connections</li>
 *   <li>TransitionExecutor for performing state changes</li>
 *   <li>StateNavigator for high-level orchestration</li>
 *   <li>StateMemory for current state information</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.navigation.transition
 * @see io.github.jspinak.brobot.model.transition.StateTransition
 * @see io.github.jspinak.brobot.statemanagement.StateMemory
 */
package io.github.jspinak.brobot.navigation.path;