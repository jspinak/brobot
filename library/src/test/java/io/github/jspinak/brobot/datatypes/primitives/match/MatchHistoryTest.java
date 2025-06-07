package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MatchHistoryTest {

    private MatchHistory history;
    private Match match;
    private ActionOptions findOptions;
    private ActionOptions vanishOptions;

    @BeforeEach
    void setUp() {
        history = new MatchHistory();
        match = new Match.Builder().setRegion(10, 10, 20, 20).build();
        findOptions = new ActionOptions.Builder().setAction(ActionOptions.Action.FIND).build();
        vanishOptions = new ActionOptions.Builder().setAction(ActionOptions.Action.VANISH).build();
    }

    @Test
    void addSnapshot_whenFound_incrementsSearchedAndFound() {
        MatchSnapshot foundSnapshot = new MatchSnapshot.Builder().addMatch(match).build();
        history.addSnapshot(foundSnapshot);
        assertThat(history.getTimesSearched()).isEqualTo(1);
        assertThat(history.getTimesFound()).isEqualTo(1);
        assertThat(history.getSnapshots()).hasSize(1);
    }

    @Test
    void addSnapshot_whenNotFound_incrementsSearchedOnly() {
        MatchSnapshot notFoundSnapshot = new MatchSnapshot.Builder().build();
        history.addSnapshot(notFoundSnapshot);
        assertThat(history.getTimesSearched()).isEqualTo(1);
        assertThat(history.getTimesFound()).isEqualTo(0);
    }

    @Test
    void addSnapshot_withTextOnly_addsMatchFromSimilarSnapshot() {
        MatchSnapshot snapshotWithMatch = new MatchSnapshot.Builder()
                .setActionOptions(findOptions)
                .addMatch(match)
                .build();
        history.addSnapshot(snapshotWithMatch);

        MatchSnapshot textOnlySnapshot = new MatchSnapshot.Builder()
                .setActionOptions(findOptions)
                .setText("some text")
                .build();
        history.addSnapshot(textOnlySnapshot);

        assertThat(history.getSnapshots().get(1).getMatchList()).containsExactly(match);
        assertThat(history.getTimesFound()).isEqualTo(2);
    }

    @Test
    void addSnapshot_withTextOnlyAndNoSimilarMatch_createsNewMatch() {
        MatchSnapshot textOnlySnapshot = new MatchSnapshot.Builder()
                .setActionOptions(findOptions)
                .setText("some text")
                .build();
        history.addSnapshot(textOnlySnapshot);

        assertThat(history.getSnapshots().get(0).getMatchList()).isNotNull().hasSize(1);
    }

    @Test
    void getRandomSnapshot_byAction_returnsCorrectSnapshot() {
        MatchSnapshot findSnapshot = new MatchSnapshot.Builder().setActionOptions(findOptions).build();
        history.addSnapshot(findSnapshot);

        Optional<MatchSnapshot> result = history.getRandomSnapshot(ActionOptions.Action.FIND);
        assertThat(result).isPresent().contains(findSnapshot);

        Optional<MatchSnapshot> emptyResult = history.getRandomSnapshot(ActionOptions.Action.VANISH);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    void getRandomSnapshot_byActionAndState_returnsCorrectSnapshot() {
        MatchSnapshot snapshot = new MatchSnapshot.Builder()
                .setActionOptions(findOptions)
                .build();
        snapshot.setStateId(101L);
        history.addSnapshot(snapshot);

        Optional<MatchSnapshot> result = history.getRandomSnapshot(ActionOptions.Action.FIND, 101L);
        assertThat(result).isPresent().contains(snapshot);

        Optional<MatchSnapshot> emptyResult = history.getRandomSnapshot(ActionOptions.Action.FIND, 999L);
        assertThat(emptyResult).isEmpty();
    }

    @Test
    void getRandomSnapshot_separatesVanishActions() {
        MatchSnapshot findSnapshot = new MatchSnapshot.Builder().setActionOptions(findOptions).build();
        MatchSnapshot vanishSnapshot = new MatchSnapshot.Builder().setActionOptions(vanishOptions).build();
        history.addSnapshot(findSnapshot);
        history.addSnapshot(vanishSnapshot);

        Optional<MatchSnapshot> result = history.getRandomSnapshot(vanishOptions);
        assertThat(result).isPresent().contains(vanishSnapshot);
    }

    @Test
    void merge_combinesHistories() {
        MatchHistory otherHistory = new MatchHistory();
        otherHistory.addSnapshot(new MatchSnapshot.Builder().addMatch(match).build());
        otherHistory.addSnapshot(new MatchSnapshot.Builder().build());

        history.addSnapshot(new MatchSnapshot.Builder().addMatch(match).build());

        history.merge(otherHistory);

        assertThat(history.getTimesSearched()).isEqualTo(3);
        assertThat(history.getTimesFound()).isEqualTo(2);
        assertThat(history.getSnapshots()).hasSize(3);
    }

    @Test
    void isEmpty_returnsCorrectState() {
        assertThat(history.isEmpty()).isTrue();
        history.addSnapshot(new MatchSnapshot());
        assertThat(history.isEmpty()).isFalse();
    }
}