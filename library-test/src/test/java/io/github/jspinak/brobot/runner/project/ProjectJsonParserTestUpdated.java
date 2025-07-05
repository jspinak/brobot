package io.github.jspinak.brobot.runner.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.runner.project.AutomationProject;
import io.github.jspinak.brobot.runner.project.TaskButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import io.github.jspinak.brobot.BrobotTestApplication;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for Project JSON parsing with Spring context for proper JSON configuration.
 */
@SpringBootTest(classes = BrobotTestApplication.class)
@TestPropertySource(properties = {
    "spring.main.lazy-initialization=true",
    "brobot.mock.enabled=true",
    "brobot.illustration.disabled=true",
    "brobot.scene.analysis.disabled=true"
})
public class ProjectJsonParserTestUpdated {

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // ObjectMapper is now injected by Spring with all necessary configurations
    }

    /**
     * Test parsing a basic Project from JSON
     */
    @Test
    public void testParseBasicProject() throws Exception {
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

        JsonNode jsonNode = objectMapper.readTree(json);
        AutomationProject project = objectMapper.treeToValue(jsonNode, AutomationProject.class);

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
    public void testParseProjectWithConfiguration() throws Exception {
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

        JsonNode jsonNode = objectMapper.readTree(json);
        AutomationProject project = objectMapper.treeToValue(jsonNode, AutomationProject.class);

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
     * Test parsing a Project with task buttons
     */
    @Test
    public void testParseProjectWithTaskButtons() throws Exception {
        String json = """
                {
                  "id": 3,
                  "name": "Project With Tasks",
                  "states": [],
                  "stateTransitions": [],
                  "automation": {
                    "buttons": [
                    {
                      "id": 1,
                      "label": "Start Process",
                      "tooltip": "Starts the automation process",
                      "functionName": "startProcess",
                      "position": {
                        "row": 0,
                        "column": 0
                      }
                    },
                    {
                      "id": 2,
                      "label": "Stop Process",
                      "tooltip": "Stops the automation process",
                      "functionName": "stopProcess",
                      "position": {
                        "row": 0,
                        "column": 1
                      }
                    }
                    ]
                  }
                }
                """;

        AutomationProject project = objectMapper.readValue(json, AutomationProject.class);

        // Verify project fields
        assertEquals(3L, project.getId());
        assertEquals("Project With Tasks", project.getName());

        // Verify task buttons
        assertNotNull(project.getAutomation());
        assertNotNull(project.getAutomation().getButtons());
        assertEquals(2, project.getAutomation().getButtons().size());

        TaskButton button1 = project.getAutomation().getButtons().get(0);
        assertEquals("1", button1.getId());
        assertEquals("Start Process", button1.getLabel());
        assertEquals("Starts the automation process", button1.getTooltip());
        assertEquals("startProcess", button1.getFunctionName());
        assertNotNull(button1.getPosition());
        assertEquals(0, button1.getPosition().getRow());
        assertEquals(0, button1.getPosition().getColumn());

        TaskButton button2 = project.getAutomation().getButtons().get(1);
        assertEquals("2", button2.getId());
        assertEquals("Stop Process", button2.getLabel());
        assertEquals("stopProcess", button2.getFunctionName());
    }

    /**
     * Test serialization of AutomationProject
     */
    @Test
    public void testSerializeProject() throws Exception {
        // Create a project
        AutomationProject project = new AutomationProject();
        project.setId(10L);
        project.setName("Serialization Test");
        project.setDescription("Testing project serialization");
        project.setVersion("2.0.0");

        // Serialize
        String json = objectMapper.writeValueAsString(project);
        System.out.println("Serialized Project: " + json);

        // Verify JSON contains expected fields
        JsonNode jsonNode = objectMapper.readTree(json);
        assertEquals(10, jsonNode.get("id").asLong());
        assertEquals("Serialization Test", jsonNode.get("name").asText());
        assertEquals("Testing project serialization", jsonNode.get("description").asText());
        assertEquals("2.0.0", jsonNode.get("version").asText());

        // Deserialize and verify round-trip
        AutomationProject deserializedProject = objectMapper.readValue(json, AutomationProject.class);
        assertEquals(project.getId(), deserializedProject.getId());
        assertEquals(project.getName(), deserializedProject.getName());
        assertEquals(project.getDescription(), deserializedProject.getDescription());
        assertEquals(project.getVersion(), deserializedProject.getVersion());
    }

    /**
     * Test parsing with missing optional fields
     */
    @Test
    public void testParseMinimalProject() throws Exception {
        String json = """
                {
                  "id": 4,
                  "name": "Minimal Project"
                }
                """;

        AutomationProject project = objectMapper.readValue(json, AutomationProject.class);

        // Verify required fields
        assertEquals(4L, project.getId());
        assertEquals("Minimal Project", project.getName());

        // Verify optional fields have defaults or are null
        assertNull(project.getDescription());
        assertNull(project.getVersion());
        // States and transitions should be initialized as empty lists
        if (project.getStates() != null) {
            assertTrue(project.getStates().isEmpty());
        }
        if (project.getStateTransitions() != null) {
            assertTrue(project.getStateTransitions().isEmpty());
        }
    }

    /**
     * Test handling of invalid JSON
     */
    @Test
    public void testInvalidJson() {
        String invalidJson = """
                {
                  "id": "not-a-number",
                  "name": "Invalid Project"
                }
                """;

        assertThrows(Exception.class, () -> {
            objectMapper.readValue(invalidJson, AutomationProject.class);
        });
    }
}