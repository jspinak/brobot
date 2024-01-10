package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.state.State;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.imageUtils.MatBuilder;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;

@Component
public class IllustrateState {

    private final ImageUtils imageUtils;

    public IllustrateState(ImageUtils imageUtils) {
        this.imageUtils = imageUtils;
    }

    /**
     * Illustrates the state by placing the first Pattern of each StateImage in the Pattern's fixed region.
     * There are other ways to show the state, such as using MatchSnapshots.
     * @param state the state to illustrate.
     * @return the illustrated state as a Mat
     */
    public Mat illustrateWithFixedSearchRegions(State state) {
        Region r = new Region();
        Mat background = new Mat(r.h, r.w, CV_8UC4); // set the background to the screen size, fill with black
        MatBuilder matBuilder = new MatBuilder();
        matBuilder.setMat(background);
        for (StateImage image : state.getStateImages()) {
            if (!image.isEmpty()) {
                Pattern p = image.getPatterns().get(0);
                if (p.isDefined()) {
                    Location xy = new Location(p.getRegion().x, p.getRegion().y);
                    matBuilder.addSubMat(xy, p.getMat());
                }
            }
        }
        return matBuilder.build();
    }

    public void writeIllustratedStateToFile(State state, String filename) {
        Mat mat = illustrateWithFixedSearchRegions(state);
        imageUtils.writeWithUniqueFilename(mat, filename);
    }
}
