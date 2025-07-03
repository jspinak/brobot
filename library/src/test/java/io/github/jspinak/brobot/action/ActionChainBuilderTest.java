package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseDownOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseMoveOptions;
import io.github.jspinak.brobot.action.basic.mouse.MouseUpOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.tools.logging.model.LogEventType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ActionChainBuilder class.
 */
class ActionChainBuilderTest {

    @Test
    void testSimpleChain() {
        // Test creating a simple two-action chain
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        ClickOptions clickOptions = new ClickOptions.Builder().build();

        ActionChainOptions chain = ActionChainBuilder
                .of(findOptions)
                .then(clickOptions)
                .build();

        assertNotNull(chain);
        assertEquals(findOptions, chain.getInitialAction());
        assertEquals(1, chain.getChainedActions().size());
        assertEquals(clickOptions, chain.getChainedActions().get(0));
    }

    @Test
    void testChainWithActionTypes() {
        // Test creating a chain using action types
        ActionChainOptions chain = ActionChainBuilder
                .of(ActionInterface.Type.FIND, new PatternFindOptions.Builder().build())
                .then(ActionInterface.Type.CLICK, new ClickOptions.Builder().build())
                .then(ActionInterface.Type.TYPE, new TypeOptions.Builder().build())
                .build();

        assertNotNull(chain);
        assertEquals(2, chain.getChainedActions().size());
    }

    @Test
    void testChainWithStrategy() {
        // Test setting the chaining strategy
        ActionChainOptions chain = ActionChainBuilder
                .of(new PatternFindOptions.Builder().build())
                .then(new ClickOptions.Builder().build())
                .withStrategy(ActionChainOptions.ChainingStrategy.CONFIRM)
                .build();

        assertEquals(ActionChainOptions.ChainingStrategy.CONFIRM, chain.getStrategy());
    }

    @Test
    void testConvenienceStrategyMethods() {
        // Test the nested() and confirm() convenience methods
        ActionChainOptions nestedChain = ActionChainBuilder
                .of(new PatternFindOptions.Builder().build())
                .nested()
                .build();

        assertEquals(ActionChainOptions.ChainingStrategy.NESTED, nestedChain.getStrategy());

        ActionChainOptions confirmChain = ActionChainBuilder
                .of(new PatternFindOptions.Builder().build())
                .confirm()
                .build();

        assertEquals(ActionChainOptions.ChainingStrategy.CONFIRM, confirmChain.getStrategy());
    }

    @Test
    void testChainWithCommonSettings() {
        // Test setting common ActionConfig settings
        ActionChainOptions chain = ActionChainBuilder
                .of(new PatternFindOptions.Builder().build())
                .pauseBeforeBegin(1.5)
                .pauseAfterEnd(0.5)
                .illustrate(ActionConfig.Illustrate.YES)
                .logEventType(LogEventType.ACTION)
                .build();

        assertEquals(1.5, chain.getPauseBeforeBegin());
        assertEquals(0.5, chain.getPauseAfterEnd());
        assertEquals(ActionConfig.Illustrate.YES, chain.getIllustrate());
        assertEquals(LogEventType.ACTION, chain.getLogType());
    }

    @Test
    void testComplexDragChain() {
        // Test creating a complex drag-like chain
        PatternFindOptions findSource = new PatternFindOptions.Builder()
                .setPauseAfterEnd(0.2)
                .build();
        PatternFindOptions findTarget = new PatternFindOptions.Builder()
                .setPauseAfterEnd(0.2)
                .build();
        MouseMoveOptions moveToSource = new MouseMoveOptions.Builder().build();
        MouseDownOptions mouseDown = new MouseDownOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();
        MouseMoveOptions moveToTarget = new MouseMoveOptions.Builder().build();
        MouseUpOptions mouseUp = new MouseUpOptions.Builder()
                .setPauseAfterEnd(0.5)
                .build();

        ActionChainOptions dragChain = ActionChainBuilder
                .of(findSource)
                .then(findTarget)
                .then(moveToSource)
                .then(mouseDown)
                .then(moveToTarget)
                .then(mouseUp)
                .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .build();

        assertNotNull(dragChain);
        assertEquals(5, dragChain.getChainedActions().size());
        assertEquals(ActionChainOptions.ChainingStrategy.NESTED, dragChain.getStrategy());
    }

    @Test
    void testThenAll() {
        // Test adding multiple actions at once
        ClickOptions click1 = new ClickOptions.Builder().build();
        ClickOptions click2 = new ClickOptions.Builder().build();
        ClickOptions click3 = new ClickOptions.Builder().build();

        ActionChainOptions chain = ActionChainBuilder
                .of(new PatternFindOptions.Builder().build())
                .thenAll(click1, click2, click3)
                .build();

        assertEquals(3, chain.getChainedActions().size());
        assertEquals(click1, chain.getChainedActions().get(0));
        assertEquals(click2, chain.getChainedActions().get(1));
        assertEquals(click3, chain.getChainedActions().get(2));
    }

    @Test
    void testSimpleStaticMethod() {
        // Test the simple() static factory method
        PatternFindOptions first = new PatternFindOptions.Builder().build();
        ClickOptions second = new ClickOptions.Builder().build();

        ActionChainOptions chain = ActionChainBuilder.simple(first, second);

        assertNotNull(chain);
        assertEquals(first, chain.getInitialAction());
        assertEquals(1, chain.getChainedActions().size());
        assertEquals(second, chain.getChainedActions().get(0));
    }

    @Test
    void testFromListStaticMethod() {
        // Test the fromList() static factory method
        List<ActionConfig> actions = Arrays.asList(
                new PatternFindOptions.Builder().build(),
                new ClickOptions.Builder().build(),
                new TypeOptions.Builder().build()
        );

        ActionChainOptions chain = ActionChainBuilder.fromList(actions);

        assertNotNull(chain);
        assertEquals(actions.get(0), chain.getInitialAction());
        assertEquals(2, chain.getChainedActions().size());
        assertEquals(actions.get(1), chain.getChainedActions().get(0));
        assertEquals(actions.get(2), chain.getChainedActions().get(1));
    }

    @Test
    void testFromListWithEmptyList() {
        // Test fromList() with empty list throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            ActionChainBuilder.fromList(Arrays.asList());
        });

        assertThrows(IllegalArgumentException.class, () -> {
            ActionChainBuilder.fromList(null);
        });
    }

    @Test
    void testBuildWithNullInitialAction() {
        // Test that building without an initial action throws exception
        assertThrows(IllegalStateException.class, () -> {
            ActionChainBuilder.of((ActionConfig) null).build();
        });
    }

    @Test
    void testFluentChaining() {
        // Test that all builder methods return the builder for chaining
        ActionChainBuilder builder = ActionChainBuilder.of(new PatternFindOptions.Builder().build());
        
        ActionChainBuilder result = builder
                .then(new ClickOptions.Builder().build())
                .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .pauseBeforeBegin(1.0)
                .pauseAfterEnd(0.5)
                .illustrate(ActionConfig.Illustrate.NO)
                .logEventType(LogEventType.ACTION)
                .nested()
                .confirm();

        assertSame(builder, result);
    }

    @Test
    void testReadabilityExample() {
        // Test showing the improved readability of the fluent builder
        // This would replace verbose manual chain construction
        ActionChainOptions chain = ActionChainBuilder
                .of(ActionInterface.Type.FIND, new PatternFindOptions.Builder().build())
                .then(ActionInterface.Type.MOVE, new MouseMoveOptions.Builder().build())
                .then(ActionInterface.Type.CLICK, new ClickOptions.Builder()
                        .setNumberOfClicks(2)
                        .build())
                .withStrategy(ActionChainOptions.ChainingStrategy.NESTED)
                .pauseAfterEnd(1.0)
                .build();

        assertNotNull(chain);
        assertEquals(2, chain.getChainedActions().size());
        assertEquals(1.0, chain.getPauseAfterEnd());
        
        // Verify the click options were preserved
        ActionConfig lastAction = chain.getChainedActions().get(1);
        assertInstanceOf(ClickOptions.class, lastAction);
        assertEquals(2, ((ClickOptions) lastAction).getNumberOfClicks());
    }
}