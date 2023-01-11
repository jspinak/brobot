package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay;

import org.springframework.stereotype.Component;

@Component
public class ReplayActionsXml {

    private ReplayAction replayAction;

    public ReplayActionsXml(ReplayAction replayAction) {
        this.replayAction = replayAction;
    }

    public void replay(ReplayCollection replayCollection) {
        //Report.println("size of replay collection: " + replayCollection.getReplayObjects().size());
        int i =0 ;
        for (ReplayObject replayObject : replayCollection.getReplayObjects()) {
            if (replayObject.isObjectReady() && replayObject.isPivotPoint()) replayAction.replay(replayObject);
            //Report.println("node number " + i + " delay: " + replayObject.getDelayAfterLastPoint() + " timeLapse: " + replayObject.getTimelapseFromStartOfRecording());
            i++;
        }
    }

}
