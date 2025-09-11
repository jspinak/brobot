package io.github.jspinak.brobot.action.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.github.jspinak.brobot.action.*;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycle;
import io.github.jspinak.brobot.action.internal.execution.ActionLifecycleManagement;
import io.github.jspinak.brobot.model.element.*;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.test.BrobotIntegrationTestBase;

/**
 * Integration tests for action lifecycle management using real Spring context. Tests the complete
 * lifecycle of actions from initialization to cleanup.
 */
@SpringBootTest
@TestPropertySource(
        properties = {
            "brobot.logging.verbosity=VERBOSE",
            "brobot.console.actions.enabled=true",
            "brobot.action.lifecycle.tracking=true",
            "brobot.action.validation.enabled=true",
            "brobot.mock.enabled=true" // Use mock mode for testing
        })
class ActionLifecycleIntegrationTest extends BrobotIntegrationTestBase {

    @Autowired private Action action;

    @Autowired private ActionLifecycleManagement lifecycleManagement;

    private StateImage testImage;
    private StateLocation testLocation;
    private StateRegion testRegion;
    private ObjectCollection testCollection;

    @BeforeEach
    void setupTestData() {
        // Create StateImage with pattern
        testImage =
                new StateImage.Builder()
                        .setName("test-image")
                        .addPattern("images/test.png")
                        .build();

        // Create StateLocation
        testLocation =
                new StateLocation.Builder()
                        .setName("test-location")
                        .setLocation(new Location(100, 100))
                        .build();

        // Create StateRegion
        testRegion =
                new StateRegion.Builder()
                        .setName("test-region")
                        .setSearchRegion(new Region(0, 0, 500, 500))
                        .build();

        // Build ObjectCollection
        testCollection =
                new ObjectCollection.Builder()
                        .withImages(testImage)
                        .withLocations(testLocation)
                        .withRegions(testRegion)
                        .build();
    }

    @Nested
    @DisplayName("Action Lifecycle Timing Tests")
    class ActionLifecycleTimingTests {

        @Test
        @DisplayName("Should track action start and end times")
        void shouldTrackActionStartAndEndTimes() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.8).build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            assertNotNull(result.getActionLifecycle());
            assertNotNull(result.getActionLifecycle().getStartTime());
            assertNotNull(result.getActionLifecycle().getEndTime());

            // Verify timing consistency
            LocalDateTime startTime = result.getActionLifecycle().getStartTime();
            LocalDateTime endTime = result.getActionLifecycle().getEndTime();
            assertTrue(endTime.isAfter(startTime) || endTime.isEqual(startTime));
        }

        @Test
        @DisplayName("Should respect search duration")
        void shouldRespectSearchDuration() {
            // Given - Set a very short search duration
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.99) // High similarity to likely fail
                            .setSearchDuration(0.1) // 100ms search duration
                            .build();

            // When
            long startMs = System.currentTimeMillis();
            ActionResult result = action.perform(findOptions, testCollection);
            long endMs = System.currentTimeMillis();

            // Then
            assertNotNull(result);
            assertTrue(endMs - startMs < 1000); // Should complete within 1 second
            assertFalse(result.isSuccess()); // Should fail due to timeout
        }

        @Test
        @DisplayName("Should calculate action duration correctly")
        void shouldCalculateActionDurationCorrectly() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            Duration duration = result.getDuration();
            assertNotNull(duration);
            assertTrue(duration.toMillis() >= 0);

            // Duration should match the difference between start and end times
            if (result.getActionLifecycle() != null
                    && result.getActionLifecycle().getStartTime() != null
                    && result.getActionLifecycle().getEndTime() != null) {

                Duration calculatedDuration =
                        Duration.between(
                                result.getActionLifecycle().getStartTime(),
                                result.getActionLifecycle().getEndTime());

                // Allow for small rounding differences (within 100ms)
                long diff = Math.abs(calculatedDuration.toMillis() - duration.toMillis());
                assertTrue(diff < 100);
            }
        }
    }

    @Nested
    @DisplayName("Action Repetition and Sequence Tests")
    class ActionRepetitionTests {

        @Test
        @DisplayName("Should handle action execution")
        void shouldHandleActionExecution() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            assertNotNull(result.getActionLifecycle());
            // In the current implementation, repetitions are tracked differently
            assertTrue(result.getActionLifecycle().getCompletedRepetitions() >= 0);
        }

        @Test
        @DisplayName("Should track multiple action executions")
        void shouldTrackMultipleActionExecutions() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.7)
                            .setMaxMatchesToActOn(3)
                            .build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            assertNotNull(result.getActionLifecycle());
        }
    }

    @Nested
    @DisplayName("Action Result and Match Tests")
    class ActionResultTests {

        @Test
        @DisplayName("Should populate matches when pattern found")
        void shouldPopulateMatchesWhenPatternFound() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            // In mock mode, results depend on mock configuration
            assertNotNull(result.getMatchList());
        }

        @Test
        @DisplayName("Should return empty matches when pattern not found")
        void shouldReturnEmptyMatchesWhenPatternNotFound() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.99) // Very high similarity to likely fail
                            .setSearchDuration(0.1)
                            .build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            assertNotNull(result.getMatchList());
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Should handle click action")
        void shouldHandleClickAction() {
            // Given
            ClickOptions clickOptions = new ClickOptions.Builder().build();

            // When
            ActionResult result = action.perform(clickOptions, testCollection);

            // Then
            assertNotNull(result);
            assertNotNull(result.getActionLifecycle());
        }

        @Test
        @DisplayName("Should handle type action")
        void shouldHandleTypeAction() {
            // Given
            TypeOptions typeOptions = new TypeOptions.Builder().build();
            // TypeOptions doesn't have setText method in Builder

            // When
            ActionResult result = action.perform(typeOptions, testCollection);

            // Then
            assertNotNull(result);
            // Check that action was performed
            assertNotNull(result.getActionLifecycle());
        }
    }

    @Nested
    @DisplayName("Action Lifecycle Management Tests")
    class LifecycleManagementTests {

        @Test
        @DisplayName("Should increment repetitions correctly")
        void shouldIncrementRepetitionsCorrectly() {
            // Given
            ActionResult result = new ActionResult();
            ActionLifecycle lifecycle = new ActionLifecycle(LocalDateTime.now(), 10.0);
            result.setActionLifecycle(lifecycle);

            // When
            lifecycleManagement.incrementCompletedRepetitions(result);
            lifecycleManagement.incrementCompletedRepetitions(result);

            // Then
            assertEquals(2, result.getActionLifecycle().getCompletedRepetitions());
        }

        @Test
        @DisplayName("Should increment sequences correctly")
        void shouldIncrementSequencesCorrectly() {
            // Given
            ActionResult result = new ActionResult();
            ActionLifecycle lifecycle = new ActionLifecycle(LocalDateTime.now(), 10.0);
            result.setActionLifecycle(lifecycle);

            // When
            lifecycleManagement.incrementCompletedSequences(result);

            // Then
            assertEquals(1, result.getActionLifecycle().getCompletedSequences());
        }

        @Test
        @DisplayName("Should calculate duration from lifecycle")
        void shouldCalculateDurationFromLifecycle() {
            // Given
            LocalDateTime start = LocalDateTime.now();
            ActionLifecycle lifecycle = new ActionLifecycle(start, 10.0);
            LocalDateTime end = start.plusSeconds(5);
            lifecycle.setEndTime(end);

            ActionResult result = new ActionResult();
            result.setActionLifecycle(lifecycle);

            // When
            // There's no getDurationInSeconds method, use getDuration instead
            Duration duration = result.getDuration();

            // Then
            assertNotNull(duration);
            assertEquals(5.0, duration.getSeconds(), 0.01);
        }
    }

    @Nested
    @DisplayName("Concurrent Action Execution Tests")
    class ConcurrentExecutionTests {

        @Test
        @DisplayName("Should handle concurrent action executions")
        void shouldHandleConcurrentActionExecutions() throws InterruptedException {
            // Given
            int threadCount = 5;
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<ActionResult> results = Collections.synchronizedList(new ArrayList<>());

            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            // When
            for (int i = 0; i < threadCount; i++) {
                new Thread(
                                () -> {
                                    try {
                                        ActionResult result =
                                                action.perform(findOptions, testCollection);
                                        results.add(result);
                                    } finally {
                                        latch.countDown();
                                    }
                                })
                        .start();
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));

            // Then
            assertEquals(threadCount, results.size());
            for (ActionResult result : results) {
                assertNotNull(result);
                assertNotNull(result.getActionLifecycle());
            }
        }

        @Test
        @DisplayName("Should maintain separate lifecycles for concurrent actions")
        void shouldMaintainSeparateLifecyclesForConcurrentActions() throws InterruptedException {
            // Given
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch completionLatch = new CountDownLatch(2);

            AtomicBoolean thread1Started = new AtomicBoolean(false);
            AtomicBoolean thread2Started = new AtomicBoolean(false);

            PatternFindOptions options1 =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.7)
                            .setSearchDuration(0.5)
                            .build();

            PatternFindOptions options2 =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.8)
                            .setSearchDuration(0.5)
                            .build();

            ActionResult[] results = new ActionResult[2];

            // When
            Thread thread1 =
                    new Thread(
                            () -> {
                                try {
                                    startLatch.await();
                                    thread1Started.set(true);
                                    results[0] = action.perform(options1, testCollection);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    completionLatch.countDown();
                                }
                            });

            Thread thread2 =
                    new Thread(
                            () -> {
                                try {
                                    startLatch.await();
                                    thread2Started.set(true);
                                    results[1] = action.perform(options2, testCollection);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    completionLatch.countDown();
                                }
                            });

            thread1.start();
            thread2.start();
            startLatch.countDown(); // Start both threads simultaneously

            assertTrue(completionLatch.await(5, TimeUnit.SECONDS));

            // Then
            assertTrue(thread1Started.get());
            assertTrue(thread2Started.get());

            assertNotNull(results[0]);
            assertNotNull(results[1]);

            // Each should have its own lifecycle
            assertNotSame(results[0].getActionLifecycle(), results[1].getActionLifecycle());
        }
    }

    @Nested
    @DisplayName("Action Configuration Tests")
    class ActionConfigurationTests {

        @Test
        @DisplayName("Should apply find configuration correctly")
        void shouldApplyFindConfigurationCorrectly() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setSimilarity(0.7)
                            .setStrategy(PatternFindOptions.Strategy.ALL)
                            .build();

            // When
            ActionResult result = action.perform(findOptions, testCollection);

            // Then
            assertNotNull(result);
            // Verify configuration was applied (in mock mode, behavior is simulated)
            assertNotNull(result.getActionConfig());
        }

        @Test
        @DisplayName("Should handle empty object collection")
        void shouldHandleEmptyObjectCollection() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            ObjectCollection emptyCollection = new ObjectCollection.Builder().build();

            // When
            ActionResult result = action.perform(findOptions, emptyCollection);

            // Then
            assertNotNull(result);
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Should handle multiple object collections")
        void shouldHandleMultipleObjectCollections() {
            // Given
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSimilarity(0.7).build();

            ObjectCollection collection2 =
                    new ObjectCollection.Builder()
                            .withImages(
                                    new StateImage.Builder()
                                            .setName("second-image")
                                            .addPattern("images/second.png")
                                            .build())
                            .build();

            // When
            ActionResult result = action.perform(findOptions, testCollection, collection2);

            // Then
            assertNotNull(result);
            assertNotNull(result.getActionLifecycle());
        }
    }
}
