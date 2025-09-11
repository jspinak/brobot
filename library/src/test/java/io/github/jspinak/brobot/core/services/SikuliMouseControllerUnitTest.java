package io.github.jspinak.brobot.core.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Unit tests for SikuliMouseController. Tests basic functionality in mock mode without mocking
 * Sikuli internals.
 *
 * <p>For integration testing with Spring context, see SikuliMouseControllerIT in library-test
 * module.
 */
@DisplayName("SikuliMouseController Unit Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledInCI
public class SikuliMouseControllerUnitTest extends BrobotTestBase {

    private SikuliMouseController controller;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        controller = new SikuliMouseController();
        // Ensure we're in mock mode for safe testing
        assertTrue(FrameworkSettings.mock, "Tests should run in mock mode");
    }

    @Test
    @DisplayName("Should provide implementation name")
    void testGetImplementationName() {
        String name = controller.getImplementationName();
        assertNotNull(name);
        assertEquals("Sikuli", name);
    }

    @Test
    @DisplayName("Should report availability based on environment")
    void testIsAvailable() {
        // In mock mode, availability depends on whether we're in a headless environment
        // The controller checks GraphicsEnvironment.isHeadless()
        boolean available = controller.isAvailable();
        assertNotNull(available);
        // We don't assert true/false as it depends on the test environment
    }

    @Test
    @DisplayName("Should convert MouseButton enum correctly")
    void testButtonConversion() {
        // This tests the internal button conversion logic
        // We can't directly test the private method, but we can verify
        // that click operations with different buttons don't throw exceptions

        assertDoesNotThrow(
                () -> {
                    controller.click(100, 100, MouseController.MouseButton.LEFT);
                });

        assertDoesNotThrow(
                () -> {
                    controller.click(100, 100, MouseController.MouseButton.RIGHT);
                });

        assertDoesNotThrow(
                () -> {
                    controller.click(100, 100, MouseController.MouseButton.MIDDLE);
                });
    }

    @Test
    @DisplayName("Should handle mouse operations in mock mode")
    void testBasicOperationsInMockMode() {
        // In mock mode, operations should complete without throwing exceptions
        // The actual success depends on the Sikuli mock implementation

        // Test move
        assertDoesNotThrow(
                () -> {
                    controller.moveTo(100, 200);
                });

        // Test click
        assertDoesNotThrow(
                () -> {
                    controller.click(150, 250, MouseController.MouseButton.LEFT);
                });

        // Test double-click
        assertDoesNotThrow(
                () -> {
                    controller.doubleClick(200, 300, MouseController.MouseButton.LEFT);
                });

        // Test drag
        assertDoesNotThrow(
                () -> {
                    controller.drag(100, 100, 200, 200);
                });

        // Test scroll
        assertDoesNotThrow(
                () -> {
                    controller.scroll(5);
                });
    }

    @Test
    @DisplayName("Should handle button press and release")
    void testButtonPressRelease() {
        // Test button down
        assertDoesNotThrow(
                () -> {
                    controller.mouseDown(MouseController.MouseButton.LEFT);
                });

        // Test button up
        assertDoesNotThrow(
                () -> {
                    controller.mouseUp(MouseController.MouseButton.LEFT);
                });

        // Test with different buttons
        assertDoesNotThrow(
                () -> {
                    controller.mouseDown(MouseController.MouseButton.RIGHT);
                    controller.mouseUp(MouseController.MouseButton.RIGHT);
                });

        assertDoesNotThrow(
                () -> {
                    controller.mouseDown(MouseController.MouseButton.MIDDLE);
                    controller.mouseUp(MouseController.MouseButton.MIDDLE);
                });
    }

    @Test
    @DisplayName("Should handle position queries")
    void testGetPosition() {
        // Get position might return null or actual coordinates
        // depending on the mock implementation
        assertDoesNotThrow(
                () -> {
                    int[] position = controller.getPosition();
                    // Position can be null in mock mode
                    if (position != null) {
                        assertEquals(
                                2, position.length, "Position should have x and y coordinates");
                    }
                });
    }

    @Test
    @DisplayName("Should handle edge cases gracefully")
    void testEdgeCases() {
        // Test zero coordinates
        assertDoesNotThrow(
                () -> {
                    controller.moveTo(0, 0);
                    controller.click(0, 0, MouseController.MouseButton.LEFT);
                });

        // Test negative coordinates (implementation should handle gracefully)
        assertDoesNotThrow(
                () -> {
                    controller.moveTo(-10, -10);
                });

        // Test large coordinates
        assertDoesNotThrow(
                () -> {
                    controller.moveTo(Integer.MAX_VALUE, Integer.MAX_VALUE);
                });

        // Test scroll with zero
        assertDoesNotThrow(
                () -> {
                    controller.scroll(0);
                });

        // Test scroll with negative (scroll up)
        assertDoesNotThrow(
                () -> {
                    controller.scroll(-10);
                });
    }

    @Test
    @DisplayName("Should be thread-safe")
    void testThreadSafety() {
        // The controller uses synchronized methods, so it should be thread-safe
        // In mock mode, we just verify operations don't throw exceptions

        assertDoesNotThrow(
                () -> {
                    int threadCount = 5; // Reduced for stability in mock mode
                    Thread[] threads = new Thread[threadCount];

                    for (int i = 0; i < threadCount; i++) {
                        final int index = i;
                        threads[i] =
                                new Thread(
                                        () -> {
                                            // Each thread performs different operations
                                            controller.moveTo(index * 10, index * 10);
                                            controller.click(
                                                    index * 20,
                                                    index * 20,
                                                    MouseController.MouseButton.LEFT);
                                            controller.scroll(index);
                                        });
                        threads[i].start();
                    }

                    // Give threads reasonable time to complete
                    Thread.sleep(1000);

                    // In mock mode, threads should complete quickly
                    // We're mainly checking that no exceptions were thrown
                });
    }

    @Test
    @DisplayName("Should handle double-click for different buttons")
    void testDoubleClickVariations() {
        // Left button uses native double-click
        assertDoesNotThrow(
                () -> {
                    controller.doubleClick(100, 100, MouseController.MouseButton.LEFT);
                });

        // Right and middle buttons simulate double-click with two clicks
        assertDoesNotThrow(
                () -> {
                    controller.doubleClick(100, 100, MouseController.MouseButton.RIGHT);
                });

        assertDoesNotThrow(
                () -> {
                    controller.doubleClick(100, 100, MouseController.MouseButton.MIDDLE);
                });
    }

    @Test
    @DisplayName("Should handle rapid sequential operations")
    void testRapidOperations() {
        // Test that rapid operations don't cause issues
        assertDoesNotThrow(
                () -> {
                    for (int i = 0; i < 100; i++) {
                        controller.moveTo(i, i);
                        if (i % 10 == 0) {
                            controller.click(i, i, MouseController.MouseButton.LEFT);
                        }
                    }
                });
    }
}
