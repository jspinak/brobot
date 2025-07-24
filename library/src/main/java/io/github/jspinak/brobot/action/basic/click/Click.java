package io.github.jspinak.brobot.action.basic.click;

import io.github.jspinak.brobot.action.ActionInterface;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.Find;
import io.github.jspinak.brobot.action.basic.find.PatternFindOptions;
import io.github.jspinak.brobot.action.internal.factory.ActionResultFactory;
import io.github.jspinak.brobot.action.internal.mouse.PostClickHandler;
import io.github.jspinak.brobot.action.internal.mouse.SingleClickExecutor;
import io.github.jspinak.brobot.model.element.Location;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.testing.mock.time.TimeProvider;

import org.springframework.stereotype.Component;

/**
 * Performs mouse click operations on GUI elements in the Brobot framework.
 * 
 * <p>Click is one of the most fundamental actions in GUI automation, enabling interaction 
 * with buttons, links, menus, and other clickable elements. It combines the visual 
 * recognition capabilities of Find with precise mouse control to reliably interact with 
 * GUI elements across different applications and platforms.</p>
 * 
 * <p>Click targets supported:
 * <ul>
 *   <li><b>Image Matches</b>: Clicks on visually identified elements</li>
 *   <li><b>Regions</b>: Clicks within defined screen areas</li>
 *   <li><b>Locations</b>: Clicks at specific screen coordinates</li>
 *   <li><b>Previous Matches</b>: Reuses results from earlier Find operations</li>
 * </ul>
 * </p>
 * 
 * <p>Advanced features:
 * <ul>
 *   <li>Multi-click support for double-clicks, triple-clicks, etc.</li>
 *   <li>Configurable click types (left, right, middle button)</li>
 *   <li>Batch clicking on multiple matches</li>
 *   <li>Post-click mouse movement to avoid hover effects</li>
 *   <li>Precise timing control between clicks</li>
 *   <li>Integration with state management for context-aware clicking</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Click actions are more than simple mouse events. They 
 * update the framework's understanding of the GUI state, track interaction history, and 
 * can trigger state transitions. This integration ensures that the automation maintains 
 * an accurate model of the application state throughout execution.</p>
 * 
 * @see Find
 * @see ActionOptions
 * @see Location
 * @see PostClickHandler
 * @see SingleClickExecutor
 */
@Component
public class Click implements ActionInterface {

    private final Find find;
    private final SingleClickExecutor clickLocationOnce;
    private final TimeProvider time;
    private final PostClickHandler afterClick;
    private final ActionResultFactory actionResultFactory;

    public Click(Find find, SingleClickExecutor clickLocationOnce, TimeProvider time, 
                 PostClickHandler afterClick, ActionResultFactory actionResultFactory) {
        this.find = find;
        this.clickLocationOnce = clickLocationOnce;
        this.time = time;
        this.afterClick = afterClick;
        this.actionResultFactory = actionResultFactory;
    }

    @Override
    public Type getActionType() {
        return Type.CLICK;
    }

    /**
     * Executes click operations on all found matches up to the maximum allowed.
     * 
     * <p>This method orchestrates the complete click process:
     * <ol>
     *   <li>Uses Find to locate target elements in the first ObjectCollection</li>
     *   <li>Iterates through matches up to {@link ActionOptions#getMaxMatchesToActOn()}</li>
     *   <li>Performs configured number of clicks on each match</li>
     *   <li>Applies pauses between clicking different matches</li>
     * </ol>
     * </p>
     * 
     * <p>The method modifies the Match objects by incrementing their "times acted on" counter,
     * which tracks interactions during this action execution.</p>
     * 
     * @param matches The ActionResult containing ActionOptions and to which found matches
     *                are added. The matches collection is populated by the Find operation.
     * @param objectCollections The collections containing objects to find and click. Only
     *                          the first collection is used for finding targets.
     */
    @Override
    public void perform(ActionResult matches, ObjectCollection... objectCollections) {
        // Get the configuration - expecting ClickOptions
        if (!(matches.getActionConfig() instanceof ClickOptions)) {
            throw new IllegalArgumentException("Click requires ClickOptions configuration");
        }
        ClickOptions clickOptions = (ClickOptions) matches.getActionConfig();

        // Create a separate ActionResult for Find with PatternFindOptions
        // This is necessary because Find expects BaseFindOptions, not ClickOptions
        PatternFindOptions findOptions = new PatternFindOptions.Builder()
            .build();
        ActionResult findResult = actionResultFactory.init(findOptions, "Click->Find", objectCollections);
        
        // Perform find operation
        find.perform(findResult, objectCollections); // find performs only on 1st collection
        
        // Copy find results back to the original matches
        matches.getMatchList().addAll(findResult.getMatchList());
        matches.setSuccess(findResult.isSuccess());
        // Find should have already limited matches based on maxMatchesToActOn
        int matchIndex = 0;
        for (Match match : matches.getMatchList()) {
            Location location = match.getTarget();
            click(location, clickOptions, match);
            matchIndex++;
            // pause only between clicking different matches, not after the last match
            if (matchIndex < matches.getMatchList().size()) {
                time.wait(clickOptions.getPauseBetweenIndividualActions());
            }
        }
    }

    /**
     * Performs multiple clicks on a single location with configurable timing and post-click behavior.
     * 
     * <p>This method handles the low-level click execution for a single match, including:
     * <ul>
     *   <li>Repeating clicks based on {@link ActionOptions#getTimesToRepeatIndividualAction()}</li>
     *   <li>Tracking click count on the match object</li>
     *   <li>Managing timing between repeated clicks</li>
     *   <li>Delegating post-click mouse movement to AfterClick when configured</li>
     * </ul>
     * </p>
     * 
     * <p>Example: With timesToRepeatIndividualAction=2, this method will:
     * <ol>
     *   <li>Click the location</li>
     *   <li>Increment match's acted-on counter</li>
     *   <li>Move mouse if configured</li>
     *   <li>Pause</li>
     *   <li>Click again</li>
     *   <li>Increment counter again</li>
     *   <li>Move mouse if configured (no pause after last click)</li>
     * </ol>
     * </p>
     * 
     * @param location The screen coordinates where clicks will be performed. This is the
     *                 final, adjusted location from the match's target.
     * @param actionOptions Configuration containing:
     *                      - timesToRepeatIndividualAction: number of clicks per location
     *                      - pauseBetweenIndividualActions: delay between clicks (ms)
     *                      - moveMouseAfterAction: whether to move mouse after clicking
     * @param match The Match object being acted upon. This object is modified by
     *              incrementing its timesActedOn counter for each click.
     */
    private void click(Location location, ClickOptions clickOptions, Match match) {
        for (int i = 0; i < clickOptions.getTimesToRepeatIndividualAction(); i++) {
            // SingleClickExecutor now accepts ActionConfig
            clickLocationOnce.click(location, clickOptions);
            match.incrementTimesActedOn();
            // TODO: Handle mouse movement after action when PostClickOptions is implemented
            if (i < clickOptions.getTimesToRepeatIndividualAction() - 1) {
                time.wait(clickOptions.getPauseBetweenIndividualActions());
            }
        }
    }
}
