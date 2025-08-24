/**
 * Motion detection and dynamic element tracking.
 * 
 * <p>This package provides sophisticated motion detection capabilities for identifying
 * and tracking moving elements in the GUI. It can distinguish between static and dynamic
 * pixels, track moving objects, and identify regions of motion activity.</p>
 * 
 * <h2>Core Capabilities</h2>
 * 
 * <ul>
 *   <li><b>Motion Detection</b> - Identify pixels and regions that change over time</li>
 *   <li><b>Dynamic Tracking</b> - Follow moving elements across frames</li>
 *   <li><b>Static vs Dynamic Classification</b> - Distinguish fixed UI elements from animated ones</li>
 *   <li><b>Motion Visualization</b> - Visual representation of detected motion for debugging</li>
 * </ul>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.motion.FindMotion}</b> - 
 *       Main motion detection action that coordinates the motion analysis process</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.motion.FindDynamicPixelMatches}</b> - 
 *       Identifies pixels that change between frames (moving elements)</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.motion.FindFixedPixelMatches}</b> - 
 *       Identifies pixels that remain constant (static UI elements)</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.motion.FindRegionsOfMotion}</b> - 
 *       Groups motion pixels into coherent regions representing moving objects</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.find.motion.IllustrateMotion}</b> - 
 *       Creates visual representations of detected motion for analysis and debugging</li>
 * </ul>
 * 
 * <h2>Motion Detection Process</h2>
 * 
 * <ol>
 *   <li>Capture multiple frames over a time period</li>
 *   <li>Compare pixels across frames to identify changes</li>
 *   <li>Classify pixels as static or dynamic based on change patterns</li>
 *   <li>Group dynamic pixels into motion regions</li>
 *   <li>Track motion regions across frames</li>
 *   <li>Return motion matches with trajectory information</li>
 * </ol>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li>Tracking animated UI elements (loading spinners, progress bars)</li>
 *   <li>Detecting pop-ups or notifications that slide into view</li>
 *   <li>Following game characters or moving objects</li>
 *   <li>Identifying areas of screen activity</li>
 *   <li>Waiting for animations to complete</li>
 *   <li>Distinguishing between static background and moving foreground</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Detect motion in a specific region
 * FindMotion findMotion = new FindMotion(...);
 * 
 * ActionOptions options = new ActionOptions.Builder()
 *     .setAction(ActionType.FIND)
 *     .setFind(Find.MOTION)
 *     .setMaxWait(5.0)  // Observe for 5 seconds
 *     .build();
 * 
 * ObjectCollection searchRegion = new ObjectCollection.Builder()
 *     .withRegions(Region.SCREEN)  // Or specific region
 *     .build();
 * 
 * ActionResult result = findMotion.perform(new ActionResult(), searchRegion);
 * 
 * // Visualize detected motion
 * IllustrateMotion illustrate = new IllustrateMotion(...);
 * ActionResult visualization = illustrate.perform(result, searchRegion);
 * 
 * // Access motion regions
 * for (Match motionMatch : result.getMatches()) {
 *     System.out.println("Motion detected at: " + motionMatch.getRegion());
 * }
 * }</pre>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li>Motion detection requires capturing multiple frames</li>
 *   <li>Larger regions require more processing time</li>
 *   <li>Adjust frame rate and duration based on expected motion speed</li>
 *   <li>Use region constraints to focus on specific areas</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.imageUtils.MotionDetector
 * @see io.github.jspinak.brobot.action.basic.wait.OnChange
 */
package io.github.jspinak.brobot.action.basic.find.motion;