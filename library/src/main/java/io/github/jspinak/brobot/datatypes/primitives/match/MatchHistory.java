package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.mock.MatchMaker;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import static java.util.stream.Collectors.toList;

/**
 * Used for mock runs when not empty; otherwise, the State's probability variable is used.
 * The MatchHistory can be filled when setting up States,
 * or in a later version of Brobot during real execution when State variables are saved in a database.
 * MatchHistory is a variable in the Image class.
 * When a Match is found, a MatchSnapshot is created along with a MatchObject
 * and the MatchSnapshot is added to the corresponding Image's MatchHistory.
 *
 * All Actions except for Vanish are used in mocks for Find operations.
 */
@Getter
@Setter
public class MatchHistory {

    private int timesSearched = 0;
    private int timesFound = 0;
    private List<MatchSnapshot> snapshots = new ArrayList<>();

    /**
     * A Snapshot is either:
     * 1. A successful/unsuccessful Match
     * 2. A successful/unsuccessful GetText Action from a successful Match
     * 3. A successful/unsuccessful Vanish
     * There cannot be a GetText Snapshot without a successful Match. If a Snapshot is
     *   passed with Text and without a Match, a new Match is added to the Snapshot. If
     *   the MatchHistory already has a Successful Match Snapshot, this Match is added.
     *   Otherwise, a Match is created using probabilities.
     *
     * @param matchSnapshot If the Snapshot has Text but no Match, a Match will be added.
     */
    public void addSnapshot(MatchSnapshot matchSnapshot) {
        addMatchToTextSnapshot(matchSnapshot);
        snapshots.add(matchSnapshot);
        timesSearched++;
        if (matchSnapshot.wasFound()) timesFound++;
    }

    private void addMatchToTextSnapshot(MatchSnapshot matchSnapshot) {
        if (matchSnapshot.getText().isEmpty() || !matchSnapshot.getMatchList().isEmpty()) return;
        List<MatchSnapshot> similarSnapshots = getSimilarSnapshots(matchSnapshot.getActionOptions());
        for (MatchSnapshot snapshot : similarSnapshots) {
            if (!snapshot.getMatchList().isEmpty()) {
                matchSnapshot.addMatchList(snapshot.getMatchList());
                return;
            }
        }
        // otherwise, if none have Match objects, add a standard Match
        Match newMatch = new MatchMaker.Builder()
                .setImageWH(200, 100)
                .setSearchRegion(new Region())
                .build();
        matchSnapshot.addMatch(newMatch);
    }

    /**
     * Useful for Action-specific operations such as GetText.
     * @param action the Action taken
     * @return an Optional of a Snapshot
     */
    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action) {
        List<MatchSnapshot> selectedSnapshots = snapshots.stream()
                .filter(snapshot -> snapshot.getActionOptions().getAction() == action)
                .collect(toList());
        return getRandomSnapshot(selectedSnapshots);
    }

    /**
     * Brobot 1.0.7 uses Snapshots that record the action and the state.
     * @param action the Action taken
     * @return an Optional of a Snapshot
     */
    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action, String state) {
        List<MatchSnapshot> selectedSnapshots = snapshots.stream()
                .filter(snapshot -> snapshot.getActionOptions().getAction() == action && snapshot.getState().equals(state))
                .collect(toList());
        return getRandomSnapshot(selectedSnapshots);
    }

    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action, String... states) {
        for (String state : states) {
            Optional<MatchSnapshot> optMS = getRandomSnapshot(action, state);
            if (optMS.isPresent()) return optMS;
        }
        return Optional.empty();
    }

    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action, Set<String> states) {
        return getRandomSnapshot(action, states.toArray(new String[0]));
    }

    /**
     * Most Actions succeed or fail based on a Find operation.
     * VANISH has different criteria and its Snapshots are separated.
     * Return the Snapshots of the specific Find type.
     *
     * @param actionOptions holds the action configuration.
     * @return an empty Optional if there are no snapshots; otherwise, a random Snapshot.
     */
    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions actionOptions) {
        return getRandomSnapshot(getSimilarSnapshots(actionOptions));
    }

    public List<MatchSnapshot> getSimilarSnapshots(ActionOptions actionOptions) {
        return getSnapshotOfFindType(actionOptions, getFindOrVanishSnapshots(actionOptions));
    }

    private List<MatchSnapshot> getFindOrVanishSnapshots(ActionOptions actionOptions) {
        if (actionOptions.getAction() == ActionOptions.Action.VANISH) {
            return snapshots.stream()
                    .filter(snapshot -> snapshot.getActionOptions().getAction() == ActionOptions.Action.VANISH)
                    .collect(toList());
        }
        return snapshots.stream()
                .filter(snapshot -> snapshot.getActionOptions().getAction() != ActionOptions.Action.VANISH)
                .collect(toList());
    }

    private List<MatchSnapshot> getSnapshotOfFindType(ActionOptions actionOptions,
                                                          List<MatchSnapshot> snapshots) {
        return snapshots.stream().filter(snapshot ->
                                snapshot.getActionOptions().getFind() == ActionOptions.Find.UNIVERSAL ||
                                snapshot.getActionOptions().getFind() == actionOptions.getFind())
                .collect(toList());
    }

    public Optional<MatchSnapshot> getRandomSnapshot(List<MatchSnapshot> snapshots) {
        if (snapshots.isEmpty()) return Optional.empty();
        return Optional.of(snapshots.get(new Random().nextInt(snapshots.size())));
    }

    /*
     * Select a random Snapshot from a list of Snapshots of the same Action.
     * For Find Actions, restrict the list of Snapshots to the Find type.
     * MatchSnapshots can have multiple Match objects (for example, Find.ALL)
     */
    public List<Match> getRandomMatchList(ActionOptions actionOptions) {
        List<Match> matchList = new ArrayList<>();
        Optional<MatchSnapshot> randomSnapshot = getRandomSnapshot(actionOptions);
        if (randomSnapshot.isEmpty()) return matchList;
        return randomSnapshot.get().getMatchList();
    }

    public String getRandomText() {
        Optional<MatchSnapshot> textSnapshot = getRandomSnapshot(ActionOptions.Action.FIND);
        if (textSnapshot.isEmpty()) return "";
        return textSnapshot.get().getText();
    }

    public boolean isEmpty() {
        return timesSearched == 0;
    }

    public void print() {
        snapshots.forEach(MatchSnapshot::print);
    }

    public void merge(MatchHistory matchHistory) {
        this.timesFound += matchHistory.getTimesFound();
        this.timesSearched += matchHistory.getTimesSearched();
        this.snapshots.addAll(matchHistory.getSnapshots());
    }

    public boolean equals(MatchHistory matchHistory) {
        if (timesSearched != matchHistory.getTimesSearched()) return false;
        if (timesFound != matchHistory.getTimesFound()) return false;
        if (snapshots.size() != matchHistory.getSnapshots().size()) return false;
        for (int i=0; i<snapshots.size(); i++) {
            if (!snapshots.get(i).equals(matchHistory.getSnapshots().get(i))) return false;
        }
        return true;
    }

}
