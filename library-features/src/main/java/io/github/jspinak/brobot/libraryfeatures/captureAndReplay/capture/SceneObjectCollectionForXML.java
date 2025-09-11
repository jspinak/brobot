package io.github.jspinak.brobot.libraryfeatures.captureAndReplay.capture;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class SceneObjectCollectionForXML {

    private List<SceneAndObjectsForXML> scenes = new ArrayList<>();

    public void addScene(SceneAndObjectsForXML scene) {
        scenes.add(scene);
    }
}
