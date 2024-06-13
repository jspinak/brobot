package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.sikuli.script.Finder;
import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Searches a scene for pattern and word matches. Scene objects are created at the beginning of an action and
 * during the action when needed, and are stored in the SceneAnalysisCollection variable in the MatchesInitializer class.
 * The MatchesInitializer class is a central point of reference for the collection of action results.
 *
 * As a wrapper function, this class only should be called by the next layer of action methods.
 * The next layer specifies whether an application should run a mock or live function.
 */
@Component
public class FindInScene {

    /**
     * Creates a Finder. Tries first with the scene's BufferedImage, and then with the filename.
     * @param scene the scene to use as the background for find operations.
     * @return the Finder.
     */
    private Finder getFinder(Image scene) {
        if (!scene.isEmpty()) return new Finder(scene.getBufferedImage());
        return new Finder(scene.getName());
    }

    /**
     * The image to search must always be smaller in both dimensions than the scene in which to search.
     * @param pattern the image to search for
     * @param scene the image to search in
     * @return all matches of the pattern in the scene
     */
    public List<Match> findAllInScene(Pattern pattern, Image scene) {
        if (pattern.w()>scene.w() || pattern.h()>scene.h()) return new ArrayList<>();
        Finder f = getFinder(scene);
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
     * Returns the Match objects corresponding to words. Match objects have an Image variable, but
     * this is null after an OCR operation. This Match Builder sets the Image's BufferedImage automatically
     * when given a Scene. The BufferedImage is set again at the end of the Find operation, as the final match region
     * may have shifted or fused.
     *
     * The method Finder.findWords() does not work on a file and cannot be used here.
     * @param scene can be created from a screenshot or file
     * @return a list of Match objects in the specified region
     */
    public List<Match> getWordMatches(Image scene) {
        List<Match> wordMatches = new ArrayList<>();
        OCR.readWords(scene.getBufferedImage()).forEach(match -> {
            Match m = new Match.Builder()
                    .setName("word")
                    .setSikuliMatch(match)
                    .setScene(scene)
                    .build();
            wordMatches.add(m);
        });
        return wordMatches;
    }

}
