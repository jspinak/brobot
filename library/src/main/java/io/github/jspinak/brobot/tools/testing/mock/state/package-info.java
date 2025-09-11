/**
 * Provides mock implementations for state management and transitions.
 *
 * <p>This package contains mock components that simulate state-related operations without actual
 * GUI interaction. These mocks enable controlled testing of state transitions, probability-based
 * outcomes, and state verification in a deterministic manner.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.state.MockStateManagement} - Manages
 *       state transition probabilities and simulates state changes
 * </ul>
 *
 * <h2>State Transition Simulation</h2>
 *
 * <p>MockStateManagement provides:
 *
 * <ul>
 *   <li>Configurable success probabilities per state
 *   <li>Deterministic state transition outcomes
 *   <li>Support for both found and not-found scenarios
 *   <li>Integration with state memory for realistic behavior
 * </ul>
 *
 * <h2>Probability Configuration</h2>
 *
 * <p>State behavior can be controlled through probability maps:
 *
 * <ul>
 *   <li>Set individual state success rates (0.0 to 1.0)
 *   <li>Configure default probability for unknown states
 *   <li>Simulate specific failure patterns
 *   <li>Test edge cases with controlled randomness
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Configure state probabilities
 * MockStateManagement mockStates = context.getBean(MockStateManagement.class);
 *
 * Map<String, Double> probabilities = new HashMap<>();
 * probabilities.put("LoginPage", 1.0);    // Always succeeds
 * probabilities.put("ErrorPage", 0.0);    // Always fails
 * probabilities.put("HomePage", 0.8);     // 80% success rate
 *
 * mockStates.setProbabilities(probabilities);
 *
 * // Test state transitions
 * boolean success = mockStates.processState("HomePage");
 * }</pre>
 *
 * <h2>Integration with State Framework</h2>
 *
 * <p>Mock state management integrates with:
 *
 * <ul>
 *   <li>StateMemory for tracking active states
 *   <li>StateTransitions for navigation simulation
 *   <li>MockFind for appropriate snapshot selection
 *   <li>State exploration for coverage testing
 * </ul>
 *
 * <h2>Testing Scenarios</h2>
 *
 * <p>Common testing patterns include:
 *
 * <ul>
 *   <li><strong>Happy Path</strong>: All states at 100% success
 *   <li><strong>Failure Testing</strong>: Specific states at 0% success
 *   <li><strong>Realistic Simulation</strong>: Mixed probabilities matching production
 *   <li><strong>Edge Cases</strong>: Low probabilities to test retry logic
 * </ul>
 *
 * <h2>Benefits</h2>
 *
 * <ul>
 *   <li><strong>Control</strong>: Precise control over state behavior
 *   <li><strong>Reproducibility</strong>: Same configuration yields same results
 *   <li><strong>Coverage</strong>: Can test all state combinations
 *   <li><strong>Speed</strong>: No actual navigation delays
 * </ul>
 *
 * @see io.github.jspinak.brobot.stateStructure.state
 * @see io.github.jspinak.brobot.tools.testing.mock.action.MockFind
 * @see io.github.jspinak.brobot.tools.testing.exploration
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing.mock.state;
