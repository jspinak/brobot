package io.github.jspinak.brobot.runner.json.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Provides advanced JSON serialization utilities with enhanced error handling and recovery.
 * <p>
 * This utility class extends the capabilities of {@link ConfigurationParser} by providing
 * additional fallback mechanisms for handling particularly challenging serialization
 * scenarios. It implements a multi-tier approach to JSON serialization:
 * <ol>
 * <li>Primary attempt using the standard Brobot-configured JsonParser</li>
 * <li>Fallback to a specially configured circular reference mapper</li>
 * <li>Error reporting with detailed failure information</li>
 * </ol>
 * <p>
 * Key features:
 * <ul>
 * <li>Circular reference handling - Serializes self-references as null</li>
 * <li>Module system compatibility - Handles classes across module boundaries</li>
 * <li>Field-based serialization - Bypasses problematic getters</li>
 * <li>Validation utilities - Ensures objects survive serialization roundtrip</li>
 * </ul>
 * <p>
 * The circular reference mapper is configured with:
 * <ul>
 * <li>Field visibility instead of getter/setter access</li>
 * <li>Self-references serialized as null to break cycles</li>
 * <li>Static typing to avoid runtime type information issues</li>
 * <li>Lenient deserialization for maximum compatibility</li>
 * </ul>
 *
 * @see ConfigurationParser
 * @see ConfigurationException
 */
@Component
public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    private final ConfigurationParser jsonParser;
    private final io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper objectMapper;
    private final ObjectMapper circularReferenceMapper;

    public JsonUtils(ConfigurationParser jsonParser, io.github.jspinak.brobot.runner.json.parsing.BrobotObjectMapper objectMapper) {
        this.jsonParser = jsonParser;
        this.objectMapper = objectMapper;
        this.circularReferenceMapper = createCircularReferenceMapper();
    }

    /**
     * Creates a specialized ObjectMapper configured to handle circular references.
     * <p>
     * This mapper serves as a fallback when standard serialization fails due to:
     * <ul>
     * <li>Circular object references causing infinite recursion</li>
     * <li>Module system access restrictions</li>
     * <li>Problematic getter methods that throw exceptions</li>
     * <li>Empty beans without serializable properties</li>
     * </ul>
     * <p>
     * Configuration strategy:
     * <ul>
     * <li>WRITE_SELF_REFERENCES_AS_NULL - Breaks circular reference chains</li>
     * <li>Field-based access - Avoids problematic getters</li>
     * <li>Static typing - Prevents runtime type resolution issues</li>
     * <li>Lenient settings - Maximizes serialization success rate</li>
     * </ul>
     *
     * @return A configured ObjectMapper optimized for difficult serialization cases
     */
    private ObjectMapper createCircularReferenceMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure features
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_SELF_REFERENCES_AS_NULL, true);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Add support for Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Configure to use fields rather than getters for better control
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);

        // IMPORTANT: Disable default typing to avoid array-based types
        // This ensures only the @JsonTypeInfo annotations on your classes control type information
        mapper.disable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.setConfig(mapper.getSerializationConfig().with(MapperFeature.USE_STATIC_TYPING));

        // Register the same modules used in your primary ObjectMapper
        // This ensures consistent handling of custom types
        SimpleModule module = new SimpleModule();
        mapper.registerModule(module);

        return mapper;
    }

    /**
     * Safely serializes an object to JSON with automatic fallback handling.
     * <p>
     * This method implements a two-tier serialization strategy:
     * <ol>
     * <li>Attempts standard serialization using the primary JsonParser</li>
     * <li>Falls back to the circular reference mapper on failure</li>
     * </ol>
     * <p>
     * Use this method when:
     * <ul>
     * <li>Objects may contain circular references</li>
     * <li>Standard serialization has failed previously</li>
     * <li>You need maximum serialization success rate</li>
     * <li>Objects cross module boundaries</li>
     * </ul>
     *
     * @param object The object to serialize (can be null)
     * @return A JSON string representation of the object
     * @throws ConfigurationException if all serialization attempts fail
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
     * Safely serializes an object to pretty-printed JSON with automatic fallback.
     * <p>
     * Similar to {@link #toJsonSafe(Object)} but produces human-readable formatted
     * output with proper indentation. The fallback mechanism ensures serialization
     * succeeds even for complex object graphs.
     *
     * @param object The object to serialize (can be null)
     * @return A pretty-printed JSON string representation
     * @throws ConfigurationException if all serialization attempts fail
     * @see #toJsonSafe(Object)
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
     * Writes an object to a JSON file using safe serialization with fallback.
     * <p>
     * This method combines safe serialization with file I/O, ensuring the object
     * is successfully serialized before writing. The output is always pretty-printed
     * for readability. Parent directories are created if they don't exist.
     *
     * @param object The object to write to file
     * @param filePath The target file path
     * @throws ConfigurationException if serialization fails
     * @throws IOException if file writing fails
     */
    public void writeToFileSafe(Object object, Path filePath) throws ConfigurationException, IOException {
        String json = toPrettyJsonSafe(object);
        // Create parent directories if they don't exist
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.writeString(filePath, json);
    }

    /**
     * Validates that an object can survive a complete serialization/deserialization cycle.
     * <p>
     * This method performs a roundtrip test by:
     * <ol>
     * <li>Serializing the object to JSON</li>
     * <li>Deserializing the JSON back to an object</li>
     * <li>Returning the deserialized object</li>
     * </ol>
     * <p>
     * This is useful for:
     * <ul>
     * <li>Testing serialization compatibility before storage</li>
     * <li>Ensuring objects can be transmitted via JSON APIs</li>
     * <li>Validating custom serializers/deserializers</li>
     * <li>Creating deep copies of objects (with data loss for transient fields)</li>
     * </ul>
     * <p>
     * Note: The returned object may not be identical to the input due to:
     * <ul>
     * <li>Transient fields being lost</li>
     * <li>Circular references being broken</li>
     * <li>Custom serialization transformations</li>
     * </ul>
     *
     * @param <T> The expected type of the deserialized object
     * @param object The object to validate
     * @param clazz The class to deserialize to
     * @return The object after serialization/deserialization
     * @throws ConfigurationException if serialization or deserialization fails
     */
    public <T> T validateSerializationCycle(Object object, Class<T> clazz) throws ConfigurationException {
        String json = toJsonSafe(object);
        return jsonParser.convertJson(json, clazz);
    }
}