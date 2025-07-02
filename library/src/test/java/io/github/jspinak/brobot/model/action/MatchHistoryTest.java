package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MatchHistoryTest {

    private ActionHistory history;
    private Match match;
    private ActionOptions findOptions;
    private ActionOptions vanishOptions;

    @BeforeEach
    void setUp() {
        history = new ActionHistory();
        match = new Match.Builder().setRegion(10, 10, 20, 20).build();
        findOptions = new ActionOptions.Builder().setAction(ActionOptions.Action.FIND).build();
        vanishOptions = new ActionOptions.Builder().setAction(ActionOptions.Action.VANISH).build();
    }

    @Test
    void addSnapshot_whenFound_incrementsSearchedAndFound() {
        ActionRecord foundSnapshot = new ActionRecord.Builder().addMatch(match).build();
        history.addSnapshot(foundSnapshot);
        assertThat(history.getTimesSearched()).isEqualTo(1);
        assertThat(history.getTimesFound()).isEqualTo(1);
        assertThat(history.getSnapshots()).hasSize(1);
    }

    @Test
    void addSnapshot_whenNotFound_incrementsSearchedOnly() {
        ActionRecord notFoundSnapshot = new ActionRecord.Builder().build();
        history.addSnapshot(notFoundSnapshot);
        assertThat(history.getTimesSearched()).isEqualTo(1);
        assertThat(history.getTimesFound()).isEqualTo(0);
    }

    @Test
    void addSnapshot_withTextOnly_addsMatchFromSimilarSnapshot() {
        ActionRecord snapshotWithMatch = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .addMatch(match)
                .build();
        history.addSnapshot(snapshotWithMatch);

        ActionRecord textOnlySnapshot = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .setText("some text")
                .build();
        history.addSnapshot(textOnlySnapshot);

        assertThat(history.getSnapshots().get(1).getMatchList()).containsExactly(match);
        assertThat(history.getTimesFound()).isEqualTo(2);
    }

    @Test
    void addSnapshot_withTextOnlyAndNoSimilarMatch_createsNewMatch() {
        ActionRecord textOnlySnapshot = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .setText("some text")
                .build();
        history.addSnapshot(textOnlySnapshot);

        assertThat(history.getSnapshots().get(0).getMatchList()).isNotNull().hasSize(1);
    }

    @Test
    void getRandomSnapshot_byAction_returnsCorrectSnapshot() {
        ActionRecord findSnapshot = new ActionRecord.Builder().setActionOptions(findOptions).build();
        history.addSnapshot(findSnapshot);

        Optional<ActionRecord> result = history.getRandomSnapshot(ActionOptions.Action.FIND);
        assertThat(result).isPresent().contains(findSnapshot);

        Optional<ActionRecord> emptyResult = history.getRandomSnapshot(ActionOptions.Action.VANISH);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    void getRandomSnapshot_byActionAndState_returnsCorrectSnapshot() {
        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .build();
        snapshot.setStateId(101L);
        history.addSnapshot(snapshot);

        Optional<ActionRecord> result = history.getRandomSnapshot(ActionOptions.Action.FIND, 101L);
        assertThat(result).isPresent().contains(snapshot);

        Optional<ActionRecord> emptyResult = history.getRandomSnapshot(ActionOptions.Action.FIND, 999L);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    void getRandomSnapshot_separatesVanishActions() {
        ActionRecord findSnapshot = new ActionRecord.Builder().setActionOptions(findOptions).build();
        ActionRecord vanishSnapshot = new ActionRecord.Builder().setActionOptions(vanishOptions).build();
        history.addSnapshot(findSnapshot);
        history.addSnapshot(vanishSnapshot);

        Optional<ActionRecord> result = history.getRandomSnapshot(vanishOptions);
        assertThat(result).isPresent().contains(vanishSnapshot);
    }

    @Test
    void merge_combinesHistories() {
        ActionHistory otherHistory = new ActionHistory();
        otherHistory.addSnapshot(new ActionRecord.Builder().addMatch(match).build());
        otherHistory.addSnapshot(new ActionRecord.Builder().build());

        history.addSnapshot(new ActionRecord.Builder().addMatch(match).build());

        history.merge(otherHistory);

        assertThat(history.getTimesSearched()).isEqualTo(3);
        assertThat(history.getTimesFound()).isEqualTo(2);
        assertThat(history.getSnapshots()).hasSize(3);
    }

    @Test
    void isEmpty_returnsCorrectState() {
        assertThat(history.isEmpty()).isTrue();
        history.addSnapshot(new ActionRecord());
        assertThat(history.isEmpty()).isFalse();
    }
    
    // Tests demonstrating current ActionOptions usage and future migration path
    
    @Test
    void actionRecord_currentlyUsesActionOptions() {
        // ActionRecord currently stores ActionOptions for historical data
        ActionRecord record = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .addMatch(match)
                .build();
                
        assertThat(record.getActionOptions()).isNotNull();
        assertThat(record.getActionOptions().getAction()).isEqualTo(ActionOptions.Action.FIND);
    }
    
    @Test
    void migrationPath_documentedForFutureActionConfigSupport() {
        // Current: ActionRecord uses ActionOptions
        ActionRecord currentRecord = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .build();
                
        // Future consideration: When ActionRecord is updated to support ActionConfig,
        // it will need to maintain backward compatibility with existing stored data
        // that uses ActionOptions. This might involve:
        // 1. Supporting both ActionOptions and ActionConfig
        // 2. Providing migration utilities to convert historical data
        // 3. Using a versioning system for stored records
        
        // Example of what future API might look like:
        // ActionRecord futureRecord = new ActionRecord.Builder()
        //     .setActionConfig(new PatternFindOptions.Builder().build())
        //     .build();
        
        assertThat(currentRecord.getActionOptions()).isNotNull();
    }
    
    @Test
    void actionHistory_worksWithCurrentActionOptionsBasedRecords() {
        // Test that ActionHistory continues to work with ActionOptions-based records
        ActionRecord findRecord = new ActionRecord.Builder()
                .setActionOptions(findOptions)
                .addMatch(match)
                .build();
                
        ActionRecord vanishRecord = new ActionRecord.Builder()
                .setActionOptions(vanishOptions)
                .build();
                
        history.addSnapshot(findRecord);
        history.addSnapshot(vanishRecord);
        
        // Verify history tracks both action types correctly
        assertThat(history.getRandomSnapshot(ActionOptions.Action.FIND)).isPresent();
        assertThat(history.getRandomSnapshot(ActionOptions.Action.VANISH)).isPresent();
        assertThat(history.getTimesSearched()).isEqualTo(2);
        assertThat(history.getTimesFound()).isEqualTo(1); // Only find had a match
    }
}