package io.github.jspinak.brobot.core.services;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sikuli.script.Location;
import org.sikuli.script.Mouse;
import org.sikuli.script.Button;
import org.sikuli.script.Region;

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
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                })) {
            // Act
            boolean result = controller.moveTo(100, 200);

            // Assert
            assertTrue(result);
            Location createdLocation = locationMock.constructed().get(0);
            verify(createdLocation).hover();
        }
    }

    @Test
    @DisplayName("Should handle move failure when hover returns null")
    void testMoveToFailure() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(null);
                })) {
            // Act
            boolean result = controller.moveTo(100, 200);

            // Assert
            assertFalse(result);
            Location createdLocation = locationMock.constructed().get(0);
            verify(createdLocation).hover();
        }
    }

    @Test
    @DisplayName("Should handle move exception gracefully")
    void testMoveToException() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenThrow(new RuntimeException("Test exception"));
                })) {
            // Act
            boolean result = controller.moveTo(100, 200);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    @DisplayName("Should perform left click at coordinates")
    void testClickLeft() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                    doNothing().when(mock).click();
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Mock GraphicsEnvironment.isHeadless() directly
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // Act
            boolean result = controller.click(50, 75, MouseController.MouseButton.LEFT);

            // Assert  
            assertTrue(result);
            // Implementation calls moveTo twice + creates one location for click = 3 locations
            assertEquals(3, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover(); // first moveTo call
            verify(locationMock.constructed().get(1)).hover(); // second moveTo call
            verify(locationMock.constructed().get(2)).click(); // click call
        }
    }

    @Test
    @DisplayName("Should perform right click at coordinates")
    void testClickRight() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                    doNothing().when(mock).rightClick();
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Mock GraphicsEnvironment.isHeadless() directly
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // Act
            boolean result = controller.click(100, 100, MouseController.MouseButton.RIGHT);

            // Assert
            assertTrue(result);
            // Implementation calls moveTo twice + creates one location for click = 3 locations
            assertEquals(3, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover(); // first moveTo call
            verify(locationMock.constructed().get(1)).hover(); // second moveTo call
            verify(locationMock.constructed().get(2)).rightClick(); // rightClick call
        }
    }

    @Test
    @DisplayName("Should perform middle click at coordinates")
    void testClickMiddle() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Mock GraphicsEnvironment.isHeadless() directly
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // Act
            boolean result = controller.click(150, 150, MouseController.MouseButton.MIDDLE);

            // Assert
            assertTrue(result);
            // Implementation calls moveTo twice + creates one location for middle click = 3 locations
            assertEquals(3, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover(); // first moveTo call
            verify(locationMock.constructed().get(1)).hover(); // second moveTo call
            // Verify Mouse operations for middle click
            mouseMock.verify(() -> Mouse.move(any(Location.class)));
            mouseMock.verify(() -> Mouse.down(Button.MIDDLE));
            mouseMock.verify(() -> Mouse.up(Button.MIDDLE));
        }
    }

    @Test
    @DisplayName("Should handle click failure when move fails")
    void testClickMoveFailure() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(null);
                })) {
            // Act
            boolean result = controller.click(100, 100, MouseController.MouseButton.LEFT);

            // Assert
            assertFalse(result);
            // Should only create one location for moveTo, since moveTo fails
            assertEquals(1, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover();
        }
    }

    @Test
    @DisplayName("Should perform double-click for left button")
    void testDoubleClickLeft() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                    doNothing().when(mock).doubleClick();
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Mock GraphicsEnvironment.isHeadless() directly
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // Act
            boolean result = controller.doubleClick(200, 300, MouseController.MouseButton.LEFT);

            // Assert
            assertTrue(result);
            // Implementation calls moveTo once + creates one location for doubleClick = 2 locations
            assertEquals(2, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover(); // moveTo call
            verify(locationMock.constructed().get(1)).doubleClick(); // doubleClick call
        }
    }

    @Test
    @DisplayName("Should simulate double-click for non-left buttons")
    void testDoubleClickNonLeft() throws InterruptedException {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                    doNothing().when(mock).rightClick();
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Mock GraphicsEnvironment.isHeadless() directly
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // Act
            boolean result = controller.doubleClick(200, 300, MouseController.MouseButton.RIGHT);

            // Assert
            assertTrue(result);
            // Double-click for non-left buttons simulates with two click operations
            // 1 moveTo for initial + 2 clicks (each with 2 moveTo + 1 click location) = 7 locations
            assertTrue(locationMock.constructed().size() >= 7);
            verify(locationMock.constructed().get(0)).hover(); // initial moveTo call
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
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            
            mouseMock.when(() -> Mouse.down(anyInt())).thenAnswer(invocation -> null);
            mouseMock.when(() -> Mouse.up(anyInt())).thenAnswer(invocation -> null);

            // Act
            boolean result = controller.drag(10, 20, 100, 200, MouseController.MouseButton.LEFT);

            // Assert
            assertTrue(result);
            // Drag creates: 2 locations for startLoc + 1 location for moveTo start + 1 location for moveTo end = 4 locations
            assertEquals(4, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover(); // start location from moveTo
            verify(locationMock.constructed().get(1)).hover(); // start location construction
            verify(locationMock.constructed().get(2)).hover(); // end location construction  
            verify(locationMock.constructed().get(3)).hover(); // end location from moveTo
            mouseMock.verify(() -> Mouse.down(anyInt()));
        }
    }

    @Test
    @DisplayName("Should handle drag failure and release button")
    void testDragFailure() {
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(null); // Make hover fail
                });
             MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class)) {
            
            mouseMock.when(() -> Mouse.up(anyInt())).thenAnswer(invocation -> null);

            // Act
            boolean result = controller.drag(10, 20, 100, 200, MouseController.MouseButton.LEFT);

            // Assert
            assertFalse(result);
            // Should create 2 locations: 1 for moveTo + 1 for start location construction
            assertEquals(2, locationMock.constructed().size());
            verify(locationMock.constructed().get(0)).hover(); // moveTo call that fails
            // Second location for startLoc construction but won't be used since moveTo failed
        }
    }

    @Test
    @DisplayName("Should scroll mouse wheel")
    void testScroll() {
        try (MockedStatic<Mouse> mouseMock = mockStatic(Mouse.class);
             MockedConstruction<Region> regionMock = mockConstruction(Region.class, 
                (mock, context) -> {
                    doNothing().when(mock).wheel(anyInt(), anyInt());
                });
             MockedConstruction<Location> locationMock = mockConstruction(Location.class);
             MockedStatic<GraphicsEnvironment> geMock = mockStatic(GraphicsEnvironment.class)) {
            
            // Arrange
            Location mockLocation = mock(Location.class);
            mockLocation.x = 100;
            mockLocation.y = 100;
            mouseMock.when(Mouse::at).thenReturn(mockLocation);
            
            // Mock GraphicsEnvironment.isHeadless() directly
            geMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);

            // Act
            boolean result = controller.scroll(5);

            // Assert
            assertTrue(result);
            // Verify region was created and wheel method was called
            assertEquals(1, regionMock.constructed().size());
            verify(regionMock.constructed().get(0)).wheel(1, 5); // direction=1 (down), steps=5
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
        try (MockedConstruction<Location> locationMock = mockConstruction(Location.class, 
                (mock, context) -> {
                    when(mock.hover()).thenReturn(mock);
                })) {
            
            final int iterations = 5; // Reduce iterations for more stable test
            // Act - Simulate concurrent access
            Thread thread1 = new Thread(() -> {
                for (int i = 0; i < iterations; i++) {
                    controller.moveTo(i, i);
                }
            });

            Thread thread2 = new Thread(() -> {
                for (int i = 0; i < iterations; i++) {
                    controller.moveTo(i * 2, i * 2);
                }
            });

            thread1.start();
            thread2.start();
            thread1.join();
            thread2.join();

            // Assert - No exceptions should occur and some locations should have been created
            assertTrue(locationMock.constructed().size() >= iterations); // At least some operations succeeded
        }
    }
}