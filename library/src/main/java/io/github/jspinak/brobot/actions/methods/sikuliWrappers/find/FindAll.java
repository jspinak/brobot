package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.Permissions;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.MatchProofer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.mock.Mock;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds all Matches for all Patterns in the Brobot Image.
 * Used by Find.ALL and Find.BEST.
 * The Find.BEST option finds all Matches and picks the best Match from these Matches.
 */
@Component
public class FindAll {

    private final Mock mock;
    private final Permissions permissions;
    private final FindInFile findInFile;
    private final SelectRegions selectRegions;
    private final MatchProofer matchProofer;

    public FindAll(Mock mock, Permissions permissions, FindInFile findInFile, SelectRegions selectRegions,
                   MatchProofer matchProofer) {
        this.mock = mock;
        this.permissions = permissions;
        this.findInFile = findInFile;
        this.selectRegions = selectRegions;
        this.matchProofer = matchProofer;
    }

    /**
     * Returns MatchObjects for all patterns found on the given scene. Find.FIRST, Find.BEST, Find.EACH, and Find.ALL
     * apply to StateImage objects and not to Pattern objects within individual StateImage objects.
     * @param stateImage the image to search for
     * @param scene the scene to search
     * @return all MatchObjects found
     */
    public List<Match> find(StateImage stateImage, Scene scene, ActionOptions actionOptions) {
        List<Match> matchObjects = new ArrayList<>();
        for (io.github.jspinak.brobot.datatypes.primitives.image.Pattern pattern : stateImage.getPatterns()) {
            // these are unique regions so there won't be any duplicate matches
            List<Region> regions = selectRegions.getRegions(actionOptions, pattern);
            List<Match> matchList = findAll(pattern, scene);
            for (Match match : matchList) {
                if (matchProofer.isInSearchRegions(match, regions)) {
                    matchObjects.add(
                            new Match.Builder()
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

    public List<Match> findWords(Scene scene, ActionOptions actionOptions) {
        List<Match> wordMatches = findInFile.getWordMatches(scene);
        List<Region> regions = selectRegions.getRegions(actionOptions);
        List<Match> matchesInRegion = new ArrayList<>();
        for (Match match : wordMatches) {
            if (matchProofer.isInSearchRegions(match, regions)) {
                matchesInRegion.add(new Match.Builder()
                        .setMatch(match)
                        .setScene(scene)
                        .build());
            }
        }
        return matchesInRegion;
    }

}
