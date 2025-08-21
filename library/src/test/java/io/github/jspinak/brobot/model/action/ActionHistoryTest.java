package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.test.BrobotTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ActionHistory - maintains historical match data for pattern learning.
 * Tests snapshot management, mock execution support, and statistical tracking.
 */
@DisplayName("ActionHistory Tests")
public class ActionHistoryTest extends BrobotTestBase {
    
    @Mock
    private Match mockMatch1;
    
    @Mock
    private Match mockMatch2;
    
    @Mock
    private ActionConfig mockActionConfig;
    
    private ActionHistory actionHistory;
    private ActionRecord testRecord;
    
    @BeforeEach
    @Override
    public void setupTest() {
        super.setupTest();
        MockitoAnnotations.openMocks(this);
        actionHistory = new ActionHistory();
        
        // Setup test record
        testRecord = new ActionRecord();
        testRecord.setActionConfig(new PatternFindOptions.Builder().build());
        testRecord.setMatchList(Arrays.asList(mockMatch1));
        testRecord.setTimeStamp(LocalDateTime.now());
        testRecord.setActionSuccess(true);
        testRecord.setResultSuccess(true);
    }
    
    @Nested
    @DisplayName("Basic Statistics")
    class BasicStatistics {
        
        @Test
        @DisplayName("Initial state is empty")
        public void testInitialState() {
            ActionHistory history = new ActionHistory();
            
            assertTrue(history.isEmpty());
            assertEquals(0, history.getTimesSearched());
            assertEquals(0, history.getTimesFound());
            assertNotNull(history.getSnapshots());
            assertTrue(history.getSnapshots().isEmpty());
        }
        
        @Test
        @DisplayName("Track times searched")
        public void testTimesSearched() {
            ActionRecord notFound = new ActionRecord();
            notFound.setMatchList(new ArrayList<>());
            
            actionHistory.addSnapshot(testRecord);
            actionHistory.addSnapshot(notFound);
            
            assertEquals(2, actionHistory.getTimesSearched());
        }
        
        @Test
        @DisplayName("Track times found")
        public void testTimesFound() {
            ActionRecord notFound = new ActionRecord();
            notFound.setMatchList(new ArrayList<>());
            
            actionHistory.addSnapshot(testRecord);
            actionHistory.addSnapshot(notFound);
            actionHistory.addSnapshot(testRecord);
            
            assertEquals(3, actionHistory.getTimesSearched());
            assertEquals(2, actionHistory.getTimesFound()); // Only 2 were found
        }
        
        @Test
        @DisplayName("Calculate success rate")
        public void testSuccessRate() {
            // Add 3 successful and 2 unsuccessful
            for (int i = 0; i < 3; i++) {
                ActionRecord success = new ActionRecord();
                success.setMatchList(Arrays.asList(mockMatch1));
                actionHistory.addSnapshot(success);
            }
            for (int i = 0; i < 2; i++) {
                ActionRecord failure = new ActionRecord();
                failure.setMatchList(new ArrayList<>());
                actionHistory.addSnapshot(failure);
            }
            
            double successRate = (double) actionHistory.getTimesFound() / actionHistory.getTimesSearched();
            assertEquals(0.6, successRate, 0.01); // 3/5 = 0.6
        }
    }
    
    @Nested
    @DisplayName("Snapshot Management")
    class SnapshotManagement {
        
        @Test
        @DisplayName("Add snapshot with matches")
        public void testAddSnapshotWithMatches() {
            actionHistory.addSnapshot(testRecord);
            
            assertEquals(1, actionHistory.getSnapshots().size());
            assertEquals(testRecord, actionHistory.getSnapshots().get(0));
            assertEquals(1, actionHistory.getTimesSearched());
            assertEquals(1, actionHistory.getTimesFound());
        }
        
        @Test
        @DisplayName("Add snapshot without matches")
        public void testAddSnapshotWithoutMatches() {
            ActionRecord emptyRecord = new ActionRecord();
            emptyRecord.setMatchList(new ArrayList<>());
            
            actionHistory.addSnapshot(emptyRecord);
            
            assertEquals(1, actionHistory.getSnapshots().size());
            assertEquals(1, actionHistory.getTimesSearched());
            assertEquals(0, actionHistory.getTimesFound());
        }
        
        @Test
        @DisplayName("Add text-only snapshot gets match added")
        public void testAddTextOnlySnapshot() {
            ActionRecord textRecord = new ActionRecord();
            textRecord.setText("Some text");
            textRecord.setMatchList(new ArrayList<>());
            textRecord.setActionConfig(new PatternFindOptions.Builder().build());
            
            actionHistory.addSnapshot(textRecord);
            
            // Since it has text but no matches, a match should be added
            assertFalse(textRecord.getMatchList().isEmpty());
            assertEquals(1, actionHistory.getTimesSearched());
            assertEquals(1, actionHistory.getTimesFound()); // Text counts as found
        }
        
        @Test
        @DisplayName("Text snapshot inherits match from similar snapshot")
        public void testTextSnapshotInheritsMatch() {
            // First add a successful match snapshot
            ActionRecord matchRecord = new ActionRecord();
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            matchRecord.setActionConfig(findOptions);
            matchRecord.setMatchList(Arrays.asList(mockMatch1, mockMatch2));
            actionHistory.addSnapshot(matchRecord);
            
            // Now add text-only snapshot with same config
            ActionRecord textRecord = new ActionRecord();
            textRecord.setText("Extracted text");
            textRecord.setMatchList(new ArrayList<>());
            textRecord.setActionConfig(findOptions);
            
            actionHistory.addSnapshot(textRecord);
            
            // Text record should inherit matches from similar snapshot
            assertEquals(2, textRecord.getMatchList().size());
            assertTrue(textRecord.getMatchList().contains(mockMatch1));
            assertTrue(textRecord.getMatchList().contains(mockMatch2));
        }
    }
    
    @Nested
    @DisplayName("Random Snapshot Selection")
    class RandomSnapshotSelection {
        
        @BeforeEach
        public void setupSnapshots() {
            // Add multiple snapshots
            for (int i = 0; i < 5; i++) {
                ActionRecord record = new ActionRecord();
                record.setActionConfig(new PatternFindOptions.Builder().build());
                record.setMatchList(Arrays.asList(mockMatch1));
                record.setStateId((long) i);
                actionHistory.addSnapshot(record);
            }
        }
        
        @Test
        @DisplayName("Get random snapshot by ActionConfig")
        public void testGetRandomSnapshotByConfig() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Optional<ActionRecord> snapshot = actionHistory.getRandomSnapshot(findOptions);
            
            assertTrue(snapshot.isPresent());
            assertNotNull(snapshot.get().getActionConfig());
        }
        
        @Test
        @DisplayName("Get random snapshot by ActionConfig and state")
        public void testGetRandomSnapshotByConfigAndState() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Optional<ActionRecord> snapshot = actionHistory.getRandomSnapshot(findOptions, 2L);
            
            assertTrue(snapshot.isPresent());
            assertEquals(2L, snapshot.get().getStateId());
        }
        
        @Test
        @DisplayName("Get random snapshot with multiple states")
        public void testGetRandomSnapshotMultipleStates() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Optional<ActionRecord> snapshot = actionHistory.getRandomSnapshot(findOptions, 1L, 2L, 3L);
            
            assertTrue(snapshot.isPresent());
            assertTrue(Arrays.asList(1L, 2L, 3L).contains(snapshot.get().getStateId()));
        }
        
        @Test
        @DisplayName("Get random snapshot with Set of states")
        public void testGetRandomSnapshotSetOfStates() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Set<Long> states = new HashSet<>(Arrays.asList(0L, 4L));
            Optional<ActionRecord> snapshot = actionHistory.getRandomSnapshot(findOptions, states);
            
            assertTrue(snapshot.isPresent());
            assertTrue(states.contains(snapshot.get().getStateId()));
        }
        
        @Test
        @DisplayName("No snapshot for non-existent state")
        public void testNoSnapshotForNonExistentState() {
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            Optional<ActionRecord> snapshot = actionHistory.getRandomSnapshot(findOptions, 99L);
            
            assertFalse(snapshot.isPresent());
        }
    }
    
    @Nested
    @DisplayName("Find Type Filtering")
    class FindTypeFiltering {
        
        @Test
        @DisplayName("Filter snapshots by find strategy")
        public void testFilterByFindStrategy() {
            // Add BEST strategy snapshots
            ActionRecord bestRecord = new ActionRecord();
            bestRecord.setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build());
            bestRecord.setMatchList(Arrays.asList(mockMatch1));
            actionHistory.addSnapshot(bestRecord);
            
            // Add ALL strategy snapshots
            ActionRecord allRecord = new ActionRecord();
            allRecord.setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build());
            allRecord.setMatchList(Arrays.asList(mockMatch1, mockMatch2));
            actionHistory.addSnapshot(allRecord);
            
            // Get similar snapshots for BEST strategy
            PatternFindOptions bestOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .build();
            List<ActionRecord> bestSnapshots = actionHistory.getSimilarSnapshots(bestOptions);
            
            assertEquals(1, bestSnapshots.size());
            assertEquals(1, bestSnapshots.get(0).getMatchList().size());
        }
        
        @Test
        @DisplayName("Get random snapshot by type")
        public void testGetRandomSnapshotByType() {
            // Add different strategy snapshots
            for (PatternFindOptions.Strategy strategy : PatternFindOptions.Strategy.values()) {
                ActionRecord record = new ActionRecord();
                record.setActionConfig(new PatternFindOptions.Builder()
                    .setStrategy(strategy)
                    .build());
                record.setMatchList(Arrays.asList(mockMatch1));
                actionHistory.addSnapshot(record);
            }
            
            PatternFindOptions firstOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
            Optional<ActionRecord> snapshot = actionHistory.getRandomSnapshotByType(firstOptions);
            
            assertTrue(snapshot.isPresent());
            PatternFindOptions config = (PatternFindOptions) snapshot.get().getActionConfig();
            assertEquals(PatternFindOptions.Strategy.FIRST, config.getStrategy());
        }
    }
    
    @Nested
    @DisplayName("Vanish Action Handling")
    class VanishActionHandling {
        
        @Test
        @DisplayName("Separate vanish snapshots from find snapshots")
        public void testSeparateVanishSnapshots() {
            // Add find snapshot
            ActionRecord findRecord = new ActionRecord();
            findRecord.setActionConfig(new PatternFindOptions.Builder().build());
            findRecord.setMatchList(Arrays.asList(mockMatch1));
            actionHistory.addSnapshot(findRecord);
            
            // Add vanish snapshot
            ActionRecord vanishRecord = new ActionRecord();
            vanishRecord.setActionConfig(new VanishOptions.Builder().build());
            vanishRecord.setMatchList(new ArrayList<>());
            actionHistory.addSnapshot(vanishRecord);
            
            // Get vanish snapshots
            VanishOptions vanishOptions = new VanishOptions.Builder().build();
            List<ActionRecord> vanishSnapshots = actionHistory.getSimilarSnapshots(vanishOptions);
            
            assertEquals(1, vanishSnapshots.size());
            assertTrue(vanishSnapshots.get(0).getActionConfig() instanceof VanishOptions);
        }
        
        @Test
        @DisplayName("Vanish snapshots not mixed with other actions")
        public void testVanishNotMixedWithOthers() {
            // Add various action snapshots
            ActionRecord clickRecord = new ActionRecord();
            clickRecord.setActionConfig(new ClickOptions.Builder().build());
            clickRecord.setMatchList(Arrays.asList(mockMatch1));
            actionHistory.addSnapshot(clickRecord);
            
            ActionRecord vanishRecord = new ActionRecord();
            vanishRecord.setActionConfig(new VanishOptions.Builder().build());
            vanishRecord.setMatchList(new ArrayList<>());
            actionHistory.addSnapshot(vanishRecord);
            
            // Find snapshots should not include vanish
            PatternFindOptions findOptions = new PatternFindOptions.Builder().build();
            List<ActionRecord> findSnapshots = actionHistory.getSimilarSnapshots(findOptions);
            
            for (ActionRecord snapshot : findSnapshots) {
                assertFalse(snapshot.getActionConfig() instanceof VanishOptions);
            }
        }
    }
    
    @Nested
    @DisplayName("Match List and Text Operations")
    class MatchListAndText {
        
        @Test
        @DisplayName("Get random match list")
        public void testGetRandomMatchList() {
            // Add snapshot with multiple matches
            ActionRecord multiMatchRecord = new ActionRecord();
            multiMatchRecord.setActionConfig(new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build());
            multiMatchRecord.setMatchList(Arrays.asList(mockMatch1, mockMatch2));
            actionHistory.addSnapshot(multiMatchRecord);
            
            PatternFindOptions allOptions = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
            List<Match> matchList = actionHistory.getRandomMatchList(allOptions);
            
            assertEquals(2, matchList.size());
            assertTrue(matchList.contains(mockMatch1));
            assertTrue(matchList.contains(mockMatch2));
        }
        
        @Test
        @DisplayName("Get random text")
        public void testGetRandomText() {
            ActionRecord textRecord = new ActionRecord();
            textRecord.setActionConfig(new PatternFindOptions.Builder().build());
            textRecord.setText("Random text");
            textRecord.setMatchList(Arrays.asList(mockMatch1));
            actionHistory.addSnapshot(textRecord);
            
            String text = actionHistory.getRandomText();
            
            assertEquals("Random text", text);
        }
        
        @Test
        @DisplayName("Get empty text when no snapshots")
        public void testGetEmptyTextWhenNoSnapshots() {
            String text = actionHistory.getRandomText();
            
            assertEquals("", text);
        }
    }
    
    @Nested
    @DisplayName("History Merging")
    class HistoryMerging {
        
        @Test
        @DisplayName("Merge two histories")
        public void testMergeTwoHistories() {
            // Create first history
            ActionHistory history1 = new ActionHistory();
            ActionRecord record1 = new ActionRecord();
            record1.setMatchList(Arrays.asList(mockMatch1));
            history1.addSnapshot(record1);
            history1.addSnapshot(record1);
            
            // Create second history
            ActionHistory history2 = new ActionHistory();
            ActionRecord record2 = new ActionRecord();
            record2.setMatchList(new ArrayList<>());
            history2.addSnapshot(record2);
            history2.addSnapshot(record1);
            
            // Merge
            history1.merge(history2);
            
            assertEquals(4, history1.getTimesSearched()); // 2 + 2
            assertEquals(3, history1.getTimesFound());    // 2 + 1
            assertEquals(4, history1.getSnapshots().size());
        }
        
        @Test
        @DisplayName("Merge empty history")
        public void testMergeEmptyHistory() {
            actionHistory.addSnapshot(testRecord);
            
            ActionHistory emptyHistory = new ActionHistory();
            actionHistory.merge(emptyHistory);
            
            assertEquals(1, actionHistory.getTimesSearched());
            assertEquals(1, actionHistory.getTimesFound());
            assertEquals(1, actionHistory.getSnapshots().size());
        }
        
        @Test
        @DisplayName("Merge into empty history")
        public void testMergeIntoEmptyHistory() {
            ActionHistory emptyHistory = new ActionHistory();
            
            actionHistory.addSnapshot(testRecord);
            emptyHistory.merge(actionHistory);
            
            assertEquals(1, emptyHistory.getTimesSearched());
            assertEquals(1, emptyHistory.getTimesFound());
            assertEquals(1, emptyHistory.getSnapshots().size());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("Handle null ActionConfig")
        public void testNullActionConfig() {
            ActionRecord nullConfigRecord = new ActionRecord();
            nullConfigRecord.setActionConfig(null);
            nullConfigRecord.setMatchList(Arrays.asList(mockMatch1));
            
            actionHistory.addSnapshot(nullConfigRecord);
            
            assertEquals(1, actionHistory.getTimesSearched());
            assertEquals(1, actionHistory.getTimesFound());
        }
        
        @Test
        @DisplayName("Large number of snapshots")
        public void testLargeNumberOfSnapshots() {
            for (int i = 0; i < 1000; i++) {
                ActionRecord record = new ActionRecord();
                record.setMatchList(i % 2 == 0 ? Arrays.asList(mockMatch1) : new ArrayList<>());
                actionHistory.addSnapshot(record);
            }
            
            assertEquals(1000, actionHistory.getTimesSearched());
            assertEquals(500, actionHistory.getTimesFound());
            assertEquals(1000, actionHistory.getSnapshots().size());
        }
        
        @ParameterizedTest
        @DisplayName("Set times searched and found")
        @ValueSource(ints = {0, 1, 100, 1000, Integer.MAX_VALUE})
        public void testSetTimesSearchedAndFound(int value) {
            actionHistory.setTimesSearched(value);
            actionHistory.setTimesFound(value / 2);
            
            assertEquals(value, actionHistory.getTimesSearched());
            assertEquals(value / 2, actionHistory.getTimesFound());
        }
    }
    
    @Nested
    @DisplayName("ToString and Print")
    class ToStringAndPrint {
        
        @Test
        @DisplayName("ToString contains key information")
        public void testToString() {
            actionHistory.addSnapshot(testRecord);
            
            String str = actionHistory.toString();
            
            assertTrue(str.contains("ActionHistory"));
            assertTrue(str.contains("timesSearched=1"));
            assertTrue(str.contains("timesFound=1"));
            assertTrue(str.contains("snapshots="));
        }
        
        @Test
        @DisplayName("Print method executes without error")
        public void testPrint() {
            actionHistory.addSnapshot(testRecord);
            
            // Should not throw exception
            assertDoesNotThrow(() -> actionHistory.print());
        }
    }
}