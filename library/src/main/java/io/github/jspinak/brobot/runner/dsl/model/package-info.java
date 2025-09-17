/**
 * Data model classes for the Brobot DSL.
 *
 * <p>This package contains the core data structures that represent automation workflows in the
 * Brobot Domain-Specific Language. These models bridge the gap between JSON configuration and
 * executable automation logic.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.TaskSequence} - An ordered sequence of
 *       automation steps
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.ActionStep} - A single action with its
 *       configuration and targets
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.Parameter} - Parameter definition for
 *       business tasks
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.model.BuilderMethod} - Method call in a builder
 *       pattern chain
 * </ul>
 *
 * <h2>Task Sequences</h2>
 *
 * <p>Task sequences represent ordered lists of actions to perform:
 *
 * <pre>{@code
 * // JSON representation
 * {
 *   "name": "loginSequence",
 *   "steps": [
 *     {
 *       "actionConfig": {
 *         "action": "CLICK"
 *       },
 *       "objectCollection": {
 *         "stateImages": ["usernameField"]
 *       }
 *     },
 *     {
 *       "actionConfig": {
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
 * <p>Each action step combines:
 *
 * <ul>
 *   <li><b>ActionConfig</b> - What action to perform and how
 *   <li><b>ObjectCollection</b> - What objects to perform it on
 * </ul>
 *
 * <h3>Example Step</h3>
 *
 * <pre>{@code
 * ActionStep clickLogin = new ActionStep(
 *     new ActionConfig.Builder()
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
 * <p>Parameters define inputs to business tasks:
 *
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
 * <p>Builder methods support fluent object construction:
 *
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
 *   <li><b>Serializable</b> - All models support JSON serialization
 *   <li><b>Immutable</b> - Models are effectively immutable after creation
 *   <li><b>Validated</b> - Schema validation ensures correctness
 *   <li><b>Self-contained</b> - Each step contains all needed information
 * </ul>
 *
 * <h2>Integration</h2>
 *
 * <p>These models integrate with:
 *
 * <ul>
 *   <li>JSON parsing for configuration loading
 *   <li>DSL execution engine for runtime interpretation
 *   <li>Action framework for step execution
 *   <li>Validation system for correctness checking
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action.ActionConfig
 * @see io.github.jspinak.brobot.model.ObjectCollection
 * @see io.github.jspinak.brobot.runner.dsl.BusinessTask
 */
package io.github.jspinak.brobot.runner.dsl.model;
