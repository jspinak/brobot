package io.github.jspinak.brobot.action.basic.find.histogram;

import io.github.jspinak.brobot.action.ActionOptions;
import io.github.jspinak.brobot.action.basic.find.color.SceneProvider;
import io.github.jspinak.brobot.action.internal.find.SearchRegionResolver;
import io.github.jspinak.brobot.analysis.histogram.HistogramExtractor;
import io.github.jspinak.brobot.analysis.histogram.HistogramComparator;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalysis;
import io.github.jspinak.brobot.model.analysis.scene.SceneAnalyses;
import io.github.jspinak.brobot.model.element.Region;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;

import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implements histogram-based image matching for finding patterns in scenes.
 * <p>
 * This class provides an alternative to template matching by using color histogram
 * comparison. It's particularly effective for finding images that may have undergone
 * lighting changes, scaling, or other transformations that preserve color distribution
 * but alter exact pixel values.
 * <p>
 * The histogram matching process:
 * <ol>
 * <li>Extracts color histograms from target images using configurable HSV bins</li>
 * <li>Compares these histograms against regions in the scene</li>
 * <li>Returns matches sorted by histogram similarity score</li>
 * </ol>
 * <p>
 * Key advantages over template matching:
 * <ul>
 * <li>More robust to lighting variations</li>
 * <li>Can handle some geometric transformations</li>
 * <li>Faster for large search areas</li>
 * </ul>
 *
 * @see HistogramExtractor
 * @see HistogramComparator
 * @see ActionOptions
 */
@Component
public class FindHistogram {
    private final SearchRegionResolver selectRegions;
    private final HistogramExtractor getHistograms;
    private final SceneProvider getScenes;
    private final ExecutionModeController mockOrLive;

    public FindHistogram(SearchRegionResolver selectRegions, HistogramExtractor getHistograms, SceneProvider getScenes,
                         ExecutionModeController mockOrLive) {
        this.selectRegions = selectRegions;
        this.getHistograms = getHistograms;
        this.getScenes = getScenes;
        this.mockOrLive = mockOrLive;
    }

    /**
     * Performs histogram-based matching to find images within scenes.
     * <p>
     * This method orchestrates the complete histogram matching workflow:
     * <ol>
     * <li>Configures histogram bins from ActionOptions (hue, saturation, value)</li>
     * <li>Acquires scenes (screenshots or provided images)</li>
     * <li>For each target image and scene combination, performs histogram matching</li>
     * <li>Sorts all matches by score and returns the best ones</li>
     * </ol>
     * <p>
     * The ObjectCollection convention:
     * <ul>
     * <li>First collection: Contains target images to find</li>
     * <li>Subsequent collections: May contain scenes (if not provided, screenshots are taken)</li>
     * </ul>
     * <p>
     * Side effects: Updates the matches ActionResult with found matches and scene analysis data.
     * The default maxMatchesToActOn is set to 1 if not specified.
     *
     * @param matches ActionResult that holds configuration and accumulates match results.
     *                Modified in-place with found matches.
     * @param objectCollections List where the first collection contains images to find.
     *                          Must not be empty.
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        ActionOptions actionOptions = matches.getActionOptions();
        if (actionOptions.getMaxMatchesToActOn() <= 0) actionOptions.setMaxMatchesToActOn(1); // default for histogram
        getHistograms.setBins(
                actionOptions.getHueBins(), actionOptions.getSaturationBins(), actionOptions.getValueBins());
        List<Scene> scenes = getScenes.getScenes(actionOptions, objectCollections);
        SceneAnalyses sceneAnalysisCollection = new SceneAnalyses();
        List<Match> matchObjects = new ArrayList<>();
        objectCollections.get(0).getStateImages().forEach(img ->
                scenes.forEach(scene -> {
                    matchObjects.addAll(forOneImage(actionOptions, img, scene.getPattern().getMatHSV()));
                    sceneAnalysisCollection.add(new SceneAnalysis(new ArrayList<>(), scene));
                }));
        int maxMatches = actionOptions.getMaxMatchesToActOn();
        // sort the MatchObjects and add the best ones (up to maxRegs) to matches
        matchObjects.stream()
                .sorted(Comparator.comparingDouble(Match::getScore))
                .limit(maxMatches)
                .forEach(matches::add);
        matches.setSceneAnalysisCollection(sceneAnalysisCollection);
    }

    /**
     * Performs histogram matching for a single image against a scene.
     * <p>
     * This method:
     * <ol>
     * <li>Determines search regions based on the image's defined regions and action options</li>
     * <li>Delegates to the mock/live histogram matcher for actual comparison</li>
     * <li>Returns matches sorted by similarity score</li>
     * </ol>
     * <p>
     * The histogram comparison is performed in HSV color space for better
     * separation of color information from brightness.
     *
     * @param actionOptions Configuration including search region adjustments
     * @param image The target StateImage whose histogram will be matched
     * @param sceneHSV The scene in HSV color space to search within
     * @return List of Match objects sorted by score (best matches first)
     */
    private List<Match> forOneImage(ActionOptions actionOptions, StateImage image, Mat sceneHSV) {
        List<Region> searchRegions = selectRegions.getRegions(actionOptions, image);
        List<Match> matchObjects = mockOrLive.findHistogram(image, sceneHSV, searchRegions);
        return matchObjects.stream().sorted(Comparator.comparing(Match::getScore)).toList();
    }

}
