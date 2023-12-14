package io.github.jspinak.brobot.actions.methods.basicactions.click;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.GetSceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.ClickLocationOnce;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Clicks on an Image Match, Region, or Location.
 * Fully configurable with ActionOptions.
 * Updates object and State properties after a successful click.
 *
 * <p>Author: Joshua Spinak</p>
 */
@Component
public class Click implements ActionInterface {

    private Find find;
    private ClickLocationOnce clickLocationOnce;
    private Wait wait;
    private AfterClick afterClick;
    private GetSceneAnalysisCollection getSceneAnalysisCollection;

    public Click(Find find, ClickLocationOnce clickLocationOnce, Wait wait, AfterClick afterClick,
                 GetSceneAnalysisCollection getSceneAnalysisCollection) {
        this.find = find;
        this.clickLocationOnce = clickLocationOnce;
        this.wait = wait;
        this.afterClick = afterClick;
        this.getSceneAnalysisCollection = getSceneAnalysisCollection;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = find.perform(actionOptions, objectCollections); // find performs only on 1st collection
        if (BrobotSettings.saveHistory) {
            SceneAnalysisCollection sceneAnalysisCollection = getSceneAnalysisCollection.
                    get(Arrays.asList(objectCollections), actionOptions);
            matches.setSceneAnalysisCollection(sceneAnalysisCollection);
        }
        int i = 0;
        for (MatchObject matchObject : matches.getMatchObjects()) {
            Location location = setClickLocation(matchObject, actionOptions);
            click(location, actionOptions, matchObject);
            i++;
            if (i == actionOptions.getMaxMatchesToActOn()) break;
            // pause only between clicks, not after the last click
            if (i < matches.getMatchObjects().size()) wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        return matches;
    }

    private Location setClickLocation(MatchObject matchObject, ActionOptions actionOptions) {
        // Define the location by the match region and the position of the StateObject
        Location location = matchObject.getLocation();
        location.setX(location.getX() + actionOptions.getAddX());
        location.setY(location.getY() + actionOptions.getAddY());
        matchObject.getMatch().setTarget(location.getX(), location.getY());
        return location;
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
    private void click(Location location, ActionOptions actionOptions, MatchObject matchObject) {
        for (int i = 0; i < actionOptions.getTimesToRepeatIndividualAction(); i++) {
            clickLocationOnce.click(location, actionOptions);
            matchObject.getStateObject().addTimesActedOn();
            if (actionOptions.isMoveMouseAfterClick()) {
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
                afterClick.moveMouseAfterClick(actionOptions);
            }
            if (i < actionOptions.getTimesToRepeatIndividualAction() - 1) {
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
            }
        }
    }
}
