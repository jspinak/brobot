package io.github.jspinak.brobot.action.basic.find.motion;

import java.util.List;
import java.util.Optional;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.history.draw.DrawMatch;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import io.github.jspinak.brobot.util.image.core.ColorMatrixUtilities;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;

/**
 * Creates visual representations of detected motion for debugging and analysis. This class overlays
 * motion information onto scene images, highlighting areas of movement with colored masks and
 * bounding boxes.
 *
 * <p>Visualization features include:
 *
 * <ul>
 *   <li>Colored overlay on pixels that have changed
 *   <li>Bounding boxes around motion regions
 *   <li>Saving illustrations to history for review
 *   <li>Customizable colors for different visualization needs
 * </ul>
 *
 * <p>The default motion color is a light orange (254, 183, 146), which provides good visibility
 * against most backgrounds.
 *
 * @see DrawMatch
 * @see MatrixVisualizer
 * @see SceneAnalyses
 * @see Visualization
 */
@Component
public class VisualizeMotion {

    private final DrawMatch drawMatch;
    private final MatrixVisualizer matVisualize;
    private final ColorMatrixUtilities matOps3d;

    /**
     * Constructs an IllustrateMotion instance with required visualization utilities.
     *
     * @param drawMatch utility for drawing match bounding boxes
     * @param matVisualize utility for saving images to history
     * @param matOps3d utility for matrix operations including color overlays
     */
    public VisualizeMotion(
            DrawMatch drawMatch, MatrixVisualizer matVisualize, ColorMatrixUtilities matOps3d) {
        this.drawMatch = drawMatch;
        this.matVisualize = matVisualize;
        this.matOps3d = matOps3d;
    }

    /**
     * Sets the motion mask for a scene and creates a visual illustration. The illustration shows
     * the scene with motion areas highlighted and is saved to history for debugging purposes.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Sets the motion mask in the scene's illustrations
     *   <li>Writes the motion visualization to history as "motionWithMatches"
     * </ul>
     *
     * @param sceneAnalysisCollection collection containing scenes and motion data
     * @param index index of the scene to illustrate
     * @param motionMask binary mask indicating motion areas
     */
    public void setMotionMatAndWriteIllustration(
            SceneAnalyses sceneAnalysisCollection, int index, Mat motionMask) {
        Visualization illustrations =
                sceneAnalysisCollection.getSceneAnalyses().get(index).getIllustrations();
        illustrations.setMotion(motionMask);
        Mat motionWithMatches =
                getSceneWithMotion(sceneAnalysisCollection, new Scalar(254, 183, 146, 0));
        matVisualize.writeMatToHistory(motionWithMatches, "motionWithMatches");
    }

    /**
     * Creates a visualization of the last scene with motion overlay. Uses the motion mask and
     * contours from the scene analysis collection to highlight areas of movement.
     *
     * @param sceneAnalysisCollection collection containing scenes and motion analysis
     * @param color the color to use for motion highlighting
     * @return Mat showing the scene with motion areas highlighted, or empty Mat if no scenes
     */
    public Mat getSceneWithMotion(SceneAnalyses sceneAnalysisCollection, Scalar color) {
        Optional<SceneAnalysis> optLastScene = sceneAnalysisCollection.getLastSceneAnalysis();
        if (optLastScene.isEmpty()) return new Mat();
        Mat sceneMat = optLastScene.get().getScene().getPattern().getImage().getMatBGR();
        List<Match> dynamicPixelMatches = sceneAnalysisCollection.getContours().getMatchList();
        return getSceneWithMotion(
                sceneMat, sceneAnalysisCollection.getResults(), dynamicPixelMatches, color);
    }

    /**
     * Creates a visual representation of motion on a scene image. Overlays a colored mask on motion
     * areas and draws bounding boxes around detected motion regions.
     *
     * @param scene the original scene image
     * @param motionMask binary mask indicating pixels with motion
     * @param motionBoundingBoxes list of {@link Match} objects defining motion regions
     * @param color the color to use for both mask overlay and bounding boxes
     * @return new Mat with motion visualization overlaid on the scene
     */
    public Mat getSceneWithMotion(
            Mat scene, Mat motionMask, List<Match> motionBoundingBoxes, Scalar color) {
        Mat baseImage = scene.clone();
        matOps3d.addColorToMat(baseImage, motionMask, color);
        drawMatch.drawMatches(baseImage, motionBoundingBoxes, color);
        return baseImage;
    }
}
