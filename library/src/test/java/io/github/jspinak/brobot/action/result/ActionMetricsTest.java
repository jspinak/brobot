package io.github.jspinak.brobot.action.result;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Unit tests for ActionMetrics class. Tests metrics collection and analysis for action execution.
 */
@DisplayName("ActionMetrics Tests")
public class ActionMetricsTest extends BrobotTestBase {

    private ActionMetrics metrics;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        metrics = new ActionMetrics();
    }

    @Nested
    @DisplayName("Initialization")
    class Initialization {

        @Test
        @DisplayName("Should initialize with current thread name")
        public void testDefaultInitialization() {
            ActionMetrics newMetrics = new ActionMetrics();

            assertNotNull(newMetrics.getThreadName());
            assertEquals(Thread.currentThread().getName(), newMetrics.getThreadName());
            assertEquals(0, newMetrics.getExecutionTimeMs());
            assertEquals(0, newMetrics.getMatchCount());
            assertEquals(0.0, newMetrics.getBestMatchConfidence());
            assertEquals(0, newMetrics.getRetryCount());
            assertEquals(0, newMetrics.getRetryTimeMs());
            assertTrue(newMetrics.getPhaseTimings().isEmpty());
            assertTrue(newMetrics.getPhaseCounts().isEmpty());
        }

        @Test
        @DisplayName("Should initialize with action ID")
        public void testInitializationWithActionId() {
            String actionId = "test-action-123";
            ActionMetrics newMetrics = new ActionMetrics(actionId);

            assertEquals(actionId, newMetrics.getActionId());
            assertNotNull(newMetrics.getThreadName());
        }
    }

    @Nested
    @DisplayName("Execution Time Recording")
    class ExecutionTimeRecording {

        @Test
        @DisplayName("Should record execution time from Duration")
        public void testRecordExecutionTimeFromDuration() {
            Duration duration = Duration.ofMillis(500);
            metrics.recordExecutionTime(duration);

            assertEquals(500, metrics.getExecutionTimeMs());
        }

        @Test
        @DisplayName("Should record execution time in milliseconds")
        public void testRecordExecutionTimeInMillis() {
            metrics.recordExecutionTime(1234L);

            assertEquals(1234, metrics.getExecutionTimeMs());
        }

        @Test
        @DisplayName("Should record execution time from instants")
        public void testRecordExecutionTimeFromInstants() {
            Instant start = Instant.now();
            Instant end = start.plusMillis(750);

            metrics.recordExecutionTime(start, end);

            assertEquals(750, metrics.getExecutionTimeMs());
        }

        @Test
        @DisplayName("Should handle null Duration")
        public void testRecordNullDuration() {
            metrics.recordExecutionTime((Duration) null);

            assertEquals(0, metrics.getExecutionTimeMs());
        }

        @Test
        @DisplayName("Should handle null instants")
        public void testRecordNullInstants() {
            metrics.recordExecutionTime(null, Instant.now());
            assertEquals(0, metrics.getExecutionTimeMs());

            metrics.recordExecutionTime(Instant.now(), null);
            assertEquals(0, metrics.getExecutionTimeMs());

            metrics.recordExecutionTime((Instant) null, null);
            assertEquals(0, metrics.getExecutionTimeMs());
        }
    }

    @Nested
    @DisplayName("Retry Recording")
    class RetryRecording {

        @Test
        @DisplayName("Should record single retry")
        public void testRecordSingleRetry() {
            Duration retryDuration = Duration.ofMillis(100);
            metrics.recordRetry(retryDuration);

            assertEquals(1, metrics.getRetryCount());
            assertEquals(100, metrics.getRetryTimeMs());
        }

        @Test
        @DisplayName("Should accumulate multiple retries")
        public void testRecordMultipleRetries() {
            metrics.recordRetry(Duration.ofMillis(100));
            metrics.recordRetry(Duration.ofMillis(200));
            metrics.recordRetry(Duration.ofMillis(150));

            assertEquals(3, metrics.getRetryCount());
            assertEquals(450, metrics.getRetryTimeMs());
        }

        @Test
        @DisplayName("Should handle null retry duration")
        public void testRecordNullRetryDuration() {
            metrics.recordRetry(null);

            assertEquals(1, metrics.getRetryCount());
            assertEquals(0, metrics.getRetryTimeMs());
        }

        @Test
        @DisplayName("Should count retries without duration")
        public void testRecordRetriesWithoutDuration() {
            metrics.recordRetry(null);
            metrics.recordRetry(Duration.ofMillis(100));
            metrics.recordRetry(null);

            assertEquals(3, metrics.getRetryCount());
            assertEquals(100, metrics.getRetryTimeMs());
        }
    }

    @Nested
    @DisplayName("Phase Timing")
    class PhaseTiming {

        @Test
        @DisplayName("Should record phase timing with Duration")
        public void testRecordPhaseWithDuration() {
            String phaseName = "initialization";
            Duration duration = Duration.ofMillis(250);

            metrics.recordPhase(phaseName, duration);

            assertEquals(250, metrics.getTotalPhaseTime(phaseName));
            assertEquals(1, metrics.getPhaseCount(phaseName));
            assertEquals(250, metrics.getAveragePhaseTime(phaseName));
        }

        @Test
        @DisplayName("Should record phase timing in milliseconds")
        public void testRecordPhaseInMillis() {
            String phaseName = "processing";
            metrics.recordPhase(phaseName, 350L);

            assertEquals(350, metrics.getTotalPhaseTime(phaseName));
            assertEquals(1, metrics.getPhaseCount(phaseName));
            assertEquals(350, metrics.getAveragePhaseTime(phaseName));
        }

        @Test
        @DisplayName("Should accumulate multiple phase recordings")
        public void testAccumulatePhaseTimings() {
            String phaseName = "validation";

            metrics.recordPhase(phaseName, Duration.ofMillis(100));
            metrics.recordPhase(phaseName, Duration.ofMillis(200));
            metrics.recordPhase(phaseName, Duration.ofMillis(150));

            assertEquals(450, metrics.getTotalPhaseTime(phaseName));
            assertEquals(3, metrics.getPhaseCount(phaseName));
            assertEquals(150, metrics.getAveragePhaseTime(phaseName));
        }

        @Test
        @DisplayName("Should track multiple phases separately")
        public void testMultiplePhases() {
            metrics.recordPhase("phase1", Duration.ofMillis(100));
            metrics.recordPhase("phase2", Duration.ofMillis(200));
            metrics.recordPhase("phase1", Duration.ofMillis(150));
            metrics.recordPhase("phase3", Duration.ofMillis(300));

            assertEquals(250, metrics.getTotalPhaseTime("phase1"));
            assertEquals(200, metrics.getTotalPhaseTime("phase2"));
            assertEquals(300, metrics.getTotalPhaseTime("phase3"));

            assertEquals(2, metrics.getPhaseCount("phase1"));
            assertEquals(1, metrics.getPhaseCount("phase2"));
            assertEquals(1, metrics.getPhaseCount("phase3"));

            assertEquals(125, metrics.getAveragePhaseTime("phase1"));
            assertEquals(200, metrics.getAveragePhaseTime("phase2"));
            assertEquals(300, metrics.getAveragePhaseTime("phase3"));
        }

        @Test
        @DisplayName("Should handle null phase name")
        public void testNullPhaseName() {
            metrics.recordPhase(null, Duration.ofMillis(100));
            metrics.recordPhase(null, 200L);

            assertEquals(0, metrics.getTotalPhaseTime(null));
            assertEquals(0, metrics.getPhaseCount(null));
            assertEquals(0, metrics.getAveragePhaseTime(null));
            assertTrue(metrics.getPhaseTimings().isEmpty());
        }

        @Test
        @DisplayName("Should handle null duration")
        public void testNullPhaseDuration() {
            metrics.recordPhase("phase", null);

            assertEquals(0, metrics.getTotalPhaseTime("phase"));
            assertEquals(0, metrics.getPhaseCount("phase"));
            assertTrue(metrics.getPhaseTimings().isEmpty());
        }

        @Test
        @DisplayName("Should return zero for non-existent phase")
        public void testNonExistentPhase() {
            assertEquals(0, metrics.getTotalPhaseTime("nonexistent"));
            assertEquals(0, metrics.getPhaseCount("nonexistent"));
            assertEquals(0, metrics.getAveragePhaseTime("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Match Metrics")
    class MatchMetrics {

        @Test
        @DisplayName("Should set and get match count")
        public void testMatchCount() {
            metrics.setMatchCount(5);
            assertEquals(5, metrics.getMatchCount());

            metrics.setMatchCount(10);
            assertEquals(10, metrics.getMatchCount());
        }

        @Test
        @DisplayName("Should set and get best match confidence")
        public void testBestMatchConfidence() {
            metrics.setBestMatchConfidence(0.95);
            assertEquals(0.95, metrics.getBestMatchConfidence(), 0.001);

            metrics.setBestMatchConfidence(0.88);
            assertEquals(0.88, metrics.getBestMatchConfidence(), 0.001);
        }
    }

    @Nested
    @DisplayName("Thread and Action Identification")
    class Identification {

        @Test
        @DisplayName("Should track thread name")
        public void testThreadName() {
            String currentThread = Thread.currentThread().getName();
            assertEquals(currentThread, metrics.getThreadName());

            // Set different thread name
            metrics.setThreadName("worker-thread-1");
            assertEquals("worker-thread-1", metrics.getThreadName());
        }

        @Test
        @DisplayName("Should track action ID")
        public void testActionId() {
            assertNull(metrics.getActionId());

            metrics.setActionId("action-456");
            assertEquals("action-456", metrics.getActionId());
        }
    }

    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {

        @Test
        @DisplayName("Should handle complete action workflow metrics")
        public void testCompleteWorkflow() {
            // Set action identity
            metrics.setActionId("find-button-123");

            // Record initialization phase
            metrics.recordPhase("init", Duration.ofMillis(50));

            // Record search phase with retries
            metrics.recordPhase("search", Duration.ofMillis(200));
            metrics.recordRetry(Duration.ofMillis(100));
            metrics.recordPhase("search", Duration.ofMillis(150));
            metrics.recordRetry(Duration.ofMillis(80));
            metrics.recordPhase("search", Duration.ofMillis(120));

            // Record post-processing
            metrics.recordPhase("post-process", Duration.ofMillis(30));

            // Record overall execution time
            Instant start = Instant.now();
            Instant end = start.plusMillis(630);
            metrics.recordExecutionTime(start, end);

            // Set match results
            metrics.setMatchCount(3);
            metrics.setBestMatchConfidence(0.92);

            // Verify all metrics
            assertEquals("find-button-123", metrics.getActionId());
            assertEquals(630, metrics.getExecutionTimeMs());
            assertEquals(2, metrics.getRetryCount());
            assertEquals(180, metrics.getRetryTimeMs());
            assertEquals(3, metrics.getMatchCount());
            assertEquals(0.92, metrics.getBestMatchConfidence(), 0.001);

            // Verify phase metrics
            assertEquals(50, metrics.getTotalPhaseTime("init"));
            assertEquals(470, metrics.getTotalPhaseTime("search"));
            assertEquals(30, metrics.getTotalPhaseTime("post-process"));

            assertEquals(1, metrics.getPhaseCount("init"));
            assertEquals(3, metrics.getPhaseCount("search"));
            assertEquals(1, metrics.getPhaseCount("post-process"));

            assertEquals(50, metrics.getAveragePhaseTime("init"));
            assertEquals(156, metrics.getAveragePhaseTime("search")); // 470/3 ≈ 156
            assertEquals(30, metrics.getAveragePhaseTime("post-process"));
        }

        @Test
        @DisplayName("Should calculate metrics accurately with mixed input types")
        public void testMixedInputTypes() {
            // Mix Duration and milliseconds for phase recording
            metrics.recordPhase("phase1", Duration.ofMillis(100));
            metrics.recordPhase("phase1", 200L);
            metrics.recordPhase("phase1", Duration.ofMillis(150));

            assertEquals(450, metrics.getTotalPhaseTime("phase1"));
            assertEquals(3, metrics.getPhaseCount("phase1"));
            assertEquals(150, metrics.getAveragePhaseTime("phase1"));

            // Mix retry recording methods
            metrics.recordRetry(Duration.ofMillis(50));
            metrics.recordRetry(null);
            metrics.recordRetry(Duration.ofMillis(75));

            assertEquals(3, metrics.getRetryCount());
            assertEquals(125, metrics.getRetryTimeMs());
        }
    }
}
