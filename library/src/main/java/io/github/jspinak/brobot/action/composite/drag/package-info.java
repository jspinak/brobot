/**
 * Drag-and-drop operations for GUI element manipulation.
 * 
 * <p>This package provides comprehensive drag-and-drop functionality, enabling complex
 * mouse-based interactions where elements need to be moved from one location to another.
 * The drag actions handle the intricate coordination of finding source and destination
 * elements, executing smooth drag motions, and verifying successful completion.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.drag.Drag}</b> - 
 *       Main drag-and-drop action supporting various source and destination types</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.drag.SimpleDrag}</b> - 
 *       Streamlined drag operation for basic use cases</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.drag.MultipleDrags}</b> - 
 *       Execute multiple drag operations in sequence</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.drag.CommonDrag}</b> - 
 *       Predefined drag patterns for common scenarios</li>
 * </ul>
 * 
 * <h2>Drag Operation Flow</h2>
 * 
 * <ol>
 *   <li>Find the source element (image, region, or location)</li>
 *   <li>Move mouse to source and press button</li>
 *   <li>Find the destination element</li>
 *   <li>Drag mouse to destination while holding button</li>
 *   <li>Release mouse button at destination</li>
 *   <li>Verify successful drop (optional)</li>
 * </ol>
 * 
 * <h2>Supported Drag Types</h2>
 * 
 * <ul>
 *   <li><b>Image to Image</b> - Drag from one visual pattern to another</li>
 *   <li><b>Location to Location</b> - Drag between specific coordinates</li>
 *   <li><b>Region to Region</b> - Drag between defined areas</li>
 *   <li><b>Mixed Types</b> - Any combination of the above</li>
 * </ul>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <p>Drag behavior can be customized through {@link io.github.jspinak.brobot.action.composite.drag.DragOptions}:</p>
 * <ul>
 *   <li><b>Mouse button</b> - Configure via MousePressOptions (LEFT, MIDDLE, RIGHT)</li>
 *   <li><b>Hold delays</b> - PauseBeforeMouseDown, pauseBeforeMouseUp via MousePressOptions</li>
 *   <li><b>Movement timing</b> - DelayBetweenMouseDownAndMove, delayAfterDrag</li>
 *   <li><b>Offset adjustments</b> - Use separate ObjectCollections for source and destination</li>
 *   <li><b>Verification</b> - Chain with find actions using ActionChainOptions</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Basic drag using ActionConfig (recommended)
 * Drag drag = new Drag(...);
 * 
 * DragOptions dragOptions = new DragOptions.Builder()
 *     .setMousePressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.LEFT)
 *         .setPauseBeforeMouseDown(0.5)
 *         .setPauseBeforeMouseUp(0.5)
 *         .build())
 *     .setDelayBetweenMouseDownAndMove(0.3)
 *     .setDelayAfterDrag(0.5)
 *     .build();
 * 
 * // Source and destination as separate ObjectCollections
 * ObjectCollection source = new ObjectCollection.Builder()
 *     .withImages("drag_handle.png")
 *     .build();
 * 
 * ObjectCollection destination = new ObjectCollection.Builder()
 *     .withImages("drop_zone.png")
 *     .build();
 * 
 * ActionResult result = drag.perform(dragOptions, source, destination);
 * 
 * // Drag with offsets using locations
 * ObjectCollection sourceWithOffset = new ObjectCollection.Builder()
 *     .withImages("item.png")
 *     .withLocations(new Location(5, 0))  // 5 pixels right from center
 *     .build();
 * 
 * ObjectCollection destWithOffset = new ObjectCollection.Builder()
 *     .withImages("trash.png")
 *     .withLocations(new Location(0, -10))  // 10 pixels above center
 *     .build();
 * 
 * // Using action chaining for complex drag operations
 * PatternFindOptions findSource = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .build();
 * 
 * PatternFindOptions findDest = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .build();
 * 
 * // Create a drag chain: find source, find destination, drag
 * ActionChainOptions dragChain = new ActionChainOptions.Builder(findSource)
 *     .then(findDest)
 *     .then(dragOptions)
 *     .build();
 * 
 * // Drag to specific coordinates
 * ObjectCollection coordDest = new ObjectCollection.Builder()
 *     .withLocations(new Location(500, 300))
 *     .build();
 * 
 * drag.perform(dragOptions, source, coordDest);
 * }</pre>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <ul>
 *   <li><b>Smart Target Resolution</b> - Automatically determines drag endpoints</li>
 *   <li><b>Motion Smoothing</b> - Natural drag movements</li>
 *   <li><b>Error Recovery</b> - Handles failed drags gracefully</li>
 *   <li><b>State Integration</b> - Updates state on successful drags</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * 
 * <ul>
 *   <li>File management (drag files to folders)</li>
 *   <li>List reordering (drag to rearrange items)</li>
 *   <li>Game automation (drag game pieces)</li>
 *   <li>UI customization (drag widgets to positions)</li>
 *   <li>Data visualization (drag chart elements)</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Verify source element is draggable before attempting</li>
 *   <li>Use appropriate drag speeds for the application</li>
 *   <li>Add small delays at endpoints for reliability</li>
 *   <li>Consider using SimpleDrag for basic operations</li>
 *   <li>Test drag operations at different screen resolutions</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.composite.drag.Drag
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseDown
 * @see io.github.jspinak.brobot.action.basic.mouse.MouseUp
 */
package io.github.jspinak.brobot.action.composite.drag;