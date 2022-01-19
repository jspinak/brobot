package actions.methods.basicactions.mouse;

import com.brobot.multimodule.actions.actionExecution.ActionInterface;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.actions.methods.basicactions.find.Find;
import com.brobot.multimodule.actions.methods.sikuliWrappers.Wait;
import com.brobot.multimodule.actions.methods.sikuliWrappers.mouse.MoveMouseWrapper;
import com.brobot.multimodule.database.primitives.location.Location;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.state.ObjectCollection;
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

    public MoveMouse(Find find, MoveMouseWrapper moveMouseWrapper, Wait wait) {
        this.find = find;
        this.moveMouseWrapper = moveMouseWrapper;
        this.wait = wait;
    }

    public Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections) {
        Matches matches = new Matches();
        List<ObjectCollection> collections = Arrays.asList(objectCollections);
        if (collections.size() == 0) addOffsetToCollections(collections, actionOptions);
        for (ObjectCollection objColl : collections) {
            Matches newMatches = find.perform(actionOptions, objColl);
            newMatches.getMatchLocations().forEach(moveMouseWrapper::move);
            matches.addAll(newMatches);
            if (newMatches.isSuccess()) matches.setSuccess(true);
            if (collections.indexOf(objColl) < collections.size() - 1)
                wait.wait(actionOptions.getPauseBetweenIndividualActions());
        }
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
