package io.github.jspinak.brobot.runner.dsl.statements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents an assignment statement in the Brobot DSL.
 * <p>
 * This statement assigns a new value to an existing variable. The variable
 * must have been previously declared in the current scope or an outer scope.
 * The value expression is evaluated and its result replaces the variable's
 * current value.
 * <p>
 * Assignment statements modify the state of the execution context and are
 * fundamental for implementing dynamic behavior in automation scripts.
 * <p>
 * Example in JSON:
 * <pre>
 * {
 *   "statementType": "assignment",
 *   "variable": "count",
 *   "value": {
 *     "expressionType": "binaryOperation",
 *     "operator": "+",
 *     "left": {"expressionType": "variable", "name": "count"},
 *     "right": {"expressionType": "literal", "valueType": "integer", "value": 1}
 *   }
 * }
 * </pre>
 *
 * @see Statement
 * @see VariableDeclarationStatement
 * @see io.github.jspinak.brobot.runner.dsl.expressions.Expression
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class AssignmentStatement extends Statement {
    /**
     * The name of the variable to assign to.
     * This variable must exist in the current scope or an accessible outer scope.
     * Attempting to assign to an undeclared variable will result in an error.
     */
    private String variable;
    
    /**
     * The expression whose value will be assigned to the variable.
     * This expression is evaluated at runtime, and its result replaces
     * the variable's current value.
     */
    private Expression value;
}