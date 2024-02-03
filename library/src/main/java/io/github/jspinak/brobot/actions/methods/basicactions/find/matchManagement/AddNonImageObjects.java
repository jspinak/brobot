package io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement;

import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateLocation;
import io.github.jspinak.brobot.datatypes.state.stateObject.otherStateObjects.StateRegion;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Non-image objects are adjusted and added directly to the Matches variable.
 */
@Component
@Setter
public class AddNonImageObjects {

    public Matches getOtherObjectsDirectlyAsMatchObjects(ObjectCollection objectCollection) {
        Matches otherMatches = new Matches();
        addRegions(otherMatches, objectCollection);
        addLocations(otherMatches, objectCollection);
        addMatches(otherMatches, objectCollection);
        return otherMatches;
    }

    public void addMatches(Matches matches, ObjectCollection objectCollection) {
        for (Matches m : objectCollection.getMatches()) {
            matches.addAllResults(m);
        }
    }

    /**
     * Finding Text in Regions doesn't happen in the Find Action.
     * Find is specific to finding Image matches.
     *
     * @param matches is the Matches object created at the beginning of the Find Action.
     * @param objectCollection StateRegions in the ObjectCollection are converted to MatchObjects.
     */
    public void addRegions(Matches matches, ObjectCollection objectCollection) {
        for (StateRegion r : objectCollection.getStateRegions()) {
            Match match = new Match.Builder()
                    .setRegion(r.getSearchRegion())
                    .setStateObjectData(r)
                    .setAnchors(r.getAnchors())
                    .build();
            matches.add(match);
        }
    }

    public void addLocations(Matches matches, ObjectCollection objectCollection) {
        for (StateLocation stateLocation : objectCollection.getStateLocations()) {
            Match match = new Match.Builder()
                    .setMatch(stateLocation.getLocation().toMatch())
                    .setStateObjectData(stateLocation)
                    .build();
            matches.add(match);
        }
    }

}
