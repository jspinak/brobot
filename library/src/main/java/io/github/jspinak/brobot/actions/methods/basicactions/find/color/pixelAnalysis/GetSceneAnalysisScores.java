package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorStatProfile;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import io.github.jspinak.brobot.imageUtils.MatVisualize;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.SCORE_DIST_BELOW_THRESHHOLD;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.*;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo.ColorStat.MEAN;

@Component
public class GetSceneAnalysisScores {

    private MatOps3d matOps3d;
    private MatVisualize matVisualize;

    public GetSceneAnalysisScores(MatOps3d matOps3d, MatVisualize matVisualize) {
        this.matOps3d = matOps3d;
        this.matVisualize = matVisualize;
    }

    /**
     * Each PixelAnalysisCollection has a Mat with the score distance below the threshold. The largest
     * score below the threshold for a given pixel gets the corresponding index (from the StateImage).
     * If all scores below the threshold are 0, there is no match and the index remains 0.
     *
     * @param sceneAnalysis the analysis of a scene with all of its StateImages
     */
    public void setSceneAnalysisIndices(SceneAnalysis sceneAnalysis) {
        List<PixelAnalysisCollection> pixelAnalysisCollections = sceneAnalysis.getPixelAnalysisCollections();
        if (pixelAnalysisCollections.size() == 0) return;
        Mat scoresBelowThresholdHSV0 = pixelAnalysisCollections.get(0).getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, HSV);
        Mat scoreBelowThresholdBGR0 = pixelAnalysisCollections.get(0).getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR);
        Mat indicesHSV = new Mat(scoresBelowThresholdHSV0.size(), scoresBelowThresholdHSV0.type(), new Scalar(0, 0, 0, 0));
        Mat indicesBGR = new Mat(scoreBelowThresholdBGR0.size(), scoreBelowThresholdBGR0.type(), new Scalar(0, 0, 0, 0));
        Mat bestScoresHSV = new Mat(scoresBelowThresholdHSV0.size(), scoresBelowThresholdHSV0.type(), new Scalar(0, 0, 0, 0));
        Mat bestScoresBGR = new Mat(scoreBelowThresholdBGR0.size(), scoreBelowThresholdBGR0.type(), new Scalar(0, 0, 0, 0));
        for (int i = 0; i < pixelAnalysisCollections.size(); i++) {
            PixelAnalysisCollection pixelAnalysisCollection = pixelAnalysisCollections.get(i);
            Mat scoresBelowThresholdHSV = pixelAnalysisCollection.getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, HSV);
            Mat scoreBelowThresholdBGR = pixelAnalysisCollection.getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR);
            int index = pixelAnalysisCollection.getStateImage().getIndex();
            matOps3d.getIndicesOfMax(bestScoresHSV, scoresBelowThresholdHSV, indicesHSV, index);
            matOps3d.getIndicesOfMax(bestScoresBGR, scoreBelowThresholdBGR, indicesBGR, index);
        }
        sceneAnalysis.addAnalysis(HSV, INDICES_3D, indicesHSV);
        sceneAnalysis.addAnalysis(BGR, INDICES_3D, indicesBGR);
        Mat indices2D = MatOps.getFirstChannel(indicesHSV); // just set the 2D indices to the Hue indices of the 3D matrix
        sceneAnalysis.addAnalysis(HSV, INDICES_2D, indices2D);
    }

    public void setSceneAnalysisIndicesTargetsOnly(SceneAnalysis sceneAnalysis, Set<StateImage> targets) {
        Set<Integer> targetIndices = targets.stream().map(StateImage::getIndex).collect(Collectors.toSet());
        Mat indicesHSV3D = sceneAnalysis.getAnalysis(HSV, INDICES_3D);
        Mat indicesBGR3D = sceneAnalysis.getAnalysis(BGR, INDICES_3D);
        Mat indicesHSV3Dtargets = matOps3d.getMatWithOnlyTheseIndices(indicesHSV3D, targetIndices);
        Mat indicesBGR3Dtargets = matOps3d.getMatWithOnlyTheseIndices(indicesBGR3D, targetIndices);
        sceneAnalysis.addAnalysis(HSV, INDICES_3D_TARGETS, indicesHSV3Dtargets);
        sceneAnalysis.addAnalysis(BGR, INDICES_3D_TARGETS, indicesBGR3Dtargets);
    }

    public void setBGRVisualizationMats(SceneAnalysis sceneAnalysis) {
        Map<Integer, Scalar> hueList = getHueMap(sceneAnalysis);
        Mat hsv3D = sceneAnalysis.getAnalysis(HSV, INDICES_3D);
        if (hsv3D == null) return; // no indices to visualize
        Mat bgrColorMatFromHSV2dIndexMat = matVisualize.getBGRColorMatFromHSV2dIndexMat(hsv3D, hueList);
        sceneAnalysis.addAnalysis(BGR, BGR_FROM_INDICES_2D, bgrColorMatFromHSV2dIndexMat);
        Mat hsv3Dtargets = sceneAnalysis.getAnalysis(HSV, INDICES_3D_TARGETS);
        if (hsv3Dtargets == null) return; // no targets
        Mat hsvTargetsColorMat = matVisualize.getBGRColorMatFromHSV2dIndexMat(hsv3Dtargets, hueList);
        sceneAnalysis.addAnalysis(BGR, BGR_FROM_INDICES_2D_TARGETS, hsvTargetsColorMat);
    }

    private Map<Integer, Scalar> getHueMap(SceneAnalysis sceneAnalysis) {
        Map<Integer, Scalar> hueList = new HashMap<>();
        Mat indicesHSV = sceneAnalysis.getAnalysis(HSV, INDICES_2D);
        for (StateImage img : sceneAnalysis.getStateImageObjects()) {
            if (MatOps.firstChannelContains(indicesHSV, img.getIndex())) {
                ColorStatProfile colorInfo = img.getColorCluster().getSchema(HSV).getColorStatProfile(MEAN);
                Scalar meanHSV = colorInfo.getMeanScalarHSV();
                hueList.put(img.getIndex(), meanHSV);
            }
        }
        return hueList;
    }
}
