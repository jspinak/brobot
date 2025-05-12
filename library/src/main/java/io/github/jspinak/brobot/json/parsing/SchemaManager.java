package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages JSON schemas for the Brobot Runner configuration system.
 */
@Component
public class SchemaManager {
    private final Logger logger = LoggerFactory.getLogger(SchemaManager.class);

    // Updated schema paths
    public final String PROJECT_SCHEMA_PATH = "/schemas/project-schema.json";
    public final String AUTOMATION_DSL_SCHEMA_PATH = "/schemas/automation-dsl-schema.json";

    private final Map<String, JsonSchema> schemaCache = new HashMap<>();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    private final ObjectMapper mapper;

    public SchemaManager(ObjectMapper objectMapper) {
        this.mapper = objectMapper;
    }

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

    public Set<ValidationMessage> validate(JsonNode json, String schemaPath) throws ConfigurationException {
        JsonSchema schema = getSchema(schemaPath);
        return schema.validate(json);
    }

    public boolean isValid(JsonNode json, String schemaPath) throws ConfigurationException {
        return validate(json, schemaPath).isEmpty();
    }

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

    private JsonSchema loadSchema(String schemaPath) throws IOException {
        try (InputStream schemaStream = SchemaManager.class.getResourceAsStream(schemaPath)) {
            if (schemaStream == null) {
                throw new IOException("Schema not found: " + schemaPath);
            }
            JsonNode schemaNode = mapper.readTree(schemaStream);
            return schemaFactory.getSchema(schemaNode);
        }
    }

    public void clearCache() {
        schemaCache.clear();
        logger.debug("Schema cache cleared");
    }

    // Updated schema getters
    public JsonSchema getProjectSchema() throws ConfigurationException {
        return getSchema(PROJECT_SCHEMA_PATH);
    }

    public JsonSchema getAutomationDslSchema() throws ConfigurationException {
        return getSchema(AUTOMATION_DSL_SCHEMA_PATH);
    }
}
