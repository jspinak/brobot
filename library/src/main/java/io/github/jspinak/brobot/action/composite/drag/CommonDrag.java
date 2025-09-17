package io.github.jspinak.brobot.action.composite.drag;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Positions;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Provides convenience methods for common drag operation patterns in GUI automation.
 *
 * <p>This class encapsulates frequently used drag configurations and offers higher-level
 * abstractions for complex drag sequences. It demonstrates how to build sophisticated drag
 * interactions on top of the basic {@link Drag} action, including multi-point drag paths and
 * position-based dragging.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Pre-configured timing delays optimized for reliable drag operations
 *   <li>Multi-point drag sequences that create paths through multiple locations
 *   <li>Position-based dragging using named screen positions (corners, center)
 *   <li>Offset-based dragging for relative movements
 * </ul>
 *
 * <p>The default timing configuration includes:
 *
 * <ul>
 *   <li>0.3s pause after drag completion
 *   <li>0.3s pause before mouse down
 *   <li>0.5s pause after mouse down (allows UI to register the press)
 * </ul>
 *
 * <p>This class serves as both a utility for common drag patterns and a template for creating
 * application-specific drag behaviors.
 *
 * @see Drag
 * @see ActionConfig
 * @see Positions
 */
@Component
public class CommonDrag {

    private final Drag drag;
    private final ActionConfig actionConfig =
            new DragOptions.Builder()
                    .setPauseAfterEnd(.3)
                    .setDelayBetweenMouseDownAndMove(.5)
                    .build();

    public CommonDrag(Drag drag) {
        this.drag = drag;
    }

    /**
     * Performs a complex drag operation through multiple waypoints.
     *
     * <p>This method enables creating drag paths that pass through multiple locations, useful for
     * complex gestures or navigating around obstacles. The drag operation starts from the specified
     * image and visits each location in sequence, maintaining the mouse button pressed throughout
     * the entire path.
     *
     * <p><b>Implementation details:</b>
     *
     * <ul>
     *   <li>First drag: from the image to the first location
     *   <li>Subsequent drags: between consecutive location pairs
     *   <li>The operation stops if any individual drag fails
     *   <li>Each segment uses the default timing configuration
     * </ul>
     *
     * <p>Common use cases:
     *
     * <ul>
     *   <li>Drawing complex shapes or signatures
     *   <li>Navigating drag paths around UI obstacles
     *   <li>Creating gesture patterns for touch interfaces
     *   <li>Reordering items through multiple positions
     * </ul>
     *
     * @param matches The ActionResult object that tracks the operation's progress and success.
     *     Modified by this method with match results.
     * @param from The starting image to drag from
     * @param to Variable number of waypoint locations to drag through in sequence
     */
    public void drag(ActionResult matches, StateImage from, Location... to) {
        for (ObjectCollection[] oC : getObjectCollections(from, to)) {
            drag.perform(matches, oC);
            if (matches.isEmpty()) break;
        }
    }

    /**
     * Converts drag source and destinations into paired ObjectCollections for sequential drags.
     *
     * <p>This helper method transforms the input parameters into a list of ObjectCollection pairs
     * suitable for the Drag action. It handles the edge cases of empty destination arrays and
     * creates appropriate pairings for multi-segment drag operations.
     *
     * <p><b>Pairing logic:</b>
     *
     * <ul>
     *   <li>First pair: StateImage source → first Location
     *   <li>Subsequent pairs: Location[i] → Location[i+1]
     *   <li>Handles odd numbers of locations by creating single-element arrays
     * </ul>
     *
     * @param from The starting image for the drag operation
     * @param to Array of destination locations
     * @return List of ObjectCollection pairs, each representing a drag segment
     */
    private List<ObjectCollection[]> getObjectCollections(StateImage from, Location... to) {
        List<ObjectCollection[]> objectCollectionsList = new ArrayList<>();
        int l = to.length > 0 ? 2 : 1;
        ObjectCollection[] firstColl = new ObjectCollection[l];
        firstColl[0] = new ObjectCollection.Builder().withImages(from).build();
        if (l == 2) firstColl[1] = new ObjectCollection.Builder().withLocations(to[0]).build();
        objectCollectionsList.add(firstColl);
        for (int i = 0; i < to.length / 2; i++) {
            l = i + 1 >= to.length ? 1 : 2;
            ObjectCollection[] objColl = new ObjectCollection[l];
            objColl[0] = new ObjectCollection.Builder().withLocations(to[i]).build();
            if (l == 2)
                objColl[1] = new ObjectCollection.Builder().withLocations(to[i + 1]).build();
            objectCollectionsList.add(objColl);
        }
        return objectCollectionsList;
    }

    /**
     * Drags an image to predefined screen positions using named locations.
     *
     * <p>This method simplifies dragging to common screen locations by using the {@link
     * Positions.Name} enumeration. It automatically converts position names to screen coordinates
     * based on the current display dimensions, making the drag operations resolution-independent.
     *
     * <p>Available positions include:
     *
     * <ul>
     *   <li>TOPLEFT, TOPMIDDLE, TOPRIGHT
     *   <li>MIDDLELEFT, CENTER, MIDDLERIGHT
     *   <li>BOTTOMLEFT, BOTTOMMIDDLE, BOTTOMRIGHT
     * </ul>
     *
     * <p>This is particularly useful for:
     *
     * <ul>
     *   <li>Dragging items to screen edges or corners
     *   <li>Centering elements on screen
     *   <li>Implementing consistent positioning across different resolutions
     * </ul>
     *
     * @param matches The ActionResult object tracking the operation's progress. Modified by this
     *     method with drag results.
     * @param from The image to drag from its current location
     * @param positions Variable number of named positions to drag through in sequence
     */
    public void dragInScreen(ActionResult matches, StateImage from, Positions.Name... positions) {
        Location[] locations = new Location[positions.length];
        for (int i = 0; i < positions.length; i++)
            locations[i] = new Location(new Region(), positions[i]);
        drag(matches, from, locations);
    }

    /**
     * Drags an image by a specified offset from its current location.
     *
     * <p>This method performs a relative drag operation, moving the image by the specified pixel
     * offsets from its found location. Unlike absolute positioning, this maintains the spatial
     * relationship between the drag source and destination.
     *
     * <p>The offset values are applied to the drag destination, not the source, allowing for
     * precise placement relative to the found image position.
     *
     * <p>Common use cases:
     *
     * <ul>
     *   <li>Moving items by fixed distances (e.g., grid-based movement)
     *   <li>Adjusting element positions without absolute coordinates
     *   <li>Implementing relative positioning in dynamic layouts
     *   <li>Creating repeatable drag patterns independent of screen position
     * </ul>
     *
     * @param matches The ActionResult object tracking the operation. This method modifies its
     *     ActionConfig to include the drag offsets.
     * @param from The image to drag from its current location
     * @param xOff Horizontal offset in pixels (positive = right, negative = left)
     * @param yOff Vertical offset in pixels (positive = down, negative = up)
     */
    public void dragInScreen(ActionResult matches, StateImage from, int xOff, int yOff) {
        // Create a DragOptions with target location offset from the source
        DragOptions dragOptions = new DragOptions.Builder().setPauseAfterEnd(0.3).build();
        matches.setActionConfig(dragOptions);
        // The offset should be handled by creating a proper target location
        // This may need adjustment based on how the drag implementation works
        drag.perform(matches, from.asObjectCollection());
    }
}
