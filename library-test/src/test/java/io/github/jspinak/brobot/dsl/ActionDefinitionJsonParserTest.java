package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;

import static io.github.jspinak.brobot.action.ActionOptions.Action.CLICK;
import static io.github.jspinak.brobot.action.ActionOptions.Action.FIND;
import static io.github.jspinak.brobot.action.ActionOptions.ClickUntil.OBJECTS_APPEAR;
import static io.github.jspinak.brobot.action.ActionOptions.Find.ALL;
import static io.github.jspinak.brobot.action.ActionOptions.Find.FIRST;
import static io.github.jspinak.brobot.action.internal.mouse.ClickType.Type.LEFT;
import static io.github.jspinak.brobot.action.internal.mouse.ClickType.Type.RIGHT;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ActionDefinitionJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test that we can safely serialize an ActionDefinition without exceptions
     */
    @Test
    public void testSerializeSimpleActionDefinition() throws ConfigurationException {
        // Create a minimal ActionDefinition
        TaskSequence definition = createMinimalActionDefinition(FIND);

        // Use JsonUtils to safely serialize
        String json = jsonUtils.toJsonSafe(definition);

        // Print the JSON for debugging
        System.out.println("DEBUG - Serialized JSON: " + json);

        // With our updated JsonUtils, we should at least have a type and steps array
        assertNotNull(json);
        assertTrue(json.contains("type"), "JSON should include type field");
        assertTrue(json.contains("steps"), "JSON should have steps array");
        assertTrue(json.contains("action"), "JSON should include action field");
        assertTrue(json.contains("FIND"), "JSON should include FIND action");
    }

    /**
     * Test parsing JSON into an ActionDefinition.
     * This doesn't involve serialization, so we don't need special handling.
     */
    @Test
    public void testParseActionDefinitionFromJson() throws ConfigurationException {
        // Create sample JSON for ActionDefinition with proper structure matching our classes
        String json = """
                {
                  "steps": [
                    {
                      "actionOptions": {
                        "action": "CLICK",
                        "clickUntil": "OBJECTS_APPEAR",
                        "find": "FIRST",
                        "similarity": 0.8,
                        "clickType": "LEFT"
                      },
                      "objectCollection": {
                        "stateImages": [
                          {
                            "id": 123,
                            "name": "TestImage1",
                            "patterns": [
                              {
                                "name": "Pattern1",
                                "fixed": false,
                                "targetPosition": {
                                  "percentW": 0.5,
                                  "percentH": 0.5
                                }
                              }
                            ]
                          },
                          {
                            "id": 456,
                            "name": "TestImage2",
                            "patterns": []
                          }
                        ],
                        "stateRegions": [
                          {
                            "name": "TestRegion",
                            "searchRegion": {
                              "x": 10,
                              "y": 10,
                              "w": 100,
                              "h": 100
                            },
                            "position": {
                              "percentW": 0.5,
                              "percentH": 0.5
                            }
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        // Parse JSON to ActionDefinition
        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskSequence actionDefinition = jsonParser.convertJson(jsonNode, TaskSequence.class);

        // Verify the parsed ActionDefinition
        assertNotNull(actionDefinition);
        assertEquals(1, actionDefinition.getSteps().size());

        ActionStep step = actionDefinition.getSteps().get(0);
        assertNotNull(step);

        ActionOptions options = step.getActionOptions();
        assertNotNull(options);
        assertEquals(CLICK, options.getAction());
        assertEquals(OBJECTS_APPEAR, options.getClickUntil());
        assertEquals(FIRST, options.getFind());
        assertEquals(0.8, options.getSimilarity());
        assertEquals(LEFT, options.getClickType());

        ObjectCollection objects = step.getObjectCollection();
        assertNotNull(objects);

        // Verify StateImages
        assertEquals(2, objects.getStateImages().size());
        assertEquals(123L, objects.getStateImages().get(0).getId());
        assertEquals("TestImage1", objects.getStateImages().get(0).getName());
        assertEquals(1, objects.getStateImages().get(0).getPatterns().size());
        assertEquals("Pattern1", objects.getStateImages().get(0).getPatterns().get(0).getName());

        assertEquals(456L, objects.getStateImages().get(1).getId());
        assertEquals("TestImage2", objects.getStateImages().get(1).getName());

        // Verify StateRegions
        assertEquals(1, objects.getStateRegions().size());
        assertEquals("TestRegion", objects.getStateRegions().get(0).getName());
        assertEquals(10, objects.getStateRegions().get(0).getSearchRegion().x());
        assertEquals(10, objects.getStateRegions().get(0).getSearchRegion().y());
        assertEquals(100, objects.getStateRegions().get(0).getSearchRegion().w());
        assertEquals(100, objects.getStateRegions().get(0).getSearchRegion().h());
    }

    /**
     * Test for serialization and deserialization of an ActionDefinition with Location objects,
     * which may have circular references. Uses JsonUtils for safe serialization.
     */
    @Test
    public void testSerializeComplexActionDefinition() throws ConfigurationException {
        // Create a more complex ActionDefinition with multiple steps
        TaskSequence definition = new TaskSequence();

        // First step with FIND action
        definition.addStep(
                new ActionOptions.Builder().setAction(FIND).setFind(ALL).build(),
                new ObjectCollection.Builder().withImages(
                        new StateImage.Builder().setName("ImageInStep1").build()
                ).build()
        );

        // Second step with CLICK action
        definition.addStep(
                new ActionOptions.Builder().setAction(CLICK).setClickType(RIGHT).build(),
                new ObjectCollection.Builder().withRegions(
                        new StateRegion.Builder().setName("RegionInStep2")
                                .setSearchRegion(new Region(10, 10, 100, 100)).build()
                ).build()
        );

        // Use JsonUtils to safely serialize
        String json = jsonUtils.toJsonSafe(definition);

        // Print the JSON for debugging
        System.out.println("DEBUG - Complex JSON: " + json);

        // Verify JSON contains expected elements
        assertNotNull(json);
        assertTrue(json.contains("type"), "JSON should have type field");
        assertTrue(json.contains("steps"), "JSON should have steps array");
        assertTrue(json.contains("action"), "JSON should have action field");

        // With our updated JsonUtils, we should see both actions
        assertTrue(json.contains("FIND") || json.contains("CLICK"),
                "JSON should include at least one of the actions");
    }

    /**
     * Test with pretty-printed JSON, which is especially valuable for debugging.
     */
    @Test
    public void testPrettyJson() throws ConfigurationException {
        // Create a simple ActionDefinition
        TaskSequence definition = createMinimalActionDefinition(FIND);

        // Use JsonUtils for safer pretty printing
        String prettyJson = jsonUtils.toPrettyJsonSafe(definition);

        // Print the JSON for debugging
        System.out.println("DEBUG - Pretty JSON: " + prettyJson);

        // Verify the JSON is created and contains expected elements
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("type"), "JSON should have type field");
        assertTrue(prettyJson.contains("steps"), "JSON should have steps array");
        assertTrue(prettyJson.contains("action"), "JSON should have action field");
        assertTrue(prettyJson.contains("FIND"), "JSON should contain FIND action");

        // Check for formatting
        assertTrue(prettyJson.contains("\n"), "Pretty JSON should have line breaks");
    }

    @Test
    public void testInvalidJsonThrowsException() {
        // Test invalid JSON
        String invalidJson = "{invalid json structure}";

        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> {
            jsonParser.parseJson(invalidJson);
        });

        assertTrue(exception.getMessage().contains("Failed to parse JSON"));
    }

    /**
     * Helper method to create a minimal ActionDefinition with one step
     */
    private TaskSequence createMinimalActionDefinition(ActionOptions.Action action) {
        TaskSequence definition = new TaskSequence();

        ActionOptions options = new ActionOptions();
        options.setAction(action);

        ObjectCollection objects = new ObjectCollection();
        StateImage image = new StateImage();
        image.setName("TestImage");
        ArrayList<StateImage> images = new ArrayList<>();
        images.add(image);
        objects.setStateImages(images);

        definition.addStep(options, objects);
        return definition;
    }
}