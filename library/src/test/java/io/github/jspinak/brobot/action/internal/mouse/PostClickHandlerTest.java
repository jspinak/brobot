package io.github.jspinak.brobot.action.internal.mouse;

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
    void moveMouseAfterClick_shouldDoNothingWhenUsingClickOptions() {
        // The new ClickOptions doesn't have setMoveMouseAfterAction
        // PostClickHandler with ActionConfig always returns false
        ClickOptions clickOptions = new ClickOptions.Builder()
                .build();

        boolean result = afterClick.moveMouseAfterClick(clickOptions);

        assertFalse(result);
        verify(moveMouseWrapper, never()).move(any(Location.class));
    }

    @Test
    void moveMouseAfterClick_shouldAlwaysReturnFalseWithActionConfig() {
        // In modern Brobot, mouse movement after click is handled differently
        // PostClickHandler.moveMouseAfterClick always returns false
        ClickOptions clickOptions = new ClickOptions.Builder()
                .build();

        boolean result = afterClick.moveMouseAfterClick(clickOptions);

        assertFalse(result);
        verify(moveMouseWrapper, never()).move(any(Location.class));
    }

    @Test
    void moveMouseAfterClick_withActionConfig_returnsExpectedValue() {
        // This test verifies the behavior with ActionConfig
        // Since PostClickHandler no longer supports mouse movement with ActionConfig,
        // it should always return false
        
        ClickOptions clickOptions = new ClickOptions.Builder()
                .build();

        boolean result = afterClick.moveMouseAfterClick(clickOptions);

        assertFalse(result);
        verify(moveMouseWrapper, never()).move(any(Location.class));
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
