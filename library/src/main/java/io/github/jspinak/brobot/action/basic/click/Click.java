package io.github.jspinak.brobot.action.basic.click;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ConditionalActionChain;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.config.mock.MockModeManager;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;

/**
 * Performs click operations on GUI elements without embedded Find operations.
 *
 * <p>Click is a pure action that only performs the click operation on provided locations, regions,
 * or matches. It does not perform any Find operations. This separation enables better testing,
 * cleaner code, and more flexible action composition through action chains.
 *
 * <p>Usage patterns:
 *
 * <ul>
 *   <li>Click on a specific location: {@code new Click().perform(actionResult, location)}
 *   <li>Click on a region's center: {@code new Click().perform(actionResult, region)}
 *   <li>Click on matches from a previous Find: {@code new Click().perform(actionResult, matches)}
 * </ul>
 *
 * <p>For Find-then-Click operations, use ConditionalActionChain:
 *
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *         .ifFoundClick()
 *         .perform(objectCollection);
 * }</pre>
 *
 * @since 2.0
 * @see ConditionalActionChain for chaining Find with Click
 */
@Component
public class Click implements ActionInterface {

    private static final Logger logger = Logger.getLogger(Click.class.getName());

    @Override
    public Type getActionType() {
        return Type.CLICK;
    }

    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        // Handle null ActionResult gracefully
        if (actionResult == null) {
            logger.warning("Click: ActionResult is null, cannot proceed");
            return;
        }

        actionResult.setSuccess(false);

        try {
            // Extract clickable objects from collections
            List<Location> locations = extractClickableLocations(objectCollections);

            if (locations.isEmpty()) {
                logger.warning("No clickable objects provided to Click");
                return;
            }

            // Perform clicks
            int successCount = 0;
            for (Location location : locations) {
                if (performClick(location)) {
                    actionResult.add(createMatchFromLocation(location));
                    successCount++;
                }
            }

            actionResult.setSuccess(successCount > 0);
            logger.info(
                    String.format(
                            "Click: Clicked %d of %d locations", successCount, locations.size()));

        } catch (Exception e) {
            logger.severe("Error in Click: " + e.getMessage());
            actionResult.setSuccess(false);
        }
    }

    /**
     * Extracts clickable locations from the provided object collections. Supports Location, Region,
     * and Match objects.
     */
    private List<Location> extractClickableLocations(ObjectCollection... collections) {
        List<Location> locations = new ArrayList<>();

        // Handle null or empty collections array
        if (collections == null || collections.length == 0) {
            return locations;
        }

        for (ObjectCollection collection : collections) {
            // Skip null collections
            if (collection == null) {
                continue;
            }

            // Extract from Locations
            for (StateLocation stateLoc : collection.getStateLocations()) {
                if (stateLoc != null && stateLoc.getLocation() != null) {
                    locations.add(stateLoc.getLocation());
                }
            }

            // Extract from StateRegions (use center point)
            for (StateRegion stateRegion : collection.getStateRegions()) {
                if (stateRegion != null && stateRegion.getSearchRegion() != null) {
                    Region region = stateRegion.getSearchRegion();
                    // Calculate center of region
                    int centerX = region.x() + region.w() / 2;
                    int centerY = region.y() + region.h() / 2;
                    locations.add(new Location(centerX, centerY));
                }
            }

            // Note: StateImages should be handled by Action class, not Click directly.
            // Action performs Find first, then passes the found Matches/Locations to Click.
        }

        return locations;
    }

    /** Performs the actual click operation at the specified location. */
    private boolean performClick(Location location) {
        // Handle null location
        if (location == null) {
            logger.warning("Cannot click null location");
            return false;
        }

        try {
            // In mock mode, simulate the click without actual operations
            if (MockModeManager.isMockMode()) {
                logger.fine("[MOCK] Simulated click at location: " + location);
                // Very small simulated pause for consistency (reduced for performance tests)
                Thread.sleep(1);
                return true;
            }

            // Debug logging for headless issue
            logger.info("=== Click Debug Info ===");
            logger.info("java.awt.headless property: " + System.getProperty("java.awt.headless"));
            logger.info(
                    "GraphicsEnvironment.isHeadless(): "
                            + java.awt.GraphicsEnvironment.isHeadless());
            logger.info("Location to click: " + location);
            logger.info("========================");

            // Use SikuliX directly - it handles Robot internally
            // Following Brobot 1.0.7 pattern of simplicity
            org.sikuli.script.Location sikuliLoc = location.sikuli();
            sikuliLoc.click();

            // Small pause after click
            Thread.sleep(100);

            logger.fine("Clicked at location: " + location);
            return true;

        } catch (Exception e) {
            logger.warning("Failed to click at location " + location + ": " + e.getMessage());
            logger.warning("Exception type: " + e.getClass().getName());
            if (e.getCause() != null) {
                logger.warning("Cause: " + e.getCause().getMessage());
            }
            return false;
        }
    }

    /** Creates a Match object from a Location for result reporting. */
    private Match createMatchFromLocation(Location location) {
        // Create a small region around the click point
        Region region = new Region(location.getX() - 5, location.getY() - 5, 10, 10);
        Match match = new Match(region);
        match.setName("Clicked location");
        return match;
    }
}
