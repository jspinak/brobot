package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionStep {
    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;

    public ActionStep(ActionOptions actionOptions, ObjectCollection objectCollection) {
        this.actionOptions = actionOptions;
        this.objectCollection = objectCollection;
    }

    public ActionStep() {
        // Empty constructor for Jackson
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(actionOptions.getAction()).append(", ");
        sb.append("StateImages: [");
        objectCollection.getStateImages().forEach(si ->
                sb.append("(id=").append(si.getIdAsString())
                        .append(", name=").append(si.getName())
                        .append(", patterns=").append(si.getPatterns().size())
                        .append("), ")
        );
        if (!objectCollection.getStateImages().isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        return sb.toString();
    }
}
