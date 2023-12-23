package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.StateObject;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import org.sikuli.script.Match;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OffsetOps {

    public void addOffset(List<ObjectCollection> objectCollections, Matches matches, ActionOptions actionOptions) {
        if (matches.isEmpty()) addOffsetAsOnlyMatch(objectCollections, matches, actionOptions, false);
        else addOffsetAsLastMatch(matches, actionOptions);
    }

    /**
     * Happens at the beginning of the FIND action if there are no objects in the collections.
     * The offset determines the Match.
     *
     * @param objectCollections the collections used in the FIND action
     * @param actionOptions the options used in the FIND action
     * @param actionOptions the action configuration
     * @return true if the offset was added.
     */
    public boolean addOffsetAsOnlyMatch(List<ObjectCollection> objectCollections, Matches matches, ActionOptions actionOptions,
                                        boolean doOnlyWhenCollectionsAreEmpty) {
        if (actionOptions.getAddX() == 0) return false;
        if (!areCollectionsEmpty(objectCollections) && doOnlyWhenCollectionsAreEmpty) return false;
        Location location = new Location(Mouse.at(), actionOptions.getAddX(), actionOptions.getAddY());
        double duration = Duration.between(LocalDateTime.now(), matches.getStartTime()).toSeconds();
        StateLocation stateLocation = new StateLocation.Builder().withLocation(location).build();
        try {
            MatchObject offsetMatch = new MatchObject(location.toMatch(), stateLocation, duration);
            matches.add(offsetMatch);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Happens after the FIND action to determine the last location to act on.
     * This need to happen after the FIND action because the offset is added to the last Match.
     * This method is often called in a MOVE or DRAG action.
     *
     * @param matches the Matches found by the FIND action
     * @param actionOptions the action configuration
     * @return true if the offset was added.
     */
    public boolean addOffsetAsLastMatch(Matches matches, ActionOptions actionOptions) {
        if (actionOptions.getAddX2() == 0) return false;
        if (matches.isEmpty()) return false;
        Match lastMatch = matches.getMatches().get(matches.getMatches().size() - 1);
        Location offsetLocation = new Location(
                lastMatch.getX() + actionOptions.getAddX2(), lastMatch.getY() + actionOptions.getAddY2());
        MatchObject lastMatchObject = matches.getMatchObjects().get(matches.getMatchObjects().size() - 1);
        StateObject stateObject = lastMatchObject.getStateObject();
        double duration = lastMatchObject.getDuration();
        try {
            Match offsetMatch = offsetLocation.toMatch();
            MatchObject offsetMatchObject = new MatchObject(offsetMatch, stateObject, duration);
            matches.getMatchObjects().add(offsetMatchObject);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean areCollectionsEmpty(List<ObjectCollection> objectCollections) {
        for (ObjectCollection objColl : objectCollections) {
            if (!objColl.isEmpty()) return false;
        }
        return true;
    }

}
