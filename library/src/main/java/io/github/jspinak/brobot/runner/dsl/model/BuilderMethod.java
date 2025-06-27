// File: io.github/jspinak/brobot/dsl/model/BuilderMethod.java
package io.github.jspinak.brobot.runner.dsl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.expressions.BuilderExpression;
import io.github.jspinak.brobot.runner.dsl.expressions.Expression;
import lombok.Data;
import java.util.List;

/**
 * Represents a method call within a builder chain in the Brobot DSL.
 * <p>
 * BuilderMethod is used specifically within {@link BuilderExpression} to represent
 * individual method calls in a builder pattern chain. Each method typically configures
 * one aspect of the object being built and returns the builder for further chaining.
 * <p>
 * This class enables the declarative representation of fluent API calls, allowing
 * complex object construction to be defined in JSON configuration files.
 * <p>
 * Example usage in a BuilderExpression:
 * <pre>
 * // Part of a BuilderExpression that creates an ActionOptions object
 * {
 *   "method": "setTimeout",
 *   "arguments": [{"expressionType": "literal", "valueType": "integer", "value": 5000}]
 * }
 * </pre>
 *
 * @see BuilderExpression
 * @see io.github.jspinak.brobot.runner.dsl.expressions.Expression
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderMethod {
    /**
     * The name of the builder method to invoke.
     * This should match a method available on the builder class specified
     * in the containing BuilderExpression.
     */
    private String method;
    
    /**
     * List of argument expressions to pass to the builder method.
     * Each expression is evaluated to produce the actual argument values
     * at runtime. The number and types of arguments must match the method signature.
     */
    private List<Expression> arguments;
}