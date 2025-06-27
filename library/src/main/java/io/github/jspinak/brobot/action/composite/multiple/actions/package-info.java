/**
 * Sequential and parallel execution of heterogeneous actions.
 * 
 * <p>This package enables the execution of different types of actions in coordinated
 * sequences or parallel batches. It provides the infrastructure for complex workflows
 * that require multiple action types working together to achieve sophisticated
 * automation goals.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActions}</b> - 
 *       Executes a sequence of different action types with shared context</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.actions.MultipleBasicActions}</b> - 
 *       Optimized execution of multiple basic actions</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActionsObject}</b> - 
 *       Container for multiple action configurations</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.multiple.actions.ActionOptionsObjectCollectionPair}</b> - 
 *       Pairs action configuration with target objects</li>
 * </ul>
 * 
 * <h2>Execution Patterns</h2>
 * 
 * <h3>Sequential Execution</h3>
 * <p>Actions executed in order with results flowing between them:</p>
 * <ul>
 *   <li>Find → Click → Type → Verify</li>
 *   <li>Navigate → Wait → Extract → Validate</li>
 *   <li>Setup → Execute → Cleanup</li>
 * </ul>
 * 
 * <h3>Conditional Execution</h3>
 * <p>Actions executed based on previous results:</p>
 * <ul>
 *   <li>If found → click, else → search alternative</li>
 *   <li>Try primary method → fallback on failure</li>
 *   <li>Branch based on classification results</li>
 * </ul>
 * 
 * <h3>Parallel Batch Processing</h3>
 * <p>Independent actions executed concurrently:</p>
 * <ul>
 *   <li>Click multiple buttons simultaneously</li>
 *   <li>Search for multiple patterns in parallel</li>
 *   <li>Type in multiple fields concurrently</li>
 * </ul>
 * 
 * <h2>Result Management</h2>
 * 
 * <ul>
 *   <li><b>Cumulative Results</b> - Aggregate all action outcomes</li>
 *   <li><b>Result Chaining</b> - Pass results between actions</li>
 *   <li><b>Error Tracking</b> - Track which actions failed</li>
 *   <li><b>Timing Analysis</b> - Performance metrics for each action</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Create a multi-action workflow
 * MultipleActions multiActions = new MultipleActions(...);
 * 
 * // Define a sequence of actions
 * List<ActionOptionsObjectCollectionPair> workflow = new ArrayList<>();
 * 
 * // Step 1: Find the search box
 * workflow.add(new ActionOptionsObjectCollectionPair(
 *     new ActionOptions.Builder()
 *         .setAction(ActionOptions.Action.FIND)
 *         .setFind(Find.BEST)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withImages("search_box.png")
 *         .build()
 * ));
 * 
 * // Step 2: Click on it
 * workflow.add(new ActionOptionsObjectCollectionPair(
 *     new ActionOptions.Builder()
 *         .setAction(ActionOptions.Action.CLICK)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withImages("search_box.png")
 *         .build()
 * ));
 * 
 * // Step 3: Type search query
 * workflow.add(new ActionOptionsObjectCollectionPair(
 *     new ActionOptions.Builder()
 *         .setAction(ActionOptions.Action.TYPE)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withStrings("automation testing")
 *         .build()
 * ));
 * 
 * // Step 4: Press Enter
 * workflow.add(new ActionOptionsObjectCollectionPair(
 *     new ActionOptions.Builder()
 *         .setAction(ActionOptions.Action.TYPE)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withStrings("{ENTER}")
 *         .build()
 * ));
 * 
 * // Step 5: Wait for results
 * workflow.add(new ActionOptionsObjectCollectionPair(
 *     new ActionOptions.Builder()
 *         .setAction(ActionOptions.Action.VANISH)
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
 *     workflow.toArray(new ActionOptionsObjectCollectionPair[0])
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
 *   <li><b>Dynamic Workflows</b> - Build action sequences at runtime</li>
 *   <li><b>Error Recovery</b> - Define fallback actions on failure</li>
 *   <li><b>Context Sharing</b> - Share data between actions</li>
 *   <li><b>Performance Optimization</b> - Reuse screen captures when possible</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Group related actions into logical workflows</li>
 *   <li>Use descriptive names for action sequences</li>
 *   <li>Include verification steps between major operations</li>
 *   <li>Handle partial failures gracefully</li>
 *   <li>Log each step for debugging complex workflows</li>
 *   <li>Consider timeout implications for long sequences</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.action.ObjectCollection
 * @see io.github.jspinak.brobot.action.ActionResult
 */
package io.github.jspinak.brobot.action.composite.multiple.actions;