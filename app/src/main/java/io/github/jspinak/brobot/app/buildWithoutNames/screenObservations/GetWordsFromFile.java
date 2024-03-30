package io.github.jspinak.brobot.app.buildWithoutNames.screenObservations;

import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.OCR;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class GetWordsFromFile {

    /**
     * SikuliX searches for the null String when using Finder.findWords();
     * Use the method getWordMatchesFromFile instead.
     * This method is still here as a reminder not to use findWords() on a file.
     */
    public List<Match> getWordMatchesFromFileOld(Region usableArea, String path) {
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        List<Match> matches = new ArrayList<>();
        Finder f = new Finder(absolutePath);
        f.findWords();
        while (f.hasNext()) {
            Match match = f.next();
            if (usableArea.contains(match)) matches.add(f.next());
        }
        f.destroy();
        return matches;
    }

    /**
     * Returns the Match objects corresponding to words. Match objects have an Image variable, but
     * this is null after an OCR operation.
     * @param usableArea the area to search
     * @param path the path of the image on file
     * @return a list of Match objects in the specified region
     */
    public List<Match> getWordMatchesFromFile(Region usableArea, String path) {
        File file = new File(path);
        String absolutePath = file.getAbsolutePath();
        List<Match> matches = OCR.readWords(absolutePath);
        List<Match> matchesInRegion = new ArrayList<>();
        for (Match match : matches) {
            if (usableArea.contains(match)) matchesInRegion.add(match);
        }
        return matchesInRegion;
    }
}
