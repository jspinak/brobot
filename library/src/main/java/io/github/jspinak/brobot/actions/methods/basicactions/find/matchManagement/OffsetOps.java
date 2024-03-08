package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObjectData;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OffsetOps {

    public void addOffset(List<ObjectCollection> objectCollections, Matches matches, ActionOptions actionOptions) {
        if (matches.isEmpty()) addOffsetAsOnlyMatch(objectCollections, matches, false);
        else addOffsetAsLastMatch(matches, actionOptions);
    }

    /**
     * Happens at the beginning of the FIND action if there are no objects in the collections.
     * The offset determines the Match.
     *
     * @param objectCollections the collections used in the FIND action
     */
    public void addOffsetAsOnlyMatch(List<ObjectCollection> objectCollections, Matches matches, boolean doOnlyWhenCollectionsAreEmpty) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.getAddX() == 0) return;
        if (!areCollectionsEmpty(objectCollections) && doOnlyWhenCollectionsAreEmpty) return;
        Location location = new Location(Mouse.at(), actionOptions.getAddX(), actionOptions.getAddY());
        double duration = Duration.between(LocalDateTime.now(), matches.getStartTime()).toSeconds();
        StateLocation stateLocation = new StateLocation.Builder().setLocation(location).build();
        Match offsetMatch = new Match.Builder()
                .setMatch(location.toMatch())
                .setStateObjectData(stateLocation)
                .build();
        matches.add(offsetMatch);
    }

    /**
     * Happens after the FIND action to determine the last location to act on.
     * This need to happen after the FIND action because the offset is added to the last Match.
     * This method is often called in a MOVE or DRAG action.
     *
     * @param matches the Matches found by the FIND action
     * @param actionOptions the action configuration
     */
    public void addOffsetAsLastMatch(Matches matches, ActionOptions actionOptions) {
        if (actionOptions.getAddX2() == 0) return;
        if (matches.isEmpty()) return;
        Match lastMatch = matches.getMatchList().get(matches.getMatchList().size() - 1);
        Location offsetLocation = new Location(
                lastMatch.x() + actionOptions.getAddX2(), lastMatch.y() + actionOptions.getAddY2());
        Match lastMatchObject = matches.getMatchList().get(matches.getMatchList().size() - 1);
        StateObjectData stateObjectData = lastMatchObject.getStateObjectData();
        //double duration = lastMatchObject.getDuration();
        Match offsetMatch = offsetLocation.toMatch();
        Match offsetMatchObject = new Match.Builder()
                .setMatch(offsetMatch)
                .setStateObjectData(stateObjectData)
                .build();
        matches.getMatchList().add(offsetMatchObject);
    }

    private boolean areCollectionsEmpty(List<ObjectCollection> objectCollections) {
        for (ObjectCollection objColl : objectCollections) {
            if (!objColl.isEmpty()) return false;
        }
        return true;
    }

}
