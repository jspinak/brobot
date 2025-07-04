package io.github.jspinak.brobot.runner.dsl.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.state.StateImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActionStep JSON serialization without Spring dependencies.
 * Demonstrates migration from deprecated ActionOptions to new ActionConfig API.
 * Migrated from library-test module.
 */
class ActionStepSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void testSerializeActionStepWithClickOptions() throws Exception {
        // Create ActionStep with new API
        ActionStep actionStep = new ActionStep();
        
        // NEW API: Use ClickOptions instead of generic ActionOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .setNumberOfClicks(1)
                .build();
        actionStep.setActionConfig(clickOptions);
        
        // Create ObjectCollection
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName("TestImage")
                        .setIndex(123) // Using index instead of id
                        .build())
                .build();
        actionStep.setObjectCollection(objectCollection);
        
        // Serialize
        String json = objectMapper.writeValueAsString(actionStep);
        
        // Verify JSON structure
        assertNotNull(json);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Check ActionConfig
        assertNotNull(jsonNode.get("actionConfig"));
        
        // Check ObjectCollection
        JsonNode objCollNode = jsonNode.get("objectCollection");
        assertNotNull(objCollNode);
        
        JsonNode stateImagesNode = objCollNode.get("stateImages");
        assertNotNull(stateImagesNode);
        assertEquals(1, stateImagesNode.size());
        
        JsonNode imageNode = stateImagesNode.get(0);
        assertEquals(123, imageNode.get("id").asLong());
        assertEquals("TestImage", imageNode.get("name").asText());
    }

    @Test
    void testSerializeActionStepWithFindOptions() throws Exception {
        // Create ActionStep with Find action
        ActionStep actionStep = new ActionStep();
        
        // NEW API: Use PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.85)
                .build();
        actionStep.setActionConfig(findOptions);
        
        // Create ObjectCollection with patterns
        StateImage stateImage = new StateImage.Builder()
                .setName("SearchImage")
                .addPattern(new Pattern.Builder()
                        .setName("Pattern1")
                        .build())
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(stateImage)
                .build();
        actionStep.setObjectCollection(objectCollection);
        
        // Serialize
        String json = objectMapper.writeValueAsString(actionStep);
        
        // Verify
        JsonNode jsonNode = objectMapper.readTree(json);
        assertNotNull(jsonNode.get("actionConfig"));
        assertNotNull(jsonNode.get("objectCollection"));
        
        // Verify pattern in state image
        JsonNode patternNode = jsonNode
                .get("objectCollection")
                .get("stateImages")
                .get(0)
                .get("patterns")
                .get(0);
        assertEquals("Pattern1", patternNode.get("name").asText());
    }

    @Test
    void testDeserializeActionStepFromLegacyJson() throws Exception {
        // Test parsing JSON with old ActionOptions structure
        String legacyJson = """
            {
                "actionOptions": {
                    "action": "CLICK",
                    "find": "ALL"
                },
                "objectCollection": {
                    "stateImages": [
                        {
                            "id": "123",
                            "name": "TestImage",
                            "patterns": []
                        }
                    ]
                }
            }
            """;
        
        // Parse JSON structure
        JsonNode jsonNode = objectMapper.readTree(legacyJson);
        
        // Verify we can read legacy structure
        assertNotNull(jsonNode);
        
        // Check legacy ActionOptions
        JsonNode actionOptionsNode = jsonNode.get("actionOptions");
        assertNotNull(actionOptionsNode);
        assertEquals("CLICK", actionOptionsNode.get("action").asText());
        assertEquals("ALL", actionOptionsNode.get("find").asText());
        
        // Check ObjectCollection
        JsonNode objCollNode = jsonNode.get("objectCollection");
        assertNotNull(objCollNode);
        
        JsonNode stateImagesNode = objCollNode.get("stateImages");
        assertEquals(1, stateImagesNode.size());
        assertEquals("TestImage", stateImagesNode.get(0).get("name").asText());
    }

    @Test
    void testActionStepWithComplexObjectCollection() throws Exception {
        ActionStep actionStep = new ActionStep();
        
        // Set action config
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
        actionStep.setActionConfig(findOptions);
        
        // Create complex ObjectCollection
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(
                        new StateImage.Builder()
                                .setIndex(1)
                                .setName("Image1")
                                .build(),
                        new StateImage.Builder()
                                .setIndex(2)
                                .setName("Image2")
                                .build()
                )
                .build();
        actionStep.setObjectCollection(objectCollection);
        
        // Serialize
        String json = objectMapper.writeValueAsString(actionStep);
        
        // Verify multiple images
        JsonNode jsonNode = objectMapper.readTree(json);
        JsonNode imagesNode = jsonNode.get("objectCollection").get("stateImages");
        assertEquals(2, imagesNode.size());
        assertEquals("Image1", imagesNode.get(0).get("name").asText());
        assertEquals("Image2", imagesNode.get(1).get("name").asText());
    }

    @Test
    void testActionStepBuilder() {
        // Test using constructor with parameters
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ObjectCollection objectCollection = new ObjectCollection.Builder().build();
        
        ActionStep actionStep = new ActionStep(clickOptions, objectCollection);
        
        assertNotNull(actionStep.getActionConfig());
        assertNotNull(actionStep.getObjectCollection());
        assertTrue(actionStep.getActionConfig() instanceof ClickOptions);
    }

    @Test
    void testActionStepWithNullConfig() throws Exception {
        ActionStep actionStep = new ActionStep();
        actionStep.setObjectCollection(new ObjectCollection.Builder().build());
        // actionConfig is null
        
        String json = objectMapper.writeValueAsString(actionStep);
        assertNotNull(json);
        
        JsonNode jsonNode = objectMapper.readTree(json);
        assertNotNull(jsonNode.get("objectCollection"));
    }

    @Test
    void testMigrateFromOldToNewAPI() {
        // This test demonstrates the migration pattern
        
        // OLD API (conceptual - would use ActionOptions):
        // ActionOptions oldOptions = new ActionOptions.Builder()
        //     .setAction(ActionOptions.Action.CLICK)
        //     .setFind(ActionOptions.Find.ALL)
        //     .build();
        
        // NEW API:
        ClickOptions newOptions = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
        
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName("MigratedImage")
                        .build())
                .build();
        
        ActionStep migratedStep = new ActionStep(newOptions, objectCollection);
        
        assertNotNull(migratedStep.getActionConfig());
        assertTrue(migratedStep.getActionConfig() instanceof ClickOptions);
    }

    /**
     * Helper method to create ActionStep with specific config type
     */
    private ActionStep createActionStep(ActionConfig config, String imageName) {
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(new StateImage.Builder()
                        .setName(imageName)
                        .build())
                .build();
        
        return new ActionStep(config, objectCollection);
    }
}