package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskSequence (formerly ActionDefinition) JSON parsing with the new ActionConfig API.
 * 
 * Key points:
 * - TaskSequence contains a list of ActionStep objects
 * - Each ActionStep has actionConfig and objectCollection
 * - ActionConfig uses @type for polymorphic deserialization
 */
@DisplayName("TaskSequence JSON Parser Tests")
class TaskSequenceJsonParserTest extends BrobotTestBase {

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
    @DisplayName("Should parse TaskSequence with multiple ActionSteps from JSON")
    void testTaskSequenceFromJson() throws Exception {
        String json = """
        {
            "steps": [
                {
                    "actionConfig": {
                        "@type": "PatternFindOptions",
                        "similarity": 0.95,
                        "searchDuration": 2.0,
                        "strategy": "FIRST"
                    },
                    "objectCollection": {
                        "stateImages": [
                            {
                                "name": "LoginButton"
                            }
                        ]
                    }
                },
                {
                    "actionConfig": {
                        "@type": "ClickOptions",
                        "similarity": 0.9,
                        "pauseAfterMouseUp": 0.5
                    },
                    "objectCollection": {
                        "stateImages": [
                            {
                                "name": "LoginButton"
                            }
                        ]
                    }
                }
            ]
        }
        """;

        TaskSequence taskSequence = objectMapper.readValue(json, TaskSequence.class);

        assertNotNull(taskSequence);
        assertNotNull(taskSequence.getSteps());
        assertEquals(2, taskSequence.getSteps().size());
        
        // Verify first step (Find)
        ActionStep firstStep = taskSequence.getSteps().get(0);
        assertNotNull(firstStep.getActionConfig());
        assertTrue(firstStep.getActionConfig() instanceof PatternFindOptions);
        PatternFindOptions findOptions = (PatternFindOptions) firstStep.getActionConfig();
        // PatternFindOptions inherits similarity from BaseFindOptions
        assertEquals(2.0, findOptions.getSearchDuration(), 0.01);
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        
        // Verify second step (Click)
        ActionStep secondStep = taskSequence.getSteps().get(1);
        assertNotNull(secondStep.getActionConfig());
        assertTrue(secondStep.getActionConfig() instanceof ClickOptions);
        ClickOptions clickOptions = (ClickOptions) secondStep.getActionConfig();
        // ClickOptions doesn't directly expose similarity and pause properties
    }

    @Test
    @DisplayName("Should serialize TaskSequence to JSON")
    void testTaskSequenceToJson() throws Exception {
        // Create TaskSequence programmatically
        TaskSequence taskSequence = new TaskSequence();
        List<ActionStep> steps = new ArrayList<>();
        
        // Add a find step
        ActionStep findStep = new ActionStep();
        findStep.setActionConfig(new PatternFindOptions.Builder()
            .setSearchDuration(3.0)
            .setStrategy(PatternFindOptions.Strategy.ALL)
            .build());
        ObjectCollection findObjects = new ObjectCollection();
        StateImage findImage = new StateImage.Builder()
            .setName("SearchTarget")
            .build();
        findObjects.getStateImages().add(findImage);
        findStep.setObjectCollection(findObjects);
        steps.add(findStep);
        
        // Add a click step
        ActionStep clickStep = new ActionStep();
        clickStep.setActionConfig(new ClickOptions.Builder()
            .setNumberOfClicks(1)
            .build());
        ObjectCollection clickObjects = new ObjectCollection();
        StateImage clickImage = new StateImage.Builder()
            .setName("ClickTarget")
            .build();
        clickObjects.getStateImages().add(clickImage);
        clickStep.setObjectCollection(clickObjects);
        steps.add(clickStep);
        
        taskSequence.setSteps(steps);
        
        String json = objectMapper.writeValueAsString(taskSequence);
        
        assertNotNull(json);
        assertTrue(json.contains("\"@type\":") && json.contains("\"PatternFindOptions\""));
        assertTrue(json.contains("\"@type\":") && json.contains("\"ClickOptions\""));
        assertTrue(json.contains("\"searchDuration\":3.0"));
        assertTrue(json.contains("\"numberOfClicks\":1"));
    }

    @Test
    @DisplayName("Should handle empty TaskSequence")
    void testEmptyTaskSequence() throws Exception {
        String json = """
        {
            "steps": []
        }
        """;

        TaskSequence taskSequence = objectMapper.readValue(json, TaskSequence.class);

        assertNotNull(taskSequence);
        assertNotNull(taskSequence.getSteps());
        assertTrue(taskSequence.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Should handle TaskSequence with single step")
    void testTaskSequenceWithSingleStep() throws Exception {
        String json = """
        {
            "steps": [
                {
                    "actionConfig": {
                        "@type": "HighlightOptions",
                        "duration": 2.0,
                        "color": "RED"
                    },
                    "objectCollection": {
                        "stateRegions": [
                            {
                                "name": "HighlightArea"
                            }
                        ]
                    }
                }
            ]
        }
        """;

        TaskSequence taskSequence = objectMapper.readValue(json, TaskSequence.class);

        assertNotNull(taskSequence);
        assertEquals(1, taskSequence.getSteps().size());
        
        ActionStep step = taskSequence.getSteps().get(0);
        assertEquals("HighlightOptions", step.getActionConfig().getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should add steps programmatically")
    void testAddStepsProgrammatically() {
        TaskSequence taskSequence = new TaskSequence();
        
        ActionStep step1 = new ActionStep();
        step1.setActionConfig(new ClickOptions.Builder().build());
        taskSequence.addStep(step1);
        
        ActionStep step2 = new ActionStep();
        step2.setActionConfig(new PatternFindOptions.Builder().build());
        taskSequence.addStep(step2);
        
        assertEquals(2, taskSequence.getSteps().size());
        assertTrue(taskSequence.getSteps().get(0).getActionConfig() instanceof ClickOptions);
        assertTrue(taskSequence.getSteps().get(1).getActionConfig() instanceof PatternFindOptions);
    }
}