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
 *       Library of single-action patterns with common configurations</li>
 *   <li><b>{@link io.github.jspinak.brobot.action.composite.chains.ActionSequenceBuilder}</b> - 
 *       Multi-step action sequences for complex interactions</li>
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
 * // Use a common action pattern
 * CommonActions common = new CommonActions(...);
 * 
 * // Click and wait for page load
 * ActionResult result = common.clickAndWait(
 *     "submit_button.png", 
 *     "success_message.png",
 *     10.0  // timeout in seconds
 * );
 * 
 * // Navigate through menu hierarchy
 * CommonMultipleActions multiActions = new CommonMultipleActions(...);
 * 
 * List<String> menuPath = Arrays.asList(
 *     "file_menu.png",
 *     "open_submenu.png", 
 *     "recent_files.png"
 * );
 * 
 * ActionResult navResult = multiActions.navigateMenuPath(menuPath);
 * 
 * // Fill a form with multiple fields
 * Map<String, String> formData = new HashMap<>();
 * formData.put("name_field.png", "John Doe");
 * formData.put("email_field.png", "john@example.com");
 * formData.put("phone_field.png", "555-1234");
 * 
 * ActionResult formResult = multiActions.fillForm(formData);
 * 
 * // Execute a series of mouse movements
 * MultipleMoves moves = new MultipleMoves(...);
 * 
 * List<Location> path = Arrays.asList(
 *     new Location(100, 100),
 *     new Location(200, 150),
 *     new Location(300, 200)
 * );
 * 
 * ActionResult moveResult = moves.followPath(path);
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
 * @see io.github.jspinak.brobot.action.composite.chains.ActionFacade
 * @see io.github.jspinak.brobot.action.composite.chains.ActionSequenceBuilder
 * @see io.github.jspinak.brobot.action.Action
 */
package io.github.jspinak.brobot.action.composite.chains;