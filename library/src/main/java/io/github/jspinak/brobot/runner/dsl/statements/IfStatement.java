// File: io/github/jspinak/brobot/dsl/model/IfStatement.java
package io.github.jspinak.brobot.runner.dsl.statements;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.Expression;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a conditional if-then-else statement in the Brobot DSL.
 *
 * <p>This statement provides branching control flow based on a boolean condition. If the condition
 * evaluates to true, the statements in the "then" branch are executed; otherwise, the statements in
 * the "else" branch are executed (if present).
 *
 * <p>The condition expression must evaluate to a boolean value. Both branches create new scopes for
 * variable declarations.
 *
 * <p>Example in JSON:
 *
 * <pre>
 * {
 *   "statementType": "if",
 *   "condition": {
 *     "expressionType": "binaryOperation",
 *     "operator": ">",
 *     "left": {"expressionType": "variable", "name": "count"},
 *     "right": {"expressionType": "literal", "valueType": "integer", "value": 0}
 *   },
 *   "thenStatements": [
 *     {"statementType": "methodCall", "method": "log", "arguments": [{"expressionType": "literal", "valueType": "string", "value": "Count is positive"}]}
 *   ],
 *   "elseStatements": [
 *     {"statementType": "methodCall", "method": "log", "arguments": [{"expressionType": "literal", "valueType": "string", "value": "Count is zero or negative"}]}
 *   ]
 * }
 * </pre>
 *
 * @see Statement
 * @see io.github.jspinak.brobot.runner.dsl.expressions.BinaryOperationExpression
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IfStatement extends Statement {
    /**
     * The boolean condition expression that determines which branch to execute. This expression
     * must evaluate to a boolean value (true or false). Common condition expressions include
     * comparisons and logical operations.
     */
    private Expression condition;

    /**
     * The list of statements to execute when the condition evaluates to true. These statements are
     * executed in order within a new scope. Cannot be null but may be empty.
     */
    private List<Statement> thenStatements;

    /**
     * The optional list of statements to execute when the condition evaluates to false. If null or
     * empty, no action is taken when the condition is false. When present, these statements are
     * executed in order within a new scope.
     */
    private List<Statement> elseStatements;
}
