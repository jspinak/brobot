// File: io/github/jspinak/brobot/dsl/model/ActionDefinition.java
package io.github.jspinak.brobot.runner.dsl.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;

import lombok.Data;

/**
 * Defines a sequence of actions as data in the Brobot Domain-Specific Language.
 *
 * <p>TaskSequence represents a declarative specification of automation steps that can be stored,
 * transmitted, and executed dynamically. It encapsulates a series of ActionSteps, each containing
 * the configuration and targets for a specific action. This data-driven approach enables flexible
 * automation definition without hardcoded logic.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li><b>Sequential Steps</b>: Maintains an ordered list of actions to execute
 *   <li><b>Data-driven</b>: Can be serialized to/from JSON for configuration-based automation
 *   <li><b>Composable</b>: Steps can be added dynamically to build complex workflows
 *   <li><b>Self-contained</b>: Each step includes both action configuration and target objects
 * </ul>
 *
 * <p>Common use patterns:
 *
 * <ul>
 *   <li>Defining state transitions as a series of actions
 *   <li>Creating reusable automation sequences
 *   <li>Building dynamic workflows from configuration files
 *   <li>Enabling non-programmers to define automation through data
 * </ul>
 *
 * <p>Example workflow:
 *
 * <pre>
 * Step 1: Find and click login button
 * Step 2: Type username
 * Step 3: Type password
 * Step 4: Click submit
 * Step 5: Verify main page appears
 * </pre>
 *
 * <p>In the model-based approach, TaskSequence bridges the gap between abstract state transitions
 * and concrete action sequences. It enables state transitions to be defined declaratively, making
 * the automation more maintainable and allowing runtime modification of behavior without code
 * changes.
 *
 * <p>The custom toString() implementation provides human-readable output for debugging and logging,
 * showing each step in the sequence with its configuration.
 *
 * @since 1.0
 * @see ActionStep
 * @see ActionConfig
 * @see ObjectCollection
 * @see TaskSequenceStateTransition
 */
@Data // Provides getters, setters, toString, equals, hashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskSequence {
    private List<ActionStep> steps = new ArrayList<>();

    /**
     * Creates an empty TaskSequence with no steps. Steps can be added later using the addStep
     * methods.
     */
    public TaskSequence() {
        // The default constructor is kept for custom initialization.
    }

    /**
     * Creates an TaskSequence with a single initial step.
     *
     * @param options The action configuration for the initial step
     * @param objects The target objects for the initial step
     */
    public TaskSequence(ActionConfig config, ObjectCollection objects) {
        ActionStep step = new ActionStep();
        step.setActionConfig(config);
        step.setObjectCollection(objects);
        steps.add(step);
    }

    /**
     * Adds a new action step to this definition. The step is appended to the end of the current
     * sequence.
     *
     * @param options The action configuration for the new step
     * @param objects The target objects for the new step
     */
    public void addStep(ActionConfig config, ObjectCollection objects) {
        ActionStep step = new ActionStep();
        step.setActionConfig(config);
        step.setObjectCollection(objects);
        steps.add(step);
    }

    /**
     * Adds an existing ActionStep to this definition. The step is appended to the end of the
     * current sequence.
     *
     * @param step The ActionStep to add
     */
    public void addStep(ActionStep step) {
        steps.add(step);
    }

    /**
     * Returns a human-readable string representation of this TaskSequence. Shows each step in the
     * sequence with its configuration for debugging and logging purposes.
     *
     * @return A formatted string showing all steps in the action sequence
     */
    @Override
    public String toString() {
        // Custom toString logic is preserved
        StringBuilder sb = new StringBuilder();
        sb.append("TaskSequence: [\n");
        for (int i = 0; i < steps.size(); i++) {
            sb.append("  Step ").append(i + 1).append(": ").append(steps.get(i)).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
