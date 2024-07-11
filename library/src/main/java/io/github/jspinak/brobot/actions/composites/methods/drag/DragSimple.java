package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionExecution.MatchesInitializer;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.DragLocation;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

@Component
public class DragSimple implements ActionInterface {
    private final DragLocation dragLocation;
    private final Find find;
    private final MatchesInitializer matchesInitializer;

    public DragSimple(DragLocation dragLocation, Find find, MatchesInitializer matchesInitializer) {
        this.dragLocation = dragLocation;
        this.find = find;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * The first object in the first ObjectCollection is the 'from' Match.
     * The second object in the first ObjectCollection is the 'to' Match.
     * All other objects are ignored.
     * If no objects are provided, the current mouse position is used as the start position and
     * the options addX and addY are used as the end position.
     * If 1 object is provided, the current mouse position is used as the start position and
     * the object is used as the end position.
     * @param matches holds the options for the drag and match objects already found
     * @param objectCollections the object collections
     */
    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        matchesInitializer.init(actionOptions, objectCollections);
        ActionOptions findFrom = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(actionOptions.getSimilarity())
                .setSearchRegions(actionOptions.getSearchRegions())
                .setAddX(actionOptions.getAddX())
                .setAddY(actionOptions.getAddY())
                .build();
        ActionOptions findTo = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setMinSimilarity(actionOptions.getSimilarity())
                .setSearchRegions(actionOptions.getSearchRegions())
                .setAddX(actionOptions.getAddX2())
                .setAddY(actionOptions.getAddY2())
                .build();
        setStartLoc(findFrom, matches, objectCollections);
        setEndLoc(findTo, matches, objectCollections);
        if (matches.size() < 2) return;
        dragLocation.drag(matches.getMatchList().get(0).getTarget(),
                matches.getMatchList().get(1).getTarget(), actionOptions);
    }

    /*
    Uses the first object in the first ObjectCollection as the 'from' Match. If it's not found the method returns.
     */
    private void setStartLoc(ActionOptions actionOptions, Matches matches, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) {
            addStartLocationFromOptions(actionOptions, matches);
            return;
        }
        ObjectCollection objColl = objectCollections[0]; // use only the first object collection
        if (!objColl.getStateImages().isEmpty()) { // use only the first object
            find.perform(matches, objColl.getStateImages().get(0).asObjectCollection());
            if (matches.isEmpty()) return;
            return;
        }
        if (!objColl.getStateRegions().isEmpty()) {
            Location loc = new Location(objColl.getStateRegions().get(0).getSearchRegion().getLocation());
            loc.setX(loc.getX() + actionOptions.getAddX());
            loc.setY(loc.getY() + actionOptions.getAddY());
            addMatch(loc, matches);
        }
        if (!objColl.getStateLocations().isEmpty()) {
            Location loc = objColl.getStateLocations().get(0).getLocation();
            loc.setX(loc.getX() + actionOptions.getAddX());
            loc.setY(loc.getY() + actionOptions.getAddY());
            addMatch(loc, matches);
        }
        addStartLocationFromOptions(actionOptions, matches);
    }

    /*
    Uses the second object in the first ObjectCollection as the 'to' Match. If it's not found the method returns.
    If there is no second object, the options addX2 and addY2 are added to the current mouse position.
     */
    private void setEndLoc(ActionOptions actionOptions, Matches matches, ObjectCollection... objectCollections) {
        if (objectCollections.length == 0) {
            addEndLocationFromOptions(actionOptions, matches);
            return;
        }
        ObjectCollection objColl = objectCollections[0]; // use only the first object collection
        int objectIndexNeeded = 1;
        if (objColl.getStateImages().size() > 1) { // use only the second object
            find.perform(matches, objColl.getStateImages().get(1).asObjectCollection());
            if (matches.isEmpty()) return;
            return;
        }
        if (objColl.getStateImages().size() == 1) objectIndexNeeded = 0;
        if (objColl.getStateRegions().size() > objectIndexNeeded) {
            Location loc = new Location(objColl.getStateRegions().get(objectIndexNeeded).getSearchRegion().getLocation());
            loc.setX(loc.getX() + actionOptions.getDragToOffsetX());
            loc.setY(loc.getY() + actionOptions.getDragToOffsetY());
            addMatch(loc, matches);
        }
        if (objColl.getStateRegions().size() == 1) objectIndexNeeded = 0;
        if (objColl.getStateLocations().size() > objectIndexNeeded) {
            Location loc = objColl.getStateLocations().get(objectIndexNeeded).getLocation();
            loc.setX(loc.getX() + actionOptions.getDragToOffsetX());
            loc.setY(loc.getY() + actionOptions.getDragToOffsetY());
            addMatch(loc, matches);
        }
        addEndLocationFromOptions(actionOptions, matches);
    }


    private void addStartLocationFromOptions(ActionOptions actionOptions, Matches matches) {
        Location loc = new Location(Mouse.at());
        loc.setX(loc.getX() + actionOptions.getAddX());
        loc.setY(loc.getY() + actionOptions.getAddY());
        addMatch(loc, matches);
    }

    private void addEndLocationFromOptions(ActionOptions actionOptions, Matches matches) {
        Location loc = new Location(Mouse.at());
        loc.setX(loc.getX() + actionOptions.getAddX2());
        loc.setY(loc.getY() + actionOptions.getAddY2());
        addMatch(loc, matches);
    }

    private void addMatch(Location loc, Matches matches) {
        Match from = new Match.Builder()
                .setMatch(loc.toMatch())
                .setStateObjectData(loc.asStateLocationInNullState())
                .build();
        matches.add(from);
    }
}
