package io.github.jspinak.brobot.json.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for serializing ObjectCollection to and from JSON.
 * Handles special cases and complex nested objects.
 */
@Component
public class ObjectCollectionJsonUtils {

    private static final Logger log = LoggerFactory.getLogger(ObjectCollectionJsonUtils.class);
    private final JsonUtils jsonUtils;
    private final MatchesJsonUtils matchesJsonUtils;
    private final JsonParser jsonParser;

    public ObjectCollectionJsonUtils(JsonUtils jsonUtils, MatchesJsonUtils matchesJsonUtils, JsonParser jsonParser) {
        this.jsonUtils = jsonUtils;
        this.matchesJsonUtils = matchesJsonUtils;
        this.jsonParser = jsonParser;
    }

    /**
     * Converts ObjectCollection to a Map representation that's easier to work with
     * for custom serialization, excluding problematic fields
     */
    public Map<String, Object> objectCollectionToMap(ObjectCollection collection) {
        Map<String, Object> map = new HashMap<>();

        // Add collection fields
        map.put("stateLocations", collection.getStateLocations());
        map.put("stateImages", collection.getStateImages());
        map.put("stateRegions", collection.getStateRegions());
        map.put("stateStrings", collection.getStateStrings());

        // For matches, create simplified versions
        map.put("matches", collection.getMatches().stream()
                .map(matchesJsonUtils::matchesToMap)
                .toList());

        // For scenes, just include filenames
        map.put("scenes", collection.getScenes().stream()
                .map(scene -> Map.of("filename", scene.getPattern().getName()))
                .toList());

        return map;
    }

    /**
     * Serializes ObjectCollection to JSON, handling special cases
     */
    public String objectCollectionToJson(ObjectCollection collection) throws ConfigurationException {
        return jsonUtils.toJsonSafe(collection);
    }

    /**
     * Creates a deep copy of ObjectCollection by serializing and deserializing
     */
    public ObjectCollection deepCopy(ObjectCollection collection) throws ConfigurationException {
        String json = objectCollectionToJson(collection);
        try {
            return jsonParser.convertJson(json, ObjectCollection.class);
        } catch (ConfigurationException e) {
            throw new ConfigurationException("Failed to create deep copy of ObjectCollection", e);
        }
    }
}