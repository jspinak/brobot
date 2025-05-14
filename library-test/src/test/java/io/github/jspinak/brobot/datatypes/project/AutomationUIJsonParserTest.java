package io.github.jspinak.brobot.datatypes.project;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class AutomationUIJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

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
        AutomationUI automationUI = jsonParser.convertJson(jsonNode, AutomationUI.class);

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
        AutomationUI automationUI = jsonParser.convertJson(jsonNode, AutomationUI.class);

        assertNotNull(automationUI);
        assertNotNull(automationUI.getButtons());
        assertEquals(2, automationUI.getButtons().size());

        // Verify first button
        Button button1 = automationUI.getButtons().getFirst();
        assertEquals("btn1", button1.getId());
        assertEquals("Button 1", button1.getLabel());
        assertEquals("function1", button1.getFunctionName());
        assertEquals("Category A", button1.getCategory());

        // Verify second button
        Button button2 = automationUI.getButtons().get(1);
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
        AutomationUI automationUI = new AutomationUI();
        List<Button> buttons = new ArrayList<>();

        // Create first button
        Button button1 = new Button();
        button1.setId("test-btn1");
        button1.setLabel("Test Button 1");
        button1.setFunctionName("testFunction1");
        buttons.add(button1);

        // Create second button
        Button button2 = new Button();
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
        AutomationUI deserializedUI = jsonParser.convertJson(jsonNode, AutomationUI.class);

        assertNotNull(deserializedUI);
        assertNotNull(deserializedUI.getButtons());
        assertEquals(2, deserializedUI.getButtons().size());

        // Verify first button
        Button deserializedButton1 = deserializedUI.getButtons().getFirst();
        assertEquals("test-btn1", deserializedButton1.getId());
        assertEquals("Test Button 1", deserializedButton1.getLabel());
        assertEquals("testFunction1", deserializedButton1.getFunctionName());

        // Verify second button
        Button deserializedButton2 = deserializedUI.getButtons().get(1);
        assertEquals("test-btn2", deserializedButton2.getId());
        assertEquals("Test Button 2", deserializedButton2.getLabel());
        assertEquals("testFunction2", deserializedButton2.getFunctionName());
    }
}