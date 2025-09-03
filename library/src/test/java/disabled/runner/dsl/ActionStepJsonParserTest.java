package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ActionStep JSON parsing with the new ActionConfig API.
 * 
 * Key points:
 * - ActionStep uses 'actionConfig' field instead of 'actionOptions'
 * - ActionConfig uses @type for polymorphic deserialization
 * - Specific config classes like ClickOptions are used
 */
@DisplayName("ActionStep JSON Parser Tests")
class ActionStepJsonParserTest extends BrobotTestBase {

    private ObjectMapper objectMapper;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Register modules to handle OpenCV Mat issues
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        // Ignore Mat-related properties that cause conflicts
        objectMapper.addMixIn(org.bytedeco.opencv.opencv_core.Mat.class, MatIgnoreMixin.class);
    }
    
    // Mixin to ignore conflicting Mat properties
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"to", "t", "deallocator", "pointer"})
    interface MatIgnoreMixin {}

    @Test
    @DisplayName("Should parse ActionStep with ClickOptions from JSON")
    void testActionStepWithClickOptionsFromJson() throws Exception {
        // Note: Cannot deserialize StateImage objects due to Mat conflicts
        // Test with empty ObjectCollection instead
        String json = """
        {
            "actionConfig": {
                "@type": "ClickOptions",
                "numberOfClicks": 2
            },
            "objectCollection": {}
        }
        """;

        ActionStep actionStep = objectMapper.readValue(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNotNull(actionStep.getActionConfig());
        assertTrue(actionStep.getActionConfig() instanceof ClickOptions);
        
        ClickOptions clickOptions = (ClickOptions) actionStep.getActionConfig();
        assertEquals(2, clickOptions.getNumberOfClicks());
        
        assertNotNull(actionStep.getObjectCollection());
        // ObjectCollection should be empty but not null
        assertTrue(actionStep.getObjectCollection().getStateImages().isEmpty());
    }

    @Test
    @DisplayName("Should serialize ActionStep to JSON")
    void testActionStepToJson() throws Exception {
        // Create ActionStep programmatically
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .build();
            
        ActionStep actionStep = new ActionStep();
        actionStep.setActionConfig(clickOptions);
        
        String json = objectMapper.writeValueAsString(actionStep);
        
        assertNotNull(json);
        assertTrue(json.contains("\"@type\":") && json.contains("\"ClickOptions\""));
        assertTrue(json.contains("\"numberOfClicks\":2"));
    }

    @Test
    @DisplayName("Should parse ActionStep with PatternFindOptions from JSON")
    void testActionStepWithFindOptionsFromJson() throws Exception {
        String json = """
        {
            "actionConfig": {
                "@type": "PatternFindOptions",
                "similarity": 0.95,
                "searchDuration": 2.0,
                "strategy": "FIRST"
            },
            "objectCollection": {}
        }
        """;

        ActionStep actionStep = objectMapper.readValue(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNotNull(actionStep.getActionConfig());
        assertTrue(actionStep.getActionConfig() instanceof PatternFindOptions);
        
        PatternFindOptions findOptions = (PatternFindOptions) actionStep.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        assertEquals(0.95, findOptions.getSimilarity(), 0.01);
        assertEquals(2.0, findOptions.getSearchDuration(), 0.01);
        
        assertNotNull(actionStep.getObjectCollection());
    }

    @Test
    @DisplayName("Should handle empty ObjectCollection")
    void testActionStepWithEmptyObjectCollection() throws Exception {
        String json = """
        {
            "actionConfig": {
                "@type": "ClickOptions"
            },
            "objectCollection": {}
        }
        """;

        ActionStep actionStep = objectMapper.readValue(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNotNull(actionStep.getObjectCollection());
        assertTrue(actionStep.getObjectCollection().getStateImages().isEmpty());
        assertTrue(actionStep.getObjectCollection().getStateRegions().isEmpty());
        assertTrue(actionStep.getObjectCollection().getStateLocations().isEmpty());
    }

    @Test
    @DisplayName("Should handle null ActionConfig")
    void testActionStepWithNullActionConfig() throws Exception {
        String json = """
        {
            "objectCollection": {
                "stateImages": []
            }
        }
        """;

        ActionStep actionStep = objectMapper.readValue(json, ActionStep.class);

        assertNotNull(actionStep);
        assertNull(actionStep.getActionConfig());
        assertNotNull(actionStep.getObjectCollection());
    }
}