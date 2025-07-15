package io.github.jspinak.brobot.action.basic.highlight;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.match.Match;
import org.sikuli.script.Screen;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Pure Highlight action that highlights regions without embedded Find.
 * <p>
 * This is a "pure" action that only performs the highlight operation on provided
 * regions or matches. It does not perform any Find operations. This separation
 * enables better testing, cleaner code, and more flexible action composition.
 * </p>
 * 
 * <p>Usage patterns:
 * <ul>
 *   <li>Highlight a specific region: {@code new HighlightV2().perform(highlightOptions, region)}</li>
 *   <li>Highlight matches from a Find: {@code new HighlightV2().perform(highlightOptions, matches)}</li>
 * </ul>
 * </p>
 * 
 * <p>For Find-then-Highlight operations, use ConditionalActionChain:
 * <pre>{@code
 * ConditionalActionChain.find(findOptions)
 *     .ifFound(new HighlightOptions.Builder().build())
 *     .perform(objectCollection);
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
    public ActionResult perform(ActionConfig actionConfig, ObjectCollection... objectCollections) {
        if (!(actionConfig instanceof HighlightOptions)) {
            throw new IllegalArgumentException("HighlightV2 requires HighlightOptions configuration");
        }
        
        HighlightOptions highlightOptions = (HighlightOptions) actionConfig;
        ActionResult result = new ActionResult();
        result.setActionType("HIGHLIGHT_V2");
        
        try {
            // Extract highlightable regions from collections
            List<Region> regions = extractHighlightableRegions(objectCollections);
            
            if (regions.isEmpty()) {
                result.setSuccess(false);
                result.setText("No highlightable regions provided");
                return result;
            }
            
            // Highlight each region
            int highlightedCount = 0;
            for (Region region : regions) {
                if (highlightRegion(region, highlightOptions)) {
                    result.addMatch(createMatchFromRegion(region));
                    highlightedCount++;
                }
            }
            
            result.setSuccess(highlightedCount > 0);
            result.setText(String.format("Highlighted %d of %d regions", highlightedCount, regions.size()));
            
        } catch (Exception e) {
            logger.severe("Error in HighlightV2: " + e.getMessage());
            result.setSuccess(false);
            result.setText("Highlight failed: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Extracts highlightable regions from the provided object collections.
     * Supports Region, StateRegion, and Match objects.
     */
    private List<Region> extractHighlightableRegions(ObjectCollection... collections) {
        List<Region> regions = new ArrayList<>();
        
        for (ObjectCollection collection : collections) {
            // Extract from StateRegions
            collection.getStateRegions().stream()
                .map(stateRegion -> stateRegion.getSearchRegion())
                .forEach(regions::add);
            
            // Extract from Matches
            collection.getMatches().getMatchList().stream()
                .map(Match::getRegion)
                .forEach(regions::add);
            
            // Note: Locations don't have bounds, so they can't be highlighted
            // Users should convert locations to small regions if needed
        }
        
        return regions;
    }
    
    /**
     * Performs the actual highlight operation on the specified region.
     */
    private boolean highlightRegion(Region region, HighlightOptions options) {
        try {
            org.sikuli.script.Region sikuliRegion = region.getSikuliRegion();
            
            // Apply highlight color
            String color = options.getHighlightColor();
            if (color != null && !color.isEmpty()) {
                sikuliRegion.setHighlightColor(color);
            }
            
            // Perform the highlight
            double duration = options.getHighlightDuration();
            if (duration > 0) {
                sikuliRegion.highlight(duration);
            } else {
                // Default to 2 seconds if duration not specified
                sikuliRegion.highlight(2);
            }
            
            logger.info("Highlighted region: " + region);
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
        Match match = new Match(region, 1.0, "");
        match.setText("Highlighted region");
        return match;
    }
}