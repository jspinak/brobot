package io.github.jspinak.brobot.actions.methods.basicactions.find.color.classification;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.PixelAnalysisCollection;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.imageUtils.MatOps3d;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis.Analysis.INDICES_2D;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorInfo.ColorStat.MEAN;
import static io.github.jspinak.brobot.actions.methods.basicactions.find.color.profiles.ColorSchema.ColorValue.HUE;
import static org.bytedeco.opencv.global.opencv_core.*;

/**
 * DynamicImages classify as probabilities, standard Images have 100% pixel probability when found.
 * The sparse matrix holds probabilities for each StateImage.
 * The screen Mat should be in hsv format.
 */
@Component
public class ClassifyPixels {

    private final MatOps3d matOps3d;

    public ClassifyPixels(MatOps3d matOps3d) {
        this.matOps3d = matOps3d;
    }

    public SceneAnalysis getSceneAnalysis(List<PixelAnalysisCollection> pixelAnalysisCollections, Image scene) {
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
     * Returns a 3d Mat representing the best Image match (or no match) for every cell.
     * (x,y) on different channels can have different matches.
     * Matches are represented by the index of the StateImage.
     * The closest distance to any of the k-means profiles is compared to a threshold adjusted by the minScore in ActionOptions.
     *
     * @param sceneAnalysis the analysis groups contain the StateImage and its k-means profiles.
     * @param colorSchemaName the color schema to use (i.e. BGR, HSV)
     */
    public Mat getImageIndices(SceneAnalysis sceneAnalysis, ColorCluster.ColorSchemaName colorSchemaName) {
        int collectionSize = sceneAnalysis.size();
        if (collectionSize == 0) {
            return new Mat();
        }
        Mat bestScores = getMinScoresFromPixelAnalyses(sceneAnalysis.getPixelAnalysisCollection(0), colorSchemaName);
        List<Integer> hues = sceneAnalysis.getColorValues(HSV, MEAN, HUE);
        List<Scalar> scalars = new ArrayList<>();
        hues.forEach(color -> scalars.add(new Scalar(color, 255, 255, 0)));
        Mat bestScoringIndices = new Mat(bestScores.size(), bestScores.type(), new Scalar(0));
        for (int i = 0; i < collectionSize; i++) {
            PixelAnalysisCollection pixelAnalysisCollection = sceneAnalysis.getPixelAnalysisCollection(i);
            StateImage img = pixelAnalysisCollection.getStateImage();
            int index = img.getIndex();
            Mat newScores = getMinScoresFromPixelAnalyses(pixelAnalysisCollection, colorSchemaName);
            matOps3d.minIndex(bestScoringIndices, bestScores, newScores, index);
        }
        return bestScoringIndices;
    }

    public Mat getMinScoresFromPixelAnalyses(PixelAnalysisCollection pixelAnalysisCollection,
                                             ColorCluster.ColorSchemaName colorSchemaName) {
        List<PixelAnalysis> pixelAnalyses = pixelAnalysisCollection.getPixelAnalyses();
        if (pixelAnalyses.isEmpty()) {
            Report.println("No pixel analyses for " + pixelAnalysisCollection.getStateImage().getName());
            return new Mat();
        }
        Mat minScores = pixelAnalyses.get(0).getAnalyses(PixelAnalysis.Analysis.SCORES, colorSchemaName);
        if (minScores == null) {
            Report.println("Scores is null for " + colorSchemaName +" "+pixelAnalysisCollection.getStateImage().getName());
            return new Mat();
        }
        for (int i = 1; i < pixelAnalyses.size(); i++) {
            Mat scores = pixelAnalyses.get(i).getAnalyses(PixelAnalysis.Analysis.SCORES, colorSchemaName);
            min(minScores, scores, minScores);
        }
        return minScores;
    }

    /*
    Sets the collection results to the 2d H Mat of the HSV results.
    This can be more sophisticated if needed. We have 6 channels (B,G,R,H,S,V) to play with.
     */
    private void set2dIndicesHSV(SceneAnalysis sceneAnalysis) {
        Mat resultsHSV = sceneAnalysis.getAnalysis(ColorCluster.ColorSchemaName.HSV, SceneAnalysis.Analysis.SCENE);
        MatVector separateHSV = new MatVector();
        split(resultsHSV, separateHSV);
        sceneAnalysis.addAnalysis(ColorCluster.ColorSchemaName.HSV, INDICES_2D, separateHSV.get(0)); // H channel
    }

    /**
     * From the 2d indices, we can get the BGR means for each pixel.
     * The BGR means are the images' color profile means for the best matches.
     * These colors are used to identify the image that was selected for classification when illustrating the action.
     * Each PixelAnalysisCollection has the pixel scores for a StateImage-Scene pair.
     * The collection of PixelAnalysisCollections here is for a single Scene.
     *
     * @param sceneAnalysis provides the 2d indices and the StateImages.
     * @return a 3d Mat of the BGR means for each pixel
     */
    private Mat convertIndicesToBGRmeans(SceneAnalysis sceneAnalysis) {
        Mat indices = sceneAnalysis.getAnalysis(ColorCluster.ColorSchemaName.HSV, INDICES_2D);
        Mat meanColorMat = new Mat(indices.size());
        for (PixelAnalysisCollection pixelAnalysisCollection : sceneAnalysis.getPixelAnalysisCollections()) {
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
