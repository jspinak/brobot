/**
 * Data model classes for the Brobot DSL.
 * 
 * <p>This package contains the core data structures that represent automation
 * workflows in the Brobot Domain-Specific Language. These models bridge the gap
 * between JSON configuration and executable automation logic.</p>
 * 
 * <h2>Core Components</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.TaskSequence} - 
 *       An ordered sequence of automation steps</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.ActionStep} - 
 *       A single action with its configuration and targets</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.Parameter} - 
 *       Parameter definition for business tasks</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.BuilderMethod} - 
 *       Method call in a builder pattern chain</li>
 * </ul>
 * 
 * <h2>Task Sequences</h2>
 * 
 * <p>Task sequences represent ordered lists of actions to perform:</p>
 * <pre>{@code
 * // JSON representation
 * {
 *   "name": "loginSequence",
 *   "steps": [
 *     {
 *       "actionOptions": {
 *         "action": "CLICK"
 *       },
 *       "objectCollection": {
 *         "stateImages": ["usernameField"]
 *       }
 *     },
 *     {
 *       "actionOptions": {
 *         "action": "TYPE",
 *         "typeString": "admin"
 *       },
 *       "objectCollection": {
 *         "stateImages": ["usernameField"]
 *       }
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <h2>Action Steps</h2>
 * 
 * <p>Each action step combines:</p>
 * <ul>
 *   <li><b>ActionOptions</b> - What action to perform and how</li>
 *   <li><b>ObjectCollection</b> - What objects to perform it on</li>
 * </ul>
 * 
 * <h3>Example Step</h3>
 * <pre>{@code
 * ActionStep clickLogin = new ActionStep(
 *     new ActionOptions.Builder()
 *         .action(Action.CLICK)
 *         .pauseAfter(1000)
 *         .build(),
 *     new ObjectCollection.Builder()
 *         .withImages("loginButton")
 *         .build()
 * );
 * }</pre>
 * 
 * <h2>Parameters</h2>
 * 
 * <p>Parameters define inputs to business tasks:</p>
 * <pre>{@code
 * // String parameter
 * new Parameter("username", "string", false, null)
 * 
 * // Optional number with default
 * new Parameter("timeout", "number", true, 5000)
 * 
 * // Object parameter
 * new Parameter("options", "object", false, null)
 * }</pre>
 * 
 * <h2>Builder Methods</h2>
 * 
 * <p>Builder methods support fluent object construction:</p>
 * <pre>{@code
 * // Represents: .action(Action.CLICK).pauseAfter(500)
 * List<BuilderMethod> methods = Arrays.asList(
 *     new BuilderMethod("action", Arrays.asList("CLICK")),
 *     new BuilderMethod("pauseAfter", Arrays.asList(500))
 * );
 * }</pre>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ul>
 *   <li><b>Serializable</b> - All models support JSON serialization</li>
 *   <li><b>Immutable</b> - Models are effectively immutable after creation</li>
 *   <li><b>Validated</b> - Schema validation ensures correctness</li>
 *   <li><b>Self-contained</b> - Each step contains all needed information</li>
 * </ul>
 * 
 * <h2>Integration</h2>
 * 
 * <p>These models integrate with:</p>
 * <ul>
 *   <li>JSON parsing for configuration loading</li>
 *   <li>DSL execution engine for runtime interpretation</li>
 *   <li>Action framework for step execution</li>
 *   <li>Validation system for correctness checking</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.action.ActionOptions
 * @see io.github.jspinak.brobot.model.ObjectCollection
 * @see io.github.jspinak.brobot.runner.dsl.BusinessTask
 */
package io.github.jspinak.brobot.runner.dsl.model;