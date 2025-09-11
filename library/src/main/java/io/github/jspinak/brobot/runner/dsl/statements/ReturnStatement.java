// File: io/github/jspinak/brobot/dsl/model/ReturnStatement.java
package io.github.jspinak.brobot.runner.dsl.statements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.Expression;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a return statement in the Brobot DSL.
 *
 * <p>This statement terminates the execution of the current function and optionally returns a value
 * to the caller. When executed, control flow immediately exits the function, skipping any remaining
 * statements.
 *
 * <p>The return value expression must be compatible with the function's declared return type. For
 * void functions, the value should be null.
 *
 * <p>Example in JSON:
 *
 * <pre>
 * // Returning a value
 * {
 *   "statementType": "return",
 *   "value": {"expressionType": "variable", "name": "result"}
 * }
 *
 * // Returning from a void function
 * {
 *   "statementType": "return",
 *   "value": null
 * }
 * </pre>
 *
 * @see Statement
 * @see BusinessTask#getReturnType()
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnStatement extends Statement {
    /**
     * The expression whose value will be returned from the function. Can be null for void functions
     * or early returns without a value. When non-null, the expression is evaluated and its result
     * becomes the function's return value.
     */
    private Expression value;
}
