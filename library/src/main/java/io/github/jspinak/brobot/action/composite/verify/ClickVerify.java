package io.github.jspinak.brobot.action.composite.verify;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ActionShortcuts;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.model.state.special.UnknownState;
import io.github.jspinak.brobot.navigation.service.StateService;
import io.github.jspinak.brobot.tools.tuning.model.TuningConstraints;
import io.github.jspinak.brobot.tools.tuning.model.TuningExperiment;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.state.State;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides convenience methods for click operations with verification.
 * <p>
 * ClickVerify simplifies the creation and execution of click-based {@link ActionResultCombo}
 * operations. It handles common patterns like clicking an element and verifying that the
 * expected result occurs (e.g., a menu opens, a dialog appears, or an element disappears).
 * <p>
 * This class is specifically designed for click operations as the primary action, with
 * the verification step used to confirm environmental success. The parameters being
 * optimized through these combos are primarily click-related timing values.
 * <p>
 * Common usage patterns:
 * <ul>
 * <li>Click and Find - Click an element and verify something appears</li>
 * <li>Click and Vanish - Click an element and verify something disappears</li>
 * <li>Click and State Change - Click an element and verify a state transition</li>
 * </ul>
 *
 * @see ActionResultCombo
 * @see RunARCombo
 * @see CommonResults
 */
@Component
public class ClickVerify {

    private final RunARCombo runARCombo;
    private final ActionShortcuts commonActionOptions;
    private final CommonResults commonResults;
    private final StateService allStatesInProjectService;

    public ClickVerify(RunARCombo runARCombo, ActionShortcuts commonActionOptions,
                       CommonResults commonResults, StateService allStatesInProjectService) {
        this.runARCombo = runARCombo;
        this.commonActionOptions = commonActionOptions;
        this.commonResults = commonResults;
        this.allStatesInProjectService = allStatesInProjectService;
    }

    /**
     * Clicks on an image and verifies that another image appears.
     * <p>
     * This is a convenience method for the common pattern of clicking a UI element
     * and verifying that it causes something to appear (e.g., clicking a button
     * to open a menu).
     *
     * @param toClick The image to click on
     * @param toFind The image that should appear after clicking
     * @return true if both the click succeeded and the expected image was found
     */
    public boolean clickAndFind(StateImage toClick, StateImage toFind) {
        return clickAndAction(toClick, toFind, ActionOptions.Action.FIND);
    }

    /**
     * Clicks on an image and verifies that another image disappears.
     * <p>
     * This is useful for operations where clicking should cause something to
     * disappear (e.g., clicking a close button to dismiss a dialog).
     *
     * @param toClick The image to click on
     * @param toVanish The image that should disappear after clicking
     * @return true if both the click succeeded and the expected image vanished
     */
    public boolean clickAndVanish(StateImage toClick, StateImage toVanish) {
        return clickAndAction(toClick, toVanish, ActionOptions.Action.VANISH);
    }

    /**
     * Clicks on an image and performs a specified verification action.
     * <p>
     * This is the general-purpose method that supports any verification action type.
     * It creates an {@link ActionResultCombo} with standard click options and the
     * specified verification action, then executes it with optimized parameters.
     *
     * @param toClick The image to click on
     * @param resultImage The image to verify with the specified action
     * @param actionType The type of verification action (e.g., FIND, VANISH)
     * @return true if both the click and verification action succeeded
     */
    public boolean clickAndAction(StateImage toClick, StateImage resultImage, ActionOptions.Action actionType) {
        ActionResultCombo arCombo = new ActionResultCombo();
        ActionOptions actionOptions1 = commonActionOptions.standard(ActionOptions.Action.CLICK, 1);
        arCombo.setActionOptions(actionOptions1);
        TuningExperiment params = new TuningExperiment(actionOptions1);
        params.setMaxWait(TuningConstraints.maxWait);
        arCombo.addActionCollection(toClick.asObjectCollection());
        arCombo.setResultOptions(commonActionOptions.standard(actionType, 1));
        arCombo.addResultCollection(resultImage.asObjectCollection());
        List<ActionResult> matches = runARCombo.perform(arCombo, params);
        return matches.size() > 1 && matches.get(1).isSuccess();
    }

    /**
     * Clicks on an image and verifies that a specific state becomes active.
     * <p>
     * This method is used when clicking should cause a state transition. It will
     * verify that any of the images associated with the target state appear.
     *
     * @param toClick The image to click on
     * @param stateName The name of the state that should become active
     * @param numberOfClicks The number of times to click
     * @return true if the click succeeded and the target state was found
     */
    public boolean clickAndFindState(StateImage toClick, String stateName, int numberOfClicks) {
        return clickAndStateAction(toClick, stateName, 1, ActionOptions.Action.FIND, numberOfClicks);
    }

    /**
     * Clicks on an image and verifies that a specific state becomes inactive.
     * <p>
     * This method verifies that all images associated with a state disappear after
     * clicking, indicating the state is no longer active.
     *
     * @param toClick The image to click on
     * @param stateName The name of the state that should become inactive
     * @param maxWait Maximum time to wait for the state to vanish
     * @param numberOfClicks The number of times to click
     * @return true if the click succeeded and the target state vanished
     */
    public boolean clickAndVanishState(StateImage toClick, String stateName, double maxWait, int numberOfClicks) {
        return clickAndStateAction(toClick, stateName, maxWait, ActionOptions.Action.VANISH, numberOfClicks);
    }

    /**
     * Clicks on an image and performs a verification action on a state.
     * <p>
     * This is the general-purpose method for state-based verifications. It retrieves
     * the specified state from the state service and verifies it using the specified
     * action type. The method will not attempt verification if the state is UNKNOWN
     * or cannot be found.
     *
     * @param toClick The image to click on
     * @param stateName The name of the state to verify
     * @param maxWait Maximum time to wait for the verification
     * @param actionType The type of verification action (e.g., FIND, VANISH)
     * @param numberOfClicks The number of times to click
     * @return true if the click succeeded and the state verification passed,
     *         false if the state is unknown, not found, or verification failed
     */
    public boolean clickAndStateAction(StateImage toClick, String stateName,
                                       double maxWait, ActionOptions.Action actionType,
                                       int numberOfClicks) {
        if (Objects.equals(stateName, UnknownState.Enum.UNKNOWN.toString())) return false; // this could be true but requires additional coding
        Optional<State> state = allStatesInProjectService.getState(stateName);
        if (state.isEmpty()) return false;
        ActionResultCombo arCombo = new ActionResultCombo();
        ActionOptions actionOptions1 = commonActionOptions.findAndMultipleClicks(maxWait, numberOfClicks);
        TuningExperiment params = new TuningExperiment(actionOptions1);
        params.setMaxWait(TuningConstraints.maxWait);
        arCombo.setActionOptions(actionOptions1);
        arCombo.addActionCollection(toClick.asObjectCollection());
        return commonResults.stateAction(state.get(), arCombo, params, actionType);
    }

}
