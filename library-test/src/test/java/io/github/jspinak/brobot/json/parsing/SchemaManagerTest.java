package io.github.jspinak.brobot.json.parsing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

    @Test
    void testGetSchema() throws ConfigurationException {
        assertNotNull(schemaManager.getProjectSchema());
        assertNotNull(schemaManager.getAutomationDslSchema());
    }

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
    void testIsValid() throws IOException, ConfigurationException {
        JsonNode validJson = objectMapper.readTree(getClass().getResourceAsStream(VALID_PROJECT_JSON));
        assertTrue(schemaManager.isValid(validJson, schemaManager.PROJECT_SCHEMA_PATH));

        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_PROJECT_JSON));
        assertFalse(schemaManager.isValid(invalidJson, schemaManager.PROJECT_SCHEMA_PATH));
    }

    @Test
    void testValidateWithErrors() throws IOException {
        JsonNode invalidJson = objectMapper.readTree(getClass().getResourceAsStream(INVALID_PROJECT_JSON));
        ConfigurationException exception = assertThrows(ConfigurationException.class, () ->
                schemaManager.validateWithErrors(invalidJson, schemaManager.PROJECT_SCHEMA_PATH, "Project"));
        assertTrue(exception.getMessage().contains("Invalid Project configuration"));
    }

    @Test
    void testClearCache() throws ConfigurationException {
        schemaManager.getProjectSchema();
        schemaManager.clearCache();
        assertDoesNotThrow(schemaManager::getProjectSchema);
    }
}