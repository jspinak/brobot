/**
 * Waiting and monitoring actions for GUI state changes.
 * 
 * <p>This package provides actions that monitor the GUI for changes over time, enabling
 * synchronization with dynamic interfaces and asynchronous operations. These actions are
 * essential for robust automation that adapts to varying response times.</p>
 * 
 * <h2>Wait Actions</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.wait.WaitVanish}</b> - 
 *       Waits for GUI elements to disappear from the screen</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.basic.wait.OnChange}</b> - 
 *       Monitors a region and triggers when changes are detected</li>
 * </ul>
 * 
 * <h2>Wait Strategies</h2>
 * 
 * <h3>Element Disappearance (WaitVanish)</h3>
 * <ul>
 *   <li>Wait for loading indicators to disappear</li>
 *   <li>Confirm dialog closure</li>
 *   <li>Verify successful navigation away from a screen</li>
 *   <li>Ensure temporary notifications have cleared</li>
 * </ul>
 * 
 * <h3>Change Detection (OnChange)</h3>
 * <ul>
 *   <li>Monitor for any pixel changes in a region</li>
 *   <li>Detect animation completion</li>
 *   <li>Wait for dynamic content to load</li>
 *   <li>Trigger on screen updates</li>
 * </ul>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <p>Through {@link io.github.jspinak.brobot.action.ActionOptions}:</p>
 * <ul>
 *   <li><b>Timeout</b> - Maximum time to wait for the condition</li>
 *   <li><b>Poll interval</b> - How often to check for changes</li>
 *   <li><b>Similarity threshold</b> - For vanish detection precision</li>
 *   <li><b>Change sensitivity</b> - Minimum change to trigger OnChange</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Wait for a loading spinner to disappear
 * WaitVanish waitVanish = new WaitVanish(...);
 * 
 * ActionOptions vanishOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.VANISH)
 *     .setMaxWait(30.0)  // Wait up to 30 seconds
 *     .build();
 * 
 * ObjectCollection loadingSpinner = new ObjectCollection.Builder()
 *     .withImages("loading_spinner.png")
 *     .build();
 * 
 * ActionResult vanishResult = waitVanish.perform(new ActionResult(), loadingSpinner);
 * 
 * if (vanishResult.isSuccess()) {
 *     System.out.println("Loading complete!");
 * }
 * 
 * // Monitor a region for any changes
 * OnChange onChange = new OnChange(...);
 * 
 * ActionOptions changeOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.ON_CHANGE)
 *     .setMaxWait(10.0)
 *     .setPollInterval(0.5)  // Check every 500ms
 *     .build();
 * 
 * ObjectCollection monitorRegion = new ObjectCollection.Builder()
 *     .withRegions(new Region(100, 100, 300, 200))
 *     .build();
 * 
 * ActionResult changeResult = onChange.perform(new ActionResult(), monitorRegion);
 * 
 * // Wait for multiple elements to vanish
 * ObjectCollection multipleElements = new ObjectCollection.Builder()
 *     .withImages("popup.png", "overlay.png", "modal.png")
 *     .build();
 * 
 * ActionResult allVanished = waitVanish.perform(new ActionResult(), multipleElements);
 * }</pre>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li>Synchronizing with page loads and transitions</li>
 *   <li>Waiting for animations to complete</li>
 *   <li>Detecting dynamic content updates</li>
 *   <li>Ensuring dialogs and popups have closed</li>
 *   <li>Monitoring for asynchronous operations</li>
 *   <li>Implementing custom wait conditions</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Set reasonable timeouts to avoid infinite waits</li>
 *   <li>Use specific regions to improve performance</li>
 *   <li>Combine with state management for context-aware waiting</li>
 *   <li>Consider using Find with retries for element appearance</li>
 *   <li>Log wait times for performance analysis</li>
 * </ul>
 * 
 * <h2>Relationship to Other Actions</h2>
 * 
 * <p>Wait actions complement other actions:</p>
 * <ul>
 *   <li>Use after Click to wait for response</li>
 *   <li>Combine with Find for "wait until appears" behavior</li>
 *   <li>Chain with state transitions for complex workflows</li>
 *   <li>Use with motion detection for activity monitoring</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.action.basic.find.motion.FindMotion
 */
package io.github.jspinak.brobot.action.basic.wait;