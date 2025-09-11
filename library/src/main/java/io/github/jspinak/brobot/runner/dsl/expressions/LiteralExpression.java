// File: io/github/jspinak/brobot/dsl/model/LiteralExpression.java
package io.github.jspinak.brobot.runner.dsl.expressions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a literal (constant) value expression in the Brobot DSL.
 *
 * <p>Literal expressions are the simplest form of expressions, representing constant values such as
 * strings, numbers, booleans, or null. They are the building blocks for more complex expressions
 * and are directly evaluated to their contained value.
 *
 * <p>Examples in JSON:
 *
 * <pre>
 * { "expressionType": "literal", "valueType": "string", "value": "Hello World" }
 * { "expressionType": "literal", "valueType": "integer", "value": 42 }
 * { "expressionType": "literal", "valueType": "boolean", "value": true }
 * </pre>
 *
 * @see Expression
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiteralExpression extends Expression {
    /**
     * The type of the literal value. Supported types: "boolean", "string", "integer", "double",
     * "null" This field helps with type checking and proper deserialization.
     */
    private String valueType;

    /**
     * The actual literal value. The runtime type corresponds to the valueType field:
     *
     * <ul>
     *   <li>"boolean" → Boolean
     *   <li>"string" → String
     *   <li>"integer" → Integer
     *   <li>"double" → Double
     *   <li>"null" → null
     * </ul>
     */
    private Object value;
}
