// File: io/github/jspinak/brobot/dsl/model/Parameter.java
package io.github.jspinak.brobot.runner.dsl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Represents a parameter definition for automation functions in the Brobot DSL.
 * <p>
 * Parameters allow functions to accept input values, making them reusable
 * and configurable. Each parameter has a name and type, which are used for
 * validation and type checking when the function is called.
 * <p>
 * When a function is invoked, arguments are matched to parameters by position,
 * and type compatibility is verified to ensure correct execution.
 * <p>
 * Example in JSON (as part of a function definition):
 * <pre>
 * "parameters": [
 *   {"name": "elementId", "type": "string"},
 *   {"name": "timeout", "type": "integer"},
 *   {"name": "retry", "type": "boolean"}
 * ]
 * </pre>
 *
 * @see io.github.jspinak.brobot.runner.dsl.BusinessTask#getParameters()
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Parameter {
    /**
     * The name of the parameter.
     * Used to reference the parameter value within the function body
     * and for documentation purposes.
     */
    private String name;
    
    /**
     * The data type of the parameter.
     * Common types include: "boolean", "string", "integer", "double", "object", "array"
     * Used for type checking when arguments are passed to the function.
     */
    private String type;
}