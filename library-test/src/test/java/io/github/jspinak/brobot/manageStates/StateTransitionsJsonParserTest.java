package io.github.jspinak.brobot.manageStates;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.dsl.ActionDefinition;
import io.github.jspinak.brobot.dsl.ActionStep;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.json.parsing.JsonParser;
import io.github.jspinak.brobot.json.parsing.exception.ConfigurationException;
import io.github.jspinak.brobot.json.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class StateTransitionsJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic StateTransitions from JSON
     */
    @Test
    public void testParseBasicStateTransitions() throws ConfigurationException {
        String json = """
                {
                  "stateName": "LoginState",
                  "stateId": 1,
                  "staysVisibleAfterTransition": true,
                  "actionDefinitionTransitions": {
                    "2": {
                      "type": "actionDefinition",
                      "actionDefinition": {
                        "steps": [
                          {
                            "actionOptions": {
                              "action": "CLICK"
                            },
                            "objectCollection": {
                              "stateImageIds": [101]
                            }
                          }
                        ]
                      },
                      "activate": [2],
                      "exit": [1]
                    }
                  }
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        StateTransitions stateTransitions = jsonParser.convertJson(jsonNode, StateTransitions.class);

        assertNotNull(stateTransitions);
        assertEquals("LoginState", stateTransitions.getStateName());
        assertEquals(1L, stateTransitions.getStateId());
        assertTrue(stateTransitions.isStaysVisibleAfterTransition());

        // Verify actionDefinitionTransitions
        assertNotNull(stateTransitions.getActionDefinitionTransitions());
        assertEquals(1, stateTransitions.getActionDefinitionTransitions().size());
        assertTrue(stateTransitions.getActionDefinitionTransitions().containsKey(2L));

        ActionDefinitionStateTransition transition = stateTransitions.getActionDefinitionTransitions().get(2L);
        assertNotNull(transition);
        assertTrue(transition.getActivate().contains(2L));
        assertTrue(transition.getExit().contains(1L));
        assertTrue(transition.getActionDefinition().isPresent());
    }

    /**
     * Test parsing StateTransitions with transitionFinish
     */
    @Test
    public void testParseWithTransitionFinish() throws ConfigurationException {
        String json = """
                {
                  "stateName": "MainMenuState",
                  "stateId": 2,
                  "transitionFinish": {
                    "type": "actionDefinition",
                    "actionDefinition": {
                      "steps": [
                        {
                          "actionOptions": {
                            "action": "FIND"
                          },
                          "objectCollection": {
                            "stateImageIds": [201]
                          }
                        }
                      ]
                    }
                  },
                  "actionDefinitionTransitions": {}
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        StateTransitions stateTransitions = jsonParser.convertJson(jsonNode, StateTransitions.class);

        assertNotNull(stateTransitions);
        assertEquals("MainMenuState", stateTransitions.getStateName());
        assertEquals(2L, stateTransitions.getStateId());

        // Verify transitionFinish
        assertNotNull(stateTransitions.getTransitionFinish());
        assertInstanceOf(ActionDefinitionStateTransition.class, stateTransitions.getTransitionFinish());

        ActionDefinitionStateTransition finishTransition =
                (ActionDefinitionStateTransition) stateTransitions.getTransitionFinish();
        assertTrue(finishTransition.getActionDefinition().isPresent());
        assertEquals(ActionOptions.Action.FIND,
                finishTransition.getActionDefinition().get().getSteps().get(0).getActionOptions().getAction());
    }

    @Test
    public void testSerializeDeserializeStateTransitions() throws ConfigurationException {
        // Create StateTransitions manually
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateName("TestState");
        stateTransitions.setStateId(5L);
        stateTransitions.setStaysVisibleAfterTransition(true);

        // Create transition finish
        ActionDefinitionStateTransition finishTransition = new ActionDefinitionStateTransition();
        // Make sure it has the type field set
        finishTransition.setType("actionDefinition");

        ActionDefinition finishActionDef = new ActionDefinition();
        ActionStep finishStep = new ActionStep();
        ActionOptions finishOptions = new ActionOptions();
        finishOptions.setAction(ActionOptions.Action.HIGHLIGHT);
        finishStep.setActionOptions(finishOptions);
        finishStep.setObjectCollection(new ObjectCollection());
        finishActionDef.addStep(finishStep);
        finishTransition.setActionDefinition(finishActionDef);
        stateTransitions.setTransitionFinish(finishTransition);

        // Create actionDefinitionTransitions
        Map<Long, ActionDefinitionStateTransition> actionDefinitionTransitions = new HashMap<>();
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        // Make sure it has the type field set
        transition.setType("actionDefinition");

        ActionDefinition actionDef = new ActionDefinition();
        ActionStep step = new ActionStep();
        ActionOptions options = new ActionOptions();
        options.setAction(ActionOptions.Action.CLICK);
        step.setActionOptions(options);
        step.setObjectCollection(new ObjectCollection());
        actionDef.addStep(step);
        transition.setActionDefinition(actionDef);
        actionDefinitionTransitions.put(6L, transition);
        stateTransitions.setActionDefinitionTransitions(actionDefinitionTransitions);

        // Populate transitions list (for runtime)
        List<IStateTransition> transitions = new ArrayList<>();
        transitions.add(transition);
        stateTransitions.setTransitions(transitions);

        // Serialize
        String json = jsonUtils.toJsonSafe(stateTransitions);
        System.out.println("DEBUG: Serialized StateTransitions: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        StateTransitions deserializedTransitions =
                jsonParser.convertJson(jsonNode, StateTransitions.class);

        // Verify
        assertNotNull(deserializedTransitions);
        assertEquals("TestState", deserializedTransitions.getStateName());
        assertEquals(5L, deserializedTransitions.getStateId());
        assertTrue(deserializedTransitions.isStaysVisibleAfterTransition());

        // Verify transitionFinish
        assertNotNull(deserializedTransitions.getTransitionFinish());
        assertInstanceOf(ActionDefinitionStateTransition.class, deserializedTransitions.getTransitionFinish());
        ActionDefinitionStateTransition deserializedFinish =
                (ActionDefinitionStateTransition) deserializedTransitions.getTransitionFinish();
        assertTrue(deserializedFinish.getActionDefinition().isPresent());
        assertEquals(ActionOptions.Action.HIGHLIGHT,
                deserializedFinish.getActionDefinition().get().getSteps().getFirst().getActionOptions().getAction());

        // Verify actionDefinitionTransitions
        assertNotNull(deserializedTransitions.getActionDefinitionTransitions());
        assertEquals(1, deserializedTransitions.getActionDefinitionTransitions().size());
        assertTrue(deserializedTransitions.getActionDefinitionTransitions().containsKey(6L));

        // Verify transitions list
        assertNotNull(deserializedTransitions.getTransitions());
        assertEquals(1, deserializedTransitions.getTransitions().size());
    }

    /**
     * Test the getTransitionFunctionByActivatedStateId method
     */
    @Test
    public void testGetTransitionFunctionByActivatedStateId() {
        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(1L);

        // Create transition finish
        ActionDefinitionStateTransition finishTransition = new ActionDefinitionStateTransition();
        finishTransition.setType("actionDefinition");
        stateTransitions.setTransitionFinish(finishTransition);

        // Create a transition to state 2
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        transition.setType("actionDefinition");
        transition.getActivate().add(2L);
        stateTransitions.addTransition(transition);

        // Test finding transition finish (to self)
        Optional<IStateTransition> result = stateTransitions.getTransitionFunctionByActivatedStateId(1L);
        assertTrue(result.isPresent());
        assertSame(finishTransition, result.get());

        // Test finding transition to state 2
        result = stateTransitions.getTransitionFunctionByActivatedStateId(2L);
        assertTrue(result.isPresent());
        assertSame(transition, result.get());

        // Test with non-existent state
        result = stateTransitions.getTransitionFunctionByActivatedStateId(3L);
        assertFalse(result.isPresent());

        // Test with null
        result = stateTransitions.getTransitionFunctionByActivatedStateId(null);
        assertFalse(result.isPresent());
    }

    /**
     * Test the stateStaysVisible method
     */
    @Test
    public void testStateStaysVisible() {
        // Create StateTransitions with staysVisibleAfterTransition = true
        StateTransitions stateTransitions = new StateTransitions();
        stateTransitions.setStateId(1L);
        stateTransitions.setStaysVisibleAfterTransition(true);

        // Create transition finish with NONE (falls back to StateTransitions setting)
        ActionDefinitionStateTransition finishTransition = new ActionDefinitionStateTransition();
        finishTransition.setStaysVisibleAfterTransition(IStateTransition.StaysVisible.NONE);
        finishTransition.setType("actionDefinition");
        stateTransitions.setTransitionFinish(finishTransition);

        // Create a transition to state 2 with explicit FALSE
        ActionDefinitionStateTransition transition1 = new ActionDefinitionStateTransition();
        transition1.setType("actionDefinition");
        transition1.getActivate().add(2L);
        transition1.setStaysVisibleAfterTransition(IStateTransition.StaysVisible.FALSE);
        stateTransitions.addTransition(transition1);

        // Create a transition to state 3 with explicit TRUE
        ActionDefinitionStateTransition transition2 = new ActionDefinitionStateTransition();
        transition2.setType("actionDefinition");
        transition2.getActivate().add(3L);
        transition2.setStaysVisibleAfterTransition(IStateTransition.StaysVisible.TRUE);
        stateTransitions.addTransition(transition2);

        // Test transition to self - should use StateTransitions setting (true)
        assertTrue(stateTransitions.stateStaysVisible(1L));

        // Test transition to state 2 - should use transition's explicit FALSE
        assertFalse(stateTransitions.stateStaysVisible(2L));

        // Test transition to state 3 - should use transition's explicit TRUE
        assertTrue(stateTransitions.stateStaysVisible(3L));

        // Test non-existent transition
        assertFalse(stateTransitions.stateStaysVisible(4L));
    }

    /**
     * Test the addTransition methods
     */
    @Test
    public void testAddTransition() {
        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();

        // Test adding ActionDefinitionStateTransition
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        transition.getActivate().add(2L);
        transition.getActivate().add(3L);
        stateTransitions.addTransition(transition);

        assertEquals(2, stateTransitions.getTransitions().size()); // Added twice (once per activate ID)
        assertEquals(2, stateTransitions.getActionDefinitionTransitions().size());
        assertTrue(stateTransitions.getActionDefinitionTransitions().containsKey(2L));
        assertTrue(stateTransitions.getActionDefinitionTransitions().containsKey(3L));

        // Test adding JavaStateTransition
        JavaStateTransition javaTransition = new JavaStateTransition();
        stateTransitions.addTransition(javaTransition);

        assertEquals(3, stateTransitions.getTransitions().size()); // Added once

        // Test adding via BooleanSupplier
        BooleanSupplier supplier = () -> true;
        stateTransitions.addTransition(supplier, "State4", "State5");

        assertEquals(4, stateTransitions.getTransitions().size()); // Added once
        // Note: The string state names won't be resolved to IDs in this test
    }

    /**
     * Test the getActionDefinition method
     */
    @Test
    public void testGetActionDefinition() {
        // Create StateTransitions
        StateTransitions stateTransitions = new StateTransitions();

        // Create ActionDefinitionStateTransition with ActionDefinition
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        ActionDefinition actionDef = new ActionDefinition();
        transition.setActionDefinition(actionDef);
        transition.getActivate().add(2L);
        stateTransitions.addTransition(transition);

        // Test finding action definition for state 2
        Optional<ActionDefinition> result = stateTransitions.getActionDefinition(2L);
        assertTrue(result.isPresent());
        assertSame(actionDef, result.get());

        // Test with non-existent state
        result = stateTransitions.getActionDefinition(3L);
        assertFalse(result.isPresent());

        // Test with JavaStateTransition (should return empty)
        JavaStateTransition javaTransition = new JavaStateTransition();
        javaTransition.getActivate().add(4L);
        stateTransitions.addTransition(javaTransition);

        result = stateTransitions.getActionDefinition(4L);
        assertFalse(result.isPresent());
    }

    /**
     * Test the Builder pattern
     */
    @Test
    public void testBuilder() {
        // Create using builder
        BooleanSupplier finishSupplier = () -> true;
        BooleanSupplier transitionSupplier = () -> false;

        StateTransitions stateTransitions = new StateTransitions.Builder("TestState")
                .addTransitionFinish(finishSupplier)
                .addTransition(transitionSupplier, "State2", "State3")
                .setStaysVisibleAfterTransition(true)
                .build();

        // Verify
        assertEquals("TestState", stateTransitions.getStateName());
        assertTrue(stateTransitions.isStaysVisibleAfterTransition());
        assertNotNull(stateTransitions.getTransitionFinish());
        assertEquals(1, stateTransitions.getTransitions().size());

        // Verify JavaStateTransition was created correctly
        JavaStateTransition transition = (JavaStateTransition) stateTransitions.getTransitions().getFirst();
        assertEquals(2, transition.getActivateNames().size());
        assertTrue(transition.getActivateNames().contains("State2"));
        assertTrue(transition.getActivateNames().contains("State3"));
    }
}