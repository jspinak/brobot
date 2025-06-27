// File: io/github/jspinak/brobot/dsl/model/VariableExpression.java
package io.github.jspinak.brobot.runner.dsl.expressions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a variable reference expression in the Brobot DSL.
 * <p>
 * Variable expressions are used to reference values that have been previously
 * declared and assigned in the current scope. During evaluation, the variable
 * name is looked up in the current execution context to retrieve its value.
 * <p>
 * Variables must be declared before they are referenced, typically through
 * {@link io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement}
 * or function parameters.
 * <p>
 * Example in JSON:
 * <pre>
 * { "expressionType": "variable", "name": "myVariable" }
 * </pre>
 *
 * @see Expression
 * @see io.github.jspinak.brobot.runner.dsl.statements.VariableDeclarationStatement
 * @see io.github.jspinak.brobot.runner.dsl.statements.AssignmentStatement
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableExpression extends Expression {
    /**
     * The name of the variable being referenced.
     * This name must match a variable that has been previously declared
     * in the current scope or passed as a parameter to the current function.
     */
    private String name;
}