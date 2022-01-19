package actions.methods.basicactions.find;

import com.brobot.multimodule.database.primitives.match.MatchObject;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
import com.brobot.multimodule.database.state.stateObject.otherStateObjects.StateLocation;
import com.brobot.multimodule.database.state.stateObject.otherStateObjects.StateRegion;
import lombok.Setter;
import org.sikuli.script.Match;
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
            matches.addAll(m);
        }
    }

    /**
     * Finding Text in Regions doesn't happen in the Find Action.
     * Find is specific to finding Image matches.
     */
    public void addRegions(Matches matches, ObjectCollection objectCollection) {
        for (StateRegion r : objectCollection.getStateRegions()) {
            try {
                matches.add(new MatchObject(r.getSearchRegion().toMatch(), r, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addLocations(Matches matches, ObjectCollection objectCollection) {
        for (StateLocation stateLocation : objectCollection.getStateLocations()) {
            try {
                Match match = stateLocation.getLocation().toMatch();
                matches.add(new MatchObject(match, stateLocation,0.0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
