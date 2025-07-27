package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.vanish.VanishOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests ActionHistory integration with ActionConfig (modern API).
 * This test class verifies that ActionHistory works correctly with the new 
 * ActionConfig system while maintaining backward compatibility.
 */
class ActionHistoryConfigTest {

    private ActionHistory history;
    private PatternFindOptions findConfig;
    private ClickOptions clickConfig;
    private VanishOptions vanishConfig;
    private Match match;

    @BeforeEach
    void setUp() {
        history = new ActionHistory();
        
        findConfig = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.BEST)
                .setSimilarity(0.85)
                .build();
                
        clickConfig = new ClickOptions.Builder()
                .setClickType(ClickOptions.Type.LEFT)
                .build();
                
        vanishConfig = new VanishOptions.Builder()
                .setTimeout(5.0)
                .build();
                
        match = new Match.Builder().setRegion(10, 10, 20, 20).build();
    }

    @Test
    void getRandomSnapshot_withActionConfig_returnCorrectSnapshot() {
        ActionRecord findRecord = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        history.addSnapshot(findRecord);

        Optional<ActionRecord> result = history.getRandomSnapshot(findConfig);
        assertThat(result).isPresent().contains(findRecord);
    }

    @Test
    void getRandomSnapshot_withActionConfigAndState_returnsCorrectSnapshot() {
        ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        record.setStateId(101L);
        history.addSnapshot(record);

        Optional<ActionRecord> result = history.getRandomSnapshot(findConfig, 101L);
        assertThat(result).isPresent().contains(record);

        Optional<ActionRecord> emptyResult = history.getRandomSnapshot(findConfig, 999L);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    void getRandomSnapshot_separatesVanishFromOtherActions() {
        ActionRecord findRecord = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        ActionRecord vanishRecord = new ActionRecord.Builder()
                .setActionConfig(vanishConfig)
                .build();
        
        history.addSnapshot(findRecord);
        history.addSnapshot(vanishRecord);

        // Vanish should only return vanish records
        Optional<ActionRecord> vanishResult = history.getRandomSnapshot(vanishConfig);
        assertThat(vanishResult).isPresent().contains(vanishRecord);

        // Find should not return vanish records
        Optional<ActionRecord> findResult = history.getRandomSnapshot(findConfig);
        assertThat(findResult).isPresent().contains(findRecord);
    }

    @Test
    void getSimilarSnapshots_withActionConfig_filtersCorrectly() {
        ActionRecord findRecord = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        ActionRecord clickRecord = new ActionRecord.Builder()
                .setActionConfig(clickConfig)
                .build();
        
        history.addSnapshot(findRecord);
        history.addSnapshot(clickRecord);

        var similarSnapshots = history.getSimilarSnapshots(findConfig);
        assertThat(similarSnapshots).containsExactly(findRecord);
    }

    @Test
    void getRandomMatchList_withActionConfig_returnsCorrectMatches() {
        ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        history.addSnapshot(record);

        var matchList = history.getRandomMatchList(findConfig);
        assertThat(matchList).containsExactly(match);
    }

    @Test
    void addSnapshot_withActionConfigTextOnly_addsSimilarMatch() {
        // First add a snapshot with a match
        ActionRecord recordWithMatch = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        history.addSnapshot(recordWithMatch);

        // Then add a text-only snapshot with same ActionConfig
        ActionRecord textOnlyRecord = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .setText("some text")
                .build();
        history.addSnapshot(textOnlyRecord);

        // The text-only record should have received the match from the similar snapshot
        assertThat(history.getSnapshots().get(1).getMatchList()).containsExactly(match);
        assertThat(history.getTimesFound()).isEqualTo(2);
    }

    @Test
    void backwardCompatibility_mixedActionOptionsAndActionConfig() {
        // Create record with legacy ActionOptions
        ActionOptions legacyOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .build();
        ActionRecord legacyRecord = new ActionRecord.Builder()
                .setActionOptions(legacyOptions)
                .addMatch(match)
                .build();
        history.addSnapshot(legacyRecord);

        // Create record with modern ActionConfig
        ActionRecord modernRecord = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        history.addSnapshot(modernRecord);

        // Both should be accessible via ActionConfig methods
        Optional<ActionRecord> result = history.getRandomSnapshot(findConfig);
        assertThat(result).isPresent();
        
        // Both should be accessible via legacy ActionOptions methods
        Optional<ActionRecord> legacyResult = history.getRandomSnapshot(ActionOptions.Action.FIND);
        assertThat(legacyResult).isPresent();
        
        assertThat(history.getTimesSearched()).isEqualTo(2);
        assertThat(history.getTimesFound()).isEqualTo(2);
    }

    @Test 
    void actionRecord_storesBothActionConfigAndActionOptions() {
        ActionRecord record = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .build();

        // Should have ActionConfig
        assertThat(record.getActionConfig()).isEqualTo(findConfig);
        
        // Should also have ActionOptions for backward compatibility
        assertThat(record.getActionOptions()).isNotNull();
        assertThat(record.getActionOptions().getAction()).isEqualTo(ActionOptions.Action.FIND);
    }

    @Test
    void actionConfigAdapter_correctlyMapsActionTypes() {
        ActionConfigAdapter adapter = new ActionConfigAdapter();
        
        assertThat(adapter.getActionType(findConfig)).isEqualTo(ActionOptions.Action.FIND);
        assertThat(adapter.getActionType(clickConfig)).isEqualTo(ActionOptions.Action.CLICK);
        assertThat(adapter.getActionType(vanishConfig)).isEqualTo(ActionOptions.Action.VANISH);
    }

    @Test
    void actionConfigAdapter_correctlyMapsFindStrategies() {
        ActionConfigAdapter adapter = new ActionConfigAdapter();
        
        PatternFindOptions firstStrategy = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.FIRST)
                .build();
        PatternFindOptions allStrategy = new PatternFindOptions.Builder()
                .setStrategy(PatternFindOptions.Strategy.ALL)
                .build();
                
        assertThat(adapter.getFindStrategy(firstStrategy)).isEqualTo(ActionOptions.Find.FIRST);
        assertThat(adapter.getFindStrategy(allStrategy)).isEqualTo(ActionOptions.Find.ALL);
        assertThat(adapter.getFindStrategy(clickConfig)).isEqualTo(ActionOptions.Find.UNIVERSAL);
    }

    @Test
    void actionHistory_handlesMultipleStatesWithActionConfig() {
        ActionRecord record1 = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        record1.setStateId(100L);
        
        ActionRecord record2 = new ActionRecord.Builder()
                .setActionConfig(findConfig)
                .addMatch(match)
                .build();
        record2.setStateId(200L);
        
        history.addSnapshot(record1);
        history.addSnapshot(record2);

        // Test multiple states lookup
        Optional<ActionRecord> result = history.getRandomSnapshot(findConfig, 100L, 200L, 300L);
        assertThat(result).isPresent();
        assertThat(result.get().getStateId()).isIn(100L, 200L);
    }
}