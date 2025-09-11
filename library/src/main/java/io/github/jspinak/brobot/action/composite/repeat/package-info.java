/**
 * Actions that repeat until specific conditions are met.
 *
 * <p>This package provides composite actions that execute repeatedly based on various termination
 * conditions. These actions enable robust automation that adapts to dynamic interfaces and handles
 * timing uncertainties through intelligent retry mechanisms.
 *
 * <h2>Key Classes</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.repeat.ClickUntil}</b> - Repeatedly
 *       clicks until a condition is satisfied (deprecated - use action chaining)
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.repeat.DoUntilActionObject}</b> -
 *       Executes any action repeatedly until a condition is met
 *   <li><b>{@link io.github.jspinak.brobot.action.ActionChainOptions}</b> - Modern approach for
 *       conditional action sequences
 * </ul>
 *
 * <h2>Termination Conditions</h2>
 *
 * <h3>Element Appearance</h3>
 *
 * <ul>
 *   <li>Continue until specific element appears
 *   <li>Wait for success indicator
 *   <li>Detect state changes
 * </ul>
 *
 * <h3>Element Disappearance</h3>
 *
 * <ul>
 *   <li>Continue until element vanishes
 *   <li>Wait for loading indicators to clear
 *   <li>Ensure dialogs are closed
 * </ul>
 *
 * <h3>Count-based</h3>
 *
 * <ul>
 *   <li>Execute fixed number of times
 *   <li>Limit maximum iterations
 *   <li>Minimum execution guarantees
 * </ul>
 *
 * <h3>Time-based</h3>
 *
 * <ul>
 *   <li>Continue for specified duration
 *   <li>Timeout protection
 *   <li>Interval-based execution
 * </ul>
 *
 * <h2>Common Use Cases</h2>
 *
 * <ul>
 *   <li><b>Navigation</b> - Click "Next" until reaching desired page
 *   <li><b>Data Processing</b> - Process items until list is empty
 *   <li><b>Synchronization</b> - Wait for asynchronous operations
 *   <li><b>Error Recovery</b> - Retry operations until successful
 *   <li><b>Pagination</b> - Navigate through multi-page results
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Modern approach: Use action chaining for repeated actions
 * // Click Next button until the last page indicator appears
 * ActionChainOptions clickUntilLast = new ActionChainOptions.Builder(
 *         new ClickOptions.Builder()
 *             .setPauseAfterEnd(1.0)
 *             .build())
 *     .setMaxRepetitions(20)
 *     .setStopCondition(matches ->
 *         matches.stream().anyMatch(m -> m.getName().contains("last_page")))
 *     .build();
 *
 * ActionResult result = chainExecutor.executeChain(clickUntilLast,
 *     new ActionResult(),
 *     nextButton.asObjectCollection());
 *
 * // Use ClickUntilOptions with the deprecated ClickUntil action
 * ClickUntilOptions untilOptions = new ClickUntilOptions.Builder()
 *     .setClickOptions(new ClickOptions.Builder().build())
 *     .setUntilFinds(new PatternFindOptions.Builder()
 *         .setStrategy(PatternFindOptions.Strategy.FIRST)
 *         .build())
 *     .setMaxRepetitions(20)
 *     .setPauseBetweenActions(1.0)
 *     .build();
 *
 * result.setActionConfig(untilOptions);
 * clickUntil.perform(result,
 *     nextButton.asObjectCollection(),
 *     lastPageIndicator.asObjectCollection());
 *
 * // Scroll until element is visible using action chaining
 * ActionChainOptions scrollUntilVisible = new ActionChainOptions.Builder(
 *         new ScrollOptions.Builder()
 *             .setDirection(-3)  // Scroll down
 *             .build())
 *     .setMaxRepetitions(10)
 *     .setStopCondition(matches -> !matches.isEmpty())
 *     .then(new PatternFindOptions.Builder()
 *             .setPauseBeforeBegin(0.5)
 *             .build())
 *     .build();
 *
 * ActionResult scrollResult = chainExecutor.executeChain(scrollUntilVisible,
 *     new ActionResult(),
 *     new ObjectCollection.Builder().build(),  // For scroll
 *     targetElement.asObjectCollection());     // For find
 *
 * // Click submit button until success message or error appears
 * ActionChainOptions submitUntilResponse = new ActionChainOptions.Builder(
 *         new ClickOptions.Builder()
 *             .setPauseAfterEnd(5.0)  // Wait 5 seconds between clicks
 *             .build())
 *     .setMaxRepetitions(3)
 *     .then(new PatternFindOptions.Builder()
 *             .setStrategy(PatternFindOptions.Strategy.FIRST)
 *             .build())
 *     .setStopCondition(matches ->
 *         matches.stream().anyMatch(m ->
 *             m.getName().contains("success") || m.getName().contains("error")))
 *     .build();
 *
 * ActionResult submitResult = chainExecutor.executeChain(submitUntilResponse,
 *     new ActionResult(),
 *     submitButton.asObjectCollection(),
 *     new ObjectCollection.Builder()
 *         .withImages(successMessage, errorMessage)
 *         .build());
 * }</pre>
 *
 * <h2>Configuration Options</h2>
 *
 * <ul>
 *   <li><b>Max Iterations</b> - Prevent infinite loops
 *   <li><b>Pause Between Actions</b> - Allow GUI to respond
 *   <li><b>Timeout</b> - Overall time limit
 *   <li><b>Success Criteria</b> - Multiple termination conditions
 *   <li><b>Failure Handling</b> - Actions on max iterations reached
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Always set maximum iterations to prevent infinite loops
 *   <li>Include appropriate pauses for GUI responsiveness
 *   <li>Use multiple termination conditions when possible
 *   <li>Log iteration counts for performance analysis
 *   <li>Consider state changes between iterations
 *   <li>Handle both success and failure scenarios
 * </ul>
 *
 * <h2>Advanced Features</h2>
 *
 * <ul>
 *   <li><b>Dynamic Conditions</b> - Modify conditions during execution
 *   <li><b>Progress Tracking</b> - Monitor iteration progress
 *   <li><b>Adaptive Timing</b> - Adjust delays based on response times
 *   <li><b>Partial Success</b> - Handle scenarios where some iterations succeed
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.composite.repeat.ClickUntil
 * @see io.github.jspinak.brobot.action.composite.repeat.ClickUntilOptions
 * @see io.github.jspinak.brobot.action.ActionChainOptions
 * @see io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor
 */
package io.github.jspinak.brobot.action.composite.repeat;
