package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.Wait;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.database.primitives.location.Location;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.state.ObjectCollection;
import io.github.jspinak.brobot.illustratedHistory.IllustrateScreenshot;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Mouse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Moves the mouse to one or more locations.
 * There can be multiple points per ObjectCollection if Find.EACH or Find.ALL is used.
 * There may be multiple ObjectCollections.
 * Points are visited in the following order:
 *   Within an ObjectCollection, as recorded by the Find operation (Images, Matches, Regions, Locations)
 *   In the order the ObjectCollection appears
 */
@Component
public class MoveMouse implements ActionInterface {

    private final Find find;
    private final MoveMouseWrapper moveMouseWrapper;
    private final Wait wait;
    private IllustrateScreenshot illustrateScreenshot;

    public MoveMouse(Find find, MoveMouseWrapper moveMouseWrapper, Wait wait,
                     IllustrateScreenshot illustrateScreenshot) {
        this.find = find;
        this.moveMouseWrapper = moveMouseWrapper;
        this.wait = wait;
        this.illustrateScreenshot = illustrateScreenshot;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        if (collections.size() == 0) addOffsetToCollections(collections, actionOptions);
        for (ObjectCollection objColl : collections) {
            Matches newMatches = find.perform(actionOptions, objColl);
            newMatches.getMatchLocations().forEach(moveMouseWrapper::move);
            Report.print("finished move. ");
            matches.addAll(newMatches);
            if (newMatches.isSuccess()) matches.setSuccess(true);
            if (collections.indexOf(objColl) < collections.size() - 1)
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
        illustrateScreenshot.drawMove(matches.getMatchLocations());
        return matches;
    }

    private boolean addOffsetToCollections(List<ObjectCollection> collections, ActionOptions actionOptions) {
        if (actionOptions.getAddX() == 0 && actionOptions.getAddY() == 0) return false;
        Location location = new Location(Mouse.at(), actionOptions.getAddX(), actionOptions.getAddY());
        collections.add(new ObjectCollection.Builder()
                .withLocations(location)
                .build());
        return true;
    }

}
