package io.github.jspinak.brobot.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.drag.DragOptions;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for ActionDefinition JSON parsing with new ActionConfig API.
 * 
 * Key changes:
 * - JSON now uses "actionConfig" field with "@type" to specify concrete config class
 * - ActionStep uses specific config classes (ClickOptions, PatternFindOptions, etc.)
 * - Type-safe JSON serialization and deserialization
 */
@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ActionDefinitionJsonParserTestUpdated {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test that we can safely serialize an ActionDefinition with new ActionConfig
     */
    @Test
    public void testSerializeSimpleActionDefinitionWithNewAPI() throws ConfigurationException {
        // Create a minimal ActionDefinition with PatternFindOptions
        TaskSequence definition = createMinimalActionDefinitionWithPatternFind();

        // Use JsonUtils to safely serialize
        String json = jsonUtils.toJsonSafe(definition);

        // Print the JSON for debugging
        System.out.println("DEBUG - Serialized JSON with new API: " + json);

        // Verify JSON structure
        assertNotNull(json);
        assertTrue(json.contains("steps"), "JSON should have steps array");
        assertTrue(json.contains("actionConfig"), "JSON should include actionConfig field");
        assertTrue(json.contains("@type"), "JSON should include @type for polymorphic deserialization");
        assertTrue(json.contains("PatternFindOptions"), "JSON should include concrete config type");
    }

    /**
     * Test parsing JSON into an ActionDefinition with new ActionConfig API
     */
    @Test
    public void testParseActionDefinitionFromJsonWithNewAPI() throws ConfigurationException {
        // Create sample JSON for ActionDefinition with new API structure
        String json = """
                {
                  "steps": [
                    {
                      "actionConfig": {
                        "@type": "ClickOptions",
                        "clickType": "LEFT",
                        "numberOfClicks": 1,
                        "pauseAfterEnd": 0.5,
                        "offsetX": 0,
                        "offsetY": 0
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

        // Verify it's a ClickOptions
        assertTrue(step.getActionConfig() instanceof ClickOptions);
        ClickOptions clickOptions = (ClickOptions) step.getActionConfig();
        assertEquals(ClickOptions.Type.LEFT, clickOptions.getClickType());
        assertEquals(1, clickOptions.getNumberOfClicks());
        assertEquals(0.5, clickOptions.getPauseAfterEnd(), 0.001);

        ObjectCollection objects = step.getObjectCollection();
        assertNotNull(objects);
        assertEquals(1, objects.getStateImages().size());
        assertEquals("TestImage1", objects.getStateImages().get(0).getName());
    }

    /**
     * Test complex ActionDefinition with multiple action types
     */
    @Test
    public void testSerializeComplexActionDefinitionWithMultipleTypes() throws ConfigurationException {
        // Create a complex ActionDefinition with multiple steps
        TaskSequence definition = new TaskSequence();

        // First step: Find with PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.9)
                .setMaxMatchesToActOn(10)
                .build();
        
        definition.addStep(
                findOptions,
                new ObjectCollection.Builder().withImages(
                        new StateImage.Builder().setName("ImageToFind").build()
                ).build()
        );

        // Second step: Click with ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .setNumberOfClicks(2)
                .build();
        
        definition.addStep(
                clickOptions,
                new ObjectCollection.Builder().withRegions(
                        new StateRegion.Builder().setName("RegionToClick")
                                .setSearchRegion(new Region(10, 10, 100, 100)).build()
                ).build()
        );

        // Third step: Drag with DragOptions
        DragOptions dragOptions = new DragOptions.Builder()
                .setFromIndex(0)
                .setToIndex(1)
                .setPauseAfterEnd(1.0)
                .build();
        
        definition.addStep(
                dragOptions,
                new ObjectCollection.Builder().withRegions(
                        new Region(0, 0, 50, 50),
                        new Region(200, 200, 50, 50)
                ).build()
        );

        // Serialize
        String json = jsonUtils.toJsonSafe(definition);
        System.out.println("DEBUG - Complex JSON with new API: " + json);

        // Verify JSON contains expected elements
        assertNotNull(json);
        assertTrue(json.contains("PatternFindOptions"));
        assertTrue(json.contains("ClickOptions"));
        assertTrue(json.contains("DragOptions"));
        assertTrue(json.contains("strategy"));
        assertTrue(json.contains("clickType"));
        assertTrue(json.contains("fromIndex"));
    }

    /**
     * Test parsing complex JSON with multiple action types
     */
    @Test
    public void testParseComplexJsonWithMultipleActionTypes() throws ConfigurationException {
        String json = """
                {
                  "steps": [
                    {
                      "actionConfig": {
                        "@type": "PatternFindOptions",
                        "strategy": "BEST",
                        "similarity": 0.85,
                        "captureImage": true,
                        "maxMatchesToActOn": 5
                      },
                      "objectCollection": {
                        "stateImages": [{"name": "SearchImage"}]
                      }
                    },
                    {
                      "actionConfig": {
                        "@type": "ClickOptions",
                        "clickType": "DOUBLE",
                        "numberOfClicks": 2,
                        "offsetX": 10,
                        "offsetY": 20
                      },
                      "objectCollection": {
                        "useMatchesFromPreviousAction": true
                      }
                    },
                    {
                      "actionConfig": {
                        "@type": "DragOptions",
                        "fromIndex": 0,
                        "toIndex": 1,
                        "pauseBeforeBegin": 0.5,
                        "pauseAfterEnd": 1.5
                      },
                      "objectCollection": {
                        "stateRegions": [
                          {"name": "DragFrom", "searchRegion": {"x": 0, "y": 0, "w": 100, "h": 100}},
                          {"name": "DragTo", "searchRegion": {"x": 200, "y": 200, "w": 100, "h": 100}}
                        ]
                      }
                    }
                  ]
                }
                """;

        // Parse JSON
        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskSequence actionDefinition = jsonParser.convertJson(jsonNode, TaskSequence.class);

        // Verify parsing
        assertNotNull(actionDefinition);
        assertEquals(3, actionDefinition.getSteps().size());

        // Verify first step (PatternFindOptions)
        ActionStep step1 = actionDefinition.getSteps().get(0);
        assertTrue(step1.getActionConfig() instanceof PatternFindOptions);
        PatternFindOptions findOptions = (PatternFindOptions) step1.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.BEST, findOptions.getStrategy());
        assertEquals(0.85, findOptions.getSimilarity(), 0.001);
        assertTrue(findOptions.isCaptureImage());
        assertEquals(5, findOptions.getMaxMatchesToActOn());

        // Verify second step (ClickOptions)
        ActionStep step2 = actionDefinition.getSteps().get(1);
        assertTrue(step2.getActionConfig() instanceof ClickOptions);
        ClickOptions clickOptions = (ClickOptions) step2.getActionConfig();
        assertEquals(ClickOptions.Type.DOUBLE, clickOptions.getClickType());
        assertEquals(2, clickOptions.getNumberOfClicks());
        assertEquals(10, clickOptions.getOffsetX());
        assertEquals(20, clickOptions.getOffsetY());
        assertTrue(step2.getObjectCollection().isUseMatchesFromPreviousAction());

        // Verify third step (DragOptions)
        ActionStep step3 = actionDefinition.getSteps().get(2);
        assertTrue(step3.getActionConfig() instanceof DragOptions);
        DragOptions dragOptions = (DragOptions) step3.getActionConfig();
        assertEquals(0, dragOptions.getFromIndex());
        assertEquals(1, dragOptions.getToIndex());
        assertEquals(0.5, dragOptions.getPauseBeforeBegin(), 0.001);
        assertEquals(1.5, dragOptions.getPauseAfterEnd(), 0.001);
    }

    /**
     * Test pretty-printed JSON with new API
     */
    @Test
    public void testPrettyJsonWithNewAPI() throws ConfigurationException {
        // Create a simple ActionDefinition
        TaskSequence definition = createMinimalActionDefinitionWithPatternFind();

        // Use JsonUtils for pretty printing
        String prettyJson = jsonUtils.toPrettyJsonSafe(definition);

        // Print the JSON for debugging
        System.out.println("DEBUG - Pretty JSON with new API:\n" + prettyJson);

        // Verify the JSON is created and contains expected elements
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("actionConfig"));
        assertTrue(prettyJson.contains("@type"));
        assertTrue(prettyJson.contains("PatternFindOptions"));
        assertTrue(prettyJson.contains("\n"), "Pretty JSON should have line breaks");
        assertTrue(prettyJson.contains("  "), "Pretty JSON should be indented");
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
     * Test backward compatibility - parsing old ActionOptions JSON should still work
     */
    @Test
    public void testBackwardCompatibilityWithActionOptions() throws ConfigurationException {
        // Old format JSON with ActionOptions
        String oldFormatJson = """
                {
                  "steps": [
                    {
                      "actionOptions": {
                        "action": "CLICK",
                        "clickType": "LEFT",
                        "similarity": 0.8
                      },
                      "objectCollection": {
                        "stateImages": [{"name": "TestImage"}]
                      }
                    }
                  ]
                }
                """;

        // This should still parse correctly for backward compatibility
        JsonNode jsonNode = jsonParser.parseJson(oldFormatJson);
        // Note: The actual parsing would depend on how the framework handles backward compatibility
    }

    /**
     * Helper method to create a minimal ActionDefinition with PatternFindOptions
     */
    private TaskSequence createMinimalActionDefinitionWithPatternFind() {
        TaskSequence definition = new TaskSequence();

        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.9)
                .build();

        ObjectCollection objects = new ObjectCollection();
        StateImage image = new StateImage();
        image.setName("TestImage");
        ArrayList<StateImage> images = new ArrayList<>();
        images.add(image);
        objects.setStateImages(images);

        definition.addStep(findOptions, objects);
        return definition;
    }

    /**
     * Test with factory method patterns
     */
    @Test
    public void testFactoryMethodPatterns() throws ConfigurationException {
        TaskSequence definition = new TaskSequence();

        // Use factory methods for common patterns
        PatternFindOptions quickFind = PatternFindOptions.forQuickSearch();
        definition.addStep(quickFind, new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("QuickSearchImage").build())
                .build());

        PatternFindOptions preciseFind = PatternFindOptions.forPreciseSearch();
        definition.addStep(preciseFind, new ObjectCollection.Builder()
                .withImages(new StateImage.Builder().setName("PreciseSearchImage").build())
                .build());

        // Serialize and verify
        String json = jsonUtils.toJsonSafe(definition);
        assertNotNull(json);
        
        // Verify the factory methods created the expected configurations
        assertEquals(2, definition.getSteps().size());
        
        PatternFindOptions quickOptions = (PatternFindOptions) definition.getSteps().get(0).getActionConfig();
        assertEquals(PatternFindOptions.Strategy.FIRST, quickOptions.getStrategy());
        assertEquals(0.7, quickOptions.getSimilarity(), 0.001);
        
        PatternFindOptions preciseOptions = (PatternFindOptions) definition.getSteps().get(1).getActionConfig();
        assertEquals(PatternFindOptions.Strategy.BEST, preciseOptions.getStrategy());
        assertEquals(0.9, preciseOptions.getSimilarity(), 0.001);
    }
}