package io.github.jspinak.brobot.action.composite.verify;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ObjectCollection;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Combines an action with its expected result for environmental success verification using ActionConfig.
 * <p>
 * ActionResultComboV2 is the modern replacement for ActionResultCombo, using the new
 * ActionConfig hierarchy instead of ActionOptions. It pairs an action (like CLICK) with 
 * a verification action (like FIND) to measure environmental success rather than just 
 * operational success. This enables validation that actions have their intended effect 
 * in the application.
 * 
 * @deprecated Use {@link io.github.jspinak.brobot.action.ActionChainOptions} instead.
 *             ActionChainOptions provides the same functionality with better integration
 *             and cleaner API. For click-verify patterns, simply chain a click action
 *             with a find action.
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
 * <strong>Example usage:</strong>
 * <pre>{@code
 * ActionResultComboV2 combo = new ActionResultComboV2();
 * combo.setActionConfig(new ClickOptions.Builder().build());
 * combo.addActionCollection(buttonCollection);
 * combo.setResultConfig(new PatternFindOptions.Builder()
 *     .setStrategy(PatternFindOptions.Strategy.FIRST)
 *     .build());
 * combo.addResultCollection(menuCollection);
 * }</pre>
 * <p>
 * <strong>Applications:</strong>
 * <ul>
 * <li>Parameter tuning - Optimize delays and wait times based on actual results</li>
 * <li>Learning algorithms - Provide feedback for reinforcement learning</li>
 * <li>Reliability testing - Measure action effectiveness across different conditions</li>
 * </ul>
 *
 * @see ActionResultCombo
 * @see RunARComboV2
 * @see ActionConfig
 */
@Deprecated
@Getter
@Setter
public class ActionResultComboV2 {

    private ActionConfig actionConfig;
    private List<ObjectCollection> actionCollection = new ArrayList<>();
    private ActionConfig resultConfig;
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
}