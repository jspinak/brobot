package io.github.jspinak.brobot.action.internal.find;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;

/**
 * Converts pre-defined regions from state objects into match results without performing searches.
 *
 * <p>This utility class provides a fast alternative to image searching when the locations of
 * objects are already known. Instead of performing computationally expensive pattern matching, it
 * creates {@link Match} objects directly from the {@link Region} definitions stored within {@link
 * Pattern} objects.
 *
 * <p>This approach is useful in scenarios where:
 *
 * <ul>
 *   <li>Object positions are fixed and known in advance
 *   <li>Previous searches have established reliable positions
 *   <li>Performance optimization is needed by bypassing image recognition
 * </ul>
 *
 * @see Pattern#getRegions()
 * @see Match
 * @see ActionResult
 */
@Component
public class DefinedRegionConverter {

    /**
     * Creates match objects from pre-defined regions within an object collection.
     *
     * <p>This method iterates through all {@link StateImage} objects in the collection, extracting
     * any defined regions from their patterns. Each region is converted into a {@link Match} object
     * that includes:
     *
     * <ul>
     *   <li>The region's location and dimensions
     *   <li>The source pattern's image (for potential image extraction)
     *   <li>Reference to the parent StateImage for context
     * </ul>
     *
     * <p>The created matches are added to the provided ActionResult, allowing this method to
     * accumulate results across multiple object collections.
     *
     * @param matches The ActionResult to which new Match objects will be added. This parameter is
     *     modified by the method and also returned.
     * @param objectCollection The collection containing StateImages with pre-defined regions to
     *     convert into matches. May contain zero or more StateImages, each with zero or more
     *     patterns.
     * @return The same ActionResult instance passed as input, now containing additional Match
     *     objects created from the defined regions.
     * @implNote This method modifies the passed ActionResult by adding new matches, making it
     *     suitable for chaining or accumulating results from multiple sources.
     */
    public ActionResult useRegion(ActionResult matches, ObjectCollection objectCollection) {
        for (StateImage si : objectCollection.getStateImages()) {
            for (Pattern pattern : si.getPatterns()) {
                for (Region region : pattern.getRegions()) {
                    matches.add(
                            new Match.Builder()
                                    .setRegion(region)
                                    .setSearchImage(pattern.getBImage())
                                    .setStateObjectData(si)
                                    .build());
                }
            }
        }
        return matches;
    }
}
