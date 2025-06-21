// File: io/github/jspinak/brobot/dsl/ActionDefinition.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a sequence of actions as data in the Brobot Domain-Specific Language.
 * 
 * <p>ActionDefinition represents a declarative specification of automation steps that can be 
 * stored, transmitted, and executed dynamically. It encapsulates a series of ActionSteps, 
 * each containing the configuration and targets for a specific action. This data-driven 
 * approach enables flexible automation definition without hardcoded logic.</p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Sequential Steps</b>: Maintains an ordered list of actions to execute</li>
 *   <li><b>Data-driven</b>: Can be serialized to/from JSON for configuration-based automation</li>
 *   <li><b>Composable</b>: Steps can be added dynamically to build complex workflows</li>
 *   <li><b>Self-contained</b>: Each step includes both action configuration and target objects</li>
 * </ul>
 * </p>
 * 
 * <p>Common use patterns:
 * <ul>
 *   <li>Defining state transitions as a series of actions</li>
 *   <li>Creating reusable automation sequences</li>
 *   <li>Building dynamic workflows from configuration files</li>
 *   <li>Enabling non-programmers to define automation through data</li>
 * </ul>
 * </p>
 * 
 * <p>Example workflow:
 * <pre>
 * Step 1: Find and click login button
 * Step 2: Type username
 * Step 3: Type password  
 * Step 4: Click submit
 * Step 5: Verify main page appears
 * </pre>
 * </p>
 * 
 * <p>In the model-based approach, ActionDefinition bridges the gap between abstract state 
 * transitions and concrete action sequences. It enables state transitions to be defined 
 * declaratively, making the automation more maintainable and allowing runtime modification 
 * of behavior without code changes.</p>
 * 
 * <p>The custom toString() implementation provides human-readable output for debugging 
 * and logging, showing each step in the sequence with its configuration.</p>
 * 
 * @since 1.0
 * @see ActionStep
 * @see ActionOptions
 * @see ObjectCollection
 * @see ActionDefinitionStateTransition
 */
@Data // Provides getters, setters, toString, equals, hashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionDefinition {
    private List<ActionStep> steps = new ArrayList<>();

    public ActionDefinition() {
        // The default constructor is kept for custom initialization.
    }

    public ActionDefinition(ActionOptions options, ObjectCollection objects) {
        steps.add(new ActionStep(options, objects));
    }

    public void addStep(ActionOptions options, ObjectCollection objects) {
        steps.add(new ActionStep(options, objects));
    }

    public void addStep(ActionStep step) {
        steps.add(step);
    }

    @Override
    public String toString() {
        // Custom toString logic is preserved
        StringBuilder sb = new StringBuilder();
        sb.append("ActionDefinition: [\n");
        for (int i = 0; i < steps.size(); i++) {
            sb.append("  Step ").append(i + 1).append(": ").append(steps.get(i)).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}