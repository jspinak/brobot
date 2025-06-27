package io.github.jspinak.brobot.action.internal.find.scene;

import io.github.jspinak.brobot.model.element.Image;
import io.github.jspinak.brobot.model.element.Pattern;
import io.github.jspinak.brobot.model.element.Scene;
import io.github.jspinak.brobot.model.match.Match;

import org.sikuli.script.Finder;
import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Performs image pattern matching and OCR text detection within captured scenes.
 * <p>
 * This wrapper class provides low-level search functionality for finding visual patterns
 * and text within {@link Scene} objects. It leverages Sikuli's {@link Finder} for
 * pattern matching and {@link OCR} for text recognition. Scene objects represent
 * screenshots or loaded images that serve as the search space.
 * <p>
 * This class is designed to be used by higher-level action classes that handle
 * mock/live execution modes. Direct usage is discouraged as it bypasses the
 * framework's execution control mechanisms.
 * <p>
 * Key responsibilities:
 * <ul>
 * <li>Pattern matching with similarity scoring</li>
 * <li>OCR-based word detection and extraction</li>
 * <li>Automatic management of fixed region updates for patterns</li>
 * <li>Resource cleanup for Finder instances</li>
 * </ul>
 * 
 * @see Scene
 * @see Pattern
 * @see Match
 * @see io.github.jspinak.brobot.action.internal.factory.ActionResultFactory
 */
@Component
public class ScenePatternMatcher {

    /**
     * Creates a Sikuli Finder instance for the given scene image.
     * <p>
     * The method attempts to create the Finder using the scene's BufferedImage
     * if available (preferred for in-memory operations). If the image is empty,
     * it falls back to using the filename, which causes Sikuli to load the
     * image from disk.
     * 
     * @param scene The image to use as the search space for find operations.
     *              Must contain either a valid BufferedImage or a valid filename.
     * @return A new Finder instance configured with the scene's image data.
     *         The caller is responsible for calling {@code destroy()} on the returned Finder.
     */
    private Finder getFinder(Image scene) {
        if (!scene.isEmpty()) return new Finder(scene.getBufferedImage());
        return new Finder(scene.getName());
    }

    /**
     * Finds all occurrences of a pattern within a scene using image matching.
     * <p>
     * This method performs template matching to locate all instances of the given
     * pattern within the scene. It enforces a size constraint where the pattern
     * must be smaller than the scene in both dimensions. If the pattern has a
     * fixed region setting, the method automatically updates the pattern's fixed
     * region to the location of the best match (highest similarity score).
     * <p>
     * The method properly manages Finder resources by calling {@code destroy()}
     * after use to prevent memory leaks.
     * 
     * @param pattern The image pattern to search for. Must be smaller than the scene
     *                in both width and height dimensions.
     * @param scene The scene image to search within. Serves as the search space.
     * @return A list of all matches found, sorted by their appearance in the scene.
     *         Returns an empty list if the pattern is larger than the scene or
     *         no matches are found. Each match includes similarity score and location.
     *         
     * @implNote If the pattern is marked as fixed ({@code pattern.isFixed() == true}),
     *           this method has the side effect of updating the pattern's fixed region
     *           to the location of the best match found.
     */
    public List<Match> findAllInScene(Pattern pattern, Scene scene) {
        if (pattern.w()>scene.getPattern().w() || pattern.h()>scene.getPattern().h()) return new ArrayList<>();
        Finder f = getFinder(scene.getPattern().getImage());
        f.findAll(pattern.sikuli());
        List<Match> matchList = new ArrayList<>();
        while (f.hasNext()) {
            Match nextMatch = new Match.Builder()
                    .setSikuliMatch(f.next())
                    .setName(pattern.getName())
                    .build();
            matchList.add(nextMatch);
        }
        f.destroy();
        if (matchList.isEmpty()) return matchList;
        Match bestMatch = Collections.max(matchList, Comparator.comparingDouble(Match::getScore));
        if (bestMatch != null && pattern.isFixed()) pattern.getSearchRegions().setFixedRegion(bestMatch.getRegion());
        return matchList;
    }

    /**
     * Extracts text regions from a scene using OCR and returns them as Match objects.
     * <p>
     * This method performs optical character recognition on the scene to identify
     * individual words and their locations. Each detected word is wrapped in a
     * {@link Match} object that includes the word's bounding region and text content.
     * The Match objects are automatically associated with the source scene, enabling
     * subsequent image extraction if needed.
     * <p>
     * Each word match is assigned a unique name based on the scene's name (if available)
     * with a "-word{index}" suffix for identification purposes.
     * <p>
     * Technical note: This method uses {@link OCR#readWords} instead of Finder.findWords()
     * because the latter doesn't support file-based operations, while readWords works
     * with BufferedImage objects directly.
     * 
     * @param scene The scene to analyze for text content. Must contain a valid
     *              BufferedImage accessible via {@code scene.getPattern().getBImage()}.
     * @return A list of Match objects, one for each word detected in the scene.
     *         Each match contains the word's location and can access the word's
     *         text through its Sikuli match. Returns an empty list if no words
     *         are detected.
     *         
     * @see OCR#readWords(java.awt.image.BufferedImage)
     * @see Match.Builder#setScene(Scene)
     */
    public List<Match> getWordMatches(Scene scene) {
        List<Match> wordMatches = new ArrayList<>();
        List<org.sikuli.script.Match> sikuliMatches = OCR.readWords(scene.getPattern().getBImage());
        String baseName = scene.getPattern().getName() == null ? "" : scene.getPattern().getName();
        int i=0;
        for (org.sikuli.script.Match match : sikuliMatches) {
            Match m = new Match.Builder()
                    .setName(baseName+"-word"+i)
                    .setSikuliMatch(match)
                    .setScene(scene)
                    .build();
            wordMatches.add(m);
            i++;
        }
        return wordMatches;
    }

}
