package io.github.jspinak.brobot.model.analysis.color;

import io.github.jspinak.brobot.action.internal.find.pixel.PixelScoreCalculator;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import io.github.jspinak.brobot.util.image.core.MatrixUtilities;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;

/**
 * Aggregates multiple pixel analyses for comprehensive scene-image matching.
 * 
 * <p>PixelProfiles manages the complete set of color analyses performed
 * when matching a scene against a StateImage. It combines individual {@link PixelProfile}
 * results (e.g., from multiple k-means clusters) and provides aggregate scoring and
 * analysis capabilities.</p>
 * 
 * <p>Key components:
 * <ul>
 *   <li>Multiple PixelAnalysis objects (one per color profile/cluster)</li>
 *   <li>Combined scoring matrices across all analyses</li>
 *   <li>Scene images in both BGR and HSV formats</li>
 *   <li>Threshold-based filtering results</li>
 * </ul>
 * </p>
 * 
 * <p>Typical workflow:
 * <ol>
 *   <li>Initialize with a Scene containing the image to analyze</li>
 *   <li>Add PixelAnalysis results for each color profile</li>
 *   <li>Generate combined scores using GetPixelScores</li>
 *   <li>Apply thresholds for final matching decisions</li>
 * </ol>
 * </p>
 * 
 * <p>This collection enables sophisticated matching strategies that consider
 * multiple color characteristics simultaneously, improving robustness over
 * single-profile matching.</p>
 * 
 * @see PixelProfile
 * @see PixelScoreCalculator
 * @see Scene
 * @see StateImage
 */
@Getter
public class PixelProfiles {

    /**
     * Types of aggregate analysis data stored in the collection.
     * 
     * <ul>
     *   <li><b>SCENE</b>: Original scene images (BGR and HSV)</li>
     *   <li><b>SCORE</b>: Combined scores from all PixelAnalysis objects</li>
     *   <li><b>SCORE_DIST_BELOW_THRESHHOLD</b>: Distance below score threshold</li>
     * </ul>
     * 
     * <p>Note: THRESHHOLD is intentionally kept with original spelling for compatibility</p>
     */
    public enum Analysis {
        SCENE, SCORE, SCORE_DIST_BELOW_THRESHHOLD
    }

    private List<PixelProfile> pixelAnalyses = new ArrayList<>();
    @Setter
    private StateImage stateImage;

    private Map<ColorCluster.ColorSchemaName, Map<Analysis, Mat>> analyses = new HashMap<>();
    {
        analyses.put(BGR, new HashMap<>());
        analyses.put(HSV, new HashMap<>());
    }

    /**
     * Creates a new collection initialized with scene images.
     * 
     * <p>Extracts BGR and HSV representations from the scene's pattern image
     * and stores them for analysis. These serve as the base images for all
     * subsequent color matching operations.</p>
     * 
     * @param scene the scene containing the image to analyze
     */
    public PixelProfiles(Scene scene) {
        analyses.get(BGR).put(Analysis.SCENE, scene.getPattern().getImage().getMatBGR());
        analyses.get(HSV).put(Analysis.SCENE, scene.getPattern().getImage().getMatHSV());
    }

    /**
     * Adds a PixelAnalysis result to the collection.
     * 
     * <p>Each PixelAnalysis typically represents matching results for one
     * color profile (e.g., one k-means cluster center).</p>
     * 
     * @param pixelAnalysis the analysis results to add
     */
    public void add(PixelProfile pixelAnalysis) {
        pixelAnalyses.add(pixelAnalysis);
    }

    /**
     * Retrieves an aggregate analysis matrix.
     * 
     * @param analysis the type of analysis to retrieve
     * @param colorSchemaName the color space (BGR or HSV)
     * @return the analysis Mat, or null if not computed
     */
    public Mat getAnalysis(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName) {
        return this.analyses.get(colorSchemaName).get(analysis);
    }

    /**
     * Stores an aggregate analysis matrix.
     * 
     * <p>Typically used to store combined scores or threshold results
     * after processing all individual PixelAnalysis objects.</p>
     * 
     * @param analysis the type of analysis being stored
     * @param colorSchemaName the color space of the analysis
     * @param mat the analysis results to store
     */
    public void setAnalyses(Analysis analysis, ColorCluster.ColorSchemaName colorSchemaName, Mat mat) {
        this.analyses.get(colorSchemaName).put(analysis, mat);
    }

    /**
     * Returns the name of the associated StateImage.
     * 
     * @return the StateImage name, or null if not set
     */
    public String getImageName() {
        return stateImage.getName();
    }

    /**
     * Prints comprehensive analysis information.
     * 
     * <p>Outputs the number of individual analyses, details from each
     * PixelAnalysis, and dimensions of all aggregate matrices. Useful
     * for debugging the complete analysis pipeline.</p>
     * 
     * <p>Side effects: Outputs to the Report logging system</p>
     */
    public void print() {
        ConsoleReporter.println("\nPixelProfiles");
        ConsoleReporter.println("Size of collection: " + pixelAnalyses.size());
        pixelAnalyses.forEach(PixelProfile::print);
        MatrixUtilities.printDimensions(getAnalysis(Analysis.SCENE, BGR), "Scene BGR");
        MatrixUtilities.printDimensions(getAnalysis(Analysis.SCORE, BGR), "Score BGR");
        MatrixUtilities.printDimensions(getAnalysis(Analysis.SCORE, BGR), "scoresBGR");
        MatrixUtilities.printDimensions(getAnalysis(Analysis.SCORE, HSV), "scoresHSV");
        MatrixUtilities.printDimensions(getAnalysis(Analysis.SCORE_DIST_BELOW_THRESHHOLD, BGR), "scoreDistanceBelowThresholdBGR");
        MatrixUtilities.printDimensions(getAnalysis(Analysis.SCORE_DIST_BELOW_THRESHHOLD, HSV), "scoreDistanceBelowThresholdHSV");
    }

}
