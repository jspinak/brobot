// File: io/github/jspinak/brobot/dsl/ActionDefinition.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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