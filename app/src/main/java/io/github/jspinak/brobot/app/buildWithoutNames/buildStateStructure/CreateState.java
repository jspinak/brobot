package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.preliminaryStates.ImageSetsAndAssociatedScreens;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateState {

    /**
     * Saves state images to a database, creates a state with these images.
     * @param imageSets used to identify which images belong to the state.
     * @param name state name
     * @return the newly created state
     */
    public State createState(ImageSetsAndAssociatedScreens imageSets, String name, Region usableArea) {
        List<StateImage> stateImages = imageSets.getStateImages();
        List<Image> scenes = new ArrayList<>();
        for (Pattern screen : imageSets.getScreens()) scenes.add(new Image(screen));
        return createState(name, stateImages, scenes, usableArea);
    }

    private State createState(String stateName, List<StateImage> stateImages, List<Image> scenes, Region usableArea) {
        return new State.Builder(stateName)
                .withImages(stateImages)
                .withScenes(scenes)
                .setUsableArea(usableArea)
                .build();
    }
}
