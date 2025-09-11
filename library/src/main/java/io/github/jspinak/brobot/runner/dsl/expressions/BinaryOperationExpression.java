// File: io/github/jspinak/brobot/dsl/BinaryOperationExpression.java
package io.github.jspinak.brobot.runner.dsl.expressions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a binary operation expression in the Brobot DSL.
 *
 * <p>Binary operations combine two expressions using an operator to produce a result. Supported
 * operations include arithmetic (e.g., +, -, *, /), comparison (e.g., ==, !=, <, >), and logical
 * operations (e.g., &&, ||).
 *
 * <p>The operation is evaluated by first evaluating the left and right expressions, then applying
 * the operator to their values.
 *
 * <p>Example in JSON:
 *
 * <pre>
 * {
 *   "expressionType": "binaryOperation",
 *   "operator": "+",
 *   "left": {"expressionType": "literal", "valueType": "integer", "value": 5},
 *   "right": {"expressionType": "literal", "valueType": "integer", "value": 3}
 * }
 * </pre>
 *
 * @see Expression
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinaryOperationExpression extends Expression {
    /**
     * The binary operator to apply. Common operators include:
     *
     * <ul>
     *   <li>Arithmetic: "+", "-", "*", "/", "%"
     *   <li>Comparison: "==", "!=", "<", ">", "<=", ">="
     *   <li>Logical: "&&", "||"
     * </ul>
     */
    private String operator;

    /**
     * The left operand expression. This expression is evaluated first, and its value becomes the
     * left operand for the binary operation.
     */
    private Expression left;

    /**
     * The right operand expression. This expression is evaluated second, and its value becomes the
     * right operand for the binary operation.
     */
    private Expression right;
}
