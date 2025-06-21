// File: io.github/jspinak/brobot/dsl/BuilderMethod.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * Represents a method call in the Brobot Domain-Specific Language (DSL).
 * 
 * <p>BuilderMethod is a fundamental component of Brobot's DSL that enables serializable 
 * representation of method invocations. It forms part of the framework's capability to 
 * express automation scripts in a declarative, data-driven format that can be stored, 
 * transmitted, and executed dynamically.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Method Abstraction</b>: Encapsulates method name and arguments as data</li>
 *   <li><b>Type Flexibility</b>: Arguments are represented as Expression objects for 
 *       maximum flexibility</li>
 *   <li><b>Serialization Support</b>: Fully serializable to/from JSON for persistence 
 *       and configuration</li>
 *   <li><b>DSL Integration</b>: Core building block for constructing complex automation 
 *       workflows</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li>Defining automation steps in configuration files</li>
 *   <li>Building dynamic automation workflows at runtime</li>
 *   <li>Creating reusable automation patterns and templates</li>
 *   <li>Enabling non-programmers to define automation sequences</li>
 * </ul>
 * </p>
 * 
 * <p>Example representations:
 * <ul>
 *   <li>Click action: method="click", arguments=[image expression]</li>
 *   <li>Type text: method="type", arguments=[text expression]</li>
 *   <li>Wait: method="wait", arguments=[duration expression]</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, BuilderMethod enables the separation of automation 
 * logic from implementation details. This abstraction allows automation scripts to be 
 * defined declaratively, making them more maintainable, testable, and accessible to 
 * users who may not be familiar with programming languages.</p>
 * 
 * @since 1.0
 * @see Expression
 * @see BuilderExpression
 * @see ArgumentType
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuilderMethod {
    private String method;
    private List<Expression> arguments;
}