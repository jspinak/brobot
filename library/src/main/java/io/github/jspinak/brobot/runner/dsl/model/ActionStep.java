// File: io/github/jspinak/brobot/dsl/model/ActionStep.java
package io.github.jspinak.brobot.runner.dsl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single step in an automation sequence within the Brobot DSL.
 *
 * <p>An ActionStep combines action configuration with target objects, forming the basic unit of
 * execution in an {@link TaskSequence}. Each step specifies what action to perform (via
 * ActionOptions) and what to perform it on (via ObjectCollection).
 *
 * <p>This class is designed for serialization, allowing automation sequences to be defined in
 * configuration files and loaded dynamically at runtime.
 *
 * <p>Example usage:
 *
 * <pre>
 * // Click on a login button
 * ActionStep loginStep = new ActionStep(
 *     new ClickOptions.Builder().build(),
 *     new ObjectCollection.Builder().withImages(loginButton).build()
 * );
 * </pre>
 *
 * @see TaskSequence
 * @see ActionConfig
 * @see ObjectCollection
 */
@Data // Provides getters, setters, toString, equals, hashCode
@NoArgsConstructor // Required for Jackson deserialization
@AllArgsConstructor // Convenience constructor for all fields
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionStep {
    /**
     * The configuration for this action step. Specifies the type of action, timing parameters,
     * success criteria, and other behavioral options.
     */
    private ActionConfig actionConfig;

    /**
     * The collection of target objects for this action. Contains the visual elements (images,
     * regions, etc.) that the action will operate on.
     */
    private ObjectCollection objectCollection;

    /**
     * Returns a human-readable string representation of this ActionStep. Shows the action type and
     * details about the target state images for debugging and logging purposes.
     *
     * @return A formatted string showing the action and its targets
     */
    @Override
    public String toString() {
        // Custom toString logic is preserved
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(actionConfig.getClass().getSimpleName()).append(", ");
        sb.append("StateImages: [");
        if (objectCollection != null
                && objectCollection.getStateImages() != null
                && !objectCollection.getStateImages().isEmpty()) {
            objectCollection
                    .getStateImages()
                    .forEach(
                            si ->
                                    sb.append("(id=")
                                            .append(si.getIdAsString())
                                            .append(", name=")
                                            .append(si.getName())
                                            .append(", patterns=")
                                            .append(si.getPatterns().size())
                                            .append("), "));
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        return sb.toString();
    }
}
