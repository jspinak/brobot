/**
 * Actions that repeat until specific conditions are met.
 * 
 * <p>This package provides composite actions that execute repeatedly based on
 * various termination conditions. These actions enable robust automation that
 * adapts to dynamic interfaces and handles timing uncertainties through
 * intelligent retry mechanisms.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.repeat.ClickUntil}</b> - 
 *       Repeatedly clicks until a condition is satisfied</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.repeat.DoUntilActionObject}</b> - 
 *       Executes any action repeatedly until a condition is met</li>
 * </ul>
 * 
 * <h2>Termination Conditions</h2>
 * 
 * <h3>Element Appearance</h3>
 * <ul>
 *   <li>Continue until specific element appears</li>
 *   <li>Wait for success indicator</li>
 *   <li>Detect state changes</li>
 * </ul>
 * 
 * <h3>Element Disappearance</h3>
 * <ul>
 *   <li>Continue until element vanishes</li>
 *   <li>Wait for loading indicators to clear</li>
 *   <li>Ensure dialogs are closed</li>
 * </ul>
 * 
 * <h3>Count-based</h3>
 * <ul>
 *   <li>Execute fixed number of times</li>
 *   <li>Limit maximum iterations</li>
 *   <li>Minimum execution guarantees</li>
 * </ul>
 * 
 * <h3>Time-based</h3>
 * <ul>
 *   <li>Continue for specified duration</li>
 *   <li>Timeout protection</li>
 *   <li>Interval-based execution</li>
 * </ul>
 * 
 * <h2>Common Use Cases</h2>
 * 
 * <ul>
 *   <li><b>Navigation</b> - Click "Next" until reaching desired page</li>
 *   <li><b>Data Processing</b> - Process items until list is empty</li>
 *   <li><b>Synchronization</b> - Wait for asynchronous operations</li>
 *   <li><b>Error Recovery</b> - Retry operations until successful</li>
 *   <li><b>Pagination</b> - Navigate through multi-page results</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Click Next button until the last page indicator appears
 * ClickUntil clickUntil = new ClickUntil(...);
 * 
 * ActionOptions untilOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.CLICK)
 *     .setUntilImageAppears("last_page_indicator.png")
 *     .setMaxIterations(20)
 *     .setPauseBetweenActions(1.0)
 *     .build();
 * 
 * ObjectCollection nextButton = new ObjectCollection.Builder()
 *     .withImages("next_button.png")
 *     .build();
 * 
 * ActionResult result = clickUntil.perform(untilOptions, nextButton);
 * 
 * System.out.println("Clicked " + result.getRepetitions() + " times");
 * 
 * // Generic repeat action - scroll until element is visible
 * DoUntilActionObject doUntil = new DoUntilActionObject(...);
 * 
 * ActionOptions scrollOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.SCROLL_MOUSE_WHEEL)
 *     .setScrollDirection(-3)  // Scroll down
 *     .build();
 * 
 * DoUntilActionObject.UntilCondition condition = 
 *     new DoUntilActionObject.UntilCondition()
 *         .untilImageAppears("target_element.png")
 *         .withMaxAttempts(10)
 *         .withTimeout(30.0);
 * 
 * ActionResult scrollResult = doUntil.perform(
 *     scrollOptions, 
 *     new ObjectCollection.Builder().build(),
 *     condition
 * );
 * 
 * // Click submit button until success message or error appears
 * ActionOptions submitOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.CLICK)
 *     .setUntilImageAppears("success_message.png", "error_message.png")
 *     .setMaxWait(5.0)  // Wait 5 seconds between clicks
 *     .setMaxIterations(3)
 *     .build();
 * 
 * ObjectCollection submitButton = new ObjectCollection.Builder()
 *     .withImages("submit_button.png")
 *     .build();
 * 
 * ActionResult submitResult = clickUntil.perform(submitOptions, submitButton);
 * 
 * // Determine which condition was met
 * if (submitResult.getMatches().stream()
 *     .anyMatch(m -> m.getName().contains("success"))) {
 *     System.out.println("Submission successful!");
 * } else {
 *     System.out.println("Submission failed or timed out");
 * }
 * }</pre>
 * 
 * <h2>Configuration Options</h2>
 * 
 * <ul>
 *   <li><b>Max Iterations</b> - Prevent infinite loops</li>
 *   <li><b>Pause Between Actions</b> - Allow GUI to respond</li>
 *   <li><b>Timeout</b> - Overall time limit</li>
 *   <li><b>Success Criteria</b> - Multiple termination conditions</li>
 *   <li><b>Failure Handling</b> - Actions on max iterations reached</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Always set maximum iterations to prevent infinite loops</li>
 *   <li>Include appropriate pauses for GUI responsiveness</li>
 *   <li>Use multiple termination conditions when possible</li>
 *   <li>Log iteration counts for performance analysis</li>
 *   <li>Consider state changes between iterations</li>
 *   <li>Handle both success and failure scenarios</li>
 * </ul>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <ul>
 *   <li><b>Dynamic Conditions</b> - Modify conditions during execution</li>
 *   <li><b>Progress Tracking</b> - Monitor iteration progress</li>
 *   <li><b>Adaptive Timing</b> - Adjust delays based on response times</li>
 *   <li><b>Partial Success</b> - Handle scenarios where some iterations succeed</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.composite.repeat.ClickUntil
 * @see io.github.jspinak.brobot.action.composite.repeat.DoUntilActionObject
 * @see io.github.jspinak.brobot.action.ActionOptions
 */
package io.github.jspinak.brobot.action.composite.repeat;