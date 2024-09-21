package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DSLParser {
    private final ActionEncoder encoder;
    private final ActionDecoder decoder;

    public DSLParser(ActionEncoder encoder, ActionDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public ActionDefinition parseAction(String dslString) {
        try {
            // Split the DSL string into lines
            String[] lines = dslString.split("\n");

            String actionType = null;
            List<ActionOptions> optionsList = new ArrayList<>();
            List<ObjectCollection> objectCollectionsList = new ArrayList<>();

            // Parse the DSL string
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("ACTION:")) {
                    actionType = line.substring(7).trim();
                } else if (line.startsWith("STEP")) {
                    // Parse options
                    StringBuilder optionsString = new StringBuilder();
                    while (++i < lines.length && !lines[i].trim().startsWith("ON:")) {
                        optionsString.append(lines[i]).append("\n");
                    }
                    ActionOptions options = decoder.decodeOptions(optionsString.toString());
                    optionsList.add(options);

                    // Parse object collection
                    if (i < lines.length && lines[i].trim().startsWith("ON:")) {
                        String objectCollectionString = lines[i].substring(3).trim();
                        ObjectCollection objectCollection = decoder.decodeObjectCollection(objectCollectionString);
                        objectCollectionsList.add(objectCollection);
                    }
                }
            }

            // Create and return the ActionDefinition
            ActionDefinition actionDefinition = new ActionDefinition();
            for (int i = 0; i < optionsList.size(); i++) {
                actionDefinition.addStep(optionsList.get(i), objectCollectionsList.get(i));
            }
            return actionDefinition;

        } catch (IllegalArgumentException e) {
            throw new DSLException(DSLException.ErrorCode.INVALID_SYNTAX, "Failed to parse DSL string", e);
        }
    }

    public String generateDSL(ActionDefinition actionDefinition) {
        StringBuilder dslBuilder = new StringBuilder();

        // Encode steps
        for (int i = 0; i < actionDefinition.getSteps().size(); i++) {
            ActionStep step = actionDefinition.getSteps().get(i);
            dslBuilder.append("STEP ").append(i + 1).append(":\n");

            // Encode options
            dslBuilder.append(encoder.encodeOptions(step.getOptions())).append("\n");

            // Encode object collection
            dslBuilder.append("ON: ").append(encoder.encodeObjectCollection(step.getObjects())).append("\n");
        }

        return dslBuilder.toString();
    }
}