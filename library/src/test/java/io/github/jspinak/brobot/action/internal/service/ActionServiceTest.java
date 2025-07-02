package io.github.jspinak.brobot.action.internal.service;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.find.color.ColorFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.composite.drag.DragOptions;
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
    
    // Tests using new ActionConfig API
    
    @Test
    void getAction_withClickOptions_shouldReturnClickAction() {
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        when(basicAction.getAction(ActionOptions.Action.CLICK)).thenReturn(Optional.of(actionInterface));
        
        Optional<ActionInterface> result = actionService.getAction(clickOptions);
        
        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.CLICK);
    }
    
    @Test
    void getAction_withPatternFindOptions_shouldReturnFindAction() {
        PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
        when(basicAction.getAction(ActionOptions.Action.FIND)).thenReturn(Optional.of(actionInterface));
        
        Optional<ActionInterface> result = actionService.getAction(findOptions);
        
        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.FIND);
    }
    
    @Test
    void getAction_withColorFindOptions_shouldReturnFindAction() {
        ColorFindOptions colorFindOptions = new ColorFindOptions.Builder().build();
        when(basicAction.getAction(ActionOptions.Action.FIND)).thenReturn(Optional.of(actionInterface));
        
        Optional<ActionInterface> result = actionService.getAction(colorFindOptions);
        
        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.FIND);
    }
    
    @Test
    void getAction_withTypeOptions_shouldReturnTypeAction() {
        TypeOptions typeOptions = new TypeOptions.Builder()
                .setTypeDelay(0.1)
                .build();
        when(basicAction.getAction(ActionOptions.Action.TYPE)).thenReturn(Optional.of(actionInterface));
        
        Optional<ActionInterface> result = actionService.getAction(typeOptions);
        
        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.TYPE);
    }
    
    @Test
    void getAction_withDragOptions_shouldReturnDragAction() {
        DragOptions dragOptions = new DragOptions.Builder().build();
        // First check basic registry
        when(basicAction.getAction(ActionOptions.Action.DRAG)).thenReturn(Optional.empty());
        // Then check composite registry
        when(compositeAction.getAction(ActionOptions.Action.DRAG)).thenReturn(Optional.of(actionInterface));
        
        Optional<ActionInterface> result = actionService.getAction(dragOptions);
        
        assertTrue(result.isPresent());
        assertEquals(actionInterface, result.get());
        verify(basicAction).getAction(ActionOptions.Action.DRAG);
        verify(compositeAction).getAction(ActionOptions.Action.DRAG);
    }
    
    @Test
    void getAction_withUnknownActionConfig_shouldReturnEmpty() {
        // Create a mock ActionConfig with an unrecognized class name
        ActionConfig unknownConfig = mock(ActionConfig.class);
        when(unknownConfig.getClass()).thenReturn((Class) UnknownOptions.class);
        
        Optional<ActionInterface> result = actionService.getAction(unknownConfig);
        
        assertFalse(result.isPresent());
    }
    
    // Mock class for testing unknown ActionConfig types
    static class UnknownOptions extends ActionConfig {
        UnknownOptions(Builder builder) {
            super(builder);
        }
        
        static class Builder extends ActionConfig.Builder<Builder> {
            @Override
            public UnknownOptions build() {
                return new UnknownOptions(this);
            }
            
            @Override
            protected Builder self() {
                return this;
            }
        }
    }
}