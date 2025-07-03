package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.StateObjectMetadata;
import io.github.jspinak.brobot.model.state.StateLocation;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages offset-based location adjustments for find and action operations.
 * <p>
 * This component handles the creation of synthetic matches based on position offsets,
 * enabling actions at relative positions from found elements or absolute screen
 * coordinates. It supports two primary offset strategies:
 * <ul>
 *   <li>Initial offsets - Used when no objects are found or as a starting point</li>
 *   <li>Relative offsets - Applied to existing matches for nearby interactions</li>
 * </ul>
 * 
 * <p>Offset operations are essential for:
 * <ul>
 *   <li>Clicking near but not directly on found elements</li>
 *   <li>Drag operations that start or end at offset positions</li>
 *   <li>Interacting with dynamic UI elements that appear relative to others</li>
 *   <li>Working with elements that can't be directly matched</li>
 * </ul>
 * 
 * @see ActionOptions
 * @see Match
 * @see Location
 */
@Component
public class OffsetLocationManager {

    /**
     * Adds an offset match based on the current context and match state.
     * <p>
     * This method intelligently determines whether to add an offset as the only match
     * (when no matches exist) or as an additional match relative to the last found match.
     * The decision is based on whether matches have already been found.
     * 
     * @param objectCollections The collections being searched
     * @param matches The current match results. Modified by adding offset matches.
     * @param actionOptions Configuration containing offset values (addX, addY, addX2, addY2)
     */
    public void addOffset(List<ObjectCollection> objectCollections, ActionResult matches, ActionOptions actionOptions) {
        if (matches.isEmpty()) addOffsetAsOnlyMatch(objectCollections, matches, false);
        else addOffsetAsLastMatch(matches, actionOptions);
    }

    /**
     * Creates a match at an offset position when no other matches exist.
     * <p>
     * This method is typically called at the beginning of a FIND action when there are
     * no searchable objects in the collections, or when an action needs to operate at
     * a specific offset from the current mouse position. The offset creates a synthetic
     * match that enables actions at arbitrary screen locations.
     * 
     * <p>The method only creates an offset match if:
     * <ul>
     *   <li>The X offset (addX) is non-zero</li>
     *   <li>Either the collections are empty OR doOnlyWhenCollectionsAreEmpty is false</li>
     * </ul>
     * 
     * @param objectCollections The collections being searched in the FIND action
     * @param matches The ActionResult to which the offset match will be added. This object
     *                is modified by adding a new match at the offset position.
     * @param doOnlyWhenCollectionsAreEmpty If true, only creates offset match when all
     *                                      collections are empty. If false, always creates
     *                                      the offset match when addX is non-zero.
     */
    public void addOffsetAsOnlyMatch(List<ObjectCollection> objectCollections, ActionResult matches, boolean doOnlyWhenCollectionsAreEmpty) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.getAddX() == 0) return;
        if (!areCollectionsEmpty(objectCollections) && doOnlyWhenCollectionsAreEmpty) return;
        Location location = new Location(Mouse.at(), actionOptions.getAddX(), actionOptions.getAddY());
        double duration = Duration.between(matches.getStartTime(), LocalDateTime.now()).toSeconds();
        StateLocation stateLocation = new StateLocation.Builder().setLocation(location).build();
        Match offsetMatch = new Match.Builder()
                .setMatch(location.toMatch())
                .setStateObjectData(stateLocation)
                .build();
        matches.add(offsetMatch);
    }

    /**
     * Adds an offset match relative to the last found match.
     * <p>
     * This method is called after the FIND action completes to add a final match at an
     * offset position relative to the last found match. This is particularly useful for
     * MOVE and DRAG actions that need to end at a position relative to a found element
     * rather than directly on it.
     * 
     * <p>The offset match inherits the state object data from the last match, maintaining
     * the context of which UI element the offset is relative to. The method only creates
     * an offset if:
     * <ul>
     *   <li>The X2 offset (addX2) is non-zero</li>
     *   <li>There is at least one existing match to offset from</li>
     * </ul>
     * 
     * @param matches The ActionResult containing found matches. Modified by appending a new
     *                match at the offset position from the last match.
     * @param actionOptions Configuration containing the offset values (addX2, addY2) to apply
     *                      to the last match position
     */
    public void addOffsetAsLastMatch(ActionResult matches, ActionOptions actionOptions) {
        if (actionOptions.getAddX2() == 0) return;
        if (matches.isEmpty()) return;
        Match lastMatch = matches.getMatchList().get(matches.getMatchList().size() - 1);
        Location offsetLocation = new Location(
                lastMatch.x() + actionOptions.getAddX2(), lastMatch.y() + actionOptions.getAddY2());
        Match lastMatchObject = matches.getMatchList().get(matches.getMatchList().size() - 1);
        StateObjectMetadata stateObjectData = lastMatchObject.getStateObjectData();
        //double duration = lastMatchObject.getDuration();
        Match offsetMatch = offsetLocation.toMatch();
        Match offsetMatchObject = new Match.Builder()
                .setMatch(offsetMatch)
                .setStateObjectData(stateObjectData)
                .build();
        matches.getMatchList().add(offsetMatchObject);
    }

    /**
     * Checks if all provided object collections are empty.
     * <p>
     * This utility method determines whether any searchable objects exist across all
     * collections. It's used to decide whether offset-only matches should be created
     * when no actual pattern matching can occur.
     * 
     * @param objectCollections The list of collections to check
     * @return true if all collections are empty (contain no searchable objects),
     *         false if at least one collection contains objects
     */
    private boolean areCollectionsEmpty(List<ObjectCollection> objectCollections) {
        for (ObjectCollection objColl : objectCollections) {
            if (!objColl.isEmpty()) return false;
        }
        return true;
    }

}
