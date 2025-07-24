package io.github.jspinak.brobot.action.composite.select;

import io.github.jspinak.brobot.action.Action;
import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.internal.options.ActionOptions;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.springframework.stereotype.Component;

/**
 * A custom composite Action that performs selection operations through swiping.
 * <p>
 * Select demonstrates how to build custom Actions using {@link SelectActionObject}.
 * It implements a common UI automation pattern: swiping through a scrollable region
 * until finding target images, clicking on them, and optionally verifying success
 * through confirmation images.
 * <p>
 * The operation flow is:
 * <ol>
 * <li>Search for target images in the current view</li>
 * <li>If found, click on them</li>
 * <li>If confirmation images are specified, verify they appear</li>
 * <li>If not found, swipe and repeat up to maxSwipes times</li>
 * </ol>
 * <p>
 * This class serves as an example of how to create custom composite Actions.
 * ActionObjects like {@link SelectActionObject} contain both {@link ActionOptions}
 * and {@link ObjectCollection} instances, providing all configuration needed for
 * the operation. The Action class (Select) implements the logic for how and when
 * these configurations are used.
 *
 * @see SelectActionObject
 * @see CommonSelect
 */
@Component
public class Select {

    private Action action;

    public Select(Action action) {
        this.action = action;
    }

    /**
     * Executes the selection operation defined by the provided SelectActionObject.
     * <p>
     * This method implements the core selection logic: repeatedly searching for target
     * images and swiping if not found. When images are found, it clicks on them and
     * optionally verifies success through confirmation images. The method modifies
     * the state of the provided {@link SelectActionObject} to track results.
     * <p>
     * Side effects on the SelectActionObject parameter:
     * <ul>
     * <li>Resets and updates the total swipe count</li>
     * <li>Sets found matches from each find operation</li>
     * <li>Sets confirmation matches if confirmation is required</li>
     * <li>Updates the success status based on the operation outcome</li>
     * </ul>
     *
     * @param sao The SelectActionObject containing all configuration and state for the
     *            operation. This object is mutated during execution to store results.
     * @return true if the operation succeeded (target found and clicked, with optional
     *         confirmation verified), false if max swipes reached without success
     */
    public boolean select(SelectActionObject sao) {
        sao.resetTotalSwipes();
        for (int i=0; i<sao.getMaxSwipes(); i++) {
            // Use the configuration getters that return either ActionConfig or ActionOptions
            Object findConfig = sao.getFindConfiguration();
            if (findConfig != null) {
                if (findConfig instanceof ActionConfig) {
                    sao.setFoundMatches(action.perform((ActionConfig)findConfig, sao.getFindObjectCollection()));
                } else if (findConfig instanceof ActionOptions) {
                    sao.setFoundMatches(action.perform((ActionOptions)findConfig, sao.getFindObjectCollection()));
                }
            }
            if (sao.getFoundMatches().isSuccess()) {
                Object clickConfig = sao.getClickConfiguration();
                if (clickConfig != null) {
                    if (clickConfig instanceof ActionConfig) {
                        action.perform((ActionConfig)clickConfig, new ObjectCollection.Builder()
                                .withMatches(sao.getFoundMatches())
                                .build());
                    } else if (clickConfig instanceof ActionOptions) {
                        action.perform((ActionOptions)clickConfig, new ObjectCollection.Builder()
                                .withMatches(sao.getFoundMatches())
                                .build());
                    }
                }
                if (sao.getConfirmationObjectCollection() == null) {
                    sao.setSuccess(true);
                    return true;
                }
                else {
                    Object confirmConfig = sao.getConfirmConfiguration();
                    if (confirmConfig != null) {
                        if (confirmConfig instanceof ActionConfig) {
                            sao.setFoundConfirmations(action.perform(
                                    (ActionConfig)confirmConfig, sao.getConfirmationObjectCollection()));
                        } else if (confirmConfig instanceof ActionOptions) {
                            sao.setFoundConfirmations(action.perform(
                                    (ActionOptions)confirmConfig, sao.getConfirmationObjectCollection()));
                        }
                    }
                    if (sao.getFoundConfirmations().isSuccess()) {
                        sao.setSuccess(true);
                        return true;
                    }
                }
            }
            Object swipeConfig = sao.getSwipeConfiguration();
            if (swipeConfig != null) {
                if (swipeConfig instanceof ActionConfig) {
                    action.perform((ActionConfig)swipeConfig, sao.getSwipeFromObjColl(), sao.getSwipeToObjColl());
                } else if (swipeConfig instanceof ActionOptions) {
                    action.perform((ActionOptions)swipeConfig, sao.getSwipeFromObjColl(), sao.getSwipeToObjColl());
                }
            }
            sao.addSwipe();
        }
        return false;
    }
}
