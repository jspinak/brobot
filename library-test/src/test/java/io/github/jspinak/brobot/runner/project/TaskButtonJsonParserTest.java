package io.github.jspinak.brobot.runner.project;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.runner.project.TaskButton;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TaskButtonJsonParserTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a simple Button from JSON
     */
    @Test
    public void testParseSimpleButton() throws ConfigurationException {
        String json = """
                {
                  "id": "simple-btn",
                  "label": "Click Me",
                  "functionName": "handleClick"
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskButton button = jsonParser.convertJson(jsonNode, TaskButton.class);

        assertNotNull(button);
        assertEquals("simple-btn", button.getId());
        assertEquals("Click Me", button.getLabel());
        assertEquals("handleClick", button.getFunctionName());
        assertNull(button.getTooltip());
        assertNull(button.getCategory());
        assertNull(button.getIcon());
        assertFalse(button.isConfirmationRequired());
        assertNull(button.getConfirmationMessage());
        assertNull(button.getPosition());
        assertNull(button.getStyling());
    }

    /**
     * Test parsing a Button with position from JSON
     */
    @Test
    public void testParseButtonWithPosition() throws ConfigurationException {
        String json = """
                {
                  "id": "positioned-btn",
                  "label": "Positioned Button",
                  "functionName": "doSomething",
                  "position": {
                    "row": 2,
                    "column": 3,
                    "order": 5
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskButton button = jsonParser.convertJson(jsonNode, TaskButton.class);

        assertNotNull(button);
        assertEquals("positioned-btn", button.getId());

        // Verify position
        assertNotNull(button.getPosition());
        assertEquals(Integer.valueOf(2), button.getPosition().getRow());
        assertEquals(Integer.valueOf(3), button.getPosition().getColumn());
        assertEquals(Integer.valueOf(5), button.getPosition().getOrder());
    }

    /**
     * Test parsing a Button with styling from JSON
     */
    @Test
    public void testParseButtonWithStyling() throws ConfigurationException {
        String json = """
                {
                  "id": "styled-btn",
                  "label": "Styled Button",
                  "functionName": "performAction",
                  "styling": {
                    "backgroundColor": "#ff0000",
                    "textColor": "#ffffff",
                    "size": "large",
                    "customClass": "special-button"
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskButton button = jsonParser.convertJson(jsonNode, TaskButton.class);

        assertNotNull(button);
        assertEquals("styled-btn", button.getId());

        // Verify styling
        assertNotNull(button.getStyling());
        assertEquals("#ff0000", button.getStyling().getBackgroundColor());
        assertEquals("#ffffff", button.getStyling().getTextColor());
        assertEquals("large", button.getStyling().getSize());
        assertEquals("special-button", button.getStyling().getCustomClass());
    }

    /**
     * Test parsing a Button with confirmation from JSON
     */
    @Test
    public void testParseButtonWithConfirmation() throws ConfigurationException {
        String json = """
                {
                  "id": "confirm-btn",
                  "label": "Delete",
                  "functionName": "deleteItem",
                  "confirmationRequired": true,
                  "confirmationMessage": "Are you sure you want to delete this item?"
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskButton button = jsonParser.convertJson(jsonNode, TaskButton.class);

        assertNotNull(button);
        assertEquals("confirm-btn", button.getId());
        assertTrue(button.isConfirmationRequired());
        assertEquals("Are you sure you want to delete this item?", button.getConfirmationMessage());
    }

    /**
     * Test parsing a Button with parameters from JSON
     */
    @Test
    public void testParseButtonWithParameters() throws ConfigurationException {
        String json = """
                {
                  "id": "param-btn",
                  "label": "Process",
                  "functionName": "processData",
                  "parameters": {
                    "maxItems": 10,
                    "filter": "active",
                    "options": {
                      "sortBy": "date",
                      "ascending": true
                    }
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskButton button = jsonParser.convertJson(jsonNode, TaskButton.class);

        assertNotNull(button);
        assertEquals("param-btn", button.getId());

        // Verify parameters exist
        // The actual parsing depends on how you handle 'Object' type in parameters
        assertNotNull(button.getParameters());
    }

    /**
     * Test serializing a Button to JSON
     */
    @Test
    public void testSerializeButton() throws ConfigurationException {
        // Create a button with all fields
        TaskButton button = new TaskButton();
        button.setId("test-btn");
        button.setLabel("Test Button");
        button.setTooltip("A button for testing");
        button.setFunctionName("testFunction");
        button.setCategory("Testing");
        button.setIcon("test-icon");
        button.setConfirmationRequired(true);
        button.setConfirmationMessage("Confirm test?");

        // Set position
        TaskButton.ButtonPosition position = new TaskButton.ButtonPosition();
        position.setRow(1);
        position.setColumn(2);
        position.setOrder(3);
        button.setPosition(position);

        // Set styling
        TaskButton.ButtonStyling styling = new TaskButton.ButtonStyling();
        styling.setBackgroundColor("#00ff00");
        styling.setTextColor("#000000");
        styling.setSize("medium");
        styling.setCustomClass("test-button");
        button.setStyling(styling);

        // Serialize
        String json = jsonUtils.toJsonSafe(button);
        System.out.println("DEBUG: Serialized Button: " + json);

        // Deserialize and verify
        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskButton deserializedButton = jsonParser.convertJson(jsonNode, TaskButton.class);

        assertEquals("test-btn", deserializedButton.getId());
        assertEquals("Test Button", deserializedButton.getLabel());
        assertEquals("A button for testing", deserializedButton.getTooltip());
        assertEquals("testFunction", deserializedButton.getFunctionName());
        assertEquals("Testing", deserializedButton.getCategory());
        assertEquals("test-icon", deserializedButton.getIcon());
        assertTrue(deserializedButton.isConfirmationRequired());
        assertEquals("Confirm test?", deserializedButton.getConfirmationMessage());

        // Verify position
        assertNotNull(deserializedButton.getPosition());
        assertEquals(Integer.valueOf(1), deserializedButton.getPosition().getRow());
        assertEquals(Integer.valueOf(2), deserializedButton.getPosition().getColumn());
        assertEquals(Integer.valueOf(3), deserializedButton.getPosition().getOrder());

        // Verify styling
        assertNotNull(deserializedButton.getStyling());
        assertEquals("#00ff00", deserializedButton.getStyling().getBackgroundColor());
        assertEquals("#000000", deserializedButton.getStyling().getTextColor());
        assertEquals("medium", deserializedButton.getStyling().getSize());
        assertEquals("test-button", deserializedButton.getStyling().getCustomClass());
    }
}