package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.model.element.Location;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Region;

import static org.mockito.Mockito.*;

/**
 * Test class for SingleClickExecutor.
 * 
 * This test demonstrates usage of both ActionOptions (for backward compatibility)
 * and the new ClickOptions API. SingleClickExecutor now supports both APIs.
 */
class SingleClickExecutorTest {

    @InjectMocks
    private SingleClickExecutor singleClickExecutor;

    @Mock
    private MouseDownWrapper mouseDownWrapper;
    @Mock
    private MouseUpWrapper mouseUpWrapper;
    @Mock
    private MoveMouseWrapper moveMouseWrapper;
    @Mock
    private Location location;
    @Mock
    private Region sikuliRegion;

    // To store the original value of BrobotSettings.mock
    private boolean originalMockState;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Store the original state of the static mock setting
        originalMockState = FrameworkSettings.mock;
        // Most tests assume a real click, so we set mock to false.
        FrameworkSettings.mock = false;

        // General setup for mocks
        when(location.sikuli()).thenReturn(new org.sikuli.script.Location(0, 0));
        when(moveMouseWrapper.move(location)).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        // Restore the original state after each test
        FrameworkSettings.mock = originalMockState;
    }

    @Test
    void click_shouldJustPrintWhenMocked() {
        FrameworkSettings.mock = true;
        // Using ActionOptions until SingleClickExecutor is updated
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        // Future: Use new ClickOptions.Builder().build()

        singleClickExecutor.click(location, actionOptions);

        // Verify no actual mouse actions were called
        verify(moveMouseWrapper, never()).move(any());
        verify(mouseDownWrapper, never()).press(anyDouble(), anyDouble(), any());
        verify(mouseUpWrapper, never()).press(anyDouble(), anyDouble(), any());
    }

    @Test
    void click_shouldPerformSingleLeftClick() {
        // Using ActionOptions for backward compatibility
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        // Compare with: new ClickOptions.Builder().build() (default is LEFT)

        singleClickExecutor.click(location, actionOptions);

        verify(moveMouseWrapper).move(location);
        verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
    }

    @Test
    void click_shouldPerformDoubleClickWithPausesAsTwoSeparateClicks() {
        // Using ActionOptions for backward compatibility
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .setPauseAfterMouseDown(0.1) // Adding a pause forces two separate clicks
                .build();
        // Compare with: new ClickOptions.Builder()
        //     .setNumberOfClicks(2)
        //     .setPressOptions(new MousePressOptions.Builder()
        //         .setPauseAfterMouseDown(0.1))
        //     .build()

        when(location.sikuli()).thenReturn(mock(org.sikuli.script.Location.class));

        singleClickExecutor.click(location, actionOptions);

        verify(mouseDownWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
        verify(mouseUpWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
    }

    @Test
    void click_shouldPerformDoubleClickWithNoPauses() {
        // Using ActionOptions for backward compatibility (double-click)
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setClickType(ClickOptions.Type.DOUBLE_LEFT)
                .build();
        // Compare with: new ClickOptions.Builder().setNumberOfClicks(2).build()

        org.sikuli.script.Location sikuliLocation = mock(org.sikuli.script.Location.class);
        when(location.sikuli()).thenReturn(sikuliLocation);

        singleClickExecutor.click(location, actionOptions);

        // In this case, Sikuli's doubleClick() is called directly
        verify(sikuliLocation).doubleClick();
        verify(mouseDownWrapper, never()).press(anyDouble(), anyDouble(), any());
        verify(mouseUpWrapper, never()).press(anyDouble(), anyDouble(), any());
    }

    @Test
    void click_shouldFailIfMoveFails() {
        when(moveMouseWrapper.move(location)).thenReturn(false);
        // Using ActionOptions for backward compatibility
        ActionOptions actionOptions = new ActionOptions.Builder().build();
        // Compare with: new ClickOptions.Builder().build()

        singleClickExecutor.click(location, actionOptions);

        verify(moveMouseWrapper).move(location);
        verify(mouseDownWrapper, never()).press(anyDouble(), anyDouble(), any());
    }
    
    // Tests using the new ActionConfig API
    
    @Test
    void click_shouldPerformSingleLeftClickWithClickOptions() {
        // Using the new ClickOptions API
        ClickOptions clickOptions = new ClickOptions.Builder()
            .build(); // Default is single left click
        
        singleClickExecutor.click(location, clickOptions);
        
        verify(moveMouseWrapper).move(location);
        verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
    }
    
    @Test
    void click_shouldPerformDoubleClickWithClickOptions() {
        // Using the new ClickOptions API for double-click
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setNumberOfClicks(2)
            .setPressOptions(new MousePressOptions.Builder()
                .setPauseAfterMouseDown(0.1))
            .build();
        
        when(location.sikuli()).thenReturn(mock(org.sikuli.script.Location.class));
        
        singleClickExecutor.click(location, clickOptions);
        
        // Should perform two separate clicks due to custom pause
        verify(mouseDownWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
        verify(mouseUpWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
    }
    
    @Test
    void click_shouldPerformRightClickWithClickOptions() {
        // Using the new ClickOptions API with MousePressOptions
        ClickOptions clickOptions = new ClickOptions.Builder()
            .setPressOptions(new MousePressOptions.Builder()
                .setButton(MouseButton.RIGHT))
            .build();
        
        singleClickExecutor.click(location, clickOptions);
        
        verify(moveMouseWrapper).move(location);
        verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.RIGHT));
        verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.RIGHT));
    }
}
