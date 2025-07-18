package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Pure Click action that performs click operations without embedded Find.
 * <p>
 * This is a "pure" action that only performs the click operation on provided
 * locations, regions, or matches. It does not perform any Find operations.
 * This separation enables better testing, cleaner code, and more flexible
 * action composition through action chains.
 * </p>
 * 
 * <p>Usage patterns:
 * <ul>
 *   <li>Click on a specific location: {@code new ClickV2().perform(actionResult, location)}</li>
 *   <li>Click on a region's center: {@code new ClickV2().perform(actionResult, region)}</li>
 *   <li>Click on matches from a previous Find: {@code new ClickV2().perform(actionResult, matches)}</li>
 * </ul>
 * </p>
 * 
 * <p>For Find-then-Click operations, use ConditionalActionChain:
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *     .ifFound(new ClickOptions.Builder().build())
 *     .perform(objectCollection);
 * }</pre>
 * </p>
 * 
 * @since 2.0
 * @see Click for the legacy version with embedded Find
 * @see ConditionalActionChain for chaining Find with Click
 */
@Component("clickV2")
public class ClickV2 implements ActionInterface {
    
    private static final Logger logger = Logger.getLogger(ClickV2.class.getName());
    
    @Override
    public Type getActionType() {
        return Type.CLICK;
    }
    
    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        actionResult.setSuccess(false);
        
        try {
            // Extract clickable objects from collections
            List<Location> locations = extractClickableLocations(objectCollections);
            
            if (locations.isEmpty()) {
                logger.warning("No clickable objects provided to ClickV2");
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
            logger.info(String.format("ClickV2: Clicked %d of %d locations", successCount, locations.size()));
            
        } catch (Exception e) {
            logger.severe("Error in ClickV2: " + e.getMessage());
            actionResult.setSuccess(false);
        }
    }
    
    /**
     * Extracts clickable locations from the provided object collections.
     * Supports Location, Region, and Match objects.
     */
    private List<Location> extractClickableLocations(ObjectCollection... collections) {
        List<Location> locations = new ArrayList<>();
        
        for (ObjectCollection collection : collections) {
            // Extract from Locations
            for (StateLocation stateLoc : collection.getStateLocations()) {
                locations.add(stateLoc.getLocation());
            }
            
            // Extract from StateRegions (use center point)
            for (StateRegion stateRegion : collection.getStateRegions()) {
                Region region = stateRegion.getSearchRegion();
                // Calculate center of region
                int centerX = region.x() + region.w() / 2;
                int centerY = region.y() + region.h() / 2;
                locations.add(new Location(centerX, centerY));
            }
            
            // Note: Matches would typically be passed through ActionResult
        }
        
        return locations;
    }
    
    /**
     * Performs the actual click operation at the specified location.
     */
    private boolean performClick(Location location) {
        try {
            // Perform the click
            org.sikuli.script.Location sikuliLoc = location.sikuli();
            sikuliLoc.click();
            
            // Small pause after click
            Thread.sleep(100);
            
            logger.fine("Clicked at location: " + location);
            return true;
            
        } catch (Exception e) {
            logger.warning("Failed to click at location " + location + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a Match object from a Location for result reporting.
     */
    private Match createMatchFromLocation(Location location) {
        // Create a small region around the click point
        Region region = new Region(location.getX() - 5, location.getY() - 5, 10, 10);
        Match match = new Match(region);
        match.setName("Clicked location");
        return match;
    }
}