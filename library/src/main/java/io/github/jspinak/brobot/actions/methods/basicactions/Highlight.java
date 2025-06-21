package io.github.jspinak.brobot.actions.methods.basicactions;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.HighlightMatch;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
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
 * @see ActionOptions
 * @see HighlightMatch
 */
@Component
public class Highlight implements ActionInterface {

    private final Find find;
    private final HighlightMatch highlightMatch;
    private final Time time;

    public Highlight(Find find, HighlightMatch highlightMatch, Time time) {
        this.find = find;
        this.highlightMatch = highlightMatch;
        this.time = time;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        find.perform(matches, objectCollections);
        if (actionOptions.isHighlightAllAtOnce()) highlightAllAtOnce(matches, actionOptions);
        else highlightOneAtATime(matches, actionOptions);
    }

    private void highlightAllAtOnce(Matches matches, ActionOptions actionOptions) {
        matches.getMatchList().forEach(match ->
                highlightMatch.turnOn(match, match.getStateObjectData(), actionOptions));
        time.wait(actionOptions.getHighlightSeconds());
        matches.getMatchList().forEach(highlightMatch::turnOff);
    }

    private void highlightOneAtATime(Matches matches, ActionOptions actionOptions) {
        for (Match match : matches.getMatchList()) {
            highlightMatch.highlight(match, match.getStateObjectData(), actionOptions);
            if (matches.getMatchList().indexOf(match) < matches.getMatchList().size() - 1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }
}
