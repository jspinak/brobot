package io.github.jspinak.brobot.action.internal.service;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategyRegistry;
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for ActionService with the modern ActionConfig API.
 * CompositeActionRegistry has been removed, so tests focus on BasicActionRegistry.
 */
class ActionServiceTest {

    @InjectMocks
    private ActionService actionService;

    @Mock
    private BasicActionRegistry basicAction;

    @Mock
    private FindStrategyRegistry findFunctions;

    @Mock
    private ActionInterface actionInterface;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAction_withPatternFindOptions_shouldReturnFindAction() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
        when(basicAction.getAction(ActionType.FIND)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(findOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionType.FIND);
    }

    @Test
    void getAction_withColorFindOptions_shouldReturnFindAction() {
        ColorFindOptions colorOptions = new ColorFindOptions.Builder()
                .build();
        when(basicAction.getAction(ActionType.FIND)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(colorOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionType.FIND);
    }

    @Test
    void getAction_withClickOptions_shouldReturnClickAction() {
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
        when(basicAction.getAction(ActionType.CLICK)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(clickOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionType.CLICK);
    }

    @Test
    void getAction_withTypeOptions_shouldReturnTypeAction() {
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build();
        when(basicAction.getAction(ActionType.TYPE)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(typeOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionType.TYPE);
    }

    @Test
    void getAction_withDragOptions_shouldReturnDragAction() {
        DragOptions dragOptions = new DragOptions.Builder()
                .build();
        when(basicAction.getAction(ActionType.DRAG)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(dragOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionType.DRAG);
    }

    @Test
    void getAction_whenActionNotFound_shouldReturnEmpty() {
        // Use a mocked ActionConfig that won't be recognized
        ActionConfig unknownConfig = mock(ActionConfig.class);
        when(unknownConfig.getClass()).thenReturn((Class) ActionConfig.class);
        when(unknownConfig.toString()).thenReturn("UnknownConfig");
        
        // ActionService will return empty for unrecognized configs
        Optional<ActionInterface> result = actionService.getAction(unknownConfig);

        assertFalse(result.isPresent());
    }

    @Test
    void setCustomFind_shouldCallFindFunctions() {
        actionService.setCustomFind((matches, objectCollections) -> {
            // Custom find implementation
        });
        
        verify(findFunctions).addCustomFind(any());
    }

    @Test
    void getAction_withPatternFindOptions_differentStrategies() {
        // Test FIRST strategy
        PatternFindOptions firstOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        when(basicAction.getAction(ActionType.FIND)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> firstResult = actionService.getAction(firstOptions);
        assertTrue(firstResult.isPresent());

        // Test BEST strategy
        PatternFindOptions bestOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();

        Optional<ActionInterface> bestResult = actionService.getAction(bestOptions);
        assertTrue(bestResult.isPresent());

        // Test ALL strategy
        PatternFindOptions allOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();

        Optional<ActionInterface> allResult = actionService.getAction(allOptions);
        assertTrue(allResult.isPresent());

        // Verify FIND action was requested for all strategies
        verify(basicAction, times(3)).getAction(ActionType.FIND);
    }

    @Test
    void getAction_withClickOptions_differentClickTypes() {
        // Test single click
        ClickOptions singleClick = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();
        when(basicAction.getAction(ActionType.CLICK)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> singleResult = actionService.getAction(singleClick);
        assertTrue(singleResult.isPresent());

        // Test double click
        ClickOptions doubleClick = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();

        Optional<ActionInterface> doubleResult = actionService.getAction(doubleClick);
        assertTrue(doubleResult.isPresent());

        // Verify CLICK action was requested for both
        verify(basicAction, times(2)).getAction(ActionType.CLICK);
    }
}