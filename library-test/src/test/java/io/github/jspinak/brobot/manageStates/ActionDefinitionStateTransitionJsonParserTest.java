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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {"java.awt.headless=false"})
public class ActionDefinitionStateTransitionJsonParserTest {

    @Autowired
    private JsonParser jsonParser;

    @Autowired
    private JsonUtils jsonUtils;

    /**
     * Test parsing a basic ActionDefinitionStateTransition from JSON
     */
    @Test
    public void testParseBasicTransition() throws ConfigurationException {
        String json = """
                {
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
                  "staysVisibleAfterTransition": "TRUE",
                  "activate": [2, 3],
                  "exit": [1],
                  "score": 5,
                  "timesSuccessful": 10
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        ActionDefinitionStateTransition transition = jsonParser.convertJson(jsonNode, ActionDefinitionStateTransition.class);

        assertNotNull(transition);

        // Verify action definition
        assertTrue(transition.getActionDefinition().isPresent());
        assertNotNull(transition.getActionDefinition().get().getSteps());
        assertEquals(1, transition.getActionDefinition().get().getSteps().size());

        // Verify basic properties
        assertEquals(IStateTransition.StaysVisible.TRUE, transition.getStaysVisibleAfterTransition());
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
                  "actionDefinition": {
                    "steps": []
                  },
                  "activate": [5]
                }
                """;

        JsonNode jsonNode = jsonParser.parseJson(json);
        ActionDefinitionStateTransition transition = jsonParser.convertJson(jsonNode, ActionDefinitionStateTransition.class);

        assertNotNull(transition);

        // Verify action definition
        assertTrue(transition.getActionDefinition().isPresent());
        assertNotNull(transition.getActionDefinition().get().getSteps());
        assertEquals(0, transition.getActionDefinition().get().getSteps().size());

        // Verify default properties
        assertEquals(IStateTransition.StaysVisible.NONE, transition.getStaysVisibleAfterTransition());
        assertEquals(0, transition.getScore());
        assertEquals(0, transition.getTimesSuccessful());

        // Verify activate and exit sets
        assertNotNull(transition.getActivate());
        assertEquals(1, transition.getActivate().size());
        assertTrue(transition.getActivate().contains(5L));

        assertNotNull(transition.getExit());
        assertEquals(0, transition.getExit().size());
    }

    /**
     * Test serializing and deserializing an ActionDefinitionStateTransition
     */
    @Test
    public void testSerializeDeserializeTransition() throws ConfigurationException {
        // Create transition manually
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();

        // Create action definition
        ActionDefinition actionDef = new ActionDefinition();
        ActionStep step = new ActionStep();
        ActionOptions options = new ActionOptions();
        options.setAction(ActionOptions.Action.FIND);
        step.setActionOptions(options);
        step.setObjectCollection(new ObjectCollection());
        actionDef.addStep(step);

        transition.setActionDefinition(actionDef);
        transition.setStaysVisibleAfterTransition(IStateTransition.StaysVisible.FALSE);
        transition.setScore(3);
        transition.setTimesSuccessful(7);

        // Set activate and exit sets
        Set<Long> activate = new HashSet<>(Arrays.asList(10L, 20L));
        Set<Long> exit = new HashSet<>(List.of(5L));
        transition.setActivate(activate);
        transition.setExit(exit);

        // Serialize
        String json = jsonUtils.toJsonSafe(transition);
        System.out.println("DEBUG: Serialized transition: " + json);

        // Deserialize
        JsonNode jsonNode = jsonParser.parseJson(json);
        ActionDefinitionStateTransition deserializedTransition =
                jsonParser.convertJson(jsonNode, ActionDefinitionStateTransition.class);

        // Verify
        assertNotNull(deserializedTransition);

        // Verify action definition
        assertTrue(deserializedTransition.getActionDefinition().isPresent());
        assertNotNull(deserializedTransition.getActionDefinition().get().getSteps());
        assertEquals(1, deserializedTransition.getActionDefinition().get().getSteps().size());

        // Verify properties
        assertEquals(IStateTransition.StaysVisible.FALSE, deserializedTransition.getStaysVisibleAfterTransition());
        assertEquals(3, deserializedTransition.getScore());
        assertEquals(7, deserializedTransition.getTimesSuccessful());

        // Verify activate and exit sets
        assertNotNull(deserializedTransition.getActivate());
        assertEquals(2, deserializedTransition.getActivate().size());
        assertTrue(deserializedTransition.getActivate().contains(10L));
        assertTrue(deserializedTransition.getActivate().contains(20L));

        assertNotNull(deserializedTransition.getExit());
        assertEquals(1, deserializedTransition.getExit().size());
        assertTrue(deserializedTransition.getExit().contains(5L));
    }

    /**
     * Test the getActionDefinition method behavior
     */
    @Test
    public void testGetActionDefinition() {
        // Create transition with actionDefinition
        ActionDefinitionStateTransition transition = new ActionDefinitionStateTransition();
        ActionDefinition actionDef = new ActionDefinition();
        transition.setActionDefinition(actionDef);

        // Test getActionDefinition returns correct Optional
        Optional<ActionDefinition> result = transition.getActionDefinition();
        assertTrue(result.isPresent());
        assertSame(actionDef, result.get());

        // Test with null actionDefinition
        transition.setActionDefinition(null);
        result = transition.getActionDefinition();
        assertFalse(result.isPresent());
    }
}