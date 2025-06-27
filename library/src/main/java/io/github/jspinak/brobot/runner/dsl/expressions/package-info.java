/**
 * Expression evaluation components for the Brobot DSL.
 * 
 * <p>This package provides the expression tree implementation for the Brobot
 * Domain-Specific Language. Expressions represent values that can be computed
 * at runtime, forming the building blocks for more complex statements and
 * control flow in automation instructions.</p>
 * 
 * <h2>Expression Types</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.Expression} - 
 *       Base interface for all expression types</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.LiteralExpression} - 
 *       Constant values (strings, numbers, booleans)</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression} - 
 *       References to variables in the execution context</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.MethodCallExpression} - 
 *       Method invocations with arguments</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.BinaryOperationExpression} - 
 *       Binary operations (arithmetic, comparison, logical)</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.expressions.BuilderExpression} - 
 *       Builder pattern object construction</li>
 * </ul>
 * 
 * <h2>Expression Examples</h2>
 * 
 * <h3>Literals</h3>
 * <pre>{@code
 * // JSON representation
 * {"type": "literal", "value": "Hello World"}
 * {"type": "literal", "value": 42}
 * {"type": "literal", "value": true}
 * }</pre>
 * 
 * <h3>Variables</h3>
 * <pre>{@code
 * // Reference a parameter or local variable
 * {"type": "variable", "name": "username"}
 * }</pre>
 * 
 * <h3>Method Calls</h3>
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
 * <pre>{@code
 * // Construct objects using builder pattern
 * {
 *   "type": "builder",
 *   "className": "ActionOptions",
 *   "methods": [
 *     {"name": "action", "arguments": [{"type": "literal", "value": "CLICK"}]},
 *     {"name": "pauseAfter", "arguments": [{"type": "literal", "value": 500}]}
 *   ]
 * }
 * }</pre>
 * 
 * <h2>Evaluation Context</h2>
 * 
 * <p>Expressions are evaluated within an execution context that provides:</p>
 * <ul>
 *   <li>Parameter values from task invocation</li>
 *   <li>Local variables declared in statements</li>
 *   <li>Access to Brobot framework services</li>
 *   <li>Built-in functions and operations</li>
 * </ul>
 * 
 * <h2>Type System</h2>
 * 
 * <p>The expression system supports:</p>
 * <ul>
 *   <li>Primitive types: string, number, boolean</li>
 *   <li>Object types: Any Java object</li>
 *   <li>Collections: Lists and arrays</li>
 *   <li>Null values</li>
 * </ul>
 * 
 * <h2>Design Principles</h2>
 * 
 * <ul>
 *   <li><b>Immutable</b> - Expression objects are immutable once created</li>
 *   <li><b>Composable</b> - Complex expressions built from simple ones</li>
 *   <li><b>Type-safe</b> - Runtime type checking with clear errors</li>
 *   <li><b>Extensible</b> - Easy to add new expression types</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.dsl.statements
 * @see io.github.jspinak.brobot.runner.dsl.BusinessTask
 */
package io.github.jspinak.brobot.runner.dsl.expressions;