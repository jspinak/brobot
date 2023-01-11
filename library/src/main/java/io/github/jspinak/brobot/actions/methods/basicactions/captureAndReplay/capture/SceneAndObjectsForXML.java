package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import lombok.Getter;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SceneAndObjectsForXML {

    private String sceneName;
    private List<String> objectsNames = new ArrayList<>();
    private List<Location> objectsLocations = new ArrayList<>();

    public SceneAndObjectsForXML(int sceneNumber) {
        this.sceneName = String.valueOf(sceneNumber);
    }

    public SceneAndObjectsForXML(int sceneNumber, List<String> objectsNames, List<Location> objectsLocations) {
        this.sceneName = String.valueOf(sceneNumber);
        this.objectsNames = objectsNames;
        this.objectsLocations = objectsLocations;
    }

    public SceneAndObjectsForXML(Node sceneAsNode) {
        this.sceneName = sceneAsNode.getAttributes().getNamedItem("name").getNodeValue();
        for (int i = 0; i < sceneAsNode.getChildNodes().getLength(); i++) {
            Node objectAsNode = sceneAsNode.getChildNodes().item(i);
            if (objectAsNode.getNodeName().equals("object")) {
                objectsNames.add(objectAsNode.getAttributes().getNamedItem("name").getNodeValue());
                objectsLocations.add(new Location(
                        Integer.parseInt(objectAsNode.getAttributes().getNamedItem("x").getNodeValue()),
                        Integer.parseInt(objectAsNode.getAttributes().getNamedItem("y").getNodeValue())));
            }
        }
    }

    public void addObject(String objectName, Location objectLocation) {
        objectsNames.add(objectName);
        objectsLocations.add(objectLocation);
    }

}
