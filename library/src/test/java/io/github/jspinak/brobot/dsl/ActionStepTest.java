package io.github.jspinak.brobot.dsl;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionStepTest {

    @Test
    void testConstructorsAndData() {
        // Test NoArgsConstructor and setters
        ActionStep emptyStep = new ActionStep();
        ActionOptions options = new ActionOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        emptyStep.setActionOptions(options);
        emptyStep.setObjectCollection(collection);
        assertSame(options, emptyStep.getActionOptions());
        assertSame(collection, emptyStep.getObjectCollection());

        // Test AllArgsConstructor and getters
        ActionStep fullStep = new ActionStep(options, collection);
        assertSame(options, fullStep.getActionOptions());
        assertSame(collection, fullStep.getObjectCollection());
    }

    @Test
    void testToStringHandlesNullCollection() {
        // The custom toString method should not throw an error if the object collection is null
        ActionOptions options = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        ActionStep step = new ActionStep(options, null);

        String stepString = step.toString();

        assertTrue(stepString.contains("Action: CLICK"), "toString should include the action type.");
        assertTrue(stepString.contains("StateImages: []"), "toString should handle a null ObjectCollection gracefully.");
    }
}