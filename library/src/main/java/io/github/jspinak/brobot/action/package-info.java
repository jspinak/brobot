/**
 * Core action framework for GUI automation in Brobot.
 * 
 * <p>This package provides the fundamental building blocks for executing automated GUI interactions
 * based on the theoretical model described in the paper on model-based GUI automation. Actions are the primary mechanism
 * for interacting with graphical user interfaces through visual recognition and direct manipulation.</p>
 * 
 * <h2>Core Concepts</h2>
 * 
 * <p>In the Brobot model, an atomic action is formally defined as a tuple <b>a = (o_a, E_a, ζ)</b> where:</p>
 * <ul>
 *   <li><b>o_a</b> - Action parameters/options (represented by {@link io.github.jspinak.brobot.action.ActionOptions})</li>
 *   <li><b>E_a</b> - Elements acted upon (represented by {@link io.github.jspinak.brobot.action.ObjectCollection})</li>
 *   <li><b>ζ</b> - Success function specific to the action type</li>
 * </ul>
 * 
 * <p>The action function is defined as <b>f_a: (a, Ξ, Θ) → (Ξ', r_a)</b> where:</p>
 * <ul>
 *   <li><b>Ξ</b> - Visible GUI before action</li>
 *   <li><b>Θ</b> - Environmental stochasticity</li>
 *   <li><b>Ξ'</b> - Resulting GUI after action</li>
 *   <li><b>r_a</b> - Action results (represented by {@link io.github.jspinak.brobot.action.ActionResult})</li>
 * </ul>
 * 
 * <h2>Package Structure</h2>
 * 
 * <ul>
 *   <li><b>Core Classes</b> - {@link io.github.jspinak.brobot.action.Action} serves as the main entry point,
 *       with {@link io.github.jspinak.brobot.action.ActionInterface} defining the contract for all actions</li>
 *   <li><b>basic</b> - Atomic actions that execute in a single iteration (Find, Click, Type, etc.)</li>
 *   <li><b>composite</b> - Higher-level actions composed of multiple basic actions (Drag, ClickUntil, etc.)</li>
 *   <li><b>internal</b> - Infrastructure and support classes for action execution and lifecycle management</li>
 * </ul>
 * 
 * <h2>Action Categories</h2>
 * 
 * <p>Actions are categorized into two main types:</p>
 * <ul>
 *   <li><b>Observation Actions</b> - Gather information about the GUI state without modifying it (Find, GetText)</li>
 *   <li><b>Interactive Actions</b> - Modify the GUI state through user interactions (Click, Type, Drag)</li>
 * </ul>
 * 
 * <h2>Key Features</h2>
 * 
 * <ul>
 *   <li>Standardized action interface for consistent execution patterns</li>
 *   <li>Comprehensive result objects containing matches, success status, and metadata</li>
 *   <li>Flexible configuration through ActionOptions with sensible defaults</li>
 *   <li>Support for both online (live GUI) and offline (mocked) execution</li>
 *   <li>Integration with state management and transition systems</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * 
 * <pre>{@code
 * // Create an action instance
 * Action action = new Action();
 * 
 * // Modern approach using ActionConfig (recommended)
 * ClickOptions clickOptions = new ClickOptions.Builder()
 *     .setNumberOfClicks(1)
 *     .setMousePressOptions(new MousePressOptions.Builder()
 *         .setButton(MouseButton.LEFT)
 *         .build())
 *     .build();
 * 
 * // Define target elements
 * ObjectCollection targets = new ObjectCollection.Builder()
 *     .withImages("button.png")
 *     .build();
 * 
 * // Execute the action
 * ActionResult result = action.perform(clickOptions, targets);
 * 
 * // Check results
 * if (result.isSuccess()) {
 *     System.out.println("Action completed successfully");
 * }
 * 
 * // Action chaining example
 * PatternFindOptions find = new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.BEST)
 *     .setSimilarity(0.85)
 *     .build();
 * 
 * ActionChainOptions chain = new ActionChainOptions.Builder(find)
 *     .then(clickOptions)
 *     .setStrategy(ActionChainOptions.ChainingStrategy.NESTED)
 *     .build();
 * 
 * ActionResult chainResult = action.perform(chain, targets);
 * }</pre>
 * 
 * @see io.github.jspinak.brobot.action.Action
 * @see io.github.jspinak.brobot.action.ActionInterface
 * @see io.github.jspinak.brobot.action.ActionConfig
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.action.ActionChainOptions
 * @see io.github.jspinak.brobot.action.ActionResult
 * @see io.github.jspinak.brobot.action.ObjectCollection
 */
package io.github.jspinak.brobot.action;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
