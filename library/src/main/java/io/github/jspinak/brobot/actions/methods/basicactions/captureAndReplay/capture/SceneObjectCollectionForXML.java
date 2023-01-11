package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SceneObjectCollectionForXML {

    private List<SceneAndObjectsForXML> scenes = new ArrayList<>();

    public void addScene(SceneAndObjectsForXML scene) {
        scenes.add(scene);
    }
}
