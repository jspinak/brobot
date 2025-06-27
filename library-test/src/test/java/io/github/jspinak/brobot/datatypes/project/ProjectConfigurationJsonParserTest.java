package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.dsl.BusinessTask;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.runner.project.AutomationConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ProjectConfigurationJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing ProjectConfiguration with default values from JSON
     */
    @Test
    public void testParseMinimalConfiguration() throws ConfigurationException {
        String json = """
                {
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationConfiguration config = jsonParser.convertJson(jsonNode, AutomationConfiguration.class);

        // Verify default values
        assertNotNull(config);
        assertEquals(0.7, config.getMinSimilarity(), 0.001);
        assertEquals(0.5, config.getMoveMouseDelay(), 0.001);
        assertEquals(0.3, config.getDelayBeforeMouseDown(), 0.001);
        assertEquals(0.3, config.getDelayAfterMouseDown(), 0.001);
        assertEquals(0.3, config.getDelayBeforeMouseUp(), 0.001);
        assertEquals(0.3, config.getDelayAfterMouseUp(), 0.001);
        assertEquals(0.3, config.getTypeDelay(), 0.001);
        assertEquals(0.5, config.getPauseBetweenActions(), 0.001);
        assertEquals(10.0, config.getMaxWait(), 0.001);
        assertNull(config.getImageDirectory());
        assertEquals("INFO", config.getLogLevel());
        assertTrue(config.getIllustrationEnabled());
        assertNull(config.getAutomationFunctions());
    }

    /**
     * Test parsing a full ProjectConfiguration from JSON
     */
    @Test
    public void testParseFullConfiguration() throws ConfigurationException {
        String json = """
                {
                  "minSimilarity": 0.9,
                  "moveMouseDelay": 0.2,
                  "delayBeforeMouseDown": 0.1,
                  "delayAfterMouseDown": 0.1,
                  "delayBeforeMouseUp": 0.1,
                  "delayAfterMouseUp": 0.1,
                  "typeDelay": 0.2,
                  "pauseBetweenActions": 0.3,
                  "maxWait": 5.0,
                  "imageDirectory": "/custom/images",
                  "logLevel": "DEBUG",
                  "illustrationEnabled": false,
                  "automationFunctions": [
                    {
                      "id": 1,
                      "name": "login",
                      "returnType": "boolean",
                      "statements": []
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationConfiguration config = jsonParser.convertJson(jsonNode, AutomationConfiguration.class);

        // Verify configured values
        assertNotNull(config);
        assertEquals(0.9, config.getMinSimilarity(), 0.001);
        assertEquals(0.2, config.getMoveMouseDelay(), 0.001);
        assertEquals(0.1, config.getDelayBeforeMouseDown(), 0.001);
        assertEquals(0.1, config.getDelayAfterMouseDown(), 0.001);
        assertEquals(0.1, config.getDelayBeforeMouseUp(), 0.001);
        assertEquals(0.1, config.getDelayAfterMouseUp(), 0.001);
        assertEquals(0.2, config.getTypeDelay(), 0.001);
        assertEquals(0.3, config.getPauseBetweenActions(), 0.001);
        assertEquals(5.0, config.getMaxWait(), 0.001);
        assertEquals("/custom/images", config.getImageDirectory());
        assertEquals("DEBUG", config.getLogLevel());
        assertFalse(config.getIllustrationEnabled());

        // Verify automation functions
        assertNotNull(config.getAutomationFunctions());
        assertEquals(1, config.getAutomationFunctions().size());
        assertEquals(Integer.valueOf(1), config.getAutomationFunctions().getFirst().getId());
        assertEquals("login", config.getAutomationFunctions().getFirst().getName());
        assertEquals("boolean", config.getAutomationFunctions().getFirst().getReturnType());
    }

    /**
     * Test serialization of a ProjectConfiguration
     */
    @Test
    public void testSerializeProjectConfiguration() throws ConfigurationException {
        // Create a configuration
        AutomationConfiguration config = new AutomationConfiguration();
        config.setMinSimilarity(0.85);
        config.setMoveMouseDelay(0.25);
        config.setMaxWait(7.5);
        config.setImageDirectory("/test/images");
        config.setLogLevel("WARN");

        // Add automation functions
        List<BusinessTask> functions = new ArrayList<>();
        BusinessTask function = new BusinessTask();
        function.setId(2);
        function.setName("testFunction");
        function.setReturnType("void");
        functions.add(function);
        config.setAutomationFunctions(functions);

        // Serialize
        String json = jsonUtils.toJsonSafe(config);
        System.out.println("DEBUG: Serialized ProjectConfiguration: " + json);

        // Deserialize and verify
        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationConfiguration deserializedConfig = jsonParser.convertJson(jsonNode, AutomationConfiguration.class);

        assertEquals(0.85, deserializedConfig.getMinSimilarity(), 0.001);
        assertEquals(0.25, deserializedConfig.getMoveMouseDelay(), 0.001);
        assertEquals(7.5, deserializedConfig.getMaxWait(), 0.001);
        assertEquals("/test/images", deserializedConfig.getImageDirectory());
        assertEquals("WARN", deserializedConfig.getLogLevel());

        assertNotNull(deserializedConfig.getAutomationFunctions());
        assertEquals(1, deserializedConfig.getAutomationFunctions().size());
        assertEquals(Integer.valueOf(2), deserializedConfig.getAutomationFunctions().getFirst().getId());
        assertEquals("testFunction", deserializedConfig.getAutomationFunctions().getFirst().getName());
    }
}