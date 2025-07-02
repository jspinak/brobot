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
 * // Modern approach: Use ActionChainOptions for sequences
 * ActionChainOptions menuSequence = new ActionChainOptions.Builder(
 *         new PatternFindOptions.Builder()
 *             .setStrategy(PatternFindOptions.Strategy.FIRST)
 *             .build())
 *     .then(new ClickOptions.Builder()
 *             .setPauseAfterEnd(0.5)
 *             .build())
 *     .then(new TypeOptions.Builder()
 *             .setTypeDelay(0.05)
 *             .build())
 *     .build();
 * 
 * ActionResult result = chainExecutor.executeChain(menuSequence,
 *     new ActionResult(),
 *     menuImage.asObjectCollection(),
 *     menuItemImage.asObjectCollection(),
 *     new ObjectCollection.Builder().withStrings("search text").build()
 * );
 * 
 * // Execute multiple finds with different strategies
 * PatternFindOptions findAll = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.ALL)
 *     .build();
 * 
 * PatternFindOptions findBest = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .setSimilarity(0.95)
 *     .build();
 * 
 * // Execute finds separately and aggregate results
 * ActionResult result1 = new ActionResult();
 * result1.setActionConfig(findAll);
 * find.perform(result1, targets);
 * 
 * ActionResult result2 = new ActionResult();
 * result2.setActionConfig(findBest);
 * find.perform(result2, targets);
 * 
 * // Nested finds using NESTED chaining strategy
 * ActionChainOptions nestedFind = new ActionChainOptions.Builder(
 *         new PatternFindOptions.Builder().build())
 *     .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
 *     .then(new PatternFindOptions.Builder().build())
 *     .build();
 * 
 * ActionResult nestedResult = chainExecutor.executeChain(nestedFind,
 *     new ActionResult(),
 *     containerImage.asObjectCollection(),
 *     nestedElementImage.asObjectCollection()
 * );
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
 * @see io.github.jspinak.brobot.action.ActionChainOptions
 * @see io.github.jspinak.brobot.action.internal.execution.ActionChainExecutor
 * @see io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActions
 * @see io.github.jspinak.brobot.action.composite.multiple.finds.MultipleFinds
 */
package io.github.jspinak.brobot.action.composite.multiple;