package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.action.internal.mouse.MouseDownWrapper;
import io.github.jspinak.brobot.model.action.MouseButton;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for MouseDown - presses and holds mouse buttons.
 * Tests mouse button press operations, timing control, and button types.
 */
@DisplayName("MouseDown Tests")
public class MouseDownTest extends BrobotTestBase {
    
    @Mock
    private MouseDownWrapper mockMouseDownWrapper;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ObjectCollection mockObjectCollection;
    
    private MouseDown mouseDown;
    private MouseDownOptions mouseDownOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        mouseDown = new MouseDown(mockMouseDownWrapper);
        mouseDownOptions = new MouseDownOptions.Builder().build();
        
        when(mockActionResult.getActionConfig()).thenReturn(mouseDownOptions);
    }
    
    @Test
    @DisplayName("Should return MOUSE_DOWN action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.MOUSE_DOWN, mouseDown.getActionType());
    }
    
    @Nested
    @DisplayName("Basic Mouse Press Operations")
    class BasicMousePressOperations {
        
        @Test
        @DisplayName("Should press left mouse button by default")
        public void testPressLeftButton() {
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseDownWrapper).press(
                mouseDownOptions.getPauseBeforeMouseDown(),
                mouseDownOptions.getPauseAfterMouseDown(),
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should press right mouse button")
        public void testPressRightButton() {
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseDownWrapper).press(
                options.getPauseBeforeMouseDown(),
                options.getPauseAfterMouseDown(),
                ClickType.Type.RIGHT
            );
        }
        
        @Test
        @DisplayName("Should press middle mouse button")
        public void testPressMiddleButton() {
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(MouseButton.MIDDLE)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseDownWrapper).press(
                options.getPauseBeforeMouseDown(),
                options.getPauseAfterMouseDown(),
                ClickType.Type.MIDDLE
            );
        }
    }
    
    @Nested
    @DisplayName("Timing Control")
    class TimingControl {
        
        @Test
        @DisplayName("Should apply pause before mouse down")
        public void testPauseBeforeMouseDown() {
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setPauseBeforeMouseDown(0.5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseDownWrapper).press(
                0.5,
                options.getPauseAfterMouseDown(),
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should apply pause after mouse down")
        public void testPauseAfterMouseDown() {
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setPauseAfterMouseDown(1.0)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseDownWrapper).press(
                options.getPauseBeforeMouseDown(),
                1.0,
                ClickType.Type.LEFT
            );
        }
        
        @ParameterizedTest
        @CsvSource({
            "0.0, 0.0",
            "0.1, 0.2",
            "0.5, 0.5",
            "1.0, 2.0",
            "0.25, 0.75"
        })
        @DisplayName("Should handle various timing combinations")
        public void testVariousTimings(double pauseBefore, double pauseAfter) {
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setPauseBeforeMouseDown(pauseBefore)
                .setPauseAfterMouseDown(pauseAfter)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseDownWrapper).press(
                pauseBefore,
                pauseAfter,
                ClickType.Type.LEFT
            );
        }
    }
    
    @Nested
    @DisplayName("Button Type Conversion")
    class ButtonTypeConversion {
        
        @ParameterizedTest
        @EnumSource(MouseButton.class)
        @DisplayName("Should handle all mouse button types")
        public void testAllButtonTypes(MouseButton button) {
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(button)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult, mockObjectCollection);
            
            ClickType.Type expectedType;
            switch (button) {
                case RIGHT:
                    expectedType = ClickType.Type.RIGHT;
                    break;
                case MIDDLE:
                    expectedType = ClickType.Type.MIDDLE;
                    break;
                case LEFT:
                default:
                    expectedType = ClickType.Type.LEFT;
                    break;
            }
            
            verify(mockMouseDownWrapper).press(
                anyDouble(),
                anyDouble(),
                eq(expectedType)
            );
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should throw exception for invalid configuration")
        public void testInvalidConfiguration() {
            when(mockActionResult.getActionConfig())
                .thenReturn(new MouseUpOptions.Builder().build());
            
            assertThrows(IllegalArgumentException.class, () ->
                mouseDown.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should throw exception for null configuration")
        public void testNullConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(null);
            
            assertThrows(IllegalArgumentException.class, () ->
                mouseDown.perform(mockActionResult, mockObjectCollection));
        }
    }
    
    @Nested
    @DisplayName("ObjectCollection Handling")
    class ObjectCollectionHandling {
        
        @Test
        @DisplayName("Should perform mouse down regardless of ObjectCollections")
        public void testWithNoObjectCollections() {
            mouseDown.perform(mockActionResult);
            
            verify(mockMouseDownWrapper).press(
                anyDouble(),
                anyDouble(),
                any(ClickType.Type.class)
            );
        }
        
        @Test
        @DisplayName("Should perform mouse down with multiple ObjectCollections")
        public void testWithMultipleObjectCollections() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            mouseDown.perform(mockActionResult, collection1, collection2);
            
            verify(mockMouseDownWrapper).press(
                anyDouble(),
                anyDouble(),
                any(ClickType.Type.class)
            );
            // ObjectCollections are not used in MouseDown
            verifyNoInteractions(collection1, collection2);
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle drag-and-drop initiation")
        public void testDragAndDropInit() {
            // Simulating drag-and-drop start with left button
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(MouseButton.LEFT)
                .setPauseBeforeMouseDown(0.1)
                .setPauseAfterMouseDown(0.2)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult);
            
            verify(mockMouseDownWrapper).press(0.1, 0.2, ClickType.Type.LEFT);
        }
        
        @Test
        @DisplayName("Should handle context menu invocation")
        public void testContextMenuInvocation() {
            // Right-click for context menu
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .setPauseBeforeMouseDown(0.05)
                .setPauseAfterMouseDown(0.5) // Longer pause for menu to appear
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult);
            
            verify(mockMouseDownWrapper).press(0.05, 0.5, ClickType.Type.RIGHT);
        }
        
        @Test
        @DisplayName("Should handle selection rectangle start")
        public void testSelectionRectangleStart() {
            // Left button for selection rectangle
            MouseDownOptions options = new MouseDownOptions.Builder()
                .setButton(MouseButton.LEFT)
                .setPauseBeforeMouseDown(0.0) // No pause needed
                .setPauseAfterMouseDown(0.1)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseDown.perform(mockActionResult);
            
            verify(mockMouseDownWrapper).press(0.0, 0.1, ClickType.Type.LEFT);
        }
    }
    
    @Nested
    @DisplayName("Default Values")
    class DefaultValues {
        
        @Test
        @DisplayName("Should use default timing values")
        public void testDefaultTimingValues() {
            MouseDownOptions defaultOptions = new MouseDownOptions.Builder().build();
            when(mockActionResult.getActionConfig()).thenReturn(defaultOptions);
            
            mouseDown.perform(mockActionResult);
            
            verify(mockMouseDownWrapper).press(
                defaultOptions.getPauseBeforeMouseDown(),
                defaultOptions.getPauseAfterMouseDown(),
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should use default left button")
        public void testDefaultButton() {
            MouseDownOptions defaultOptions = new MouseDownOptions.Builder().build();
            assertEquals(MouseButton.LEFT, defaultOptions.getButton());
            
            when(mockActionResult.getActionConfig()).thenReturn(defaultOptions);
            
            mouseDown.perform(mockActionResult);
            
            verify(mockMouseDownWrapper).press(
                anyDouble(),
                anyDouble(),
                eq(ClickType.Type.LEFT)
            );
        }
    }
}