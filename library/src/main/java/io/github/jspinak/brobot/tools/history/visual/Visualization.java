package io.github.jspinak.brobot.tools.history.visual;

import io.github.jspinak.brobot.tools.history.ActionVisualizer;
import io.github.jspinak.brobot.tools.history.IllustrationController;
import io.github.jspinak.brobot.tools.history.StateLayoutVisualizer;
import io.github.jspinak.brobot.tools.history.RuntimeStateVisualizer;
import io.github.jspinak.brobot.tools.logging.ConsoleReporter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.Mat;

import java.util.Arrays;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * Container for visual debugging and analysis illustrations in Brobot.
 * 
 * <p>Visualization manages the collection of visual outputs generated during automation 
 * execution for debugging, analysis, and reporting purposes. It creates comprehensive 
 * visual documentation showing what Brobot "sees" during pattern matching, including 
 * match locations, search regions, image classifications, and motion detection. These 
 * illustrations are invaluable for understanding automation behavior and troubleshooting 
 * recognition issues.</p>
 * 
 * <p>Illustration types:
 * <ul>
 *   <li><b>Scene with Matches</b>: Screenshot annotated with found patterns and search regions</li>
 *   <li><b>Sidebar Details</b>: Detailed view of individual matches with scores and metadata</li>
 *   <li><b>Class Segmentation</b>: Visual classification of screen regions by image type</li>
 *   <li><b>Legend</b>: Reference showing image classes, patterns, and color profiles</li>
 *   <li><b>Motion Detection</b>: Highlights pixels that changed between frames</li>
 * </ul>
 * </p>
 * 
 * <p>Mat components managed:
 * <ul>
 *   <li><b>scene</b>: Original screenshot without annotations</li>
 *   <li><b>matchesOnScene</b>: Scene with match rectangles and search regions</li>
 *   <li><b>sceneWithMatchesAndSidebar</b>: Complete illustration with detail panel</li>
 *   <li><b>classes</b>: Scene with image classification overlay</li>
 *   <li><b>matchesOnClasses</b>: Classification view with matches</li>
 *   <li><b>classesWithMatchesAndLegend</b>: Complete classification illustration</li>
 *   <li><b>motion</b>: Motion detection visualization</li>
 * </ul>
 * </p>
 * 
 * <p>Use cases:
 * <ul>
 *   <li>Debugging pattern matching failures</li>
 *   <li>Analyzing search region effectiveness</li>
 *   <li>Understanding state detection results</li>
 *   <li>Documenting automation behavior</li>
 *   <li>Creating test reports with visual evidence</li>
 *   <li>Training users on pattern selection</li>
 * </ul>
 * </p>
 * 
 * <p>Workflow:
 * <ol>
 *   <li>Set base scene from screenshot</li>
 *   <li>Draw matches and regions on scene</li>
 *   <li>Create sidebar with match details</li>
 *   <li>Generate classification overlays</li>
 *   <li>Compose final illustrations</li>
 *   <li>Write to files for review</li>
 * </ol>
 * </p>
 * 
 * <p>File management:
 * <ul>
 *   <li>Automatic filename generation with prefixes</li>
 *   <li>Scene illustrations: "[name]-scene-"</li>
 *   <li>Class illustrations: "[name]-classes-"</li>
 *   <li>Batch writing of all illustration types</li>
 *   <li>PNG format for web compatibility</li>
 * </ul>
 * </p>
 * 
 * <p>Visual elements included:
 * <ul>
 *   <li>Match rectangles with confidence scores</li>
 *   <li>Search region boundaries</li>
 *   <li>Pattern thumbnails in sidebar</li>
 *   <li>Color-coded classifications</li>
 *   <li>Motion detection highlights</li>
 *   <li>Timestamp and state information</li>
 * </ul>
 * </p>
 * 
 * <p>Benefits for development:
 * <ul>
 *   <li>Visual confirmation of recognition accuracy</li>
 *   <li>Easy identification of false positives/negatives</li>
 *   <li>Understanding of search region coverage</li>
 *   <li>Insight into classification algorithms</li>
 *   <li>Historical record of automation execution</li>
 * </ul>
 * </p>
 * 
 * <p>In the model-based approach, Visualization provides the visual feedback loop 
 * essential for developing and maintaining robust automation. By showing exactly 
 * what the automation system perceives and how it makes decisions, developers can 
 * quickly identify and resolve recognition issues, optimize search regions, and 
 * ensure reliable state detection.</p>
 * 
 * @since 1.0
 * @see IllustrationController
 * @see StateLayoutVisualizer
 * @see ActionVisualizer
 * @see RuntimeStateVisualizer
 * @see Mat
 */
@Slf4j
@Getter
@Setter
public class Visualization {

    public enum Type {
        SCENE_MATCHES_SIDEBAR, CLASSES_LEGEND, MOTION
    }

    private String sceneName = "";
    private Mat sceneWithMatchesAndSidebar; // matches drawn on the scene together with the sidebar
    private Mat matchesOnScene; // matches and search regions drawn on the scene
    private Mat scene; // the scene
    private Mat sidebar; // shows matches in more detail
    private String filenameScene;
    private Mat classesWithMatchesAndLegend; // classes drawn on the scene together with the legend
    private Mat matchesOnClasses; // the scene with matches and classes
    private Mat classes; // the scene with classes drawn on it (segmentation by image)
    private Mat legend; // shows the classes: their underlying images and kMeans centers
    private String filenameClasses;
    private Mat motion; // pixels that have changed between scenes
    private Mat motionWithMatches; // matches depend on minSize and other parameters

    public Mat getMat(Type type) {
        switch (type) {
            case SCENE_MATCHES_SIDEBAR:
                return sceneWithMatchesAndSidebar;
            case CLASSES_LEGEND:
                return classesWithMatchesAndLegend;
            default:
                ConsoleReporter.println("No such type of Mat: " + type);
                return null;
        }
    }

    public String getFilename(Type type) {
        switch (type) {
            case SCENE_MATCHES_SIDEBAR:
                return filenameScene;
            case CLASSES_LEGEND:
                return filenameClasses;
            default:
                ConsoleReporter.println("No such type of filename: " + type);
                return null;
        }
    }

    public void write(Type type) {
        Mat mat = getMat(type);
        String filename = getFilename(type);
        if (mat == null || filename == null) {
            ConsoleReporter.println("Didn't write illustration. Mat or filename is null.");
            return;
        }
        imwrite(filename, mat);
    }

    public void write() {
        write(Type.SCENE_MATCHES_SIDEBAR);
        write(Type.CLASSES_LEGEND);
    }

    public void setScene(Mat scene) {
        log.debug("[VISUALIZATION] setScene called");
        if (scene == null) {
            log.error("[VISUALIZATION] Cannot set null scene");
            return;
        }
        if (scene.empty()) {
            log.error("[VISUALIZATION] Cannot set empty scene");
            return;
        }
        log.debug("[VISUALIZATION] Setting scene with dimensions: {}x{}", scene.cols(), scene.rows());
        this.scene = scene;
        this.matchesOnScene = scene.clone();
        log.debug("[VISUALIZATION] Scene and matchesOnScene set successfully");
    }

    public void setClasses(Mat classes) {
        this.classes = classes;
        this.matchesOnClasses = classes.clone();
    }

    public void setFilenames(String filename) {
        this.filenameScene = filename + "-scene-";
        this.filenameClasses = filename + "-classes-";
    }

    public List<String> getFilenames() {
        return Arrays.asList(filenameScene, filenameClasses);
    }

    public List<Mat> getFinishedMats() {
        log.debug("[VISUALIZATION] getFinishedMats called");
        log.debug("[VISUALIZATION] sceneWithMatchesAndSidebar: {}", 
                sceneWithMatchesAndSidebar == null ? "null" : 
                (sceneWithMatchesAndSidebar.empty() ? "empty" : 
                String.format("%dx%d", sceneWithMatchesAndSidebar.cols(), sceneWithMatchesAndSidebar.rows())));
        log.debug("[VISUALIZATION] classesWithMatchesAndLegend: {}", 
                classesWithMatchesAndLegend == null ? "null" : 
                (classesWithMatchesAndLegend.empty() ? "empty" : 
                String.format("%dx%d", classesWithMatchesAndLegend.cols(), classesWithMatchesAndLegend.rows())));
        return Arrays.asList(sceneWithMatchesAndSidebar, classesWithMatchesAndLegend);
    }
}
