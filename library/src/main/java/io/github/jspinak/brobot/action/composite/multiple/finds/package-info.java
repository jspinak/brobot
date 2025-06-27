/**
 * Advanced find operations with multiple strategies and hierarchical searching.
 * 
 * <p>This package extends the basic find capabilities with sophisticated multi-find
 * operations including nested searches, confirmed finds, and batch pattern matching.
 * These advanced find operations enable complex visual search scenarios that go beyond
 * simple pattern matching.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.finds.MultipleFinds}</b> - 
 *       Execute multiple find operations with different strategies</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.finds.NestedFinds}</b> - 
 *       Find patterns within previously found regions</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.finds.ConfirmedFinds}</b> - 
 *       Find operations with additional verification criteria</li>
 * </ul>
 * 
 * <h2>Advanced Find Patterns</h2>
 * 
 * <h3>Hierarchical Searching</h3>
 * <p>Search for elements within specific containers:</p>
 * <ul>
 *   <li>Find buttons within dialog boxes</li>
 *   <li>Locate items within list containers</li>
 *   <li>Search text within specific regions</li>
 *   <li>Nested pattern matching</li>
 * </ul>
 * 
 * <h3>Multi-Strategy Searching</h3>
 * <p>Apply different find strategies to the same targets:</p>
 * <ul>
 *   <li>Try BEST first, then ALL if needed</li>
 *   <li>Combine image and text searching</li>
 *   <li>Use motion detection as fallback</li>
 *   <li>Apply different similarity thresholds</li>
 * </ul>
 * 
 * <h3>Verified Matching</h3>
 * <p>Ensure found elements meet additional criteria:</p>
 * <ul>
 *   <li>Verify surrounding context</li>
 *   <li>Check element state or appearance</li>
 *   <li>Confirm stability over time</li>
 *   <li>Validate against expected properties</li>
 * </ul>
 * 
 * <h2>Use Cases</h2>
 * 
 * <ul>
 *   <li><b>Complex UI Navigation</b> - Find elements in dynamic layouts</li>
 *   <li><b>Data Extraction</b> - Locate and extract structured information</li>
 *   <li><b>State Verification</b> - Confirm multiple UI elements are present</li>
 *   <li><b>Adaptive Searching</b> - Adjust strategies based on results</li>
 *   <li><b>Performance Optimization</b> - Search only within relevant areas</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Nested find - find buttons within a dialog
 * NestedFinds nestedFind = new NestedFinds(...);
 * 
 * // First find the dialog
 * ObjectCollection dialogTarget = new ObjectCollection.Builder()
 *     .withImages("dialog_box.png")
 *     .build();
 * 
 * // Then find buttons within the dialog
 * ObjectCollection buttonTargets = new ObjectCollection.Builder()
 *     .withImages("ok_button.png", "cancel_button.png")
 *     .build();
 * 
 * ActionResult nestedResult = nestedFind.findInside(
 *     dialogTarget, 
 *     buttonTargets
 * );
 * 
 * // Multiple find strategies
 * MultipleFinds multiFind = new MultipleFinds(...);
 * 
 * // Define different strategies to try
 * List<ActionOptions> strategies = Arrays.asList(
 *     new ActionOptions.Builder()
 *         .setFind(Find.BEST)
 *         .setSimilarity(0.95)
 *         .build(),
 *     new ActionOptions.Builder()
 *         .setFind(Find.ALL)
 *         .setSimilarity(0.85)
 *         .build(),
 *     new ActionOptions.Builder()
 *         .setFind(Find.COLOR)
 *         .build()
 * );
 * 
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("dynamic_element.png")
 *     .build();
 * 
 * ActionResult multiResult = multiFind.perform(strategies, targets);
 * 
 * // Confirmed find - verify found elements
 * ConfirmedFinds confirmedFind = new ConfirmedFinds(...);
 * 
 * // Find with confirmation criteria
 * ActionOptions confirmOptions = new ActionOptions.Builder()
 *     .setFind(Find.ALL)
 *     .setConfirmationImage("checked_state.png")
 *     .setConfirmationTimeout(2.0)
 *     .build();
 * 
 * ActionResult confirmedResult = confirmedFind.perform(
 *     confirmOptions,
 *     new ObjectCollection.Builder()
 *         .withImages("checkbox.png")
 *         .build()
 * );
 * 
 * // Process only confirmed matches
 * for (Match match : confirmedResult.getMatches()) {
 *     if (match.isConfirmed()) {
 *         // Process confirmed checkbox
 *     }
 * }
 * }</pre>
 * 
 * <h2>Performance Considerations</h2>
 * 
 * <ul>
 *   <li><b>Region Constraints</b> - Narrow search areas for faster results</li>
 *   <li><b>Strategy Ordering</b> - Try fastest strategies first</li>
 *   <li><b>Caching</b> - Reuse results when possible</li>
 *   <li><b>Early Termination</b> - Stop when sufficient matches found</li>
 *   <li><b>Parallel Execution</b> - Run independent searches concurrently</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Use nested finds to reduce search space</li>
 *   <li>Apply confirmation only when necessary</li>
 *   <li>Order strategies by likelihood of success</li>
 *   <li>Set appropriate timeouts for each strategy</li>
 *   <li>Log strategy performance for optimization</li>
 *   <li>Consider memory usage with large result sets</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.basic.find.Find
 * @see io.github.jspinak.brobot.model.Match
 * @see io.github.jspinak.brobot.action.ActionOptions.Find
 */
package io.github.jspinak.brobot.action.composite.multiple.finds;