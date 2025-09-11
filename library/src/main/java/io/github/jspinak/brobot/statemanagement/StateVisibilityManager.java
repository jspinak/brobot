package io.github.jspinak.brobot.statemanagement;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.navigation.service.StateService;

/**
 * Manages the conversion of active states to hidden states during transitions.
 *
 * <p>StateVisibilityManager implements a crucial mechanism in Brobot's state management system that
 * handles overlapping or layered GUI elements. When a new state becomes active, it may partially or
 * completely obscure previously active states. This class identifies which active states should be
 * "hidden" by the new state and updates their status accordingly.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li><b>Hidden States</b>: States that are obscured but still conceptually present
 *   <li><b>Can Hide Relationship</b>: Each state defines which other states it can hide
 *   <li><b>State Layering</b>: Supports GUI elements that overlay each other
 *   <li><b>Back Navigation</b>: Hidden states become targets for "back" operations
 * </ul>
 *
 * <p>The hiding process:
 *
 * <ol>
 *   <li>New state becomes active after successful transition
 *   <li>Check new state's canHide list against currently active states
 *   <li>Move matching states from active to hidden status
 *   <li>Update the new state's hidden state list
 *   <li>Remove hidden states from StateMemory's active list
 * </ol>
 *
 * <p>Common scenarios:
 *
 * <ul>
 *   <li><b>Modal Dialogs</b>: Dialog hides underlying page but page remains in memory
 *   <li><b>Dropdown Menus</b>: Menu hides portion of page while open
 *   <li><b>Navigation Drawers</b>: Drawer slides over main content
 *   <li><b>Tab Switching</b>: New tab hides previous tab content
 *   <li><b>Popups/Tooltips</b>: Temporary overlays that hide underlying elements
 * </ul>
 *
 * <p>Benefits of hidden state tracking:
 *
 * <ul>
 *   <li>Enables accurate "back" navigation to previous states
 *   <li>Maintains context about GUI layering and hierarchy
 *   <li>Supports complex navigation patterns with overlapping elements
 *   <li>Prevents false positive state detections from hidden elements
 *   <li>Provides foundation for state stack management
 * </ul>
 *
 * <p>Example flow:
 *
 * <pre>
 * // Initial: MainPage is active
 * // User opens settings dialog
 * // SettingsDialog.canHide = [MainPage]
 * setHiddenStates.set(SettingsDialog)
 * // Result: SettingsDialog active, MainPage hidden
 * // User clicks "back"
 * // System knows to return to MainPage
 * </pre>
 *
 * <p>In the model-based approach, StateVisibilityManager enables sophisticated state management
 * that mirrors the actual GUI behavior. By tracking which states are hidden rather than inactive,
 * the framework maintains a more accurate model of the GUI's current configuration and can make
 * better navigation decisions.
 *
 * <p>This hidden state mechanism is essential for:
 *
 * <ul>
 *   <li>Applications with complex layered interfaces
 *   <li>Supporting natural back navigation patterns
 *   <li>Maintaining state context during overlay interactions
 *   <li>Accurate state detection in multi-layered GUIs
 * </ul>
 *
 * @since 1.0
 * @see State#getCanHideIds()
 * @see State#addHiddenState(Long)
 * @see StateMemory
 * @see StateService
 */
@Component
public class StateVisibilityManager {

    private final StateService allStatesInProjectService;
    private final StateMemory stateMemory;

    public StateVisibilityManager(StateService allStatesInProjectService, StateMemory stateMemory) {
        this.allStatesInProjectService = allStatesInProjectService;
        this.stateMemory = stateMemory;
    }

    /**
     * Processes state hiding relationships after a state becomes active.
     *
     * <p>Examines all currently active states and determines which ones should be hidden by the
     * newly activated state based on its canHide configuration. States that can be hidden are moved
     * from the active state list to the new state's hidden state list, maintaining the layering
     * relationship.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Modifies the stateToSet's hidden state list
     *   <li>Removes hidden states from StateMemory's active list
     *   <li>Preserves hidden states for potential back navigation
     * </ul>
     *
     * <p>Implementation note: Uses ArrayList copy to avoid concurrent modification while iterating
     * through active states that may be removed.
     *
     * @param stateToSet ID of the newly activated state that may hide others
     * @return true if stateToSet is valid, false if state not found
     * @see State#getCanHideIds()
     * @see State#addHiddenState(Long)
     * @see StateMemory#removeInactiveState(Long)
     */
    public boolean set(Long stateToSet) {
        Optional<State> optStateToSet = allStatesInProjectService.getState(stateToSet);
        if (optStateToSet.isEmpty()) return false;
        State state = optStateToSet.get();
        for (Long activeState : new ArrayList<>(stateMemory.getActiveStates())) {
            if (state.getCanHideIds().contains(activeState)) {
                state.addHiddenState(activeState);
                stateMemory.removeInactiveState(activeState);
            }
        }
        return true;
    }
}
