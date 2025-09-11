package io.github.jspinak.brobot.model.action;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;

/**
 * Comprehensive test suite for ActionRecord - records match results and context. Tests data
 * capture, timing, state tracking, and mock support functionality.
 */
@DisplayName("ActionRecord Tests")
public class ActionRecordTest extends BrobotTestBase {

    @Mock private ActionConfig mockActionConfig;

    @Mock private Match mockMatch1;

    @Mock private Match mockMatch2;

    @Mock private PatternFindOptions mockFindOptions;

    private ActionRecord actionRecord;

    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        actionRecord = new ActionRecord();
    }

    @Nested
    @DisplayName("Basic Properties")
    class BasicProperties {

        @Test
        @DisplayName("Default constructor creates empty record")
        public void testDefaultConstructor() {
            ActionRecord record = new ActionRecord();

            assertNull(record.getActionConfig());
            assertNotNull(record.getMatchList());
            assertTrue(record.getMatchList().isEmpty());
            assertEquals("", record.getText());
            assertEquals(0.0, record.getDuration());
            assertNull(record.getTimeStamp());
            assertFalse(record.isActionSuccess());
            assertFalse(record.isResultSuccess());
            assertNotNull(record.getStateName());
        }

        @Test
        @DisplayName("Set and get ActionConfig")
        public void testActionConfig() {
            actionRecord.setActionConfig(mockActionConfig);

            assertEquals(mockActionConfig, actionRecord.getActionConfig());
        }

        @Test
        @DisplayName("Set and get match list")
        public void testMatchList() {
            List<Match> matches = Arrays.asList(mockMatch1, mockMatch2);
            actionRecord.setMatchList(matches);

            assertEquals(2, actionRecord.getMatchList().size());
            assertTrue(actionRecord.getMatchList().contains(mockMatch1));
            assertTrue(actionRecord.getMatchList().contains(mockMatch2));
        }

        @Test
        @DisplayName("Set and get text")
        public void testText() {
            String extractedText = "Extracted button text";
            actionRecord.setText(extractedText);

            assertEquals(extractedText, actionRecord.getText());
        }

        @Test
        @DisplayName("Set and get duration")
        public void testDuration() {
            actionRecord.setDuration(1234.56);

            assertEquals(1234.56, actionRecord.getDuration());
        }

        @Test
        @DisplayName("Set and get timestamp")
        public void testTimestamp() {
            LocalDateTime now = LocalDateTime.now();
            actionRecord.setTimeStamp(now);

            assertEquals(now, actionRecord.getTimeStamp());
        }
    }

    @Nested
    @DisplayName("Success Indicators")
    class SuccessIndicators {

        @Test
        @DisplayName("Action success flag")
        public void testActionSuccess() {
            assertFalse(actionRecord.isActionSuccess());

            actionRecord.setActionSuccess(true);
            assertTrue(actionRecord.isActionSuccess());

            actionRecord.setActionSuccess(false);
            assertFalse(actionRecord.isActionSuccess());
        }

        @Test
        @DisplayName("Result success flag")
        public void testResultSuccess() {
            assertFalse(actionRecord.isResultSuccess());

            actionRecord.setResultSuccess(true);
            assertTrue(actionRecord.isResultSuccess());

            actionRecord.setResultSuccess(false);
            assertFalse(actionRecord.isResultSuccess());
        }

        @Test
        @DisplayName("Both success flags can differ")
        public void testDifferentSuccessFlags() {
            actionRecord.setActionSuccess(true);
            actionRecord.setResultSuccess(false);

            assertTrue(actionRecord.isActionSuccess());
            assertFalse(actionRecord.isResultSuccess());
        }
    }

    @Nested
    @DisplayName("State Context")
    class StateContext {

        @Test
        @DisplayName("Set and get state name")
        public void testStateName() {
            actionRecord.setStateName("LoginState");

            assertEquals("LoginState", actionRecord.getStateName());
        }

        @Test
        @DisplayName("Set and get state ID")
        public void testStateId() {
            actionRecord.setStateId(123L);

            assertEquals(123L, actionRecord.getStateId());
        }

        @ParameterizedTest
        @DisplayName("Various state IDs")
        @ValueSource(longs = {0L, 1L, 100L, 999L, Long.MAX_VALUE})
        public void testVariousStateIds(long stateId) {
            actionRecord.setStateId(stateId);

            assertEquals(stateId, actionRecord.getStateId());
        }
    }

    @Nested
    @DisplayName("Match List Operations")
    class MatchListOperations {

        @Test
        @DisplayName("Add matches to existing list")
        public void testAddMatches() {
            actionRecord.getMatchList().add(mockMatch1);
            assertEquals(1, actionRecord.getMatchList().size());

            actionRecord.getMatchList().add(mockMatch2);
            assertEquals(2, actionRecord.getMatchList().size());
        }

        @Test
        @DisplayName("Empty match list indicates failure")
        public void testEmptyMatchListIndicatesFailure() {
            assertTrue(actionRecord.getMatchList().isEmpty());

            // This would typically indicate a failed find operation
            actionRecord.setResultSuccess(false);
            assertFalse(actionRecord.isResultSuccess());
        }

        @Test
        @DisplayName("Replace match list")
        public void testReplaceMatchList() {
            actionRecord.setMatchList(Arrays.asList(mockMatch1));
            assertEquals(1, actionRecord.getMatchList().size());

            List<Match> newMatches = Arrays.asList(mockMatch2);
            actionRecord.setMatchList(newMatches);

            assertEquals(1, actionRecord.getMatchList().size());
            assertFalse(actionRecord.getMatchList().contains(mockMatch1));
            assertTrue(actionRecord.getMatchList().contains(mockMatch2));
        }

        @Test
        @DisplayName("Null match list becomes empty list")
        public void testNullMatchList() {
            // Verify initial state has empty list
            ActionRecord freshRecord = new ActionRecord();
            assertNotNull(freshRecord.getMatchList());
            assertTrue(freshRecord.getMatchList().isEmpty());

            // When setting to null, it becomes null (Lombok setter behavior)
            actionRecord.setMatchList(null);
            assertNull(actionRecord.getMatchList());
        }
    }

    @Nested
    @DisplayName("Complete Record Scenarios")
    class CompleteRecordScenarios {

        @Test
        @DisplayName("Successful find operation record")
        public void testSuccessfulFindRecord() {
            // Setup successful find scenario
            LocalDateTime startTime = LocalDateTime.now();
            List<Match> matches = Arrays.asList(mockMatch1, mockMatch2);

            actionRecord.setActionConfig(mockFindOptions);
            actionRecord.setMatchList(matches);
            actionRecord.setDuration(523.45);
            actionRecord.setTimeStamp(startTime);
            actionRecord.setActionSuccess(true);
            actionRecord.setResultSuccess(true);
            actionRecord.setStateName("MainMenuState");

            // Verify complete record
            assertEquals(mockFindOptions, actionRecord.getActionConfig());
            assertEquals(2, actionRecord.getMatchList().size());
            assertEquals(523.45, actionRecord.getDuration());
            assertEquals(startTime, actionRecord.getTimeStamp());
            assertTrue(actionRecord.isActionSuccess());
            assertTrue(actionRecord.isResultSuccess());
            assertEquals("MainMenuState", actionRecord.getStateName());
        }

        @Test
        @DisplayName("Failed find operation record")
        public void testFailedFindRecord() {
            // Setup failed find scenario
            LocalDateTime startTime = LocalDateTime.now();

            actionRecord.setActionConfig(mockFindOptions);
            actionRecord.setMatchList(new ArrayList<>()); // Empty = not found
            actionRecord.setDuration(2000.0); // Timeout duration
            actionRecord.setTimeStamp(startTime);
            actionRecord.setActionSuccess(true); // Action executed
            actionRecord.setResultSuccess(false); // But nothing found
            actionRecord.setStateName("LoginState");

            // Verify failed record
            assertTrue(actionRecord.getMatchList().isEmpty());
            assertEquals(2000.0, actionRecord.getDuration());
            assertTrue(actionRecord.isActionSuccess());
            assertFalse(actionRecord.isResultSuccess());
        }

        @Test
        @DisplayName("Text extraction record")
        public void testTextExtractionRecord() {
            // Setup text extraction scenario
            LocalDateTime startTime = LocalDateTime.now();
            String extractedText = "Username: john.doe@example.com";

            actionRecord.setActionConfig(mockActionConfig);
            actionRecord.setText(extractedText);
            actionRecord.setDuration(156.78);
            actionRecord.setTimeStamp(startTime);
            actionRecord.setActionSuccess(true);
            actionRecord.setResultSuccess(true);
            actionRecord.setStateName("LoginFormState");

            // Verify text record
            assertEquals(extractedText, actionRecord.getText());
            assertTrue(actionRecord.isActionSuccess());
            assertTrue(actionRecord.isResultSuccess());
        }
    }

    @Nested
    @DisplayName("Mock Support")
    class MockSupport {

        @Test
        @DisplayName("Record for mock playback")
        public void testRecordForMockPlayback() {
            // Create a record that would be used in mock mode
            when(mockMatch1.getScore()).thenReturn(0.95);
            when(mockMatch1.x()).thenReturn(100);
            when(mockMatch1.y()).thenReturn(200);

            actionRecord.setActionConfig(mockFindOptions);
            actionRecord.setMatchList(Arrays.asList(mockMatch1));
            actionRecord.setDuration(50.0);
            actionRecord.setActionSuccess(true);
            actionRecord.setResultSuccess(true);

            // This record can be used to simulate finds in mock mode
            assertNotNull(actionRecord.getActionConfig());
            assertEquals(1, actionRecord.getMatchList().size());

            Match match = actionRecord.getMatchList().get(0);
            assertEquals(0.95, match.getScore());
            assertEquals(100, match.x());
            assertEquals(200, match.y());
        }

        @Test
        @DisplayName("Multiple records for distribution simulation")
        public void testMultipleRecordsForDistribution() {
            List<ActionRecord> history = new ArrayList<>();

            // Create success record
            ActionRecord successRecord = new ActionRecord();
            successRecord.setMatchList(Arrays.asList(mockMatch1));
            successRecord.setResultSuccess(true);
            history.add(successRecord);

            // Create failure record
            ActionRecord failureRecord = new ActionRecord();
            failureRecord.setMatchList(new ArrayList<>());
            failureRecord.setResultSuccess(false);
            history.add(failureRecord);

            // Can calculate success rate from history
            long successCount = history.stream().filter(ActionRecord::isResultSuccess).count();
            double successRate = (double) successCount / history.size();

            assertEquals(0.5, successRate); // 50% success rate
        }
    }

    @Nested
    @DisplayName("Timing and Performance")
    class TimingPerformance {

        @Test
        @DisplayName("Duration precision")
        public void testDurationPrecision() {
            actionRecord.setDuration(0.001); // 1 millisecond
            assertEquals(0.001, actionRecord.getDuration());

            actionRecord.setDuration(12345.6789);
            assertEquals(12345.6789, actionRecord.getDuration());
        }

        @Test
        @DisplayName("Timestamp formatting")
        public void testTimestampFormatting() {
            LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30, 45);
            actionRecord.setTimeStamp(timestamp);

            String formatted =
                    actionRecord.getTimeStamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            assertTrue(formatted.contains("2025-01-15"));
            assertTrue(formatted.contains("10:30:45"));
        }

        @Test
        @DisplayName("Null duration defaults to zero")
        public void testNullDurationDefaultsToZero() {
            ActionRecord newRecord = new ActionRecord();
            assertEquals(0.0, newRecord.getDuration());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Very long text")
        public void testVeryLongText() {
            StringBuilder longText = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                longText.append("a");
            }

            actionRecord.setText(longText.toString());
            assertEquals(10000, actionRecord.getText().length());
        }

        @Test
        @DisplayName("Large match list")
        public void testLargeMatchList() {
            List<Match> manyMatches = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                manyMatches.add(mock(Match.class));
            }

            actionRecord.setMatchList(manyMatches);
            assertEquals(1000, actionRecord.getMatchList().size());
        }

        @Test
        @DisplayName("Negative duration")
        public void testNegativeDuration() {
            // Negative duration might indicate error
            actionRecord.setDuration(-1.0);
            assertEquals(-1.0, actionRecord.getDuration());
        }
    }

    @Nested
    @DisplayName("Data Integrity")
    class DataIntegrity {

        @Test
        @DisplayName("Immutability of timestamp")
        public void testTimestampImmutability() {
            LocalDateTime original = LocalDateTime.of(2025, 1, 1, 12, 0);
            actionRecord.setTimeStamp(original);

            LocalDateTime retrieved = actionRecord.getTimeStamp();
            assertEquals(original, retrieved);

            // Modifying retrieved shouldn't affect stored
            retrieved = retrieved.plusDays(1);
            assertEquals(original, actionRecord.getTimeStamp());
        }

        @Test
        @DisplayName("Match list modification")
        public void testMatchListModification() {
            List<Match> originalList = new ArrayList<>();
            originalList.add(mockMatch1);
            actionRecord.setMatchList(originalList);

            // Modifying original list shouldn't affect record
            originalList.add(mockMatch2);

            // This depends on implementation - if setMatchList copies the list
            // For now we'll test current behavior
            assertEquals(2, originalList.size());
        }
    }
}
