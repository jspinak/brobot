package io.github.jspinak.brobot.actions.methods.basicactions.find.motion;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysisCollection;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.illustratedHistory.Illustrations;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawMatch;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class IllustrateMotion {

    private final DrawMatch drawMatch;
    private final MatVisualize matVisualize;
    private final MatOps3d matOps3d;

    public IllustrateMotion(DrawMatch drawMatch, MatVisualize matVisualize, MatOps3d matOps3d) {
        this.drawMatch = drawMatch;
        this.matVisualize = matVisualize;
        this.matOps3d = matOps3d;
    }

    public void setMotionMatAndWriteIllustration(SceneAnalysisCollection sceneAnalysisCollection, int index,
                                                 Mat motionMask) {
        Illustrations illustrations = sceneAnalysisCollection.getSceneAnalyses().get(index).getIllustrations();
        illustrations.setMotion(motionMask);
        Mat motionWithMatches = getSceneWithMotion(sceneAnalysisCollection, new Scalar(254, 183, 146, 0));
        matVisualize.writeMatToHistory(motionWithMatches, "motionWithMatches");
    }

    public Mat getSceneWithMotion(SceneAnalysisCollection sceneAnalysisCollection, Scalar color) {
        Optional<SceneAnalysis> optLastScene = sceneAnalysisCollection.getLastSceneAnalysis();
        if (optLastScene.isEmpty()) return new Mat();
        Mat sceneMat = optLastScene.get().getScene().getMatBGR();
        List<Match> dynamicPixelMatches = sceneAnalysisCollection.getContours().getMatchList();
        return getSceneWithMotion(sceneMat, sceneAnalysisCollection.getResults(), dynamicPixelMatches, color);
    }

    public Mat getSceneWithMotion(Mat scene, Mat motionMask, List<Match> motionBoundingBoxes, Scalar color) {
        Mat baseImage = scene.clone();
        matOps3d.addColorToMat(baseImage, motionMask, color);
        drawMatch.drawMatches(baseImage, motionBoundingBoxes, color);
        return baseImage;
    }
}
