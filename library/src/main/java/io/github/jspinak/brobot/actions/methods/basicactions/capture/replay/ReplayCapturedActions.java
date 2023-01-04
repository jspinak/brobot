package io.github.jspinak.brobot.actions.methods.basicactions.capture.replay;

import io.github.jspinak.brobot.actions.actionExecution.Action;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.capture.CaptureConversions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.state.ObjectCollection;
import io.github.jspinak.brobot.imageUtils.GetImageJavaCV;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

@Component
public class ReplayCapturedActions {

    private Mat capturedActions;

    private GetImageJavaCV getImageJavaCV;
    private CaptureConversions captureConversions;
    private Action action;

    public ReplayCapturedActions(GetImageJavaCV getImageJavaCV, CaptureConversions captureConversions,
                                 Action action) {
        this.getImageJavaCV = getImageJavaCV;
        this.captureConversions = captureConversions;
        this.action = action;
    }

    public void setActionsMat() {
        capturedActions = getImageJavaCV.getMatFromFilename("capture/actions.bmp", ColorCluster.ColorSchemaName.BGR);
    }

    public void replayActionsFromMat() {
        MatOps.info(capturedActions);
        MatOps.printPartOfMat(capturedActions, 10, 5, 1, "actions");

        double lastTimestamp = 0;
        Report.println("# of actions = " + capturedActions.rows());
        for (int i = 0; i < capturedActions.rows(); i++) {
            double[] row = MatOps.getDoubleRow(i, capturedActions);
            int actionInt = (int) row[0];
            double delay = row[4] - lastTimestamp;
            //Report.println("delay in millis = " + delay);
            lastTimestamp = row[4];
            if (captureConversions.getActionMap().containsKey(actionInt)) {
                ActionOptions actionOptions = new ActionOptions.Builder()
                        .setAction(captureConversions.getActionMap().get(actionInt))
                        .setPauseBeforeBegin(delay/1000)
                        .build();
                Location mouseLocation = new Location((int)row[1], (int)row[2]);
                ObjectCollection objectCollection = new ObjectCollection.Builder()
                        .withLocations(mouseLocation)
                        .build();
                //Report.println("Replaying action: " + actionOptions.getAction().toString() + " @ x=" + row[1] + " y=" + row[2] + " key:" + row[3] + " millis:" + delay);
                //action.perform(actionOptions, objectCollection);
            }
        }
    }
}
