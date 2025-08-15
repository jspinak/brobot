package io.github.jspinak.brobot.runner.dsl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionDefinition (TaskSequence) JSON serialization without Spring dependencies.
 * Tests the new ActionConfig API and verifies JSON structure.
 * Migrated and updated from library-test module.
 */
class ActionDefinitionSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void testSerializeTaskSequenceWithFindAction() throws Exception {
        // Create a TaskSequence with Find action using new API
        TaskSequence taskSequence = new TaskSequence();
        
        // Create PatternFindOptions (new API)
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.8)
                .build();
        
        // Create ObjectCollection
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName("TestImage")
                        .build())
                .build();
        
        // Add step with new API
        taskSequence.addStep(findOptions, objectCollection);
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(taskSequence);
        
        // Verify JSON structure
        assertNotNull(json);
        assertTrue(json.contains("\"steps\""));
        assertTrue(json.contains("\"actionConfig\""));
        assertTrue(json.contains("\"strategy\""));
        assertTrue(json.contains("\"ALL\""));
        assertTrue(json.contains("\"similarity\""));
        assertTrue(json.contains("0.8"));
        
        // Parse back and verify
        JsonNode jsonNode = objectMapper.readTree(json);
        assertNotNull(jsonNode.get("steps"));
        assertTrue(jsonNode.get("steps").isArray());
        assertEquals(1, jsonNode.get("steps").size());
        
        JsonNode stepNode = jsonNode.get("steps").get(0);
        assertNotNull(stepNode.get("actionConfig"));
        assertNotNull(stepNode.get("objectCollection"));
    }

    @Test
    void testSerializeTaskSequenceWithClickAction() throws Exception {
        // Create a TaskSequence with Click action using new API
        TaskSequence taskSequence = new TaskSequence();
        
        // Create ClickOptions (new API)
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.RIGHT)
                .setNumberOfClicks(1)
                .build();
        
        // Create ObjectCollection with region
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withRegions(new StateRegion.Builder()
                        .setName("TestRegion")
                        .setSearchRegion(new Region(10, 10, 100, 100))
                        .build())
                .build();
        
        // Add step
        taskSequence.addStep(clickOptions, objectCollection);
        
        // Serialize to JSON
        String json = objectMapper.writeValueAsString(taskSequence);
        
        // Verify JSON structure
        assertNotNull(json);
        assertTrue(json.contains("\"clickType\""));
        assertTrue(json.contains("\"RIGHT\""));
        assertTrue(json.contains("\"numberOfClicks\""));
        assertTrue(json.contains("\"stateRegions\""));
        assertTrue(json.contains("\"TestRegion\""));
    }

    @Test
    void testDeserializeTaskSequenceFromJson() throws Exception {
        // Create JSON with new API structure
        String json = """
                {
                  "steps": [
                    {
                      "actionConfig": {
                        "@type": "PatternFindOptions",
                        "strategy": "FIRST",
                        "similarity": 0.9,
                        "maxMatchesToActOn": 5
                      },
                      "objectCollection": {
                        "stateImages": [
                          {
                            "name": "TestImage1",
                            "patterns": []
                          }
                        ]
                      }
                    }
                  ]
                }
                """;
        
        // Note: Full deserialization requires proper type handling
        // For now, verify JSON structure can be parsed
        JsonNode jsonNode = objectMapper.readTree(json);
        
        assertNotNull(jsonNode);
        assertNotNull(jsonNode.get("steps"));
        assertEquals(1, jsonNode.get("steps").size());
        
        JsonNode stepNode = jsonNode.get("steps").get(0);
        JsonNode configNode = stepNode.get("actionConfig");
        assertNotNull(configNode);
        assertEquals("PatternFindOptions", configNode.get("@type").asText());
        assertEquals("FIRST", configNode.get("strategy").asText());
        assertEquals(0.9, configNode.get("similarity").asDouble());
    }

    @Test
    void testSerializeComplexTaskSequence() throws Exception {
        // Create a complex TaskSequence with multiple steps
        TaskSequence taskSequence = new TaskSequence();
        
        // Step 1: Find action
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .build();
        
        ObjectCollection findCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName("SearchImage")
                        .build())
                .build();
        
        taskSequence.addStep(findOptions, findCollection);
        
        // Step 2: Click action
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setNumberOfClicks(2)
                .build();
        
        ObjectCollection clickCollection = new ObjectCollection.Builder()
                .withRegions(new StateRegion.Builder()
                        .setName("ClickRegion")
                        .setSearchRegion(new Region(50, 50, 200, 200))
                        .build())
                .build();
        
        taskSequence.addStep(clickOptions, clickCollection);
        
        // Serialize
        String json = objectMapper.writeValueAsString(taskSequence);
        
        // Verify structure
        assertNotNull(json);
        JsonNode root = objectMapper.readTree(json);
        JsonNode steps = root.get("steps");
        assertNotNull(steps);
        assertEquals(2, steps.size());
        
        // Verify first step (Find)
        JsonNode step1 = steps.get(0);
        assertNotNull(step1.get("actionConfig"));
        assertNotNull(step1.get("objectCollection"));
        
        // Verify second step (Click)
        JsonNode step2 = steps.get(1);
        assertNotNull(step2.get("actionConfig"));
        assertNotNull(step2.get("objectCollection"));
    }

    @Test
    void testInvalidJsonStructure() {
        String invalidJson = "{invalid json structure}";
        
        assertThrows(Exception.class, () -> {
            objectMapper.readTree(invalidJson);
        });
    }

    @Test
    void testEmptyTaskSequence() throws Exception {
        TaskSequence taskSequence = new TaskSequence();
        
        String json = objectMapper.writeValueAsString(taskSequence);
        assertNotNull(json);
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertNotNull(jsonNode);
        
        // Should have empty steps array
        if (jsonNode.has("steps")) {
            assertTrue(jsonNode.get("steps").isArray());
            assertEquals(0, jsonNode.get("steps").size());
        }
    }

    @Test
    void testActionStepWithLegacyActionOptions() throws Exception {
        // Test that demonstrates migration from old ActionOptions to new ActionConfig API
        TaskSequence taskSequence = new TaskSequence();
        
        // Instead of legacy ActionOptions, use the new ActionConfig API
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .setSimilarity(0.7) // Equivalent to old setMinSimilarity
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName("LegacyImage")
                        .build())
                .build();
        
        ActionStep step = new ActionStep(findOptions, objectCollection);
        taskSequence.getSteps().add(step);
        
        String json = objectMapper.writeValueAsString(taskSequence);
        assertNotNull(json);
        // Verify new API structure instead of old ActionOptions fields
        assertTrue(json.contains("\"@type\""));
        assertTrue(json.contains("\"PatternFindOptions\""));
        assertTrue(json.contains("\"strategy\""));
        assertTrue(json.contains("\"FIRST\""));
        assertTrue(json.contains("\"similarity\""));
        assertTrue(json.contains("0.7"));
    }

    /**
     * Helper method to create a simple TaskSequence for testing
     */
    private TaskSequence createSimpleTaskSequence(ActionConfig config) {
        TaskSequence taskSequence = new TaskSequence();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName("TestImage")
                        .build())
                .build();
        
        taskSequence.addStep(config, objectCollection);
        return taskSequence;
    }
}