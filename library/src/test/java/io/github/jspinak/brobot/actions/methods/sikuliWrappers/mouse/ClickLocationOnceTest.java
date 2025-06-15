package io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Region;

import static org.mockito.Mockito.*;

class ClickLocationOnceTest {

    @InjectMocks
    private ClickLocationOnce clickLocationOnce;

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
        originalMockState = BrobotSettings.mock;
        // Most tests assume a real click, so we set mock to false.
        BrobotSettings.mock = false;

        // General setup for mocks
        when(location.sikuli()).thenReturn(new org.sikuli.script.Location(0, 0));
        when(moveMouseWrapper.move(location)).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        // Restore the original state after each test
        BrobotSettings.mock = originalMockState;
    }

    @Test
    void click_shouldJustPrintWhenMocked() {
        BrobotSettings.mock = true;
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();

        clickLocationOnce.click(location, actionOptions);

        // Verify no actual mouse actions were called
        verify(moveMouseWrapper, never()).move(any());
        verify(mouseDownWrapper, never()).press(anyDouble(), anyDouble(), any());
        verify(mouseUpWrapper, never()).press(anyDouble(), anyDouble(), any());
    }

    @Test
    void click_shouldPerformSingleLeftClick() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickType(ClickType.Type.LEFT)
                .build();

        clickLocationOnce.click(location, actionOptions);

        verify(moveMouseWrapper).move(location);
        verify(mouseDownWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
        verify(mouseUpWrapper).press(anyDouble(), anyDouble(), eq(ClickType.Type.LEFT));
    }

    @Test
    void click_shouldPerformDoubleClickWithPausesAsTwoSeparateClicks() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setClickType(ClickType.Type.DOUBLE_LEFT)
                .setPauseAfterMouseDown(0.1) // Adding a pause forces two separate clicks
                .build();

        when(location.sikuli()).thenReturn(mock(org.sikuli.script.Location.class));

        clickLocationOnce.click(location, actionOptions);

        verify(mouseDownWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
        verify(mouseUpWrapper, times(2)).press(anyDouble(), anyDouble(), eq(ClickType.Type.DOUBLE_LEFT));
    }

    @Test
    void click_shouldPerformDoubleClickWithNoPauses() {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setClickType(ClickType.Type.DOUBLE_LEFT)
                .build();

        org.sikuli.script.Location sikuliLocation = mock(org.sikuli.script.Location.class);
        when(location.sikuli()).thenReturn(sikuliLocation);

        clickLocationOnce.click(location, actionOptions);

        // In this case, Sikuli's doubleClick() is called directly
        verify(sikuliLocation).doubleClick();
        verify(mouseDownWrapper, never()).press(anyDouble(), anyDouble(), any());
        verify(mouseUpWrapper, never()).press(anyDouble(), anyDouble(), any());
    }

    @Test
    void click_shouldFailIfMoveFails() {
        when(moveMouseWrapper.move(location)).thenReturn(false);
        ActionOptions actionOptions = new ActionOptions.Builder().build();

        clickLocationOnce.click(location, actionOptions);

        verify(moveMouseWrapper).move(location);
        verify(mouseDownWrapper, never()).press(anyDouble(), anyDouble(), any());
    }
}
