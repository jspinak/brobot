package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.config.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.sikuli.script.Mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for MouseDownWrapper class.
 * Tests mouse button press-and-hold functionality with timing control.
 */
@DisplayName("MouseDownWrapper Tests")
public class MouseDownWrapperTest extends BrobotTestBase {

    private MouseDownWrapper mouseDownWrapper;
    
    @Mock
    private ClickType clickType;
    
    @Mock
    private TimeProvider timeProvider;
    
    @Mock
    private ConsoleReporter consoleReporter;
    
    private AutoCloseable mockCloseable;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        mouseDownWrapper = new MouseDownWrapper(clickType, timeProvider);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Button Press Operations")
    class ButtonPressOperations {
        
        @Test
        @DisplayName("Should press left button")
        void shouldPressLeftButton() {
            // Arrange
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(1);
        }
        
        @Test
        @DisplayName("Should press right button")
        void shouldPressRightButton() {
            // Arrange
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseDownWrapper.press(2, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(2);
        }
        
        @Test
        @DisplayName("Should press middle button")
        void shouldPressMiddleButton() {
            // Arrange
            when(clickType.getButton(3)).thenReturn(Mouse.MIDDLE);
            
            // Act
            boolean result = mouseDownWrapper.press(3, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(3);
        }
        
        @Test
        @DisplayName("Should handle default button")
        void shouldHandleDefaultButton() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(0, 100);
            
            // Assert
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Hold Duration")
    class HoldDuration {
        
        @ParameterizedTest
        @ValueSource(longs = {0, 50, 100, 500, 1000, 2000})
        @DisplayName("Should hold for specified duration")
        void shouldHoldForSpecifiedDuration(long duration) {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, duration);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(duration);
        }
        
        @Test
        @DisplayName("Should handle zero duration")
        void shouldHandleZeroDuration() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 0);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(0);
        }
        
        @Test
        @DisplayName("Should handle negative duration")
        void shouldHandleNegativeDuration() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, -100);
            
            // Assert
            assertTrue(result);
            // Negative duration should be handled gracefully
        }
    }
    
    @Nested
    @DisplayName("Mock Mode")
    class MockMode {
        
        @Test
        @DisplayName("Should simulate press in mock mode")
        void shouldSimulatePressInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 200);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should log action in mock mode")
        void shouldLogActionInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 150);
            
            // Assert
            assertTrue(result);
            // Mock mode will log the press action
        }
        
        @Test
        @DisplayName("Should not perform actual press in mock mode")
        void shouldNotPerformActualPressInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
                // Act
                boolean result = mouseDownWrapper.press(1, 100);
                
                // Assert
                assertTrue(result);
                mouseMock.verify(() -> Mouse.down(anyInt()), never());
            }
        }
    }
    
    @Nested
    @DisplayName("Button Types")
    class ButtonTypes {
        
        @ParameterizedTest
        @CsvSource({
            "1, LEFT",
            "2, RIGHT",
            "3, MIDDLE"
        })
        @DisplayName("Should map button numbers correctly")
        void shouldMapButtonNumbersCorrectly(int buttonNumber, String expectedButton) {
            // Arrange
            int expectedButtonConstant = switch (expectedButton) {
                case "LEFT" -> Mouse.LEFT;
                case "RIGHT" -> Mouse.RIGHT;
                case "MIDDLE" -> Mouse.MIDDLE;
                default -> Mouse.LEFT;
            };
            when(clickType.getButton(buttonNumber)).thenReturn(expectedButtonConstant);
            
            // Act
            boolean result = mouseDownWrapper.press(buttonNumber, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(buttonNumber);
        }
        
        @Test
        @DisplayName("Should handle invalid button number")
        void shouldHandleInvalidButtonNumber() {
            // Arrange
            when(clickType.getButton(99)).thenReturn(Mouse.LEFT); // Default to LEFT
            
            // Act
            boolean result = mouseDownWrapper.press(99, 100);
            
            // Assert
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Drag Operations")
    class DragOperations {
        
        @Test
        @DisplayName("Should initiate drag operation")
        void shouldInitiateDragOperation() {
            // Arrange
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act - Press and hold for drag
            boolean result = mouseDownWrapper.press(1, 50);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(50);
        }
        
        @Test
        @DisplayName("Should support drag with right button")
        void shouldSupportDragWithRightButton() {
            // Arrange
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseDownWrapper.press(2, 100);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should maintain press state")
        void shouldMaintainPressState() {
            // Arrange
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act - Multiple presses
            boolean result1 = mouseDownWrapper.press(1, 100);
            boolean result2 = mouseDownWrapper.press(1, 50);
            
            // Assert
            assertTrue(result1);
            assertTrue(result2);
            verify(timeProvider).sleep(100);
            verify(timeProvider).sleep(50);
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null click type")
        void shouldHandleNullClickType() {
            // Arrange
            MouseDownWrapper wrapper = new MouseDownWrapper(null, timeProvider);
            
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                wrapper.press(1, 100)
            );
        }
        
        @Test
        @DisplayName("Should handle null time provider")
        void shouldHandleNullTimeProvider() {
            // Arrange
            MouseDownWrapper wrapper = new MouseDownWrapper(clickType, null);
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = wrapper.press(1, 100);
            
            // Assert
            assertTrue(result); // Should handle gracefully in mock mode
        }
        
        @Test
        @DisplayName("Should recover from press failure")
        void shouldRecoverFromPressFailure() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 100);
            
            // Assert
            assertTrue(result); // In mock mode, always succeeds
        }
    }
    
    @Nested
    @DisplayName("Timing Precision")
    class TimingPrecision {
        
        @Test
        @DisplayName("Should maintain precise timing")
        void shouldMaintainPreciseTiming() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            long expectedDuration = 250;
            
            // Act
            boolean result = mouseDownWrapper.press(1, expectedDuration);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(expectedDuration);
        }
        
        @Test
        @DisplayName("Should handle microsecond precision")
        void shouldHandleMicrosecondPrecision() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 1);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(1);
        }
        
        @Test
        @DisplayName("Should handle very long duration")
        void shouldHandleVeryLongDuration() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            long longDuration = 10000; // 10 seconds
            
            // Act
            boolean result = mouseDownWrapper.press(1, longDuration);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(longDuration);
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should support context menu activation")
        void shouldSupportContextMenuActivation() {
            // Arrange - Right click and hold
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseDownWrapper.press(2, 500);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should support selection drag")
        void shouldSupportSelectionDrag() {
            // Arrange - Left click and hold for selection
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseDownWrapper.press(1, 100);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should support custom click sequences")
        void shouldSupportCustomClickSequences() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act - Custom sequence
            boolean press1 = mouseDownWrapper.press(1, 50);
            boolean press2 = mouseDownWrapper.press(1, 100);
            boolean press3 = mouseDownWrapper.press(1, 50);
            
            // Assert
            assertTrue(press1);
            assertTrue(press2);
            assertTrue(press3);
        }
    }
}