package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import lombok.Getter;

@Getter
public class ActionStep {
    private ActionOptions options;
    private ObjectCollection objects;

    public ActionStep(ActionOptions options, ObjectCollection objects) {
        this.options = options;
        this.objects = objects;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: ").append(options.getAction()).append(", ");
        sb.append("StateImages: [");
        objects.getStateImages().forEach(si ->
                sb.append("(id=").append(si.getId())
                        .append(", name=").append(si.getName())
                        .append(", patterns=").append(si.getPatterns().size())
                        .append("), ")
        );
        if (!objects.getStateImages().isEmpty()) {
            sb.setLength(sb.length() - 2); // Remove last ", "
        }
        sb.append("]");
        return sb.toString();
    }
}
