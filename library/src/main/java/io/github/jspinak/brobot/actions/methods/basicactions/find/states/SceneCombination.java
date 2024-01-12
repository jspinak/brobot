package io.github.jspinak.brobot.actions.methods.basicactions.find.states;

import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SceneCombination {

    private Mat dynamicPixels;
    private int scene1;
    private int scene2;
    private List<StateImage> images = new ArrayList<>();

    public SceneCombination(Mat dynamicPixels, int scene1, int scene2) {
        this.dynamicPixels = dynamicPixels;
        this.scene1 = scene1;
        this.scene2 = scene2;
    }

    public void addImage(StateImage stateImage) {
        images.add(stateImage);
    }

    public boolean contains(StateImage stateImage) {
        return images.contains(stateImage);
    }

    public boolean contains(int scene) {
        return scene1 == scene || scene2 == scene;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Scenes: ").append(scene1).append(" ").append(scene2).append(" ");
        stringBuilder.append("Images: ");
        images.forEach(image -> stringBuilder.append(image).append(" "));
        return stringBuilder.toString();
    }

}
