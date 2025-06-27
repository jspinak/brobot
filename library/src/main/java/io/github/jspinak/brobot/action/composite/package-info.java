/**
 * Composite actions that orchestrate multiple basic actions to perform complex GUI automation tasks.
 * 
 * <p>This package contains higher-level actions built by composing basic actions into
 * sophisticated workflows. Composite actions encapsulate common patterns and complex
 * interactions that would otherwise require multiple coordinated basic actions.</p>
 * 
 * <h2>Design Philosophy</h2>
 * 
 * <p>Composite actions follow these principles:</p>
 * <ul>
 *   <li><b>Reusability</b> - Encapsulate common interaction patterns</li>
 *   <li><b>Atomicity</b> - Present complex operations as single actions</li>
 *   <li><b>Consistency</b> - Maintain the same interface as basic actions</li>
 *   <li><b>Flexibility</b> - Support customization through ActionOptions</li>
 * </ul>
 * 
 * <h2>Categories of Composite Actions</h2>
 * 
 * <h3>Drag Operations (drag package)</h3>
 * <ul>
 *   <li>Drag-and-drop between elements</li>
 *   <li>Complex dragging patterns</li>
 *   <li>Multi-step drag operations</li>
 * </ul>
 * 
 * <h3>Action Chains (chains package)</h3>
 * <ul>
 *   <li>Predefined sequences of common actions</li>
 *   <li>Reusable interaction patterns</li>
 *   <li>Complex navigation workflows</li>
 * </ul>
 * 
 * <h3>Multiple Actions (multiple package)</h3>
 * <ul>
 *   <li>Batch operations on multiple targets</li>
 *   <li>Parallel action execution</li>
 *   <li>Aggregated results from multiple operations</li>
 * </ul>
 * 
 * <h3>Conditional Actions (select package)</h3>
 * <ul>
 *   <li>Choose actions based on GUI state</li>
 *   <li>Branching logic within actions</li>
 *   <li>Adaptive behavior patterns</li>
 * </ul>
 * 
 * <h3>Repeated Actions (repeat package)</h3>
 * <ul>
 *   <li>Actions that repeat until conditions are met</li>
 *   <li>Polling and retry mechanisms</li>
 *   <li>Iterative interactions</li>
 * </ul>
 * 
 * <h3>Verified Actions (verify package)</h3>
 * <ul>
 *   <li>Actions with built-in verification</li>
 *   <li>Ensure expected outcomes</li>
 *   <li>Automatic error detection and handling</li>
 * </ul>
 * 
 * <h2>Common Patterns</h2>
 * 
 * <p>Composite actions implement these common automation patterns:</p>
 * <ul>
 *   <li><b>Click and Verify</b> - Click followed by verification of result</li>
 *   <li><b>Type and Confirm</b> - Text entry with validation</li>
 *   <li><b>Find and Interact</b> - Locate element then perform action</li>
 *   <li><b>Wait and Proceed</b> - Synchronization before next action</li>
 *   <li><b>Retry on Failure</b> - Automatic retry with backoff</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Drag and drop operation
 * Drag drag = new Drag(...);
 * 
 * ObjectCollection dragTargets = new ObjectCollection.Builder()
 *     .withImages("drag_source.png", "drop_target.png")
 *     .build();
 * 
 * ActionResult dragResult = drag.perform(new ActionResult(), dragTargets);
 * 
 * // Click until condition is met
 * ClickUntil clickUntil = new ClickUntil(...);
 * 
 * ActionOptions clickUntilOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.CLICK)
 *     .setUntilImageAppears("success_message.png")
 *     .setMaxIterations(5)
 *     .build();
 * 
 * // Verified click with automatic retry
 * ClickVerify clickVerify = new ClickVerify(...);
 * 
 * ActionOptions verifyOptions = new ActionOptions.Builder()
 *     .setAction(ActionOptions.Action.CLICK)
 *     .setVerifyImage("button_pressed.png")
 *     .setRetryOnFailure(true)
 *     .build();
 * }</pre>
 * 
 * <h2>Creating Custom Composite Actions</h2>
 * 
 * <p>To create new composite actions:</p>
 * <ol>
 *   <li>Implement {@link io.github.jspinak.brobot.action.ActionInterface}</li>
 *   <li>Compose basic actions in the perform method</li>
 *   <li>Aggregate results appropriately</li>
 *   <li>Handle errors and edge cases</li>
 *   <li>Document the composite behavior clearly</li>
 * </ol>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Use composite actions for repeated patterns</li>
 *   <li>Keep composite actions focused on a single task</li>
 *   <li>Provide meaningful default configurations</li>
 *   <li>Document expected preconditions and outcomes</li>
 *   <li>Consider performance implications of multiple operations</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.ActionInterface
 * @see io.github.jspinak.brobot.action.composite.drag.Drag
 * @see io.github.jspinak.brobot.action.composite.chains.ActionFacade
 */
package io.github.jspinak.brobot.action.composite;