package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

@Component
public class ActionDecoder {

    public ActionOptions decodeOptions(String optionsString) {
        if (optionsString == null || optionsString.isEmpty()) {
            throw new DSLException(DSLException.ErrorCode.INVALID_SYNTAX, "Options string is null or empty");
        }
        ActionOptions.Builder builder = new ActionOptions.Builder();
        String[] lines = optionsString.split("\n");
        for (String line : lines) {
            if (line.startsWith("action:")) {
                builder.setAction(ActionOptions.Action.valueOf(line.substring(7).trim()));
            } else if (line.startsWith("clickUntil:")) {
                builder.setClickUntil(ActionOptions.ClickUntil.valueOf(line.substring(11).trim()));
            }
            // ... decode other options ...
        }
        return builder.build();
    }

    public ObjectCollection decodeObjectCollection(String objectCollectionString) {
        ObjectCollection.Builder builder = new ObjectCollection.Builder();
        String[] parts = objectCollectionString.split(";");
        for (String part : parts) {
            if (part.startsWith("LOCATIONS:")) {
                // Decode and add locations
            } else if (part.startsWith("IMAGES:")) {
                // Decode and add images
            }
            // ... decode other types of objects ...
        }
        return builder.build();
    }

}
