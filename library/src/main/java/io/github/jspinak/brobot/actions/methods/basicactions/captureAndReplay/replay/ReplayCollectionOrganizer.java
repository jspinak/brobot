package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay;

import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.CLICK;
import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.TYPE;

@Component
public class ReplayCollectionOrganizer {

    private int allowedDeviationFromProjectedAngle = 20;

    /**
     * Mouse movements are recorded for every mouse move, but such incremental movements are replayed
     * too slowly by Brobot to match the initial speed of the user. This method organizes the replay
     * collection by merging the mouse movements into one movement. The result is a replay collection
     * that is faster than the original and should match the speed of the user.
     * @param nodeList the list of nodes from the XML file
     * @return the organized replay collection
     */
    public ReplayCollection annotate(NodeList nodeList) {
        ReplayCollection replayCollection = new ReplayCollection();
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) { // make sure it's element node.
                if (tempNode.hasAttributes()) {
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    ReplayObject replayObject = new ReplayObject(nodeMap);
                    replayCollection.add(replayObject);
                }
            }
        }
        setDelays(replayCollection);
        setPivotsByDelayAndType(replayCollection);
        replayCollection.sortByTimelapseFromBeginning();
        setStartAndEndPivots(replayCollection);
        return replayCollection;
    }

    private void setDelays(ReplayCollection replayCollection) {
        replayCollection.sortByTimelapseFromBeginning();
        List<ReplayObject> replayObjects = replayCollection.getReplayObjects();
        for (int i = 0; i < replayObjects.size() - 1; i++) {
            ReplayObject replayObject = replayObjects.get(i);
            ReplayObject nextReplayObject = replayObjects.get(i + 1);
            replayObject.setDelayAfterLastPoint(
                    nextReplayObject.getTimelapseFromStartOfRecording() - replayObject.getTimelapseFromStartOfRecording());
        }
    }
    private void printTopTenMaxDelays(ReplayCollection replayCollection) {
        List<ReplayObject> replayObjects = replayCollection.getReplayObjects();
        Report.println("size of replay collection: " + replayObjects.size());
        replayObjects.sort((o1, o2) -> (int)(o2.getDelayAfterLastPoint() - o1.getDelayAfterLastPoint()));
        for (int i = 0; i < 10; i++) {
            Report.println("Delay: " + replayObjects.get(i).getDelayAfterLastPoint() + " timeLapse: " + replayObjects.get(i).getTimelapseFromStartOfRecording());
        }
    }

    private void setPivotsByDelayAndType(ReplayCollection replayCollection) {
        List<ReplayObject> replayObjects = replayCollection.getReplayObjects();
        for (int i = 0; i < replayObjects.size() - 1; i++) {
            ReplayObject rO = replayObjects.get(i);
            if (rO.getDelayAfterLastPoint() > 40 ||
                rO.getAction() == CLICK ||
                rO.getAction() == TYPE) {
                    replayObjects.get(i).setPivotPoint(true);
            }
        }
    }

    private void setStartAndEndPivots(ReplayCollection replayCollection) {
        List<ReplayObject> replayObjects = replayCollection.getReplayObjects();
        replayObjects.get(0).setPivotPoint(true);
        replayObjects.get(replayObjects.size() - 1).setPivotPoint(true);
    }

    public ReplayCollection getActionsAfterTime(ReplayCollection replayCollection, double time) {
        ReplayCollection replayCollectionAfterTime = new ReplayCollection();
        List<ReplayObject> replayObjects = replayCollection.getReplayObjects();
        for (ReplayObject replayObject : replayObjects) {
            if (replayObject.getTimelapseFromStartOfRecording() >= time) {
                replayCollectionAfterTime.add(replayObject);
            }
        }
        return replayCollectionAfterTime;
    }

    public ReplayCollection getActionsBetweenTimes(ReplayCollection replayCollection, double startTime, double endTime) {
        ReplayCollection replayCollectionBetweenTimes = new ReplayCollection();
        List<ReplayObject> replayObjects = replayCollection.getReplayObjects();
        for (ReplayObject replayObject : replayObjects) {
            if (replayObject.getTimelapseFromStartOfRecording() >= startTime && replayObject.getTimelapseFromStartOfRecording() <= endTime) {
                replayCollectionBetweenTimes.add(replayObject);
            }
        }
        return replayCollectionBetweenTimes;
    }
}
