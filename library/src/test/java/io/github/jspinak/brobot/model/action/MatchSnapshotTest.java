package io.github.jspinak.brobot.model.action;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
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
        ActionOptions options = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();

        ActionRecord snapshot = new ActionRecord.Builder()
                .setActionOptions(options)
                .addMatch(match1)
                .setText("found text")
                .setDuration(1.23)
                .setActionSuccess(true)
                .setResultSuccess(true)
                .setState("TEST_STATE")
                .build();

        assertThat(snapshot.getActionOptions()).isEqualTo(options);
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
        ActionOptions options = new ActionOptions.Builder().setAction(ActionOptions.Action.CLICK).build();
        Match match = new Match.Builder().setRegion(0, 0, 10, 10).build();
        ActionRecord snapshot1 = new ActionRecord.Builder()
                .setActionOptions(options)
                .addMatch(match)
                .setText("test")
                .setDuration(1.0)
                .build();
        snapshot1.setStateId(1L);

        // 2. Create a second snapshot with identical properties
        ActionRecord snapshot2 = new ActionRecord.Builder()
                .setActionOptions(options)
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
    
    // Tests documenting ActionRecord's current ActionOptions usage
    
    @Test
    void actionRecord_currentlyRequiresActionOptions() {
        // ActionRecord is a model class that stores historical action data
        // It currently uses ActionOptions to maintain compatibility with existing stored data
        ActionRecord record = new ActionRecord();
        
        // Default ActionOptions are automatically set
        assertThat(record.getActionOptions()).isNotNull();
        assertThat(record.getActionOptions().getAction()).isEqualTo(ActionOptions.Action.FIND);
        
        // Can be updated with specific ActionOptions
        ActionOptions clickOptions = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .build();
        record.setActionOptions(clickOptions);
        
        assertThat(record.getActionOptions().getAction()).isEqualTo(ActionOptions.Action.CLICK);
    }
    
    @Test
    void migrationConsiderations_forActionConfigSupport() {
        // When ActionRecord is updated to support ActionConfig, considerations include:
        // 1. Backward compatibility with existing stored ActionOptions data
        // 2. Potential dual support for both ActionOptions and ActionConfig
        // 3. Migration utilities for converting historical data
        // 4. Versioning system for stored records
        
        // Current state - ActionOptions only
        ActionRecord currentRecord = new ActionRecord.Builder()
                .setActionOptions(new ActionOptions.Builder()
                        .setAction(ActionOptions.Action.TYPE)
                        .build())
                .setText("typed text")
                .build();
                
        // Future state might support ActionConfig:
        // ActionRecord futureRecord = new ActionRecord.Builder()
        //     .setActionConfig(new TypeOptions.Builder().build())
        //     .setText("typed text")
        //     .build();
        
        assertThat(currentRecord.getActionOptions()).isNotNull();
        assertThat(currentRecord.getText()).isEqualTo("typed text");
    }
}