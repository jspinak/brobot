package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.model.element.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PostClickHandler.
 * 
 * This test demonstrates usage of both ActionOptions (for backward compatibility)
 * and the new ActionConfig API. In the new API, post-click mouse movement
 * should be done via action chaining, not through PostClickHandler.
 */
class PostClickHandlerTest {

    @InjectMocks
    private PostClickHandler afterClick;

    @Mock
    private MoveMouseWrapper moveMouseWrapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void moveMouseAfterClick_shouldDoNothingWhenOptionIsFalse() {
        // Note: The new ClickOptions doesn't have setMoveMouseAfterAction
        // This test is testing PostClickHandler which still uses ActionOptions
        // Until PostClickHandler is updated, we need to keep using ActionOptions
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setMoveMouseAfterAction(false)
                .build();

        afterClick.moveMouseAfterClick(actionOptions);

        verify(moveMouseWrapper, never()).move(any(Location.class));
    }

    @Test
    void moveMouseAfterClick_shouldMoveByOffsetWhenDefined() {
        Location offset = mock(Location.class);
        when(offset.defined()).thenReturn(true);

        // Note: The new ClickOptions doesn't have setMoveMouseAfterAction
        // This test is testing PostClickHandler which still uses ActionOptions
        // Until PostClickHandler is updated, we need to keep using ActionOptions
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setMoveMouseAfterAction(true)
                .setMoveMouseAfterActionBy(0,0) // A stub, the mock 'offset' is what's used
                .build();
        // Manually set the mocked location since the builder creates a new one
        actionOptions.setMoveMouseAfterActionBy(offset);

        afterClick.moveMouseAfterClick(actionOptions);

        verify(moveMouseWrapper).move(offset);
    }

    @Test
    void moveMouseAfterClick_shouldMoveToLocationWhenOffsetNotDefined() {
        Location offset = mock(Location.class);
        when(offset.defined()).thenReturn(false);

        Location destination = mock(Location.class);

        // Note: The new ClickOptions doesn't have setMoveMouseAfterAction
        // This test is testing PostClickHandler which still uses ActionOptions
        // Until PostClickHandler is updated, we need to keep using ActionOptions
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setMoveMouseAfterAction(true)
                .build();
        actionOptions.setMoveMouseAfterActionBy(offset); // Set the undefined offset
        actionOptions.setMoveMouseAfterActionTo(destination); // Set the destination

        afterClick.moveMouseAfterClick(actionOptions);

        verify(moveMouseWrapper).move(destination);
        verify(moveMouseWrapper, never()).move(offset);
    }
    
    // Tests using new ActionConfig API
    
    @Test
    void moveMouseAfterClick_withActionConfig_shouldAlwaysReturnFalse() {
        // In the new API, post-click mouse movement is handled via action chaining
        ClickOptions clickOptions = new ClickOptions.Builder().build();
        
        boolean result = afterClick.moveMouseAfterClick(clickOptions);
        
        // Should always return false as movement is done via chaining
        assertFalse(result);
        verify(moveMouseWrapper, never()).move(any(Location.class));
    }
    
    @Test
    void moveMouseAfterClick_withActionConfig_shouldNotMoveRegardlessOfOptions() {
        // Even with complex click options, no movement should occur
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .setPauseAfterEnd(0.5)
                .build();
        
        boolean result = afterClick.moveMouseAfterClick(clickOptions);
        
        assertFalse(result);
        verify(moveMouseWrapper, never()).move(any(Location.class));
    }
    
    @Test
    void moveMouseAfterClick_withAnyActionConfig_shouldNotInvokeMoveWrapper() {
        // Test with a mock ActionConfig to ensure the method works with any config type
        ActionConfig mockConfig = mock(ActionConfig.class);
        
        boolean result = afterClick.moveMouseAfterClick(mockConfig);
        
        assertFalse(result);
        verify(moveMouseWrapper, never()).move(any(Location.class));
        // Should not interact with the config at all
        verifyNoInteractions(mockConfig);
    }
}
