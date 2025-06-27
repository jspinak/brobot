package io.github.jspinak.brobot.tools.history.draw;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.tools.history.visual.Visualization;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides high-level drawing operations for visualizing match results on images.
 * <p>
 * This class builds upon {@link DrawRect} to provide specialized functionality for
 * drawing collections of matches with consistent styling. It supports drawing matches
 * on both raw scenes and layered illustrations, making it a key component in the
 * Brobot visualization pipeline.
 * <p>
 * Key features:
 * <ul>
 * <li>Batch drawing of multiple matches</li>
 * <li>Default and custom color support</li>
 * <li>Scene-aware filtering of matches</li>
 * <li>Multi-layer drawing on Illustrations objects</li>
 * </ul>
 * <p>
 * Default visualization:
 * <ul>
 * <li>Default color: Pink/Purple (RGB: 255, 150, 255)</li>
 * <li>Rectangles drawn with 1-pixel padding around matches</li>
 * <li>Automatic null-safety for illustration layers</li>
 * </ul>
 * <p>
 * Common use cases:
 * <ul>
 * <li>Visualizing find operation results</li>
 * <li>Debugging pattern matching accuracy</li>
 * <li>Creating annotated screenshots for reports</li>
 * <li>Highlighting multiple UI elements simultaneously</li>
 * </ul>
 *
 * @see DrawRect
 * @see Match
 * @see Visualization
 * @see ActionResult
 */
@Component
public class DrawMatch {

    private final DrawRect drawRect;

    public DrawMatch(DrawRect drawRect) {
        this.drawRect = drawRect;
    }

    /**
     * Draws rectangles around all matches using the default pink/purple color.
     * <p>
     * This convenience method uses a predefined color that provides good
     * visibility on most backgrounds. Each match is drawn with a 1-pixel
     * padding around its boundaries.
     *
     * @param scene The OpenCV Mat image on which to draw. Must not be null.
     * @param matchList List of matches to visualize. Empty lists are handled safely.
     */
    public void drawMatches(Mat scene, List<Match> matchList) {
        matchList.forEach(m -> drawRect.drawRectAroundMatch(scene, m, new Scalar(255, 150, 255, 0)));
    }

    /**
     * Draws rectangles around all matches using a custom color.
     * <p>
     * This method allows for color customization, useful when different
     * match types need distinct visual representation or when adapting
     * to specific background colors.
     *
     * @param scene The OpenCV Mat image on which to draw. Must not be null.
     * @param matchList List of matches to visualize. Empty lists are handled safely.
     * @param color The color for the rectangles as an OpenCV Scalar (B,G,R,A format).
     */
    public void drawMatches(Mat scene, List<Match> matchList, Scalar color) {
        matchList.forEach(m -> drawRect.drawRectAroundMatch(scene, m, color));
    }

    /**
     * Draws matches from an ActionResult onto multiple illustration layers.
     * <p>
     * This method performs scene-aware filtering, only drawing matches that
     * belong to the current scene. Matches are drawn on both the scene layer
     * and the classes layer of the Illustrations object, providing multiple
     * visualization perspectives.
     * <p>
     * The filtering process:
     * <ol>
     * <li>Extracts matches from the ActionResult</li>
     * <li>Filters matches by scene name</li>
     * <li>Draws filtered matches on available layers</li>
     * </ol>
     * <p>
     * Layer handling:
     * <ul>
     * <li>Scene layer - Shows matches on the raw screenshot</li>
     * <li>Classes layer - Shows matches with class/type annotations</li>
     * <li>Null layers are safely skipped</li>
     * </ul>
     *
     * @param illScn The Illustrations object containing visualization layers and scene context.
     * @param matches The ActionResult containing matches to draw. Matches are filtered by scene name.
     */
    public void drawMatches(Visualization illScn, ActionResult matches) {
        List<Match> matchList = new ArrayList<>();
        matches.getMatchList().forEach(mO -> {
            if (mO.getImage().getName().equals(illScn.getSceneName())) {
                matchList.add(mO);
            }
        });
        if (illScn.getMatchesOnScene() != null) drawMatches(illScn.getMatchesOnScene(), matchList);
        if (illScn.getMatchesOnClasses() != null) drawMatches(illScn.getMatchesOnClasses(), matchList);
    }
}
