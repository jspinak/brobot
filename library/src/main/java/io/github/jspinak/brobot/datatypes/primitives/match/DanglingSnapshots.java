package io.github.jspinak.brobot.datatypes.primitives.match;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: deprecated. review code and delete classes.
 *
 * DanglingSnapshots keep Snapshots with their objects while the operation is in progress.
 * Snapshots are then finalized at the end of the operation and added to their respective objects,
 * or in some cases not added.
 *
 * Cases where DanglingSnapshots are not added:
 * - Mock runs
 * - Snapshots are disabled. Running a process for a long period of time could accumulate
 *   a lot of Snapshots. Snapshots are disabled by default in Brobot 1.x since Brobot 1.x doesn't
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

    private final Map<Long, MatchSnapshot> snapshots = new HashMap<>();

    /**
     * If there are no Match objects,
     * the operation was unsuccessful. A failed MatchSnapshot will become successful if
     * a Match is found later during the same Action.
     *
     * @param actionOptions holds the action configuration.
     * @param matches is the Matches object that will receive the MatchSnapshots.
     */
    public void addAllMatches(ActionOptions actionOptions, Matches matches) {
        matches.getMatchList().forEach(match -> addMatch(actionOptions, match));
    }

    public void addMatch(ActionOptions actionOptions, Match match) {
        Long id = match.getStateObjectData().getId();
        if (snapshots.containsKey(id)) {
            snapshots.get(id).addMatch(match);
        } else {
            snapshots.put(id, new MatchSnapshot.Builder()
                    .setActionOptions(actionOptions)
                    .addMatch(match)
                    .build());
        }
    }

    /**
     * Successful MatchSnapshots are always created with a Match.
     * If text is found, the Snapshot is successful. Text cannot be found without a Match.
     *
     * @param match  the match contains the text found.
     * @param string is the text found.
     */
    public void setString(Match match, String string) {
        Long id = match.getStateObjectData().getId();
        if (snapshots.containsKey(id)) {
            snapshots.get(id).setString(string);
        }
    }

    /**
     * For transferring a Snapshot to an existing Matches object.
     *
     * @param id is the Id of the StateObject that resulted in the MatchSnapshot.
     * @param matchSnapshot is the new MatchSnapshot to be added.
     */
    public void addSnapshot(Long id, MatchSnapshot matchSnapshot) {
        if (!snapshots.containsKey(id)) snapshots.put(id, matchSnapshot);
        else {
            snapshots.get(id).addMatchList(matchSnapshot.getMatchList());
            snapshots.get(id).setText(matchSnapshot.getText());
        }
    }

    public void addAllSnapshots(DanglingSnapshots danglingSnapshots) {
        danglingSnapshots.snapshots.forEach(this::addSnapshot);
    }

    /*
     * Adds as new Snapshots only those with unique Match objects. If the Snapshot is
     * not unique, Strings are added to its Text when they are unique.
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
        // get the stateobject from the repos and add the snapshot
        //snapshots.forEach((id, matchSnapshot) -> pattern.getMatchHistory().addSnapshot(matchSnapshot));
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
