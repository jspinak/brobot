/**
 * State management and active state tracking implementation.
 * 
 * <p>This package implements the State Management Model (M) from Brobot's formal framework,
 * where M = (S_Ξ) maintains the current set of active states. It provides the runtime
 * memory and detection capabilities that enable the framework to maintain explicit awareness
 * of the GUI's current configuration, supporting dynamic adaptation and recovery from
 * unexpected situations.</p>
 * 
 * <h2>Theoretical Foundation</h2>
 * 
 * <p>Based on the formal model, the state management system implements:</p>
 * <ul>
 *   <li><b>Active State Set (S_Ξ)</b>: S_Ξ ⊆ S, the subset of currently active states</li>
 *   <li><b>State Updates</b>: f_M: (S_Ξ, S_a, S_t) → S'_Ξ</li>
 *   <li><b>Activation Logic</b>: S_+ = {s ∈ S | (s, True) ∈ (S_a ∪ S_t)}</li>
 *   <li><b>Deactivation Logic</b>: S_- = {s ∈ S | (s, False) ∈ (S_a ∪ S_t)}</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.StateMemory} - 
 *       Runtime memory maintaining the active state set (S_Ξ)</li>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.StateDetector} - 
 *       Visual detection of active states through pattern matching</li>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.StateVisibilityManager} - 
 *       Management of hidden and visible state relationships</li>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.StateIdResolver} - 
 *       Name-to-ID resolution for efficient state processing</li>
 * </ul>
 * 
 * <h3>Supporting Components</h3>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.ActiveStateSet} - 
 *       Lightweight enum-based state tracking for simple scenarios</li>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.AdjacentStates} - 
 *       Graph relationships between connected states</li>
 *   <li>{@link io.github.jspinak.brobot.statemanagement.InitialStates} - 
 *       Starting state configuration management</li>
 * </ul>
 * 
 * <h2>State Management Model</h2>
 * 
 * <p>The package implements explicit state tracking that distinguishes model-based
 * from process-based automation:</p>
 * <ul>
 *   <li><b>Explicit Tracking</b> - States are explicitly maintained, not implicitly assumed</li>
 *   <li><b>Multiple Active States</b> - Supports concurrent state activation</li>
 *   <li><b>Dynamic Updates</b> - States change based on action results and transitions</li>
 *   <li><b>Persistent Memory</b> - States remain active until explicitly deactivated</li>
 * </ul>
 * 
 * <h2>Usage Patterns</h2>
 * 
 * <h3>State Memory Operations</h3>
 * <pre>{@code
 * StateMemory memory = context.getBean(StateMemory.class);
 * 
 * // Check active states
 * Set<Long> activeIds = memory.getActiveStates();
 * List<State> activeStates = memory.getActiveStateList();
 * 
 * // Update based on found elements
 * ActionResult result = find.perform(stateElements);
 * memory.adjustActiveStatesWithMatches(result);
 * 
 * // Manual state management
 * memory.addActiveState(loginStateId);
 * memory.removeInactiveState(splashStateId);
 * }</pre>
 * 
 * <h3>State Detection</h3>
 * <pre>{@code
 * StateDetector detector = context.getBean(StateDetector.class);
 * 
 * // Find current active states
 * Set<State> foundStates = detector.findStates();
 * 
 * // Check specific state
 * boolean isActive = detector.findState(targetState);
 * 
 * // Refresh all states (full rediscovery)
 * detector.refreshStates();
 * }</pre>
 * 
 * <h3>Hidden State Management</h3>
 * <pre>{@code
 * StateVisibilityManager visibility = context.getBean(StateVisibilityManager.class);
 * 
 * // Modal dialog hides main screen
 * visibility.setHiddenStates(modalState, Set.of(mainScreenState));
 * 
 * // Check visibility
 * boolean isHidden = visibility.isStateHidden(mainScreenState);
 * 
 * // Restore hidden states
 * Set<State> toRestore = visibility.getHiddenStates(modalState);
 * }</pre>
 * 
 * <h2>State Update Flow</h2>
 * 
 * <ol>
 *   <li><b>Action Results</b> - Found elements update active states (S_a)</li>
 *   <li><b>Transition Results</b> - State changes from transitions (S_t)</li>
 *   <li><b>Merge Updates</b> - S_+ added, S_- removed from S_Ξ</li>
 *   <li><b>Persistence</b> - Active states persist until explicitly changed</li>
 * </ol>
 * 
 * <h2>Design Principles</h2>
 * 
 * <h3>Explicit State Awareness</h3>
 * <p>Unlike implicit tracking through action sequences, the system maintains
 * explicit knowledge of active states, enabling:</p>
 * <ul>
 *   <li>Recovery from unexpected navigation</li>
 *   <li>Validation of expected vs. actual state</li>
 *   <li>Dynamic path recalculation</li>
 *   <li>Robust error handling</li>
 * </ul>
 * 
 * <h3>Multiple Active States</h3>
 * <p>Real GUIs often have multiple concurrent states:</p>
 * <ul>
 *   <li>Main window + modal dialog</li>
 *   <li>Multiple panels or tabs</li>
 *   <li>Overlay notifications</li>
 *   <li>Background processes</li>
 * </ul>
 * 
 * <h3>State Persistence</h3>
 * <p>States remain active until explicitly deactivated because:</p>
 * <ul>
 *   <li>Not finding an element doesn't mean the state is gone</li>
 *   <li>Elements may be temporarily occluded</li>
 *   <li>Transitions explicitly manage state changes</li>
 *   <li>Provides stability in uncertain environments</li>
 * </ul>
 * 
 * <h2>Integration Points</h2>
 * 
 * <p>State management integrates with:</p>
 * <ul>
 *   <li><b>Actions</b> - Update active states based on found elements</li>
 *   <li><b>Transitions</b> - Explicitly activate and deactivate states</li>
 *   <li><b>Navigation</b> - Use active states for pathfinding start points</li>
 *   <li><b>Monitoring</b> - Track state changes over time</li>
 * </ul>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>State detection can be expensive with many states</li>
 *   <li>Check known active states before full search</li>
 *   <li>Consider ML-based instant state recognition for future</li>
 *   <li>Cache state detection results when appropriate</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li>Initialize with known starting states when possible</li>
 *   <li>Use state detection sparingly due to performance cost</li>
 *   <li>Let transitions manage state changes explicitly</li>
 *   <li>Monitor active state count for debugging</li>
 *   <li>Use hidden states for overlays and modals</li>
 *   <li>Validate state assumptions in critical paths</li>
 * </ol>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.model.state
 * @see io.github.jspinak.brobot.navigation
 * @see io.github.jspinak.brobot.action
 */
package io.github.jspinak.brobot.statemanagement;