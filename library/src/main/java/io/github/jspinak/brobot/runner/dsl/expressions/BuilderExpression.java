// File: io.github.jspinak/brobot/dsl/BuilderExpression.java
package io.github.jspinak.brobot.runner.dsl.expressions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.model.BuilderMethod;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a builder pattern expression in the Brobot DSL.
 *
 * <p>Builder expressions enable the fluent construction of complex objects using method chaining.
 * They are particularly useful for creating configuration objects or other data structures with
 * many optional parameters.
 *
 * <p>The expression starts with a builder type and applies a series of builder methods in sequence.
 * Each method typically returns the builder instance, allowing for method chaining. The final
 * result is the constructed object.
 *
 * <p>Example in JSON:
 *
 * <pre>
 * {
 *   "expressionType": "builder",
 *   "builderType": "ActionOptionsBuilder",
 *   "methods": [
 *     {"name": "setTimeout", "arguments": [{"expressionType": "literal", "valueType": "integer", "value": 5000}]},
 *     {"name": "setRetries", "arguments": [{"expressionType": "literal", "valueType": "integer", "value": 3}]},
 *     {"name": "build", "arguments": []}
 *   ]
 * }
 * </pre>
 *
 * @see Expression
 * @see BuilderMethod
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderExpression extends Expression {
    /**
     * The type of builder to instantiate. This should correspond to a known builder class in the
     * automation framework (e.g., "ActionOptionsBuilder", "RegionBuilder").
     */
    private String builderType;

    /**
     * Ordered list of builder methods to invoke. These methods are called sequentially on the
     * builder instance, typically ending with a build() method that returns the final object.
     */
    private List<BuilderMethod> methods;
}
