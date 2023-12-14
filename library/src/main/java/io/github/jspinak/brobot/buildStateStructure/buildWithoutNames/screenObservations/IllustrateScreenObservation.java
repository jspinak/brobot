package io.github.jspinak.brobot.buildStateStructure.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.actions.methods.basicactions.find.motion.IllustrateMotion;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawMatch;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawRect;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

@Component
public class IllustrateScreenObservation {

    private final IllustrateMotion illustrateMotion;
    private final DrawMatch drawMatch;
    private final MatVisualize matVisualize;
    private final DrawRect drawRect;

    private Scalar babyBlue = new Scalar(173, 216, 230, 255);

    public IllustrateScreenObservation(IllustrateMotion illustrateMotion, DrawMatch drawMatch, MatVisualize matVisualize,
                                       DrawRect drawRect) {
        this.illustrateMotion = illustrateMotion;
        this.drawMatch = drawMatch;
        this.matVisualize = matVisualize;
        this.drawRect = drawRect;
    }

    public Mat getScreenWithMotionAndImages(ScreenObservation screenObservation) {
        Mat illustratedScene = illustrateMotion.getSceneWithMotion(
                screenObservation.getMatches().getSceneAnalysisCollection(), new Scalar(254, 183, 146, 200));
        drawRect.drawRectAroundRegions(illustratedScene, screenObservation.getImageRegions(), babyBlue);
        return illustratedScene;
    }

    public void writeIllustratedSceneToHistory(ScreenObservation screenObservation) {
        Mat illustratedScene = getScreenWithMotionAndImages(screenObservation);
        matVisualize.writeMatToHistory(illustratedScene, "screen with motion and images matches");
    }

}
