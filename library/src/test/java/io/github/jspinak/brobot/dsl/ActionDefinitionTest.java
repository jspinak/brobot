package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActionDefinitionTest {

    @Test
    void testConstructors() {
        // Test empty constructor
        ActionDefinition emptyDef = new ActionDefinition();
        assertNotNull(emptyDef.getSteps());
        assertTrue(emptyDef.getSteps().isEmpty());

        // Test convenience constructor
        ActionOptions options = new ActionOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        ActionDefinition convenienceDef = new ActionDefinition(options, collection);

        assertEquals(1, convenienceDef.getSteps().size());
        assertSame(options, convenienceDef.getSteps().get(0).getActionOptions());
    }

    @Test
    void testAddStepMethods() {
        ActionDefinition definition = new ActionDefinition();

        // Test addStep(ActionOptions, ObjectCollection)
        ActionOptions options1 = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        definition.addStep(options1, new ObjectCollection());
        assertEquals(1, definition.getSteps().size());
        assertEquals(ActionOptions.Action.CLICK, definition.getSteps().get(0).getActionOptions().getAction());

        // Test addStep(ActionStep)
        ActionStep step2 = new ActionStep(new ActionOptions.Builder().build(), new ObjectCollection());
        definition.addStep(step2);
        assertEquals(2, definition.getSteps().size());
        assertSame(step2, definition.getSteps().get(1));
    }
}