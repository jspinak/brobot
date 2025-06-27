package io.github.jspinak.brobot.action.actionExecution;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.FindStrategyRegistry;
import io.github.jspinak.brobot.action.internal.execution.BasicActionRegistry;
import io.github.jspinak.brobot.action.internal.execution.CompositeActionRegistry;
import io.github.jspinak.brobot.action.internal.service.ActionService;

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

class ActionServiceTest {

    @InjectMocks
    private ActionService actionService;

    @Mock
    private BasicActionRegistry basicAction;

    @Mock
    private CompositeActionRegistry compositeAction;

    @Mock
    private FindStrategyRegistry findFunctions;

    @Mock
    private ActionInterface actionInterface;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAction_shouldReturnCompositeFindActionWhenMultipleFindActions() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .addFind(ActionOptions.Find.FIRST)
                .addFind(ActionOptions.Find.ALL)
                .build();
        when(compositeAction.getAction(ActionOptions.Action.FIND)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(actionOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(compositeAction).getAction(ActionOptions.Action.FIND);
        verify(basicAction, never()).getAction(any());
    }

    @Test
    void getAction_shouldReturnBasicFindActionWhenSingleFindAction() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        when(basicAction.getAction(ActionOptions.Action.FIND)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(actionOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.FIND);
        verify(compositeAction, never()).getAction(any());
    }

    @Test
    void getAction_shouldReturnBasicActionWhenAvailable() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        when(basicAction.getAction(ActionOptions.Action.CLICK)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(actionOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.CLICK);
        verify(compositeAction, never()).getAction(any());
    }

    @Test
    void getAction_shouldReturnCompositeActionWhenBasicActionNotAvailable() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .build();
        when(basicAction.getAction(ActionOptions.Action.DRAG)).thenReturn(Optional.empty());
        when(compositeAction.getAction(ActionOptions.Action.DRAG)).thenReturn(Optional.of(actionInterface));

        Optional<ActionInterface> result = actionService.getAction(actionOptions);

        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.DRAG);
        verify(compositeAction).getAction(ActionOptions.Action.DRAG);
    }

    @Test
    void getAction_shouldReturnEmptyOptionalWhenNoActionFound() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLASSIFY)
                .build();
        when(basicAction.getAction(ActionOptions.Action.CLASSIFY)).thenReturn(Optional.empty());
        when(compositeAction.getAction(ActionOptions.Action.CLASSIFY)).thenReturn(Optional.empty());

        Optional<ActionInterface> result = actionService.getAction(actionOptions);

        assertFalse(result.isPresent());
        verify(basicAction).getAction(ActionOptions.Action.CLASSIFY);
        verify(compositeAction).getAction(ActionOptions.Action.CLASSIFY);
    }

    @Test
    void setCustomFind_shouldCallFindFunctions() {
        actionService.setCustomFind((matches, objectCollections) -> {
        });
        verify(findFunctions).addCustomFind(any());
    }
}