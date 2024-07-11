package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickLocationOnce;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

/**
 * Clicks on an Image Match, Region, or Location.
 * Fully configurable with ActionOptions.
 * Updates object and State properties after a successful click.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class Click implements ActionInterface {

    private final Find find;
    private final ClickLocationOnce clickLocationOnce;
    private final Time time;
    private final AfterClick afterClick;

    public Click(Find find, ClickLocationOnce clickLocationOnce, Time time, AfterClick afterClick) {
        this.find = find;
        this.clickLocationOnce = clickLocationOnce;
        this.time = time;
        this.afterClick = afterClick;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        find.perform(matches, objectCollections); // find performs only on 1st collection
        int i = 0;
        for (Match match : matches.getMatchList()) {
            Location location = match.getTarget();
            click(location, actionOptions, match);
            i++;
            if (i == actionOptions.getMaxMatchesToActOn()) break;
            // pause only between clicks, not after the last click
            if (i < matches.getMatchList().size()) time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }

    /**
     * @param location is the final, adjusted location.
     * @param actionOptions gives us 4 parameters:
     *                      1) Number of times to click a location in each iteration
     *                         For example, with 3 iterations, NumberOfActions=2, and 2 match locations,
     *                         the set of actions
     *                         (location #1 will be clicked twice, location #2 will be clicked twice)
     *                         will occur 3 times.
     *                      2) Pause after each action
     *                      3) Move mouse after click when selected (handled by AfterClick class)
     *                      4) New StateProbabilities on click (handled by AfterClick class)
     */
    private void click(Location location, ActionOptions actionOptions, Match match) {
        for (int i = 0; i < actionOptions.getTimesToRepeatIndividualAction(); i++) {
            clickLocationOnce.click(location, actionOptions);
            match.incrementTimesActedOn();
            if (actionOptions.isMoveMouseAfterAction()) {
                time.wait(actionOptions.getPauseBetweenIndividualActions());
                afterClick.moveMouseAfterClick(actionOptions);
            }
            if (i < actionOptions.getTimesToRepeatIndividualAction() - 1) {
                time.wait(actionOptions.getPauseBetweenIndividualActions());
            }
        }
    }
}
