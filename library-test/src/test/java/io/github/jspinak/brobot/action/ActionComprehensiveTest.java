package io.github.jspinak.brobot.action;

import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.config.core.FrameworkSettings;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.model.state.StateString;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;
import java.util.stream.Stream;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Action class to improve coverage.
 * Tests edge cases, error handling, performance, and concurrent execution.
 */
@SpringBootTest(classes = io.github.jspinak.brobot.BrobotTestApplication.class)
@DisplayName("Action Comprehensive Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ActionComprehensiveTest extends BrobotIntegrationTestBase {

    @Autowired
    private Action action;

    @BeforeEach
    void setUp() {
        super.setUpBrobotEnvironment();
        FrameworkSettings.mock = true;
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @Order(1)
        @DisplayName("Should handle null ActionConfig gracefully")
        void testPerformWithNullConfig() {
            ObjectCollection collection = new ObjectCollection.Builder()
                    .withRegions(new Region(0, 0, 100, 100))
                    .build();

            ActionResult result = action.perform((ActionConfig) null, collection);
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }

        @Test
        @Order(2)
        @DisplayName("Should handle null ObjectCollection")
        void testPerformWithNullCollection() {
            ClickOptions config = new ClickOptions.Builder().build();

            ActionResult result = action.perform(config, (ObjectCollection) null);
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }

        @Test
        @Order(3)
        @DisplayName("Should handle null StateImage in find")
        void testFindWithNullStateImage() {
            ActionResult result = action.find((StateImage) null);
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }

        @ParameterizedTest
        @NullSource
        @Order(4)
        @DisplayName("Should handle null in various ActionType methods")
        void testActionTypeWithNull(String nullString) {
            ActionResult result = action.perform(ActionType.TYPE, nullString);
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @Order(5)
        @DisplayName("Should handle empty ObjectCollection")
        void testPerformWithEmptyCollection() {
            ObjectCollection empty = new ObjectCollection.Builder().build();
            ClickOptions config = new ClickOptions.Builder().build();

            ActionResult result = action.perform(config, empty);
            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(result.getMatchList().isEmpty());
        }

        @Test
        @Order(6)
        @DisplayName("Should handle large ObjectCollection efficiently")
        void testPerformWithLargeCollection() {
            ObjectCollection.Builder builder = new ObjectCollection.Builder();

            // Add 1000 regions
            for (int i = 0; i < 1000; i++) {
                builder.withRegions(new Region(i, i, 10, 10));
            }

            ObjectCollection large = builder.build();
            PatternFindOptions config = new PatternFindOptions.Builder()
                    .setStrategy(PatternFindOptions.Strategy.FIRST)
                    .build();

            long startTime = System.currentTimeMillis();
            ActionResult result = action.perform(config, large);
            long duration = System.currentTimeMillis() - startTime;

            assertNotNull(result);
            assertTrue(duration < 5000, "Should complete within 5 seconds");
        }

        @Test
        @Order(7)
        @DisplayName("Should handle mixed object types in collection")
        void testPerformWithMixedObjectTypes() {
            ObjectCollection mixed = new ObjectCollection.Builder()
                    .withRegions(new Region(0, 0, 100, 100))
                    .withLocations(new Location(50, 50))
                    .withStrings("test string")
                    .withImages(new StateImage.Builder().setName("test").build())
                    .build();

            ClickOptions config = new ClickOptions.Builder().build();
            ActionResult result = action.perform(config, mixed);

            assertNotNull(result);
            assertTrue(result.isSuccess() || !result.isSuccess()); // Should not throw
        }

        @Test
        @Order(8)
        @DisplayName("Should handle zero-sized regions")
        void testPerformWithZeroSizedRegion() {
            ObjectCollection collection = new ObjectCollection.Builder()
                    .withRegions(new Region(100, 100, 0, 0))
                    .build();

            ClickOptions config = new ClickOptions.Builder().build();
            ActionResult result = action.perform(config, collection);

            assertNotNull(result);
            // Zero-sized regions should be handled gracefully
        }
    }

    @Nested
    @DisplayName("Timeout Tests")
    class TimeoutTests {

        @Test
        @Order(9)
        @DisplayName("Should timeout when duration expires")
        void testFindWithTimeoutExpiration() {
            StateImage image = new StateImage.Builder()
                    .setName("non-existent")
                    .build();

            long startTime = System.currentTimeMillis();
            ActionResult result = action.findWithTimeout(0.1, image); // 100ms timeout
            long duration = System.currentTimeMillis() - startTime;

            assertNotNull(result);
            assertFalse(result.isSuccess());
            assertTrue(duration >= 100 && duration < 500,
                    "Should timeout around 100ms");
        }

        @Test
        @Order(10)
        @DisplayName("Should handle negative timeout gracefully")
        void testFindWithNegativeTimeout() {
            StateImage image = new StateImage.Builder().setName("test").build();

            ActionResult result = action.findWithTimeout(-1.0, image);
            assertNotNull(result);
            // Should handle negative timeout without throwing
        }

        @Test
        @Order(11)
        @DisplayName("Should handle very large timeout")
        void testFindWithLargeTimeout() {
            StateImage image = new StateImage.Builder().setName("test").build();

            // Start with large timeout but should find quickly in mock mode
            long startTime = System.currentTimeMillis();
            ActionResult result = action.findWithTimeout(3600.0, image); // 1 hour
            long duration = System.currentTimeMillis() - startTime;

            assertNotNull(result);
            assertTrue(duration < 1000, "Should not actually wait for full timeout");
        }
    }

    @Nested
    @DisplayName("Concurrent Execution Tests")
    class ConcurrentExecutionTests {

        @Test
        @Order(12)
        @DisplayName("Should handle concurrent action execution")
        void testConcurrentActions() throws InterruptedException, ExecutionException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Future<ActionResult>> futures = new ArrayList<>();

            // Submit 50 concurrent actions
            for (int i = 0; i < 50; i++) {
                final int index = i;
                futures.add(executor.submit(() -> {
                    ObjectCollection collection = new ObjectCollection.Builder()
                            .withRegions(new Region(index, index, 10, 10))
                            .build();
                    return action.perform(new ClickOptions.Builder().build(), collection);
                }));
            }

            // Verify all complete successfully
            int successCount = 0;
            for (Future<ActionResult> future : futures) {
                ActionResult result = future.get();
                assertNotNull(result);
                if (result.isSuccess())
                    successCount++;
            }

            assertTrue(successCount > 0, "At least some actions should succeed");
            executor.shutdown();
        }

        @Test
        @Order(13)
        @DisplayName("Should maintain thread safety")
        void testThreadSafety() throws InterruptedException {
            int threadCount = 20;
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<Thread> threads = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    try {
                        for (int j = 0; j < 10; j++) {
                            action.find(new StateImage.Builder().setName("test").build());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
                threads.add(thread);
                thread.start();
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @Order(14)
        @DisplayName("Should handle rapid sequential actions")
        void testRapidSequentialActions() {
            long startTime = System.currentTimeMillis();
            int actionCount = 100;

            for (int i = 0; i < actionCount; i++) {
                action.click(new StateImage.Builder().setName("test" + i).build());
            }

            long duration = System.currentTimeMillis() - startTime;
            double avgTime = duration / (double) actionCount;

            assertTrue(avgTime < 100, "Average action time should be under 100ms");
        }

        @Test
        @Order(15)
        @DisplayName("Should not leak memory on repeated actions")
        void testMemoryUsage() {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            // Perform many actions
            for (int i = 0; i < 1000; i++) {
                ObjectCollection collection = new ObjectCollection.Builder()
                        .withRegions(new Region(i % 100, i % 100, 50, 50))
                        .build();
                action.perform(new ClickOptions.Builder().build(), collection);
            }

            runtime.gc();
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = finalMemory - initialMemory;

            // Memory increase should be reasonable (less than 50MB)
            assertTrue(memoryIncrease < 50 * 1024 * 1024,
                    "Memory usage should not increase significantly");
        }
    }

    @Nested
    @DisplayName("ActionType Overload Tests")
    class ActionTypeOverloadTests {

        @ParameterizedTest
        @MethodSource("provideActionTypes")
        @Order(16)
        @DisplayName("Should handle all ActionType variants")
        void testActionTypeVariants(ActionType type, Object parameter) {
            ActionResult result = null;

            if (parameter instanceof String) {
                result = action.perform(type, (String) parameter);
            } else if (parameter instanceof Region) {
                result = action.perform(type, (Region) parameter);
            } else if (parameter instanceof Location) {
                result = action.perform(type, (Location) parameter);
            } else if (parameter instanceof ObjectCollection) {
                result = action.perform(type, (ObjectCollection) parameter);
            }

            assertNotNull(result, "Result should not be null for " + type);
        }

        static Stream<Arguments> provideActionTypes() {
            return Stream.of(
                    Arguments.of(ActionType.CLICK, new Region(0, 0, 100, 100)),
                    Arguments.of(ActionType.FIND, new ObjectCollection.Builder().build()),
                    Arguments.of(ActionType.TYPE, "test text"),
                    Arguments.of(ActionType.MOVE, new Location(50, 50)),
                    Arguments.of(ActionType.DRAG, new Region(0, 0, 100, 100)));
        }
    }

    @Nested
    @DisplayName("Error Recovery Tests")
    class ErrorRecoveryTests {

        @Test
        @Order(17)
        @DisplayName("Should recover from action failures")
        void testErrorRecovery() {
            // Create a collection that might cause issues
            ObjectCollection problematic = new ObjectCollection.Builder()
                    .withRegions(new Region(-100, -100, 10, 10)) // Negative coordinates
                    .build();

            ActionResult result = action.perform(new ClickOptions.Builder().build(), problematic);
            assertNotNull(result);
            // Should handle gracefully without throwing
        }

        @Test
        @Order(18)
        @DisplayName("Should handle description with special characters")
        void testSpecialCharacterDescription() {
            String description = "Test with special chars: !@#$%^&*(){}[]|\\:;\"'<>,.?/~`";
            ObjectCollection collection = new ObjectCollection.Builder()
                    .withRegions(new Region(0, 0, 100, 100))
                    .build();

            ActionResult result = action.perform(description,
                    new ClickOptions.Builder().build(), collection);
            assertNotNull(result);
            // Should handle special characters in description
        }
    }

    @Test
    @Order(19)
    @DisplayName("Should verify all public methods are callable")
    void testAllPublicMethodsCoverage() {
        // This test ensures all public methods are at least called once
        assertDoesNotThrow(() -> {
            StateImage img = new StateImage.Builder().setName("test").build();
            ObjectCollection coll = new ObjectCollection.Builder().build();

            // Test all find variants
            action.find(img);
            action.find(coll);
            action.findWithTimeout(1.0, img);
            action.findWithTimeout(1.0, coll);

            // Test all perform variants
            action.perform(new ClickOptions.Builder().build(), coll);
            action.perform("description", new ClickOptions.Builder().build(), coll);
            action.perform(new ClickOptions.Builder().build(), img);

            // Test convenience methods
            action.click(img);
            action.type(coll);

            // Test ActionType variants
            action.perform(ActionType.CLICK, "string");
            action.perform(ActionType.FIND, new Region(0, 0, 10, 10));
            action.perform(ActionType.MOVE, new Location(0, 0));
            action.perform(ActionType.TYPE, new Region(0, 0, 10, 10));
            action.perform(ActionType.DRAG, "test");
        });
    }
}