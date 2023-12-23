package io.github.jspinak.brobot.illustratedHistory;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.SceneAnalysis;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawMatch;
import io.github.jspinak.brobot.illustratedHistory.draw.DrawRect;
import io.github.jspinak.brobot.imageUtils.ImageUtils;
import io.github.jspinak.brobot.reports.Report;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.List;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Action.*;
import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.*;

@Component
public class IllustrationManager {

    private Sidebar sidebar;
    private IllustrationFilename illustrationFilename;
    private DrawMatch drawMatch;
    private DrawRect drawRect;
    private DrawClassesLegend drawClassesLegend;
    private ImageUtils imageUtils;
    private Draw draw;
    private DrawContours drawContours;

    public IllustrationManager(Sidebar sidebar, IllustrationFilename illustrationFilename,
                               DrawMatch drawMatch, DrawRect drawRect, DrawClassesLegend drawClassesLegend,
                               ImageUtils imageUtils, Draw draw, DrawContours drawContours) {
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
     * Illustrates the last action performed.
     *
     * @param matches the matches to be illustrated
     * @param searchRegions the search regions to be illustrated
     * @param actionOptions the action to be illustrated
     */
    public void draw(Matches matches, List<Region> searchRegions, ActionOptions actionOptions) {
        drawIllustrations(matches, searchRegions, actionOptions);
        matches.getSceneAnalysisCollection().getAllIllustratedScenes().forEach(
                ill -> imageUtils.writeAllWithUniqueFilename(ill.getFinishedMats(), ill.getFilenames()));
    }

    /**
     * Initializes the illustrations with matches, regions, and filenames.
     * @param matches the matches to be illustrated
     * @param actionOptions the action to be illustrated
     */
    public void drawIllustrations(Matches matches, List<Region> searchRegions, ActionOptions actionOptions) {
        for (SceneAnalysis sceneAnalysis : matches.getSceneAnalysisCollection().getSceneAnalyses()) {
            Illustrations ill = sceneAnalysis.getIllustrations();
            String filename = illustrationFilename.getFilenameFromSceneAnalysis(sceneAnalysis, actionOptions);
            ill.setFilenames(filename);
            /*
            Draw the search regions on the scenes before drawing the matches. These regions are displayed in blue,
            and in the case of a 'confirmed FIND' action, they may represent initial matches that are not confirmed.
            It is useful to see them, but we don't want them to paint over the confirmed matches displayed in pink.
             */
            drawRect.drawRectAroundMatch(ill, searchRegions, new Scalar(235, 206, 135, 0)); // the search regions
            drawContours.draw(sceneAnalysis);
            if (actionOptions.getFind() == MOTION || actionOptions.getFind() == REGIONS_OF_MOTION)
                drawMatch.drawMatches(ill.getMatchesOnScene(), sceneAnalysis.getMatchList());
            else drawMatch.drawMatches(ill, matches); // draw the matches on the scenes
            if (actionOptions.getAction() == MOVE) draw.drawMove(ill, matches); // draw the move on the scenes
            if (actionOptions.getAction() == DRAG) draw.drawDrag(ill, matches); // draw the drag on the scenes
            if (actionOptions.getAction() == CLICK) draw.drawClick(ill, matches); // draw the click on the scenes
            if (actionOptions.getAction() == DEFINE) draw.drawDefinedRegion(ill, matches);
            sidebar.drawSidebars(ill, matches, actionOptions, sceneAnalysis.getMatchList()); // draw the sidebars
            sidebar.mergeSceneAndSidebar(ill); // merge the scene and the sidebar
            if (showClassesMat(actionOptions)) {
                drawClassesLegend.drawLegend(ill, sceneAnalysis.getStateImageObjects(), actionOptions); // draw the legend
                drawClassesLegend.mergeClassesAndLegend(ill); // merge the classes and the legend
            }
        }
    }

    private boolean showClassesMat(ActionOptions actionOptions) {
        return actionOptions.getAction() == CLASSIFY
                || actionOptions.getFind() == HISTOGRAM
                || actionOptions.getFind() == COLOR
                || actionOptions.getFindActions().contains(COLOR)
                || actionOptions.getFindActions().contains(HISTOGRAM);
    }

}
