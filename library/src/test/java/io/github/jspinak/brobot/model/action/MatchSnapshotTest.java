package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.ClickOptions;
import io.github.jspinak.brobot.action.basic.type.TypeOptions;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.match.Match;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MatchSnapshotTest {

    private Match match1;
    private Match match2;

    @BeforeEach
    void setUp() {
        match1 = new Match.Builder().setRegion(0, 0, 10, 10).setName("match1").build();
        match2 = new Match.Builder().setRegion(20, 20, 10, 10).setName("match2").build();
    }

    @Test
    void builder_shouldBuildCompleteSnapshot() {
        ClickOptions options = new ClickOptions.Builder()
                .setNumberOfClicks(1)
                .build();

        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionConfig(options)
                .addMatch(match1)
                .setText("found text")
                .setDuration(1.23)
                .setActionSuccess(true)
                .setResultSuccess(true)
                .setState("TEST_STATE")
                .build();

        assertThat(snapshot.getActionConfig()).isEqualTo(options);
        assertThat(snapshot.getMatchList()).containsExactly(match1);
        assertThat(snapshot.getText()).isEqualTo("found text");
        assertThat(snapshot.getDuration()).isEqualTo(1.23);
        assertThat(snapshot.isActionSuccess()).isTrue();
        assertThat(snapshot.isResultSuccess()).isTrue();
        assertThat(snapshot.getStateName()).isEqualTo("TEST_STATE");
        assertThat(snapshot.getTimeStamp()).isNotNull();
    }

    @Test
    void wasFound_shouldReturnTrueIfMatchListIsNotEmpty() {
        ActionRecord snapshot = new ActionRecord.Builder().addMatch(match1).build();
        assertThat(snapshot.wasFound()).isTrue();
    }

    @Test
    void wasFound_shouldReturnTrueIfTextIsNotEmpty() {
        ActionRecord snapshot = new ActionRecord.Builder().setText("some text").build();
        assertThat(snapshot.wasFound()).isTrue();
    }

    @Test
    void wasFound_shouldReturnFalseIfEmpty() {
        ActionRecord snapshot = new ActionRecord.Builder().build();
        assertThat(snapshot.wasFound()).isFalse();
    }

    @Test
    void hasSameResultsAs_shouldReturnTrueForIdenticalResults() {
        ActionRecord snapshot1 = new ActionRecord.Builder().addMatch(match1).setText("text").build();
        ActionRecord snapshot2 = new ActionRecord.Builder().addMatch(match1).setText("text").build();
        assertThat(snapshot1.hasSameResultsAs(snapshot2)).isTrue();
    }

    @Test
    void hasSameResultsAs_shouldReturnFalseForDifferentText() {
        ActionRecord snapshot1 = new ActionRecord.Builder().addMatch(match1).setText("text1").build();
        ActionRecord snapshot2 = new ActionRecord.Builder().addMatch(match1).setText("text2").build();
        assertThat(snapshot1.hasSameResultsAs(snapshot2)).isFalse();
    }

    @Test
    void hasSameResultsAs_shouldReturnFalseForDifferentMatches() {
        ActionRecord snapshot1 = new ActionRecord.Builder().addMatch(match1).build();
        ActionRecord snapshot2 = new ActionRecord.Builder().addMatch(match2).build();
        assertThat(snapshot1.hasSameResultsAs(snapshot2)).isFalse();
    }

    @Test
    void hasSameResultsAs_shouldReturnFalseForSubsetOfMatches() {
        ActionRecord snapshot1 = new ActionRecord.Builder().addMatch(match1).addMatch(match2).build();
        ActionRecord snapshot2 = new ActionRecord.Builder().addMatch(match1).build();
        assertThat(snapshot1.hasSameResultsAs(snapshot2)).isFalse();
    }

    @Test
    void equals_shouldBeTrueForIdenticalObjects() {
        ActionRecord snapshot1 = new ActionRecord.Builder().addMatch(match1).setText("text").build();
        // To ensure they are considered equal, we manually set the timestamp.
        ActionRecord snapshot2 = new ActionRecord.Builder().addMatch(match1).setText("text").build();
        snapshot2.setTimeStamp(snapshot1.getTimeStamp());

        assertThat(snapshot1).isEqualTo(snapshot2);
        assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());
    }

    @Test
    void equals_shouldBeFalseForDifferentActionSuccess() {
        ActionRecord snapshot1 = new ActionRecord.Builder().setActionSuccess(true).build();
        ActionRecord snapshot2 = new ActionRecord.Builder().setActionSuccess(false).build();
        assertThat(snapshot1).isNotEqualTo(snapshot2);
    }

    @Test
    void equals_shouldBeFalseForDifferentMatchList() {
        ActionRecord snapshot1 = new ActionRecord.Builder().addMatch(match1).build();
        ActionRecord snapshot2 = new ActionRecord.Builder().addMatch(match2).build();
        assertThat(snapshot1).isNotEqualTo(snapshot2);
    }

    @Test
    void equals_shouldCorrectlyCompareTimestampsWithSecondPrecision() {
        // 1. Arrange: Create a base snapshot
        ClickOptions options = new ClickOptions.Builder().setNumberOfClicks(1).build();
        Match match = new Match.Builder().setRegion(0, 0, 10, 10).build();
        ActionRecord snapshot1 = new ActionRecord.Builder()
                .setActionConfig(options)
                .addMatch(match)
                .setText("test")
                .setDuration(1.0)
                .build();
        snapshot1.setStateId(1L);

        // 2. Create a second snapshot with identical properties
        ActionRecord snapshot2 = new ActionRecord.Builder()
                .setActionConfig(options)
                .addMatch(match)
                .setText("test")
                .setDuration(1.0)
                .build();
        snapshot2.setStateId(1L);

        // 3. Set timestamps to be different but within the same second
        snapshot1.setTimeStamp(LocalDateTime.of(2025, 1, 1, 12, 0, 5, 100_000_000)); // 12:00:05.1
        snapshot2.setTimeStamp(LocalDateTime.of(2025, 1, 1, 12, 0, 5, 800_000_000)); // 12:00:05.8

        // 4. Assert: They should be equal because the equals method truncates to the second
        assertThat(snapshot1).isEqualTo(snapshot2);
        assertThat(snapshot1.hashCode()).isEqualTo(snapshot2.hashCode());

        // 5. Assert: They should NOT be equal when the second is different
        snapshot2.setTimeStamp(snapshot2.getTimeStamp().plusSeconds(1)); // 12:00:06.8
        assertThat(snapshot1).isNotEqualTo(snapshot2);
    }
    
    // Tests documenting ActionRecord's current ActionConfig usage
    
    @Test
    void actionRecord_usesActionConfig() {
        // ActionRecord is a model class that stores historical action data
        // It now uses ActionConfig instead of the deprecated ActionOptions
        ActionRecord record = new ActionRecord();
        
        // Can be set with specific ActionConfig
        ClickOptions clickOptions = new ClickOptions.Builder()
                .setNumberOfClicks(2)
                .build();
        record.setActionConfig(clickOptions);
        
        assertThat(record.getActionConfig()).isEqualTo(clickOptions);
    }
    
    @Test
    void actionConfig_supportNowAvailable() {
        // ActionRecord now fully supports ActionConfig
        // The migration from ActionOptions to ActionConfig is complete
        
        // Current state - ActionConfig support
        ActionRecord currentRecord = new ActionRecord.Builder()
                .setActionConfig(new TypeOptions.Builder()
                        .build())
                .setText("typed text")
                .build();
                
        assertThat(currentRecord.getActionConfig()).isNotNull();
        assertThat(currentRecord.getText()).isEqualTo("typed text");
    }
}