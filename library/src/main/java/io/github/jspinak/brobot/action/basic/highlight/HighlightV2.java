package io.github.jspinak.brobot.action.basic.highlight;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Pure Highlight action that highlights regions without embedded Find.
 * <p>
 * This is a "pure" action that only performs the highlight operation on
 * provided
 * regions or matches. It does not perform any Find operations. This separation
 * enables better testing, cleaner code, and more flexible action composition.
 * </p>
 * 
 * <p>
 * Usage patterns:
 * <ul>
 * <li>Highlight a specific region:
 * {@code new HighlightV2().perform(actionResult, region)}</li>
 * <li>Highlight matches from a Find:
 * {@code new HighlightV2().perform(actionResult, matches)}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * For Find-then-Highlight operations, use ConditionalActionChain:
 * 
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *         .ifFound(new HighlightOptions.Builder().build())
 *         .perform(objectCollection);
 * }</pre>
 * </p>
 * 
 * @since 2.0
 * @see Highlight for the legacy version with embedded Find
 * @see ConditionalActionChain for chaining Find with Highlight
 */
@Component("highlightV2")
public class HighlightV2 implements ActionInterface {

    private static final Logger logger = Logger.getLogger(HighlightV2.class.getName());

    @Override
    public Type getActionType() {
        return Type.HIGHLIGHT;
    }

    @Override
    public void perform(ActionResult actionResult, ObjectCollection... objectCollections) {
        actionResult.setSuccess(false);

        try {
            // Extract highlightable regions from collections
            List<Region> regions = extractHighlightableRegions(objectCollections);

            if (regions.isEmpty()) {
                logger.warning("No highlightable regions provided to HighlightV2");
                return;
            }

            // Highlight each region
            int highlightedCount = 0;
            for (Region region : regions) {
                if (highlightRegion(region)) {
                    actionResult.add(createMatchFromRegion(region));
                    highlightedCount++;
                }
            }

            actionResult.setSuccess(highlightedCount > 0);
            logger.info(String.format("HighlightV2: Highlighted %d of %d regions", highlightedCount, regions.size()));

        } catch (Exception e) {
            logger.severe("Error in HighlightV2: " + e.getMessage());
            actionResult.setSuccess(false);
        }
    }

    /**
     * Extracts highlightable regions from the provided object collections.
     * Supports Region and StateRegion objects.
     */
    private List<Region> extractHighlightableRegions(ObjectCollection... collections) {
        List<Region> regions = new ArrayList<>();

        for (ObjectCollection collection : collections) {
            // Extract regions from StateRegions
            for (StateRegion stateRegion : collection.getStateRegions()) {
                regions.add(stateRegion.getSearchRegion());
            }
        }

        return regions;
    }

    /**
     * Performs the actual highlight operation on the specified region.
     */
    private boolean highlightRegion(Region region) {
        try {
            // Get Sikuli region and highlight
            org.sikuli.script.Region sikuliRegion = region.sikuli();

            // Perform the highlight with default duration
            sikuliRegion.highlight(2.0);

            logger.fine("Highlighted region: " + region);
            return true;

        } catch (Exception e) {
            logger.warning("Failed to highlight region " + region + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a Match object from a Region for result reporting.
     */
    private Match createMatchFromRegion(Region region) {
        Match match = new Match(region);
        match.setName("Highlighted region");
        return match;
    }
}