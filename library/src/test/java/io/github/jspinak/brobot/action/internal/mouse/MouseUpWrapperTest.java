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
 * Test suite for MouseUpWrapper class.
 * Tests mouse button release functionality with timing control.
 */
@DisplayName("MouseUpWrapper Tests")
public class MouseUpWrapperTest extends BrobotTestBase {

    private MouseUpWrapper mouseUpWrapper;
    
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
        mouseUpWrapper = new MouseUpWrapper(clickType, timeProvider);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }
    
    @Nested
    @DisplayName("Button Release Operations")
    class ButtonReleaseOperations {
        
        @Test
        @DisplayName("Should release left button")
        void shouldReleaseLeftButton() {
            // Arrange
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, 50, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(1);
        }
        
        @Test
        @DisplayName("Should release right button")
        void shouldReleaseRightButton() {
            // Arrange
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseUpWrapper.press(2, 50, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(2);
        }
        
        @Test
        @DisplayName("Should release middle button")
        void shouldReleaseMiddleButton() {
            // Arrange
            when(clickType.getButton(3)).thenReturn(Mouse.MIDDLE);
            
            // Act
            boolean result = mouseUpWrapper.press(3, 50, 100);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(3);
        }
        
        @Test
        @DisplayName("Should handle default button release")
        void shouldHandleDefaultButtonRelease() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(0, 50, 100);
            
            // Assert
            assertTrue(result);
        }
    }
    
    @Nested
    @DisplayName("Timing Control")
    class TimingControl {
        
        @Test
        @DisplayName("Should apply before delay")
        void shouldApplyBeforeDelay() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            long beforeDelay = 200;
            
            // Act
            boolean result = mouseUpWrapper.press(1, beforeDelay, 100);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(beforeDelay);
        }
        
        @Test
        @DisplayName("Should apply after delay")
        void shouldApplyAfterDelay() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            long afterDelay = 300;
            
            // Act
            boolean result = mouseUpWrapper.press(1, 100, afterDelay);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(afterDelay);
        }
        
        @ParameterizedTest
        @CsvSource({
            "0, 0",
            "50, 100",
            "100, 50",
            "200, 200",
            "500, 1000"
        })
        @DisplayName("Should handle various timing combinations")
        void shouldHandleVariousTimingCombinations(long beforeDelay, long afterDelay) {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, beforeDelay, afterDelay);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(beforeDelay);
            verify(timeProvider).sleep(afterDelay);
        }
        
        @Test
        @DisplayName("Should handle zero delays")
        void shouldHandleZeroDelays() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, 0, 0);
            
            // Assert
            assertTrue(result);
            verify(timeProvider, times(2)).sleep(0);
        }
    }
    
    @Nested
    @DisplayName("Mock Mode")
    class MockMode {
        
        @Test
        @DisplayName("Should simulate release in mock mode")
        void shouldSimulateReleaseInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, 100, 100);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should log action in mock mode")
        void shouldLogActionInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseUpWrapper.press(2, 50, 50);
            
            // Assert
            assertTrue(result);
            // Mock mode will log the release action
        }
        
        @Test
        @DisplayName("Should not perform actual release in mock mode")
        void shouldNotPerformActualReleaseInMockMode() {
            // Arrange
            assertTrue(FrameworkSettings.mock);
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
                // Act
                boolean result = mouseUpWrapper.press(1, 100, 100);
                
                // Assert
                assertTrue(result);
                mouseMock.verify(() -> Mouse.up(anyInt()), never());
            }
        }
    }
    
    @Nested
    @DisplayName("Drag and Drop Support")
    class DragAndDropSupport {
        
        @Test
        @DisplayName("Should complete drag operation")
        void shouldCompleteDragOperation() {
            // Arrange - Release after drag
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, 50, 100);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should release after right-button drag")
        void shouldReleaseAfterRightButtonDrag() {
            // Arrange
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseUpWrapper.press(2, 50, 100);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should support quick release for drop")
        void shouldSupportQuickReleaseForDrop() {
            // Arrange
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act - Quick release with minimal delays
            boolean result = mouseUpWrapper.press(1, 10, 10);
            
            // Assert
            assertTrue(result);
            verify(timeProvider).sleep(10);
        }
    }
    
    @Nested
    @DisplayName("Button Combinations")
    class ButtonCombinations {
        
        @ParameterizedTest
        @CsvSource({
            "1, LEFT",
            "2, RIGHT",
            "3, MIDDLE"
        })
        @DisplayName("Should release correct button type")
        void shouldReleaseCorrectButtonType(int buttonNumber, String expectedButton) {
            // Arrange
            int expectedButtonConstant = switch (expectedButton) {
                case "LEFT" -> Mouse.LEFT;
                case "RIGHT" -> Mouse.RIGHT;
                case "MIDDLE" -> Mouse.MIDDLE;
                default -> Mouse.LEFT;
            };
            when(clickType.getButton(buttonNumber)).thenReturn(expectedButtonConstant);
            
            // Act
            boolean result = mouseUpWrapper.press(buttonNumber, 50, 50);
            
            // Assert
            assertTrue(result);
            verify(clickType).getButton(buttonNumber);
        }
        
        @Test
        @DisplayName("Should handle sequential releases")
        void shouldHandleSequentialReleases() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act - Multiple releases
            boolean result1 = mouseUpWrapper.press(1, 50, 50);
            boolean result2 = mouseUpWrapper.press(1, 100, 100);
            boolean result3 = mouseUpWrapper.press(1, 25, 25);
            
            // Assert
            assertTrue(result1);
            assertTrue(result2);
            assertTrue(result3);
            verify(timeProvider, times(6)).sleep(anyLong()); // 2 sleeps per press
        }
    }
    
    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        
        @Test
        @DisplayName("Should handle null click type")
        void shouldHandleNullClickType() {
            // Arrange
            MouseUpWrapper wrapper = new MouseUpWrapper(null, timeProvider);
            
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                wrapper.press(1, 100, 100)
            );
        }
        
        @Test
        @DisplayName("Should handle null time provider")
        void shouldHandleNullTimeProvider() {
            // Arrange
            MouseUpWrapper wrapper = new MouseUpWrapper(clickType, null);
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = wrapper.press(1, 100, 100);
            
            // Assert
            assertTrue(result); // Should handle gracefully in mock mode
        }
        
        @Test
        @DisplayName("Should handle negative delays")
        void shouldHandleNegativeDelays() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, -50, -100);
            
            // Assert
            assertTrue(result); // Should handle gracefully
        }
    }
    
    @Nested
    @DisplayName("Performance")
    class Performance {
        
        @Test
        @DisplayName("Should release quickly with minimal delays")
        void shouldReleaseQuicklyWithMinimalDelays() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            long startTime = System.currentTimeMillis();
            boolean result = mouseUpWrapper.press(1, 0, 0);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertTrue(result);
            assertTrue(endTime - startTime < 50, "Release should be very quick with zero delays");
        }
        
        @ParameterizedTest
        @ValueSource(ints = {10, 50, 100, 200, 500})
        @DisplayName("Should handle various delay durations efficiently")
        void shouldHandleVariousDelayDurationsEfficiently(int delayMs) {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, delayMs, delayMs);
            
            // Assert
            assertTrue(result);
            verify(timeProvider, times(2)).sleep(delayMs);
        }
    }
    
    @Nested
    @DisplayName("Use Cases")
    class UseCases {
        
        @Test
        @DisplayName("Should complete drag and drop")
        void shouldCompleteDragAndDrop() {
            // Arrange - Release after dragging
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, 50, 100);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should release after selection")
        void shouldReleaseAfterSelection() {
            // Arrange - Release after text selection
            when(clickType.getButton(1)).thenReturn(Mouse.LEFT);
            
            // Act
            boolean result = mouseUpWrapper.press(1, 25, 50);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should complete context menu action")
        void shouldCompleteContextMenuAction() {
            // Arrange - Release right button
            when(clickType.getButton(2)).thenReturn(Mouse.RIGHT);
            
            // Act
            boolean result = mouseUpWrapper.press(2, 100, 200);
            
            // Assert
            assertTrue(result);
        }
        
        @Test
        @DisplayName("Should support custom click sequences")
        void shouldSupportCustomClickSequences() {
            // Arrange
            when(clickType.getButton(anyInt())).thenReturn(Mouse.LEFT);
            
            // Act - Custom release sequence
            assertTrue(mouseUpWrapper.press(1, 10, 20));
            assertTrue(mouseUpWrapper.press(1, 30, 40));
            assertTrue(mouseUpWrapper.press(1, 50, 60));
        }
    }
}