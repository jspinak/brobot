package io.github.jspinak.brobot.tools.history.visual;

import static org.bytedeco.opencv.global.opencv_imgproc.resize;

import java.util.ArrayList;
import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.history.VisualizationLayout;
import io.github.jspinak.brobot.tools.history.draw.DrawHistogram;
import io.github.jspinak.brobot.util.image.visualization.MatBuilder;
import io.github.jspinak.brobot.util.image.visualization.MatrixVisualizer;

import lombok.extern.slf4j.Slf4j;

/**
 * Creates detailed sidebar visualizations showing individual match results.
 *
 * <p>This component generates sidebar panels that display detailed views of each match found during
 * action execution. The sidebar provides a zoomed-in view of matched regions, allowing developers
 * to inspect individual matches closely and understand why certain patterns were or weren't
 * detected.
 *
 * <p>AnalysisSidebar features:
 *
 * <ul>
 *   <li>Grid layout of match thumbnails (default 50x50 pixels)
 *   <li>Multi-column display for large match sets
 *   <li>Special handling for motion and histogram matches
 *   <li>Automatic resizing to maintain consistent entry size
 *   <li>Configurable spacing between entries
 * </ul>
 *
 * <p>Layout algorithm:
 *
 * <ul>
 *   <li>Calculates entries per column based on scene height
 *   <li>Distributes matches vertically first, then across columns
 *   <li>Maintains aspect ratio when resizing match regions
 *   <li>Handles edge cases for matches at scene boundaries
 * </ul>
 *
 * <p>Special visualizations:
 *
 * <ul>
 *   <li><b>Motion matches</b>: Shows regions where motion was detected
 *   <li><b>Histogram matches</b>: Displays match alongside its histogram
 *   <li><b>Standard matches</b>: Shows the matched region from the scene
 * </ul>
 *
 * <p>Integration workflow:
 *
 * <ol>
 *   <li>Extract match regions from the scene
 *   <li>Resize to standard thumbnail size
 *   <li>Arrange in column-based grid layout
 *   <li>Merge with main scene visualization
 * </ol>
 *
 * <p>The sidebar complements the main scene view by providing:
 *
 * <ul>
 *   <li>Clear visibility of small matches
 *   <li>Detailed inspection of match quality
 *   <li>Visual confirmation of correct detections
 *   <li>Easy identification of false positives
 * </ul>
 *
 * @see Visualization
 * @see VisualizationLayout
 * @see DrawHistogram
 * @see ActionResult
 */
@Slf4j
@Component
public class AnalysisSidebar {

    private DrawHistogram drawHistogram;
    private MatrixVisualizer matVisualize;
    private VisualizationLayout columnMatOps;

    /** Width of each sidebar entry thumbnail in pixels. */
    private int sidebarEntryW = 50;

    /** Height of each sidebar entry thumbnail in pixels. */
    private int sidebarEntryH = 50;

    /** Pixel spacing between sidebar entries for visual separation. */
    private int spacesBetweenEntries = 4;

    /**
     * Number of match entries that fit vertically in one column. Calculated based on scene height
     * and entry dimensions.
     */
    private int matchesPerColumn;

    /**
     * Number of columns needed to display all matches. Calculated based on total matches and
     * matches per column.
     */
    private int columns;

    public AnalysisSidebar(
            DrawHistogram drawHistogram,
            MatrixVisualizer matVisualize,
            VisualizationLayout columnMatOps) {
        this.drawHistogram = drawHistogram;
        this.matVisualize = matVisualize;
        this.columnMatOps = columnMatOps;
    }

    /**
     * Creates sidebar visualization for all matches in the action result.
     *
     * <p>Generates a comprehensive sidebar showing thumbnails of all matched regions. The method
     * handles different match types (standard, motion, histogram) and creates appropriate
     * visualizations for each.
     *
     * <p>Processing includes:
     *
     * <ul>
     *   <li>Extracting match regions from the scene
     *   <li>Creating specialized entries for different match types
     *   <li>Arranging entries in a multi-column grid
     *   <li>Setting the completed sidebar in illustrations
     * </ul>
     *
     * @param illustrations container for visualization components; sidebar is set here
     * @param matches action results containing matches to display
     * @param actionConfig determines special handling for motion/histogram matches
     * @param matchList specific matches to display (may differ from matches.getMatchList())
     */
    public void drawSidebars(
            Visualization illustrations,
            ActionResult matches,
            ActionConfig actionConfig,
            List<Match> matchList) {
        log.debug("[SIDEBAR] drawSidebars called");

        if (illustrations.getScene() == null) {
            log.warn("[SIDEBAR] Scene is null, cannot draw sidebars");
            return;
        }

        log.debug(
                "[SIDEBAR] Scene dimensions: {}x{}",
                illustrations.getScene().cols(),
                illustrations.getScene().rows());

        List<Mat> sidebarEntries =
                getEntriesForSceneSidebar(illustrations, matches, actionConfig, matchList);
        log.debug("[SIDEBAR] Number of sidebar entries: {}", sidebarEntries.size());

        Mat sidebar = getSidebar(illustrations.getScene(), sidebarEntries, matches, matchList);

        if (sidebar == null) {
            log.error("[SIDEBAR] getSidebar returned null");
        } else if (sidebar.empty()) {
            log.error("[SIDEBAR] getSidebar returned empty Mat");
        } else {
            log.debug("[SIDEBAR] Sidebar dimensions: {}x{}", sidebar.cols(), sidebar.rows());
        }

        illustrations.setSidebar(sidebar);
    }

    /**
     * Combines the annotated scene with its sidebar into a single visualization.
     *
     * <p>Creates the final illustration by horizontally concatenating the scene (with match
     * annotations) and the sidebar (with match details). This provides a complete view showing both
     * context and details.
     *
     * <p>The merged visualization shows:
     *
     * <ul>
     *   <li>Left side: Full scene with match rectangles
     *   <li>Right side: Sidebar with match thumbnails
     *   <li>Spacing between for visual separation
     * </ul>
     *
     * @param illustrations container with scene and sidebar; sets merged result
     */
    public void mergeSceneAndSidebar(Visualization illustrations) {
        log.debug("[SIDEBAR] mergeSceneAndSidebar called");

        if (illustrations.getMatchesOnScene() == null) {
            log.warn("[SIDEBAR] MatchesOnScene is null, cannot merge");
            return;
        }

        if (illustrations.getSidebar() == null || illustrations.getSidebar().empty()) {
            log.warn("[SIDEBAR] Sidebar is null or empty, using scene only");
            // If no sidebar, just use the scene with matches
            illustrations.setSceneWithMatchesAndSidebar(illustrations.getMatchesOnScene());
            return;
        }

        log.debug(
                "[SIDEBAR] MatchesOnScene dimensions: {}x{}",
                illustrations.getMatchesOnScene().cols(),
                illustrations.getMatchesOnScene().rows());
        log.debug(
                "[SIDEBAR] Sidebar dimensions: {}x{}",
                illustrations.getSidebar().cols(),
                illustrations.getSidebar().rows());

        Mat sceneAndSidebar =
                new MatBuilder()
                        .addHorizontalSubmats(
                                illustrations.getMatchesOnScene(), illustrations.getSidebar())
                        .setSpaceBetween(spacesBetweenEntries)
                        .build();

        if (sceneAndSidebar == null) {
            log.error("[SIDEBAR] MatBuilder returned null");
        } else if (sceneAndSidebar.empty()) {
            log.error("[SIDEBAR] MatBuilder returned empty Mat");
        } else {
            log.debug(
                    "[SIDEBAR] Merged Mat dimensions: {}x{}",
                    sceneAndSidebar.cols(),
                    sceneAndSidebar.rows());
        }

        illustrations.setSceneWithMatchesAndSidebar(sceneAndSidebar);
    }

    /**
     * Calculates sidebar layout parameters based on scene dimensions.
     *
     * <p>Determines how many match entries fit vertically and how many columns are needed. The
     * calculation accounts for entry height and spacing to ensure proper fit within the scene
     * height.
     *
     * @param scene reference for height calculation
     * @param matchesSize total number of matches to display
     */
    public void initSidebar(Mat scene, int matchesSize) {
        matchesPerColumn = scene.rows() / (sidebarEntryH + spacesBetweenEntries);
        columns = (int) Math.ceil((double) matchesSize / matchesPerColumn);
    }

    /**
     * Assembles sidebar entries into a multi-column grid layout.
     *
     * <p>Creates individual columns of match thumbnails and merges them horizontally to form the
     * complete sidebar. Uses ColumnMatOps for efficient column-based layout management.
     *
     * <p>Layout process:
     *
     * <ol>
     *   <li>Initialize layout parameters based on match count
     *   <li>Distribute entries across columns
     *   <li>Build each column vertically
     *   <li>Merge columns horizontally with spacing
     * </ol>
     *
     * @param scene reference for layout calculations
     * @param sidebarEntries prepared thumbnail images for all matches
     * @param matches action results for size calculation
     * @param matchList actual matches being displayed
     * @return completed sidebar Mat with all entries arranged
     */
    private Mat getSidebar(
            Mat scene, List<Mat> sidebarEntries, ActionResult matches, List<Match> matchList) {
        if (sidebarEntries.isEmpty()) {
            log.debug("[SIDEBAR] No sidebar entries to display");
            return new Mat(); // Return empty Mat for no entries
        }

        initSidebar(scene, matches.size());
        List<Mat> sidebarColumns = new ArrayList<>();
        for (int i = 0; i < columns; i++) {
            List<Mat> columnEntries =
                    columnMatOps.getColumnEntries(sidebarEntries, i, matchesPerColumn);
            Mat columnMat = columnMatOps.getColumnMat(columnEntries);
            sidebarColumns.add(columnMat);
        }
        return columnMatOps.mergeColumnMats(sidebarColumns, spacesBetweenEntries);
    }

    /**
     * Prepares sidebar entries based on match type and visualization needs.
     *
     * <p>Creates appropriate thumbnail representations for different match types:
     *
     * <ul>
     *   <li><b>Motion/Regions of Motion</b>: Simple thumbnails from matchList
     *   <li><b>Histogram</b>: Thumbnails paired with histogram visualizations
     *   <li><b>Standard</b>: Regular thumbnails from matches.getMatchList()
     * </ul>
     *
     * <p>The distinction between matchList and matches.getMatchList() allows for specialized
     * handling when the display list differs from the actual match results.
     *
     * @param illustrations source of scene data for extracting regions
     * @param matches complete action results
     * @param actionConfig determines special visualization requirements
     * @param matchList specific matches to display (may be filtered)
     * @return list of prepared Mat entries ready for grid layout
     */
    private List<Mat> getEntriesForSceneSidebar(
            Visualization illustrations,
            ActionResult matches,
            ActionConfig actionConfig,
            List<Match> matchList) {
        List<Mat> sidebarEntries = new ArrayList<>();
        // For now, just show all matches without special handling
        // Motion and histogram strategies would need separate handling based on config type
        matches.getMatchList()
                .forEach(m -> sidebarEntries.add(getMatchForSidebar(illustrations, m)));
        return sidebarEntries;
    }

    /**
     * Extracts and prepares a single match region for sidebar display.
     *
     * <p>Safely extracts the matched region from the scene and resizes it to standard thumbnail
     * dimensions. Includes boundary checking to handle edge cases where matches might exceed scene
     * boundaries.
     *
     * <p>Safety features:
     *
     * <ul>
     *   <li>Size validation to prevent invalid extractions
     *   <li>Returns full scene if match region is invalid
     *   <li>Maintains aspect ratio during resize
     * </ul>
     *
     * @param illustrations source of scene data
     * @param match region to extract from scene
     * @return resized thumbnail Mat ready for sidebar display
     */
    private Mat getMatchForSidebar(Visualization illustrations, Match match) {
        if (illustrations.getScene().sizeof() < match.w() * match.h())
            return illustrations.getScene();
        Rect rect = new Rect(match.x(), match.y(), match.w(), match.h());
        Mat matchFromScene = illustrations.getScene().apply(rect);
        resize(matchFromScene, matchFromScene, new Size(sidebarEntryW, sidebarEntryH));
        return matchFromScene;
    }

    /**
     * Creates a combined visualization of match thumbnail and histogram.
     *
     * <p>For histogram-based matches, this method creates a side-by-side display showing both the
     * matched region and its histogram. This dual view helps understand why the histogram matching
     * succeeded.
     *
     * <p>The combined entry shows:
     *
     * <ul>
     *   <li>Left: Thumbnail of the matched region
     *   <li>Right: Histogram visualization
     *   <li>Spacing between for clarity
     * </ul>
     *
     * @param matchFromScene thumbnail of the matched region
     * @param match contains histogram data for visualization
     * @return combined Mat showing match and histogram side-by-side
     */
    private Mat getMatchAndHistogram(Mat matchFromScene, Match match) {
        Mat histMat = drawHistogram.draw(sidebarEntryW, sidebarEntryH, match.getHistogram());
        return new MatBuilder()
                .setName("matchAndHist")
                .setSpaceBetween(spacesBetweenEntries)
                .addHorizontalSubmats(matchFromScene, histMat)
                .build();
    }
}
