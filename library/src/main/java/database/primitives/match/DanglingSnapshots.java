package database.primitives.match;

import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.state.stateObject.StateObject;

import java.util.HashMap;
import java.util.Map;

/**
 * DanglingSnapshots keep Snapshots with their objects while the operation is in progress.
 * Snapshots are then finalized at the end of the operation and added to their respective objects,
 * or in some cases not added.
 *
 * Cases where DanglingSnapshots are not added:
 * - Mock runs
 * - Snapshots are disabled. Running a process for a long period of time could accumulate
 *   a lot of Snapshots. Snapshots are disabled by default in Brobot 1.0 since Brobot 1.0 doesn't
 *   do anything with them yet (they are meant to be saved in a database for later mock runs).
 *
 * Transferring to existing Matches objects:
 *   All Match objects and all Strings are added to existing Snapshots, and new Snapshots are copied.
 *
 * Snapshots for the same Action and same Image can hold multiple Match objects.
 * For example, Find.ALL operations can return multiple Match objects.
 * Also, for an Action that executes multiple Find operations (for example, ClickUntil),
 * there may be multiple Match objects for the same Image, even when using Find.FIRST.
 * This causes Snapshots to have different behaviors depending on the Action that called them.
 * This is desired since we are interested in Snapshots because they give us information about the
 * efficacy of their underlying Actions.
 */
public class DanglingSnapshots {

    private Map<StateObject, MatchSnapshot> snapshots = new HashMap<>();

    /**
     * If there are no Match objects,
     * the operation was unsuccessful. A failed MatchSnapshot will become successful if
     * a Match is found later during the same Action.
     */
    public void addAllMatches(ActionOptions actionOptions, Matches matches) {
        matches.getMatchObjects().forEach(matchObject -> addMatch(actionOptions, matchObject));
    }

    public void addMatch(ActionOptions actionOptions, MatchObject matchObject) {
        StateObject stObj = matchObject.getStateObject();
        if (snapshots.containsKey(stObj)) {
            snapshots.get(stObj).addMatch(matchObject.getMatch());
        } else {
            snapshots.put(stObj, new MatchSnapshot.Builder()
                    .setActionOptions(actionOptions)
                    .addMatch(matchObject.getMatch())
                    .build());
        }
    }

    /**
     * Successful MatchSnapshots are always created with a Match.
     * If text is found, the Snapshot is successful. Text cannot be found without a Match.
     */
    public boolean addString(StateObject stateObject, String string) {
        if (snapshots.containsKey(stateObject)) {
            snapshots.get(stateObject).addString(string);
            return true;
        }
        return false;
    }

    /**
     * For transferring a Snapshot to an existing Matches object.
     */
    public void addSnapshot(StateObject stateObject, MatchSnapshot matchSnapshot) {
        if (!snapshots.containsKey(stateObject)) snapshots.put(stateObject, matchSnapshot);
        else {
            snapshots.get(stateObject).addMatchList(matchSnapshot.getMatchList());
            snapshots.get(stateObject).addText(matchSnapshot.getText());
        }
    }

    public void addAllSnapshots(DanglingSnapshots danglingSnapshots) {
        danglingSnapshots.snapshots.forEach(this::addSnapshot);
    }

    /**
     * Adds as new Snapshots only those with unique Match objects. If the Snapshot is
     * not unique, Strings are added to its Text when they are unique.
     * @param danglingSnapshots
     */
    public void mergeAllSnapshots(DanglingSnapshots danglingSnapshots) {
        // need to implement
    }

    public void setDuration(double seconds) {
        snapshots.values().forEach(snapshot -> snapshot.setDuration(seconds));
    }

    // this happens at the end of an Action just before the Snapshots are saved
    public void setSuccess(boolean success) {
        snapshots.forEach((image, matchSnapshot) -> matchSnapshot.setActionSuccess(success));
    }

    public void save() {
        snapshots.forEach(StateObject::addSnapshot);
    }

    public int totalSnapshots() {
        return snapshots.size();
    }

    /*
    If a match is not found, the Snapshot will have an empty MatchList.
    Regions and other objects that are converted directly to MatchObjects will
    always have an entry in the MatchList.
     */
    public int successfulSnapshots() {
        return (int) snapshots.values()
                .stream()
                .filter(snapshot -> snapshot.getMatchList().size() > 0)
                .count();
    }

    public boolean allImagesFound() {
        return totalSnapshots() == successfulSnapshots();
    }
}
