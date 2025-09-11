/**
 * Domain-Specific Language (DSL) for defining automation instructions.
 *
 * <p>This package implements a declarative language for expressing business logic in Brobot
 * automation projects. It allows users to define complex automation workflows using JSON
 * configuration rather than direct Java programming, implementing the Automation Instructions (ι)
 * component of the Applied Model.
 *
 * <h2>Core Components</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.InstructionSet} - Container for automation
 *       instruction definitions
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.BusinessTask} - A reusable unit of business
 *       logic with parameters and statements
 * </ul>
 *
 * <h2>DSL Structure</h2>
 *
 * <p>The DSL provides constructs for:
 *
 * <ul>
 *   <li><b>Business Tasks</b> - Named, parameterized automation procedures
 *   <li><b>Statements</b> - Control flow and action execution
 *   <li><b>Expressions</b> - Value computation and method calls
 *   <li><b>Task Sequences</b> - Ordered lists of automation steps
 * </ul>
 *
 * <h2>Language Features</h2>
 *
 * <h3>Task Definition</h3>
 *
 * <pre>{@code
 * {
 *   "name": "loginTask",
 *   "parameters": [
 *     {"name": "username", "type": "string"},
 *     {"name": "password", "type": "string"}
 *   ],
 *   "statements": [
 *     {
 *       "type": "methodCall",
 *       "method": "click",
 *       "target": "loginButton"
 *     }
 *   ]
 * }
 * }</pre>
 *
 * <h3>Control Flow</h3>
 *
 * <ul>
 *   <li>If/else conditionals
 *   <li>For-each loops over collections
 *   <li>Variable declarations and assignments
 *   <li>Return statements
 * </ul>
 *
 * <h3>Expressions</h3>
 *
 * <ul>
 *   <li>Literals (string, number, boolean)
 *   <li>Variable references
 *   <li>Method calls
 *   <li>Binary operations
 *   <li>Builder pattern expressions
 * </ul>
 *
 * <h2>Task Sequences</h2>
 *
 * <p>Task sequences define ordered lists of actions:
 *
 * <pre>{@code
 * TaskSequence loginSequence = new TaskSequence();
 * loginSequence.addStep(new ActionStep(
 *     clickAction,
 *     usernameField
 * ));
 * loginSequence.addStep(new ActionStep(
 *     typeAction,
 *     usernameValue
 * ));
 * }</pre>
 *
 * <h2>Design Philosophy</h2>
 *
 * <p>The DSL is designed to be:
 *
 * <ul>
 *   <li><b>Declarative</b> - Focus on what to do, not how
 *   <li><b>Type-safe</b> - Validated against schemas
 *   <li><b>Composable</b> - Build complex workflows from simple tasks
 *   <li><b>Readable</b> - Clear intent without programming knowledge
 *   <li><b>Extensible</b> - Easy to add new statement and expression types
 * </ul>
 *
 * <h2>Execution Model</h2>
 *
 * <ol>
 *   <li>Parse JSON into DSL objects
 *   <li>Validate structure and references
 *   <li>Build execution context with parameters
 *   <li>Execute statements sequentially
 *   <li>Return results to caller
 * </ol>
 *
 * <h2>Integration</h2>
 *
 * <p>The DSL integrates with Brobot's core framework:
 *
 * <ul>
 *   <li>Translates to Action Model operations
 *   <li>References states from State Structure
 *   <li>Leverages navigation capabilities
 *   <li>Produces standard ActionResult objects
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.action
 * @see io.github.jspinak.brobot.model.action
 */
package io.github.jspinak.brobot.runner.dsl;
