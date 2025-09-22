package io.github.jspinak.brobot.action.internal.mouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.basic.mouse.ScrollOptions;
import io.github.jspinak.brobot.config.core.BrobotProperties;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.test.DisabledInCI;

/**
 * Comprehensive test suite for MouseWheelScroller - provides mouse wheel scrolling functionality.
 * Tests scrolling in both real and mock modes with various configurations.
 */
@DisplayName("MouseWheelScroller Tests")
@DisabledInCI
public class MouseWheelScrollerTest extends BrobotTestBase {

    private MouseWheelScroller mouseWheelScroller;
    private BrobotProperties mockBrobotProperties;
    private BrobotProperties.Core mockCore;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);

        // Setup mock BrobotProperties
        mockBrobotProperties = mock(BrobotProperties.class);
        mockCore = mock(BrobotProperties.Core.class);
        when(mockBrobotProperties.getCore()).thenReturn(mockCore);
        when(mockCore.isMock()).thenReturn(true);  // Default to mock mode

        mouseWheelScroller = new MouseWheelScroller(mockBrobotProperties, null);
    }

    @AfterEach
    public void tearDown() {
        // Cleanup
    }

    @Nested
    @DisplayName("Mock Mode Scrolling")
    class MockModeScrolling {

        private PrintStream originalOut;

        @BeforeEach
        public void setupMockMode() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            // Save original output stream
            originalOut = System.out;
        }

        @AfterEach
        public void tearDown() {
            // Restore original output stream
            System.setOut(originalOut);
        }

        @Test
        @DisplayName("Scroll up in mock mode")
        public void testScrollUpMockMode() {
            // Create fresh output stream for this test
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));

            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(3)
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
            String output = outputStream.toString();
            assertTrue(output.contains("Mock: scroll UP 3 times."), "Expected output to contain 'Mock: scroll UP 3 times.' but got: " + output);
        }

        @Test
        @DisplayName("Scroll down in mock mode")
        public void testScrollDownMockMode() {
            // Create fresh output stream for this test
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));

            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(5)
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
            String output = outputStream.toString();
            assertTrue(output.contains("Mock: scroll DOWN 5 times."), "Expected output to contain 'Mock: scroll DOWN 5 times.' but got: " + output);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 10, 20})
        @DisplayName("Various scroll steps in mock mode")
        public void testVariousScrollSteps(int steps) {
            // Create fresh output stream for this test
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));

            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(steps)
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
            String output = outputStream.toString();
            assertTrue(output.contains("Mock: scroll UP " + steps + " times."), "Expected output to contain 'Mock: scroll UP " + steps + " times.' but got: " + output);
        }

        @ParameterizedTest
        @EnumSource(ScrollOptions.Direction.class)
        @DisplayName("All scroll directions in mock mode")
        public void testAllDirections(ScrollOptions.Direction direction) {
            // Create fresh output stream for this test
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputStream));

            ScrollOptions options =
                    new ScrollOptions.Builder().setDirection(direction).setScrollSteps(1).build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
            String output = outputStream.toString();
            assertTrue(output.contains("Mock: scroll " + direction), "Expected output to contain 'Mock: scroll " + direction + "' but got: " + output);
        }
    }

    @Nested
    @DisplayName("Real Mode Scrolling")
    class RealModeScrolling {

        @BeforeEach
        public void setupRealMode() {
            // Set mock mode to false for real mode tests
            when(mockCore.isMock()).thenReturn(false);
        }

        @Test
        @DisplayName("Scroll up in real mode")
        public void testScrollUpRealMode() {
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(2)
                            .build();

            try (MockedConstruction<Region> mockedRegion =
                    mockConstruction(
                            Region.class,
                            (mock, context) -> {
                                org.sikuli.script.Region sikuliRegion =
                                        mock(org.sikuli.script.Region.class);
                                when(mock.sikuli()).thenReturn(sikuliRegion);
                            })) {

                boolean result = mouseWheelScroller.scroll(options);

                assertTrue(result);
                assertEquals(1, mockedRegion.constructed().size(), "Should create one Region in real mode");
                Region region = mockedRegion.constructed().get(0);
                verify(region.sikuli()).wheel(-1, 2); // UP = -1
            }
        }

        @Test
        @DisplayName("Scroll down in real mode")
        public void testScrollDownRealMode() {
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(4)
                            .build();

            try (MockedConstruction<Region> mockedRegion =
                    mockConstruction(
                            Region.class,
                            (mock, context) -> {
                                org.sikuli.script.Region sikuliRegion =
                                        mock(org.sikuli.script.Region.class);
                                when(mock.sikuli()).thenReturn(sikuliRegion);
                            })) {

                boolean result = mouseWheelScroller.scroll(options);

                assertTrue(result);
                assertEquals(1, mockedRegion.constructed().size(), "Should create one Region in real mode");
                Region region = mockedRegion.constructed().get(0);
                verify(region.sikuli()).wheel(1, 4); // DOWN = 1
            }
        }

        @ParameterizedTest
        @CsvSource({"UP, 1, -1", "UP, 5, -1", "DOWN, 1, 1", "DOWN, 10, 1"})
        @DisplayName("Direction conversion to Sikuli values")
        public void testDirectionConversion(
                ScrollOptions.Direction direction, int steps, int expectedSikuliDirection) {
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(direction)
                            .setScrollSteps(steps)
                            .build();

            try (MockedConstruction<Region> mockedRegion =
                    mockConstruction(
                            Region.class,
                            (mock, context) -> {
                                org.sikuli.script.Region sikuliRegion =
                                        mock(org.sikuli.script.Region.class);
                                when(mock.sikuli()).thenReturn(sikuliRegion);
                            })) {

                boolean result = mouseWheelScroller.scroll(options);

                assertTrue(result);
                assertEquals(1, mockedRegion.constructed().size(), "Should create one Region in real mode");
                Region region = mockedRegion.constructed().get(0);
                verify(region.sikuli()).wheel(expectedSikuliDirection, steps);
            }
        }
    }

    @Nested
    @DisplayName("Scroll Options Configuration")
    class ScrollOptionsConfiguration {

        @Test
        @DisplayName("Default scroll options")
        public void testDefaultScrollOptions() {
            ScrollOptions options = new ScrollOptions.Builder().build();

            assertNotNull(options);
            assertNotNull(options.getDirection());
            assertTrue(options.getScrollSteps() > 0);
        }

        @Test
        @DisplayName("Custom scroll options")
        public void testCustomScrollOptions() {
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(7)
                            .build();

            assertEquals(ScrollOptions.Direction.DOWN, options.getDirection());
            assertEquals(7, options.getScrollSteps());
        }

        @Test
        @DisplayName("Zero scroll steps")
        public void testZeroScrollSteps() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(0)
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result); // Should handle gracefully
        }

        @Test
        @DisplayName("Negative scroll steps")
        public void testNegativeScrollSteps() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(-5)
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result); // Should handle gracefully
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Very large scroll steps")
        public void testVeryLargeScrollSteps() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(Integer.MAX_VALUE)
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
        }

        @Test
        @DisplayName("Rapid consecutive scrolls")
        public void testRapidConsecutiveScrolls() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions upOptions =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(2)
                            .build();
            ScrollOptions downOptions =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(2)
                            .build();

            // Simulate rapid scrolling
            for (int i = 0; i < 10; i++) {
                assertTrue(mouseWheelScroller.scroll(upOptions));
                assertTrue(mouseWheelScroller.scroll(downOptions));
            }
        }

        @Test
        @DisplayName("Always returns true")
        public void testAlwaysReturnsTrue() {
            // In both mock and real mode, the method always returns true
            // Test mock mode
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options = new ScrollOptions.Builder().build();
            assertTrue(mouseWheelScroller.scroll(options));

            // Test real mode
            when(mockCore.isMock()).thenReturn(false);

            try (MockedConstruction<Region> mockedRegion =
                    mockConstruction(
                            Region.class,
                            (mock, context) -> {
                                org.sikuli.script.Region sikuliRegion =
                                        mock(org.sikuli.script.Region.class);
                                when(mock.sikuli()).thenReturn(sikuliRegion);
                            })) {
                assertTrue(mouseWheelScroller.scroll(options));
            }
        }
    }

    @Nested
    @DisplayName("Use Case Scenarios")
    class UseCaseScenarios {

        @Test
        @DisplayName("Scroll to bottom of page")
        public void testScrollToBottom() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(20) // Large number to reach bottom
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
        }

        @Test
        @DisplayName("Scroll to top of page")
        public void testScrollToTop() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(20) // Large number to reach top
                            .build();

            boolean result = mouseWheelScroller.scroll(options);

            assertTrue(result);
        }

        @Test
        @DisplayName("Fine-grained scrolling")
        public void testFineGrainedScrolling() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);
            ScrollOptions options =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(1) // Single step for precise control
                            .build();

            // Perform multiple fine-grained scrolls
            for (int i = 0; i < 5; i++) {
                assertTrue(mouseWheelScroller.scroll(options));
            }
        }

        @Test
        @DisplayName("Page navigation with scrolling")
        public void testPageNavigation() {
            // Ensure mock mode is enabled
            when(mockCore.isMock()).thenReturn(true);

            // Scroll down to find content
            ScrollOptions scrollDown =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.DOWN)
                            .setScrollSteps(5)
                            .build();
            assertTrue(mouseWheelScroller.scroll(scrollDown));

            // Scroll back up
            ScrollOptions scrollUp =
                    new ScrollOptions.Builder()
                            .setDirection(ScrollOptions.Direction.UP)
                            .setScrollSteps(5)
                            .build();
            assertTrue(mouseWheelScroller.scroll(scrollUp));
        }
    }
}
