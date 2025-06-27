/**
 * Provides mock implementations for deterministic and rapid GUI testing.
 * 
 * <p>This package implements the mock testing framework that enables Brobot to
 * execute tests without actual GUI interaction. By using historical match data
 * (snapshots) and simulated time, tests run orders of magnitude faster while
 * maintaining deterministic behavior essential for reliable automated testing.
 * 
 * <h2>Mock Testing Philosophy</h2>
 * <p>The mock framework is built on several key principles:
 * <ul>
 *   <li><strong>Snapshot-Based</strong>: Uses historical match data to provide
 *       realistic responses without GUI interaction</li>
 *   <li><strong>Deterministic</strong>: Same inputs always produce same outputs,
 *       eliminating test flakiness</li>
 *   <li><strong>Fast Execution</strong>: Removes GUI delays, enabling rapid
 *       iteration and extensive testing</li>
 *   <li><strong>Controlled Failures</strong>: Can simulate specific failure
 *       scenarios for robustness testing</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.MockStatus} - 
 *       Tracks mock operation counts for bounded execution</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.MockMatchBuilder} - 
 *       Generates synthetic Match objects for testing</li>
 * </ul>
 * 
 * <h2>Subpackage Organization</h2>
 * <ul>
 *   <li><strong>action</strong> - Mock implementations of GUI actions (find, click, drag, etc.)</li>
 *   <li><strong>state</strong> - Mock state management and transitions</li>
 *   <li><strong>time</strong> - Simulated time control for deterministic testing</li>
 *   <li><strong>environment</strong> - Mock environment detection (window focus, etc.)</li>
 * </ul>
 * 
 * <h2>Mock vs Live Execution</h2>
 * <p>The framework supports seamless switching between mock and live modes:
 * <ul>
 *   <li><strong>Mock Mode</strong>: Uses snapshots for instant, deterministic results</li>
 *   <li><strong>Live Mode</strong>: Interacts with actual GUI for validation</li>
 *   <li><strong>Hybrid Mode</strong>: Can mix mock and live operations as needed</li>
 * </ul>
 * 
 * <h2>Snapshot System</h2>
 * <p>Mock operations leverage the Match History system:
 * <ul>
 *   <li>Historical matches are stored with their context</li>
 *   <li>Mock Find returns appropriate snapshots based on current state</li>
 *   <li>Success rates can be controlled for failure testing</li>
 *   <li>Visual verification uses stored screenshot data</li>
 * </ul>
 * 
 * <h2>Time Simulation</h2>
 * <p>MockTime provides:
 * <ul>
 *   <li>Virtual clock for instant time progression</li>
 *   <li>Configurable action durations</li>
 *   <li>Deterministic wait operations</li>
 *   <li>Time travel capabilities for testing timeouts</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Enable mock mode
 * ExecutionModeController.setMockTrue();
 * 
 * // Configure mock behavior
 * MockStateManagement mockStates = context.getBean(MockStateManagement.class);
 * mockStates.setProbabilities(successMap);
 * 
 * // Run tests with mock operations
 * ActionResult result = action.perform(); // Uses MockFind internally
 * 
 * // Verify mock execution
 * MockStatus status = context.getBean(MockStatus.class);
 * System.out.println("Mock operations: " + status.getMocksPerformed());
 * }</pre>
 * 
 * <h2>Benefits</h2>
 * <ul>
 *   <li><strong>Speed</strong>: Tests run 100-1000x faster than live execution</li>
 *   <li><strong>Reliability</strong>: Eliminates GUI-related test flakiness</li>
 *   <li><strong>Control</strong>: Can simulate any scenario including failures</li>
 *   <li><strong>Coverage</strong>: Enables extensive testing impractical with live GUI</li>
 * </ul>
 * 
 * <h2>Integration</h2>
 * <p>The mock framework integrates seamlessly with:
 * <ul>
 *   <li>Action execution framework through ExecutionModeController</li>
 *   <li>State management for navigation simulation</li>
 *   <li>Match History for snapshot retrieval</li>
 *   <li>Exploration framework for rapid coverage testing</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.testing.mock.action
 * @see io.github.jspinak.brobot.tools.testing.mock.state
 * @see io.github.jspinak.brobot.tools.testing.mock.time
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing.mock;