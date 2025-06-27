// File: io/github/jspinak/brobot/dsl/Expression.java
package io.github.jspinak.brobot.runner.dsl.expressions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract base class for all expressions in the Brobot DSL.
 * <p>
 * An expression represents a value-producing computation in the DSL. Expressions can be:
 * <ul>
 * <li>Literals (constant values like "hello" or 42)</li>
 * <li>Variables (references to previously declared values)</li>
 * <li>Method calls (invocations that return values)</li>
 * <li>Binary operations (arithmetic or logical operations between two expressions)</li>
 * <li>Builder expressions (fluent API pattern for constructing complex objects)</li>
 * </ul>
 * <p>
 * This class uses Jackson polymorphic deserialization to support parsing different
 * expression types from JSON based on the "expressionType" discriminator field.
 *
 * @see LiteralExpression
 * @see VariableExpression
 * @see MethodCallExpression
 * @see BinaryOperationExpression
 * @see BuilderExpression
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "expressionType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LiteralExpression.class, name = "literal"),
        @JsonSubTypes.Type(value = VariableExpression.class, name = "variable"),
        @JsonSubTypes.Type(value = MethodCallExpression.class, name = "methodCall"),
        @JsonSubTypes.Type(value = BinaryOperationExpression.class, name = "binaryOperation"),
        @JsonSubTypes.Type(value = BuilderExpression.class, name = "builder")
})
public abstract class Expression {
    /**
     * The discriminator field used by Jackson to determine the concrete type
     * of this expression during JSON deserialization.
     * Valid values: "literal", "variable", "methodCall", "binaryOperation", "builder"
     */
    @JsonProperty("expressionType")
    @Getter
    @Setter
    private String expressionType;
}