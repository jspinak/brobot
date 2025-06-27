package io.github.jspinak.brobot.action.basic.find.motion;

import io.github.jspinak.brobot.analysis.compare.ContourExtractor;
import io.github.jspinak.brobot.analysis.match.MatchProofer;
import io.github.jspinak.brobot.analysis.motion.FindDynamicPixels;
import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds matches based on fixed (unchanging) pixels across multiple patterns.
 * This class identifies regions that remain constant across different images,
 * which is useful for finding stable UI elements or static backgrounds.
 * 
 * <p>The process involves:
 * <ul>
 * <li>Collecting all patterns from object collections</li>
 * <li>Creating a fixed pixel mask showing unchanging areas</li>
 * <li>Finding contours in the mask to identify distinct regions</li>
 * <li>Validating matches against search regions and area constraints</li>
 * </ul></p>
 * 
 * <p>This approach is particularly effective for:
 * <ul>
 * <li>Identifying static UI elements in dynamic interfaces</li>
 * <li>Finding stable reference points for navigation</li>
 * <li>Detecting unchanging background regions</li>
 * </ul></p>
 * 
 * @see FindDynamicPixels
 * @see ContourExtractor
 * @see MatchProofer
 * @see ActionResult
 */
@Component
public class FindFixedPixelMatches {

    private final FindDynamicPixels findDynamicPixels;
    private final MatchProofer matchProofer;

    /**
     * Constructs a FindFixedPixelMatches instance with required dependencies.
     * 
     * @param findDynamicPixels service for identifying dynamic and fixed pixels
     * @param matchProofer service for validating matches against constraints
     */
    public FindFixedPixelMatches(FindDynamicPixels findDynamicPixels, MatchProofer matchProofer) {
        this.findDynamicPixels = findDynamicPixels;
        this.matchProofer = matchProofer;
    }

    /**
     * Finds matches based on pixels that remain fixed across multiple patterns.
     * This method creates a mask of unchanging pixels and uses contour detection
     * to identify distinct regions of fixed content.
     * 
     * <p>Side effects:
     * <ul>
     * <li>Sets the mask in the ActionResult to the fixed pixel mask</li>
     * <li>Populates the ActionResult with matches for each fixed region found</li>
     * </ul></p>
     * 
     * @param matches the {@link ActionResult} to populate with found matches
     * @param objectCollections list of {@link ObjectCollection} containing patterns to analyze
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        List<Pattern> allPatterns = getAllPatterns(objectCollections);
        MatVector matVector = new MatVector();
        allPatterns.forEach(pattern -> matVector.push_back(pattern.getMat()));
        if (matVector.size() < 2) return; // nothing to compare
        Mat fixedPixelMask = findDynamicPixels.getFixedPixelMask(matVector);
        matches.setMask(fixedPixelMask);
        // since all Patterns have the same fixed pixels, we can use any Pattern for the Match objects
        setMatches(fixedPixelMask, matches, allPatterns.get(0));
    }

    /**
     * Extracts all patterns from the provided object collections.
     * Collects patterns from both StateImages and Scenes within each collection.
     * 
     * @param objectCollections list of {@link ObjectCollection} to extract patterns from
     * @return list of all {@link Pattern} objects found in the collections
     */
    public List<Pattern> getAllPatterns(List<ObjectCollection> objectCollections) {
        List<Pattern> allPatterns = new ArrayList<>();
        // add all StateImages and Scenes
        objectCollections.forEach(objColl -> {
            objColl.getStateImages().forEach(stateImage -> {
                allPatterns.addAll(stateImage.getPatterns());
            });
            objColl.getScenes().forEach(scene -> allPatterns.add(scene.getPattern()));
        });
        return allPatterns;
    }

    /**
     * Creates matches from contours found in the provided mask.
     * Each contour represents a region of fixed pixels that could be a match.
     * Matches are validated against area constraints and search regions before
     * being added to the results.
     * 
     * <p>Side effects:
     * <ul>
     * <li>Adds validated matches to the ActionResult</li>
     * <li>Sets the search image for each match to the provided pattern</li>
     * </ul></p>
     * 
     * @param mask binary mask indicating fixed pixel regions
     * @param matches the {@link ActionResult} to populate with validated matches
     * @param pattern the {@link Pattern} to associate with found matches
     * @see ContourExtractor
     * @see MatchProofer#isInSearchRegions
     */
    public void setMatches(Mat mask, ActionResult matches, Pattern pattern) {
        ContourExtractor contours = new ContourExtractor.Builder()
                .setBgrFromClassification2d(mask)
                .setMinArea(matches.getActionOptions().getMinArea())
                .setMaxArea(matches.getActionOptions().getMaxArea())
                .build();
        for (Match match : contours.getMatchList()) {
            if (matchProofer.isInSearchRegions(match, matches.getActionOptions(), pattern)) {
                match.setSearchImage(new Image(pattern));
                matches.add(match);
            }
        }
    }
}
