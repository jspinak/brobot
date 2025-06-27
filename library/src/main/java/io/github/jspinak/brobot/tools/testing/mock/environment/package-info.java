/**
 * Provides mock implementations for environment detection and interaction.
 * 
 * <p>This package contains mock components that simulate environment-related
 * operations such as window focus detection, screen properties, and system
 * state queries. These mocks enable testing of environment-dependent behaviors
 * without requiring specific system configurations.
 * 
 * <h2>Core Components</h2>
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.tools.testing.mock.environment.MockFocusedWindow} - 
 *       Simulates window focus detection for controlled testing</li>
 * </ul>
 * 
 * <h2>Window Focus Simulation</h2>
 * <p>MockFocusedWindow provides:
 * <ul>
 *   <li>Configurable window titles and properties</li>
 *   <li>Deterministic focus state responses</li>
 *   <li>Support for multi-window scenarios</li>
 *   <li>Testing of focus-dependent behaviors</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>{@code
 * // Configure mock window focus
 * MockFocusedWindow mockWindow = context.getBean(MockFocusedWindow.class);
 * mockWindow.setFocusedWindowTitle("MyApplication - Main Window");
 * mockWindow.setWindowBounds(new Rectangle(0, 0, 1920, 1080));
 * 
 * // Test focus-dependent behavior
 * String title = mockWindow.getFocusedWindowTitle();
 * boolean isCorrectWindow = title.contains("MyApplication");
 * }</pre>
 * 
 * <h2>Environment Testing Scenarios</h2>
 * <ul>
 *   <li><strong>Multi-Window Applications</strong>: Test window switching logic</li>
 *   <li><strong>Focus Loss Handling</strong>: Verify behavior when focus changes</li>
 *   <li><strong>Resolution Independence</strong>: Test across different screen sizes</li>
 *   <li><strong>Platform Variations</strong>: Simulate different OS behaviors</li>
 * </ul>
 * 
 * <h2>Integration Points</h2>
 * <p>Environment mocks integrate with:
 * <ul>
 *   <li>State detection for window-specific states</li>
 *   <li>Action execution for window-relative coordinates</li>
 *   <li>Screen capture for region validation</li>
 *   <li>Focus management strategies</li>
 * </ul>
 * 
 * <h2>Benefits</h2>
 * <ul>
 *   <li><strong>Portability</strong>: Tests run on any system configuration</li>
 *   <li><strong>Isolation</strong>: No dependency on actual window state</li>
 *   <li><strong>Control</strong>: Precise simulation of edge cases</li>
 *   <li><strong>Speed</strong>: No window manipulation delays</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.screen
 * @see io.github.jspinak.brobot.tools.testing.mock.state
 * @since 1.0
 */
package io.github.jspinak.brobot.tools.testing.mock.environment;