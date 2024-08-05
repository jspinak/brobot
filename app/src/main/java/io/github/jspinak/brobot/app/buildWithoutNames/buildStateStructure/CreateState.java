package io.github.jspinak.brobot.app.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.buildWithoutNames.preliminaryStates.ImageSetsAndAssociatedScreens;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.illustratedHistory.StateIllustration;
import io.github.jspinak.brobot.illustratedHistory.StateIllustrator;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateState {

    private final StateIllustrator stateIllustrator;

    public CreateState(StateIllustrator stateIllustrator) {
        this.stateIllustrator = stateIllustrator;
    }

    /**
     * Saves state images to a database, creates a state with these images.
     * @param imageSets used to identify which images belong to the state.
     * @param name state name
     * @return the newly created state
     */
    public State createState(ImageSetsAndAssociatedScreens imageSets, String name, Region usableArea) {
        List<StateImage> stateImages = imageSets.getStateImages();
        List<Image> scenes = new ArrayList<>();
        List<StateIllustration> stateIllustrations = new ArrayList<>();
        populateScenesAndIllustrations(imageSets, usableArea, scenes, stateIllustrations);
        return createState(name, stateImages, scenes, stateIllustrations);
    }

    private void populateScenesAndIllustrations(ImageSetsAndAssociatedScreens imageSets, Region usableArea,
                                               List<Image> scenes, List<StateIllustration> stateIllustrations) {
        for (Pattern screen : imageSets.getScreens()) {
            Mat screenMat = screen.getMat();
            scenes.add(new Image(screenMat));
            stateIllustrations.add(new StateIllustration(new Mat(screenMat, usableArea.getJavaCVRect())));
        }
    }

    private State createState(String stateName, List<StateImage> stateImages, List<Image> scenes,
                             List<StateIllustration> stateIllustrations) {
        State newState = new State.Builder(stateName)
                .withImages(stateImages)
                .withScenes(scenes)
                .addIllustrations(stateIllustrations)
                .build();
        for (StateIllustration stateIllustration : newState.getIllustrations()) {
            stateIllustrator.drawState(newState, stateIllustration);
        }
        return newState;
    }
}
