// File: io/github/jspinak/brobot/dsl/model/VariableDeclarationStatement.java
package io.github.jspinak.brobot.runner.dsl.statements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a variable declaration statement in the Brobot DSL.
 * <p>
 * This statement declares a new variable in the current scope, optionally
 * initializing it with a value. The variable can then be referenced in
 * subsequent statements and expressions within its scope.
 * <p>
 * Variables must be declared before use and cannot be redeclared within
 * the same scope. The type information helps with validation and type checking.
 * <p>
 * Example in JSON:
 * <pre>
 * {
 *   "statementType": "variableDeclaration",
 *   "name": "count",
 *   "type": "integer",
 *   "value": {"expressionType": "literal", "valueType": "integer", "value": 0}
 * }
 * </pre>
 *
 * @see Statement
 * @see io.github.jspinak.brobot.runner.dsl.expressions.VariableExpression
 * @see AssignmentStatement
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VariableDeclarationStatement extends Statement {
    /**
     * The name of the variable being declared.
     * This name must be unique within the current scope and will be used
     * to reference the variable in subsequent statements.
     */
    private String name;
    
    /**
     * The data type of the variable.
     * Common types include: "string", "integer", "double", "boolean", "object"
     * This helps with type checking and validation during DSL execution.
     */
    private String type;
    
    /**
     * Optional initial value expression for the variable.
     * If provided, the expression is evaluated and its result is assigned
     * to the variable. If null, the variable is initialized with a default
     * value based on its type.
     */
    private Expression value;
}