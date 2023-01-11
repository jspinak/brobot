package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ReplayCollection {

    private List<ReplayObject> replayObjects = new ArrayList<>();

    public void add(ReplayObject replayObject) {
        replayObjects.add(replayObject);
    }

    public void sortByTimelapseFromBeginning() {
        replayObjects.sort((o1, o2) -> {
            if (o1.getTimelapseFromStartOfRecording() > o2.getTimelapseFromStartOfRecording()) {
                return 1;
            } else if (o1.getTimelapseFromStartOfRecording() < o2.getTimelapseFromStartOfRecording()) {
                return -1;
            } else {
                return 0;
            }
        });
    }
}
