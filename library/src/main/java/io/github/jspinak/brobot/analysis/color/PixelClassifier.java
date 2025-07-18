package io.github.jspinak.brobot.analysis.color;

import io.github.jspinak.brobot.model.analysis.color.PixelProfile;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;
import static io.github.jspinak.brobot.model.analysis.color.ColorInfo.ColorStat.MEAN;
import static io.github.jspinak.brobot.model.analysis.color.ColorSchema.ColorValue.HUE;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.INDICES_2D;
import static org.bytedeco.opencv.global.opencv_core.*;

/**
 * Performs pixel-level classification to assign scene regions to state images.
 * 
 * <p>PixelClassifier implements a multi-class classification system where each
 * pixel in a scene is assigned to the state image it most closely matches based
 * on color similarity. This enables sophisticated scene understanding and
 * state detection in GUI automation.</p>
 * 
 * <p>Classification approach:</p>
 * <ul>
 *   <li>Analyzes pixel similarities across multiple state images</li>
 *   <li>Selects best-matching state image for each pixel</li>
 *   <li>Operates independently on BGR and HSV color channels</li>
 *   <li>Generates index maps indicating pixel-to-image assignments</li>
 * </ul>
 * 
 * <p>The classification process produces several outputs:</p>
 * <ul>
 *   <li><b>3D indices</b>: Per-channel classification results</li>
 *   <li><b>2D indices</b>: Simplified classification (typically HSV hue channel)</li>
 *   <li><b>BGR visualization</b>: Color-coded classification results</li>
 * </ul>
 * 
 * <p>This component is essential for:</p>
 * <ul>
 *   <li>Multi-state detection in complex GUIs</li>
 *   <li>Dynamic content classification</li>
 *   <li>Scene segmentation based on learned patterns</li>
 * </ul>
 * 
 * @see SceneAnalysis
 * @see PixelProfiles
 * @see ColorMatrixUtilities
 */
@Component
public class PixelClassifier {

    private final ColorMatrixUtilities matOps3d;

    public PixelClassifier(ColorMatrixUtilities matOps3d) {
        this.matOps3d = matOps3d;
    }

    /**
     * Creates a comprehensive scene analysis with pixel classifications.
     * 
     * <p>Processes pixel analysis collections to generate classification
     * indices and visualization data. The analysis includes both 3D
     * (per-channel) and 2D (simplified) classification results.</p>
     * 
     * @param pixelAnalysisCollections analyses for each state image
     * @param scene the scene being classified
     * @return SceneAnalysis with classification indices and visualizations
     */
    public SceneAnalysis getSceneAnalysis(List<PixelProfiles> pixelAnalysisCollections, Scene scene) {
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
     * Generates pixel-wise classification indices for the specified color space.
     * 
     * <p>Creates a 3-channel matrix where each pixel value represents the
     * index of the best-matching state image. Different channels may have
     * different classifications, allowing for nuanced color-based matching.</p>
     * 
     * <p>The classification algorithm:
     * <ol>
     *   <li>Finds minimum scores across all k-means profiles per image</li>
     *   <li>Compares scores between different state images</li>
     *   <li>Assigns each pixel to the lowest-scoring (best match) image</li>
     * </ol>
     * </p>
     *
     * @param sceneAnalysis contains pixel analyses for all state images
     * @param colorSchemaName color space to use for classification (BGR or HSV)
     * @return 3D matrix with state image indices at each pixel position
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
            PixelProfiles pixelAnalysisCollection = sceneAnalysis.getPixelAnalysisCollection(i);
            StateImage img = pixelAnalysisCollection.getStateImage();
            int index = img.getIndex();
            Mat newScores = getMinScoresFromPixelAnalyses(pixelAnalysisCollection, colorSchemaName);
            matOps3d.minIndex(bestScoringIndices, bestScores, newScores, index);
        }
        return bestScoringIndices;
    }

    /**
     * Finds minimum scores across all color profiles for a state image.
     * 
     * <p>When using k-means clustering, each state image may have multiple
     * color profiles. This method finds the best (minimum) score across
     * all profiles, representing the closest match to any cluster center.</p>
     * 
     * <p>Side effects: Logs warnings if analyses are missing</p>
     * 
     * @param pixelAnalysisCollection analyses for one state image
     * @param colorSchemaName color space to process
     * @return matrix of minimum scores, or empty Mat if analysis unavailable
     */
    public Mat getMinScoresFromPixelAnalyses(PixelProfiles pixelAnalysisCollection,
                                             ColorCluster.ColorSchemaName colorSchemaName) {
        List<PixelProfile> pixelAnalyses = pixelAnalysisCollection.getPixelAnalyses();
        if (pixelAnalyses.isEmpty()) {
            ConsoleReporter.println("No pixel analyses for " + pixelAnalysisCollection.getStateImage().getName());
            return new Mat();
        }
        Mat minScores = pixelAnalyses.get(0).getAnalyses(PixelProfile.Analysis.SCORES, colorSchemaName);
        if (minScores == null) {
            ConsoleReporter.println("Scores is null for " + colorSchemaName +" "+pixelAnalysisCollection.getStateImage().getName());
            return new Mat();
        }
        for (int i = 1; i < pixelAnalyses.size(); i++) {
            Mat scores = pixelAnalyses.get(i).getAnalyses(PixelProfile.Analysis.SCORES, colorSchemaName);
            min(minScores, scores, minScores);
        }
        return minScores;
    }

    /**
     * Extracts 2D classification indices from HSV hue channel.
     * 
     * <p>Simplifies 3D classification to 2D by selecting the hue channel,
     * which often provides the most discriminative color information.
     * This 2D representation is used for visualization and simplified
     * analysis.</p>
     * 
     * <p>Note: Could be extended to use other channels or combinations
     * for more sophisticated classification strategies.</p>
     * 
     * @param sceneAnalysis the analysis to update with 2D indices
     */
    private void set2dIndicesHSV(SceneAnalysis sceneAnalysis) {
        Mat resultsHSV = sceneAnalysis.getAnalysis(ColorCluster.ColorSchemaName.HSV, SceneAnalysis.Analysis.SCENE);
        MatVector separateHSV = new MatVector();
        split(resultsHSV, separateHSV);
        sceneAnalysis.addAnalysis(ColorCluster.ColorSchemaName.HSV, INDICES_2D, separateHSV.get(0)); // H channel
    }

    /**
     * Creates a color visualization of classification results.
     * 
     * <p>Generates a BGR image where each pixel is colored according to
     * the mean color of its assigned state image. This creates an intuitive
     * visualization showing which regions of the scene match which state
     * images.</p>
     * 
     * <p>The visualization process:
     * <ol>
     *   <li>Read 2D classification indices</li>
     *   <li>For each unique index, create a mask</li>
     *   <li>Fill masked regions with corresponding mean colors</li>
     * </ol>
     * </p>
     *
     * @param sceneAnalysis provides classification indices and state images
     * @return BGR matrix with mean colors representing classifications
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
