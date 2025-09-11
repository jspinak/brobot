package io.github.jspinak.brobot.runner.json.serializers;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import io.github.jspinak.brobot.action.ObjectCollection;

/**
 * Custom serializer for {@link ObjectCollection} that provides controlled serialization of complex
 * state management objects while preventing circular references and handling native resources.
 *
 * <p>This serializer addresses several critical challenges when serializing ObjectCollection:
 *
 * <ul>
 *   <li><b>Circular References:</b> ObjectCollection contains various state objects that may
 *       reference each other or back to the collection itself, creating circular dependencies that
 *       would cause infinite recursion during standard serialization.
 *   <li><b>Native Resources:</b> The collection contains Scene objects with BufferedImages and Mat
 *       (OpenCV matrix) objects that hold native memory references which cannot be meaningfully
 *       serialized to JSON.
 *   <li><b>Complex Object Graphs:</b> The collection aggregates multiple types of state objects
 *       (locations, images, regions, strings, matches, scenes) that each have their own
 *       serialization requirements.
 *   <li><b>Performance:</b> Full serialization of all nested objects would be extremely slow and
 *       produce very large JSON documents.
 * </ul>
 *
 * <p><b>Serialization Strategy:</b>
 *
 * <p>This serializer carefully orchestrates the serialization of each component:
 *
 * <ul>
 *   <li><b>State Components:</b> Directly serializes stateLocations, stateImages, stateRegions, and
 *       stateStrings as these are typically simple objects.
 *   <li><b>Matches:</b> Uses the provider's default serialization for Match objects, which have
 *       their own custom serializer to handle their complexity.
 *   <li><b>Scenes:</b> Delegates to custom serializers that skip BufferedImage and Mat fields to
 *       avoid native memory serialization issues.
 * </ul>
 *
 * <p><b>Output Format:</b>
 *
 * <pre>{@code
 * {
 *   "stateLocations": [...],
 *   "stateImages": [...],
 *   "stateRegions": [...],
 *   "stateStrings": [...],
 *   "matches": [...],
 *   "scenes": [...]
 * }
 * }</pre>
 *
 * @see ObjectCollection
 * @see MatchesSerializer
 * @see ImageSerializer
 * @see com.fasterxml.jackson.databind.JsonSerializer
 */
@Component
public class ObjectCollectionSerializer extends JsonSerializer<ObjectCollection> {
    /**
     * Serializes an ObjectCollection instance to JSON format, carefully handling each component
     * type to avoid circular references and native resource issues.
     *
     * <p><b>Serialization Process:</b>
     *
     * <ol>
     *   <li>Opens a JSON object
     *   <li>Writes state components (locations, images, regions, strings) directly
     *   <li>Serializes matches array using the provider's serialization mechanism
     *   <li>Writes scenes with special handling for BufferedImage/Mat exclusion
     *   <li>Closes the JSON object
     * </ol>
     *
     * <p><b>Special Handling:</b>
     *
     * <ul>
     *   <li>Matches are serialized individually to ensure each gets proper treatment from the
     *       MatchesSerializer
     *   <li>Scenes rely on custom serializers registered for their image/mat fields
     * </ul>
     *
     * @param collection the ObjectCollection to serialize
     * @param gen the JsonGenerator used to write JSON content
     * @param provider the SerializerProvider used to access other serializers
     * @throws IOException if there's an error writing to the JsonGenerator
     */
    @Override
    public void serialize(
            ObjectCollection collection, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeStartObject();

        // Write state locations
        gen.writeObjectField("stateLocations", collection.getStateLocations());

        // Write state images
        gen.writeObjectField("stateImages", collection.getStateImages());

        // Write state regions
        gen.writeObjectField("stateRegions", collection.getStateRegions());

        // Write state strings
        gen.writeObjectField("stateStrings", collection.getStateStrings());

        // Write matches - but with care to avoid circular references
        // We'll just write simplified versions
        gen.writeArrayFieldStart("matches");
        for (int i = 0; i < collection.getMatches().size(); i++) {
            provider.defaultSerializeValue(collection.getMatches().get(i), gen);
        }
        gen.writeEndArray();

        // Write scenes - but skip the bufferedImage and Mat fields
        gen.writeObjectField("scenes", collection.getScenes());

        gen.writeEndObject();
    }
}
