package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.MatchAdjustmentOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manages offset-based location adjustments for find and action operations.
 * <p>
 * This component handles the creation of synthetic matches based on position offsets,
 * enabling actions at relative positions from found elements or absolute screen
 * coordinates.
 * </p>
 * <p>
 * This is version 2 of the OffsetLocationManager, updated to work with the new
 * ActionConfig hierarchy. The functionality for secondary offsets (addX2/addY2) has
 * been removed as this is now handled through action chaining.
 * </p>
 * 
 * @see MatchAdjustmentOptions
 * @see Match
 * @see Location
 * @since 2.0
 */
@Component
public class OffsetMatchCreator {
    
    // No longer needs legacy dependency - this class is now standalone

    /**
     * Creates a match at an offset position when no other matches exist.
     * <p>
     * This method is typically called at the beginning of a FIND action when there are
     * no searchable objects in the collections, or when an action needs to operate at
     * a specific offset from the current mouse position. The offset creates a synthetic
     * match that enables actions at arbitrary screen locations.
     * </p>
     * 
     * @param objectCollections The collections being searched in the FIND action
     * @param matches The ActionResult to which the offset match will be added
     * @param adjustmentOptions The adjustment options containing offset values (addX/addY)
     * @param doOnlyWhenCollectionsAreEmpty If true, only creates offset match when all
     *                                      collections are empty
     */
    public void addOffsetAsOnlyMatch(List<ObjectCollection> objectCollections, 
                                   ActionResult matches,
                                   MatchAdjustmentOptions adjustmentOptions,
                                   boolean doOnlyWhenCollectionsAreEmpty) {
        if (adjustmentOptions == null || (adjustmentOptions.getAddX() == 0 && adjustmentOptions.getAddY() == 0)) {
            return;
        }
        
        if (!areCollectionsEmpty(objectCollections) && doOnlyWhenCollectionsAreEmpty) {
            return;
        }
        
        // Create a location at the current mouse position plus offset
        Location location = new Location(
            Mouse.at().x + adjustmentOptions.getAddX(), 
            Mouse.at().y + adjustmentOptions.getAddY()
        );
        
        // Create the offset match with a 1x1 region at the location
        // Note: In the new architecture, offset matches don't need StateObjectMetadata
        Region offsetRegion = new Region(location.getX(), location.getY(), 1, 1);
        Match offsetMatch = new Match(offsetRegion);
        offsetMatch.setTarget(location);
        
        matches.add(offsetMatch);
    }
    
    /**
     * Checks if all provided object collections are empty.
     * <p>
     * This utility method determines whether any searchable objects exist across all
     * collections. It's used to decide whether offset-only matches should be created
     * when no actual pattern matching can occur.
     * </p>
     * 
     * @param objectCollections The list of collections to check
     * @return true if all collections are empty, false otherwise
     */
    private boolean areCollectionsEmpty(List<ObjectCollection> objectCollections) {
        for (ObjectCollection objColl : objectCollections) {
            if (!objColl.isEmpty()) return false;
        }
        return true;
    }
}