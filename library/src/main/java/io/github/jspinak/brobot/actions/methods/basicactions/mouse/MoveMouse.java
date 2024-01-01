package io.github.jspinak.brobot.actions.methods.basicactions.mouse;

import io.github.jspinak.brobot.actions.actionExecution.ActionInterface;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.Find;
import io.github.jspinak.brobot.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
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
    private final Time time;

    public MoveMouse(Find find, MoveMouseWrapper moveMouseWrapper, Time time) {
        this.find = find;
        this.moveMouseWrapper = moveMouseWrapper;
        this.time = time;
    }

    public void perform(Matches matches, ObjectCollection... objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        for (ObjectCollection objColl : collections) {
            find.perform(matches, objColl);
            matches.getMatchLocations().forEach(moveMouseWrapper::move);
            Report.print("finished move. ");
            if (collections.indexOf(objColl) < collections.size() - 1)
                time.wait(actionOptions.getPauseBetweenIndividualActions());
        }
    }



}
