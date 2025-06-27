package io.github.jspinak.brobot.action.internal.mouse;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.click.Click;

import org.springframework.stereotype.Component;

/**
 * Handles post-click operations, particularly mouse movement after click actions.
 * 
 * <p>This class is responsible for managing what happens immediately after a click
 * operation completes. Its primary function is to move the mouse cursor away from
 * the clicked location to prevent unwanted hover effects or tooltip interference
 * that could block subsequent operations.</p>
 * 
 * <p>Common use cases for post-click mouse movement:
 * <ul>
 *   <li>Preventing hover tooltips from obscuring elements</li>
 *   <li>Avoiding unintended hover state changes on buttons</li>
 *   <li>Improving visibility for screenshot verification</li>
 *   <li>Preventing accidental double-clicks from mouse drift</li>
 * </ul>
 * </p>
 * 
 * <p>The class supports two movement strategies:
 * <ul>
 *   <li><b>Offset-based</b>: Moves the mouse by a relative offset from the click point</li>
 *   <li><b>Fixed location</b>: Moves the mouse to an absolute screen position</li>
 * </ul>
 * Offset-based movement takes priority when both are configured.</p>
 * 
 * @see Click
 * @see ActionOptions
 * @see MoveMouseWrapper
 */
@Component
public class PostClickHandler {
    private final MoveMouseWrapper moveMouseWrapper;

    public PostClickHandler(MoveMouseWrapper moveMouseWrapper) {
        this.moveMouseWrapper = moveMouseWrapper;
    }

    /**
     * Moves the mouse cursor after a click operation based on configured options.
     * 
     * <p>This method implements a priority system for post-click mouse movement:
     * <ol>
     *   <li>First checks if mouse movement is enabled via {@link ActionOptions#isMoveMouseAfterAction()}</li>
     *   <li>If enabled, attempts offset-based movement if defined</li>
     *   <li>Falls back to fixed location movement if offset is not defined</li>
     * </ol>
     * </p>
     * 
     * <p>The offset-based movement is useful for relative positioning (e.g., "move 50 pixels
     * to the right of where I clicked"), while fixed location movement is useful for
     * moving to a consistent "safe" area of the screen.</p>
     * 
     * @param actionOptions Configuration containing mouse movement settings:
     *                      - moveMouseAfterAction: master enable/disable flag
     *                      - moveMouseAfterActionBy: relative offset from click point (priority)
     *                      - moveMouseAfterActionTo: absolute screen location (fallback)
     * @return true if the mouse was successfully moved, false if movement was disabled
     *         or if both movement options failed
     */
    public boolean moveMouseAfterClick(ActionOptions actionOptions) {
        if (!actionOptions.isMoveMouseAfterAction()) return false;
        if (actionOptions.getMoveMouseAfterActionBy().defined())
            return moveMouseWrapper.move(actionOptions.getMoveMouseAfterActionBy());
        return moveMouseWrapper.move(actionOptions.getMoveMouseAfterActionTo());
    }
}
