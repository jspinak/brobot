/**
 * Conditional action selection based on GUI state or runtime conditions.
 * 
 * <p>This package provides mechanisms for choosing actions dynamically based on
 * the current state of the GUI or other runtime conditions. It enables adaptive
 * automation that can handle multiple scenarios and branch execution paths
 * intelligently.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.select.Select}</b> - 
 *       Main conditional action selector that chooses actions based on criteria</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.select.CommonSelect}</b> - 
 *       Predefined conditional patterns for common scenarios</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.select.SelectActionObject}</b> - 
 *       Container for action selection criteria and alternatives</li>
 * </ul>
 * 
 * <h2>Selection Patterns</h2>
 * 
 * <h3>State-based Selection</h3>
 * <ul>
 *   <li>Choose action based on current application state</li>
 *   <li>Different actions for different screens</li>
 *   <li>Context-aware automation</li>
 * </ul>
 * 
 * <h3>Element-based Selection</h3>
 * <ul>
 *   <li>If element A exists, perform action X</li>
 *   <li>If element B exists, perform action Y</li>
 *   <li>Default action if no elements found</li>
 * </ul>
 * 
 * <h3>Condition-based Selection</h3>
 * <ul>
 *   <li>Select based on previous action results</li>
 *   <li>Choose based on time of day or duration</li>
 *   <li>Select based on system state or resources</li>
 * </ul>
 * 
 * <h3>Priority-based Selection</h3>
 * <ul>
 *   <li>Try preferred action first</li>
 *   <li>Fallback to alternatives on failure</li>
 *   <li>Ordered list of action attempts</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * 
 * <ul>
 *   <li><b>Multi-path Navigation</b> - Handle different UI layouts</li>
 *   <li><b>Error Handling</b> - Different actions for error states</li>
 *   <li><b>Cross-platform Support</b> - Platform-specific actions</li>
 *   <li><b>A/B Testing</b> - Handle different UI versions</li>
 *   <li><b>Dynamic Forms</b> - Adapt to varying form fields</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Select action based on what's visible on screen
 * Select select = new Select(...);
 * 
 * SelectActionObject selection = new SelectActionObject.Builder()
 *     .when("login_button.png").then(
 *         new ActionOptions.Builder()
 *             .setAction(ActionType.CLICK)
 *             .build(),
 *         new ObjectCollection.Builder()
 *             .withImages("login_button.png")
 *             .build()
 *     )
 *     .when("logout_button.png").then(
 *         new ActionOptions.Builder()
 *             .setAction(ActionType.CLICK)
 *             .build(),
 *         new ObjectCollection.Builder()
 *             .withImages("logout_button.png")
 *             .build()
 *     )
 *     .otherwise(
 *         new ActionOptions.Builder()
 *             .setAction(ActionType.TYPE)
 *             .build(),
 *         new ObjectCollection.Builder()
 *             .withStrings("{ESC}")
 *             .build()
 *     )
 *     .build();
 * 
 * ActionResult result = select.perform(selection);
 * 
 * // Common selection patterns
 * CommonSelect commonSelect = new CommonSelect(...);
 * 
 * // Click one of several possible buttons
 * ActionResult buttonResult = commonSelect.clickFirstFound(
 *     "submit_button.png",
 *     "ok_button.png", 
 *     "continue_button.png",
 *     "next_button.png"
 * );
 * 
 * // Handle different error messages
 * Map<String, ActionHandler> errorHandlers = new HashMap<>();
 * errorHandlers.put("network_error.png", () -> {
 *     // Retry network operation
 * });
 * errorHandlers.put("auth_error.png", () -> {
 *     // Re-authenticate
 * });
 * errorHandlers.put("data_error.png", () -> {
 *     // Clean up and retry
 * });
 * 
 * ActionResult errorResult = commonSelect.handleErrors(errorHandlers);
 * 
 * // Platform-specific actions
 * SelectActionObject platformSelect = new SelectActionObject.Builder()
 *     .whenPlatform("Windows").then(windowsAction)
 *     .whenPlatform("Mac").then(macAction)
 *     .whenPlatform("Linux").then(linuxAction)
 *     .build();
 * }</pre>
 * 
 * <h2>Selection Strategies</h2>
 * 
 * <ul>
 *   <li><b>First Match</b> - Execute action for first matching condition</li>
 *   <li><b>Best Match</b> - Evaluate all conditions, choose best fit</li>
 *   <li><b>All Matches</b> - Execute actions for all matching conditions</li>
 *   <li><b>Weighted Selection</b> - Choose based on confidence scores</li>
 * </ul>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <ul>
 *   <li><b>Nested Selections</b> - Selections within selections</li>
 *   <li><b>Dynamic Conditions</b> - Runtime condition evaluation</li>
 *   <li><b>Learning Selection</b> - Adapt based on success rates</li>
 *   <li><b>Parallel Evaluation</b> - Check conditions concurrently</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Order conditions by likelihood or priority</li>
 *   <li>Always include a default/otherwise action</li>
 *   <li>Keep selection logic simple and clear</li>
 *   <li>Log which branch was taken for debugging</li>
 *   <li>Test all possible paths thoroughly</li>
 *   <li>Consider timeout implications for condition checks</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.composite.select.Select
 * @see io.github.jspinak.brobot.action.composite.select.CommonSelect
 * @see io.github.jspinak.brobot.action.ActionOptions
 */
package io.github.jspinak.brobot.action.composite.select;