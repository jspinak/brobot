package io.github.jspinak.brobot.tools.history;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionConfig;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.tools.history.draw.DrawClassesLegend;
import io.github.jspinak.brobot.tools.history.draw.DrawContours;
import io.github.jspinak.brobot.tools.history.draw.DrawMatch;
import io.github.jspinak.brobot.tools.history.draw.DrawRect;
import io.github.jspinak.brobot.tools.history.visual.AnalysisSidebar;
import io.github.jspinak.brobot.tools.history.visual.Visualization;
import io.github.jspinak.brobot.util.image.io.ImageFileUtilities;

import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates the creation of comprehensive visual illustrations for action results.
 *
 * <p>This central manager coordinates all drawing operations to create rich visual documentation of
 * Brobot's automated actions. It combines multiple visualization components to produce layered
 * illustrations that show matches, movements, regions, and analysis results in a single cohesive
 * image.
 *
 * <p>Illustration components:
 *
 * <ul>
 *   <li>Base scene: Screenshot or analyzed image
 *   <li>Search regions: Blue rectangles showing where searches occurred
 *   <li>Matches: Pink highlights on found elements
 *   <li>Actions: Visual indicators for clicks, drags, moves
 *   <li>Sidebars: Detailed match information panels
 *   <li>Legends: Color mappings for classification results
 *   <li>Contours: Object boundaries from analysis
 * </ul>
 *
 * <p>Drawing order (important for proper layering):
 *
 * <ol>
 *   <li>Search regions (blue) - drawn first to avoid obscuring matches
 *   <li>Contours from scene analysis
 *   <li>Matches (pink) - the primary results
 *   <li>Action visualizations (clicks, drags, moves)
 *   <li>Sidebars with match details
 *   <li>Classification legends when applicable
 * </ol>
 *
 * <p>The manager handles special cases for different action types:
 *
 * <ul>
 *   <li>MOTION/REGIONS_OF_MOTION: Uses scene analysis match list
 *   <li>MOVE: Draws movement paths
 *   <li>DRAG: Draws drag trajectories
 *   <li>CLICK: Marks click points
 *   <li>DEFINE: Highlights defined regions
 *   <li>CLASSIFY/COLOR/HISTOGRAM: Includes classification legends
 * </ul>
 *
 * <p>Thread safety: Components are injected via Spring, ensuring single instances. The manager
 * itself maintains no mutable state.
 *
 * @see Visualization
 * @see AnalysisSidebar
 * @see DrawMatch
 * @see DrawClassesLegend
 * @see ActionResult
 */
@Slf4j
@Component
public class VisualizationOrchestrator {

    private AnalysisSidebar sidebar;
    private HistoryFileNamer illustrationFilename;
    private DrawMatch drawMatch;
    private DrawRect drawRect;
    private DrawClassesLegend drawClassesLegend;
    private ImageFileUtilities imageUtils;
    private ActionVisualizer draw;
    private DrawContours drawContours;

    public VisualizationOrchestrator(
            AnalysisSidebar sidebar,
            HistoryFileNamer illustrationFilename,
            DrawMatch drawMatch,
            DrawRect drawRect,
            DrawClassesLegend drawClassesLegend,
            ImageFileUtilities imageUtils,
            ActionVisualizer draw,
            DrawContours drawContours) {
        this.sidebar = sidebar;
        this.illustrationFilename = illustrationFilename;
        this.drawMatch = drawMatch;
        this.drawRect = drawRect;
        this.drawClassesLegend = drawClassesLegend;
        this.imageUtils = imageUtils;
        this.draw = draw;
        this.drawContours = drawContours;
    }

    /**
     * Creates and saves complete illustrations for an action's results.
     *
     * <p>This high-level method orchestrates the entire illustration process: generates all visual
     * components, merges them into finished illustrations, and writes them to disk with unique
     * filenames. Each scene analysis in the results gets its own illustration file.
     *
     * <p>The method handles multiple scenes efficiently, processing each independently and saving
     * all resulting illustrations with appropriate filenames based on the action context.
     *
     * @param matches action results containing scenes and matches to illustrate
     * @param searchRegions regions where searches were performed, shown in blue
     * @param actionConfig configuration determining illustration style and content
     */
    public void draw(ActionResult matches, List<Region> searchRegions, ActionConfig actionConfig) {
        log.debug(
                "[VISUALIZATION] draw() called with actionConfig: {}",
                actionConfig.getClass().getSimpleName());
        log.debug(
                "[VISUALIZATION] Number of scenes to illustrate: {}",
                matches.getSceneAnalysisCollection().getSceneAnalyses().size());

        drawIllustrations(matches, searchRegions, actionConfig);

        List<Visualization> illustrations =
                matches.getSceneAnalysisCollection().getAllIllustratedScenes();
        log.debug("[VISUALIZATION] Number of illustrated scenes: {}", illustrations.size());

        illustrations.forEach(
                ill ->
                        imageUtils.writeAllWithUniqueFilename(
                                ill.getFinishedMats(), ill.getFilenames()));

        log.debug("[VISUALIZATION] draw() completed");
    }

    /**
     * Generates layered illustrations for all scenes in the action results.
     *
     * <p>Processes each scene analysis independently, building up visual layers in a specific order
     * to ensure proper visibility. Search regions are drawn first in blue to avoid obscuring the
     * primary pink match highlights.
     *
     * <p>Layer composition strategy:
     *
     * <ul>
     *   <li>Blue search regions provide context without dominating
     *   <li>Pink matches show the primary results prominently
     *   <li>Action indicators add specific interaction details
     *   <li>Sidebars provide detailed match information
     *   <li>Legends explain color coding when needed
     * </ul>
     *
     * <p>Special handling for motion-based finds ensures the correct match list is used from the
     * scene analysis rather than the general results.
     *
     * @param matches action results with scenes to illustrate; modified with illustration data
     * @param searchRegions regions to highlight as search areas
     * @param actionConfig determines which visual elements to include
     */
    public void drawIllustrations(
            ActionResult matches, List<Region> searchRegions, ActionConfig actionConfig) {
        for (SceneAnalysis sceneAnalysis :
                matches.getSceneAnalysisCollection().getSceneAnalyses()) {
            Visualization ill = sceneAnalysis.getIllustrations();
            String filename =
                    illustrationFilename.getFilenameFromSceneAnalysis(sceneAnalysis, actionConfig);
            ill.setFilenames(filename);
            /*
            Draw the search regions on the scenes before drawing the matches. These regions are displayed in blue,
            and in the case of a 'confirmed FIND' action, they may represent initial matches that are not confirmed.
            It is useful to see them, but we don't want them to paint over the confirmed matches displayed in pink.
             */
            drawRect.drawRectAroundMatch(
                    ill, searchRegions, new Scalar(235, 206, 135, 0)); // the search regions
            drawContours.draw(sceneAnalysis);
            // For motion-based actions, would need specific handling based on config type
            drawMatch.drawMatches(ill, matches); // draw the matches on the scenes

            // Draw action-specific visualizations based on config type
            String configType = actionConfig.getClass().getSimpleName();
            if (configType.contains("Move")) draw.drawMove(ill, matches);
            if (configType.contains("Drag")) draw.drawDrag(ill, matches);
            if (configType.contains("Click")) draw.drawClick(ill, matches);
            if (configType.contains("Define")) draw.drawDefinedRegion(ill, matches);

            sidebar.drawSidebars(
                    ill, matches, actionConfig, sceneAnalysis.getMatchList()); // draw the sidebars
            sidebar.mergeSceneAndSidebar(ill); // merge the scene and the sidebar
            if (showClassesMat(actionConfig)) {
                drawClassesLegend.drawLegend(
                        ill, sceneAnalysis.getStateImageObjects(), actionConfig); // draw the legend
                drawClassesLegend.mergeClassesAndLegend(ill); // merge the classes and the legend
            }
        }
    }

    /**
     * Determines whether classification visualization should be included.
     *
     * <p>Classification visualizations (color legends and class mappings) are shown for actions
     * that involve pixel classification or color analysis. This includes explicit CLASSIFY actions
     * and any FIND operations that use color or histogram-based matching.
     *
     * <p>The method checks both the primary find type and any additional find actions to ensure
     * classification data is shown whenever relevant.
     *
     * @param actionConfig configuration to check for classification-related actions
     * @return true if classification visualizations should be included
     */
    private boolean showClassesMat(ActionConfig actionConfig) {
        // Show classes mat for classification-related configs
        String configType = actionConfig.getClass().getSimpleName();
        return configType.contains("Classify")
                || configType.contains("Histogram")
                || configType.contains("Color");
    }
}
