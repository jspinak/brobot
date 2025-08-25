package io.github.jspinak.brobot.action.basic.mouse;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.action.internal.mouse.MouseUpWrapper;
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
 * Comprehensive test suite for MouseUp - releases previously pressed mouse buttons.
 * Tests mouse button release operations, timing control, and button types.
 */
@DisplayName("MouseUp Tests")
public class MouseUpTest extends BrobotTestBase {
    
    @Mock
    private MouseUpWrapper mockMouseUpWrapper;
    
    @Mock
    private ActionResult mockActionResult;
    
    @Mock
    private ObjectCollection mockObjectCollection;
    
    private MouseUp mouseUp;
    private MouseUpOptions mouseUpOptions;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        
        // Create MouseUp with mock wrapper
        mouseUp = new MouseUp(mockMouseUpWrapper);
        mouseUpOptions = new MouseUpOptions.Builder().build();
        
        when(mockActionResult.getActionConfig()).thenReturn(mouseUpOptions);
    }
    
    @Test
    @DisplayName("Should return MOUSE_UP action type")
    public void testGetActionType() {
        assertEquals(ActionInterface.Type.MOUSE_UP, mouseUp.getActionType());
    }
    
    @Nested
    @DisplayName("Basic Mouse Release Operations")
    class BasicMouseReleaseOperations {
        
        @Test
        @DisplayName("Should release left mouse button by default")
        public void testReleaseLeftButton() {
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseUpWrapper).press(
                mouseUpOptions.getPauseBeforeMouseUp(),
                mouseUpOptions.getPauseAfterMouseUp(),
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should release right mouse button")
        public void testReleaseRightButton() {
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseUpWrapper).press(
                options.getPauseBeforeMouseUp(),
                options.getPauseAfterMouseUp(),
                ClickType.Type.RIGHT
            );
        }
        
        @Test
        @DisplayName("Should release middle mouse button")
        public void testReleaseMiddleButton() {
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setButton(MouseButton.MIDDLE)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseUpWrapper).press(
                options.getPauseBeforeMouseUp(),
                options.getPauseAfterMouseUp(),
                ClickType.Type.MIDDLE
            );
        }
    }
    
    @Nested
    @DisplayName("Timing Control")
    class TimingControl {
        
        @Test
        @DisplayName("Should apply pause before mouse up")
        public void testPauseBeforeMouseUp() {
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setPauseBeforeMouseUp(0.5)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseUpWrapper).press(
                0.5,
                options.getPauseAfterMouseUp(),
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should apply pause after mouse up")
        public void testPauseAfterMouseUp() {
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setPauseAfterMouseUp(1.0)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseUpWrapper).press(
                options.getPauseBeforeMouseUp(),
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
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setPauseBeforeMouseUp(pauseBefore)
                .setPauseAfterMouseUp(pauseAfter)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
            verify(mockMouseUpWrapper).press(
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
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setButton(button)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult, mockObjectCollection);
            
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
            
            verify(mockMouseUpWrapper).press(
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
                .thenReturn(new MouseDownOptions.Builder().build());
            
            assertThrows(IllegalArgumentException.class, () ->
                mouseUp.perform(mockActionResult, mockObjectCollection));
        }
        
        @Test
        @DisplayName("Should throw exception for null configuration")
        public void testNullConfiguration() {
            when(mockActionResult.getActionConfig()).thenReturn(null);
            
            assertThrows(IllegalArgumentException.class, () ->
                mouseUp.perform(mockActionResult, mockObjectCollection));
        }
    }
    
    @Nested
    @DisplayName("ObjectCollection Handling")
    class ObjectCollectionHandling {
        
        @Test
        @DisplayName("Should perform mouse up regardless of ObjectCollections")
        public void testWithNoObjectCollections() {
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(
                anyDouble(),
                anyDouble(),
                any(ClickType.Type.class)
            );
        }
        
        @Test
        @DisplayName("Should perform mouse up with multiple ObjectCollections")
        public void testWithMultipleObjectCollections() {
            ObjectCollection collection1 = mock(ObjectCollection.class);
            ObjectCollection collection2 = mock(ObjectCollection.class);
            
            mouseUp.perform(mockActionResult, collection1, collection2);
            
            verify(mockMouseUpWrapper).press(
                anyDouble(),
                anyDouble(),
                any(ClickType.Type.class)
            );
            // ObjectCollections are not used in MouseUp
            verifyNoInteractions(collection1, collection2);
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("Should handle drag-and-drop completion")
        public void testDragAndDropCompletion() {
            // Simulating drag-and-drop end with left button
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setButton(MouseButton.LEFT)
                .setPauseBeforeMouseUp(0.1)
                .setPauseAfterMouseUp(0.3) // Give time for UI to update
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(0.1, 0.3, ClickType.Type.LEFT);
        }
        
        @Test
        @DisplayName("Should handle context menu dismissal")
        public void testContextMenuDismissal() {
            // Right-click release after context menu
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .setPauseBeforeMouseUp(0.05)
                .setPauseAfterMouseUp(0.2)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(0.05, 0.2, ClickType.Type.RIGHT);
        }
        
        @Test
        @DisplayName("Should handle selection rectangle completion")
        public void testSelectionRectangleCompletion() {
            // Left button release for selection rectangle
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setButton(MouseButton.LEFT)
                .setPauseBeforeMouseUp(0.0) // No pause needed
                .setPauseAfterMouseUp(0.1)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(0.0, 0.1, ClickType.Type.LEFT);
        }
    }
    
    @Nested
    @DisplayName("Default Values")
    class DefaultValues {
        
        @Test
        @DisplayName("Should use default timing values")
        public void testDefaultTimingValues() {
            MouseUpOptions defaultOptions = new MouseUpOptions.Builder().build();
            when(mockActionResult.getActionConfig()).thenReturn(defaultOptions);
            
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(
                defaultOptions.getPauseBeforeMouseUp(),
                defaultOptions.getPauseAfterMouseUp(),
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should use default left button")
        public void testDefaultButton() {
            MouseUpOptions defaultOptions = new MouseUpOptions.Builder().build();
            assertEquals(MouseButton.LEFT, defaultOptions.getButton());
            
            when(mockActionResult.getActionConfig()).thenReturn(defaultOptions);
            
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(
                anyDouble(),
                anyDouble(),
                eq(ClickType.Type.LEFT)
            );
        }
    }
    
    @Nested
    @DisplayName("Mouse State Management")
    class MouseStateManagement {
        
        @Test
        @DisplayName("Should complete mouse press-release cycle")
        public void testPressReleaseCycle() {
            // This would be part of a larger integration test
            // but we can verify the release is called correctly
            MouseUpOptions options = new MouseUpOptions.Builder()
                .setPauseBeforeMouseUp(0.1)
                .setPauseAfterMouseUp(0.1)
                .build();
            when(mockActionResult.getActionConfig()).thenReturn(options);
            
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper, times(1)).press(
                0.1,
                0.1,
                ClickType.Type.LEFT
            );
        }
        
        @Test
        @DisplayName("Should handle multiple button releases")
        public void testMultipleButtonReleases() {
            // Test releasing different buttons in sequence
            MouseUpOptions leftOptions = new MouseUpOptions.Builder()
                .setButton(MouseButton.LEFT)
                .build();
            MouseUpOptions rightOptions = new MouseUpOptions.Builder()
                .setButton(MouseButton.RIGHT)
                .build();
            
            when(mockActionResult.getActionConfig()).thenReturn(leftOptions);
            mouseUp.perform(mockActionResult);
            
            when(mockActionResult.getActionConfig()).thenReturn(rightOptions);
            mouseUp.perform(mockActionResult);
            
            verify(mockMouseUpWrapper).press(
                anyDouble(),
                anyDouble(),
                eq(ClickType.Type.LEFT)
            );
            verify(mockMouseUpWrapper).press(
                anyDouble(),
                anyDouble(),
                eq(ClickType.Type.RIGHT)
            );
        }
    }
}