/**
 * Expression evaluation components for the Brobot DSL.
 *
 * <p>This package provides the expression tree implementation for the Brobot Domain-Specific
 * Language. Expressions represent values that can be computed at runtime, forming the building
 * blocks for more complex statements and control flow in automation instructions.
 *
 * <h2>Expression Types</h2>
 *
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.Expression} - Base interface for all
 *       expression types
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression} - Constant values
 *       (strings, numbers, booleans)
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression} - References to
 *       variables in the execution context
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.MethodCallExpression} - Method
 *       invocations with arguments
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.BinaryOperationExpression} - Binary
 *       operations (arithmetic, comparison, logical)
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.BuilderExpression} - Builder pattern
 *       object construction
 * </ul>
 *
 * <h2>Expression Examples</h2>
 *
 * <h3>Literals</h3>
 *
 * <pre>{@code
 * // JSON representation
 * {"type": "literal", "value": "Hello World"}
 * {"type": "literal", "value": 42}
 * {"type": "literal", "value": true}
 * }</pre>
 *
 * <h3>Variables</h3>
 *
 * <pre>{@code
 * // Reference a parameter or local variable
 * {"type": "variable", "name": "username"}
 * }</pre>
 *
 * <h3>Method Calls</h3>
 *
 * <pre>{@code
 * // Call a method with arguments
 * {
 *   "type": "methodCall",
 *   "target": {"type": "variable", "name": "state"},
 *   "method": "find",
 *   "arguments": [
 *     {"type": "literal", "value": "button.png"}
 *   ]
 * }
 * }</pre>
 *
 * <h3>Binary Operations</h3>
 *
 * <pre>{@code
 * // Arithmetic and comparisons
 * {
 *   "type": "binaryOp",
 *   "operator": "+",
 *   "left": {"type": "variable", "name": "count"},
 *   "right": {"type": "literal", "value": 1}
 * }
 * }</pre>
 *
 * <h3>Builder Pattern</h3>
 *
 * <pre>{@code
 * // Construct objects using builder pattern
 * {
 *   "type": "builder",
 *   "className": "ActionConfig",
 *   "methods": [
 *     {"name": "action", "arguments": [{"type": "literal", "value": "CLICK"}]},
 *     {"name": "pauseAfter", "arguments": [{"type": "literal", "value": 500}]}
 *   ]
 * }
 * }</pre>
 *
 * <h2>Evaluation Context</h2>
 *
 * <p>Expressions are evaluated within an execution context that provides:
 *
 * <ul>
 *   <li>Parameter values from task invocation
 *   <li>Local variables declared in statements
 *   <li>Access to Brobot framework services
 *   <li>Built-in functions and operations
 * </ul>
 *
 * <h2>Type System</h2>
 *
 * <p>The expression system supports:
 *
 * <ul>
 *   <li>Primitive types: string, number, boolean
 *   <li>Object types: Any Java object
 *   <li>Collections: Lists and arrays
 *   <li>Null values
 * </ul>
 *
 * <h2>Design Principles</h2>
 *
 * <ul>
 *   <li><b>Immutable</b> - Expression objects are immutable once created
 *   <li><b>Composable</b> - Complex expressions built from simple ones
 *   <li><b>Type-safe</b> - Runtime type checking with clear errors
 *   <li><b>Extensible</b> - Easy to add new expression types
 * </ul>
 *
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.dsl.statements
 * @see io.github.jspinak.brobot.runner.dsl.BusinessTask
 */
package io.github.jspinak.brobot.runner.dsl.expressions;
