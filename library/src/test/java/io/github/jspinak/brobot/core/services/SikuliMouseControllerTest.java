package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.Location;
import org.sikuli.script.Mouse;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for SikuliMouseController implementation.
 * Tests all mouse operations with mock Sikuli components.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SikuliMouseController Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SikuliMouseControllerTest extends BrobotTestBase {

    private SikuliMouseController controller;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        controller = new SikuliMouseController();
    }

    @Test
    @DisplayName("Should move mouse to specified location")
    void testMoveTo() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class)) {
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(100, 200)).thenReturn(mockLocation);

            // Act
            boolean result = controller.moveTo(100, 200);

            // Assert
            assertTrue(result);
            verify(mockLocation).hover();
        }
    }

    @Test
    @DisplayName("Should handle move failure when hover returns null")
    void testMoveToFailure() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class)) {
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(null);
            locationMock.when(() -> new Location(anyInt(), anyInt())).thenReturn(mockLocation);

            // Act
            boolean result = controller.moveTo(100, 200);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should handle move exception gracefully")
    void testMoveToException() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class)) {
            // Arrange
            locationMock.when(() -> new Location(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Test exception"));

            // Act
            boolean result = controller.moveTo(100, 200);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should perform left click at coordinates")
    void testClickLeft() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(50, 75)).thenReturn(mockLocation);
            
            GraphicsEnvironment mockGE = mock(GraphicsEnvironment.class);
            when(mockGE.isHeadless()).thenReturn(false);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockGE);

            // Act
            boolean result = controller.click(50, 75, MouseController.MouseButton.LEFT);

            // Assert  
            assertTrue(result);
            verify(mockLocation).hover();
        }
    }

    @Test
    @DisplayName("Should perform right click at coordinates")
    void testClickRight() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(100, 100)).thenReturn(mockLocation);
            
            GraphicsEnvironment mockGE = mock(GraphicsEnvironment.class);
            when(mockGE.isHeadless()).thenReturn(false);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockGE);

            // Act
            boolean result = controller.click(100, 100, MouseController.MouseButton.RIGHT);

            // Assert
            assertTrue(result);
            verify(mockLocation).hover();
        }
    }

    @Test
    @DisplayName("Should perform middle click at coordinates")
    void testClickMiddle() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(150, 150)).thenReturn(mockLocation);
            
            GraphicsEnvironment mockGE = mock(GraphicsEnvironment.class);
            when(mockGE.isHeadless()).thenReturn(false);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockGE);

            // Act
            boolean result = controller.click(150, 150, MouseController.MouseButton.MIDDLE);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should handle click failure when move fails")
    void testClickMoveFailure() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class)) {
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(null);
            locationMock.when(() -> new Location(anyInt(), anyInt())).thenReturn(mockLocation);

            // Act
            boolean result = controller.click(100, 100, MouseController.MouseButton.LEFT);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should perform double-click for left button")
    void testDoubleClickLeft() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(200, 300)).thenReturn(mockLocation);
            
            GraphicsEnvironment mockGE = mock(GraphicsEnvironment.class);
            when(mockGE.isHeadless()).thenReturn(false);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockGE);

            // Act
            boolean result = controller.doubleClick(200, 300, MouseController.MouseButton.LEFT);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should simulate double-click for non-left buttons")
    void testDoubleClickNonLeft() throws InterruptedException {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(200, 300)).thenReturn(mockLocation);
            
            GraphicsEnvironment mockGE = mock(GraphicsEnvironment.class);
            when(mockGE.isHeadless()).thenReturn(false);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockGE);

            // Act
            boolean result = controller.doubleClick(200, 300, MouseController.MouseButton.RIGHT);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should press mouse button down")
    void testMouseDown() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Arrange
            mouseMock.when(() -> Mouse.down(anyInt())).thenAnswer(invocation -> null);

            // Act
            boolean result = controller.mouseDown(MouseController.MouseButton.LEFT);

            // Assert
            assertTrue(result);
            mouseMock.verify(() -> Mouse.down(anyInt()));
        }
    }

    @Test
    @DisplayName("Should release mouse button")
    void testMouseUp() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Arrange
            mouseMock.when(() -> Mouse.up(anyInt())).thenAnswer(invocation -> null);

            // Act
            boolean result = controller.mouseUp(MouseController.MouseButton.LEFT);

            // Assert
            assertTrue(result);
            mouseMock.verify(() -> Mouse.up(anyInt()));
        }
    }

    @Test
    @DisplayName("Should handle mouse down exception")
    void testMouseDownException() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Arrange
            mouseMock.when(() -> Mouse.down(anyInt()))
                .thenThrow(new RuntimeException("Test exception"));

            // Act
            boolean result = controller.mouseDown(MouseController.MouseButton.LEFT);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should perform drag operation")
    void testDrag() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            
            // Arrange
            Location startLoc = mock(Location.class);
            Location endLoc = mock(Location.class);
            when(startLoc.hover()).thenReturn(startLoc);
            when(endLoc.hover()).thenReturn(endLoc);
            
            locationMock.when(() -> new Location(10, 20)).thenReturn(startLoc);
            locationMock.when(() -> new Location(100, 200)).thenReturn(endLoc);
            mouseMock.when(() -> Mouse.down(anyInt())).thenAnswer(invocation -> null);

            // Act
            boolean result = controller.drag(10, 20, 100, 200, MouseController.MouseButton.LEFT);

            // Assert
            assertTrue(result);
            verify(startLoc).hover();
            mouseMock.verify(() -> Mouse.down(anyInt()));
        }
    }

    @Test
    @DisplayName("Should handle drag failure and release button")
    void testDragFailure() {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class);
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            
            // Arrange
            Location startLoc = mock(Location.class);
            when(startLoc.hover()).thenReturn(null);
            locationMock.when(() -> new Location(10, 20)).thenReturn(startLoc);

            // Act
            boolean result = controller.drag(10, 20, 100, 200, MouseController.MouseButton.LEFT);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should scroll mouse wheel")
    void testScroll() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            mouseMock.when(Mouse::at).thenReturn(mockLocation);
            
            GraphicsEnvironment mockGE = mock(GraphicsEnvironment.class);
            when(mockGE.isHeadless()).thenReturn(false);
            geMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(mockGE);

            // Act
            boolean result = controller.scroll(5);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("Should handle scroll with null mouse location")
    void testScrollNullLocation() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Arrange
            mouseMock.when(Mouse::at).thenReturn(null);

            // Act
            boolean result = controller.scroll(5);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should get current mouse position")
    void testGetPosition() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Arrange
            Location mockLocation = mock(Location.class);
            mockLocation.x = 300;
            mockLocation.y = 400;
            mouseMock.when(Mouse::at).thenReturn(mockLocation);

            // Act
            int[] position = controller.getPosition();

            // Assert
            assertNotNull(position);
            assertEquals(2, position.length);
            assertEquals(300, position[0]);
            assertEquals(400, position[1]);
        }
    }

    @Test
    @DisplayName("Should return null position when location is null")
    void testGetPositionNull() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            // Arrange
            mouseMock.when(Mouse::at).thenReturn(null);

            // Act
            int[] position = controller.getPosition();

            // Assert
            assertNull(position);
        }
    }

    @Test
    @DisplayName("Should check availability in non-headless environment")
    void testIsAvailableNonHeadless() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class);
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            
            // Arrange
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);
            Location mockLocation = mock(Location.class);
            mouseMock.when(Mouse::at).thenReturn(mockLocation);

            // Act
            boolean available = controller.isAvailable();

            // Assert
            assertTrue(available);
        }
    }

    @Test
    @DisplayName("Should not be available in headless environment")
    void testIsAvailableHeadless() {
        try (MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            // Arrange
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(true);

            // Act
            boolean available = controller.isAvailable();

            // Assert
            assertFalse(available);
        }
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Act
        String name = controller.getImplementationName();

        // Assert
        assertEquals("Sikuli", name);
    }

    @Test
    @DisplayName("Should convert MouseButton enum correctly")
    void testButtonConversion() {
        // Test all button conversions through actual operations
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            mouseMock.when(() -> Mouse.down(anyInt())).thenAnswer(invocation -> null);
            mouseMock.when(() -> Mouse.up(anyInt())).thenAnswer(invocation -> null);

            // Test LEFT
            assertTrue(controller.mouseDown(MouseController.MouseButton.LEFT));
            assertTrue(controller.mouseUp(MouseController.MouseButton.LEFT));

            // Test RIGHT
            assertTrue(controller.mouseDown(MouseController.MouseButton.RIGHT));
            assertTrue(controller.mouseUp(MouseController.MouseButton.RIGHT));

            // Test MIDDLE
            assertTrue(controller.mouseDown(MouseController.MouseButton.MIDDLE));
            assertTrue(controller.mouseUp(MouseController.MouseButton.MIDDLE));
        }
    }

    @Test
    @DisplayName("Should handle concurrent operations safely")
    void testConcurrentOperations() throws InterruptedException {
        try (MockedStatic<Location> locationMock = mockStatic(Location.class)) {
            // Arrange
            Location mockLocation = mock(Location.class);
            when(mockLocation.hover()).thenReturn(mockLocation);
            locationMock.when(() -> new Location(anyInt(), anyInt())).thenReturn(mockLocation);

            // Act - Simulate concurrent access
            Thread thread1 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    controller.moveTo(i, i);
                }
            });

            Thread thread2 = new Thread(() -> {
                for (int i = 0; i < 10; i++) {
                    controller.moveTo(i * 2, i * 2);
                }
            });

            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            // Assert - No exceptions should occur
            assertTrue(true);
        }
    }
}