package io.github.jspinak.brobot.json.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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