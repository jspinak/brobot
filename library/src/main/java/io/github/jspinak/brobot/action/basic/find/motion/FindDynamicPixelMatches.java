package io.github.jspinak.brobot.action.basic.find.motion;

import java.util.List;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.action.ActionResult;
import io.github.jspinak.brobot.action.ObjectCollection;
import io.github.jspinak.brobot.analysis.motion.FindDynamicPixels;
import io.github.jspinak.brobot.model.element.Pattern;

/**
 * Finds matches based on dynamic (changing) pixels across multiple patterns. This class analyzes a
 * collection of patterns to identify pixels that vary between them, then uses this information to
 * create match results.
 *
 * <p>The process involves:
 *
 * <ul>
 *   <li>Collecting all patterns from the provided object collections
 *   <li>Creating a dynamic pixel mask showing which pixels change
 *   <li>Using the mask to generate matches based on motion areas
 * </ul>
 *
 * <p>Note: Despite the method name suggesting it finds dynamic pixels, the current implementation
 * appears to use the dynamic pixel mask directly, which may be a naming inconsistency.
 *
 * @see FindDynamicPixels
 * @see FindFixedPixelMatches
 * @see ActionResult
 * @see ObjectCollection
 */
@Component
public class FindDynamicPixelMatches {

    private final FindDynamicPixels findDynamicPixels;
    private final FindFixedPixelMatches findFixedPixelMatches;

    /**
     * Constructs a FindDynamicPixelMatches instance with the required services.
     *
     * @param findDynamicPixels service for finding pixels that change between images
     * @param findFixedPixelMatches service for creating matches from pixel masks
     */
    public FindDynamicPixelMatches(
            FindDynamicPixels findDynamicPixels, FindFixedPixelMatches findFixedPixelMatches) {
        this.findDynamicPixels = findDynamicPixels;
        this.findFixedPixelMatches = findFixedPixelMatches;
    }

    /**
     * Finds matches based on dynamic pixels across multiple patterns. This method analyzes all
     * patterns in the object collections to identify pixels that change between them, then creates
     * matches based on these dynamic regions.
     *
     * <p>Side effects:
     *
     * <ul>
     *   <li>Sets the mask in the ActionResult to the dynamic pixel mask
     *   <li>Populates the ActionResult with matches based on the dynamic regions
     * </ul>
     *
     * @param matches the {@link ActionResult} to populate with found matches
     * @param objectCollections list of {@link ObjectCollection} containing patterns to analyze
     */
    public void find(ActionResult matches, List<ObjectCollection> objectCollections) {
        List<Pattern> allPatterns = findFixedPixelMatches.getAllPatterns(objectCollections);
        MatVector matVector = new MatVector();
        allPatterns.forEach(pattern -> matVector.push_back(pattern.getMat()));
        if (matVector.size() < 2) return; // nothing to compare
        Mat fixedPixelMask = findDynamicPixels.getDynamicPixelMask(matVector);
        matches.setMask(fixedPixelMask);
        // since all Patterns have the same fixed pixels, we can use any Pattern for the Match
        // objects
        findFixedPixelMatches.setMatches(fixedPixelMask, matches, allPatterns.get(0));
    }
}
