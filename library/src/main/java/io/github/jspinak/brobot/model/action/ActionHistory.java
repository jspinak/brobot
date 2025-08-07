package io.github.jspinak.brobot.model.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.migration.DeprecationMetrics;
import io.github.jspinak.brobot.tools.testing.mock.MockMatchBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import static java.util.stream.Collectors.toList;

/**
 * Maintains historical match data for pattern learning and mock execution in the Brobot framework.
 * 
 * <p>ActionHistory accumulates MatchSnapshot records over time, building a statistical model of 
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
 * <p>In the model-based approach, ActionHistory transforms the framework from a deterministic 
 * automation tool into an adaptive system that learns from experience. By capturing not just 
 * whether elements were found but also when, where, and under what conditions, it enables 
 * sophisticated simulation and optimization of GUI interactions.</p>
 * 
 * <p>The distinction between action types (especially vanish vs. other actions) ensures that 
 * mock behavior accurately reflects the different success criteria and timing characteristics 
 * of various operations.</p>
 * 
 * @since 1.0
 * @see ActionRecord
 * @see Image
 * @see StateImage
 * @see MockMatchBuilder
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionHistory {

    @Autowired
    private ActionConfigAdapter actionConfigAdapter;

    private int timesSearched = 0;
    private int timesFound = 0;
    private List<ActionRecord> snapshots = new ArrayList<>();

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
     *   the ActionHistory already has a Successful Match Snapshot, this Match is added.
     *   Otherwise, a Match is created using probabilities.
     *
     * @param matchSnapshot If the Snapshot has Text but no Match, a Match will be added.
     */
    public void addSnapshot(ActionRecord matchSnapshot) {
        addMatchToTextSnapshot(matchSnapshot);
        snapshots.add(matchSnapshot);
        timesSearched++;
        if (matchSnapshot.wasFound()) timesFound++;
    }

    private void addMatchToTextSnapshot(ActionRecord matchSnapshot) {
        if (matchSnapshot.getText().isEmpty() || !matchSnapshot.getMatchList().isEmpty()) return;
        
        // Try to get similar snapshots using ActionConfig if available, otherwise use ActionOptions
        List<ActionRecord> similarSnapshots;
        if (matchSnapshot.getActionConfig() != null) {
            similarSnapshots = getSimilarSnapshots(matchSnapshot.getActionConfig());
        } else {
            similarSnapshots = getSimilarSnapshots(matchSnapshot.getActionOptions());
        }
        
        for (ActionRecord snapshot : similarSnapshots) {
            if (!snapshot.getMatchList().isEmpty()) {
                matchSnapshot.addMatchList(snapshot.getMatchList());
                return;
            }
        }
        // otherwise, if none have Match objects, add a standard Match
        Match newMatch = new MockMatchBuilder.Builder()
                .setImageWH(200, 100)
                .setSearchRegion(new Region())
                .build();
        matchSnapshot.addMatch(newMatch);
    }

    /**
     * Gets a random snapshot for the specified ActionConfig.
     * This is the modern API replacement for getRandomSnapshot(ActionOptions.Action).
     * @param actionConfig the ActionConfig used for the action
     * @return an Optional of a Snapshot
     */
    public Optional<ActionRecord> getRandomSnapshot(ActionConfig actionConfig) {
        if (actionConfigAdapter == null) {
            actionConfigAdapter = new ActionConfigAdapter();
        }
        ActionOptions.Action action = actionConfigAdapter.getActionType(actionConfig);
        return getRandomSnapshot(action);
    }

    /**
     * Useful for Action-specific operations such as GetText.
     * @deprecated Use getRandomSnapshot(ActionConfig) instead
     * @param action the Action taken
     * @return an Optional of a Snapshot
     */
    @Deprecated
    public Optional<ActionRecord> getRandomSnapshot(ActionOptions.Action action) {
        // Log deprecation usage
        DeprecationMetrics.log("getRandomSnapshot", "ActionOptions.Action");
        
        List<ActionRecord> selectedSnapshots = snapshots.stream()
                .filter(snapshot -> getSnapshotActionType(snapshot) == action)
                .collect(toList());
        return getRandomSnapshot(selectedSnapshots);
    }

    /**
     * Gets a random snapshot for the specified ActionConfig and state.
     * This is the modern API replacement for getRandomSnapshot(ActionOptions.Action, Long).
     * @param actionConfig the ActionConfig used for the action
     * @param state the state ID
     * @return an Optional of a Snapshot
     */
    public Optional<ActionRecord> getRandomSnapshot(ActionConfig actionConfig, Long state) {
        if (actionConfigAdapter == null) {
            actionConfigAdapter = new ActionConfigAdapter();
        }
        ActionOptions.Action action = actionConfigAdapter.getActionType(actionConfig);
        return getRandomSnapshot(action, state);
    }

    /**
     * Brobot 1.0.7 uses Snapshots that record the action and the state.
     * @deprecated Use getRandomSnapshot(ActionConfig, Long) instead
     * @param action the Action taken
     * @return an Optional of a Snapshot
     */
    @Deprecated
    public Optional<ActionRecord> getRandomSnapshot(ActionOptions.Action action, Long state) {
        List<ActionRecord> selectedSnapshots = snapshots.stream()
                .filter(snapshot -> getSnapshotActionType(snapshot) == action && snapshot.getStateId().equals(state))
                .collect(toList());
        return getRandomSnapshot(selectedSnapshots);
    }

    /**
     * Gets a random snapshot for the specified ActionConfig and multiple states.
     * @param actionConfig the ActionConfig used for the action
     * @param states the state IDs to check
     * @return an Optional of a Snapshot
     */
    public Optional<ActionRecord> getRandomSnapshot(ActionConfig actionConfig, Long... states) {
        if (actionConfigAdapter == null) {
            actionConfigAdapter = new ActionConfigAdapter();
        }
        ActionOptions.Action action = actionConfigAdapter.getActionType(actionConfig);
        return getRandomSnapshot(action, states);
    }
    
    /**
     * @deprecated Use getRandomSnapshot(ActionConfig, Long...) instead
     */
    @Deprecated
    public Optional<ActionRecord> getRandomSnapshot(ActionOptions.Action action, Long... states) {
        for (Long state : states) {
            Optional<ActionRecord> optMS = getRandomSnapshot(action, state);
            if (optMS.isPresent()) return optMS;
        }
        return Optional.empty();
    }

    /**
     * Gets a random snapshot for the specified ActionConfig and set of states.
     * @param actionConfig the ActionConfig used for the action
     * @param states the set of state IDs to check
     * @return an Optional of a Snapshot
     */
    public Optional<ActionRecord> getRandomSnapshot(ActionConfig actionConfig, Set<Long> states) {
        return getRandomSnapshot(actionConfig, states.toArray(new Long[0]));
    }
    
    /**
     * @deprecated Use getRandomSnapshot(ActionConfig, Set<Long>) instead
     */
    @Deprecated
    public Optional<ActionRecord> getRandomSnapshot(ActionOptions.Action action, Set<Long> states) {
        return getRandomSnapshot(action, states.toArray(new Long[0]));
    }

    /**
     * Most Actions succeed or fail based on a Find operation.
     * VANISH has different criteria and its Snapshots are separated.
     * Return the Snapshots of the specific Find type.
     *
     * @param actionConfig holds the action configuration.
     * @return an empty Optional if there are no snapshots; otherwise, a random Snapshot.
     */
    public Optional<ActionRecord> getRandomSnapshotByType(ActionConfig actionConfig) {
        return getRandomSnapshot(getSimilarSnapshots(actionConfig));
    }
    
    /**
     * @deprecated Use getRandomSnapshot(ActionConfig) instead
     * @param actionOptions holds the action configuration.
     * @return an empty Optional if there are no snapshots; otherwise, a random Snapshot.
     */
    @Deprecated
    public Optional<ActionRecord> getRandomSnapshot(ActionOptions actionOptions) {
        return getRandomSnapshot(getSimilarSnapshots(actionOptions));
    }

    /**
     * Gets similar snapshots for the specified ActionConfig.
     * @param actionConfig the action configuration
     * @return list of similar snapshots
     */
    public List<ActionRecord> getSimilarSnapshots(ActionConfig actionConfig) {
        return getSnapshotOfFindType(actionConfig, getFindOrVanishSnapshots(actionConfig));
    }
    
    /**
     * @deprecated Use getSimilarSnapshots(ActionConfig) instead
     */
    @Deprecated
    public List<ActionRecord> getSimilarSnapshots(ActionOptions actionOptions) {
        return getSnapshotOfFindType(actionOptions, getFindOrVanishSnapshots(actionOptions));
    }

    private List<ActionRecord> getFindOrVanishSnapshots(ActionConfig actionConfig) {
        if (actionConfigAdapter == null) {
            actionConfigAdapter = new ActionConfigAdapter();
        }
        ActionOptions.Action action = actionConfigAdapter.getActionType(actionConfig);
        if (action == ActionOptions.Action.VANISH) {
            return snapshots.stream()
                    .filter(snapshot -> getSnapshotActionType(snapshot) == ActionOptions.Action.VANISH)
                    .collect(toList());
        }
        return snapshots.stream()
                .filter(snapshot -> getSnapshotActionType(snapshot) != ActionOptions.Action.VANISH)
                .collect(toList());
    }
    
    /**
     * @deprecated Use getFindOrVanishSnapshots(ActionConfig) instead
     */
    @Deprecated
    private List<ActionRecord> getFindOrVanishSnapshots(ActionOptions actionOptions) {
        if (actionOptions.getAction() == ActionOptions.Action.VANISH) {
            return snapshots.stream()
                    .filter(snapshot -> getSnapshotActionType(snapshot) == ActionOptions.Action.VANISH)
                    .collect(toList());
        }
        return snapshots.stream()
                .filter(snapshot -> getSnapshotActionType(snapshot) != ActionOptions.Action.VANISH)
                .collect(toList());
    }

    private List<ActionRecord> getSnapshotOfFindType(ActionConfig actionConfig,
                                                          List<ActionRecord> snapshots) {
        if (actionConfigAdapter == null) {
            actionConfigAdapter = new ActionConfigAdapter();
        }
        ActionOptions.Find findType = actionConfigAdapter.getFindStrategy(actionConfig);
        return snapshots.stream().filter(snapshot -> {
                    ActionOptions.Find snapshotFind = getSnapshotFindType(snapshot);
                    return snapshotFind == ActionOptions.Find.UNIVERSAL ||
                           snapshotFind == findType;
                })
                .collect(toList());
    }
    
    /**
     * @deprecated Use getSnapshotOfFindType(ActionConfig, List<ActionRecord>) instead
     */
    @Deprecated
    private List<ActionRecord> getSnapshotOfFindType(ActionOptions actionOptions,
                                                          List<ActionRecord> snapshots) {
        return snapshots.stream().filter(snapshot -> {
                    ActionOptions.Find snapshotFind = getSnapshotFindType(snapshot);
                    return snapshotFind == ActionOptions.Find.UNIVERSAL ||
                           snapshotFind == actionOptions.getFind();
                })
                .collect(toList());
    }

    public Optional<ActionRecord> getRandomSnapshot(List<ActionRecord> snapshots) {
        if (snapshots.isEmpty()) return Optional.empty();
        return Optional.of(snapshots.get(new Random().nextInt(snapshots.size())));
    }

    /*
     * Select a random Snapshot from a list of Snapshots of the same Action.
     * For Find Actions, restrict the list of Snapshots to the Find type.
     * MatchSnapshots can have multiple Match objects (for example, Find.ALL)
     */
    public List<Match> getRandomMatchList(ActionConfig actionConfig) {
        List<Match> matchList = new ArrayList<>();
        Optional<ActionRecord> randomSnapshot = getRandomSnapshotByType(actionConfig);
        if (randomSnapshot.isEmpty()) return matchList;
        return randomSnapshot.get().getMatchList();
    }
    
    /**
     * @deprecated Use getRandomMatchList(ActionConfig) instead
     */
    @Deprecated
    public List<Match> getRandomMatchList(ActionOptions actionOptions) {
        List<Match> matchList = new ArrayList<>();
        Optional<ActionRecord> randomSnapshot = getRandomSnapshot(actionOptions);
        if (randomSnapshot.isEmpty()) return matchList;
        return randomSnapshot.get().getMatchList();
    }

    public String getRandomText() {
        // Use modern ActionConfig instead of deprecated ActionOptions.Action
        PatternFindOptions findConfig = new PatternFindOptions.Builder()
            .setStrategy(PatternFindOptions.Strategy.BEST)
            .build();
        Optional<ActionRecord> textSnapshot = getRandomSnapshot(findConfig);
        if (textSnapshot.isEmpty()) return "";
        return textSnapshot.get().getText();
    }

    public boolean isEmpty() {
        return timesSearched == 0;
    }

    public void print() {
        snapshots.forEach(ActionRecord::print);
    }

    public void merge(ActionHistory actionHistory) {
        this.timesFound += actionHistory.getTimesFound();
        this.timesSearched += actionHistory.getTimesSearched();
        this.snapshots.addAll(actionHistory.getSnapshots());
    }

    /**
     * Helper method to get the action type from a snapshot, preferring ActionConfig over ActionOptions.
     */
    private ActionOptions.Action getSnapshotActionType(ActionRecord snapshot) {
        if (snapshot.getActionConfig() != null) {
            if (actionConfigAdapter == null) {
                actionConfigAdapter = new ActionConfigAdapter();
            }
            return actionConfigAdapter.getActionType(snapshot.getActionConfig());
        }
        return snapshot.getActionOptions().getAction();
    }
    
    /**
     * Helper method to get the find type from a snapshot, preferring ActionConfig over ActionOptions.
     */
    private ActionOptions.Find getSnapshotFindType(ActionRecord snapshot) {
        if (snapshot.getActionConfig() != null) {
            if (actionConfigAdapter == null) {
                actionConfigAdapter = new ActionConfigAdapter();
            }
            return actionConfigAdapter.getFindStrategy(snapshot.getActionConfig());
        }
        return snapshot.getActionOptions().getFind();
    }

    @Override
    public String toString() {
        return "ActionHistory{" +
                "timesSearched=" + timesSearched +
                ", timesFound=" + timesFound +
                ", snapshots=" + snapshots +
                '}';
    }

}