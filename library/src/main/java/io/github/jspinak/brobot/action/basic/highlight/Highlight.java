package io.github.jspinak.brobot.action.basic.highlight;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateRegion;
import io.github.jspinak.brobot.tools.history.draw.HighlightMatchedRegionV2;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.springframework.stereotype.Component;

/**
 * Highlights visual matches on screen in the Brobot model-based GUI automation framework.
 * 
 * <p>Highlight is a diagnostic and demonstration action in the Action Model (α) that provides 
 * visual feedback by drawing attention to matched elements on the screen. It serves both 
 * debugging and presentation purposes, making automation behavior visible and verifiable 
 * to developers and stakeholders.</p>
 * 
 * <p>Highlighting modes:
 * <ul>
 *   <li><b>All at Once</b>: Simultaneously highlights all matches, useful for showing 
 *       multiple found elements or validating pattern matching accuracy</li>
 *   <li><b>One at a Time</b>: Sequentially highlights each match, ideal for demonstrating 
 *       the order of operations or focusing attention on individual elements</li>
 * </ul>
 * </p>
 * 
 * <p>Key features:
 * <ul>
 *   <li><b>Visual Feedback</b>: Draws colored rectangles around matched regions</li>
 *   <li><b>Configurable Duration</b>: Control how long highlights remain visible</li>
 *   <li><b>Non-invasive</b>: Does not interfere with GUI state or functionality</li>
 *   <li><b>Match Integration</b>: Works seamlessly with Find results</li>
 * </ul>
 * </p>
 * 
 * <p>Common use cases:
 * <ul>
 *   <li>Debugging pattern matching to verify correct element identification</li>
 *   <li>Demonstrating automation scripts to stakeholders</li>
 *   <li>Creating visual documentation of automation flows</li>
 *   <li>Validating search regions and match accuracy</li>
 *   <li>Training new users on how the framework identifies GUI elements</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Highlight actions provide transparency into the framework's 
 * visual perception layer. They help bridge the gap between the abstract state model and 
 * the concrete visual representation, making it easier to understand and debug how the 
 * framework interprets and interacts with the GUI.</p>
 * 
 * <p>Note: Highlight operations are typically disabled in production environments to avoid 
 * performance overhead and visual distractions during automated execution.</p>
 * 
 * @since 1.0
 * @see Find
 * @see Match
 * @see HighlightOptions
 * @see HighlightMatchedRegionV2
 */
@Component
public class Highlight implements ActionInterface {

    @Override
    public Type getActionType() {
        return Type.HIGHLIGHT;
    }

    private final Find find;
    private final HighlightMatchedRegionV2 highlightMatch;
    private final TimeProvider time;

    public Highlight(Find find, HighlightMatchedRegionV2 highlightMatch, TimeProvider time) {
        this.find = find;
        this.highlightMatch = highlightMatch;
        this.time = time;
    }

    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting HighlightOptions or ActionConfig
        ActionConfig config = matches.getActionConfig();
        
        // Extract highlight-specific settings
        boolean highlightAllAtOnce = false;
        double highlightSeconds = 1.0;
        if (config instanceof HighlightOptions) {
            HighlightOptions highlightOptions = (HighlightOptions) config;
            highlightAllAtOnce = highlightOptions.isHighlightAllAtOnce();
            highlightSeconds = highlightOptions.getHighlightSeconds();
        }
        
        // Only run Find if we have images or other searchable objects
        if (objectCollections.length > 0 && !hasOnlyRegions(objectCollections)) {
            find.perform(matches, objectCollections);
        } else if (objectCollections.length > 0) {
            // For regions only, convert them directly to matches without Find
            for (ObjectCollection collection : objectCollections) {
                for (StateRegion stateRegion : collection.getStateRegions()) {
                    Match match = new Match.Builder()
                        .setRegion(stateRegion.getSearchRegion())
                        .setSimScore(1.0) // Perfect score for direct regions
                        .setStateObjectData(stateRegion)
                        .build();
                    matches.add(match);
                }
            }
            matches.setSuccess(true);
        }
        
        if (highlightAllAtOnce) {
            highlightAllAtOnce(matches, config, highlightSeconds);
        } else {
            highlightOneAtATime(matches, config);
        }
    }

    private void highlightAllAtOnce(ActionResult matches, ActionConfig config, double highlightSeconds) {
        matches.getMatchList().forEach(match ->
                highlightMatch.turnOn(match, match.getStateObjectData(), config));
        time.wait(highlightSeconds);
        matches.getMatchList().forEach(highlightMatch::turnOff);
    }

    private void highlightOneAtATime(ActionResult matches, ActionConfig config) {
        for (Match match : matches.getMatchList()) {
            highlightMatch.highlight(match, match.getStateObjectData(), config);
            if (matches.getMatchList().indexOf(match) < matches.getMatchList().size() - 1)
                time.wait(config.getPauseAfterEnd());
        }
    }
    
    private boolean hasOnlyRegions(ObjectCollection[] objectCollections) {
        boolean hasRegions = false;
        for (ObjectCollection collection : objectCollections) {
            if (!collection.getStateImages().isEmpty() || 
                !collection.getStateLocations().isEmpty() || 
                !collection.getStateStrings().isEmpty() ||
                !collection.getMatches().isEmpty() ||
                !collection.getScenes().isEmpty()) {
                return false;
            }
            if (!collection.getStateRegions().isEmpty()) {
                hasRegions = true;
            }
        }
        return hasRegions;
    }
}
