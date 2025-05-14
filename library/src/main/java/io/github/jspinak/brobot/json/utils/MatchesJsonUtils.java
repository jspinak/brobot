package io.github.jspinak.brobot.json.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.bytedeco.opencv.opencv_core.Mat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for serializing Matches to and from JSON.
 * Handles special cases like Mat objects and other complex fields.
 */
@Component
public class MatchesJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(MatchesJsonUtils.class);
    private final JsonUtils jsonUtils;
    private final JsonParser jsonParser;

    public MatchesJsonUtils(JsonUtils jsonUtils, JsonParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.jsonParser = jsonParser;
    }

    /**
     * Custom serializer for Matches to handle special cases
     */
    public static class MatchesSerializer extends JsonSerializer<Matches> {
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

    /**
     * Converts Matches to a Map representation that's easier to work with
     * for custom serialization, excluding problematic fields
     */
    public Map<String, Object> matchesToMap(Matches matches) {
        Map<String, Object> map = new HashMap<>();

        // Add basic fields
        map.put("actionDescription", matches.getActionDescription());
        map.put("success", matches.isSuccess());
        map.put("duration", matches.getDuration());
        map.put("startTime", matches.getStartTime());
        map.put("endTime", matches.getEndTime());
        map.put("selectedText", matches.getSelectedText());
        map.put("activeStates", matches.getActiveStates());
        map.put("definedRegions", matches.getDefinedRegions());
        map.put("text", matches.getText());

        // Handle match list - copy to avoid circular references
        List<Map<String, Object>> matchesList = new ArrayList<>();
        for (Match match : matches.getMatchList()) {
            Map<String, Object> matchMap = new HashMap<>();
            matchMap.put("region", match.getRegion());
            matchMap.put("score", match.getScore());
            matchMap.put("name", match.getName());

            if (match.getStateObjectData() != null) {
                Map<String, String> stateObjectData = new HashMap<>();
                stateObjectData.put("ownerStateName", match.getOwnerStateName());
                stateObjectData.put("stateObjectName", match.getStateObjectData().getStateObjectName());
                matchMap.put("stateObjectData", stateObjectData);
            }

            matchesList.add(matchMap);
        }
        map.put("matchList", matchesList);

        return map;
    }

    /**
     * Serializes Matches to JSON, handling special cases
     */
    public String matchesToJson(Matches matches) throws ConfigurationException {
        return jsonUtils.toJsonSafe(matches);
    }

    /**
     * Creates a deep copy of Matches by serializing and deserializing
     */
    public Matches deepCopy(Matches matches) throws ConfigurationException {
        String json = matchesToJson(matches);
        try {
            return jsonParser.convertJson(json, Matches.class);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Failed to create deep copy of Matches", e);
        }
    }

    /**
     * Custom serializer for Mat objects
     */
    public static class MatSerializer extends JsonSerializer<Mat> {
        @Override
        public void serialize(Mat mat, JsonGenerator gen, SerializerProvider provider) throws IOException {
            // Instead of trying to serialize the full Mat, just output dimensions
            gen.writeStartObject();
            if (mat != null && !mat.isNull()) {
                gen.writeNumberField("rows", mat.rows());
                gen.writeNumberField("cols", mat.cols());
                gen.writeNumberField("channels", mat.channels());
                gen.writeStringField("type", "Mat");
            } else {
                gen.writeStringField("type", "null");
            }
            gen.writeEndObject();
        }
    }
}