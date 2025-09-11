// File: io/github/jspinak/brobot/dsl/model/MethodCallStatement.java
package io.github.jspinak.brobot.runner.dsl.statements;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.Expression;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a method call statement in the Brobot DSL.
 *
 * <p>This statement invokes a method for its side effects rather than its return value. While
 * {@link io.github.jspinak.brobot.runner.dsl.expressions.MethodCallExpression} is used when the
 * return value is needed, this statement is used when only the method's side effects are desired
 * (e.g., logging, UI interactions, state modifications).
 *
 * <p>The method can be called on an object instance or as a static/global function.
 *
 * <p>Example in JSON:
 *
 * <pre>
 * {
 *   "statementType": "methodCall",
 *   "object": "browser",
 *   "method": "click",
 *   "arguments": [{"expressionType": "literal", "valueType": "string", "value": "#submit-button"}]
 * }
 * </pre>
 *
 * @see Statement
 * @see io.github.jspinak.brobot.runner.dsl.expressions.MethodCallExpression
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MethodCallStatement extends Statement {
    /**
     * The name of the object on which to invoke the method. Can be null for static methods or
     * global function calls. When non-null, this should reference a variable in the current scope.
     */
    private String object;

    /**
     * The name of the method or function to invoke. Must match an available method on the target
     * object or a global function. The method is called for its side effects; any return value is
     * discarded.
     */
    private String method;

    /**
     * List of argument expressions to pass to the method. Each expression is evaluated before the
     * method call, and the resulting values are passed as arguments in the order specified.
     */
    private List<Expression> arguments;
}
