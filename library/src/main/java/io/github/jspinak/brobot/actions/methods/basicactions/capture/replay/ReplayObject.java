package io.github.jspinak.brobot.actions.methods.basicactions.capture.replay;

import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.NamedNodeMap;

@Getter
@Setter
public class ReplayObject {
    private String action;
    private int x;
    private int y;
    private String key;
    private double timelapseFromStartOfRecording; // in milliseconds
    private double delayAfterLastPoint; // in milliseconds
    private boolean pivotPoint = false;
    private int group = 0; // a group begins and ends with pivot points
    private double angleInGroup = 0;
    private double meanAngleInGroup = 0;
    private double stdDevOfAnglesInGroup = 0;

    public boolean isObjectReady() {
        return action != null;
    }

    public ReplayObject(NamedNodeMap nodeMap) {
        action = nodeMap.getNamedItem("action").getNodeValue();
        x = Integer.parseInt(nodeMap.getNamedItem("x").getNodeValue());
        y = Integer.parseInt(nodeMap.getNamedItem("y").getNodeValue());
        key = nodeMap.getNamedItem("key").getNodeValue();
        timelapseFromStartOfRecording = Double.parseDouble(nodeMap.getNamedItem("millis").getNodeValue());
    }
}
