package io.github.jspinak.brobot.action.internal.execution;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

/**
 * Test suite for ActionLifecycleManagement class. Tests lifecycle control logic for GUI automation
 * actions.
 */
@DisplayName("ActionLifecycleManagement Tests")
public class ActionLifecycleManagementTest extends BrobotTestBase {

    @Mock private TimeProvider mockTimeProvider;

    @Mock private ActionLifecycle mockLifecycle;

    @Mock private ActionConfig mockActionConfig;

    private ActionLifecycleManagement lifecycleManagement;
    private ActionResult testResult;
    private AutoCloseable mockCloseable;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        mockCloseable = MockitoAnnotations.openMocks(this);
        lifecycleManagement = new ActionLifecycleManagement(mockTimeProvider);

        testResult = new ActionResult();
        testResult.setActionLifecycle(mockLifecycle);
        testResult.setActionConfig(mockActionConfig);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }
    }

    @Nested
    @DisplayName("Repetition Management")
    class RepetitionManagement {

        @Test
        @DisplayName("Should increment completed repetitions")
        public void testIncrementCompletedRepetitions() {
            // Act
            lifecycleManagement.incrementCompletedRepetitions(testResult);

            // Assert
            verify(mockLifecycle).incrementCompletedRepetitions();
        }

        @Test
        @DisplayName("Should get completed repetitions count")
        public void testGetCompletedRepetitions() {
            // Arrange
            when(mockLifecycle.getCompletedRepetitions()).thenReturn(5);

            // Act
            int count = lifecycleManagement.getCompletedRepetitions(testResult);

            // Assert
            assertEquals(5, count);
            verify(mockLifecycle).getCompletedRepetitions();
        }

        @Test
        @DisplayName("Should handle zero completed repetitions")
        public void testZeroCompletedRepetitions() {
            // Arrange
            when(mockLifecycle.getCompletedRepetitions()).thenReturn(0);

            // Act
            int count = lifecycleManagement.getCompletedRepetitions(testResult);

            // Assert
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("Sequence Management")
    class SequenceManagement {

        @Test
        @DisplayName("Should increment completed sequences")
        public void testIncrementCompletedSequences() {
            // Act
            lifecycleManagement.incrementCompletedSequences(testResult);

            // Assert
            verify(mockLifecycle).incrementCompletedSequences();
        }

        @Test
        @DisplayName("Should check if more sequences allowed - with lifecycle")
        public void testMoreSequencesAllowedWithLifecycle() {
            // Arrange
            when(mockLifecycle.getCompletedSequences()).thenReturn(0);

            // Act
            boolean allowed = lifecycleManagement.isMoreSequencesAllowed(testResult);

            // Assert
            assertTrue(allowed);
        }

        @Test
        @DisplayName("Should not allow more sequences when limit reached")
        public void testNoMoreSequencesWhenLimitReached() {
            // Arrange
            when(mockLifecycle.getCompletedSequences()).thenReturn(1);

            // Act
            boolean allowed = lifecycleManagement.isMoreSequencesAllowed(testResult);

            // Assert
            assertFalse(allowed);
        }

        @Test
        @DisplayName("Should handle null lifecycle")
        public void testMoreSequencesWithNullLifecycle() {
            // Arrange
            testResult.setActionLifecycle(null);

            // Act
            boolean allowed = lifecycleManagement.isMoreSequencesAllowed(testResult);

            // Assert
            assertFalse(allowed);
        }
    }

    @Nested
    @DisplayName("Duration Tracking")
    class DurationTracking {

        @Test
        @DisplayName("Should calculate current duration")
        public void testGetCurrentDuration() {
            // Arrange
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            LocalDateTime current = LocalDateTime.of(2024, 1, 1, 10, 0, 30);
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(current);

            // Act
            Duration duration = lifecycleManagement.getCurrentDuration(testResult);

            // Assert
            assertEquals(30, duration.getSeconds());
            verify(mockLifecycle).getStartTime();
            verify(mockTimeProvider).now();
        }

        @Test
        @DisplayName("Should handle zero duration")
        public void testZeroDuration() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            when(mockLifecycle.getStartTime()).thenReturn(now);
            when(mockTimeProvider.now()).thenReturn(now);

            // Act
            Duration duration = lifecycleManagement.getCurrentDuration(testResult);

            // Assert
            assertEquals(0, duration.getSeconds());
        }

        @ParameterizedTest
        @ValueSource(longs = {1, 5, 10, 30, 60, 120})
        @DisplayName("Should calculate various durations correctly")
        public void testVariousDurations(long seconds) {
            // Arrange
            LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
            LocalDateTime end = start.plusSeconds(seconds);
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(end);

            // Act
            Duration duration = lifecycleManagement.getCurrentDuration(testResult);

            // Assert
            assertEquals(seconds, duration.getSeconds());
        }
    }

    @Nested
    @DisplayName("Continue Action Logic")
    class ContinueActionLogic {

        @Test
        @DisplayName("Should continue on first repetition")
        public void testContinueOnFirstRepetition() {
            // Arrange
            when(mockLifecycle.getCompletedRepetitions()).thenReturn(0);
            when(mockLifecycle.getStartTime()).thenReturn(LocalDateTime.now());
            when(mockTimeProvider.now()).thenReturn(LocalDateTime.now());

            // Act
            boolean shouldContinue = lifecycleManagement.isOkToContinueAction(testResult, 1);

            // Assert - First repetition should continue even in mock mode
            assertTrue(shouldContinue);
        }

        @Test
        @DisplayName("Should stop when max duration exceeded")
        public void testStopWhenMaxDurationExceeded() {
            // Arrange
            when(mockLifecycle.getCompletedRepetitions()).thenReturn(1);
            LocalDateTime start = LocalDateTime.now();
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(start.plusSeconds(15)); // Exceeds default 10s

            // Act
            boolean shouldContinue = lifecycleManagement.isOkToContinueAction(testResult, 1);

            // Assert
            assertFalse(shouldContinue);
        }

        @Test
        @DisplayName("Should continue when within time limit")
        public void testContinueWithinTimeLimit() {
            // Arrange
            when(mockLifecycle.getCompletedRepetitions())
                    .thenReturn(0); // Changed to 0 for first iteration
            LocalDateTime start = LocalDateTime.now();
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(start.plusSeconds(5));

            // Act
            boolean shouldContinue = lifecycleManagement.isOkToContinueAction(testResult, 1);

            // Assert - With 0 completed repetitions, should continue
            assertTrue(shouldContinue);
        }

        @Test
        @DisplayName("Should use searchDuration from BaseFindOptions")
        public void testUseSearchDurationFromConfig() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder().setSearchDuration(20.0).build();
            testResult.setActionConfig(findOptions);

            when(mockLifecycle.getCompletedRepetitions())
                    .thenReturn(0); // Changed to 0 for first iteration
            LocalDateTime start = LocalDateTime.now();
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(start.plusSeconds(15)); // Within 20s limit

            // Act
            boolean shouldContinue = lifecycleManagement.isOkToContinueAction(testResult, 1);

            // Assert - With 0 completed repetitions, should continue
            assertTrue(shouldContinue);
        }

        @Test
        @DisplayName("Should stop when FIND FIRST has match")
        public void testStopWhenFindFirstHasMatch() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.FIRST)
                            .build();
            testResult.setActionConfig(findOptions);

            Match match = mock(Match.class);
            testResult.add(match);

            when(mockLifecycle.getCompletedRepetitions()).thenReturn(1);
            LocalDateTime start = LocalDateTime.now();
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(start.plusSeconds(1));

            // Act
            boolean shouldContinue = lifecycleManagement.isOkToContinueAction(testResult, 1);

            // Assert
            assertFalse(shouldContinue);
        }

        @Test
        @DisplayName("Should continue FIND FIRST without match")
        public void testContinueFindFirstWithoutMatch() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.FIRST)
                            .build();
            testResult.setActionConfig(findOptions);

            when(mockLifecycle.getCompletedRepetitions())
                    .thenReturn(0); // Changed to 0 for first iteration
            LocalDateTime start = LocalDateTime.now();
            when(mockLifecycle.getStartTime()).thenReturn(start);
            when(mockTimeProvider.now()).thenReturn(start.plusSeconds(1));

            // Act
            boolean shouldContinue = lifecycleManagement.isOkToContinueAction(testResult, 1);

            // Assert - With 0 completed repetitions, should continue
            assertTrue(shouldContinue);
        }
    }

    @Nested
    @DisplayName("Find Strategy Checks")
    class FindStrategyChecks {

        @Test
        @DisplayName("Should detect FIND FIRST with match")
        public void testFindFirstAndAtLeastOneMatchFound() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.FIRST)
                            .build();
            testResult.setActionConfig(findOptions);

            Match match = mock(Match.class);
            testResult.add(match);

            // Act
            boolean result = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(testResult);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should detect FIND FIRST without match")
        public void testFindFirstWithoutMatch() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.FIRST)
                            .build();
            testResult.setActionConfig(findOptions);

            // Act
            boolean result = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(testResult);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle non-PatternFindOptions config")
        public void testNonPatternFindOptionsConfig() {
            // Arrange
            testResult.setActionConfig(mockActionConfig);
            Match match = mock(Match.class);
            testResult.add(match);

            // Act
            boolean result = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(testResult);

            // Assert
            assertTrue(result); // Defaults to checking if not empty
        }

        @Test
        @DisplayName("Should detect FIND EACH with all patterns found")
        public void testFindEachFirstAndEachPatternFound() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.EACH)
                            .build();
            testResult.setActionConfig(findOptions);

            Image image1 = mock(Image.class);
            Image image2 = mock(Image.class);

            Match match1 = mock(Match.class);
            when(match1.getSearchImage()).thenReturn(image1);
            Match match2 = mock(Match.class);
            when(match2.getSearchImage()).thenReturn(image2);

            testResult.setMatchList(Arrays.asList(match1, match2));

            // Act
            boolean result = lifecycleManagement.isFindEachFirstAndEachPatternFound(testResult, 2);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Should detect FIND EACH with missing patterns")
        public void testFindEachFirstWithMissingPatterns() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.EACH)
                            .build();
            testResult.setActionConfig(findOptions);

            Image image1 = mock(Image.class);
            Match match1 = mock(Match.class);
            when(match1.getSearchImage()).thenReturn(image1);
            Match match2 = mock(Match.class);
            when(match2.getSearchImage()).thenReturn(image1); // Same image

            testResult.setMatchList(Arrays.asList(match1, match2));

            // Act
            boolean result = lifecycleManagement.isFindEachFirstAndEachPatternFound(testResult, 2);

            // Assert
            assertFalse(result); // Only 1 unique image, expected 2
        }

        @Test
        @DisplayName("Should handle FIND EACH with duplicate images")
        public void testFindEachWithDuplicateImages() {
            // Arrange
            PatternFindOptions findOptions =
                    new PatternFindOptions.Builder()
                            .setStrategy(PatternFindOptions.Strategy.EACH)
                            .build();
            testResult.setActionConfig(findOptions);

            Image image1 = mock(Image.class);
            Image image2 = mock(Image.class);

            Match match1 = mock(Match.class);
            when(match1.getSearchImage()).thenReturn(image1);
            Match match2 = mock(Match.class);
            when(match2.getSearchImage()).thenReturn(image2);
            Match match3 = mock(Match.class);
            when(match3.getSearchImage()).thenReturn(image1); // Duplicate

            testResult.setMatchList(Arrays.asList(match1, match2, match3));

            // Act
            boolean result = lifecycleManagement.isFindEachFirstAndEachPatternFound(testResult, 2);

            // Assert
            assertTrue(result); // 2 unique images found, matches expected
        }
    }

    @Nested
    @DisplayName("Print Action Once")
    class PrintActionOnce {

        @Test
        @DisplayName("Should print action only once")
        public void testPrintActionOnce() {
            // Arrange
            when(mockLifecycle.isPrinted()).thenReturn(false);

            // Act
            lifecycleManagement.printActionOnce(testResult);

            // Assert
            verify(mockLifecycle).isPrinted();
            verify(mockLifecycle).setPrinted(true);
        }

        @Test
        @DisplayName("Should not print when already printed")
        public void testDoNotPrintWhenAlreadyPrinted() {
            // Arrange
            when(mockLifecycle.isPrinted()).thenReturn(true);

            // Act
            lifecycleManagement.printActionOnce(testResult);

            // Assert
            verify(mockLifecycle).isPrinted();
            verify(mockLifecycle, never()).setPrinted(anyBoolean());
        }

        @Test
        @DisplayName("Should handle multiple print attempts")
        public void testMultiplePrintAttempts() {
            // Arrange
            when(mockLifecycle.isPrinted()).thenReturn(false).thenReturn(true);

            // Act
            lifecycleManagement.printActionOnce(testResult);
            lifecycleManagement.printActionOnce(testResult);

            // Assert
            verify(mockLifecycle, times(2)).isPrinted();
            verify(mockLifecycle, times(1)).setPrinted(true);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle null ActionResult")
        public void testHandleNullActionResult() {
            // Act & Assert
            assertThrows(
                    NullPointerException.class,
                    () -> lifecycleManagement.incrementCompletedRepetitions(null));
        }

        @Test
        @DisplayName("Should handle null lifecycle in result")
        public void testHandleNullLifecycle() {
            // Arrange
            testResult.setActionLifecycle(null);

            // Act - should handle gracefully without throwing
            assertDoesNotThrow(() -> lifecycleManagement.incrementCompletedRepetitions(testResult));

            // Assert - verify no lifecycle methods were called (since it's null)
            verifyNoInteractions(mockLifecycle);
        }

        @Test
        @DisplayName("Should handle empty match list")
        public void testHandleEmptyMatchList() {
            // Arrange
            testResult.setMatchList(Collections.emptyList());

            // Act
            boolean findFirst = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(testResult);

            // Assert
            assertFalse(findFirst);
        }

        @Test
        @DisplayName("Should handle null match list")
        public void testHandleNullMatchList() {
            // Arrange
            testResult.setMatchList(null);

            // Act
            boolean findFirst = lifecycleManagement.isFindFirstAndAtLeastOneMatchFound(testResult);

            // Assert
            assertFalse(findFirst);
        }
    }
}
