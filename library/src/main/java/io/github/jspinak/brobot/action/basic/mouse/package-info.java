/**
 * Low-level mouse control actions for precise cursor manipulation.
 * 
 * <p>This package provides fine-grained control over mouse operations, allowing for
 * precise cursor movement, button control, and scroll wheel manipulation. These actions
 * form the foundation for higher-level interactive actions like Click and Drag.</p>
 * 
 * <h2>Mouse Actions</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.mouse.MoveMouse}</b> - 
 *       Moves the mouse cursor to specific locations or along paths</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.mouse.MouseDown}</b> - 
 *       Presses and holds a mouse button</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.mouse.MouseUp}</b> - 
 *       Releases a previously pressed mouse button</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.mouse.ScrollMouseWheel}</b> - 
 *       Scrolls the mouse wheel up or down</li>
 * </ul>
 * 
 * <h2>Features</h2>
 * 
 * <ul>
 *   <li><b>Precise Movement</b> - Pixel-perfect cursor positioning</li>
 *   <li><b>Natural Motion</b> - Configurable movement patterns and speeds</li>
 *   <li><b>Multi-button Support</b> - Left, middle, and right mouse buttons</li>
 *   <li><b>Scroll Control</b> - Variable speed and distance scrolling</li>
 *   <li><b>Path Following</b> - Move along custom paths for realistic motion</li>
 * </ul>
 * 
 * <h2>Movement Patterns</h2>
 * 
 * <p>Mouse movement can be configured with different patterns:</p>
 * <ul>
 *   <li><b>Direct</b> - Straight line to destination (fastest)</li>
 *   <li><b>Curved</b> - Natural curved path (more human-like)</li>
 *   <li><b>Random</b> - Adds small variations for realism</li>
 *   <li><b>Custom</b> - Follow user-defined paths</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Move mouse to specific location with custom speed
 * MouseMoveOptions moveOptions = new MouseMoveOptions.Builder()
 *     .setMoveMouseDelay(0.5f)
 *     .setPauseAfterEnd(0.2)
 *     .build();
 * 
 * ActionResult result = new ActionResult();
 * result.setActionConfig(moveOptions);
 * 
 * ObjectCollection target = new ObjectCollection.Builder()
 *     .withLocations(new Location(500, 300))
 *     .build();
 * 
 * moveMouse.perform(result, target);
 * 
 * // Perform a drag operation using low-level actions
 * // Press right mouse button at start location
 * MouseDownOptions downOptions = new MouseDownOptions.Builder()
 *     .setPressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.RIGHT)
 *         .setPauseBeforeMouseDown(0.1))
 *     .build();
 * 
 * result.setActionConfig(downOptions);
 * ObjectCollection startLoc = new ObjectCollection.Builder()
 *     .withLocations(new Location(100, 100))
 *     .build();
 * moveMouse.perform(result, startLoc);
 * mouseDown.perform(result, new ObjectCollection.Builder().build());
 * 
 * // Move to end location while holding button
 * result.setActionConfig(moveOptions);
 * ObjectCollection endLoc = new ObjectCollection.Builder()
 *     .withLocations(new Location(300, 300))
 *     .build();
 * moveMouse.perform(result, endLoc);
 * 
 * // Release mouse button
 * MouseUpOptions upOptions = new MouseUpOptions.Builder()
 *     .setPressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.RIGHT)
 *         .setPauseAfterMouseUp(0.5))
 *     .build();
 * 
 * result.setActionConfig(upOptions);
 * mouseUp.perform(result, new ObjectCollection.Builder().build());
 * 
 * // Scroll the mouse wheel
 * ScrollOptions scrollOptions = new ScrollOptions.Builder()
 *     .setDirection(-5)  // Scroll down 5 units
 *     .setPauseAfterEnd(0.3)
 *     .build();
 * 
 * result.setActionConfig(scrollOptions);
 * scroll.perform(result, new ObjectCollection.Builder().build());
 * }</pre>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li>Building custom interaction patterns</li>
 *   <li>Hovering over elements to trigger tooltips</li>
 *   <li>Drawing or painting operations</li>
 *   <li>Game automation requiring precise mouse control</li>
 *   <li>Simulating human-like mouse movements</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Use higher-level actions (Click, Drag) when possible</li>
 *   <li>Add appropriate delays between actions for realism</li>
 *   <li>Consider screen boundaries when calculating paths</li>
 *   <li>Test movement patterns on different screen resolutions</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.click.Click
 * @see io.github.jspinak.brobot.action.composite.drag.Drag
 * @see io.github.jspinak.brobot.model.element.Location
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions
 * @see io.github.jspinak.brobot.action.basic.scroll.ScrollOptions
 */
package io.github.jspinak.brobot.action.basic.mouse;