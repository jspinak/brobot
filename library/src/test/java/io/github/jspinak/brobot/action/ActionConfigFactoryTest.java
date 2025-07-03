package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ActionConfigFactory class.
 */
class ActionConfigFactoryTest {

    private ActionConfigFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ActionConfigFactory();
    }

    @Test
    void testCreateWithDefaultSettings() {
        // Test creating various action configs with defaults
        ActionConfig clickConfig = factory.create(ActionInterface.Type.CLICK);
        assertNotNull(clickConfig);
        assertInstanceOf(ClickOptions.class, clickConfig);

        ActionConfig findConfig = factory.create(ActionInterface.Type.FIND);
        assertNotNull(findConfig);
        assertInstanceOf(PatternFindOptions.class, findConfig);

        ActionConfig dragConfig = factory.create(ActionInterface.Type.DRAG);
        assertNotNull(dragConfig);
        assertInstanceOf(DragOptions.class, dragConfig);
    }

    @Test
    void testCreateWithOverrides() {
        // Test creating a ClickOptions with overrides
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("numberOfClicks", 2);
        overrides.put("pauseBeforeBegin", 1.5);
        overrides.put("pauseAfterEnd", 0.5);

        ActionConfig config = factory.create(ActionInterface.Type.CLICK, overrides);
        assertInstanceOf(ClickOptions.class, config);
        
        ClickOptions clickOptions = (ClickOptions) config;
        assertEquals(2, clickOptions.getNumberOfClicks());
        assertEquals(1.5, clickOptions.getPauseBeforeBegin());
        assertEquals(0.5, clickOptions.getPauseAfterEnd());
    }

    @Test
    void testCreateWithCommonOverrides() {
        // Test common ActionConfig overrides
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("pauseBeforeBegin", 2.0);
        overrides.put("pauseAfterEnd", 1.0);
        overrides.put("illustrate", ActionConfig.Illustrate.YES);
        overrides.put("logType", LogEventType.ACTION);

        ActionConfig config = factory.create(ActionInterface.Type.MOVE, overrides);
        assertInstanceOf(MouseMoveOptions.class, config);
        
        assertEquals(2.0, config.getPauseBeforeBegin());
        assertEquals(1.0, config.getPauseAfterEnd());
        assertEquals(ActionConfig.Illustrate.YES, config.getIllustrate());
        assertEquals(LogEventType.ACTION, config.getLogType());
    }

    @Test
    void testCreateDragOptionsWithOverrides() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("delayBetweenMouseDownAndMove", 0.3);
        overrides.put("delayAfterDrag", 0.7);

        ActionConfig config = factory.create(ActionInterface.Type.DRAG, overrides);
        assertInstanceOf(DragOptions.class, config);
        
        DragOptions dragOptions = (DragOptions) config;
        assertEquals(0.3, dragOptions.getDelayBetweenMouseDownAndMove());
        assertEquals(0.7, dragOptions.getDelayAfterDrag());
    }

    @Test
    void testCreateTypeOptionsWithOverrides() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("typeDelay", 0.1);
        overrides.put("modifiers", "CTRL+SHIFT");

        ActionConfig config = factory.create(ActionInterface.Type.TYPE, overrides);
        assertInstanceOf(TypeOptions.class, config);
        
        TypeOptions typeOptions = (TypeOptions) config;
        assertEquals(0.1, typeOptions.getTypeDelay());
        assertEquals("CTRL+SHIFT", typeOptions.getModifiers());
    }

    @Test
    void testCreateClassifyOptions() {
        ActionConfig config = factory.create(ActionInterface.Type.CLASSIFY);
        assertInstanceOf(ColorFindOptions.class, config);
        
        ColorFindOptions colorOptions = (ColorFindOptions) config;
        assertEquals(ColorFindOptions.Color.CLASSIFICATION, colorOptions.getColor());
    }

    @Test
    void testCreateWithInvalidActionType() {
        // Test with a null action type
        assertThrows(IllegalArgumentException.class, () -> {
            factory.create(null);
        });
    }

    @Test
    void testGetActionTypeFromConfig() {
        // Create various configs and verify the factory can determine their type
        ActionConfig clickConfig = factory.create(ActionInterface.Type.CLICK);
        assertInstanceOf(ClickOptions.class, clickConfig);

        ActionConfig findConfig = factory.create(ActionInterface.Type.FIND);
        assertInstanceOf(PatternFindOptions.class, findConfig);
    }

    @Test
    void testCreateFromExistingConfig() {
        // Create an initial config with some settings
        Map<String, Object> initialOverrides = new HashMap<>();
        initialOverrides.put("numberOfClicks", 3);
        ClickOptions originalConfig = (ClickOptions) factory.create(ActionInterface.Type.CLICK, initialOverrides);

        // Create a new config from the existing one with additional overrides
        Map<String, Object> newOverrides = new HashMap<>();
        newOverrides.put("pauseAfterEnd", 2.0);
        
        ActionConfig newConfig = factory.createFrom(originalConfig, newOverrides);
        assertInstanceOf(ClickOptions.class, newConfig);
        
        // The new config should have the overridden value
        assertEquals(2.0, newConfig.getPauseAfterEnd());
    }

    @Test
    void testAllActionTypesHaveFactoryMethod() {
        // List of action types that should be supported
        ActionInterface.Type[] supportedTypes = {
            ActionInterface.Type.CLICK,
            ActionInterface.Type.DRAG,
            ActionInterface.Type.FIND,
            ActionInterface.Type.TYPE,
            ActionInterface.Type.MOVE,
            ActionInterface.Type.VANISH,
            ActionInterface.Type.HIGHLIGHT,
            ActionInterface.Type.SCROLL_MOUSE_WHEEL,
            ActionInterface.Type.MOUSE_DOWN,
            ActionInterface.Type.MOUSE_UP,
            ActionInterface.Type.KEY_DOWN,
            ActionInterface.Type.KEY_UP,
            ActionInterface.Type.CLASSIFY,
            ActionInterface.Type.CLICK_UNTIL,
            ActionInterface.Type.DEFINE
        };

        for (ActionInterface.Type actionType : supportedTypes) {
            ActionConfig config = factory.create(actionType);
            assertNotNull(config, "Factory should create config for " + actionType);
            assertInstanceOf(ActionConfig.class, config, 
                "Created config should be an ActionConfig for " + actionType);
        }
    }

    @Test
    void testSuccessCriteriaOverride() {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("successCriteria", (java.util.function.Predicate<ActionResult>) result -> result.isSuccess());

        ActionConfig config = factory.create(ActionInterface.Type.FIND, overrides);
        assertNotNull(config.getSuccessCriteria());
        
        // Create a mock ActionResult to test the predicate
        ActionResult mockResult = new ActionResult();
        mockResult.setSuccess(true);
        assertTrue(config.getSuccessCriteria().test(mockResult));
    }
}