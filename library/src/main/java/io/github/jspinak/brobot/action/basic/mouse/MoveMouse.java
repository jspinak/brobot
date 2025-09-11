package io.github.jspinak.brobot.action.basic.mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.internal.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

/**
 * Moves the mouse to one or more locations without embedded Find operations.
 *
 * <p>MoveMouse is a pure action that provides precise cursor control for GUI automation. It only
 * moves the mouse to provided locations, regions, or matches without performing any Find
 * operations. This separation enables better testing, cleaner code, and more flexible action
 * composition through action chains.
 *
 * <p>Movement patterns supported:
 *
 * <ul>
 *   <li><b>Single Target</b>: Direct movement to a specific location
 *   <li><b>Multiple Targets</b>: Sequential movement through multiple locations
 *   <li><b>Region Centers</b>: Movement to the center of regions
 *   <li><b>Match Locations</b>: Movement to previously found matches
 * </ul>
 *
 * <p>Processing order:
 *
 * <ul>
 *   <li>Locations are processed in the order they appear in ObjectCollections
 *   <li>Multiple ObjectCollections are processed sequentially
 * </ul>
 *
 * <p>For Find-then-Move operations, use ConditionalActionChain:
 *
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *         .ifFound(new MouseMoveOptions.Builder().build())
 *         .perform(objectCollection);
 * }</pre>
 *
 * <p>Common use cases:
 *
 * <ul>
 *   <li>Hovering over elements to trigger tooltips or dropdown menus
 *   <li>Positioning cursor for subsequent click or drag operations
 *   <li>Following paths through UI elements for gesture-based interactions
 *   <li>Moving mouse away from active areas to prevent interference
 * </ul>
 *
 * @since 2.0
 * @see MoveMouseWrapper
 * @see ConditionalActionChain for chaining Find with MoveMouse
 */
@Component
public class MoveMouse implements ActionInterface {

    private static final Logger logger = Logger.getLogger(MoveMouse.class.getName());

    private final MoveMouseWrapper moveMouseWrapper;
    private final TimeProvider time;

    public MoveMouse(MoveMouseWrapper moveMouseWrapper, TimeProvider time) {
        this.moveMouseWrapper = moveMouseWrapper;
        this.time = time;
    }

    @Override
    public Type getActionType() {
        return Type.MOVE;
    }

    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        actionResult.setSuccess(false);

        try {
            // Extract locations to move to
            List<Location> locations = extractLocations(objectCollections);

            if (locations.isEmpty()) {
                logger.warning("No locations provided to MoveMouse");
                return;
            }

            // Get configuration if available
            MouseMoveOptions options = null;
            if (actionResult.getActionConfig() instanceof MouseMoveOptions) {
                options = (MouseMoveOptions) actionResult.getActionConfig();
            }

            // Move to each location
            int successCount = 0;
            for (int i = 0; i < locations.size(); i++) {
                Location location = locations.get(i);

                if (moveMouseWrapper.move(location)) {
                    successCount++;
                    actionResult.add(createMatchFromLocation(location));
                    logger.fine("Moved mouse to location: " + location);

                    // Pause between movements (except after last one)
                    if (i < locations.size() - 1 && options != null) {
                        time.wait(options.getPauseAfterEnd());
                    }
                } else {
                    logger.warning("Failed to move mouse to location: " + location);
                }
            }

            actionResult.setSuccess(successCount > 0);
            logger.info(
                    String.format(
                            "MoveMouse: Moved to %d of %d locations",
                            successCount, locations.size()));

        } catch (Exception e) {
            logger.severe("Error in MoveMouse: " + e.getMessage());
            actionResult.setSuccess(false);
        }
    }

    /**
     * Extracts locations from the provided object collections. Supports Location, Region, and Match
     * objects from ActionResult.
     */
    private List<Location> extractLocations(ObjectCollection... collections) {
        List<Location> locations = new ArrayList<>();

        for (ObjectCollection collection : collections) {
            // Extract from StateLocations
            for (StateLocation stateLocation : collection.getStateLocations()) {
                locations.add(stateLocation.getLocation());
            }

            // Extract from StateRegions (use center point)
            for (StateRegion stateRegion : collection.getStateRegions()) {
                Region region = stateRegion.getSearchRegion();
                int centerX = region.x() + region.w() / 2;
                int centerY = region.y() + region.h() / 2;
                locations.add(new Location(centerX, centerY));
            }

            // Note: Matches from ActionResult would need to be handled separately
            // as they're typically passed through the ActionResult, not ObjectCollection
        }

        return locations;
    }

    /** Creates a Match object from a Location for result reporting. */
    private Match createMatchFromLocation(Location location) {
        // Create a small region around the move point
        Region region = new Region(location.getX() - 5, location.getY() - 5, 10, 10);
        Match match = new Match(region);
        match.setName("Mouse moved to location");
        return match;
    }
}
