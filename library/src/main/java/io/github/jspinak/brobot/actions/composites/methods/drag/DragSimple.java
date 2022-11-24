package io.github.jspinak.brobot.actions.composites.methods.drag;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.DragLocation;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DragSimple implements ActionInterface {

    private final GetSceneAnalysisCollection getSceneAnalysisCollection;
    private DragLocation dragLocation;
    private Find find;

    public DragSimple(GetSceneAnalysisCollection getSceneAnalysisCollection, DragLocation dragLocation, Find find) {
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
        this.dragLocation = dragLocation;
        this.find = find;
    }

    /**
     * The first object in the first ObjectCollection is the 'from' Match.
     * The second object in the first ObjectCollection is the 'to' Match.
     * All other objects are ignored.
     * If no objects are provided, the current mouse position is used as the start position and
     * the options addX and addY are used as the end position.
     * If 1 object is provided, the current mouse position is used as the start position and
     * the object is used as the end position.
     * @param actionOptions the options for the drag
     * @param objectCollections the object collections
     * @return the matches
     */
    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.
                get(Arrays.asList(objectCollections), actionOptions);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
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
        if (matches.size() < 2) return matches;
        dragLocation.drag(matches.getMatchObjects().get(0).getLocation(),
                matches.getMatchObjects().get(1).getLocation(), actionOptions);
        return matches;
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
        if (objColl.getStateImages().size() > 0) { // use only the first object
            Matches findMatches = find.perform(actionOptions, objColl.getStateImages().get(0).asObjectCollection());
            if (findMatches.isEmpty()) return;
            matches.addMatchObjects(findMatches);
            return;
        }
        if (objColl.getStateRegions().size() > 0) {
            Location loc = objColl.getStateRegions().get(0).getSearchRegion().getLocation();
            loc.setX(loc.getX() + actionOptions.getAddX());
            loc.setY(loc.getY() + actionOptions.getAddY());
            addMatchObject(loc, matches);
        }
        if (objColl.getStateLocations().size() > 0) {
            Location loc = objColl.getStateLocations().get(0).getLocation();
            loc.setX(loc.getX() + actionOptions.getAddX());
            loc.setY(loc.getY() + actionOptions.getAddY());
            addMatchObject(loc, matches);
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
            Matches findMatches = find.perform(actionOptions, objColl.getStateImages().get(1).asObjectCollection());
            if (findMatches.isEmpty()) return;
            matches.addMatchObjects(findMatches);
            return;
        }
        if (objColl.getStateImages().size() == 1) objectIndexNeeded = 0;
        if (objColl.getStateRegions().size() > objectIndexNeeded) {
            Location loc = objColl.getStateRegions().get(objectIndexNeeded).getSearchRegion().getLocation();
            loc.setX(loc.getX() + actionOptions.getDragToOffsetX());
            loc.setY(loc.getY() + actionOptions.getDragToOffsetY());
            addMatchObject(loc, matches);
        }
        if (objColl.getStateRegions().size() == 1) objectIndexNeeded = 0;
        if (objColl.getStateLocations().size() > objectIndexNeeded) {
            Location loc = objColl.getStateLocations().get(objectIndexNeeded).getLocation();
            loc.setX(loc.getX() + actionOptions.getDragToOffsetX());
            loc.setY(loc.getY() + actionOptions.getDragToOffsetY());
            addMatchObject(loc, matches);
        }
        addEndLocationFromOptions(actionOptions, matches);
    }


    private void addStartLocationFromOptions(ActionOptions actionOptions, Matches matches) {
        Location loc = new Location(Mouse.at());
        loc.setX(loc.getX() + actionOptions.getAddX());
        loc.setY(loc.getY() + actionOptions.getAddY());
        addMatchObject(loc, matches);
    }

    private void addEndLocationFromOptions(ActionOptions actionOptions, Matches matches) {
        Location loc = new Location(Mouse.at());
        loc.setX(loc.getX() + actionOptions.getAddX2());
        loc.setY(loc.getY() + actionOptions.getAddY2());
        addMatchObject(loc, matches);
    }

    private void addMatchObject(Location loc, Matches matches) {
        try {
            MatchObject from = new MatchObject(loc.toMatch(), loc.inNullState(),
                    Duration.between(matches.getStartTime(), LocalDateTime.now()).toSeconds());
            matches.add(from);
        } catch (Exception e) {
            Report.println("location not added to matches");
        }
    }
}
