package io.github.jspinak.brobot.tools.testing.exploration;

import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TestRun class, which represents a single test run session with metadata.
 */
@DisplayName("TestRun Tests")
class TestRunTest extends BrobotTestBase {

    private TestRun testRun;
    private String description;
    private LocalDateTime startTime;
    private Set<Long> startStates;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        description = "Test run description";
        startTime = LocalDateTime.now();
        startStates = Set.of(1L, 2L, 3L);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create TestRun with all required parameters")
        void shouldCreateTestRunWithRequiredParameters() {
            testRun = new TestRun(description, startTime, startStates);
            
            assertEquals(description, testRun.getDescription());
            assertEquals(startTime, testRun.getStartTime());
            assertEquals(startStates, testRun.getStartStates());
            assertNull(testRun.getEndTime());
            assertNull(testRun.getEndStates());
            assertNull(testRun.getRecordingFilename());
        }

        @Test
        @DisplayName("Should handle empty state set")
        void shouldHandleEmptyStateSet() {
            Set<Long> emptyStates = new HashSet<>();
            testRun = new TestRun(description, startTime, emptyStates);
            
            assertTrue(testRun.getStartStates().isEmpty());
        }

    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @BeforeEach
        void setup() {
            testRun = new TestRun(description, startTime, startStates);
        }

        @Test
        @DisplayName("Should set end time")
        void shouldSetEndTime() {
            LocalDateTime endTime = LocalDateTime.now().plusHours(1);
            testRun.setEndTime(endTime);
            
            assertEquals(endTime, testRun.getEndTime());
        }

        @Test
        @DisplayName("Should set end states")
        void shouldSetEndStates() {
            Set<Long> endStates = Set.of(4L, 5L, 6L);
            testRun.setEndStates(endStates);
            
            assertEquals(endStates, testRun.getEndStates());
        }

        @Test
        @DisplayName("Should set recording filename")
        void shouldSetRecordingFilename() {
            String filename = "test_recording_123.mp4";
            testRun.setRecordingFilename(filename);
            
            assertEquals(filename, testRun.getRecordingFilename());
        }

        @Test
        @DisplayName("Should update description")
        void shouldUpdateDescription() {
            String newDescription = "Updated description";
            testRun.setDescription(newDescription);
            
            assertEquals(newDescription, testRun.getDescription());
        }

        @Test
        @DisplayName("Should handle null values in setters")
        void shouldHandleNullValuesInSetters() {
            testRun.setEndTime(null);
            testRun.setEndStates(null);
            testRun.setRecordingFilename(null);
            
            assertNull(testRun.getEndTime());
            assertNull(testRun.getEndStates());
            assertNull(testRun.getRecordingFilename());
        }
    }

    @Nested
    @DisplayName("Complete Test Run Lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Should track complete test run lifecycle")
        void shouldTrackCompleteTestRunLifecycle() {
            // Start test run
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            Set<Long> initialStates = Set.of(1L);
            testRun = new TestRun("Comprehensive test", start, initialStates);
            
            // Simulate test execution
            LocalDateTime end = start.plusMinutes(30);
            Set<Long> finalStates = Set.of(1L, 2L, 3L, 4L, 5L);
            String recording = "test_run_2024-01-01_10-00-00.mp4";
            
            // Complete test run
            testRun.setEndTime(end);
            testRun.setEndStates(finalStates);
            testRun.setRecordingFilename(recording);
            
            // Verify complete state
            assertEquals("Comprehensive test", testRun.getDescription());
            assertEquals(start, testRun.getStartTime());
            assertEquals(end, testRun.getEndTime());
            assertEquals(initialStates, testRun.getStartStates());
            assertEquals(finalStates, testRun.getEndStates());
            assertEquals(recording, testRun.getRecordingFilename());
        }

        @Test
        @DisplayName("Should track state coverage improvement")
        void shouldTrackStateCoverageImprovement() {
            Set<Long> initial = Set.of(1L, 2L);
            testRun = new TestRun("Coverage test", startTime, initial);
            
            // After exploration, more states are active
            Set<Long> final_ = Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);
            testRun.setEndStates(final_);
            
            // Verify coverage improvement
            assertTrue(testRun.getEndStates().containsAll(testRun.getStartStates()));
            assertTrue(testRun.getEndStates().size() > testRun.getStartStates().size());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle same start and end states")
        void shouldHandleSameStartAndEndStates() {
            testRun = new TestRun(description, startTime, startStates);
            testRun.setEndStates(startStates);
            
            assertEquals(testRun.getStartStates(), testRun.getEndStates());
        }

        @Test
        @DisplayName("Should handle overlapping state sets")
        void shouldHandleOverlappingStateSets() {
            Set<Long> initial = Set.of(1L, 2L, 3L);
            Set<Long> final_ = Set.of(2L, 3L, 4L, 5L);
            
            testRun = new TestRun("Overlap test", startTime, initial);
            testRun.setEndStates(final_);
            
            // Calculate overlap
            Set<Long> overlap = new HashSet<>(initial);
            overlap.retainAll(final_);
            
            assertEquals(2, overlap.size());
            assertTrue(overlap.contains(2L));
            assertTrue(overlap.contains(3L));
        }

        @Test
        @DisplayName("Should handle completely different end states")
        void shouldHandleCompletelyDifferentEndStates() {
            Set<Long> initial = Set.of(1L, 2L, 3L);
            Set<Long> final_ = Set.of(10L, 11L, 12L);
            
            testRun = new TestRun("Different states test", startTime, initial);
            testRun.setEndStates(final_);
            
            // Verify no overlap
            Set<Long> overlap = new HashSet<>(initial);
            overlap.retainAll(final_);
            assertTrue(overlap.isEmpty());
        }
    }
}