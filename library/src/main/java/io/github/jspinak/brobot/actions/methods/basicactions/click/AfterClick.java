package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.reports.Report;
import io.github.jspinak.brobot.services.StateService;
import org.springframework.stereotype.Component;

/**
 * After a successful click,
 * Sets the probability that the object remains visible or disappears. This is used for mocking.
 * Increases the times this object has been clicked during this action. TimesActedOn will reset to 0 when
 * the action is complete.
 * Move the mouse when the option in ActionOptions is specified. This is often practical for multiple clicks
 * to the same image, either to better visualize individual clicks or to avoid blocking an image.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class AfterClick {

    private final StateService stateService;
    private final MoveMouseWrapper moveMouseWrapper;

    public AfterClick(StateService stateService, MoveMouseWrapper moveMouseWrapper) {
        this.stateService = stateService;
        this.moveMouseWrapper = moveMouseWrapper;
    }

    /**
     * We have 2 options for moving the mouse after a click:
     * 1) To an offset of the click point
     * 2) To a fixed location
     *
     * If the offset is defined we move there; otherwise we move to the fixed location.
     *
     * @param actionOptions contains all details of the action, including moving the mouse after a click
     */
    public boolean moveMouseAfterClick(ActionOptions actionOptions) {
        if (!actionOptions.isMoveMouseAfterClick()) return false;
        Report.print("after click, ");
        if (actionOptions.getOffsetLocationBy().defined())
            return moveMouseWrapper.move(actionOptions.getOffsetLocationBy());
        return moveMouseWrapper.move(actionOptions.getLocationAfterClick());
    }
}
