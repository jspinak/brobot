package io.github.jspinak.brobot.datatypes.primitives.match;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.mock.MatchMaker;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import static java.util.stream.Collectors.toList;

/**
 * Maintains historical match data for pattern learning and mock execution in the Brobot framework.
 * 
 * <p>MatchHistory accumulates MatchSnapshot records over time, building a statistical model of 
 * how GUI elements behave in different contexts. This historical data serves two critical 
 * purposes: enabling realistic mock execution during development/testing and providing empirical 
 * data for optimizing automation strategies.</p>
 * 
 * <p>Key components:
 * <ul>
 *   <li><b>Search Statistics</b>: Tracks total searches and successful finds for probability calculations</li>
 *   <li><b>Snapshot Collection</b>: Stores detailed records of each match attempt with full context</li>
 *   <li><b>Action-specific Data</b>: Separates snapshots by action type for accurate simulation</li>
 *   <li><b>State Context</b>: Links matches to their containing states for context-aware mocking</li>
 * </ul>
 * </p>
 * 
 * <p>Mock execution support:
 * <ul>
 *   <li>When history exists, uses actual snapshot data for realistic behavior</li>
 *   <li>Falls back to state probability variables when history is empty</li>
 *   <li>Provides action-specific and state-specific snapshot selection</li>
 *   <li>Handles special cases like text extraction and vanish operations</li>
 * </ul>
 * </p>
 * 
 * <p>Snapshot management:
 * <ul>
 *   <li><b>Automatic Text Handling</b>: Adds matches to text-only snapshots when needed</li>
 *   <li><b>Find Type Filtering</b>: Respects Find.ALL, Find.EACH, Find.FIRST distinctions</li>
 *   <li><b>Vanish Separation</b>: Treats vanish operations differently from find operations</li>
 *   <li><b>Random Selection</b>: Provides stochastic behavior based on historical distribution</li>
 * </ul>
 * </p>
 * 
 * <p>Data collection strategies:
 * <ul>
 *   <li>Can be pre-populated during state setup for immediate mocking capability</li>
 *   <li>Accumulates naturally during real execution for learning</li>
 *   <li>Persists to database in production environments for long-term learning</li>
 *   <li>Merges histories from multiple sources for comprehensive coverage</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, MatchHistory transforms the framework from a deterministic 
 * automation tool into an adaptive system that learns from experience. By capturing not just 
 * whether elements were found but also when, where, and under what conditions, it enables 
 * sophisticated simulation and optimization of GUI interactions.</p>
 * 
 * <p>The distinction between action types (especially vanish vs. other actions) ensures that 
 * mock behavior accurately reflects the different success criteria and timing characteristics 
 * of various operations.</p>
 * 
 * @since 1.0
 * @see MatchSnapshot
 * @see Image
 * @see StateImage
 * @see MatchMaker
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchHistory {

    private int timesSearched = 0;
    private int timesFound = 0;
    private List<MatchSnapshot> snapshots = new ArrayList<>();

    @JsonProperty
    public void setTimesSearched(int timesSearched) {
        this.timesSearched = timesSearched;
    }

    @JsonProperty
    public void setTimesFound(int timesFound) {
        this.timesFound = timesFound;
    }

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
    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action, Long state) {
        List<MatchSnapshot> selectedSnapshots = snapshots.stream()
                .filter(snapshot -> snapshot.getActionOptions().getAction() == action && snapshot.getStateId().equals(state))
                .collect(toList());
        return getRandomSnapshot(selectedSnapshots);
    }

    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action, Long... states) {
        for (Long state : states) {
            Optional<MatchSnapshot> optMS = getRandomSnapshot(action, state);
            if (optMS.isPresent()) return optMS;
        }
        return Optional.empty();
    }

    public Optional<MatchSnapshot> getRandomSnapshot(ActionOptions.Action action, Set<Long> states) {
        return getRandomSnapshot(action, states.toArray(new Long[0]));
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

    @Override
    public String toString() {
        return "MatchHistory{" +
                "timesSearched=" + timesSearched +
                ", timesFound=" + timesFound +
                ", snapshots=" + snapshots +
                '}';
    }

}
