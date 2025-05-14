package io.github.jspinak.brobot.json.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class that provides additional JSON functionality focused on
 * handling problematic Java classes (circular references, module system issues).
 */
@Component
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private final JsonParser jsonParser;
    private final io.github.jspinak.brobot.json.parsing.ObjectMapper objectMapper;
    private final ObjectMapper circularReferenceMapper;

    public JsonUtils(JsonParser jsonParser, io.github.jspinak.brobot.json.parsing.ObjectMapper objectMapper) {
        this.jsonParser = jsonParser;
        this.objectMapper = objectMapper;
        this.circularReferenceMapper = createCircularReferenceMapper();
    }

    /**
     * Creates a mapper specifically designed to handle circular references
     */
    private ObjectMapper createCircularReferenceMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Enable detection of circular references
        mapper.enable(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL);

        // Configure to use fields directly rather than getters
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);

        return mapper;
    }

    /**
     * Safely serializes an object to JSON, handling circular references and module system issues
     */
    public String toJsonSafe(Object object) throws ConfigurationException {
        // First try with the standard JsonParser
        try {
            return jsonParser.toJson(object);
        } catch (ConfigurationException e) {
            log.warn("Standard serialization failed: {}", e.getMessage());

            // Then try with the circular reference mapper
            try {
                return circularReferenceMapper.writeValueAsString(object);
            } catch (JsonProcessingException ex) {
                log.error("Failed to serialize object using all methods: {}", ex.getMessage());
                throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Safely serializes an object to pretty-printed JSON, handling circular references
     */
    public String toPrettyJsonSafe(Object object) throws ConfigurationException {
        // First try with the standard JsonParser
        try {
            return jsonParser.toPrettyJson(object);
        } catch (ConfigurationException e) {
            log.warn("Standard pretty serialization failed: {}", e.getMessage());

            // Then try with the circular reference mapper
            try {
                return circularReferenceMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } catch (JsonProcessingException ex) {
                log.error("Failed to pretty serialize object using all methods: {}", ex.getMessage());
                throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Writes an object to a file using safe serialization
     */
    public void writeToFileSafe(Object object, Path filePath) throws ConfigurationException, IOException {
        String json = toPrettyJsonSafe(object);
        Files.writeString(filePath, json);
    }

    /**
     * Converts an object to JSON and back to ensure it can be safely serialized
     * This is useful for validating that an object can be properly serialized and deserialized
     */
    public <T> T validateSerializationCycle(Object object, Class<T> clazz) throws ConfigurationException {
        String json = toJsonSafe(object);
        return jsonParser.convertJson(json, clazz);
    }
}