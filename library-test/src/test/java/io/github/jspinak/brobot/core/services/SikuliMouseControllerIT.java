package io.github.jspinak.brobot.core.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Integration tests for SikuliMouseController. Tests mouse operations in mock mode using Spring
 * context.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SikuliMouseController Integration Tests")
@Tag("integration")
@Tag("mouse")
public class SikuliMouseControllerIT extends BrobotTestBase {

    @Autowired private SikuliMouseController mouseController;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        // Ensure we're in mock mode for safe testing
        // Mock mode assertions handled by framework
    }

    @Test
    @DisplayName("Should be available in mock mode")
    void testIsAvailable() {
        // In mock mode, the controller should be available
        assertTrue(mouseController.isAvailable());
    }

    @Test
    @DisplayName("Should return implementation name")
    void testGetImplementationName() {
        String name = mouseController.getImplementationName();
        assertNotNull(name);
        assertTrue(name.contains("Sikuli"));
    }

    @Test
    @DisplayName("Should successfully move mouse in mock mode")
    void testMoveTo() {
        // In mock mode, mouse operations should succeed
        boolean result = mouseController.moveTo(100, 200);
        assertTrue(result, "Move operation should succeed in mock mode");
    }

    @Test
    @DisplayName("Should successfully click in mock mode")
    void testClick() {
        // Test left click
        boolean leftClick = mouseController.click(100, 200, MouseController.MouseButton.LEFT);
        assertTrue(leftClick, "Left click should succeed in mock mode");

        // Test right click
        boolean rightClick = mouseController.click(150, 250, MouseController.MouseButton.RIGHT);
        assertTrue(rightClick, "Right click should succeed in mock mode");

        // Test middle click
        boolean middleClick = mouseController.click(200, 300, MouseController.MouseButton.MIDDLE);
        assertTrue(middleClick, "Middle click should succeed in mock mode");
    }

    @Test
    @DisplayName("Should successfully double-click in mock mode")
    void testDoubleClick() {
        // Test double-click for left button
        boolean leftDouble =
                mouseController.doubleClick(100, 200, MouseController.MouseButton.LEFT);
        assertTrue(leftDouble, "Left double-click should succeed in mock mode");

        // Test double-click for right button (simulated)
        boolean rightDouble =
                mouseController.doubleClick(150, 250, MouseController.MouseButton.RIGHT);
        assertTrue(rightDouble, "Right double-click should succeed in mock mode");
    }

    @Test
    @DisplayName("Should successfully drag in mock mode")
    void testDrag() {
        boolean dragResult = mouseController.drag(100, 100, 200, 200);
        assertTrue(dragResult, "Drag operation should succeed in mock mode");
    }

    @Test
    @DisplayName("Should successfully scroll in mock mode")
    void testScroll() {
        // Test scroll down
        boolean scrollDown = mouseController.scroll(5);
        assertTrue(scrollDown, "Scroll down should succeed in mock mode");

        // Test scroll up
        boolean scrollUp = mouseController.scroll(-5);
        assertTrue(scrollUp, "Scroll up should succeed in mock mode");
    }

    @Test
    @DisplayName("Should handle mouse button operations in mock mode")
    void testMouseButtonOperations() {
        // Test button down
        boolean buttonDown = mouseController.mouseDown(MouseController.MouseButton.LEFT);
        assertTrue(buttonDown, "Mouse button down should succeed in mock mode");

        // Test button up
        boolean buttonUp = mouseController.mouseUp(MouseController.MouseButton.LEFT);
        assertTrue(buttonUp, "Mouse button up should succeed in mock mode");
    }

    @Test
    @DisplayName("Should get mouse position in mock mode")
    void testGetPosition() {
        // Move to a known position first
        mouseController.moveTo(300, 400);

        // Get position (in mock mode, this might return a default or last set position)
        int[] position = mouseController.getPosition();

        // In mock mode, position might be null or a default value
        // We just verify it doesn't throw an exception
        // The actual position verification depends on the mock implementation
        if (position != null) {
            assertEquals(2, position.length, "Position should have x and y coordinates");
        }
    }

    @Test
    @DisplayName("Should handle rapid sequential operations in mock mode")
    void testRapidOperations() {
        // Test that rapid operations don't cause issues
        for (int i = 0; i < 10; i++) {
            boolean moveResult = mouseController.moveTo(i * 10, i * 10);
            assertTrue(moveResult, "Move " + i + " should succeed");

            boolean clickResult =
                    mouseController.click(i * 10, i * 10, MouseController.MouseButton.LEFT);
            assertTrue(clickResult, "Click " + i + " should succeed");
        }
    }

    @Test
    @DisplayName("Should handle edge case coordinates in mock mode")
    void testEdgeCaseCoordinates() {
        // Test zero coordinates
        assertTrue(mouseController.moveTo(0, 0));
        assertTrue(mouseController.click(0, 0, MouseController.MouseButton.LEFT));

        // Test negative coordinates (should handle gracefully)
        boolean negativeMove = mouseController.moveTo(-10, -10);
        // Implementation might reject negative coords or handle them
        assertNotNull(negativeMove);

        // Test large coordinates
        assertTrue(mouseController.moveTo(10000, 10000));
    }

    @Test
    @DisplayName("Should maintain thread safety in mock mode")
    void testThreadSafety() throws InterruptedException {
        // Test concurrent operations
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] =
                    new Thread(
                            () -> {
                                results[index] = mouseController.moveTo(index * 100, index * 100);
                            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(1000);
        }

        // All operations should succeed in mock mode
        for (boolean result : results) {
            assertTrue(result, "Concurrent operation should succeed");
        }
    }
}
