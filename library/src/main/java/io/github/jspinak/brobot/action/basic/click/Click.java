package io.github.jspinak.brobot.action.basic.click;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import io.github.jspinak.brobot.util.coordinates.CoordinateScaler;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
@Component
public class Click implements ActionInterface {

    // Logger is now provided by @Slf4j annotation as 'log'

    @Autowired(required = false)
    private CoordinateScaler coordinateScaler;

    @Override
    public Type getActionType() {
        return Type.CLICK;
    }

    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        // Handle null ActionResult gracefully
        if (actionResult == null) {
            log.warn("Click: ActionResult is null, cannot proceed");
            return;
        }

        actionResult.setSuccess(false);

        try {
            // Extract clickable objects from collections
            List<Location> locations = extractClickableLocations(objectCollections);

            if (locations.isEmpty()) {
                log.warn("No clickable objects provided to Click");
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
            log.info(
                    String.format(
                            "Click: Clicked %d of %d locations", successCount, locations.size()));

        } catch (Exception e) {
            log.error("Error in Click: {}", e.getMessage(), e);
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
            log.warn("Cannot click null location");
            return false;
        }

        try {
            // In mock mode, simulate the click without actual operations
            if (MockModeManager.isMockMode()) {
                log.debug("[MOCK] Simulated click at location: {}", location);
                // Very small simulated pause for consistency (reduced for performance tests)
                Thread.sleep(1);
                return true;
            }

            // Debug logging for headless issue
            log.debug("=== Click Debug Info ===");
            log.debug("java.awt.headless property: {}", System.getProperty("java.awt.headless"));
            log.debug(
                    "GraphicsEnvironment.isHeadless(): {}",
                    java.awt.GraphicsEnvironment.isHeadless());
            log.debug("Location to click (capture coords): {}", location);

            // Scale coordinates if needed (from physical capture to logical SikuliX)
            org.sikuli.script.Location sikuliLoc;
            if (coordinateScaler != null && coordinateScaler.isScalingNeeded()) {
                sikuliLoc = coordinateScaler.scaleLocationToLogical(location);
                log.debug("Scaled to logical coords: {}", sikuliLoc);
                double[] factors = coordinateScaler.getScaleFactors();
                log.debug(
                        "Scale factors: X={}, Y={}",
                        String.format("%.3f", factors[0]),
                        String.format("%.3f", factors[1]));
            } else {
                sikuliLoc = location.sikuli();
                log.debug("No scaling needed, using original coords");
            }
            log.debug("========================");

            // Use SikuliX directly - it handles Robot internally
            // Following Brobot 1.0.7 pattern of simplicity
            sikuliLoc.click();

            // Small pause after click
            Thread.sleep(100);

            log.debug("Clicked at location: {}", location);
            return true;

        } catch (Exception e) {
            log.warn("Failed to click at location {}: {}", location, e.getMessage());
            log.warn("Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                log.warn("Cause: {}", e.getCause().getMessage());
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
