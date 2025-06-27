package io.github.jspinak.brobot.action.composite.verify;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.tools.tuning.model.TuningExperiment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines an action with its expected result for environmental success verification.
 * <p>
 * ActionResultCombo pairs an action (like CLICK) with a verification action (like FIND)
 * to measure environmental success rather than just operational success. This enables
 * validation that actions have their intended effect in the application.
 * <p>
 * <strong>Key Concepts:</strong>
 * <ul>
 * <li><strong>Operational Success:</strong> The action completes successfully
 *     (e.g., Find locates a match, Click executes on a clickable element)</li>
 * <li><strong>Environmental Success:</strong> The action achieves its intended effect
 *     in the application (e.g., clicking a button actually opens the expected menu)</li>
 * </ul>
 * <p>
 * <strong>Common Use Case - Click-Find:</strong><br>
 * Click a button that should open a menu. The combo succeeds only when both:
 * <ol>
 * <li>The button is successfully clicked (operational success)</li>
 * <li>The expected menu appears (environmental success)</li>
 * </ol>
 * <p>
 * <strong>Applications:</strong>
 * <ul>
 * <li>Parameter tuning - Optimize delays and wait times based on actual results</li>
 * <li>Learning algorithms - Provide feedback for reinforcement learning</li>
 * <li>Reliability testing - Measure action effectiveness across different conditions</li>
 * </ul>
 * <p>
 * Results are collected in {@link TuningExperimentStore} for analysis and optimization
 * of hyperparameters like click delays and wait times.
 *
 * @see RunARCombo
 * @see TuningExperiment
 * @see TuningExperimentStore
 */
@Getter
@Setter
public class ActionResultCombo {

    private ActionOptions actionOptions;
    private List<ObjectCollection> actionCollection = new ArrayList<>();
    private ActionOptions resultOptions;
    private List<ObjectCollection> resultCollection = new ArrayList<>();

    /**
     * Adds an ObjectCollection to the action collections.
     * <p>
     * The action collections define what objects the primary action will operate on
     * (e.g., images to click, regions to search).
     *
     * @param objectCollection The collection of objects for the action to process
     */
    public void addActionCollection(ObjectCollection objectCollection) {
        actionCollection.add(objectCollection);
    }

    /**
     * Adds an ObjectCollection to the result collections.
     * <p>
     * The result collections define what objects to look for to verify environmental
     * success (e.g., images that should appear after clicking).
     *
     * @param objectCollection The collection of objects to verify in the result
     */
    public void addResultCollection(ObjectCollection objectCollection) {
        resultCollection.add(objectCollection);
    }

    /**
     * Applies parameter values from a ParameterCollection to the action options.
     * <p>
     * This method updates the timing parameters for both the primary action and
     * the result verification action based on the provided parameter collection.
     * The parameters modified include:
     * <ul>
     * <li>Mouse timing delays for the action (pauseBeforeMouseDown, pauseAfterMouseDown, pauseAfterMouseUp)</li>
     * <li>Mouse movement delay for the action</li>
     * <li>Maximum wait time for the result verification</li>
     * </ul>
     *
     * @param params The ParameterCollection containing timing values to apply
     */
    public void setParameters(TuningExperiment params) {
        actionOptions.setPauseBeforeMouseDown(params.getPauseBeforeMouseDown());
        actionOptions.setPauseAfterMouseDown(params.getPauseAfterMouseDown());
        actionOptions.setPauseAfterMouseUp(params.getPauseAfterMouseUp());
        actionOptions.setMoveMouseDelay(params.getMoveMouseDelay());
        resultOptions.setMaxWait(params.getMaxWait());
    }
}
