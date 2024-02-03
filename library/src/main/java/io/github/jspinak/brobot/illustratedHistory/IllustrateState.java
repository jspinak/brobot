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
        MatBuilder matBuilder = new MatBuilder().init();
        for (StateImage image : state.getStateImages()) {
            if (!image.isEmpty()) {
                Pattern p = image.getPatterns().get(0);
                if (p.isDefined()) {
                    Region r = p.getRegion();
                    Location xy = new Location(r.x(), r.y());
                    Mat mat = p.getMat();
                    matBuilder.addSubMat(xy, mat);
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
