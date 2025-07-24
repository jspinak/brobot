package io.github.jspinak.brobot.action.composite.drag;

import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.util.Optional;


/**
 * Resolves source and destination locations for drag operations in the Brobot framework.
 * <p>
 * This utility class is responsible for determining the precise screen coordinates
 * for both the start and end points of a drag operation. It handles the complexity
 * of finding locations from various input types (images, regions, locations) and
 * applies appropriate offsets based on the drag configuration.
 * 
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Finding visual elements on screen to determine drag coordinates</li>
 *   <li>Applying source and destination-specific offsets</li>
 *   <li>Handling edge cases like missing ObjectCollections</li>
 *   <li>Providing fallback to current mouse position when needed</li>
 * </ul>
 * 
 * <p>Location resolution logic:</p>
 * <ul>
 *   <li><b>Two collections provided:</b> First is source, second is destination</li>
 *   <li><b>One collection provided:</b> Used for both source and destination</li>
 *   <li><b>No collections provided:</b> Current mouse position is used as source</li>
 * </ul>
 * 
 * <p>This class is crucial for the flexibility of the drag system, allowing it to
 * work with various input combinations while maintaining consistent behavior.</p>
 * 
 * @see Drag
 * @see ActionOptionsForDrag
 * @see Find
 */
@Component
public class GetDragLocation {

    private final Find find;
    private final ActionOptionsForDrag actionOptionsForDrag;

    public GetDragLocation(Find find, ActionOptionsForDrag actionOptionsForDrag) {
        this.find = find;
        this.actionOptionsForDrag = actionOptionsForDrag;
    }

    /**
     * Determines the starting location for a drag operation.
     * <p>
     * This method finds the source location by searching for elements in the first
     * ObjectCollection using configured Find options. The source-specific offsets
     * (addX, addY) from the ActionOptions are applied to adjust the click point
     * relative to the found element's center.
     * 
     * <p><b>Note:</b> There appears to be a bug in this implementation - it returns
     * matches.getBestLocation() instead of matchesForDrag.getBestLocation(), which
     * means the actual find results are discarded.</p>
     * 
     * @param matches The ActionResult containing the drag configuration options
     * @param objectCollections Variable array of collections; the first collection
     *                          is used to find the drag source
     * @return An Optional containing the source location if found, empty otherwise
     */
    public Optional<Location> getFromLocation(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions dragActionOptions = actionOptionsForDrag.getFindFrom(matches.getActionOptions());
        ActionResult matchesForDrag = new ActionResult(dragActionOptions);
        find.perform(matchesForDrag, getDragFromObjColl(objectCollections));
        return matches.getBestLocation();
    }

    /**
     * Determines the destination location for a drag operation.
     * <p>
     * This method finds the target location by searching for elements in the appropriate
     * ObjectCollection (see getDragToObjColl for selection logic). The destination-specific
     * offsets (dragToOffsetX, dragToOffsetY) from the ActionOptions are applied to adjust
     * the drop point relative to the found element's center.
     * 
     * <p><b>Note:</b> Similar to getFromLocation, this method appears to have a bug - it
     * returns matches.getBestLocation() instead of matchesForDrag.getBestLocation(), which
     * means the actual find results are discarded.</p>
     * 
     * @param matches The ActionResult containing the drag configuration options
     * @param objectCollections Variable array of collections used to find the drag destination
     * @return An Optional containing the destination location if found, empty otherwise
     */
    public Optional<Location> getToLocation(ActionResult matches, ObjectCollection... objectCollections) {
        ActionOptions dragActionOptions = actionOptionsForDrag.getFindTo(matches.getActionOptions());
        ActionResult matchesForDrag = new ActionResult(dragActionOptions);
        find.perform(matchesForDrag, getDragToObjColl(objectCollections));
        return matches.getBestLocation();
    }

    /**
     * Selects the appropriate ObjectCollection for finding the drag destination.
     * <p>
     * This method implements a fallback strategy to handle various input scenarios:
     * <ol>
     *   <li><b>Two or more collections:</b> Uses the second collection as destination</li>
     *   <li><b>One collection:</b> Uses the same collection for both source and destination
     *       (enables dragging within the same area)</li>
     *   <li><b>No collections:</b> Returns an empty collection (the calling code should
     *       handle this case, possibly using current mouse position with offsets)</li>
     * </ol>
     * 
     * <p>This design allows flexible drag operations:</p>
     * <ul>
     *   <li>Standard drag: Different source and destination collections</li>
     *   <li>Reposition drag: Same collection with different offsets</li>
     *   <li>Offset-only drag: No collections, relies on mouse position and offsets</li>
     * </ul>
     *
     * @param objectCollections Variable array containing Images, Regions, and Locations
     *                          used to find the drag destination
     * @return The ObjectCollection to use for finding the destination location, or an
     *         empty collection if none are provided
     */
    private ObjectCollection getDragToObjColl(ObjectCollection... objectCollections) {
        if (objectCollections.length >= 2) return objectCollections[1];
        if (objectCollections.length == 1) return objectCollections[0];
        return new ObjectCollection.Builder().build();
    }

    /**
     * Selects the appropriate ObjectCollection for finding the drag source.
     * <p>
     * This method provides intelligent fallback behavior for determining where to
     * start the drag operation:
     * <ul>
     *   <li><b>Collections provided:</b> Uses the first collection to find the source</li>
     *   <li><b>No collections:</b> Creates a collection containing the current mouse
     *       position, enabling drag operations to start from wherever the cursor is</li>
     * </ul>
     * 
     * <p>The fallback to mouse position is particularly useful for:</p>
     * <ul>
     *   <li>User-initiated drags where the start point is the current cursor location</li>
     *   <li>Continuation of drag sequences</li>
     *   <li>Gesture-based interactions that don't require finding a specific element</li>
     * </ul>
     * 
     * @param objectCollections Variable array containing potential source elements
     * @return The ObjectCollection to use for finding the source location, guaranteed
     *         to be non-null (either the first collection or one containing the mouse position)
     */
    private ObjectCollection getDragFromObjColl(ObjectCollection... objectCollections) {
        if (objectCollections.length >= 1) return objectCollections[0];
        return new ObjectCollection.Builder()
                .withLocations(new Location(Mouse.at()))
                .build();
    }

}
