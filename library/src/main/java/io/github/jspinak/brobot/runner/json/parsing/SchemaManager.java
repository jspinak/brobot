package io.github.jspinak.brobot.runner.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages JSON schema loading, caching, and validation for the Brobot configuration system.
 * <p>
 * This class provides centralized schema management for validating JSON configurations
 * against predefined schemas. It supports JSON Schema draft v7 and provides efficient
 * caching to avoid repeated schema loading from resources.
 * <p>
 * Key features:
 * <ul>
 * <li>Schema caching - Schemas are loaded once and cached for performance</li>
 * <li>Multiple schema support - Manages different schemas for various config types</li>
 * <li>Validation utilities - Provides both boolean and detailed error validation</li>
 * <li>Resource-based loading - Schemas are loaded from classpath resources</li>
 * </ul>
 * <p>
 * Supported schemas:
 * <ul>
 * <li>Project schema - Validates project configuration structure</li>
 * <li>Automation DSL schema - Validates automation workflow definitions</li>
 * </ul>
 *
 * @see com.networknt.schema.JsonSchema
 * @see JsonNode
 * @see ConfigurationException
 */
@Component
public class SchemaManager {
    private final Logger logger = LoggerFactory.getLogger(SchemaManager.class);

    // Updated schema paths
    public final String PROJECT_SCHEMA_PATH = "/schemas/project-schema.json";
    public final String AUTOMATION_DSL_SCHEMA_PATH = "/schemas/automation-dsl-schema.json";

    private final Map<String, JsonSchema> schemaCache = new HashMap<>();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    private final BrobotObjectMapper mapper;

    public SchemaManager(BrobotObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

    /**
     * Retrieves a JSON schema from cache or loads it from resources.
     * <p>
     * This method implements lazy loading with caching. The first request for a schema
     * triggers loading from the classpath, and subsequent requests return the cached instance.
     *
     * @param schemaPath The classpath path to the schema resource (e.g., "/schemas/project-schema.json")
     * @return The loaded JsonSchema instance
     * @throws ConfigurationException if the schema cannot be loaded or is invalid
     */
    public JsonSchema getSchema(String schemaPath) throws ConfigurationException {
        if (schemaCache.containsKey(schemaPath)) {
            return schemaCache.get(schemaPath);
        }
        try {
            JsonSchema schema = loadSchema(schemaPath);
            schemaCache.put(schemaPath, schema);
            return schema;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load schema " + schemaPath + ": " + e.getMessage(), e);
        }
    }

    /**
     * Validates a JSON node against a specified schema.
     * <p>
     * This method returns the complete set of validation messages, allowing callers
     * to inspect all validation errors in detail. An empty set indicates valid JSON.
     *
     * @param json The JSON node to validate
     * @param schemaPath The classpath path to the schema to validate against
     * @return A set of validation messages; empty if validation passes
     * @throws ConfigurationException if the schema cannot be loaded
     */
    public Set<ValidationMessage> validate(JsonNode json, String schemaPath) throws ConfigurationException {
        JsonSchema schema = getSchema(schemaPath);
        return schema.validate(json);
    }

    /**
     * Checks if a JSON node is valid according to a specified schema.
     * <p>
     * This is a convenience method that returns a simple boolean result.
     * Use {@link #validate(JsonNode, String)} for detailed error information.
     *
     * @param json The JSON node to validate
     * @param schemaPath The classpath path to the schema to validate against
     * @return true if the JSON is valid, false otherwise
     * @throws ConfigurationException if the schema cannot be loaded
     */
    public boolean isValid(JsonNode json, String schemaPath) throws ConfigurationException {
        return validate(json, schemaPath).isEmpty();
    }

    /**
     * Validates JSON and throws an exception with detailed error messages if validation fails.
     * <p>
     * This method is useful when you want validation to halt execution on failure.
     * All validation errors are collected into a single, formatted error message
     * for clear problem identification.
     *
     * @param json The JSON node to validate
     * @param schemaPath The classpath path to the schema to validate against
     * @param configName A descriptive name for the configuration being validated (used in error messages)
     * @throws ConfigurationException containing all validation errors if validation fails
     */
    public void validateWithErrors(JsonNode json, String schemaPath, String configName) throws ConfigurationException {
        Set<ValidationMessage> errors = validate(json, schemaPath);
        if (!errors.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder("Invalid " + configName + " configuration:");
            for (ValidationMessage error : errors) {
                errorMsg.append("\n- ").append(error.getMessage());
            }
            throw new ConfigurationException(errorMsg.toString());
        }
    }

    /**
     * Loads a JSON schema from classpath resources.
     * <p>
     * Schemas must be available as classpath resources. This method handles resource
     * loading and converts the schema JSON into a JsonSchema instance using the
     * configured schema factory.
     *
     * @param schemaPath The classpath path to the schema resource
     * @return The loaded JsonSchema instance
     * @throws IOException if the schema resource cannot be found or read
     */
    private JsonSchema loadSchema(String schemaPath) throws IOException {
        try (InputStream schemaStream = SchemaManager.class.getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new IOException("Schema not found: " + schemaPath);
            }
            JsonNode schemaNode = mapper.readTree(schemaStream);
            return schemaFactory.getSchema(schemaNode);
        }
    }

    /**
     * Clears the schema cache, forcing schemas to be reloaded on next access.
     * <p>
     * This method is useful during development or when schemas may have changed.
     * In production, schemas are typically static and clearing the cache is unnecessary.
     */
    public void clearCache() {
        schemaCache.clear();
        logger.debug("Schema cache cleared");
    }

    /**
     * Retrieves the project configuration schema.
     * <p>
     * The project schema defines the structure for project-level configurations
     * including metadata, dependencies, and project settings.
     *
     * @return The project configuration JsonSchema
     * @throws ConfigurationException if the schema cannot be loaded
     */
    public JsonSchema getProjectSchema() throws ConfigurationException {
        return getSchema(PROJECT_SCHEMA_PATH);
    }

    /**
     * Retrieves the automation DSL (Domain Specific Language) schema.
     * <p>
     * The automation DSL schema defines the structure for automation workflows,
     * including states, transitions, actions, and other automation primitives.
     *
     * @return The automation DSL JsonSchema
     * @throws ConfigurationException if the schema cannot be loaded
     */
    public JsonSchema getAutomationDslSchema() throws ConfigurationException {
        return getSchema(AUTOMATION_DSL_SCHEMA_PATH);
    }
}
