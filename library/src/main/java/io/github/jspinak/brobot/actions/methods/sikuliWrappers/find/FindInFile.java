package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject_;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches a scene for pattern and word matches. Scene objects are created at the beginning of an action and
 * during the action when needed, and are stored in the SceneAnalysisCollection variable in the MatchesInitializer class.
 * The MatchesInitializer class is a central point of reference for the collection of action results.
 */
@Component
public class FindInFile {

    private final FindPattern findPattern;
    private final SelectRegions selectRegions;

    public FindInFile(FindPattern findPattern, SelectRegions selectRegions) {
        this.findPattern = findPattern;
        this.selectRegions = selectRegions;
    }

    public List<Match> findAllInScene(Pattern pattern, Scene scene) {
        Finder f = findPattern.getFinder(scene);
        f.findAll(pattern);
        List<Match> matchList = new ArrayList<>();
        while (f.hasNext()) {
            Match nextMatch = f.next();
            matchList.add(nextMatch);
        }
        f.destroy();
        return matchList;
    }

    /**
     * Returns the Match objects corresponding to words. Match objects have an Image variable, but
     * this is null after an OCR operation. Also, the method Finder.findWords() does not work on a file and
     * cannot be used here.
     * @param scene can be created from a screenshot or file
     * @return a list of Match objects in the specified region
     */
    public List<Match> getWordMatches(Scene scene) {
        return OCR.readWords(scene.getBufferedImageBGR());
    }

}
