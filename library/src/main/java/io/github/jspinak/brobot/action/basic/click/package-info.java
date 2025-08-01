/**
 * Mouse click operations for GUI interaction.
 * 
 * <p>This package contains the click action implementation, which combines visual pattern
 * recognition with precise mouse control to interact with GUI elements. The Click action
 * is one of the most fundamental interactive actions in GUI automation.</p>
 * 
 * <h2>Core Functionality</h2>
 * 
 * <p>The {@link io.github.jspinak.brobot.action.basic.click.Click} action performs:</p>
 * <ul>
 *   <li>Single clicks, double clicks, or custom multi-clicks</li>
 *   <li>Left, middle, or right mouse button clicks</li>
 *   <li>Clicks on found image patterns, specific locations, or defined regions</li>
 *   <li>Batch clicking on multiple targets</li>
 *   <li>Click-and-hold operations</li>
 * </ul>
 * 
 * <h2>Click Process</h2>
 * 
 * <ol>
 *   <li>Find target element(s) using visual pattern matching</li>
 *   <li>Calculate click location (center, offset, or specific point)</li>
 *   <li>Move mouse to target location</li>
 *   <li>Perform click operation with specified parameters</li>
 *   <li>Execute post-click actions if configured</li>
 *   <li>Update state management if integrated</li>
 * </ol>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <p>Click behavior can be customized through {@link io.github.jspinak.brobot.action.basic.click.ClickOptions}:</p>
 * <ul>
 *   <li><b>Number of clicks</b> - Single, double, or multi-click via setNumberOfClicks()</li>
 *   <li><b>Mouse button</b> - LEFT, MIDDLE, or RIGHT via MousePressOptions</li>
 *   <li><b>Click location</b> - Center, offset via setClickLocationOffset(), or absolute position</li>
 *   <li><b>Timing</b> - Precise control via MousePressOptions (pauseBeforeMouseDown, pauseAfterMouseUp, etc.)</li>
 *   <li><b>Find strategy</b> - Configured separately in PatternFindOptions when chaining</li>
 *   <li><b>Movement</b> - DelayBeforeMouseMove and delayAfterMouseMove for timing control</li>
 * </ul>
 * 
 * <h2>Integration Features</h2>
 * 
 * <ul>
 *   <li><b>State Management</b> - Automatic state transition on successful clicks</li>
 *   <li><b>Action Chaining</b> - Combine with other actions for complex workflows</li>
 *   <li><b>Error Handling</b> - Configurable retry and fallback behaviors</li>
 *   <li><b>Mocking Support</b> - Full offline testing capabilities</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Simple click on an image using ActionConfig (recommended)
 * Click click = new Click(...);
 * 
 * ClickOptions clickOptions = new ClickOptions.Builder()
 *     .setNumberOfClicks(1)
 *     .setMousePressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.LEFT)
 *         .build())
 *     .build();
 * 
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("submit_button.png")
 *     .build();
 * 
 * ActionResult result = click.perform(clickOptions, targets);
 * 
 * // Double-click with offset
 * ClickOptions doubleClickOptions = new ClickOptions.Builder()
 *     .setNumberOfClicks(2)
 *     .setMousePressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.LEFT)
 *         .build())
 *     .setClickLocationOffset(new LocationOffset(10, -5))  // 10 pixels right, 5 pixels up
 *     .build();
 * 
 * // Right-click example
 * ClickOptions rightClickOptions = new ClickOptions.Builder()
 *     .setMousePressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.RIGHT)
 *         .setPauseBeforeMouseDown(0.5)
 *         .setPauseAfterMouseUp(0.5)
 *         .build())
 *     .build();
 * 
 * // Click at specific location
 * ObjectCollection locationTarget = new ObjectCollection.Builder()
 *     .withLocations(new Location(500, 300))
 *     .build();
 * 
 * ActionResult locationResult = click.perform(clickOptions, locationTarget);
 * 
 * // Using convenience utilities
 * ClickOptions quickDouble = ActionConfigShortcuts.doubleClick();
 * ActionResult quickResult = click.perform(quickDouble, targets);
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Use appropriate find strategies for reliable element location</li>
 *   <li>Add small delays for applications that need time to respond</li>
 *   <li>Consider using state management for complex navigation</li>
 *   <li>Test with different similarity thresholds for robust matching</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.click.Click
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.action.composite.ClickVerify
 */
package io.github.jspinak.brobot.action.basic.click;