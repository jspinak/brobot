package io.github.jspinak.brobot.model.analysis.scene;

import io.github.jspinak.brobot.model.analysis.color.ColorCluster;
import io.github.jspinak.brobot.model.analysis.color.ColorInfo;
import io.github.jspinak.brobot.model.analysis.color.ColorSchema;
import io.github.jspinak.brobot.model.analysis.color.ColorStatistics;
import io.github.jspinak.brobot.model.analysis.color.PixelProfiles;
import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import lombok.Getter;
import lombok.Setter;

import org.bytedeco.opencv.opencv_core.Mat;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.model.analysis.color.PixelProfiles.Analysis.SCORE;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.SCENE;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.HSV;

/**
 * Comprehensive analysis results for a single scene with multiple state images.
 * 
 * <p>SceneAnalysis aggregates all pixel-level analyses for a scene, organizing
 * results by state image and color space. It serves as the central data structure
 * for color-based matching operations, containing classification indices, scores,
 * visualizations, and extracted matches.</p>
 * 
 * <p>Key components:</p>
 * <ul>
 *   <li><b>PixelAnalysisCollections</b>: One per state image, containing detailed pixel scores</li>
 *   <li><b>Classification indices</b>: Which state image best matches each pixel</li>
 *   <li><b>Visualizations</b>: BGR representations for display and debugging</li>
 *   <li><b>Contours</b>: Extracted regions from classification results</li>
 *   <li><b>Matches</b>: Concrete match objects for found patterns</li>
 * </ul>
 * 
 * <p>Analysis types stored:</p>
 * <ul>
 *   <li><b>SCENE</b>: Original scene in BGR and HSV formats</li>
 *   <li><b>INDICES_3D</b>: Per-channel classification results</li>
 *   <li><b>INDICES_3D_TARGETS</b>: Filtered to target images only</li>
 *   <li><b>INDICES_2D</b>: Flattened classification (HSV H-channel)</li>
 *   <li><b>BGR_FROM_INDICES_2D</b>: Colored visualization of results</li>
 *   <li><b>BGR_FROM_INDICES_2D_TARGETS</b>: Visualization of targets only</li>
 * </ul>
 * 
 * @see PixelProfiles
 * @see Scene
 * @see ContourExtractor
 * @see Visualization
 */
@Getter
@Setter
public class SceneAnalysis {

    public enum Analysis {
        SCENE, INDICES_3D, INDICES_3D_TARGETS, INDICES_2D, BGR_FROM_INDICES_2D, BGR_FROM_INDICES_2D_TARGETS
    }
    /*
     SCENE is a 3d Mat of the scene (BGR, HSV).
     INDICES_3D mats have the corresponding indices for each cell in each channel (i.e. the value at (0,0) for HUE
        can be a different index than that at (0,0) for SATURATION).
     INDICES_3D_TARGETS is the same as INDICES_3D, but only has data for the target images in the scene (any cell that was
        classified as one of the additional images is here shown as 'no match').
     INDICES_2D are results Mats containing the selected class indices for each pixel. The selected indices
        are chosen from the INDICES_3D Mat. The HSV format is used as default for this analysis,
        since it is easy and effective to take the H channel and use it as the index results per pixel.
     BGR_FROM_INDICES_2D holds 3d BGR Mats used for writing to file or showing results on-screen (must be in BGR format).
        Each pixel is the mean color of the corresponding class (represented by the image index of the 2d results Mat).
     */

    private List<PixelProfiles> pixelAnalysisCollections;
    private Scene scene;
    private ContourExtractor contours;
    // these matches are scene specific. for example, the matches from motion from the previous scene to this one.
    private List<Match> matchList = new ArrayList<>();
    private Visualization illustrations; // the results in a writeable format
    private Map<ColorCluster.ColorSchemaName, Map<Analysis, Mat>> analysis = new HashMap<>();
    {
        analysis.put(BGR, new HashMap<>());
        analysis.put(HSV, new HashMap<>());
    }

    public SceneAnalysis(List<PixelProfiles> pixelAnalysisCollections, Scene scene) {
        this.pixelAnalysisCollections = pixelAnalysisCollections;
        this.scene = scene;
        addAnalysis(BGR, SCENE, scene.getPattern().getImage().getMatBGR());
        addAnalysis(HSV, SCENE, scene.getPattern().getImage().getMatHSV());
        illustrations = new Visualization();
        illustrations.setScene(scene.getPattern().getImage().getMatBGR());
    }

    /**
     * Creates a minimal scene analysis without pixel-level color analysis.
     * 
     * <p>This constructor is used when color analysis is not required,
     * typically for non-color-based operations. The scene is stored
     * in both BGR and HSV formats for potential visualization, but
     * no pixel analysis collections are created.</p>
     * 
     * <p>Side effects: Initializes illustrations with the scene image</p>
     * 
     * @param scene the scene to analyze
     */
    public SceneAnalysis(Scene scene) {
        pixelAnalysisCollections = new ArrayList<>();
        this.scene = scene;
        addAnalysis(BGR, SCENE, scene.getPattern().getImage().getMatBGR());
        addAnalysis(HSV, SCENE, scene.getPattern().getImage().getMatHSV());
        illustrations = new Visualization();
        illustrations.setScene(scene.getPattern().getImage().getMatBGR());
    }

    /**
     * Adds an analysis result matrix for a specific color space and analysis type.
     * 
     * <p>Stores the provided matrix in the internal analysis maps organized
     * by color space and analysis type. When adding BGR visualization data,
     * also updates the illustrations for display purposes.</p>
     * 
     * <p>Side effects: Updates illustrations if analysis type is BGR_FROM_INDICES_2D</p>
     * 
     * @param colorSchemaName the color space (BGR or HSV)
     * @param analysis the type of analysis data
     * @param mat the analysis result matrix
     */
    public void addAnalysis(ColorCluster.ColorSchemaName colorSchemaName, Analysis analysis, Mat mat) {
        this.analysis.get(colorSchemaName).put(analysis, mat);
        if (analysis == BGR_FROM_INDICES_2D) illustrations.setClasses(mat);
    }

    /**
     * Retrieves a specific analysis result matrix.
     * 
     * @param colorSchemaName the color space of the analysis
     * @param analysis the type of analysis to retrieve
     * @return the analysis matrix, or null if not found
     */
    public Mat getAnalysis(ColorCluster.ColorSchemaName colorSchemaName, Analysis analysis) {
        return this.analysis.get(colorSchemaName).get(analysis);
    }

    /**
     * Extracts all state images from the pixel analysis collections.
     * 
     * @return list of state images being analyzed in this scene
     */
    public List<StateImage> getStateImageObjects() {
        return pixelAnalysisCollections.stream().map(PixelProfiles::getStateImage).collect(Collectors.toList());
    }

    /**
     * Concatenates all state image names for display purposes.
     * 
     * @return concatenated string of all image names
     */
    public String getImageNames() {
        return pixelAnalysisCollections.stream().map(PixelProfiles::getImageName).collect(Collectors.joining(""));
    }

    /**
     * Returns the number of state images being analyzed.
     * 
     * @return count of pixel analysis collections
     */
    public int size() {
        return pixelAnalysisCollections.size();
    }

    /**
     * Retrieves a specific pixel analysis collection by index.
     * 
     * @param index the index of the collection to retrieve
     * @return the pixel analysis collection at the specified index
     */
    public PixelProfiles getPixelAnalysisCollection(int index) {
        return pixelAnalysisCollections.get(index);
    }

    /**
     * Finds the highest state image index in this analysis.
     * 
     * @return maximum state image index, or 0 if no collections exist
     */
    public int getLastIndex() {
        OptionalInt maxOpt = pixelAnalysisCollections.stream().mapToInt(pic -> pic.getStateImage().getIndex()).max();
        return maxOpt.isPresent() ? maxOpt.getAsInt() : 0;
    }

    /**
     * Collects color statistics profiles for all state images.
     * 
     * <p>Retrieves the specified color statistic (mean, stddev, etc.) profiles
     * from each state image's color cluster for the given color space.</p>
     * 
     * @param colorSchemaName the color space (BGR or HSV)
     * @param colorStat the type of statistic to retrieve
     * @return list of color stat profiles from all state images
     */
    public List<ColorStatistics> getColorStatProfiles(ColorCluster.ColorSchemaName colorSchemaName,
                                                       ColorInfo.ColorStat colorStat) {
        List<ColorStatistics> colorStatProfiles = new ArrayList<>();
        for (PixelProfiles pixelAnalysisCollection : pixelAnalysisCollections) {
            ColorCluster colorCluster = pixelAnalysisCollection.getStateImage().getColorCluster();
            ColorStatistics colorStatProfile = colorCluster.getSchema(colorSchemaName).getColorStatistics(colorStat);
            colorStatProfiles.add(colorStatProfile);
        }
        return colorStatProfiles;
    }

    /**
     * Extracts specific color channel values from all state images.
     * 
     * <p>Retrieves a single color channel value (e.g., HUE) from the specified
     * statistic type for all state images, useful for visualization and debugging.</p>
     * 
     * @param colorSchemaName the color space (BGR or HSV)
     * @param colorStat the statistic type (MEAN, STDDEV, etc.)
     * @param colorValue the specific channel to extract
     * @return list of integer color values from all state images
     */
    public List<Integer> getColorValues(ColorCluster.ColorSchemaName colorSchemaName, ColorInfo.ColorStat colorStat,
                                        ColorSchema.ColorValue colorValue) {
        List<ColorStatistics> colorStatProfiles = getColorStatProfiles(colorSchemaName, colorStat);
        return colorStatProfiles.stream().mapToDouble(profile -> profile.getStat(colorValue)).boxed()
                .mapToInt(Double::intValue).boxed().toList();
    }

    /**
     * Finds the pixel analysis collection for a specific state image.
     * 
     * @param stateImage the state image to find analysis for
     * @return Optional containing the collection if found
     */
    public Optional<PixelProfiles> getPixelAnalysisCollection(StateImage stateImage) {
        return pixelAnalysisCollections.stream()
                .filter(pic -> pic.getStateImage().equals(stateImage)).findFirst();
    }

    /**
     * Retrieves the BGR score matrix for a specific state image.
     * 
     * @param stateImage the state image to get scores for
     * @return Optional containing the score matrix if found
     */
    public Optional<Mat> getScores(StateImage stateImage) {
        Optional<PixelProfiles> coll = pixelAnalysisCollections.stream()
                .filter(pic -> pic.getStateImage().equals(stateImage))
                .findFirst();
        if (coll.isEmpty()) return Optional.empty();
        return Optional.of(coll.get().getAnalysis(SCORE, BGR));
    }

    /**
     * Gets the score matrix for a state image, returning empty Mat if not found.
     * 
     * @param stateImage the state image to get scores for
     * @return score matrix or empty Mat if not found
     */
    public Mat getScoresMat(StateImage stateImage) {
        return getScores(stateImage).orElse(new Mat());
    }

}
