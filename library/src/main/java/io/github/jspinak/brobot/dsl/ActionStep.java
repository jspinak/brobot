// File: io/github/jspinak/brobot/dsl/ActionStep.java
package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Provides getters, setters, toString, equals, hashCode
@NoArgsConstructor // Required for Jackson deserialization
@AllArgsConstructor // Convenience constructor for all fields
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionStep {
    private ActionOptions actionOptions;
    private ObjectCollection objectCollection;

    @Override
    public String toString() {
        // Custom toString logic is preserved
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(actionOptions.getAction()).append(", ");
        sb.append("StateImages: [");
        if (objectCollection != null && objectCollection.getStateImages() != null && !objectCollection.getStateImages().isEmpty()) {
            objectCollection.getStateImages().forEach(si ->
                    sb.append("(id=").append(si.getIdAsString())
                            .append(", name=").append(si.getName())
                            .append(", patterns=").append(si.getPatterns().size())
                            .append("), ")
            );
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        return sb.toString();
    }
}