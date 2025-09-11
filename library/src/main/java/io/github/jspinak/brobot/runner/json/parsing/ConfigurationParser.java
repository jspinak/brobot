package io.github.jspinak.brobot.runner.json.parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

/**
 * Provides robust JSON parsing and serialization capabilities for the Brobot framework.
 *
 * <p>This class acts as a central JSON processing utility, offering both standard and safe
 * serialization approaches. It handles common serialization challenges such as circular references,
 * problematic third-party objects, and complex object graphs that may cause standard Jackson
 * serialization to fail.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Standard JSON operations using the configured BrobotObjectMapper
 *   <li>Fallback "safe" serialization for problematic objects
 *   <li>File I/O operations for JSON persistence
 *   <li>Schema validation through SchemaManager integration
 *   <li>Automatic pretty-printing support
 * </ul>
 *
 * <p>The class maintains two BrobotObjectMappers:
 *
 * <ul>
 *   <li>Primary mapper - Configured with all Brobot-specific serializers and mixins
 *   <li>Fallback mapper - Filters out known problematic fields for safe serialization
 * </ul>
 *
 * @see SchemaManager
 * @see BrobotObjectMapper
 * @see ConfigurationException
 */
@Component
public class ConfigurationParser {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationParser.class);

    private final SchemaManager schemaManager;
    private final BrobotObjectMapper objectMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper fallbackMapper;

    public ConfigurationParser(SchemaManager schemaManager, BrobotObjectMapper objectMapper) {
        this.schemaManager = schemaManager;
        this.objectMapper = objectMapper;
        this.fallbackMapper = createFallbackMapper();
    }

    /**
     * Creates a specialized fallback ObjectMapper for handling problematic objects.
     *
     * <p>This mapper is configured with filters to exclude fields that commonly cause serialization
     * issues such as circular references, native objects, or heavyweight resources. It serves as a
     * safety net when the primary mapper fails.
     *
     * <p>Excluded fields include:
     *
     * <ul>
     *   <li>Image-related: image, bufferedImage, mat, matBGR, matHSV
     *   <li>Low-level data: raster, data, colorModel, source
     *   <li>Sikuli objects: sikuli, screen
     *   <li>Pattern collections: patterns
     *   <li>Native/problematic properties: refcount, allocator, graphics, properties
     * </ul>
     *
     * @return A configured ObjectMapper with safe serialization settings
     */
    private com.fasterxml.jackson.databind.ObjectMapper createFallbackMapper() {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        // Configure for safe serialization
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.FAIL_ON_SELF_REFERENCES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Handle circular references by using identity information
        mapper.configure(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, true);

        // Add JavaTimeModule for handling LocalDateTime, Duration, etc.
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Exclude common problematic fields including "self" for circular refs and "error" for
        // throwing getters
        mapper.setFilterProvider(
                new SimpleFilterProvider()
                        .setDefaultFilter(
                                SimpleBeanPropertyFilter.serializeAllExcept(
                                        "image",
                                        "bufferedImage",
                                        "patterns",
                                        "mat",
                                        "raster",
                                        "data",
                                        "colorModel",
                                        "sikuli",
                                        "screen",
                                        "matBGR",
                                        "matHSV",
                                        "source",
                                        "refcount",
                                        "allocator",
                                        "graphics",
                                        "properties",
                                        "propertyNames",
                                        "accelerationPriority",
                                        "u",
                                        "step",
                                        "dims",
                                        "size",
                                        "self",
                                        "error"))
                        .setFailOnUnknownId(false));

        return mapper;
    }

    /**
     * Parses a JSON string into a JsonNode for flexible JSON manipulation.
     *
     * <p>JsonNode provides a tree-based representation of JSON data that can be navigated and
     * modified before conversion to strongly-typed objects.
     *
     * @param json The JSON string to parse
     * @return A JsonNode representing the parsed JSON structure
     * @throws ConfigurationException if the JSON string is malformed or parsing fails
     */
    public JsonNode parseJson(String json) throws ConfigurationException {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a JsonNode to a strongly-typed object of the specified class.
     *
     * <p>This method uses the configured BrobotObjectMapper with all Brobot-specific serializers
     * and deserializers to properly reconstruct domain objects.
     *
     * @param <T> The type of the target object
     * @param json The JsonNode to convert
     * @param clazz The target class type
     * @return An instance of the specified class populated from the JSON data
     * @throws ConfigurationException if conversion fails due to type mismatch or missing data
     */
    public <T> T convertJson(JsonNode json, Class<T> clazz) throws ConfigurationException {
        try {
            return objectMapper.treeToValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException("Failed to convert JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Converts a JSON string directly to an object of the specified class.
     *
     * <p>This is a convenience method that combines parsing and conversion in a single step. It's
     * more efficient than parsing to JsonNode first when you know the target type.
     *
     * @param <T> The type of the target object
     * @param json The JSON string to convert
     * @param clazz The target class type
     * @return An instance of the specified class populated from the JSON data
     * @throws ConfigurationException if the JSON is malformed or conversion fails
     */
    public <T> T convertJson(String json, Class<T> clazz) throws ConfigurationException {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to convert JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Converts an object to a JSON string with automatic fallback to safe serialization.
     *
     * <p>This method first attempts standard serialization using the primary BrobotObjectMapper. If
     * that fails (typically due to circular references or problematic fields), it automatically
     * falls back to the safe serialization approach that filters out known problematic fields.
     *
     * <p>The fallback behavior is logged as a warning for debugging purposes.
     *
     * @param object The object to serialize
     * @return A JSON string representation of the object
     * @throws ConfigurationException if both standard and safe serialization fail
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
     * Converts an object to a pretty-printed JSON string with automatic fallback.
     *
     * <p>Similar to {@link #toJson(Object)} but produces human-readable formatted JSON with proper
     * indentation and line breaks. Useful for configuration files, debugging output, and human
     * inspection.
     *
     * @param object The object to serialize
     * @return A pretty-printed JSON string representation of the object
     * @throws ConfigurationException if both standard and safe serialization fail
     * @see #toJson(Object)
     */
    public String toPrettyJson(Object object) throws ConfigurationException {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn(
                    "Standard pretty serialization failed, using fallback approach: {}",
                    e.getMessage());
            return toPrettyJsonSafe(object);
        }
    }

    /**
     * Directly serializes an object using the safe fallback approach.
     *
     * <p>This method bypasses the standard serialization attempt and goes straight to the filtered
     * approach. Use this when you know the object contains problematic fields (e.g., BufferedImage,
     * Mat, circular references) to avoid the overhead of a failed standard serialization attempt.
     *
     * <p>Common use cases:
     *
     * <ul>
     *   <li>Objects containing Sikuli Screen or Region references
     *   <li>Objects with OpenCV Mat fields
     *   <li>Complex object graphs with potential circular references
     * </ul>
     *
     * @param object The object to serialize safely
     * @return A JSON string with problematic fields filtered out
     * @throws ConfigurationException if safe serialization fails
     */
    public String toJsonSafe(Object object) throws ConfigurationException {
        try {
            return fallbackMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException(
                    "Failed to serialize object safely: " + e.getMessage(), e);
        }
    }

    /**
     * Directly serializes an object using the safe fallback approach with pretty printing.
     *
     * <p>Combines the benefits of safe serialization with human-readable formatting. This is
     * particularly useful for debugging or generating configuration files from objects that contain
     * problematic fields.
     *
     * @param object The object to serialize safely
     * @return A pretty-printed JSON string with problematic fields filtered out
     * @throws ConfigurationException if safe serialization fails
     * @see #toJsonSafe(Object)
     */
    public String toPrettyJsonSafe(Object object) throws ConfigurationException {
        try {
            return fallbackMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ConfigurationException(
                    "Failed to serialize object safely: " + e.getMessage(), e);
        }
    }

    /**
     * Writes an object to a JSON file with automatic safe serialization fallback.
     *
     * <p>This method combines serialization with file I/O, automatically handling problematic
     * objects by falling back to safe serialization if needed. The output is always pretty-printed
     * for readability.
     *
     * <p>The method ensures the parent directories exist before writing.
     *
     * @param object The object to write to file
     * @param filePath The path where the JSON file should be written
     * @throws ConfigurationException if serialization fails
     * @throws IOException if file writing fails
     */
    public void writeToFile(Object object, Path filePath)
            throws ConfigurationException, IOException {
        // Ensure parent directories exist
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String json = toPrettyJson(object);
        Files.writeString(filePath, json);
    }

    /**
     * Writes an object to a JSON file using only safe serialization.
     *
     * <p>This method bypasses standard serialization and uses only the filtered approach, making it
     * ideal for objects known to contain problematic fields. The output is pretty-printed for
     * readability.
     *
     * @param object The object to write to file safely
     * @param filePath The path where the JSON file should be written
     * @throws ConfigurationException if safe serialization fails
     * @throws IOException if file writing fails
     * @see #toJsonSafe(Object)
     */
    public void writeToFileSafe(Object object, Path filePath)
            throws ConfigurationException, IOException {
        // Ensure parent directories exist
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String json = toPrettyJsonSafe(object);
        Files.writeString(filePath, json);
    }

    /**
     * Reads a JSON file and deserializes it to an object of the specified class.
     *
     * <p>This method handles the complete file-to-object conversion process, using the configured
     * BrobotObjectMapper with all Brobot-specific deserializers to properly reconstruct domain
     * objects.
     *
     * @param <T> The type of the target object
     * @param filePath The path to the JSON file to read
     * @param clazz The target class type
     * @return An instance of the specified class populated from the JSON file
     * @throws ConfigurationException if JSON parsing or conversion fails
     * @throws IOException if file reading fails
     */
    public <T> T readFromFile(Path filePath, Class<T> clazz)
            throws ConfigurationException, IOException {
        String json = Files.readString(filePath);
        return convertJson(json, clazz);
    }
}
