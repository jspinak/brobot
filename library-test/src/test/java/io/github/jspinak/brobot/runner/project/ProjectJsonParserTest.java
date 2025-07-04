package io.github.jspinak.brobot.runner.project;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.TaskButton;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ProjectJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic Project from JSON
     */
    @Test
    public void testParseBasicProject() throws ConfigurationException {
        String json = """
                {
                  "id": 1,
                  "name": "Test Project",
                  "description": "A project for testing",
                  "version": "1.0.0",
                  "created": "2023-01-01T12:00:00",
                  "updated": "2023-01-02T12:00:00",
                  "states": [],
                  "stateTransitions": []
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationProject project = jsonParser.convertJson(jsonNode, AutomationProject.class);

        // Verify project fields
        assertEquals(1L, project.getId());
        assertEquals("Test Project", project.getName());
        assertEquals("A project for testing", project.getDescription());
        assertEquals("1.0.0", project.getVersion());
        assertNotNull(project.getCreated());
        assertNotNull(project.getUpdated());
        assertNotNull(project.getStates());
        assertTrue(project.getStates().isEmpty());
        assertNotNull(project.getStateTransitions());
        assertTrue(project.getStateTransitions().isEmpty());
    }

    /**
     * Test parsing a Project with configuration from JSON
     */
    @Test
    public void testParseProjectWithConfiguration() throws ConfigurationException {
        String json = """
                {
                  "id": 2,
                  "name": "Project With Config",
                  "states": [],
                  "stateTransitions": [],
                  "configuration": {
                    "minSimilarity": 0.8,
                    "moveMouseDelay": 0.3,
                    "maxWait": 5.0,
                    "imageDirectory": "/images",
                    "logLevel": "DEBUG",
                    "illustrationEnabled": false
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationProject project = jsonParser.convertJson(jsonNode, AutomationProject.class);

        // Verify basic project fields
        assertEquals(2L, project.getId());
        assertEquals("Project With Config", project.getName());

        // Verify configuration
        assertNotNull(project.getConfiguration());
        assertEquals(0.8, project.getConfiguration().getMinSimilarity(), 0.001);
        assertEquals(0.3, project.getConfiguration().getMoveMouseDelay(), 0.001);
        assertEquals(5.0, project.getConfiguration().getMaxWait(), 0.001);
        assertEquals("/images", project.getConfiguration().getImageDirectory());
        assertEquals("DEBUG", project.getConfiguration().getLogLevel());
        assertFalse(project.getConfiguration().getIllustrationEnabled());
    }

    /**
     * Test parsing a Project with automation UI settings from JSON
     */
    @Test
    public void testParseProjectWithAutomation() throws ConfigurationException {
        String json = """
                {
                  "id": 3,
                  "name": "Project With Automation",
                  "states": [],
                  "stateTransitions": [],
                  "automation": {
                    "buttons": [
                      {
                        "id": "btn1",
                        "label": "Login",
                        "functionName": "performLogin",
                        "tooltip": "Click to login",
                        "category": "Authentication",
                        "icon": "login-icon",
                        "confirmationRequired": true,
                        "confirmationMessage": "Are you sure you want to login?",
                        "position": {
                          "row": 1,
                          "column": 2,
                          "order": 1
                        },
                        "styling": {
                          "backgroundColor": "#007bff",
                          "textColor": "#ffffff",
                          "size": "medium",
                          "customClass": "login-button"
                        }
                      }
                    ]
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        AutomationProject project = jsonParser.convertJson(jsonNode, AutomationProject.class);

        // Verify basic project fields
        assertEquals(3L, project.getId());
        assertEquals("Project With Automation", project.getName());

        // Verify automation
        assertNotNull(project.getAutomation());
        assertNotNull(project.getAutomation().getButtons());
        assertEquals(1, project.getAutomation().getButtons().size());

        // Verify button details
        TaskButton button = project.getAutomation().getButtons().getFirst();
        assertEquals("btn1", button.getId());
        assertEquals("Login", button.getLabel());
        assertEquals("performLogin", button.getFunctionName());
        assertEquals("Click to login", button.getTooltip());
        assertEquals("Authentication", button.getCategory());
        assertEquals("login-icon", button.getIcon());
        assertTrue(button.isConfirmationRequired());
        assertEquals("Are you sure you want to login?", button.getConfirmationMessage());

        // Verify button position
        assertNotNull(button.getPosition());
        assertEquals(Integer.valueOf(1), button.getPosition().getRow());
        assertEquals(Integer.valueOf(2), button.getPosition().getColumn());
        assertEquals(Integer.valueOf(1), button.getPosition().getOrder());

        // Verify button styling
        assertNotNull(button.getStyling());
        assertEquals("#007bff", button.getStyling().getBackgroundColor());
        assertEquals("#ffffff", button.getStyling().getTextColor());
        assertEquals("medium", button.getStyling().getSize());
        assertEquals("login-button", button.getStyling().getCustomClass());
    }

    /**
     * Test serializing a Project to JSON
     */
    @Test
    public void testSerializeProject() throws ConfigurationException {
        // Create a new project
        AutomationProject project = new AutomationProject();
        project.setId(5L);
        project.setName("Serialization Test Project");

        // Serialize
        String json = jsonUtils.toJsonSafe(project);
        System.out.println("DEBUG: Serialized Project: " + json);

        // Validate serialized JSON
        JsonNode jsonNode = jsonParser.parseJson(json);
        assertTrue(jsonNode.has("id"));
        assertEquals(5, jsonNode.get("id").asLong());
        assertTrue(jsonNode.has("name"));
        assertEquals("Serialization Test Project", jsonNode.get("name").asText());
    }
}