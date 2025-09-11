package io.github.jspinak.brobot.tools.history.draw;

import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static org.bytedeco.opencv.global.opencv_core.hconcat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.tools.history.HistoryFileNamer;
import io.github.jspinak.brobot.tools.history.visual.ClassificationLegend;

/**
 * Visualizes classification results with color-coded regions and legends.
 *
 * <p>DrawClassifications creates comprehensive visualizations of image classification results,
 * where different regions are painted with colors representing their assigned classes. Each
 * visualization includes the classified image alongside a legend explaining the color-to-class
 * mappings, making the results easy to interpret.
 *
 * <p><b>Visual Output Structure:</b>
 *
 * <ul>
 *   <li>Left side: Original image with regions colored by classification
 *   <li>Right side: Legend showing color-to-class mappings
 *   <li>BGR color space for all visualizations
 *   <li>Horizontal concatenation of image and legend
 * </ul>
 *
 * <p><b>Configuration Parameters:</b>
 *
 * <ul>
 *   <li>Color space: BGR (fixed)
 *   <li>Analysis type: BGR_FROM_INDICES_2D
 *   <li>Layout: Horizontal concatenation
 *   <li>File format: Determined by {@link HistoryFileNamer}
 * </ul>
 *
 * <p><b>Use Cases:</b>
 *
 * <ul>
 *   <li>Visualizing results of CLASSIFY actions
 *   <li>Creating visual reports of scene segmentation
 *   <li>Debugging classification algorithms
 *   <li>Generating documentation of classification results
 *   <li>Comparing classification outputs across different images
 * </ul>
 *
 * <p><b>Relationships:</b>
 *
 * <ul>
 *   <li>Works with {@link SceneAnalysis} for classification data
 *   <li>Uses {@link ClassificationLegend} to generate color legends
 *   <li>Integrates with {@link HistoryFileNamer} for file naming
 *   <li>Processes results from CLASSIFY {@link ActionType}
 *   <li>Complements {@link DrawColorProfile} and {@link DrawClassesLegend}
 * </ul>
 *
 * @see SceneAnalysis
 * @see ClassificationLegend
 * @see HistoryFileNamer
 * @see ActionType#CLASSIFY
 * @see DrawColorProfile
 * @see DrawClassesLegend
 */
@Component
public class DrawClassifications {

    private HistoryFileNamer illustrationFilename;
    private ClassificationLegend classificationLegend;

    public DrawClassifications(
            HistoryFileNamer illustrationFilename, ClassificationLegend classificationLegend) {
        this.illustrationFilename = illustrationFilename;
        this.classificationLegend = classificationLegend;
    }

    /**
     * Creates and saves classification visualizations for all scene analyses.
     *
     * <p>Processes classification results from a CLASSIFY action and generates visual output files.
     * Each scene analysis gets its own output file with the classified image and accompanying
     * legend.
     *
     * <p>The method handles:
     *
     * <ul>
     *   <li>Iterating through all scene analyses in the collection
     *   <li>Generating unique filenames for each visualization
     *   <li>Creating combined image-legend visualizations
     *   <li>Saving results to the appropriate directory
     * </ul>
     *
     * @param matches ActionResult containing SceneAnalysisCollection with classification data
     * @param actionConfig configuration options affecting filename generation
     */
    public void paintClasses(ActionResult matches, ActionConfig actionConfig) {
        // CLASSIFY produces only one SceneAnalysisCollection, so we can use the first one
        matches.getSceneAnalysisCollection()
                .getSceneAnalyses()
                .forEach(
                        sceneAnalysis -> {
                            String outputPath =
                                    illustrationFilename.getFilenameFromMatchObjects(
                                            matches, actionConfig);
                            writeImage(outputPath, sceneAnalysis);
                        });
    }

    /**
     * Generates and writes a complete classification visualization.
     *
     * <p>Creates a composite image containing:
     *
     * <ol>
     *   <li>The classified image with colored regions
     *   <li>A legend mapping colors to class names
     * </ol>
     *
     * <p>The visualization uses BGR color space and the BGR_FROM_INDICES_2D analysis type, which
     * provides pixel-by-pixel classification results rendered as colors.
     *
     * @param file output filename path for the visualization
     * @param sceneAnalysis analysis results containing classification data
     */
    private void writeImage(String file, SceneAnalysis sceneAnalysis) {
        Mat legend =
                classificationLegend.draw(
                        sceneAnalysis.getAnalysis(
                                ColorCluster.ColorSchemaName.BGR, BGR_FROM_INDICES_2D),
                        sceneAnalysis);
        Mat fused =
                fuseScreenAndLegend(
                        sceneAnalysis.getAnalysis(
                                ColorCluster.ColorSchemaName.BGR, BGR_FROM_INDICES_2D),
                        legend);
        imwrite(file, fused);
    }

    /**
     * Horizontally concatenates the classified image with its legend.
     *
     * <p>Creates a single visualization by placing the legend to the right of the classified image.
     * This side-by-side layout allows viewers to easily reference the color meanings while
     * examining the classification results.
     *
     * @param screen the classified image with colored regions
     * @param legend the color-to-class mapping legend
     * @return combined Mat with screen on left, legend on right
     */
    private Mat fuseScreenAndLegend(Mat screen, Mat legend) {
        MatVector concatMats = new MatVector(screen, legend);
        Mat result = new Mat();
        hconcat(concatMats, result);
        return result;
    }
}
