package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class TempStateWithImages {

    private String name;
    private Set<Integer> scenes = new HashSet<>();
    private List<StateImage> images = new ArrayList<>();

    public TempStateWithImages(String name) {
        this.name = name;
    }

    public void addImage(StateImage stateImage) {
        images.add(stateImage);
    }

    public boolean contains(int scene) {
        return scenes.contains(scene);
    }

    public boolean hasEqualSceneSets(Set<Integer> scenes) {
        return this.scenes.equals(scenes);
    }
}
