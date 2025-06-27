/**
 * Batch and parallel execution of multiple actions.
 * 
 * <p>This package provides mechanisms for executing multiple actions efficiently,
 * either on multiple targets or combining different action types. It enables
 * batch processing, parallel execution, and sophisticated multi-action workflows
 * that would be cumbersome to implement with individual action calls.</p>
 * 
 * <h2>Subpackages</h2>
 * 
 * <ul>
 *   <li><b>actions</b> - Execute different action types in sequence or parallel</li>
 *   <li><b>finds</b> - Specialized multiple find operations with advanced matching</li>
 * </ul>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <h3>Batch Processing</h3>
 * <p>Execute the same action on multiple targets efficiently:</p>
 * <ul>
 *   <li>Click multiple buttons in sequence</li>
 *   <li>Type text in multiple fields</li>
 *   <li>Find all instances of multiple patterns</li>
 * </ul>
 * 
 * <h3>Action Composition</h3>
 * <p>Combine different action types into complex workflows:</p>
 * <ul>
 *   <li>Find, click, and verify in one operation</li>
 *   <li>Mixed action sequences with shared context</li>
 *   <li>Conditional execution based on intermediate results</li>
 * </ul>
 * 
 * <h3>Result Aggregation</h3>
 * <p>Collect and combine results from multiple operations:</p>
 * <ul>
 *   <li>Aggregate all matches from multiple finds</li>
 *   <li>Track success/failure of each operation</li>
 *   <li>Provide comprehensive execution reports</li>
 * </ul>
 * 
 * <h2>Performance Benefits</h2>
 * 
 * <ul>
 *   <li><b>Reduced Overhead</b> - Single initialization for multiple operations</li>
 *   <li><b>Parallel Execution</b> - Run independent actions concurrently</li>
 *   <li><b>Shared Context</b> - Reuse screen captures and analysis</li>
 *   <li><b>Optimized Workflows</b> - Minimize redundant operations</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Execute multiple finds with different strategies
 * MultipleFinds multiFind = new MultipleFinds(...);
 * 
 * List<ActionOptions> findConfigs = Arrays.asList(
 *     new ActionOptions.Builder().setFind(Find.ALL).build(),
 *     new ActionOptions.Builder().setFind(Find.BEST).build(),
 *     new ActionOptions.Builder().setFind(Find.MOTION).build()
 * );
 * 
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("button.png", "icon.png", "text_field.png")
 *     .build();
 * 
 * ActionResult allFinds = multiFind.perform(findConfigs, targets);
 * 
 * // Execute different actions in sequence
 * MultipleActions multiAction = new MultipleActions(...);
 * 
 * List<ActionOptionsObjectCollectionPair> actionSequence = Arrays.asList(
 *     new ActionOptionsObjectCollectionPair(
 *         new ActionOptions.Builder().setAction(FIND).build(),
 *         new ObjectCollection.Builder().withImages("menu.png").build()
 *     ),
 *     new ActionOptionsObjectCollectionPair(
 *         new ActionOptions.Builder().setAction(CLICK).build(),
 *         new ObjectCollection.Builder().withImages("menu_item.png").build()
 *     ),
 *     new ActionOptionsObjectCollectionPair(
 *         new ActionOptions.Builder().setAction(TYPE).build(),
 *         new ObjectCollection.Builder().withStrings("search text").build()
 *     )
 * );
 * 
 * ActionResult sequenceResult = multiAction.perform(actionSequence);
 * 
 * // Nested finds - find within found regions
 * NestedFinds nestedFind = new NestedFinds(...);
 * 
 * ObjectCollection outerTargets = new ObjectCollection.Builder()
 *     .withImages("container.png")
 *     .build();
 * 
 * ObjectCollection innerTargets = new ObjectCollection.Builder()
 *     .withImages("nested_element.png")
 *     .build();
 * 
 * ActionResult nestedResult = nestedFind.findInside(outerTargets, innerTargets);
 * }</pre>
 * 
 * <h2>Advanced Features</h2>
 * 
 * <h3>Confirmed Finds</h3>
 * <p>Find operations with additional verification:</p>
 * <ul>
 *   <li>Verify found elements meet additional criteria</li>
 *   <li>Filter matches based on context</li>
 *   <li>Ensure stability before proceeding</li>
 * </ul>
 * 
 * <h3>Nested Operations</h3>
 * <p>Operations within found regions:</p>
 * <ul>
 *   <li>Find elements within containers</li>
 *   <li>Hierarchical searching</li>
 *   <li>Context-aware matching</li>
 * </ul>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Group related operations for efficiency</li>
 *   <li>Use parallel execution for independent actions</li>
 *   <li>Handle partial failures gracefully</li>
 *   <li>Log individual operation results for debugging</li>
 *   <li>Consider memory usage with large batch operations</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActions
 * @see io.github.jspinak.brobot.action.composite.multiple.finds.MultipleFinds
 */
package io.github.jspinak.brobot.action.composite.multiple;