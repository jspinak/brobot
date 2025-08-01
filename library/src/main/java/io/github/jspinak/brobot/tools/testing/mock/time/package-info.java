/**
 * Provides time simulation capabilities for deterministic testing.
 * 
 * <p>This package implements a virtual time system that enables rapid test execution
 * by eliminating real-world delays. Mock time allows tests to simulate the passage
 * of time instantly while maintaining realistic timing relationships between actions.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.time.MockTime} - 
 *       Virtual clock implementation with instant time progression</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.time.ActionDurations} - 
 *       Configurable durations for different action types</li>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider} - 
 *       Simple wrapper delegating to ExecutionModeController</li>
 * </ul>
 * 
 * <h2>Virtual Time System</h2>
 * <p>MockTime provides:
 * <ul>
 *   <li>Instant progression of time without actual delays</li>
 *   <li>Consistent timestamps for deterministic behavior</li>
 *   <li>Support for wait operations without blocking</li>
 *   <li>Time travel capabilities for testing timeouts</li>
 * </ul>
 * 
 * <h2>Action Duration Configuration</h2>
 * <p>ActionDurations stores realistic durations for:
 * <ul>
 *   <li><strong>Find operations</strong>: Image search time</li>
 *   <li><strong>Click actions</strong>: Mouse movement and click</li>
 *   <li><strong>Drag operations</strong>: Drag gesture duration</li>
 *   <li><strong>Type actions</strong>: Keyboard input time</li>
 *   <li><strong>Wait operations</strong>: Explicit delays</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Configure mock time
 * MockTime mockTime = context.getBean(MockTime.class);
 * mockTime.setStartTime(System.currentTimeMillis());
 * 
 * // Simulate passage of time
 * mockTime.wait(5000); // Instantly "waits" 5 seconds
 * long elapsed = mockTime.getDuration(); // Returns 5000
 * 
 * // Configure action durations
 * ActionDurations durations = context.getBean(ActionDurations.class);
 * durations.setFindDuration(100);   // Find takes 100ms
 * durations.setClickDuration(50);   // Click takes 50ms
 * }</pre>
 * 
 * <h2>Benefits for Testing</h2>
 * <ul>
 *   <li><strong>Speed</strong>: Tests run without waiting for timeouts</li>
 *   <li><strong>Determinism</strong>: Timing is predictable and reproducible</li>
 *   <li><strong>Control</strong>: Can test timeout scenarios instantly</li>
 *   <li><strong>Realism</strong>: Maintains relative timing relationships</li>
 * </ul>
 * 
 * <h2>Integration Points</h2>
 * <p>Mock time integrates with:
 * <ul>
 *   <li>Action execution for simulated delays</li>
 *   <li>State transitions for timeout testing</li>
 *   <li>Wait operations throughout the framework</li>
 *   <li>Performance metrics collection</li>
 * </ul>
 * 
 * <h2>Time-Based Testing Scenarios</h2>
 * <ul>
 *   <li><strong>Timeout Testing</strong>: Verify behavior when operations exceed limits</li>
 *   <li><strong>Performance Testing</strong>: Measure operation counts without delays</li>
 *   <li><strong>Race Conditions</strong>: Test timing-sensitive scenarios</li>
 *   <li><strong>Long-Running Tests</strong>: Simulate hours of execution in seconds</li>
 * </ul>
 * 
 * <h2>Limitations</h2>
 * <p>Mock time cannot detect:
 * <ul>
 *   <li>Real-world performance issues</li>
 *   <li>Actual GUI responsiveness</li>
 *   <li>System load impacts</li>
 *   <li>Network latency effects</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.tools.testing.mock.action
 * @see io.github.jspinak.brobot.action.wait
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing.mock.time;