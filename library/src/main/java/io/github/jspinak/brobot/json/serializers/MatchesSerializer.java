package io.github.jspinak.brobot.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom serializer for Matches to handle special cases
 */
@Component
public class MatchesSerializer extends JsonSerializer<Matches> {
    @Override
    public void serialize(Matches matches, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        // Handle basic fields
        gen.writeStringField("actionDescription", matches.getActionDescription());
        gen.writeBooleanField("success", matches.isSuccess());
        gen.writeObjectField("duration", matches.getDuration());
        gen.writeObjectField("startTime", matches.getStartTime());
        gen.writeObjectField("endTime", matches.getEndTime());
        gen.writeObjectField("selectedText", matches.getSelectedText());
        gen.writeObjectField("activeStates", matches.getActiveStates());
        gen.writeObjectField("definedRegions", matches.getDefinedRegions());

        // Handle text
        if (matches.getText() != null) {
            gen.writeObjectField("text", matches.getText());
        }

        // Handle match list - copy to avoid circular references
        List<Match> sanitizedMatches = new ArrayList<>();
        for (Match match : matches.getMatchList()) {
            // Create a simplified version without problematic fields
            Match.Builder builder = new Match.Builder()
                    .setRegion(match.getRegion())
                    .setSimScore(match.getScore())
                    .setName(match.getName());

            // Properly handle StateObjectData
            if (match.getStateObjectData() != null) {
                // Create a new StateObjectData with the necessary info
                StateObjectData stateObjectData = new StateObjectData();
                stateObjectData.setOwnerStateName(match.getOwnerStateName());
                stateObjectData.setStateObjectName(match.getStateObjectData().getStateObjectName());

                // Set the StateObjectData on the builder
                builder.setStateObjectData(stateObjectData);
            }

            sanitizedMatches.add(builder.build());
        }
        gen.writeObjectField("matchList", sanitizedMatches);

        // Skip problematic fields: mask, sceneAnalysisCollection, actionLifecycle, actionOptions
        gen.writeEndObject();
    }
}


