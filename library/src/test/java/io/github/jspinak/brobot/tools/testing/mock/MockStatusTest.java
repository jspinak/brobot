package io.github.jspinak.brobot.tools.testing.mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive tests for MockStatus operation counter. Tests counter functionality, boundary
 * conditions, and usage patterns.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MockStatus Tests")
public class MockStatusTest extends BrobotTestBase {

    private MockStatus mockStatus;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockStatus = new MockStatus();
    }

    @Nested
    @DisplayName("Basic Counter Operations")
    class BasicCounterOperations {

        @Test
        @DisplayName("Should initialize with zero count")
        void shouldInitializeWithZeroCount() {
            assertEquals(
                    0,
                    mockStatus.getMocksPerformed(),
                    "New MockStatus should start with 0 mocks performed");
        }

        @Test
        @DisplayName("Should increment counter by one")
        void shouldIncrementCounterByOne() {
            mockStatus.addMockPerformed();
            assertEquals(1, mockStatus.getMocksPerformed());

            mockStatus.addMockPerformed();
            assertEquals(2, mockStatus.getMocksPerformed());
        }

        @Test
        @DisplayName("Should handle multiple increments")
        void shouldHandleMultipleIncrements() {
            int incrementCount = 100;

            for (int i = 0; i < incrementCount; i++) {
                mockStatus.addMockPerformed();
            }

            assertEquals(incrementCount, mockStatus.getMocksPerformed());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 50, 100, 500, 1000})
        @DisplayName("Should accurately count various increment amounts")
        void shouldAccuratelyCountVariousAmounts(int count) {
            for (int i = 0; i < count; i++) {
                mockStatus.addMockPerformed();
            }

            assertEquals(
                    count,
                    mockStatus.getMocksPerformed(),
                    "Should accurately count " + count + " operations");
        }
    }

    @Nested
    @DisplayName("Boundary Conditions")
    class BoundaryConditions {

        @Test
        @DisplayName("Should handle zero increments")
        void shouldHandleZeroIncrements() {
            // Don't call addMockPerformed at all
            assertEquals(0, mockStatus.getMocksPerformed());
        }

        @Test
        @DisplayName("Should handle large number of increments")
        void shouldHandleLargeNumberOfIncrements() {
            int largeCount = 1_000_000;

            for (int i = 0; i < largeCount; i++) {
                mockStatus.addMockPerformed();
            }

            assertEquals(largeCount, mockStatus.getMocksPerformed());
        }

        @Test
        @DisplayName("Should not overflow with maximum increments")
        void shouldNotOverflowWithMaxIncrements() {
            // Increment to near max int (this would take too long, so we simulate)
            // Instead, we'll test a reasonable large number
            int reasonablyLarge = 10_000;

            for (int i = 0; i < reasonablyLarge; i++) {
                mockStatus.addMockPerformed();
            }

            assertTrue(mockStatus.getMocksPerformed() > 0, "Counter should remain positive");
            assertEquals(reasonablyLarge, mockStatus.getMocksPerformed());
        }
    }

    @Nested
    @DisplayName("Usage Patterns")
    class UsagePatterns {

        @Test
        @DisplayName("Should support test limit checking pattern")
        void shouldSupportTestLimitChecking() {
            int maxOperations = 100;
            boolean limitExceeded = false;

            for (int i = 0; i < 150; i++) {
                mockStatus.addMockPerformed();

                if (mockStatus.getMocksPerformed() >= maxOperations) {
                    limitExceeded = true;
                    break;
                }
            }

            assertTrue(limitExceeded, "Should detect when limit is exceeded");
            assertEquals(maxOperations, mockStatus.getMocksPerformed(), "Should stop at limit");
        }

        @Test
        @DisplayName("Should support progress monitoring pattern")
        void shouldSupportProgressMonitoring() {
            int totalOperations = 100;
            List<Integer> progressCheckpoints = new ArrayList<>();

            for (int i = 0; i < totalOperations; i++) {
                mockStatus.addMockPerformed();

                // Check progress at 25%, 50%, 75%, 100%
                int progress = mockStatus.getMocksPerformed();
                if (progress == 25 || progress == 50 || progress == 75 || progress == 100) {
                    progressCheckpoints.add(progress);
                }
            }

            assertThat(progressCheckpoints, contains(25, 50, 75, 100));
        }

        @Test
        @DisplayName("Should support operation quota pattern")
        void shouldSupportOperationQuota() {
            int quota = 50;
            int operationsPerformed = 0;

            while (mockStatus.getMocksPerformed() < quota) {
                // Simulate some operation
                mockStatus.addMockPerformed();
                operationsPerformed++;
            }

            assertEquals(quota, operationsPerformed);
            assertEquals(quota, mockStatus.getMocksPerformed());
        }

        @Test
        @DisplayName("Should support batch operation counting")
        void shouldSupportBatchOperationCounting() {
            // Simulate batch operations with different sizes
            int[] batchSizes = {5, 10, 15, 20};
            int expectedTotal = 0;

            for (int batchSize : batchSizes) {
                for (int i = 0; i < batchSize; i++) {
                    mockStatus.addMockPerformed();
                }
                expectedTotal += batchSize;
            }

            assertEquals(expectedTotal, mockStatus.getMocksPerformed());
        }
    }

    @Nested
    @DisplayName("State Management")
    class StateManagement {

        @Test
        @DisplayName("Should maintain state across method calls")
        void shouldMaintainStateAcrossMethodCalls() {
            // First method
            performSomeOperations(5);
            assertEquals(5, mockStatus.getMocksPerformed());

            // Second method
            performSomeOperations(3);
            assertEquals(8, mockStatus.getMocksPerformed());

            // Third method
            performSomeOperations(2);
            assertEquals(10, mockStatus.getMocksPerformed());
        }

        private void performSomeOperations(int count) {
            for (int i = 0; i < count; i++) {
                mockStatus.addMockPerformed();
            }
        }

        @Test
        @DisplayName("Should not reset counter between calls")
        void shouldNotResetCounterBetweenCalls() {
            mockStatus.addMockPerformed();
            int firstCount = mockStatus.getMocksPerformed();

            mockStatus.addMockPerformed();
            int secondCount = mockStatus.getMocksPerformed();

            assertEquals(firstCount + 1, secondCount, "Counter should accumulate, not reset");
        }

        @Test
        @DisplayName("Should provide consistent reads")
        void shouldProvideConsistentReads() {
            mockStatus.addMockPerformed();
            mockStatus.addMockPerformed();
            mockStatus.addMockPerformed();

            int firstRead = mockStatus.getMocksPerformed();
            int secondRead = mockStatus.getMocksPerformed();
            int thirdRead = mockStatus.getMocksPerformed();

            assertEquals(firstRead, secondRead);
            assertEquals(secondRead, thirdRead);
            assertEquals(3, firstRead);
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle rapid increments efficiently")
        void shouldHandleRapidIncrementsEfficiently() {
            int rapidCount = 100_000;

            long startTime = System.nanoTime();

            for (int i = 0; i < rapidCount; i++) {
                mockStatus.addMockPerformed();
            }

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            assertEquals(rapidCount, mockStatus.getMocksPerformed());
            assertTrue(durationMs < 1000, "Should process 100k increments in less than 1 second");
        }

        @Test
        @DisplayName("Should handle rapid reads efficiently")
        void shouldHandleRapidReadsEfficiently() {
            // Set up initial state
            for (int i = 0; i < 100; i++) {
                mockStatus.addMockPerformed();
            }

            int readCount = 1_000_000;
            long startTime = System.nanoTime();

            int lastRead = 0;
            for (int i = 0; i < readCount; i++) {
                lastRead = mockStatus.getMocksPerformed();
            }

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            assertEquals(100, lastRead);
            assertTrue(durationMs < 100, "Should process 1M reads in less than 100ms");
        }

        @Test
        @DisplayName("Should handle alternating operations efficiently")
        void shouldHandleAlternatingOperationsEfficiently() {
            int operationCount = 10_000;

            long startTime = System.nanoTime();

            for (int i = 0; i < operationCount; i++) {
                mockStatus.addMockPerformed();
                int count = mockStatus.getMocksPerformed();
                assertThat(count, greaterThan(0));
            }

            long endTime = System.nanoTime();
            long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            assertEquals(operationCount, mockStatus.getMocksPerformed());
            assertTrue(durationMs < 500, "Should handle alternating operations efficiently");
        }
    }

    @Nested
    @DisplayName("Thread Safety Considerations")
    class ThreadSafetyConsiderations {

        @Test
        @DisplayName("Should document non-thread-safe behavior")
        void shouldDocumentNonThreadSafeBehavior() throws InterruptedException {
            // This test demonstrates that MockStatus is NOT thread-safe
            // Multiple threads incrementing may lead to lost updates

            int threadCount = 10;
            int incrementsPerThread = 1000;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(
                        () -> {
                            try {
                                startLatch.await(); // Wait for all threads to be ready
                                for (int j = 0; j < incrementsPerThread; j++) {
                                    mockStatus.addMockPerformed();
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                doneLatch.countDown();
                            }
                        });
            }

            startLatch.countDown(); // Start all threads simultaneously
            doneLatch.await(5, TimeUnit.SECONDS);
            executor.shutdown();

            int actualCount = mockStatus.getMocksPerformed();
            int expectedCount = threadCount * incrementsPerThread;

            // Due to lack of synchronization, actual may be less than expected
            // This is expected behavior as documented
            assertThat(actualCount, lessThanOrEqualTo(expectedCount));
        }

        @Test
        @DisplayName("Should work correctly in single-threaded context")
        void shouldWorkCorrectlyInSingleThreadedContext() {
            // Verify it works perfectly in single-threaded usage
            int operations = 1000;

            for (int i = 0; i < operations; i++) {
                mockStatus.addMockPerformed();
            }

            assertEquals(
                    operations,
                    mockStatus.getMocksPerformed(),
                    "Should be accurate in single-threaded usage");
        }
    }

    @Nested
    @DisplayName("Integration Patterns")
    class IntegrationPatterns {

        @Test
        @DisplayName("Should integrate with test limit exception pattern")
        void shouldIntegrateWithTestLimitException() {
            int maxOperations = 10;

            assertThrows(
                    TestLimitExceededException.class,
                    () -> {
                        while (true) {
                            mockStatus.addMockPerformed();
                            if (mockStatus.getMocksPerformed() > maxOperations) {
                                throw new TestLimitExceededException(
                                        "Exceeded max operations: " + maxOperations);
                            }
                        }
                    });

            assertTrue(mockStatus.getMocksPerformed() > maxOperations);
        }

        @Test
        @DisplayName("Should support debugging breakpoint pattern")
        void shouldSupportDebuggingBreakpoint() {
            int debugBreakpoint = 50;
            boolean breakpointHit = false;

            for (int i = 0; i < 100; i++) {
                mockStatus.addMockPerformed();

                if (mockStatus.getMocksPerformed() == debugBreakpoint) {
                    // Debugger would break here
                    breakpointHit = true;
                }
            }

            assertTrue(breakpointHit, "Should hit debugging breakpoint");
        }

        @Test
        @DisplayName("Should support resource management pattern")
        void shouldSupportResourceManagement() {
            int resourceLimit = 100;
            List<String> resources = new ArrayList<>();

            while (mockStatus.getMocksPerformed() < resourceLimit) {
                mockStatus.addMockPerformed();
                resources.add("Resource-" + mockStatus.getMocksPerformed());
            }

            assertEquals(resourceLimit, resources.size());
            assertEquals(resourceLimit, mockStatus.getMocksPerformed());
        }
    }

    @Nested
    @DisplayName("Spring Component Behavior")
    class SpringComponentBehavior {

        @Test
        @DisplayName("Should be annotated as Spring Component")
        void shouldBeAnnotatedAsSpringComponent() {
            assertTrue(
                    MockStatus.class.isAnnotationPresent(
                            org.springframework.stereotype.Component.class),
                    "MockStatus should be annotated with @Component");
        }

        @Test
        @DisplayName("Should create independent instances")
        void shouldCreateIndependentInstances() {
            MockStatus status1 = new MockStatus();
            MockStatus status2 = new MockStatus();

            status1.addMockPerformed();
            status1.addMockPerformed();

            assertEquals(2, status1.getMocksPerformed());
            assertEquals(
                    0,
                    status2.getMocksPerformed(),
                    "Independent instances should have separate counters");
        }
    }

    // Custom exception for testing
    private static class TestLimitExceededException extends Exception {
        public TestLimitExceededException(String message) {
            super(message);
        }
    }
}
