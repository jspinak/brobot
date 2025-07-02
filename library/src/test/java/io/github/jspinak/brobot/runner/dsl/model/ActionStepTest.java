package io.github.jspinak.brobot.runner.dsl.model;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActionStepTest {

    @Test
    void testConstructorsAndData() {
        // Test NoArgsConstructor and setters with ActionConfig
        ActionStep emptyStep = new ActionStep();
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        emptyStep.setActionConfig(clickOptions);
        emptyStep.setObjectCollection(collection);
        assertSame(clickOptions, emptyStep.getActionConfig());
        assertSame(collection, emptyStep.getObjectCollection());

        // Test AllArgsConstructor and getters
        ActionStep fullStep = new ActionStep(clickOptions, collection);
        assertSame(clickOptions, fullStep.getActionConfig());
        assertSame(collection, fullStep.getObjectCollection());
    }

    @Test
    void testToStringHandlesNullCollection() {
        // The custom toString method should not throw an error if the object collection is null
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ActionStep step = new ActionStep(clickOptions, null);

        String stepString = step.toString();

        assertTrue(stepString.contains("Action: ClickOptions"), "toString should include the action config type.");
        assertTrue(stepString.contains("StateImages: []"), "toString should handle a null ObjectCollection gracefully.");
    }
    
    @Test
    void testWithDifferentActionConfigs() {
        // Test with different ActionConfig implementations
        ObjectCollection collection = new ObjectCollection.Builder().build();
        
        // Test with PatternFindOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setSimilarity(0.9)
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
        ActionStep findStep = new ActionStep(findOptions, collection);
        assertSame(findOptions, findStep.getActionConfig());
        assertTrue(findStep.toString().contains("Action: PatternFindOptions"));
        
        // Test with ClickOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
        ActionStep clickStep = new ActionStep(clickOptions, collection);
        assertSame(clickOptions, clickStep.getActionConfig());
        assertTrue(clickStep.toString().contains("Action: ClickOptions"));
    }
    
    // Test for backward compatibility - demonstrating old ActionOptions usage
    @Test
    void testLegacyActionOptionsUsage() {
        // Note: ActionStep now uses ActionConfig. This test documents the old API
        // which is no longer supported. In production, use ActionConfig implementations.
        
        // Old API (no longer works):
        // ActionOptions options = new ActionOptions.Builder().build();
        // ActionStep step = new ActionStep(options, collection); // Won't compile
        
        // New API:
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        ObjectCollection collection = new ObjectCollection.Builder().build();
        ActionStep step = new ActionStep(clickOptions, collection);
        
        assertTrue(step.getActionConfig() instanceof ActionConfig);
    }
}