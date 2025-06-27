package io.github.jspinak.brobot.action.internal.find;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateLocation;
import io.github.jspinak.brobot.model.state.StateRegion;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Converts non-image state objects into Match objects for action processing.
 * <p>
 * This component handles the conversion of various non-image elements (regions,
 * locations, and existing matches) from ObjectCollections into Match objects.
 * Unlike image-based finding which requires pattern matching, these objects
 * are directly converted since their positions are already known.
 * 
 * <p>Non-image objects include:</p>
 * <ul>
 * <li>{@link StateRegion} - Predefined regions on screen</li>
 * <li>{@link StateLocation} - Specific screen coordinates</li>
 * <li>Existing {@link Match} objects from previous operations</li>
 * </ul>
 * 
 * @see Match
 * @see ObjectCollection
 * @see ActionResult
 */
@Component
@Setter
public class NonImageObjectConverter {

    /**
     * Converts all non-image objects from an ObjectCollection into Match objects.
     * <p>
     * This method processes regions, locations, and existing matches from the collection,
     * converting each into appropriate Match objects and aggregating them into a single
     * ActionResult.
     * 
     * @param objectCollection The collection containing non-image objects to convert
     * @return An ActionResult containing Match objects created from all non-image
     *         elements in the collection
     */
    public ActionResult getOtherObjectsDirectlyAsMatchObjects(ObjectCollection objectCollection) {
        ActionResult otherMatches = new ActionResult();
        addRegions(otherMatches, objectCollection);
        addLocations(otherMatches, objectCollection);
        addMatches(otherMatches, objectCollection);
        return otherMatches;
    }

    /**
     * Adds existing matches from the ObjectCollection to the ActionResult.
     * <p>
     * This method transfers all pre-existing match results from the collection
     * to the provided ActionResult. The matches parameter is modified by adding
     * all results from the ObjectCollection's matches.
     * 
     * @param matches The ActionResult to which matches will be added. This object
     *                is modified by the method.
     * @param objectCollection The source collection containing existing matches
     */
    public void addMatches(ActionResult matches, ObjectCollection objectCollection) {
        for (ActionResult m : objectCollection.getMatches()) {
            matches.addAllResults(m);
        }
    }

    /**
     * Converts StateRegions from the ObjectCollection into Match objects.
     * <p>
     * Each StateRegion is transformed into a Match object that preserves the region's
     * boundaries, anchor points, and associated state data. Note that text finding
     * within regions is handled separately and not part of this conversion process.
     *
     * @param matches The ActionResult to which region-based matches will be added.
     *                This object is modified by the method.
     * @param objectCollection The source collection containing StateRegions to convert
     */
    public void addRegions(ActionResult matches, ObjectCollection objectCollection) {
        for (StateRegion r : objectCollection.getStateRegions()) {
            Match match = new Match.Builder()
                    .setRegion(r.getSearchRegion())
                    .setStateObjectData(r)
                    .setAnchors(r.getAnchors())
                    .build();
            matches.add(match);
        }
    }

    /**
     * Converts StateLocations from the ObjectCollection into Match objects.
     * <p>
     * Each StateLocation represents a specific point on screen and is converted
     * into a Match object centered at that location. The original state data
     * is preserved in the resulting Match.
     * 
     * @param matches The ActionResult to which location-based matches will be added.
     *                This object is modified by the method.
     * @param objectCollection The source collection containing StateLocations to convert
     */
    public void addLocations(ActionResult matches, ObjectCollection objectCollection) {
        for (StateLocation stateLocation : objectCollection.getStateLocations()) {
            Match match = new Match.Builder()
                    .setMatch(stateLocation.getLocation().toMatch())
                    .setStateObjectData(stateLocation)
                    .build();
            matches.add(match);
        }
    }

}
