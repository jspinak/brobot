package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.util.string.StringSimilarity;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.MEAN;
import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.HUE;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.INDICES_2D;
import static org.bytedeco.opencv.global.opencv_core.*;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;

/**
 * DynamicImages classify as probabilities, standard Images have 100% pixel probability when found.
 * The sparse matrix holds probabilities for each StateImage. The screen Mat should be in hsv
 * format.
 */
@Component
public class ColorClassifier {

    private final ColorMatrixUtilities matOps3d;

    public ColorClassifier(ColorMatrixUtilities matOps3d) {
        this.matOps3d = matOps3d;
    }

    public SceneAnalysis getSceneAnalysis(
            List<PixelProfiles> pixelAnalysisCollections, Scene scene) {
        SceneAnalysis sceneAnalysis = new SceneAnalysis(pixelAnalysisCollections, scene);
        Mat indices3dBGR = getImageIndices(sceneAnalysis, BGR);
        Mat indices3dHSV = getImageIndices(sceneAnalysis, HSV);
        sceneAnalysis.addAnalysis(BGR, SceneAnalysis.Analysis.INDICES_3D, indices3dBGR);
        sceneAnalysis.addAnalysis(HSV, SceneAnalysis.Analysis.INDICES_3D, indices3dHSV);
        set2dIndicesHSV(sceneAnalysis);
        Mat bgrFromIndices2d = convertIndicesToBGRmeans(sceneAnalysis);
        sceneAnalysis.addAnalysis(BGR, BGR_FROM_INDICES_2D, bgrFromIndices2d);
        return sceneAnalysis;
    }

    /**
     * Returns a 3d Mat representing the best Image match (or no match) for every cell. (x,y) on
     * different channels can have different matches. Matches are represented by the index of the
     * StateImage. The closest distance to any of the k-means profiles is compared to a threshold
     * adjusted by the minScore in ActionConfig.
     *
     * @param sceneAnalysis the analysis groups contain the StateImage and its k-means profiles.
     * @param colorSchemaName the color schema to use (i.e. BGR, HSV)
     */
    public Mat getImageIndices(
            SceneAnalysis sceneAnalysis, ColorCluster.ColorSchemaName colorSchemaName) {
        int collectionSize = sceneAnalysis.size();
        if (collectionSize == 0) {
            return new Mat();
        }
        Mat bestScores =
                getMinScoresFromPixelAnalyses(
                        sceneAnalysis.getPixelAnalysisCollection(0), colorSchemaName);
        List<Integer> hues = sceneAnalysis.getColorValues(HSV, MEAN, HUE);
        List<Scalar> scalars = new ArrayList<>();
        hues.forEach(color -> scalars.add(new Scalar(color, 255, 255, 0)));
        Mat bestScoringIndices = new Mat(bestScores.size(), bestScores.type(), new Scalar(0));
        for (int i = 0; i < collectionSize; i++) {
            PixelProfiles pixelAnalysisCollection = sceneAnalysis.getPixelAnalysisCollection(i);
            StateImage img = pixelAnalysisCollection.getStateImage();
            int index = img.getIndex();
            Mat newScores = getMinScoresFromPixelAnalyses(pixelAnalysisCollection, colorSchemaName);
            matOps3d.minIndex(bestScoringIndices, bestScores, newScores, index);
        }
        return bestScoringIndices;
    }

    public Mat getMinScoresFromPixelAnalyses(
            PixelProfiles pixelAnalysisCollection, ColorCluster.ColorSchemaName colorSchemaName) {
        List<PixelProfile> pixelAnalyses = pixelAnalysisCollection.getPixelAnalyses();
        if (pixelAnalyses.isEmpty()) {
            return new Mat();
        }
        Mat minScores =
                pixelAnalyses.get(0).getAnalyses(PixelProfile.Analysis.SCORES, colorSchemaName);
        if (minScores == null) {
            return new Mat();
        }
        for (int i = 1; i < pixelAnalyses.size(); i++) {
            Mat scores =
                    pixelAnalyses.get(i).getAnalyses(PixelProfile.Analysis.SCORES, colorSchemaName);
            min(minScores, scores, minScores);
        }
        return minScores;
    }

    /*
    Sets the collection results to the 2d H Mat of the HSV results.
    This can be more sophisticated if needed. We have 6 channels (B,G,R,H,S,V) to play with.
     */
    private void set2dIndicesHSV(SceneAnalysis sceneAnalysis) {
        Mat resultsHSV =
                sceneAnalysis.getAnalysis(
                        ColorCluster.ColorSchemaName.HSV, SceneAnalysis.Analysis.SCENE);
        MatVector separateHSV = new MatVector();
        split(resultsHSV, separateHSV);
        sceneAnalysis.addAnalysis(
                ColorCluster.ColorSchemaName.HSV, INDICES_2D, separateHSV.get(0)); // H channel
    }

    /**
     * From the 2d indices, we can get the BGR means for each pixel. The BGR means are the images'
     * color profile means for the best matches. These colors are used to identify the image that
     * was selected for classification when illustrating the action. Each PixelAnalysisCollection
     * has the pixel scores for a StateImage-Scene pair. The collection of PixelAnalysisCollections
     * here is for a single Scene.
     *
     * @param sceneAnalysis provides the 2d indices and the StateImages.
     * @return a 3d Mat of the BGR means for each pixel
     */
    private Mat convertIndicesToBGRmeans(SceneAnalysis sceneAnalysis) {
        Mat indices = sceneAnalysis.getAnalysis(ColorCluster.ColorSchemaName.HSV, INDICES_2D);
        Mat meanColorMat = new Mat(indices.size());
        for (PixelProfiles pixelAnalysisCollection : sceneAnalysis.getPixelAnalysisCollections()) {
            int index = pixelAnalysisCollection.getStateImage().getIndex();
            Mat mask = new Mat(indices.size());
            Mat allIndex = new Mat(indices.size(), indices.type(), new Scalar(index));
            compare(indices, allIndex, mask, CMP_EQ);
            ColorCluster colorCluster = pixelAnalysisCollection.getStateImage().getColorCluster();
            Mat meanColor = colorCluster.getMat(BGR, MEAN, indices.size());
            meanColor.copyTo(meanColorMat, mask);
        }
        return meanColorMat;
    }
}
