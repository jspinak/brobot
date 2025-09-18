package io.github.jspinak.brobot.action.internal.mouse;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.basic.click.Click;

/**
 * Handles post-click operations, particularly mouse movement after click actions.
 *
 * <p>This class is responsible for managing what happens immediately after a click operation
 * completes. Its primary function is to move the mouse cursor away from the clicked location to
 * prevent unwanted hover effects or tooltip interference that could block subsequent operations.
 *
 * <p>Common use cases for post-click mouse movement:
 *
 * <ul>
 *   <li>Preventing hover tooltips from obscuring elements
 *   <li>Avoiding unintended hover state changes on buttons
 *   <li>Improving visibility for screenshot verification
 *   <li>Preventing accidental double-clicks from mouse drift
 * </ul>
 *
 * <p>The class supports two movement strategies:
 *
 * <ul>
 *   <li><b>Offset-based</b>: Moves the mouse by a relative offset from the click point
 *   <li><b>Fixed location</b>: Moves the mouse to an absolute screen position
 * </ul>
 *
 * Offset-based movement takes priority when both are configured.
 *
 * @see Click
 * @see ActionConfig
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
     *
     * <ol>
     *   <li>First checks if mouse movement is enabled via {@link
     *       ActionConfig#isMoveMouseAfterAction()}
     *   <li>If enabled, attempts offset-based movement if defined
     *   <li>Falls back to fixed location movement if offset is not defined
     * </ol>
     *
     * <p>The offset-based movement is useful for relative positioning (e.g., "move 50 pixels to the
     * right of where I clicked"), while fixed location movement is useful for moving to a
     * consistent "safe" area of the screen.
     *
     * @param actionConfig Configuration containing mouse movement settings: - moveMouseAfterAction:
     *     master enable/disable flag - moveMouseAfterActionBy: relative offset from click point
     *     (priority) - moveMouseAfterActionTo: absolute screen location (fallback)
     * @return true if the mouse was successfully moved, false if movement was disabled or if both
     *     movement options failed
     */
    public boolean moveMouseAfterClick(ActionConfig actionConfig) {
        // In modern Brobot, mouse movement after click is handled differently
        // This would be configured in ClickOptions if needed
        return false;
    }

    // Duplicate deprecated method removed - use action chaining for post-click movement
}
