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
 * <p>Drag behavior can be customized through {@link io.github.jspinak.brobot.action.ActionOptions}:</p>
 * <ul>
 *   <li><b>Drag speed</b> - Control movement velocity</li>
 *   <li><b>Hold delays</b> - Pause duration at source and destination</li>
 *   <li><b>Path style</b> - Straight line or curved motion</li>
 *   <li><b>Offset adjustments</b> - Fine-tune grab and drop points</li>
 *   <li><b>Verification</b> - Post-drag validation options</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Basic drag from one image to another
 * Drag drag = new Drag(...);
 * 
 * ObjectCollection dragTargets = new ObjectCollection.Builder()
 *     .withImages("drag_handle.png", "drop_zone.png")
 *     .build();
 * 
 * ActionOptions dragOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.DRAG)
 *     .setDragSpeed(DragSpeed.MEDIUM)
 *     .build();
 * 
 * ActionResult result = drag.perform(new ActionResult(), dragTargets);
 * 
 * // Drag with offsets
 * ObjectCollection offsetTargets = new ObjectCollection.Builder()
 *     .withImages("item.png", "trash.png")
 *     .build();
 * 
 * ActionOptions offsetOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.DRAG)
 *     .setFromOffsetX(5)   // Grab 5 pixels from center
 *     .setToOffsetY(-10)   // Drop 10 pixels above center
 *     .build();
 * 
 * // Multiple sequential drags
 * MultipleDrags multipleDrags = new MultipleDrags(...);
 * 
 * List<ObjectCollection> dragSequence = Arrays.asList(
 *     new ObjectCollection.Builder().withImages("item1.png", "slot1.png").build(),
 *     new ObjectCollection.Builder().withImages("item2.png", "slot2.png").build(),
 *     new ObjectCollection.Builder().withImages("item3.png", "slot3.png").build()
 * );
 * 
 * // Drag to specific coordinates
 * ObjectCollection coordDrag = new ObjectCollection.Builder()
 *     .withImages("draggable.png")
 *     .withLocations(new Location(500, 300))
 *     .build();
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