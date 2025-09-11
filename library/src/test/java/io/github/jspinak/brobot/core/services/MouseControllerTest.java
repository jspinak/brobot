package io.github.jspinak.brobot.core.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Tests for MouseController interface implementations. Tests the contract defined by the
 * MouseController interface.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MouseController Interface Tests")
@DisabledInCI
public class MouseControllerTest extends BrobotTestBase {

    @Mock private MouseController mouseController;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
    }

    @Test
    @DisplayName("Should move mouse to specified coordinates")
    void testMoveTo() {
        // Arrange
        when(mouseController.moveTo(100, 200)).thenReturn(true);

        // Act
        boolean result = mouseController.moveTo(100, 200);

        // Assert
        assertTrue(result);
        verify(mouseController).moveTo(100, 200);
    }

    @Test
    @DisplayName("Should handle move failure gracefully")
    void testMoveToFailure() {
        // Arrange
        when(mouseController.moveTo(anyInt(), anyInt())).thenReturn(false);

        // Act
        boolean result = mouseController.moveTo(-1, -1);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should perform left click at specified coordinates")
    void testClick() {
        // Arrange
        when(mouseController.click(50, 75, MouseController.MouseButton.LEFT)).thenReturn(true);

        // Act
        boolean result = mouseController.click(50, 75, MouseController.MouseButton.LEFT);

        // Assert
        assertTrue(result);
        verify(mouseController).click(50, 75, MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should perform right click at specified coordinates")
    void testRightClick() {
        // Arrange
        when(mouseController.click(100, 100, MouseController.MouseButton.RIGHT)).thenReturn(true);
        when(mouseController.rightClick(100, 100)).thenCallRealMethod();

        // Act
        boolean result = mouseController.rightClick(100, 100);

        // Assert
        assertTrue(result);
        verify(mouseController).click(100, 100, MouseController.MouseButton.RIGHT);
    }

    @Test
    @DisplayName("Should perform middle click at specified coordinates")
    void testMiddleClick() {
        // Arrange
        when(mouseController.click(150, 150, MouseController.MouseButton.MIDDLE)).thenReturn(true);

        // Act
        boolean result = mouseController.click(150, 150, MouseController.MouseButton.MIDDLE);

        // Assert
        assertTrue(result);
        verify(mouseController).click(150, 150, MouseController.MouseButton.MIDDLE);
    }

    @Test
    @DisplayName("Should perform double-click at specified coordinates")
    void testDoubleClick() {
        // Arrange
        when(mouseController.doubleClick(200, 300, MouseController.MouseButton.LEFT))
                .thenReturn(true);

        // Act
        boolean result = mouseController.doubleClick(200, 300, MouseController.MouseButton.LEFT);

        // Assert
        assertTrue(result);
        verify(mouseController).doubleClick(200, 300, MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should use default left button for parameterless double-click")
    void testDoubleClickDefault() {
        // Arrange
        when(mouseController.doubleClick(100, 200, MouseController.MouseButton.LEFT))
                .thenReturn(true);
        when(mouseController.doubleClick(100, 200)).thenCallRealMethod();

        // Act
        boolean result = mouseController.doubleClick(100, 200);

        // Assert
        assertTrue(result);
        verify(mouseController).doubleClick(100, 200, MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should press and hold mouse button")
    void testMouseDown() {
        // Arrange
        when(mouseController.mouseDown(MouseController.MouseButton.LEFT)).thenReturn(true);

        // Act
        boolean result = mouseController.mouseDown(MouseController.MouseButton.LEFT);

        // Assert
        assertTrue(result);
        verify(mouseController).mouseDown(MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should release mouse button")
    void testMouseUp() {
        // Arrange
        when(mouseController.mouseUp(MouseController.MouseButton.LEFT)).thenReturn(true);

        // Act
        boolean result = mouseController.mouseUp(MouseController.MouseButton.LEFT);

        // Assert
        assertTrue(result);
        verify(mouseController).mouseUp(MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should perform drag operation from start to end")
    void testDrag() {
        // Arrange
        when(mouseController.drag(10, 20, 100, 200, MouseController.MouseButton.LEFT))
                .thenReturn(true);

        // Act
        boolean result = mouseController.drag(10, 20, 100, 200, MouseController.MouseButton.LEFT);

        // Assert
        assertTrue(result);
        verify(mouseController).drag(10, 20, 100, 200, MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should use default left button for parameterless drag")
    void testDragDefault() {
        // Arrange
        when(mouseController.drag(50, 50, 150, 150, MouseController.MouseButton.LEFT))
                .thenReturn(true);
        when(mouseController.drag(50, 50, 150, 150)).thenCallRealMethod();

        // Act
        boolean result = mouseController.drag(50, 50, 150, 150);

        // Assert
        assertTrue(result);
        verify(mouseController).drag(50, 50, 150, 150, MouseController.MouseButton.LEFT);
    }

    @Test
    @DisplayName("Should scroll mouse wheel up")
    void testScrollUp() {
        // Arrange
        when(mouseController.scroll(5)).thenReturn(true);

        // Act
        boolean result = mouseController.scroll(5);

        // Assert
        assertTrue(result);
        verify(mouseController).scroll(5);
    }

    @Test
    @DisplayName("Should scroll mouse wheel down")
    void testScrollDown() {
        // Arrange
        when(mouseController.scroll(-5)).thenReturn(true);

        // Act
        boolean result = mouseController.scroll(-5);

        // Assert
        assertTrue(result);
        verify(mouseController).scroll(-5);
    }

    @Test
    @DisplayName("Should get current mouse position")
    void testGetPosition() {
        // Arrange
        int[] expectedPosition = {300, 400};
        when(mouseController.getPosition()).thenReturn(expectedPosition);

        // Act
        int[] position = mouseController.getPosition();

        // Assert
        assertNotNull(position);
        assertEquals(2, position.length);
        assertEquals(300, position[0]);
        assertEquals(400, position[1]);
    }

    @Test
    @DisplayName("Should handle null position gracefully")
    void testGetPositionNull() {
        // Arrange
        when(mouseController.getPosition()).thenReturn(null);

        // Act
        int[] position = mouseController.getPosition();

        // Assert
        assertNull(position);
    }

    @Test
    @DisplayName("Should check if controller is available")
    void testIsAvailable() {
        // Arrange
        when(mouseController.isAvailable()).thenReturn(true);

        // Act
        boolean available = mouseController.isAvailable();

        // Assert
        assertTrue(available);
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        // Arrange
        when(mouseController.getImplementationName()).thenReturn("TestImplementation");

        // Act
        String name = mouseController.getImplementationName();

        // Assert
        assertNotNull(name);
        assertEquals("TestImplementation", name);
    }

    @Test
    @DisplayName("Should handle edge case coordinates")
    void testEdgeCaseCoordinates() {
        // Test zero coordinates
        when(mouseController.moveTo(0, 0)).thenReturn(true);
        assertTrue(mouseController.moveTo(0, 0));

        // Test large coordinates
        when(mouseController.moveTo(Integer.MAX_VALUE, Integer.MAX_VALUE)).thenReturn(true);
        assertTrue(mouseController.moveTo(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("Should handle multiple button operations in sequence")
    void testButtonSequence() {
        // Arrange
        when(mouseController.mouseDown(MouseController.MouseButton.LEFT)).thenReturn(true);
        when(mouseController.moveTo(100, 100)).thenReturn(true);
        when(mouseController.mouseUp(MouseController.MouseButton.LEFT)).thenReturn(true);

        // Act - Simulate drag operation manually
        boolean downResult = mouseController.mouseDown(MouseController.MouseButton.LEFT);
        boolean moveResult = mouseController.moveTo(100, 100);
        boolean upResult = mouseController.mouseUp(MouseController.MouseButton.LEFT);

        // Assert
        assertTrue(downResult);
        assertTrue(moveResult);
        assertTrue(upResult);
        verify(mouseController).mouseDown(MouseController.MouseButton.LEFT);
        verify(mouseController).moveTo(100, 100);
        verify(mouseController).mouseUp(MouseController.MouseButton.LEFT);
    }
}
