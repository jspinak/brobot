package io.github.jspinak.brobot.action.composite.chains;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.internal.mouse.ClickType;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActions;
import io.github.jspinak.brobot.action.composite.multiple.actions.MultipleActionsObject;
import io.github.jspinak.brobot.model.element.Region;

import org.springframework.stereotype.Component;

import static io.github.jspinak.brobot.action.ActionOptions.Action.*;

/**
 * Provides advanced composite action patterns that combine multiple sequential actions.
 * <p>
 * This class demonstrates how to build complex automation workflows by orchestrating
 * multiple basic actions into cohesive sequences. It serves as both a utility class
 * and an example of how to create custom composite actions that encapsulate
 * application-specific interaction patterns.
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Sequential execution of multiple action types</li>
 *   <li>Coordinated mouse movements and clicks</li>
 *   <li>Advanced wait conditions with visual feedback</li>
 *   <li>Fine-grained timing control between action steps</li>
 * </ul>
 * 
 * <p>This class exemplifies the power of the composite action pattern in Brobot,
 * showing how complex GUI interactions can be abstracted into reusable methods.
 * Teams are encouraged to create similar classes tailored to their specific
 * application workflows.</p>
 * 
 * @see MultipleActions
 * @see ActionFacade
 * @see MultipleActionsObject
 * @see ActionOptions
 */
@Component
public class ActionSequenceBuilder {

    private MultipleActions multipleActions;
    private Action action;
    private ActionFacade commonActions;

    public ActionSequenceBuilder(MultipleActions multipleActions, Action action, ActionFacade commonActions) {
        this.multipleActions = multipleActions;
        this.action = action;
        this.commonActions = commonActions;
    }

    /**
     * Repeatedly right-clicks an image with controlled mouse movements until it vanishes.
     * <p>
     * This method demonstrates a sophisticated composite action pattern that combines:
     * <ol>
     *   <li>Mouse movement to the target position with a pause</li>
     *   <li>Right-click action with configurable timing</li>
     *   <li>Mouse movement away from the clicked position</li>
     *   <li>Visual feedback through region highlighting</li>
     *   <li>Vanish detection with reduced similarity threshold</li>
     * </ol>
     * 
     * <p>This pattern is particularly useful for context menu operations where the mouse
     * needs to be moved away after clicking to avoid interfering with menu visibility
     * or to trigger hover-based UI changes. The reduced similarity threshold for vanish
     * detection (original similarity - 0.10) provides more lenient matching to account
     * for visual changes during the interaction.</p>
     * 
     * <p><b>Execution flow:</b></p>
     * <ul>
     *   <li>For each iteration (up to timesToClick):
     *     <ol>
     *       <li>Move mouse to image location and pause</li>
     *       <li>Perform right-click</li>
     *       <li>If click successful, move mouse by (xMove, yMove) and highlight the region</li>
     *       <li>Check if image has vanished within pauseBetweenClicks seconds</li>
     *       <li>Return true immediately if vanished, otherwise continue</li>
     *     </ol>
     *   </li>
     * </ul>
     * 
     * @param timesToClick Maximum number of right-click attempts before giving up
     * @param pauseBetweenClicks Time in seconds to wait for vanish detection after each click
     * @param pauseBeforeClick Delay in seconds after moving to target before clicking
     * @param pauseAfterMove Delay in seconds after moving away from the clicked position
     * @param image The state image to right-click repeatedly
     * @param xMove Horizontal offset in pixels to move the mouse after clicking
     * @param yMove Vertical offset in pixels to move the mouse after clicking
     * @return true if the image vanished before reaching the click limit, false if the
     *         maximum attempts were exhausted without the image vanishing
     */
    public boolean rightClickAndMoveUntilVanishes(int timesToClick, double pauseBetweenClicks,
                                                  double pauseBeforeClick, double pauseAfterMove,
                                                  StateImage image, int xMove, int yMove) {
        ActionOptions moveBeforeClick = new ActionOptions.Builder()
                .setAction(MOVE)
                .setPauseAfterEnd(pauseBeforeClick)
                .build();
        ActionOptions doAction = new ActionOptions.Builder()
                .setAction(CLICK)
                .setClickType(ClickType.Type.RIGHT)
                .setPauseBeforeMouseDown(pauseBeforeClick)
                .build();
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withImages(image)
                .build();
        ActionOptions moveAfterClick = new ActionOptions.Builder()
                .setAction(MOVE)
                .setAddX(xMove)
                .setAddY(yMove)
                .setPauseAfterEnd(pauseAfterMove)
                .build();
        MultipleActionsObject mao = new MultipleActionsObject();
        mao.addActionOptionsObjectCollectionPair(moveBeforeClick, objectCollection);
        mao.addActionOptionsObjectCollectionPair(doAction, objectCollection);
        ActionOptions vanish = new ActionOptions.Builder()
                .setAction(VANISH)
                .setMaxWait(pauseBetweenClicks)
                .setMinSimilarity(doAction.getSimilarity() - .10)
                .build();
        ActionResult matches;
        for (int i=0; i<timesToClick; i++) {
            matches = multipleActions.perform(mao);
            if (matches.isSuccess()) {
                action.perform(moveAfterClick);
                matches.getBestMatch().ifPresent(match ->
                        commonActions.highlightRegion(1, new Region(match)));
            }
            if (action.perform(vanish, image).isSuccess()) {
                System.out.println("object vanished, min sim = "+vanish.getSimilarity());
                return true;
            }
        }
        return false;
    }
}
