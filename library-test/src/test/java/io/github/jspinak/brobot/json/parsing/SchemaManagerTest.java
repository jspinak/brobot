package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

import io.github.jspinak.brobot.runner.json.parsing.SchemaManager;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SchemaManagerTest {

    private static final String VALID_PROJECT_JSON = "/valid-project.json";
    private static final String INVALID_PROJECT_JSON = "/invalid-project.json";
    private static final String VALID_AUTOMATION_JSON = "/valid-automation.json";
    private static final String INVALID_AUTOMATION_JSON = "/invalid-automation.json";

    private ObjectMapper objectMapper;

    @Autowired
    private SchemaManager schemaManager;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        schemaManager.clearCache();
    }

    // Group 1: Schema retrieval tests
    @Test
    void testGetSchema() throws ConfigurationException {
        assertNotNull(schemaManager.getProjectSchema(), "Project schema should not be null");
        assertNotNull(schemaManager.getAutomationDslSchema(), "Automation DSL schema should not be null");
    }

    @Test
    void testInvalidSchemaPath() {
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            schemaManager.getSchema("/nonexistent-schema.json");
        });
        assertTrue(exception.getMessage().contains("Failed to load schema"), "Exception message should indicate schema loading failure");
    }

    // Group 2: Validation tests for project schema
    @Test
    void testValidateValidProjectJson() throws IOException, ConfigurationException {
        JsonNode validJson = objectMapper.readTree(getClass().getResourceAsStream(VALID_PROJECT_JSON));
        Set<ValidationMessage> errors = schemaManager.validate(validJson, schemaManager.PROJECT_SCHEMA_PATH);
        assertTrue(errors.isEmpty(), "Expected no validation errors for valid project JSON");
    }

    @Test
    void testValidateInvalidProjectJson() throws IOException, ConfigurationException {
        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_PROJECT_JSON));
        Set<ValidationMessage> errors = schemaManager.validate(invalidJson, schemaManager.PROJECT_SCHEMA_PATH);
        assertFalse(errors.isEmpty(), "Expected validation errors for invalid project JSON");
    }

    @Test
    void testIsValidForProjectSchema() throws IOException, ConfigurationException {
        JsonNode validJson = objectMapper.readTree(getClass().getResourceAsStream(VALID_PROJECT_JSON));
        assertTrue(schemaManager.isValid(validJson, schemaManager.PROJECT_SCHEMA_PATH), "Valid project JSON should return true");

        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_PROJECT_JSON));
        assertFalse(schemaManager.isValid(invalidJson, schemaManager.PROJECT_SCHEMA_PATH), "Invalid project JSON should return false");
    }

    @Test
    void testValidateWithErrorsForProjectSchema() throws IOException {
        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_PROJECT_JSON));
        ConfigurationException exception = assertThrows(ConfigurationException.class, () ->
                schemaManager.validateWithErrors(invalidJson, schemaManager.PROJECT_SCHEMA_PATH, "Project"));
        assertTrue(exception.getMessage().contains("Invalid Project configuration"), "Exception message should indicate invalid project configuration");
    }

    // Group 3: Validation tests for automation DSL schema
    @Test
    void testValidateValidAutomationJson() throws IOException, ConfigurationException {
        JsonNode validJson = objectMapper.readTree(getClass().getResourceAsStream(VALID_AUTOMATION_JSON));
        Set<ValidationMessage> errors = schemaManager.validate(validJson, schemaManager.AUTOMATION_DSL_SCHEMA_PATH);
        assertTrue(errors.isEmpty(), "Expected no validation errors for valid automation JSON");
    }

    @Test
    void testValidateInvalidAutomationJson() throws IOException, ConfigurationException {
        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_AUTOMATION_JSON));
        Set<ValidationMessage> errors = schemaManager.validate(invalidJson, schemaManager.AUTOMATION_DSL_SCHEMA_PATH);
        assertFalse(errors.isEmpty(), "Expected validation errors for invalid automation JSON");
    }

    @Test
    void testIsValidForAutomationSchema() throws IOException, ConfigurationException {
        JsonNode validJson = objectMapper.readTree(getClass().getResourceAsStream(VALID_AUTOMATION_JSON));
        assertTrue(schemaManager.isValid(validJson, schemaManager.AUTOMATION_DSL_SCHEMA_PATH), "Valid automation JSON should return true");

        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_AUTOMATION_JSON));
        assertFalse(schemaManager.isValid(invalidJson, schemaManager.AUTOMATION_DSL_SCHEMA_PATH), "Invalid automation JSON should return false");
    }

    // Group 4: Cache management tests
    @Test
    void testClearCache() throws ConfigurationException {
        JsonSchema schema1 = schemaManager.getSchema(schemaManager.PROJECT_SCHEMA_PATH);
        assertNotNull(schema1, "First schema retrieval should not return null");

        schemaManager.clearCache();

        JsonSchema schema2 = schemaManager.getSchema(schemaManager.PROJECT_SCHEMA_PATH);
        assertNotNull(schema2, "Second schema retrieval after cache clear should not return null");
        assertNotSame(schema1, schema2, "Schemas should be different objects after cache clearing");
    }
}