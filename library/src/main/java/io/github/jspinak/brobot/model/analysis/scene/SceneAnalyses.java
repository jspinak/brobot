package io.github.jspinak.brobot.model.analysis.scene;

import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.jspinak.brobot.model.analysis.color.ColorCluster.ColorSchemaName.BGR;
import static io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis.Analysis.BGR_FROM_INDICES_2D;

/**
 * Container for multiple scene analyses, supporting time-series and multi-scene operations.
 * 
 * <p>SceneAnalysisCollection aggregates analyses from multiple scenes, enabling
 * operations that span across time or multiple images. This includes motion
 * detection, temporal pattern analysis, and batch processing of related scenes.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Ordered collection of scene analyses</li>
 *   <li>Support for time-based analysis (motion detection)</li>
 *   <li>Aggregated results across all scenes</li>
 *   <li>Convenient access to first/last scene data</li>
 * </ul>
 * 
 * <p>Common use cases:</p>
 * <ul>
 *   <li>Motion analysis: Compare consecutive scenes</li>
 *   <li>Batch processing: Analyze multiple screenshots</li>
 *   <li>Temporal patterns: Detect changes over time</li>
 *   <li>Multi-state detection: Analyze scene transitions</li>
 * </ul>
 * 
 * @see SceneAnalysis
 * @see Scene
 * @see ContourExtractor
 */
@Getter
@Setter
public class SceneAnalyses {

    private double secondsBetweenScenes = 0;
    private List<SceneAnalysis> sceneAnalyses = new ArrayList<>();
    /**
     * The results of a multi-scene analysis are stored here.
     * This can be for actions such as motion analysis, or for aggregating matches across multiple scenes.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Mat results;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ContourExtractor contours;

    /**
     * Merges another collection into this one, combining scene analyses.
     * 
     * <p>Adds all scene analyses from the provided collection to this one.
     * The results and contours from the merged collection replace the
     * existing values in this collection.</p>
     * 
     * <p>Side effects: Overwrites results and contours fields</p>
     * 
     * @param sceneAnalysisCollection the collection to merge into this one
     */
    public void merge(SceneAnalyses sceneAnalysisCollection) {
        this.sceneAnalyses.addAll(sceneAnalysisCollection.getSceneAnalyses());
        this.results = sceneAnalysisCollection.results;
        this.contours = sceneAnalysisCollection.contours;
    }

    /**
     * Adds a single scene analysis to the collection.
     * 
     * @param sceneAnalysis the scene analysis to add
     */
    public void add(SceneAnalysis sceneAnalysis) {
        sceneAnalyses.add(sceneAnalysis);
    }

    /**
     * Retrieves the last scene in the collection.
     * 
     * <p>The last scene typically represents the current state and is used
     * for illustration purposes. In motion analysis, this is the most
     * recent frame analyzed.</p>
     * 
     * @return Optional containing the last scene, or empty if no scenes exist
     */
    public Optional<Scene> getLastScene() {
        if (sceneAnalyses.isEmpty()) return Optional.empty();
        return Optional.of(sceneAnalyses.get(sceneAnalyses.size() - 1).getScene());
    }

    /**
     * Retrieves the last scene analysis in the collection.
     * 
     * @return Optional containing the last analysis, or empty if no analyses exist
     */
    public Optional<SceneAnalysis> getLastSceneAnalysis() {
        if (sceneAnalyses.isEmpty()) return Optional.empty();
        return Optional.of(sceneAnalyses.get(sceneAnalyses.size() - 1));
    }

    /**
     * Gets the BGR visualization from the last scene's classification results.
     * 
     * @return Optional containing the BGR visualization matrix
     */
    public Optional<Mat> getLastResultsBGR() {
        return getLastSceneAnalysis().map(sceneAnalysis -> sceneAnalysis.getAnalysis(BGR, BGR_FROM_INDICES_2D));
    }

    /**
     * Extracts all scenes from the collection.
     * 
     * @return list of all scenes in order
     */
    public List<Scene> getScenes() {
        return sceneAnalyses.stream().map(SceneAnalysis::getScene).toList();
    }

    /**
     * Retrieves all scene images as BGR matrices.
     * 
     * @return list of BGR matrices from all scenes
     */
    public List<Mat> getAllScenesAsBGR() {
        List<Mat> mats = new ArrayList<>();
        for (SceneAnalysis sceneAnalysis : sceneAnalyses) {
            mats.add(sceneAnalysis.getScene().getPattern().getImage().getMatBGR());
        }
        return mats;
    }

    /**
     * Gets the BGR matrix from the last scene.
     * 
     * @return Optional containing the last scene's BGR matrix
     */
    public Optional<Mat> getLastSceneBGR() {
        List<Scene> scenes = getScenes();
        if (scenes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(scenes.get(scenes.size() - 1).getPattern().getImage().getMatBGR());
    }

    /**
     * Retrieves illustrations from all scene analyses.
     * 
     * @return list of illustrations for visualization
     */
    public List<Visualization> getAllIllustratedScenes() {
        return sceneAnalyses.stream().map(SceneAnalysis::getIllustrations).toList();
    }

    /**
     * Checks if the collection contains any scene analyses.
     * 
     * @return true if no scene analyses exist
     */
    public boolean isEmpty() {
        return sceneAnalyses.isEmpty();
    }

    /**
     * Prints a summary of the collection to the report.
     */
    public void print() {
        ConsoleReporter.println("Scene analysis collection with "+ sceneAnalyses.size() + " scenes");
    }
}
