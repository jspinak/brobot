package io.github.jspinak.brobot.runner.project;

import com.fasterxml.jackson.databind.JsonNode;

import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.runner.project.RunnerInterface;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AutomationUIJsonParserTest {
    
    @BeforeAll
    static void setupHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
    }

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing an AutomationUI with empty buttons list from JSON
     */
    @Test
    public void testParseEmptyAutomationUI() throws ConfigurationException {
        String json = """
                {
                  "buttons": []
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        RunnerInterface automationUI = jsonParser.convertJson(jsonNode, RunnerInterface.class);

        assertNotNull(automationUI);
        assertNotNull(automationUI.getButtons());
        assertTrue(automationUI.getButtons().isEmpty());
    }

    /**
     * Test parsing an AutomationUI with buttons from JSON
     */
    @Test
    public void testParseAutomationUIWithButtons() throws ConfigurationException {
        String json = """
                {
                  "buttons": [
                    {
                      "id": "btn1",
                      "label": "Button 1",
                      "functionName": "function1",
                      "category": "Category A"
                    },
                    {
                      "id": "btn2",
                      "label": "Button 2",
                      "functionName": "function2",
                      "category": "Category B"
                    }
                  ]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        RunnerInterface automationUI = jsonParser.convertJson(jsonNode, RunnerInterface.class);

        assertNotNull(automationUI);
        assertNotNull(automationUI.getButtons());
        assertEquals(2, automationUI.getButtons().size());

        // Verify first button
        TaskButton button1 = automationUI.getButtons().getFirst();
        assertEquals("btn1", button1.getId());
        assertEquals("Button 1", button1.getLabel());
        assertEquals("function1", button1.getFunctionName());
        assertEquals("Category A", button1.getCategory());

        // Verify second button
        TaskButton button2 = automationUI.getButtons().get(1);
        assertEquals("btn2", button2.getId());
        assertEquals("Button 2", button2.getLabel());
        assertEquals("function2", button2.getFunctionName());
        assertEquals("Category B", button2.getCategory());
    }

    /**
     * Test serializing an AutomationUI to JSON
     */
    @Test
    public void testSerializeAutomationUI() throws ConfigurationException {
        // Create an AutomationUI with buttons
        RunnerInterface automationUI = new RunnerInterface();
        List<TaskButton> buttons = new ArrayList<>();

        // Create first button
        TaskButton button1 = new TaskButton();
        button1.setId("test-btn1");
        button1.setLabel("Test Button 1");
        button1.setFunctionName("testFunction1");
        buttons.add(button1);

        // Create second button
        TaskButton button2 = new TaskButton();
        button2.setId("test-btn2");
        button2.setLabel("Test Button 2");
        button2.setFunctionName("testFunction2");
        buttons.add(button2);

        automationUI.setButtons(buttons);

        // Serialize
        String json = jsonUtils.toJsonSafe(automationUI);
        System.out.println("DEBUG: Serialized AutomationUI: " + json);

        // Deserialize and verify
        JsonNode jsonNode = jsonParser.parseJson(json);
        RunnerInterface deserializedUI = jsonParser.convertJson(jsonNode, RunnerInterface.class);

        assertNotNull(deserializedUI);
        assertNotNull(deserializedUI.getButtons());
        assertEquals(2, deserializedUI.getButtons().size());

        // Verify first button
        TaskButton deserializedButton1 = deserializedUI.getButtons().getFirst();
        assertEquals("test-btn1", deserializedButton1.getId());
        assertEquals("Test Button 1", deserializedButton1.getLabel());
        assertEquals("testFunction1", deserializedButton1.getFunctionName());

        // Verify second button
        TaskButton deserializedButton2 = deserializedUI.getButtons().get(1);
        assertEquals("test-btn2", deserializedButton2.getId());
        assertEquals("Test Button 2", deserializedButton2.getLabel());
        assertEquals("testFunction2", deserializedButton2.getFunctionName());
    }
}