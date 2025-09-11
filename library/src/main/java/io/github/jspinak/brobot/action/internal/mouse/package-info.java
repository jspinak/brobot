/**
 * Internal wrappers and utilities for low-level mouse operations.
 *
 * <p>This package provides the internal implementation layer between high-level mouse actions and
 * the underlying robot/native mouse control. These wrappers ensure consistent behavior, add
 * logging, handle platform differences, and provide a mockable interface for testing.
 *
 * <h2>Mouse Operation Wrappers</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.SingleClickExecutor}</b> -
 *       Low-level click execution at specific coordinates
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper}</b> - Mouse
 *       movement with path interpolation
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.MouseDownWrapper}</b> - Mouse
 *       button press operations
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.MouseUpWrapper}</b> - Mouse button
 *       release operations
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.MouseWheel}</b> - Mouse wheel
 *       scrolling operations
 * </ul>
 *
 * <h2>Support Components</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.ClickType}</b> - Enumeration of
 *       click types (single, double, right-click, etc.)
 *   <li><b>{@link io.github.jspinak.brobot.action.internal.mouse.PostClickHandler}</b> - Post-click
 *       operations and cleanup
 * </ul>
 *
 * <h2>Mouse Operation Features</h2>
 *
 * <h3>Movement Patterns</h3>
 *
 * <ul>
 *   <li>Linear interpolation for straight paths
 *   <li>Bezier curves for natural movement
 *   <li>Random variations for human-like motion
 *   <li>Speed control and acceleration
 * </ul>
 *
 * <h3>Click Operations</h3>
 *
 * <ul>
 *   <li>Single, double, and triple clicks
 *   <li>Left, middle, and right buttons
 *   <li>Click-and-hold durations
 *   <li>Multi-button combinations
 * </ul>
 *
 * <h3>Platform Abstraction</h3>
 *
 * <ul>
 *   <li>Consistent behavior across Windows, Mac, Linux
 *   <li>Platform-specific timing adjustments
 *   <li>Screen boundary handling
 *   <li>DPI awareness and scaling
 * </ul>
 *
 * <h2>Implementation Details</h2>
 *
 * <h3>Click Execution Flow</h3>
 *
 * <pre>{@code
 * // Internal wrapper usage
 * Location target = new Location(500, 300);
 * ClickType clickType = ClickType.LEFT_DOUBLE;
 *
 * // Move to location
 * MoveMouseWrapper.moveTo(target);
 *
 * // Perform click
 * ClickLocationOnce.click(target, clickType);
 *
 * // Post-click operations
 * AfterClick.perform(clickType, target);
 * }</pre>
 *
 * <h3>Drag Operation Support</h3>
 *
 * <pre>{@code
 * // Internal drag implementation
 * Location start = new Location(100, 100);
 * Location end = new Location(300, 300);
 *
 * MoveMouseWrapper.moveTo(start);
 * MouseDownWrapper.press(MouseButton.LEFT);
 * MoveMouseWrapper.dragTo(end); // Special drag movement
 * MouseUpWrapper.release(MouseButton.LEFT);
 * }</pre>
 *
 * <h2>Error Handling</h2>
 *
 * <ul>
 *   <li>Boundary checking for all coordinates
 *   <li>Recovery from interrupted operations
 *   <li>Platform-specific error handling
 *   <li>Graceful degradation on failures
 * </ul>
 *
 * <h2>Timing and Delays</h2>
 *
 * <ul>
 *   <li>Configurable delays between operations
 *   <li>Platform-specific timing adjustments
 *   <li>Human-like timing variations
 *   <li>Synchronization with GUI response times
 * </ul>
 *
 * <h2>Testing Support</h2>
 *
 * <ul>
 *   <li>Mockable interfaces for unit testing
 *   <li>Operation recording for playback
 *   <li>Deterministic mode for reproducible tests
 *   <li>Event simulation without actual mouse movement
 * </ul>
 *
 * <h2>Integration with Public API</h2>
 *
 * <p>These wrappers support the public mouse actions:
 *
 * <ul>
 *   <li>{@code Click} action uses click wrappers
 *   <li>{@code MoveMouse} uses movement wrappers
 *   <li>{@code Drag} combines multiple wrappers
 *   <li>{@code ScrollMouseWheel} uses wheel wrapper
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.basic.click.Click
 * @see io.github.jspinak.brobot.action.basic.mouse.MoveMouse
 * @see io.github.jspinak.brobot.action.composite.drag.Drag
 */
package io.github.jspinak.brobot.action.internal.mouse;
