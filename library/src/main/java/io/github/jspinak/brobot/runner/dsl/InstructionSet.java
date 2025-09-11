// File: io/github/jspinak/brobot/dsl/AutomationDsl.java
package io.github.jspinak.brobot.runner.dsl;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

/**
 * Root data structure for Brobot's Domain-Specific Language (DSL) automation definitions.
 *
 * <p>This class serves as the top-level container for automation functions defined in JSON format.
 * It allows users to define reusable automation functions that can be executed by Brobot. Each
 * function represents a discrete automation task with its own parameters and logic.
 *
 * <p>The DSL supports parsing JSON files that contain automation function definitions, enabling
 * declarative automation script creation without direct Java programming.
 *
 * @see BusinessTask
 * @see io.github.jspinak.brobot.runner.dsl.statements.Statement
 * @see io.github.jspinak.brobot.runner.dsl.expressions.Expression
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstructionSet {
    /**
     * List of automation functions defined in this DSL instance. Each function can be independently
     * executed and may call other functions.
     */
    private List<BusinessTask> automationFunctions;
}
