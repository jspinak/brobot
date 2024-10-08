package io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.buildStateStructure;

import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.ScreenObservation;
import io.github.jspinak.brobot.app.stateStructureBuilders.buildWithoutNames.screenObservations.StatelessImage;
import lombok.Getter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ObservedState {

    private Mat screenshot;
    private Mat mask;
    private int id;
    private List<ScreenObservation> screens = new ArrayList<>();
    private List<StatelessImage> images = new ArrayList<>();

    public ObservedState(Mat screenshot, Mat mask, int id, ScreenObservation screen) {
        this.screenshot = screenshot;
        this.mask = mask;
        this.id = id;
        this.screens.add(screen);
    }

    public void addScreen(ScreenObservation screen) {
        this.screens.add(screen);
    }

}
