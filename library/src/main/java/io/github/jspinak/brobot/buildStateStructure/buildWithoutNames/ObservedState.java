package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ObservedState {

    private Mat screenshot;
    private Mat mask;
    private int id;
    private List<Integer> screens = new ArrayList<>();

    public ObservedState(Mat screenshot, Mat mask, int id, int screen) {
        this.screenshot = screenshot;
        this.mask = mask;
        this.id = id;
        this.screens.add(screen);
    }

    public void addScreen(int screen) {
        this.screens.add(screen);
    }

}
