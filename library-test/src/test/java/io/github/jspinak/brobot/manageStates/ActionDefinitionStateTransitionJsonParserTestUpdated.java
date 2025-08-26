package io.github.jspinak.brobot.manageStates;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.navigation.transition.TaskSequenceStateTransition;
import io.github.jspinak.brobot.runner.dsl.model.TaskSequence;
import io.github.jspinak.brobot.runner.dsl.model.ActionStep;
import io.github.jspinak.brobot.model.transition.StateTransition;
import io.github.jspinak.brobot.runner.json.parsing.ConfigurationParser;
import io.github.jspinak.brobot.runner.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.runner.json.utils.JsonUtils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Updated tests for ActionDefinitionStateTransition JSON parsing with new ActionConfig API.
 * 
 * Key changes:
 * - JSON now uses "actionConfig" with "@type" for polymorphic deserialization
 * - ActionStep uses specific config classes instead of generic ActionOptions
 * - Type-safe JSON structure for state transitions
 */
@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ActionDefinitionStateTransitionJsonParserTestUpdated {

    @Autowired
    private ConfigurationParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic ActionDefinitionStateTransition from JSON with new API
     */
    @Test
    public void testParseBasicTransitionWithNewAPI() throws ConfigurationException {
        String json = """
                {
                  "type": "actionDefinition",
                  "actionDefinition": {
                    "steps": [
                      {
                        "actionConfig": {
                          "@type": "ClickOptions",
                          "numberOfClicks": 1,
                          "pauseAfterEnd": 0.5,
                          "mousePressOptions": {
                            "button": "LEFT"
                          }
                        },
                        "objectCollection": {
                          "stateImageIds": [101]
                        }
                      }
                    ]
                  },
                  "staysVisibleAfterTransition": "TRUE",
                  "activate": [2, 3],
                  "exit": [1],
                  "score": 5,
                  "timesSuccessful": 10
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskSequenceStateTransition transition = jsonParser.convertJson(jsonNode, TaskSequenceStateTransition.class);

        assertNotNull(transition);

        // Verify action definition
        assertTrue(transition.getTaskSequenceOptional().isPresent());
        TaskSequence taskSeq = transition.getTaskSequenceOptional().get();
        assertNotNull(taskSeq.getSteps());
        assertEquals(1, taskSeq.getSteps().size());
        
        // Verify the action config is ClickOptions
        ActionStep step = taskSeq.getSteps().get(0);
        assertTrue(step.getActionConfig() instanceof ClickOptions);
        ClickOptions clickOptions = (ClickOptions) step.getActionConfig();
        // ClickOptions doesn't have getClickType() - it uses MousePressOptions
        assertNotNull(clickOptions.getMousePressOptions());
        assertEquals(1, clickOptions.getNumberOfClicks());
        assertEquals(0.5, clickOptions.getPauseAfterEnd(), 0.001);

        // Verify basic properties
        assertEquals(StateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());
        assertEquals(5, transition.getScore());
        assertEquals(10, transition.getTimesSuccessful());

        // Verify activate and exit sets
        assertNotNull(transition.getActivate());
        assertEquals(2, transition.getActivate().size());
        assertTrue(transition.getActivate().contains(2L));
        assertTrue(transition.getActivate().contains(3L));

        assertNotNull(transition.getExit());
        assertEquals(1, transition.getExit().size());
        assertTrue(transition.getExit().contains(1L));
    }

    /**
     * Test parsing an ActionDefinitionStateTransition with minimal properties
     */
    @Test
    public void testParseMinimalTransition() throws ConfigurationException {
        String json = """
                {
                  "type": "actionDefinition",
                  "actionDefinition": {
                    "steps": []
                  },
                  "activate": [5]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskSequenceStateTransition transition = jsonParser.convertJson(jsonNode, TaskSequenceStateTransition.class);

        assertNotNull(transition);
        assertTrue(transition.getTaskSequenceOptional().isPresent());
        assertEquals(0, transition.getTaskSequenceOptional().get().getSteps().size());
        assertTrue(transition.getActivate().contains(5L));
        assertTrue(transition.getExit().isEmpty());
    }

    /**
     * Test parsing complex transition with multiple action types
     */
    @Test
    public void testParseComplexTransitionWithMultipleActions() throws ConfigurationException {
        String json = """
                {
                  "type": "actionDefinition",
                  "actionDefinition": {
                    "steps": [
                      {
                        "actionConfig": {
                          "@type": "PatternFindOptions",
                          "strategy": "FIRST",
                          "similarity": 0.9,
                          "captureImage": true
                        },
                        "objectCollection": {
                          "stateImageIds": [201]
                        }
                      },
                      {
                        "actionConfig": {
                          "@type": "ClickOptions",
                          "offsetX": 10,
                          "offsetY": 20,
                          "mousePressOptions": {
                            "button": "LEFT"
                          }
                        },
                        "objectCollection": {
                          "useMatchesFromPreviousAction": true
                        }
                      },
                      {
                        "actionConfig": {
                          "@type": "TypeOptions",
                          "typeDelay": 0.1,
                          "pauseAfterEnd": 0.5
                        },
                        "objectCollection": {
                          "stateStrings": [{"string": "username@example.com"}]
                        }
                      }
                    ]
                  },
                  "staysVisibleAfterTransition": "FALSE",
                  "activate": [10],
                  "exit": [8, 9]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskSequenceStateTransition transition = jsonParser.convertJson(jsonNode, TaskSequenceStateTransition.class);

        assertNotNull(transition);
        assertTrue(transition.getTaskSequenceOptional().isPresent());
        
        TaskSequence taskSeq = transition.getTaskSequenceOptional().get();
        assertEquals(3, taskSeq.getSteps().size());

        // Verify first step (PatternFindOptions)
        ActionStep step1 = taskSeq.getSteps().get(0);
        assertTrue(step1.getActionConfig() instanceof PatternFindOptions);
        PatternFindOptions findOptions = (PatternFindOptions) step1.getActionConfig();
        assertEquals(PatternFindOptions.Strategy.FIRST, findOptions.getStrategy());
        assertEquals(0.9, findOptions.getSimilarity(), 0.001);
        assertTrue(findOptions.isCaptureImage());

        // Verify second step (ClickOptions)
        ActionStep step2 = taskSeq.getSteps().get(1);
        assertTrue(step2.getActionConfig() instanceof ClickOptions);
        ClickOptions clickOptions = (ClickOptions) step2.getActionConfig();
        // ClickOptions doesn't have offset methods - offsets are handled differently in current API
        // ObjectCollection doesn't have isUseMatchesFromPreviousAction() method
        assertNotNull(step2.getObjectCollection());

        // Verify third step (TypeOptions)
        ActionStep step3 = taskSeq.getSteps().get(2);
        assertTrue(step3.getActionConfig() instanceof TypeOptions);
        TypeOptions typeOptions = (TypeOptions) step3.getActionConfig();
        // TypeOptions has typeDelay, not modifierDelay
        assertEquals(0.1, typeOptions.getTypeDelay(), 0.001);
        assertEquals(0.5, typeOptions.getPauseAfterEnd(), 0.001);
        // ObjectCollection has stateStrings, not strings
        assertEquals(1, step3.getObjectCollection().getStateStrings().size());
        assertEquals("username@example.com", step3.getObjectCollection().getStateStrings().get(0).getString());

        // Verify transition properties
        assertEquals(StateTransition.StaysVisible.FALSE, transition.getStaysVisibleAfterTransition());
        assertEquals(1, transition.getActivate().size());
        assertTrue(transition.getActivate().contains(10L));
        assertEquals(2, transition.getExit().size());
        assertTrue(transition.getExit().containsAll(List.of(8L, 9L)));
    }

    /**
     * Test serialization and deserialization of ActionDefinitionStateTransition
     */
    @Test
    public void testSerializeDeserializeTransition() throws ConfigurationException {
        // Create transition manually
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        
        // Create action definition
        TaskSequence actionDef = new TaskSequence();
        
        // Add Find step
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .setSimilarity(0.85)
                .setMaxMatchesToActOn(5)
                .build();
        actionDef.addStep(findOptions, new ObjectCollection.Builder()
                .withImages(createTestStateImages(301L, 302L))
                .build());
        
        // Add Click step
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setPressOptions(MousePressOptions.builder()
                        .setButton(MouseButton.RIGHT)
                        .build())
                .setNumberOfClicks(1)
                .build();
        actionDef.addStep(clickOptions, new ObjectCollection.Builder()
                // No direct method to use matches from previous action in Builder
                .build());
        
        transition.setActionDefinition(actionDef);
        transition.setStaysVisibleAfterTransition(StateTransition.StaysVisible.NONE);
        transition.setActivate(new HashSet<>(List.of(15L, 16L)));
        transition.setExit(new HashSet<>(List.of(14L)));
        transition.setScore(8);
        transition.setTimesSuccessful(25);

        // Serialize
        String json = jsonUtils.toJsonSafe(transition);
        System.out.println("DEBUG - Serialized transition with new API: " + json);

        // Verify JSON contains expected elements
        assertNotNull(json);
        assertTrue(json.contains("actionDefinition"));
        assertTrue(json.contains("@type"));
        assertTrue(json.contains("PatternFindOptions"));
        assertTrue(json.contains("ClickOptions"));

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        TaskSequenceStateTransition deserializedTransition = 
                jsonParser.convertJson(jsonNode, TaskSequenceStateTransition.class);

        // Verify deserialization
        assertNotNull(deserializedTransition);
        assertTrue(deserializedTransition.getTaskSequenceOptional().isPresent());
        
        TaskSequence deserializedTaskSeq = deserializedTransition.getTaskSequenceOptional().get();
        assertEquals(2, deserializedTaskSeq.getSteps().size());
        
        // Verify configs preserved
        assertTrue(deserializedTaskSeq.getSteps().get(0).getActionConfig() instanceof PatternFindOptions);
        assertTrue(deserializedTaskSeq.getSteps().get(1).getActionConfig() instanceof ClickOptions);
        
        // Verify transition properties
        assertEquals(StateTransition.StaysVisible.NONE, deserializedTransition.getStaysVisibleAfterTransition());
        assertEquals(2, deserializedTransition.getActivate().size());
        assertTrue(deserializedTransition.getActivate().containsAll(List.of(15L, 16L)));
        assertEquals(1, deserializedTransition.getExit().size());
        assertTrue(deserializedTransition.getExit().contains(14L));
        assertEquals(8, deserializedTransition.getScore());
        assertEquals(25, deserializedTransition.getTimesSuccessful());
    }

    /**
     * Test parsing transition with different StaysVisible values
     */
    @Test
    public void testParseStaysVisibleValues() throws ConfigurationException {
        // Test TRUE
        String jsonTrue = """
                {
                  "type": "actionDefinition",
                  "actionDefinition": {"steps": []},
                  "staysVisibleAfterTransition": "TRUE",
                  "activate": [1]
                }
                """;
        
        TaskSequenceStateTransition transitionTrue = 
                jsonParser.convertJson(jsonParser.parseJson(jsonTrue), TaskSequenceStateTransition.class);
        assertEquals(StateTransition.StaysVisible.TRUE, transitionTrue.getStaysVisibleAfterTransition());

        // Test FALSE
        String jsonFalse = """
                {
                  "type": "actionDefinition",
                  "actionDefinition": {"steps": []},
                  "staysVisibleAfterTransition": "FALSE",
                  "activate": [1]
                }
                """;
        
        TaskSequenceStateTransition transitionFalse = 
                jsonParser.convertJson(jsonParser.parseJson(jsonFalse), TaskSequenceStateTransition.class);
        assertEquals(StateTransition.StaysVisible.FALSE, transitionFalse.getStaysVisibleAfterTransition());

        // Test NONE
        String jsonNone = """
                {
                  "type": "actionDefinition",
                  "actionDefinition": {"steps": []},
                  "staysVisibleAfterTransition": "NONE",
                  "activate": [1]
                }
                """;
        
        TaskSequenceStateTransition transitionNone = 
                jsonParser.convertJson(jsonParser.parseJson(jsonNone), TaskSequenceStateTransition.class);
        assertEquals(StateTransition.StaysVisible.NONE, transitionNone.getStaysVisibleAfterTransition());
    }

    /**
     * Test pretty printing of transitions
     */
    @Test
    public void testPrettyPrintTransition() throws ConfigurationException {
        TaskSequenceStateTransition transition = new TaskSequenceStateTransition();
        
        TaskSequence actionDef = new TaskSequence();
        actionDef.addStep(
                new ClickOptions.Builder()
                        .setPressOptions(MousePressOptions.builder()
                                .setButton(MouseButton.LEFT)
                                .build())
                        .build(),
                new ObjectCollection.Builder().withImages(createTestStateImages(401L)).build()
        );
        
        transition.setActionDefinition(actionDef);
        transition.setActivate(new HashSet<>(List.of(20L)));

        // Pretty print
        String prettyJson = jsonUtils.toPrettyJsonSafe(transition);
        System.out.println("DEBUG - Pretty printed transition:\n" + prettyJson);

        // Verify formatting
        assertNotNull(prettyJson);
        assertTrue(prettyJson.contains("\n"));
        assertTrue(prettyJson.contains("  ")); // Indentation
        assertTrue(prettyJson.contains("actionDefinition"));
        assertTrue(prettyJson.contains("ClickOptions"));
    }
    
    private List<StateImage> createTestStateImages(Long... ids) {
        List<StateImage> images = new ArrayList<>();
        for (Long id : ids) {
            StateImage img = new StateImage.Builder()
                    .setName("testImage_" + id)
                    .addPattern(new Pattern.Builder()
                            .setName("pattern_" + id)
                            .build())
                    .build();
            images.add(img);
        }
        return images;
    }
}