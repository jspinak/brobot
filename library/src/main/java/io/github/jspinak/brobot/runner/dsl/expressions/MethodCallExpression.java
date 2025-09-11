// File: io/github/jspinak/brobot/dsl/model/MethodCallExpression.java
package io.github.jspinak.brobot.runner.dsl.expressions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a method invocation expression in the Brobot DSL.
 *
 * <p>Method call expressions allow invoking methods on objects or calling static/global functions.
 * They consist of an optional object reference, a method name, and a list of argument expressions.
 * The expression evaluates to the return value of the method.
 *
 * <p>Examples in JSON:
 *
 * <pre>
 * // Instance method call
 * {
 *   "expressionType": "methodCall",
 *   "object": "myObject",
 *   "method": "doSomething",
 *   "arguments": [{"expressionType": "literal", "valueType": "string", "value": "arg1"}]
 * }
 *
 * // Static/global function call (object is null)
 * {
 *   "expressionType": "methodCall",
 *   "object": null,
 *   "method": "globalFunction",
 *   "arguments": []
 * }
 * </pre>
 *
 * @see Expression
 * @see io.github.jspinak.brobot.runner.dsl.statements.MethodCallStatement
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodCallExpression extends Expression {
    /**
     * The name of the object on which to invoke the method. Can be null for static methods or
     * global function calls. When non-null, this should reference a variable in the current scope.
     */
    private String object;

    /**
     * The name of the method or function to invoke. Must match an available method on the target
     * object or a global function.
     */
    private String method;

    /**
     * List of argument expressions to pass to the method. Each expression is evaluated before the
     * method call, and the resulting values are passed as arguments in the order specified.
     */
    private List<Expression> arguments;
}
