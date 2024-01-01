package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import org.sikuli.script.Finder;
import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches a scene for pattern and word matches. Scene objects are created at the beginning of an action and
 * during the action when needed, and are stored in the SceneAnalysisCollection variable in the MatchesInitializer class.
 * The MatchesInitializer class is a central point of reference for the collection of action results.
 *
 * As a wrapper function, this class only should be called by the next layer of action methods.
 * The next layer specifies whether an application should run a mock or live function.
 */
@Component
public class FindInFile {
    private Finder getFinder(Scene scene) {
        return new Finder(scene.getAbsolutePath());
    }

    public List<Match> findAllInScene(Pattern pattern, Scene scene) {
        Finder f = getFinder(scene);
        f.findAll(pattern);
        List<Match> matchList = new ArrayList<>();
        while (f.hasNext()) {
            Match nextMatch = new Match(f.next());
            matchList.add(nextMatch);
        }
        f.destroy();
        return matchList;
    }

    /**
     * Returns the Match objects corresponding to words. Match objects have an Image variable, but
     * this is null after an OCR operation. Instead, a Pattern with a Mat is created.
     *
     * The method Finder.findWords() does not work on a file and cannot be used here.
     * @param scene can be created from a screenshot or file
     * @return a list of Match objects in the specified region
     */
    List<Match> getWordMatches(Scene scene) {
        List<Match> wordMatches = new ArrayList<>();
        OCR.readWords(scene.getBufferedImageBGR()).forEach(match -> {
            Match m = new Match.Builder()
                    .setMatch(match)
                    .setScene(scene)
                    .build();
            m.setMatWithScene();
            wordMatches.add(m);
        });
        return wordMatches;
    }

}
