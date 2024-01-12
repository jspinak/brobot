package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
public class TempStateWithImages {

    private final String name;
    private final Set<Integer> scenes = new HashSet<>();
    private final List<StateImage> images = new ArrayList<>();

    public TempStateWithImages(String name) {
        this.name = name;
    }

    public void addImage(StateImage stateImage) {
        if (isImageNested(stateImage)) return; // don't add if another image's region contains its region
        images.add(stateImage);
    }

    public boolean contains(int scene) {
        return scenes.contains(scene);
    }

    public boolean hasEqualSceneSets(Set<Integer> scenes) {
        return this.scenes.equals(scenes);
    }

    private boolean isImageNested(StateImage stateImage) {
        for (StateImage image : images) {
            if (image.getLargestDefinedFixedRegionOrNewRegion().contains(stateImage.getLargestDefinedFixedRegionOrNewRegion()))
                return true;
        }
        return false;
    }
}
