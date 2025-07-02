/**
 * Predefined action sequences for common automation patterns.
 * 
 * <p>This package provides reusable chains of actions that encapsulate frequently used
 * interaction patterns. These chains simplify complex workflows by combining multiple
 * basic actions into logical, named operations that can be easily reused across
 * different automation scenarios.</p>
 * 
 * <h2>Key Classes</h2>
 * 
 * <ul>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.chains.ActionFacade}</b> - 
 *       Library of single-action patterns with common configurations (deprecated - use ActionConfigFacade)</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.chains.ActionConfigFacade}</b> - 
 *       Modern facade using ActionConfig API with type-safe configurations</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.chains.ActionSequenceBuilder}</b> - 
 *       Multi-step action sequences using ActionChainOptions for complex interactions</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.chains.MultipleMoves}</b> - 
 *       Sequences of mouse movements for path-based operations</li>
 * </ul>
 * 
 * <h2>Common Action Patterns</h2>
 * 
 * <h3>Navigation Patterns</h3>
 * <ul>
 *   <li><b>Click and Wait</b> - Click followed by wait for response</li>
 *   <li><b>Navigate To</b> - Multi-click navigation through menus</li>
 *   <li><b>Return Home</b> - Reset to known starting state</li>
 * </ul>
 * 
 * <h3>Form Interaction Patterns</h3>
 * <ul>
 *   <li><b>Clear and Type</b> - Clear field before entering new text</li>
 *   <li><b>Tab and Type</b> - Navigate form fields with tab key</li>
 *   <li><b>Fill Form</b> - Complete multi-field form entry</li>
 * </ul>
 * 
 * <h3>Verification Patterns</h3>
 * <ul>
 *   <li><b>Click and Verify</b> - Ensure action had expected result</li>
 *   <li><b>Wait and Proceed</b> - Wait for condition before continuing</li>
 *   <li><b>Retry Until Success</b> - Repeat action until successful</li>
 * </ul>
 * 
 * <h2>Benefits of Action Chains</h2>
 * 
 * <ul>
 *   <li><b>Reusability</b> - Define once, use many times</li>
 *   <li><b>Consistency</b> - Standardized approaches to common tasks</li>
 *   <li><b>Maintainability</b> - Update pattern in one place</li>
 *   <li><b>Readability</b> - Self-documenting action names</li>
 *   <li><b>Error Handling</b> - Built-in retry and recovery logic</li>
 * </ul>
 * 
 * <h2>Example Usage</h2>
 * 
 * <pre>{@code
 * // Use the modern ActionConfig facade
 * ActionConfigFacade facade = new ActionConfigFacade(action);
 * 
 * // Simple click with timeout
 * boolean clicked = facade.click(5.0, stateImage);
 * 
 * // Right-click on best match
 * boolean rightClicked = facade.rightClickBest(0.9, stateImage);
 * 
 * // Click until vanished
 * boolean vanished = facade.clickUntilVanished(10, 0.5, targetImage);
 * 
 * // Use ActionSequenceBuilder for complex sequences
 * ActionSequenceBuilder builder = new ActionSequenceBuilder(chainExecutor, action);
 * 
 * // Right-click and move mouse away until element vanishes
 * boolean success = builder.rightClickAndMoveUntilVanishes(
 *     5,      // max attempts
 *     2.0,    // pause between clicks
 *     0.5,    // pause before click
 *     0.3,    // pause after move
 *     image,  // target image
 *     50,     // x offset
 *     -30     // y offset
 * );
 * 
 * // Chain multiple actions with ActionChainOptions
 * ActionChainOptions loginSequence = new ActionChainOptions.Builder(
 *         new ClickOptions.Builder().build())
 *     .then(new TypeOptions.Builder().build())
 *     .then(new ClickOptions.Builder().build())
 *     .then(new TypeOptions.Builder().build())
 *     .then(new ClickOptions.Builder().build())
 *     .build();
 * }</pre>
 * 
 * <h2>Creating Custom Chains</h2>
 * 
 * <p>To create custom action chains:</p>
 * <ol>
 *   <li>Identify repetitive action sequences in your automation</li>
 *   <li>Extract common parameters and variations</li>
 *   <li>Create methods that encapsulate the sequence</li>
 *   <li>Add appropriate error handling and retries</li>
 *   <li>Document expected preconditions and outcomes</li>
 * </ol>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ul>
 *   <li>Keep chains focused on single logical operations</li>
 *   <li>Use descriptive names that indicate the chain's purpose</li>
 *   <li>Provide sensible defaults for common parameters</li>
 *   <li>Include verification steps where appropriate</li>
 *   <li>Allow customization through optional parameters</li>
 *   <li>Log chain execution for debugging</li>
 * </ul>
 * 
 * <h2>Integration with State Management</h2>
 * 
 * <p>Action chains can be integrated with state management:</p>
 * <ul>
 *   <li>Define chains as state transitions</li>
 *   <li>Use chains in state-specific contexts</li>
 *   <li>Trigger state changes on chain completion</li>
 *   <li>Build complex workflows from simple chains</li>
 * </ul>
 * 
 * @see io.github.jspinak.brobot.action.composite.chains.ActionConfigFacade
 * @see io.github.jspinak.brobot.action.composite.chains.ActionSequenceBuilder
 * @see io.github.jspinak.brobot.action.ActionChainOptions
 * @see io.github.jspinak.brobot.action.Action
 */
package io.github.jspinak.brobot.action.composite.chains;