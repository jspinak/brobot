package io.github.jspinak.brobot.analysis.compare;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.EmptyMatch;
import io.github.jspinak.brobot.model.match.Match;
import io.github.jspinak.brobot.model.state.StateImage;
import io.github.jspinak.brobot.tools.testing.mock.action.ExecutionModeController;

/**
 * Compares images to determine similarity and find matches between patterns.
 *
 * <p>This class provides functionality to compare StateImage objects and their underlying Pattern
 * objects to determine how similar they are. It implements a hierarchical comparison approach that
 * can handle comparisons between single images, multiple patterns within StateImages, and lists of
 * StateImages.
 *
 * <p>The comparison strategy involves finding the smaller image within the larger one, treating the
 * larger image as a scene and the smaller as a search target. This approach is particularly useful
 * for GUI automation where UI elements need to be located within screenshots.
 *
 * @see StateImage
 * @see Pattern
 * @see Match
 * @see SizeComparator
 */
@Component
public class ImageComparer {

    private final ExecutionModeController mockOrLive;
    private final SizeComparator compareSize;

    public ImageComparer(ExecutionModeController mockOrLive, SizeComparator compareSize) {
        this.mockOrLive = mockOrLive;
        this.compareSize = compareSize;
    }

    /**
     * Compares multiple StateImage objects against a single target StateImage.
     *
     * <p>This method iterates through all StateImage objects in the provided list and compares each
     * one with the target image. It returns the comparison result with the highest similarity
     * score.
     *
     * @param imgs List of StateImage objects to compare against the target. Must not be null.
     * @param img2 The target StateImage to compare against. Must not be null.
     * @return The Match object with the highest similarity score, or a NoMatch if no valid
     *     comparisons could be made.
     */
    public Match compare(List<StateImage> imgs, StateImage img2) {
        if (imgs == null || imgs.isEmpty() || img2 == null) {
            return new EmptyMatch();
        }
        Match bestScoringMatch = new EmptyMatch();
        for (StateImage img1 : imgs) {
            Match newMatch = compare(img1, img2);
            if (newMatch.getScore() > bestScoringMatch.getScore()) bestScoringMatch = newMatch;
        }
        return bestScoringMatch;
    }

    /**
     * Compares two StateImage objects by examining all their Pattern combinations.
     *
     * <p>This method performs a comprehensive comparison by testing all Pattern objects from img1
     * against all Pattern objects from img2. The larger image automatically becomes the scene,
     * while the smaller becomes the search target. The returned Match contains details about img2
     * and its similarity score relative to img1.
     *
     * <p>This approach handles StateImages with multiple Pattern variations (e.g., different scales
     * or visual states of the same UI element).
     *
     * @param img1 The base StateImage for comparison. Must not be null.
     * @param img2 The StateImage being compared against img1. Must not be null.
     * @return A Match object containing img2's details and the best similarity score found across
     *     all Pattern combinations, or NoMatch if comparison fails.
     */
    public Match compare(StateImage img1, StateImage img2) {
        if (img1 == null
                || img2 == null
                || img1.getPatterns() == null
                || img2.getPatterns() == null
                || img1.getPatterns().isEmpty()
                || img2.getPatterns().isEmpty()) {
            return new EmptyMatch();
        }
        Match bestScoringMatch = new EmptyMatch();
        for (Pattern p1 : img1.getPatterns()) {
            for (Pattern p2 : img2.getPatterns()) {
                Match newMatch = compare(p1, p2);
                if (newMatch.getScore() > bestScoringMatch.getScore()) bestScoringMatch = newMatch;
            }
        }
        return bestScoringMatch;
    }

    /**
     * Compares two Pattern objects to find the best match of one within the other.
     *
     * <p>This is the core comparison method that implements the actual matching logic. It
     * automatically determines which Pattern is larger and treats it as the scene, while the
     * smaller Pattern becomes the search target. The method uses the configured matcher (mock or
     * live) to find all occurrences of the smaller pattern within the larger one.
     *
     * <p>The comparison is currently optimized for simplicity, returning the best scoring match
     * when multiple instances are found. Future enhancements may include ActionConfig to customize
     * comparison strategies (e.g., weighting multiple matches vs. single high-quality matches).
     *
     * <p>Note: This method handles null inputs gracefully by returning a NoMatch.
     *
     * @param p1 The first Pattern to compare. May be null.
     * @param p2 The second Pattern to compare. May be null.
     * @return A Match object where the smaller Pattern is the searchImage and the larger Pattern is
     *     the Scene. Returns NoMatch if either pattern is null, has null BufferedImage, or if size
     *     comparison fails.
     */
    public Match compare(Pattern p1, Pattern p2) {
        if (p1 == null || p2 == null || p1.getBImage() == null || p2.getBImage() == null)
            return new EmptyMatch();
        List<Pattern> sortedPatterns = compareSize.getEnvelopedFirstOrNone(p1, p2);
        if (sortedPatterns.isEmpty()) return new EmptyMatch();
        Pattern biggestPattern = sortedPatterns.get(1);
        Pattern smallestPattern = sortedPatterns.get(0);
        String name = smallestPattern.getName() + " found in " + biggestPattern.getName();
        Scene scene = new Scene(new Pattern(biggestPattern.getImage()));
        List<Match> matchList = mockOrLive.findAll(smallestPattern, scene);
        if (matchList.isEmpty()) {
            return new EmptyMatch.Builder()
                    .setName(name)
                    .setSearchImage(smallestPattern.getImage())
                    .setScene(scene)
                    .build();
        }
        return Collections.max(matchList, Comparator.comparingDouble(Match::getScore));
    }
}
