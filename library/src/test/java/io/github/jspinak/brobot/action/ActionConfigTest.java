package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ActionConfig base class.
 * Tests the common configuration options inherited by all action configurations.
 */
class ActionConfigTest {
    
    /**
     * Test implementation of ActionConfig for testing purposes.
     */
    static class TestActionConfig extends ActionConfig {
        private TestActionConfig(Builder builder) {
            super(builder);
        }
        
        static class Builder extends ActionConfig.Builder<Builder> {
            @Override
            protected Builder self() {
                return this;
            }
            
            public TestActionConfig build() {
                return new TestActionConfig(this);
            }
        }
    }
    
    @Test
    void builder_shouldSetDefaultValues() {
        TestActionConfig config = new TestActionConfig.Builder().build();
        
        assertEquals(0.0, config.getPauseBeforeBegin());
        assertEquals(0.0, config.getPauseAfterEnd());
        assertEquals(ActionConfig.Illustrate.USE_GLOBAL, config.getIllustrate());
        assertNotNull(config.getSubsequentActions());
        assertTrue(config.getSubsequentActions().isEmpty());
        assertNull(config.getSuccessCriteria());
        assertEquals(LogEventType.ACTION, config.getLogType());
    }
    
    @Test
    void builder_shouldSetPauseBeforeBegin() {
        double pauseValue = 1.5;
        TestActionConfig config = new TestActionConfig.Builder()
            .setPauseBeforeBegin(pauseValue)
            .build();
        
        assertEquals(pauseValue, config.getPauseBeforeBegin());
    }
    
    @Test
    void builder_shouldSetPauseAfterEnd() {
        double pauseValue = 2.0;
        TestActionConfig config = new TestActionConfig.Builder()
            .setPauseAfterEnd(pauseValue)
            .build();
        
        assertEquals(pauseValue, config.getPauseAfterEnd());
    }
    
    @Test
    void builder_shouldSetIllustrate() {
        TestActionConfig config = new TestActionConfig.Builder()
            .setIllustrate(ActionConfig.Illustrate.YES)
            .build();
        
        assertEquals(ActionConfig.Illustrate.YES, config.getIllustrate());
    }
    
    @Test
    void builder_shouldSetSuccessCriteria() {
        TestActionConfig config = new TestActionConfig.Builder()
            .setSuccessCriteria(result -> result.isSuccess())
            .build();
        
        assertNotNull(config.getSuccessCriteria());
        
        // Test the predicate
        ActionResult successResult = new ActionResult();
        successResult.setSuccess(true);
        assertTrue(config.getSuccessCriteria().test(successResult));
        
        ActionResult failureResult = new ActionResult();
        failureResult.setSuccess(false);
        assertFalse(config.getSuccessCriteria().test(failureResult));
    }
    
    @Test
    void builder_shouldSetLogType() {
        TestActionConfig config = new TestActionConfig.Builder()
            .setLogType(LogEventType.ERROR)
            .build();
        
        assertEquals(LogEventType.ERROR, config.getLogType());
    }
    
    @Test
    void builder_shouldChainActions() {
        TestActionConfig subsequentAction = new TestActionConfig.Builder()
            .setPauseBeforeBegin(1.0)
            .build();
            
        TestActionConfig config = new TestActionConfig.Builder()
            .then(subsequentAction)
            .build();
        
        assertEquals(1, config.getSubsequentActions().size());
        assertEquals(subsequentAction, config.getSubsequentActions().get(0));
    }
    
    @Test
    void builder_shouldChainMultipleActions() {
        TestActionConfig action1 = new TestActionConfig.Builder().build();
        TestActionConfig action2 = new TestActionConfig.Builder().build();
        TestActionConfig action3 = new TestActionConfig.Builder().build();
            
        TestActionConfig config = new TestActionConfig.Builder()
            .then(action1)
            .then(action2)
            .then(action3)
            .build();
        
        assertEquals(3, config.getSubsequentActions().size());
        assertEquals(action1, config.getSubsequentActions().get(0));
        assertEquals(action2, config.getSubsequentActions().get(1));
        assertEquals(action3, config.getSubsequentActions().get(2));
    }
    
    @Test
    void illustrateEnum_shouldHaveCorrectValues() {
        ActionConfig.Illustrate[] values = ActionConfig.Illustrate.values();
        assertEquals(3, values.length);
        
        assertNotNull(ActionConfig.Illustrate.valueOf("YES"));
        assertNotNull(ActionConfig.Illustrate.valueOf("NO"));
        assertNotNull(ActionConfig.Illustrate.valueOf("USE_GLOBAL"));
    }
}