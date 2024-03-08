package io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatOps;
import io.github.jspinak.brobot.reports.Report;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection.Analysis.*;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;

/**
 * Contains a series of PixelAnalysis objects that comprise all analysis of a {scene, StateImage} pair.
 * Each PixelAnalysis object could correspond to the analysis of a k-Means cluster (if k-Means is 3, there would
 * be 3 clusters for the StateImage, and each of these clusters would engender a PixelAnalysis).
 */
@Getter
public class PixelAnalysisCollection {

    public enum Analysis {
        SCENE, SCORE, SCORE_DIST_BELOW_THRESHHOLD
    }
    /**
     SCORE: subjective in that there are different methods that can produce the pixelScores matrix.
     The default method is found in the class GetPixelScores, which takes the per-cell minimum of all Mats.
     SCORE_DIST_BELOW_THRESHHOLD: The distance below a threshold.
     */

    private List<PixelAnalysis> pixelAnalyses = new ArrayList<>();
    @Setter
    private StateImage stateImage;

    private Map<ColorCluster.ColorSchemaName, Map<Analysis, Mat>> analyses = new HashMap<>();
    {
        analyses.put(BGR, new HashMap<>());
        analyses.put(HSV, new HashMap<>());
    }

    public PixelAnalysisCollection(Image scene) {
        analyses.get(BGR).put(SCENE, scene.getMatBGR());
        analyses.get(HSV).put(SCENE, scene.getMatHSV());
    }

    public void add(PixelAnalysis pixelAnalysis) {
        pixelAnalyses.add(pixelAnalysis);
    }

    public Mat getAnalysis(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName) {
        return this.analyses.get(colorSchemaName).get(analysis);
    }

    public void setAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName, Mat mat) {
        this.analyses.get(colorSchemaName).put(analysis, mat);
    }

    public String getImageName() {
        return stateImage.getName();
    }

    public void print() {
        Report.println("\nPixelAnalysisCollection");
        Report.println("Size of collection: " + pixelAnalyses.size());
        pixelAnalyses.forEach(PixelAnalysis::print);
        MatOps.printDimensions(getAnalysis(SCENE, BGR), "Scene BGR");
        MatOps.printDimensions(getAnalysis(SCORE, BGR), "Score BGR");
        MatOps.printDimensions(getAnalysis(SCORE, BGR), "scoresBGR");
        MatOps.printDimensions(getAnalysis(SCORE, HSV), "scoresHSV");
        MatOps.printDimensions(getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, BGR), "scoreDistanceBelowThresholdBGR");
        MatOps.printDimensions(getAnalysis(SCORE_DIST_BELOW_THRESHHOLD, HSV), "scoreDistanceBelowThresholdHSV");
    }

}
