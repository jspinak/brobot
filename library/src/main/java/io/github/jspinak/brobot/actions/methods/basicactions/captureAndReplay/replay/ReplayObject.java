package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.replay;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.NamedNodeMap;


@Getter
@Setter
public class ReplayObject {
    private ActionOptions.Action action;
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
        action = ActionOptions.Action.valueOf(nodeMap.getNamedItem("action").getNodeValue());
        x = Integer.parseInt(nodeMap.getNamedItem("x").getNodeValue());
        y = Integer.parseInt(nodeMap.getNamedItem("y").getNodeValue());
        String keyString = nodeMap.getNamedItem("key").getNodeValue();
        if (keyString.equals("")) {
            key = null;
        } else {
            int asciiInt = Integer.parseInt(nodeMap.getNamedItem("key").getNodeValue());
            if (asciiInt <= 127 && asciiInt >= 32) {
                key = String.valueOf((char) asciiInt);
            } else {
                key = null;
            }
        }
        timelapseFromStartOfRecording = Double.parseDouble(nodeMap.getNamedItem("millis").getNodeValue());
    }

}
