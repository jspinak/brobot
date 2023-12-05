package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames;

import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

@Setter
@Getter
public class DecisionMat {

    private Mat analysis;
    private Mat changedPixels;
    private Mat combinedMats;
    private int numberOfChangedPixels;
    private int screenComparedTo;

    public String getFilename() {
        return "decisionMat changed = " + numberOfChangedPixels + " compared to = " + screenComparedTo + " random";
    }
}
