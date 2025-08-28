package io.github.jspinak.brobot.runner.dsl;

import io.github.jspinak.brobot.BrobotTestApplication;
import io.github.jspinak.brobot.test.TestEnvironmentInitializer;
import io.github.jspinak.brobot.test.mock.MockGuiAccessConfig;
import io.github.jspinak.brobot.test.mock.MockGuiAccessMonitor;
import io.github.jspinak.brobot.test.mock.MockScreenConfig;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.highlight.HighlightOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TaskSequence (formerly ActionDefinition) JSON parsing with the new ActionConfig API.
 * Uses Spring Boot's configured ObjectMapper with all necessary Jackson modules.
 * 
 * Key points:
 * - TaskSequence contains a list of ActionStep objects
 * - Each ActionStep has actionConfig and objectCollection
 * - ActionConfig uses @type for polymorphic deserialization
 */
@DisplayName("TaskSequence JSON Parser Tests")
@SpringBootTest(classes = BrobotTestApplication.class,
    properties = {
        "spring.main.lazy-initialization=true",
        "brobot.mock.enabled=true",
        "brobot.illustration.disabled=true",
        "brobot.scene.analysis.disabled=true",
        "brobot.gui-access.continue-on-error=true",
        "brobot.gui-access.check-on-startup=false",
        "java.awt.headless=true",
        "spring.main.allow-bean-definition-overriding=true",
        "brobot.test.type=unit",
        "brobot.capture.physical-resolution=false"
    })
@Import({MockGuiAccessConfig.class, MockGuiAccessMonitor.class, MockScreenConfig.class,
         io.github.jspinak.brobot.test.config.TestApplicationConfiguration.class})
@ContextConfiguration(initializers = TestEnvironmentInitializer.class)
class TaskSequenceJsonParserTest {

    @Autowired
    private ConfigurationParser jsonParser;

    @Test
    @DisplayName("Should parse TaskSequence with multiple ActionSteps from JSON")
    void testTaskSequenceFromJson() throws ConfigurationException {
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
                        "numberOfClicks": 1
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

        TaskSequence taskSequence = jsonParser.convertJson(json, TaskSequence.class);

        assertNotNull(taskSequence);
        assertNotNull(taskSequence.getSteps());
        assertEquals(2, taskSequence.getSteps().size());
        
        // Verify first step (Find)
        ActionStep firstStep = taskSequence.getSteps().get(0);
        assertNotNull(firstStep.getActionConfig());
        assertTrue(firstStep.getActionConfig() instanceof PatternFindOptions);
        PatternFindOptions findOptions = (PatternFindOptions) firstStep.getActionConfig();
        assertEquals(0.95, findOptions.getSimilarity(), 0.01);
        assertEquals(2.0, findOptions.getSearchDuration(), 0.01);
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        
        // Verify second step (Click)
        ActionStep secondStep = taskSequence.getSteps().get(1);
        assertNotNull(secondStep.getActionConfig());
        assertTrue(secondStep.getActionConfig() instanceof ClickOptions);
        ClickOptions clickOptions = (ClickOptions) secondStep.getActionConfig();
        assertEquals(1, clickOptions.getNumberOfClicks());
    }

    @Test
    @DisplayName("Should serialize TaskSequence to JSON")
    void testTaskSequenceToJson() throws ConfigurationException {
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
        
        String json = jsonParser.toJson(taskSequence);
        
        assertNotNull(json);
        assertTrue(json.contains("\"@type\" : \"PatternFindOptions\""));
        assertTrue(json.contains("\"@type\" : \"ClickOptions\""));
        assertTrue(json.contains("\"searchDuration\" : 3.0"));
        assertTrue(json.contains("\"numberOfClicks\" : 1"));
    }

    @Test
    @DisplayName("Should handle empty TaskSequence")
    void testEmptyTaskSequence() throws ConfigurationException {
        String json = """
        {
            "steps": []
        }
        """;

        TaskSequence taskSequence = jsonParser.convertJson(json, TaskSequence.class);

        assertNotNull(taskSequence);
        assertNotNull(taskSequence.getSteps());
        assertTrue(taskSequence.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Should handle TaskSequence with single step")
    void testTaskSequenceWithSingleStep() throws ConfigurationException {
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

        TaskSequence taskSequence = jsonParser.convertJson(json, TaskSequence.class);

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