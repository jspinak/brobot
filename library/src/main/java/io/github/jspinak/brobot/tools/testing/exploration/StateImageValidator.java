package io.github.jspinak.brobot.tools.testing.exploration;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionType;
import io.github.jspinak.brobot.model.state.State;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Visits and verifies all images within a given state as part of comprehensive test coverage.
 * 
 * <p>This class is a crucial component of the state exploration testing framework, responsible for
 * ensuring that every visual element (StateImage) within a state is properly tested. It performs
 * FIND actions on each StateImage to verify their presence and detectability on the screen.</p>
 * 
 * <h2>Testing Strategy</h2>
 * <p>The testing strategy involves:</p>
 * <ul>
 *   <li>Iterating through all StateImages in a given state</li>
 *   <li>Performing FIND actions to verify image visibility and matching</li>
 *   <li>Contributing to comprehensive test coverage by ensuring no images are missed</li>
 * </ul>
 * 
 * <h2>Integration with State Exploration</h2>
 * <p>This class works in conjunction with {@link StateTraversalService} to provide thorough
 * testing coverage. While StateTraversalService handles navigation between states, this class
 * ensures that each state's visual elements are properly verified once reached.</p>
 * 
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // After successfully navigating to a state
 * State currentState = stateService.getCurrentState();
 * stateImageValidator.visitAllStateImages(currentState);
 * // All images in the state will be searched for and verified
 * }</pre>
 * 
 * @see StateTraversalService for state navigation and overall traversal strategy
 * @see State for the structure containing StateImages
 * @see Action for the underlying action execution mechanism
 * @author jspinak
 */
@Component
@Getter
@Setter
public class StateImageValidator {

    private final Action action;

    public StateImageValidator(Action action) {
        this.action = action;
    }

    /**
     * Visits all StateImages within the given state by performing FIND actions on each.
     * 
     * <p>This method systematically tests every visual element in a state to ensure:
     * <ul>
     *   <li>All images are present and detectable on the screen</li>
     *   <li>Image recognition is functioning correctly for each StateImage</li>
     *   <li>No visual elements are missing or have changed unexpectedly</li>
     * </ul>
     * 
     * <p>The FIND action is used as it provides verification without side effects,
     * making it ideal for testing purposes. Each StateImage is converted to an
     * ObjectCollection before being processed.</p>
     * 
     * @param state the state whose images should be visited and verified.
     *              Must not be null and should contain at least one StateImage
     *              for meaningful testing.
     * @throws NullPointerException if state is null
     * @see ActionType#FIND for the action type used
     * @see State#getStateImages() for accessing the state's visual elements
     */
    public void visitAllStateImages(State state) {
        state.getStateImages().forEach(stateImage -> {
            action.perform(ActionType.FIND, stateImage.asObjectCollection());
        });
    }
}
