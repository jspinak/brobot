// File: io/github/jspinak/brobot/dsl/AutomationFunction.java
package io.github.jspinak.brobot.runner.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.runner.dsl.model.Parameter;
import io.github.jspinak.brobot.runner.dsl.statements.Statement;
import lombok.Data;
import java.util.List;

/**
 * Represents a single automation function in the Brobot DSL.
 * <p>
 * An automation function is a reusable unit of automation logic that can:
 * <ul>
 * <li>Accept parameters for customization</li>
 * <li>Execute a series of statements to perform automation tasks</li>
 * <li>Return a value to the caller</li>
 * <li>Call other automation functions</li>
 * </ul>
 * <p>
 * Functions are typically defined in JSON and parsed at runtime, allowing for
 * dynamic automation script creation without recompilation.
 *
 * @see io.github.jspinak.brobot.runner.dsl.statements.Statement
 * @see io.github.jspinak.brobot.runner.dsl.model.Parameter
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessTask {
    /**
     * Unique identifier for this function within the automation context.
     * Used for referencing and debugging purposes.
     */
    private Integer id;
    
    /**
     * The name of this function, used when calling it from other functions
     * or from the main automation script.
     */
    private String name;
    
    /**
     * Human-readable description of what this function does.
     * This helps maintainers understand the function's purpose.
     */
    private String description;
    
    /**
     * The data type that this function returns (e.g., "string", "boolean", "void").
     * Used for type checking and validation during DSL parsing.
     */
    private String returnType;
    
    /**
     * List of parameters that this function accepts.
     * Parameters allow the function to be customized for different use cases.
     */
    private List<Parameter> parameters;
    
    /**
     * The ordered list of statements that make up this function's body.
     * These statements are executed sequentially when the function is called.
     */
    private List<Statement> statements;
}