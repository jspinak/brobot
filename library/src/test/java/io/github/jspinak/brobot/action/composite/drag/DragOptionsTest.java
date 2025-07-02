package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.basic.mouse.MousePressOptions;
import io.github.jspinak.brobot.model.action.MouseButton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DragOptionsTest {

    @Test
    void testBuilder_WithDefaultValues() {
        // Act
        DragOptions options = new DragOptions.Builder().build();

        // Assert
        assertNotNull(options.getMousePressOptions());
        assertEquals(MouseButton.LEFT, options.getMousePressOptions().getButton());
        assertEquals(0.5, options.getDelayBetweenMouseDownAndMove());
        assertEquals(0.5, options.getDelayAfterDrag());
        assertEquals(0.0, options.getPauseBeforeBegin());
        assertEquals(0.5, options.getPauseAfterEnd());
    }

    @Test
    void testBuilder_WithCustomMousePressOptions() {
        // Arrange
        MousePressOptions mousePressOptions = new MousePressOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .setPauseAfterMouseDown(1.0)
                .setPauseAfterMouseUp(0.8)
                .build();

        // Act
        DragOptions options = new DragOptions.Builder()
                .setMousePressOptions(mousePressOptions)
                .build();

        // Assert
        assertEquals(mousePressOptions, options.getMousePressOptions());
        assertEquals(MouseButton.RIGHT, options.getMousePressOptions().getButton());
        assertEquals(1.0, options.getMousePressOptions().getPauseAfterMouseDown());
        assertEquals(0.8, options.getMousePressOptions().getPauseAfterMouseUp());
    }

    @Test
    void testBuilder_WithCustomDelays() {
        // Act
        DragOptions options = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(1.5)
                .setDelayAfterDrag(2.0)
                .setPauseBeforeBegin(0.3)
                .setPauseAfterEnd(0.7)
                .build();

        // Assert
        assertEquals(1.5, options.getDelayBetweenMouseDownAndMove());
        assertEquals(2.0, options.getDelayAfterDrag());
        assertEquals(0.3, options.getPauseBeforeBegin());
        assertEquals(0.7, options.getPauseAfterEnd());
    }

    @Test
    void testBuilder_WithMiddleMouseButton() {
        // Arrange
        MousePressOptions middleButtonOptions = new MousePressOptions.Builder()
                .setButton(MouseButton.MIDDLE)
                .build();

        // Act
        DragOptions options = new DragOptions.Builder()
                .setMousePressOptions(middleButtonOptions)
                .build();

        // Assert
        assertEquals(MouseButton.MIDDLE, options.getMousePressOptions().getButton());
    }

    @Test
    void testBuilder_WithZeroDelays() {
        // Act
        DragOptions options = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(0.0)
                .setDelayAfterDrag(0.0)
                .setPauseBeforeBegin(0.0)
                .setPauseAfterEnd(0.0)
                .build();

        // Assert
        assertEquals(0.0, options.getDelayBetweenMouseDownAndMove());
        assertEquals(0.0, options.getDelayAfterDrag());
        assertEquals(0.0, options.getPauseBeforeBegin());
        assertEquals(0.0, options.getPauseAfterEnd());
    }

    @Test
    void testBuilder_ChainedConfiguration() {
        // Act
        DragOptions options = new DragOptions.Builder()
                .setMousePressOptions(new MousePressOptions.Builder()
                        .setButton(MouseButton.RIGHT)
                        .setPauseAfterMouseDown(0.3)
                        .build())
                .setDelayBetweenMouseDownAndMove(0.7)
                .setDelayAfterDrag(1.3)
                .setPauseBeforeBegin(0.2)
                .setPauseAfterEnd(0.8)
                .build();

        // Assert all values were set correctly
        assertEquals(MouseButton.RIGHT, options.getMousePressOptions().getButton());
        assertEquals(0.3, options.getMousePressOptions().getPauseAfterMouseDown());
        assertEquals(0.7, options.getDelayBetweenMouseDownAndMove());
        assertEquals(1.3, options.getDelayAfterDrag());
        assertEquals(0.2, options.getPauseBeforeBegin());
        assertEquals(0.8, options.getPauseAfterEnd());
    }

    @Test
    void testBuilder_ModifyMousePressOptionsAfterSetting() {
        // Arrange
        MousePressOptions originalPress = new MousePressOptions.Builder()
                .setButton(MouseButton.LEFT)
                .setPauseAfterMouseDown(0.5)
                .build();

        // Act
        DragOptions drag1 = new DragOptions.Builder()
                .setMousePressOptions(originalPress)
                .build();
                
        DragOptions drag2 = new DragOptions.Builder()
                .setMousePressOptions(new MousePressOptions.Builder()
                        .setButton(MouseButton.RIGHT)
                        .setPauseAfterMouseDown(1.0)
                        .build())
                .build();

        // Assert - different configurations
        assertEquals(MouseButton.LEFT, drag1.getMousePressOptions().getButton());
        assertEquals(0.5, drag1.getMousePressOptions().getPauseAfterMouseDown());
        assertEquals(MouseButton.RIGHT, drag2.getMousePressOptions().getButton());
        assertEquals(1.0, drag2.getMousePressOptions().getPauseAfterMouseDown());
    }

    @Test
    void testBuilder_InheritsActionConfigProperties() {
        // Act
        DragOptions options = new DragOptions.Builder()
                .setPauseBeforeBegin(1.0)
                .setPauseAfterEnd(2.0)
                .setDelayBetweenMouseDownAndMove(0.8)
                .build();

        // Assert - tests that base class properties are inherited
        assertEquals(1.0, options.getPauseBeforeBegin());
        assertEquals(2.0, options.getPauseAfterEnd());
        assertEquals(0.8, options.getDelayBetweenMouseDownAndMove());
    }

    @Test
    void testBuilder_FluentInterface() {
        // This test verifies the fluent builder pattern works correctly
        DragOptions options = new DragOptions.Builder()
                .setDelayBetweenMouseDownAndMove(1.0)
                .setPauseBeforeBegin(0.5)  // From ActionConfig
                .setMousePressOptions(new MousePressOptions.Builder().build())
                .setDelayAfterDrag(1.5)
                .setPauseAfterEnd(0.8)  // From ActionConfig
                .build();

        assertNotNull(options);
        assertEquals(1.0, options.getDelayBetweenMouseDownAndMove());
        assertEquals(0.5, options.getPauseBeforeBegin());
        assertEquals(1.5, options.getDelayAfterDrag());
        assertEquals(0.8, options.getPauseAfterEnd());
    }
}