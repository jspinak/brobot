package io.github.jspinak.brobot.runner.json.serializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;

/**
 * Custom serializer for {@link ActionResult} objects (formerly Matches) that provides safe
 * serialization by handling circular references and complex object graphs.
 *
 * <p>This serializer addresses several critical challenges when serializing ActionResult:
 *
 * <ul>
 *   <li><b>Circular References:</b> ActionResult contains Match objects that may reference back to
 *       parent objects or contain StateObjectData with circular dependencies, potentially causing
 *       infinite recursion.
 *   <li><b>Complex State Management:</b> Match objects contain StateObjectData that tracks
 *       ownership relationships between states and objects, which can create complex reference
 *       graphs.
 *   <li><b>Native Resources:</b> ActionResult may contain references to mask images, scene analysis
 *       data, and other objects with native memory that cannot be serialized directly.
 *   <li><b>Large Data Structures:</b> Fields like sceneAnalysisCollection and actionLifecycle can
 *       be very large and contain redundant or circular data.
 * </ul>
 *
 * <p><b>Serialization Strategy:</b>
 *
 * <p>This serializer carefully selects which fields to include:
 *
 * <ul>
 *   <li><b>Basic Fields:</b> Includes all simple fields like success flags, timing data, text
 *       results, and state names
 *   <li><b>Match List:</b> Creates sanitized copies of Match objects that exclude problematic
 *       fields while preserving essential match data
 *   <li><b>Excluded Fields:</b> Skips mask, sceneAnalysisCollection, actionLifecycle, and
 *       actionConfig to avoid serialization issues
 * </ul>
 *
 * <p><b>Match Sanitization Process:</b>
 *
 * <ol>
 *   <li>Creates new Match objects using the Builder pattern
 *   <li>Copies only safe fields: region, score, name
 *   <li>Carefully reconstructs StateObjectData with only essential fields
 *   <li>Avoids copying any fields that could contain circular references
 * </ol>
 *
 * <p><b>Output Format Example:</b>
 *
 * <pre>{@code
 * {
 *   "actionDescription": "Click on button",
 *   "success": true,
 *   "duration": "PT0.523S",
 *   "startTime": "2024-01-15T10:30:00Z",
 *   "endTime": "2024-01-15T10:30:00.523Z",
 *   "selectedText": null,
 *   "activeStates": ["MainMenu"],
 *   "definedRegions": {...},
 *   "text": "Button clicked",
 *   "matchList": [
 *     {
 *       "region": {"x": 100, "y": 200, "w": 50, "h": 30},
 *       "score": 0.95,
 *       "name": "submitButton",
 *       "stateObjectData": {...}
 *     }
 *   ]
 * }
 * }</pre>
 *
 * @see ActionResult
 * @see Match
 * @see StateObjectMetadata
 * @see JsonSerializer
 */
@Component
public class MatchesSerializer extends JsonSerializer<ActionResult> {
    /**
     * Serializes an ActionResult object to JSON format, carefully handling complex fields and
     * avoiding circular references.
     *
     * <p><b>Serialization Process:</b>
     *
     * <ol>
     *   <li>Writes all simple fields directly (strings, booleans, timestamps)
     *   <li>Handles optional text field with null checking
     *   <li>Creates sanitized copies of Match objects to avoid circular references
     *   <li>Skips problematic fields that could cause serialization failures
     * </ol>
     *
     * <p><b>Match List Handling:</b>
     *
     * <p>Each Match in the matchList is reconstructed using a Builder to include only:
     *
     * <ul>
     *   <li>Region data (position and dimensions)
     *   <li>Similarity score
     *   <li>Object name
     *   <li>Minimal StateObjectData (owner state and object names only)
     * </ul>
     *
     * <p><b>Excluded Fields:</b>
     *
     * <ul>
     *   <li>{@code mask} - Contains image data that's too large for JSON
     *   <li>{@code sceneAnalysisCollection} - Complex object with circular references
     *   <li>{@code actionLifecycle} - Contains execution state that may reference parent objects
     *   <li>{@code actionConfig} - Has its own serializer to handle its complexity
     * </ul>
     *
     * @param matches the ActionResult to serialize
     * @param gen the JsonGenerator used to write JSON content
     * @param provider the SerializerProvider (not used directly but passed to field serializers)
     * @throws IOException if there's an error writing to the JsonGenerator
     */
    @Override
    public void serialize(ActionResult matches, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
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
            Match.Builder builder =
                    new Match.Builder()
                            .setRegion(match.getRegion())
                            .setSimScore(match.getScore())
                            .setName(match.getName());

            // Properly handle StateObjectData
            if (match.getStateObjectData() != null) {
                // Create a new StateObjectData with the necessary info
                StateObjectMetadata stateObjectData = new StateObjectMetadata();
                stateObjectData.setOwnerStateName(match.getOwnerStateName());
                stateObjectData.setStateObjectName(match.getStateObjectData().getStateObjectName());

                // Set the StateObjectData on the builder
                builder.setStateObjectData(stateObjectData);
            }

            sanitizedMatches.add(builder.build());
        }
        gen.writeObjectField("matchList", sanitizedMatches);

        // Skip problematic fields: mask, sceneAnalysisCollection, actionLifecycle, actionConfig
        gen.writeEndObject();
    }
}
