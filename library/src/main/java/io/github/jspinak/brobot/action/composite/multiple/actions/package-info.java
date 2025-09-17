/**
 * Sequential and parallel execution of heterogeneous actions.
 *
 * <p>This package enables the execution of different types of actions in coordinated sequences or
 * parallel batches. It provides the infrastructure for complex workflows that require multiple
 * action types working together to achieve sophisticated automation goals.
 *
 * <h2>Key Classes</h2>
 *
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActions}</b> -
 *       Executes a sequence of different action types with shared context
 *   <li><b>{@link
 *       io.github.jspinak.brobot.action.composite.multiple.actions.MultipleBasicActions}</b> -
 *       Optimized execution of multiple basic actions
 *   <li><b>{@link
 *       io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActionsObject}</b> -
 *       Container for multiple action configurations
 *   <li><b>{@link
 *       io.github.jspinak.brobot.action.composite.multiple.actions.ActionConfigObjectCollectionPair}</b>
 *       - Pairs action configuration with target objects
 * </ul>
 *
 * <h2>Execution Patterns</h2>
 *
 * <h3>Sequential Execution</h3>
 *
 * <p>Actions executed in order with results flowing between them:
 *
 * <ul>
 *   <li>Find → Click → Type → Verify
 *   <li>Navigate → Wait → Extract → Validate
 *   <li>Setup → Execute → Cleanup
 * </ul>
 *
 * <h3>Conditional Execution</h3>
 *
 * <p>Actions executed based on previous results:
 *
 * <ul>
 *   <li>If found → click, else → search alternative
 *   <li>Try primary method → fallback on failure
 *   <li>Branch based on classification results
 * </ul>
 *
 * <h3>Parallel Batch Processing</h3>
 *
 * <p>Independent actions executed concurrently:
 *
 * <ul>
 *   <li>Click multiple buttons simultaneously
 *   <li>Search for multiple patterns in parallel
 *   <li>Type in multiple fields concurrently
 * </ul>
 *
 * <h2>Result Management</h2>
 *
 * <ul>
 *   <li><b>Cumulative Results</b> - Aggregate all action outcomes
 *   <li><b>Result Chaining</b> - Pass results between actions
 *   <li><b>Error Tracking</b> - Track which actions failed
 *   <li><b>Timing Analysis</b> - Performance metrics for each action
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Create a multi-action workflow
 * MultipleActions multiActions = new MultipleActions(...);
 *
 * // Define a sequence of actions
 * List<ActionConfigObjectCollectionPair> workflow = new ArrayList<>();
 *
 * // Step 1: Find the search box
 * workflow.add(new ActionConfigObjectCollectionPair(
 *     new ActionConfig.Builder()
 *         .setAction(ActionType.FIND)
 *         .setFind(Find.BEST)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withImages("search_box.png")
 *         .build()
 * ));
 *
 * // Step 2: Click on it
 * workflow.add(new ActionConfigObjectCollectionPair(
 *     new ActionConfig.Builder()
 *         .setAction(ActionType.CLICK)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withImages("search_box.png")
 *         .build()
 * ));
 *
 * // Step 3: Type search query
 * workflow.add(new ActionConfigObjectCollectionPair(
 *     new ActionConfig.Builder()
 *         .setAction(ActionType.TYPE)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withStrings("automation testing")
 *         .build()
 * ));
 *
 * // Step 4: Press Enter
 * workflow.add(new ActionConfigObjectCollectionPair(
 *     new ActionConfig.Builder()
 *         .setAction(ActionType.TYPE)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withStrings("{ENTER}")
 *         .build()
 * ));
 *
 * // Step 5: Wait for results
 * workflow.add(new ActionConfigObjectCollectionPair(
 *     new ActionConfig.Builder()
 *         .setAction(ActionType.VANISH)
 *         .setMaxWait(5.0)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withImages("loading_indicator.png")
 *         .build()
 * ));
 *
 * // Execute the workflow
 * ActionResult workflowResult = multiActions.perform(
 *     new ActionResult(),
 *     workflow.toArray(new ActionConfigObjectCollectionPair[0])
 * );
 *
 * // Check results
 * if (workflowResult.isSuccess()) {
 *     System.out.println("Search completed successfully");
 *     System.out.println("Total duration: " + workflowResult.getDuration());
 * }
 * }</pre>
 *
 * <h2>Advanced Features</h2>
 *
 * <ul>
 *   <li><b>Dynamic Workflows</b> - Build action sequences at runtime
 *   <li><b>Error Recovery</b> - Define fallback actions on failure
 *   <li><b>Context Sharing</b> - Share data between actions
 *   <li><b>Performance Optimization</b> - Reuse screen captures when possible
 * </ul>
 *
 * <h2>Best Practices</h2>
 *
 * <ul>
 *   <li>Group related actions into logical workflows
 *   <li>Use descriptive names for action sequences
 *   <li>Include verification steps between major operations
 *   <li>Handle partial failures gracefully
 *   <li>Log each step for debugging complex workflows
 *   <li>Consider timeout implications for long sequences
 * </ul>
 *
 * @see io.github.jspinak.brobot.action.ActionConfig
 * @see io.github.jspinak.brobot.action.ObjectCollection
 * @see io.github.jspinak.brobot.action.ActionResult
 */
package io.github.jspinak.brobot.action.composite.multiple.actions;
