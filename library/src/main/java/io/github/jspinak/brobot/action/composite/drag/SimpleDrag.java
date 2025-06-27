package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.utility.DragCoordinateCalculator;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

/**
 * Provides a simplified drag implementation using a single ObjectCollection for both endpoints.
 * <p>
 * SimpleDrag offers an alternative approach to drag operations where both the source
 * and destination are specified within a single ObjectCollection, rather than using
 * separate collections as in the standard {@link Drag} implementation. This design
 * is particularly useful when both drag endpoints are logically related or when
 * working with paired elements.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Uses first object as drag source, second as destination</li>
 *   <li>Falls back to mouse position when objects are missing</li>
 *   <li>Supports mixed object types (images, regions, locations)</li>
 *   <li>Applies different offsets to source (addX/Y) and destination (addX2/Y2)</li>
 * </ul>
 * 
 * <p>Object selection logic:</p>
 * <ol>
 *   <li><b>Two objects provided:</b> First is source, second is destination</li>
 *   <li><b>One object provided:</b> Mouse position is source, object is destination</li>
 *   <li><b>No objects provided:</b> Mouse position with offsets for both endpoints</li>
 * </ol>
 * 
 * <p>This implementation provides flexibility for various drag scenarios while
 * maintaining a simpler API compared to the full Drag action.</p>
 * 
 * @see Drag
 * @see DragCoordinateCalculator
 * @see ActionOptions
 */
@Component
public class SimpleDrag implements ActionInterface {
    private final DragCoordinateCalculator dragLocation;
    private final Find find;
    private final ActionResultFactory matchesInitializer;

    public SimpleDrag(DragCoordinateCalculator dragLocation, Find find, ActionResultFactory matchesInitializer) {
        this.dragLocation = dragLocation;
        this.find = find;
        this.matchesInitializer = matchesInitializer;
    }

    /**
     * Executes a drag operation using objects from a single ObjectCollection.
     * <p>
     * This method implements a unique approach where both drag endpoints come from
     * the same ObjectCollection, selected by position. The selection logic prioritizes
     * visual elements (StateImages) over static elements (StateRegions, StateLocations).
     * 
     * <p><b>Object selection priority:</b>
     * <ol>
     *   <li>StateImages (visual search required)</li>
     *   <li>StateRegions (direct coordinates)</li>
     *   <li>StateLocations (direct coordinates)</li>
     *   <li>Current mouse position with offsets</li>
     * </ol>
     * 
     * <p><b>Endpoint determination:</b>
     * <ul>
     *   <li><b>Start point:</b> First object or mouse position with addX/Y offsets</li>
     *   <li><b>End point:</b> Second object or mouse position with addX2/Y2 offsets</li>
     *   <li>Only the first ObjectCollection is used; others are ignored</li>
     *   <li>Objects beyond the first two in the collection are ignored</li>
     * </ul>
     * 
     * <p><b>Offset application:</b>
     * <ul>
     *   <li>Source uses: addX, addY</li>
     *   <li>Destination uses: addX2, addY2 (or dragToOffsetX/Y for regions/locations)</li>
     * </ul>
     * 
     * @param matches The ActionResult containing configuration options. Modified with
     *                the found match objects (two matches representing start and end).
     * @param objectCollections Variable array of collections; only the first is used.
     *                          Should contain up to two objects for start and end points.
     */
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
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

    /**
     * Determines and sets the starting location for the drag operation.
     * <p>
     * This method implements a fallback hierarchy to find the drag source:
     * <ol>
     *   <li>First StateImage (requires visual search)</li>
     *   <li>First StateRegion (uses direct coordinates)</li>
     *   <li>First StateLocation (uses direct coordinates)</li>
     *   <li>Current mouse position (ultimate fallback)</li>
     * </ol>
     * 
     * <p>For visual elements (StateImages), a Find operation is performed. If the
     * find fails, the method returns early, aborting the drag. For coordinate-based
     * elements, the location is used directly with applied offsets.</p>
     * 
     * @param actionOptions Find options configured for the source location
     * @param matches The ActionResult to populate with the found start location
     * @param objectCollections Array of collections; only the first is examined
     */
    private void setStartLoc(ActionOptions actionOptions, ActionResult matches, ObjectCollection... objectCollections) {
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
            loc.setX(loc.getCalculatedX() + actionOptions.getAddX());
            loc.setY(loc.getCalculatedY() + actionOptions.getAddY());
            addMatch(loc, matches);
        }
        if (!objColl.getStateLocations().isEmpty()) {
            Location loc = objColl.getStateLocations().get(0).getLocation();
            loc.setX(loc.getCalculatedX() + actionOptions.getAddX());
            loc.setY(loc.getCalculatedY() + actionOptions.getAddY());
            addMatch(loc, matches);
        }
        addStartLocationFromOptions(actionOptions, matches);
    }

    /**
     * Determines and sets the destination location for the drag operation.
     * <p>
     * This method implements complex logic to find the drag destination, adapting
     * based on what objects are available:
     * <ul>
     *   <li>Normally uses the second object of each type</li>
     *   <li>Falls back to first object if only one exists of a previous type</li>
     *   <li>Uses dragToOffsetX/Y for regions and locations (not addX2/Y2)</li>
     *   <li>Falls back to mouse position with addX2/Y2 if needed</li>
     * </ul>
     * 
     * <p><b>Selection logic:</b> If there's only one StateImage, the method looks
     * for the first (not second) StateRegion or StateLocation. This allows mixing
     * object types for source and destination.</p>
     * 
     * @param actionOptions Find options configured for the destination location
     * @param matches The ActionResult to populate with the found end location
     * @param objectCollections Array of collections; only the first is examined
     */
    private void setEndLoc(ActionOptions actionOptions, ActionResult matches, ObjectCollection... objectCollections) {
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
            loc.setX(loc.getCalculatedX() + actionOptions.getDragToOffsetX());
            loc.setY(loc.getCalculatedY() + actionOptions.getDragToOffsetY());
            addMatch(loc, matches);
        }
        if (objColl.getStateRegions().size() == 1) objectIndexNeeded = 0;
        if (objColl.getStateLocations().size() > objectIndexNeeded) {
            Location loc = objColl.getStateLocations().get(objectIndexNeeded).getLocation();
            loc.setX(loc.getCalculatedX() + actionOptions.getDragToOffsetX());
            loc.setY(loc.getCalculatedY() + actionOptions.getDragToOffsetY());
            addMatch(loc, matches);
        }
        addEndLocationFromOptions(actionOptions, matches);
    }


    /**
     * Creates a start location based on current mouse position and source offsets.
     * <p>
     * This fallback method is used when no valid objects are found for the drag source.
     * It takes the current mouse position and applies the addX and addY offsets from
     * the ActionOptions to determine the starting point.
     * 
     * @param actionOptions Contains the addX and addY offset values
     * @param matches The ActionResult to add the calculated location to
     */
    private void addStartLocationFromOptions(ActionOptions actionOptions, ActionResult matches) {
        Location loc = new Location(Mouse.at());
        loc.setX(loc.getCalculatedX() + actionOptions.getAddX());
        loc.setY(loc.getCalculatedY() + actionOptions.getAddY());
        addMatch(loc, matches);
    }

    /**
     * Creates an end location based on current mouse position and destination offsets.
     * <p>
     * This fallback method is used when no valid objects are found for the drag destination.
     * It takes the current mouse position and applies the addX2 and addY2 offsets from
     * the ActionOptions to determine the ending point. Note that this uses different
     * offset fields (addX2/Y2) than the start location.
     * 
     * @param actionOptions Contains the addX2 and addY2 offset values
     * @param matches The ActionResult to add the calculated location to
     */
    private void addEndLocationFromOptions(ActionOptions actionOptions, ActionResult matches) {
        Location loc = new Location(Mouse.at());
        loc.setX(loc.getCalculatedX() + actionOptions.getAddX2());
        loc.setY(loc.getCalculatedY() + actionOptions.getAddY2());
        addMatch(loc, matches);
    }

    /**
     * Converts a Location to a Match and adds it to the ActionResult.
     * <p>
     * This utility method wraps a Location object as a Match, associating it with
     * a StateLocation in the null state. This allows coordinate-based drag endpoints
     * to be handled uniformly with visual match results.
     * 
     * @param loc The Location to convert and add
     * @param matches The ActionResult to add the match to. Modified by adding the
     *                new match to its internal match list.
     */
    private void addMatch(Location loc, ActionResult matches) {
        Match from = new Match.Builder()
                .setMatch(loc.toMatch())
                .setStateObjectData(loc.asStateLocationInNullState())
                .build();
        matches.add(from);
    }
}
