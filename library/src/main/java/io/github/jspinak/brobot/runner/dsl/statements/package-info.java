/**
 * Statement execution components for the Brobot DSL.
 * 
 * <p>This package provides the statement types that form the control flow and
 * execution logic of Brobot automation instructions. Statements represent
 * actions to be performed, including variable manipulation, control flow,
 * and method invocations.</p>
 * 
 * <h2>Statement Types</h2>
 * 
 * <ul>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.Statement} - 
 *       Base interface for all statement types</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement} - 
 *       Declares and optionally initializes variables</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.AssignmentStatement} - 
 *       Assigns values to existing variables</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.MethodCallStatement} - 
 *       Invokes methods on objects</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.IfStatement} - 
 *       Conditional execution with optional else branch</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.ForEachStatement} - 
 *       Iteration over collections</li>
 *   <li>{@link io.github.jspinak.brobot.runner.dsl.statements.ReturnStatement} - 
 *       Returns a value from a business task</li>
 * </ul>
 * 
 * <h2>Statement Examples</h2>
 * 
 * <h3>Variable Declaration</h3>
 * <pre>{@code
 * // Declare with initialization
 * {
 *   "type": "variableDeclaration",
 *   "variableName": "count",
 *   "variableType": "number",
 *   "initialValue": {"type": "literal", "value": 0}
 * }
 * 
 * // Declare without initialization
 * {
 *   "type": "variableDeclaration",
 *   "variableName": "result",
 *   "variableType": "object"
 * }
 * }</pre>
 * 
 * <h3>Assignment</h3>
 * <pre>{@code
 * // Assign new value to variable
 * {
 *   "type": "assignment",
 *   "variableName": "count",
 *   "value": {
 *     "type": "binaryOp",
 *     "operator": "+",
 *     "left": {"type": "variable", "name": "count"},
 *     "right": {"type": "literal", "value": 1}
 *   }
 * }
 * }</pre>
 * 
 * <h3>Method Call</h3>
 * <pre>{@code
 * // Call a method on an object
 * {
 *   "type": "methodCall",
 *   "target": {"type": "variable", "name": "loginPage"},
 *   "method": "click",
 *   "arguments": [
 *     {"type": "literal", "value": "submitButton"}
 *   ]
 * }
 * }</pre>
 * 
 * <h3>Conditional Execution</h3>
 * <pre>{@code
 * // If-else statement
 * {
 *   "type": "if",
 *   "condition": {
 *     "type": "methodCall",
 *     "target": {"type": "variable", "name": "page"},
 *     "method": "exists",
 *     "arguments": [{"type": "literal", "value": "errorMessage"}]
 *   },
 *   "thenStatements": [
 *     {
 *       "type": "return",
 *       "value": {"type": "literal", "value": false}
 *     }
 *   ],
 *   "elseStatements": [
 *     {
 *       "type": "methodCall",
 *       "method": "continue"
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <h3>Iteration</h3>
 * <pre>{@code
 * // For-each loop
 * {
 *   "type": "forEach",
 *   "variableName": "item",
 *   "collection": {"type": "variable", "name": "items"},
 *   "statements": [
 *     {
 *       "type": "methodCall",
 *       "method": "process",
 *       "arguments": [{"type": "variable", "name": "item"}]
 *     }
 *   ]
 * }
 * }</pre>
 * 
 * <h3>Return</h3>
 * <pre>{@code
 * // Return a value
 * {
 *   "type": "return",
 *   "value": {"type": "variable", "name": "result"}
 * }
 * 
 * // Return without value
 * {
 *   "type": "return"
 * }
 * }</pre>
 * 
 * <h2>Execution Semantics</h2>
 * 
 * <ul>
 *   <li><b>Sequential</b> - Statements execute in order</li>
 *   <li><b>Scoped</b> - Variables have block scope</li>
 *   <li><b>Type-checked</b> - Runtime type validation</li>
 *   <li><b>Exception handling</b> - Errors propagate with context</li>
 * </ul>
 * 
 * <h2>Integration</h2>
 * 
 * <p>Statements integrate with:</p>
 * <ul>
 *   <li>Expression evaluation for computing values</li>
 *   <li>Execution context for variable storage</li>
 *   <li>Brobot action framework for automation operations</li>
 *   <li>Error reporting for debugging</li>
 * </ul>
 * 
 * @since 1.0
 * @see io.github.jspinak.brobot.runner.dsl.expressions
 * @see io.github.jspinak.brobot.runner.dsl.BusinessTask
 */
package io.github.jspinak.brobot.runner.dsl.statements;