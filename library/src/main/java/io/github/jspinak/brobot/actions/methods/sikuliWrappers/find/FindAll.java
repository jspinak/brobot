package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.Permissions;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.ImagePatterns;
import io.github.jspinak.brobot.datatypes.primitives.image.StateImage_;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject_;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.mock.Mock;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds all Matches for all Patterns in the Brobot Image.
 * Used by Find.ALL and Find.BEST.
 * The Find.BEST option finds all Matches and picks the best Match from these Matches.
 */
@Component
public class FindAll implements FindPatternInterface {

    private final ImagePatterns imagePatterns;
    private final Mock mock;
    private final FindPattern findPattern;
    private final Permissions permissions;
    private final FindInFile findInFile;
    private final SelectRegions selectRegions;

    public FindAll(ImagePatterns imagePatterns, Mock mock, FindPattern findPattern,
                   Permissions permissions, FindInFile findInFile, SelectRegions selectRegions) {
        this.imagePatterns = imagePatterns;
        this.mock = mock;
        this.findPattern = findPattern;
        this.permissions = permissions;
        this.findInFile = findInFile;
        this.selectRegions = selectRegions;
    }

    @Override
    public Matches find(Region region, StateImage stateImage, ActionOptions actionOptions, Scene scene) {
        if (BrobotSettings.mock && BrobotSettings.screenshots.isEmpty())
            return mock.getMatches(stateImage, region, actionOptions);
        List<Pattern> patterns = imagePatterns.getPatterns(stateImage.getImage(), actionOptions);
        Matches matches = new Matches();
        for (Pattern pattern : patterns) {
            matches.addAllResults(findPattern.findAll(region, pattern, stateImage, actionOptions, scene));
        }
        /*
         * Store Snapshots before adjusting the Match. This makes it easier to reuse.
         * A failed operation will be stored here without a Match.
         */
        matches.getDanglingSnapshots().addAllMatches(actionOptions, matches);
        return matches;
    }

    /**
     * Returns MatchObjects for all patterns found on the given scene. Find.FIRST, Find.BEST, Find.EACH, and Find.ALL
     * apply to StateImage objects and not to Pattern objects within individual StateImage objects.
     * @param stateImage the image to search for
     * @param scene the scene to search
     * @return all MatchObjects found
     */
    public List<MatchObject_> find(StateImage_ stateImage, Scene scene, ActionOptions actionOptions) {
        List<MatchObject_> matchObjects = new ArrayList<>();
        for (io.github.jspinak.brobot.datatypes.primitives.image.Pattern pattern : stateImage.getPatterns()) {
            // these are unique regions so there won't be any duplicate matches
            List<Region> regions = selectRegions.getRegions(actionOptions, pattern);
            List<Match> matchList = findAll(pattern, scene);
            for (Match match : matchList) {
                if (matchInRegions(match, regions)) {
                    matchObjects.add(
                            new MatchObject_.Builder()
                                    .setMatch(match)
                                    .setPattern(pattern)
                                    .setScene(scene)
                                    .build());
                }
            }
        }
        return matchObjects;
    }

    /**
     * Chooses to mock the action or execute it live.
     * @param pattern the pattern to find
     * @param scene the scene used as the template
     * @return a list of MatchObject
     */
    public List<Match> findAll(io.github.jspinak.brobot.datatypes.primitives.image.Pattern pattern, Scene scene) {
        if (permissions.isMock()) return mock.getMatches(pattern);
        return findInFile.findAllInScene(pattern, scene);
    }

    public List<MatchObject_> findWords(Scene scene, ActionOptions actionOptions) {
        List<Match> wordMatches = findInFile.getWordMatches(scene);
        List<Region> regions = selectRegions.getRegions(actionOptions);
        List<MatchObject_> matchesInRegion = new ArrayList<>();
        for (Match match : wordMatches) {
            if (matchInRegions(match, regions)) {
                matchesInRegion.add(new MatchObject_.Builder()
                        .setMatch(match)
                        .setScene(scene)
                        .build());
            }
        }
        return matchesInRegion;
    }

    public boolean matchInRegions(Match match, List<Region> regions) {
        for (Region r : regions) {
            if (r.contains(match)) return true;
        }
        return false;
    }

}
