package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.MatchProofer;
import io.github.jspinak.brobot.actions.methods.basicactions.find.matchManagement.SelectRegions;
import io.github.jspinak.brobot.actions.methods.mockOrLiveInterface.MockOrLive;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.image.Pattern;
import io.github.jspinak.brobot.datatypes.primitives.location.Location;
import io.github.jspinak.brobot.datatypes.primitives.location.Position;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
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
    private final SelectRegions selectRegions;
    private final MatchProofer matchProofer;
    private final MockOrLive mockOrLive;

    public FindAll(SelectRegions selectRegions, MatchProofer matchProofer, MockOrLive mockOrLive) {
        this.selectRegions = selectRegions;
        this.matchProofer = matchProofer;
        this.mockOrLive = mockOrLive;
    }

    /**
     * Returns Match objects for all patterns found on the given scene. Find.FIRST, Find.BEST, Find.EACH, and Find.ALL
     * apply to StateImage objects and not to Pattern objects within individual StateImage objects, so all matches are returned.
     * @param stateImage the image to search for
     * @param scene the scene to search
     * @return all Match objects found
     *
     * Note: if adding snapshots during execution, they should be added before selected specific Match objects based
     * on regions. Snapshots should not be region-specific to make them more universally applicable.
     */
    public List<Match> find(StateImage stateImage, Image scene, ActionOptions actionOptions) {
        List<Match> allMatchObjects = new ArrayList<>();
        for (Pattern pattern : stateImage.getPatterns()) {
            Position targetPosition = actionOptions.getTargetPosition() == null?
                    pattern.getTargetPosition() : actionOptions.getTargetPosition();
            Location xyOffset = actionOptions.getTargetOffset() == null?
                    pattern.getTargetOffset() : actionOptions.getTargetOffset();
            List<Match> matchList = mockOrLive.findAll(pattern, scene);
            addMatchObjects(allMatchObjects, matchList, pattern, stateImage, actionOptions, scene, targetPosition, xyOffset);
        }
        return allMatchObjects;
    }

    private void addMatchObjects(List<Match> allMatchObjects, List<Match> matchList, Pattern pattern,
                               StateImage stateImage, ActionOptions actionOptions, Image scene,
                               Position target, Location offset) {
        for (Match match : matchList) {
            List<Region> regionsAllowedForMatch = selectRegions.getRegions(actionOptions, stateImage);
            if (matchProofer.isInSearchRegions(match, regionsAllowedForMatch)) {
                Match newMatch = new Match.Builder()
                        .setMatch(match)
                        .setPosition(target)
                        .setOffset(offset)
                        .setSearchImage(pattern.getBImage())
                        .setAnchors(pattern.getAnchors())
                        .setStateObjectData(stateImage)
                        .setScene(scene)
                        .build();
                allMatchObjects.add(newMatch);
            }
        }
    }

    public List<Match> findWords(Image scene, ActionOptions actionOptions) {
        List<Match> wordMatches = mockOrLive.findAllWords(scene);
        List<Region> regions = selectRegions.getRegions(actionOptions);
        List<Match> matchesInRegion = new ArrayList<>();
        for (Match match : wordMatches) {
            if (matchProofer.isInSearchRegions(match, regions)) {
                Match newMatch = new Match.Builder()
                    .setMatch(match)
                    .setScene(scene)
                    .build();
                matchesInRegion.add(newMatch);
            }
        }
        return matchesInRegion;
    }

}
