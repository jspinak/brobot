package io.github.jspinak.brobot.action.composite.verify;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionShortcuts;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.state.State;
import io.github.jspinak.brobot.model.state.special.UnknownState;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.tools.tuning.model.TuningConstraints;
import io.github.jspinak.brobot.tools.tuning.model.TuningExperiment;

import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Provides convenience methods for type operations with verification.
 * <p>
 * TypeVerify simplifies the creation and execution of type-based {@link ActionResultCombo}
 * operations. It handles patterns where typing text should cause a state change in the
 * application (e.g., typing in a search field causes results to appear).
 * <p>
 * This class focuses on keyboard input operations as the primary action, with state-based
 * verification to confirm that the typing had the expected effect on the application.
 * <p>
 * Common usage patterns:
 * <ul>
 * <li>Type and Find State - Type text and verify a state becomes active</li>
 * <li>Type and Vanish State - Type text and verify a state becomes inactive</li>
 * </ul>
 *
 * @see ActionResultCombo
 * @see CommonResults
 * @see ClickVerify
 */
@Component
public class TypeVerify {

    private final ActionShortcuts commonActionOptions;
    private final CommonResults commonResults;
    private final StateService allStatesInProjectService;

    public TypeVerify(ActionShortcuts commonActionOptions,
                       CommonResults commonResults, StateService allStatesInProjectService) {
        this.commonActionOptions = commonActionOptions;
        this.commonResults = commonResults;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    /**
     * Types text and verifies that a specific state becomes active.
     * <p>
     * This convenience method types the provided string without any modifier keys
     * and checks if the specified state appears as a result.
     *
     * @param stateName The name of the state that should become active after typing
     * @param string The text to type
     * @return true if the typing succeeded and the target state was found
     */
    public boolean typeAndFindState(String stateName, String string) {
        return typeAndFindState(stateName, string, "");
    }

    /**
     * Types text with modifier keys and verifies that a specific state becomes active.
     * <p>
     * This method allows typing with modifier keys (e.g., "ctrl", "shift") and
     * verifies that the expected state appears after the keyboard input.
     *
     * @param stateName The name of the state that should become active after typing
     * @param string The text to type
     * @param modifier The modifier key(s) to hold while typing (e.g., "ctrl", "shift")
     * @return true if the typing succeeded and the target state was found
     */
    public boolean typeAndFindState(String stateName, String string, String modifier) {
        return typeAndStateAction(ActionOptions.Action.FIND, stateName, string, modifier);
    }

    /**
     * Types text with modifier keys and verifies that a specific state becomes inactive.
     * <p>
     * This method is useful when typing should cause something to disappear
     * (e.g., typing escape to close a dialog or menu).
     *
     * @param stateName The name of the state that should become inactive after typing
     * @param string The text to type
     * @param modifier The modifier key(s) to hold while typing (e.g., "ctrl", "shift")
     * @return true if the typing succeeded and the target state vanished
     */
    public boolean typeAndVanishState(String stateName, String string, String modifier) {
        return typeAndStateAction(ActionOptions.Action.VANISH, stateName, string, modifier);
    }

    /**
     * Types text and performs a specified verification action on a state.
     * <p>
     * This is the general-purpose method that supports any verification action type
     * for state-based typing verification. It creates an {@link ActionResultCombo}
     * with the typing configuration and verifies the state change using the specified
     * action. The method will not attempt verification if the state is UNKNOWN or
     * cannot be found.
     * <p>
     * The method sets up:
     * <ul>
     * <li>A TYPE action with the specified modifier keys</li>
     * <li>An ObjectCollection containing the string to type</li>
     * <li>Parameters with the maximum wait threshold</li>
     * <li>State verification using the CommonResults helper</li>
     * </ul>
     *
     * @param resultAction The type of verification to perform (e.g., FIND, VANISH)
     * @param stateName The name of the state to verify
     * @param string The text to type
     * @param modifier The modifier key(s) to hold while typing (e.g., "ctrl", "shift")
     * @return true if the typing succeeded and the state verification passed,
     *         false if the state is unknown, not found, or verification failed
     */
    public boolean typeAndStateAction(ActionOptions.Action resultAction, String stateName,
                                      String string, String modifier) {
        if (Objects.equals(stateName, UnknownState.Enum.UNKNOWN.toString())) return false; // this could be true but requires additional coding
        Optional<State> state = allStatesInProjectService.getState(stateName);
        if (state.isEmpty()) return false;
        ActionResultCombo arCombo = new ActionResultCombo();
        // add the type action
        ActionOptions actionOptions1 = commonActionOptions.type(modifier);
        TuningExperiment params = new TuningExperiment(actionOptions1);
        params.setMaxWait(TuningConstraints.maxWait);
        arCombo.setActionOptions(actionOptions1);
        ObjectCollection strColl = new ObjectCollection.Builder()
                .withStrings(string)
                .build();
        arCombo.addActionCollection(strColl);
        return commonResults.stateAction(state.get(), arCombo, params, resultAction);
    }

}
