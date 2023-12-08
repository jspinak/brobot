package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.contours.Contours;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawMatch;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

@Component
public class IllustrateMotion {

    private final DrawMatch drawMatch;
    private final MatVisualize matVisualize;

    public IllustrateMotion(DrawMatch drawMatch, MatVisualize matVisualize) {
        this.drawMatch = drawMatch;
        this.matVisualize = matVisualize;
    }

    public void illustrateMotion(SceneAnalysisCollection sceneAnalysisCollection, int index, Mat absdiff, Contours contours) {
        Illustrations illustrations = sceneAnalysisCollection.getSceneAnalyses().get(index).getIllustrations();
        illustrations.setMotion(absdiff);
        Mat motionWithMatches = absdiff.clone();
        drawMatch.drawMatches(motionWithMatches, contours.getMatches(), new Scalar(254, 183, 146, 0));
        matVisualize.writeMatToHistory(motionWithMatches, "motionWithMatches");
    }
}
