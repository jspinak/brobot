package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JsonParser provides methods for working with JSON in a robust way.
 * Uses the configured ObjectMapper to parse and convert JSON.
 */
@Component
public class JsonParser {

    private static final Logger log = LoggerFactory.getLogger(JsonParser.class);

    private final SchemaManager schemaManager;
    private final ObjectMapper objectMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper fallbackMapper;

    public JsonParser(SchemaManager schemaManager, ObjectMapper objectMapper) {
        this.schemaManager = schemaManager;
        this.objectMapper = objectMapper;
        this.fallbackMapper = createFallbackMapper();
    }

    /**
     * Creates a specialized fallback mapper for handling problematic objects
     */
    private com.fasterxml.jackson.databind.ObjectMapper createFallbackMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Exclude common problematic fields
        mapper.setFilterProvider(new SimpleFilterProvider()
                .setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(
                        "image", "bufferedImage", "patterns", "mat", "raster", "data",
                        "colorModel", "sikuli", "screen", "matBGR", "matHSV"))
                .setFailOnUnknownId(false));

        return mapper;
    }

    /**
     * Parse a JSON string into a JsonNode
     */
    public JsonNode parseJson(String json) throws ConfigurationException {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert a JsonNode to an object of the specified class
     */
    public <T> T convertJson(JsonNode json, Class<T> clazz) throws ConfigurationException {
        try {
            return objectMapper.treeToValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Failed to convert JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert a JSON string to an object of the specified class
     */
    public <T> T convertJson(String json, Class<T> clazz) throws ConfigurationException {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to convert JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Convert an object to a JSON string, using safe serialization that handles problematic classes
     */
    public String toJson(Object object) throws ConfigurationException {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Standard serialization failed, using fallback approach: {}", e.getMessage());
            return toJsonSafe(object);
        }
    }

    /**
     * Convert an object to a pretty-printed JSON string, using safe serialization that handles problematic classes
     */
    public String toPrettyJson(Object object) throws ConfigurationException {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn("Standard pretty serialization failed, using fallback approach: {}", e.getMessage());
            return toPrettyJsonSafe(object);
        }
    }

    /**
     * Directly uses the fallback serialization approach without trying standard serialization first.
     * Useful when you know the object contains problematic fields or circular references.
     */
    public String toJsonSafe(Object object) throws ConfigurationException {
        try {
            return fallbackMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
        }
    }

    /**
     * Directly uses the fallback serialization approach with pretty printing.
     * Useful when you know the object contains problematic fields or circular references.
     */
    public String toPrettyJsonSafe(Object object) throws ConfigurationException {
        try {
            return fallbackMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Failed to serialize object safely: " + e.getMessage(), e);
        }
    }

    /**
     * Writes an object to a JSON file, safely handling problematic classes
     */
    public void writeToFile(Object object, Path filePath) throws ConfigurationException, IOException {
        String json = toPrettyJson(object);
        Files.writeString(filePath, json);
    }

    /**
     * Writes an object to a JSON file using the safe serialization approach
     */
    public void writeToFileSafe(Object object, Path filePath) throws ConfigurationException, IOException {
        String json = toPrettyJsonSafe(object);
        Files.writeString(filePath, json);
    }

    /**
     * Reads a JSON file and converts it to an object of the specified class
     */
    public <T> T readFromFile(Path filePath, Class<T> clazz) throws ConfigurationException, IOException {
        String json = Files.readString(filePath);
        return convertJson(json, clazz);
    }
}