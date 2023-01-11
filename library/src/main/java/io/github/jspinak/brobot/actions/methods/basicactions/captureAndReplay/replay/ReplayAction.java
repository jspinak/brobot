package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

@Component
public class ReplayAction {

    private Action action;

    public ReplayAction(Action action) {
        this.action = action;
    }

    public void replay(ActionOptions.Action actionEnum, int x, int y, String key, double delayInMillis) {
        ActionOptions actionOptions = new ActionOptions.Builder()
                .setAction(actionEnum)
                //.setPauseBeforeBegin(delayInMillis)
                .build();
        Location mouseLocation = new Location(x, y);
        ObjectCollection objectCollection = new ObjectCollection.Builder()
                .withLocations(mouseLocation)
                .withStrings(key)
                .build();
        Report.println("Replaying action: " + actionOptions.getAction().toString() + " @ x=" + x + " y=" + y + " key:" + key + " millis:" + delayInMillis);
        action.perform(actionOptions, objectCollection);
    }

    public void replay(ReplayObject replayObject) {
        replay(replayObject.getAction(), replayObject.getX(), replayObject.getY(), replayObject.getKey(), replayObject.getTimelapseFromStartOfRecording());
    }
}
