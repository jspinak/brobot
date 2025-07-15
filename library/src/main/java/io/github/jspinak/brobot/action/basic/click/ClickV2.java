package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.exception.ActionFailedException;
import org.sikuli.script.Mouse;
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
 *   <li>Click on a specific location: {@code new ClickV2().perform(clickOptions, location)}</li>
 *   <li>Click on a region's center: {@code new ClickV2().perform(clickOptions, region)}</li>
 *   <li>Click on matches from a previous Find: {@code new ClickV2().perform(clickOptions, matches)}</li>
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
    public ActionResult perform(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        if (!(actionConfig instanceof ClickOptions)) {
            throw new IllegalArgumentException("ClickV2 requires ClickOptions configuration");
        }
        
        ClickOptions clickOptions = (ClickOptions) actionConfig;
        ActionResult result = new ActionResult();
        result.setActionType("CLICK_V2");
        
        try {
            // Extract clickable objects from collections
            List<Location> locations = extractClickableLocations(objectCollections);
            
            if (locations.isEmpty()) {
                result.setSuccess(false);
                result.setText("No clickable objects provided");
                return result;
            }
            
            // Perform clicks based on configuration
            for (Location location : locations) {
                boolean clickSuccess = performClick(location, clickOptions);
                if (clickSuccess) {
                    result.addMatch(createMatchFromLocation(location));
                }
                
                // Handle click until configuration
                if (clickOptions.isClickUntil() && !clickSuccess) {
                    break;
                }
            }
            
            result.setSuccess(!result.getMatchList().isEmpty());
            result.setText(String.format("Clicked %d of %d locations", 
                result.getMatchList().size(), locations.size()));
            
        } catch (Exception e) {
            logger.severe("Error in ClickV2: " + e.getMessage());
            result.setSuccess(false);
            result.setText("Click failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Extracts clickable locations from the provided object collections.
     * Supports Location, Region, Match, and any object with getLocation() method.
     */
    private List<Location> extractClickableLocations(ObjectCollection... collections) {
        List<Location> locations = new ArrayList<>();
        
        for (ObjectCollection collection : collections) {
            // Extract from Locations
            locations.addAll(collection.getLocations());
            
            // Extract from Regions (use center point)
            collection.getStateRegions().stream()
                .map(stateRegion -> stateRegion.getSearchRegion().getCenter())
                .forEach(locations::add);
            
            // Extract from Matches
            collection.getMatches().getMatchList().stream()
                .map(match -> match.getRegion().getCenter())
                .forEach(locations::add);
        }
        
        return locations;
    }
    
    /**
     * Performs the actual click operation at the specified location.
     */
    private boolean performClick(Location location, ClickOptions options) {
        try {
            // Move to location if needed
            if (options.isMoveMouseFirst()) {
                Mouse.move(location.getSikuliLocation());
                Thread.sleep((long)(options.getPauseAfterMouseMove() * 1000));
            }
            
            // Perform the click based on type
            switch (options.getClickType()) {
                case LEFT:
                    Mouse.click(location.getSikuliLocation());
                    break;
                case RIGHT:
                    Mouse.rightClick(location.getSikuliLocation());
                    break;
                case MIDDLE:
                    Mouse.middleClick(location.getSikuliLocation());
                    break;
                case DOUBLE:
                    Mouse.doubleClick(location.getSikuliLocation());
                    break;
            }
            
            // Apply post-click delay
            if (options.getPauseAfterClick() > 0) {
                Thread.sleep((long)(options.getPauseAfterClick() * 1000));
            }
            
            logger.info("Clicked at location: " + location);
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
        Region region = new Region(location.getX() - 5, location.getY() - 5, 10, 10);
        Match match = new Match(region, 1.0, "");
        match.setText("Clicked location");
        return match;
    }
}