package io.github.jspinak.brobot.actions.methods.basicactions.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.primitives.regionImagePairs.RegionImagePair;
import io.github.jspinak.brobot.datatypes.primitives.regionImagePairs.RegionImagePairs;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.jspinak.brobot.actions.actionOptions.ActionOptions.Find.FIRST;

/**
 * RegionImagePairs are pairs of (Region, Image)
 * The Region in a RegionImagePairs is defined once an Image is found. Subsequent searches for the Image
 * are then limited to its Region.
 * The Find operation for RIPs looks first for RIPs with defined Regions, and then for undefined RIPs.
 */
@Component
public class FindRIP implements FindImageObject {

    private FindImage findImage;

    public FindRIP(FindImage findImage) {
        this.findImage = findImage;
    }

    private Set<RegionImagePair> getDefinedPairs(RegionImagePairs regionImagePairs) {
        return regionImagePairs.getPairs().stream()
                .filter(pair -> pair.getRegion().defined())
                .collect(Collectors.toSet());
    }

    private Set<RegionImagePair> getUndefinedPairs(RegionImagePairs regionImagePairs) {
        return regionImagePairs.getPairs().stream()
                .filter(pair -> !pair.getRegion().defined())
                .collect(Collectors.toSet());
    }

    /**
     * First search the defined pairs, then the undefined pairs.
     * @param actionOptions holds the action configuration.
     * @param stateImage is the StateImage to find.
     * @return a Matches object will all matches found.
     */
    @Override
    public Matches find(ActionOptions actionOptions, StateImage stateImage, Scene scene) {
        Matches matches = new Matches();
        matches.addAllResults(
                findAndDefineRegions(actionOptions, stateImage, stateImage.getRegionImagePairs(),
                        this::getDefinedPairs, scene));
        if (breakCondition(matches, actionOptions)) return matches;
        matches.addAllResults(
                findAndDefineRegions(actionOptions, stateImage, stateImage.getRegionImagePairs(),
                        this::getUndefinedPairs, scene));
        return matches;
    }

    /**
     * For Find.FIRST, the first Image found will return its Matches.
     * For Find.ALL, Find.EACH, and Find.BEST, each RegionImagePair will be searched.
     * When a Pair is found, its Region is defined.
     * @param actionOptions holds the action configuration.
     * @param stateImage is the StateImage containing the RegionImagePairs.
     * @param pairs is the RegionImagePairs to find.
     * @param definedOrUndefined selects pairs in the RegionImagePairs object.
     * @return a Matches object will all matches found.
     */
    public Matches findAndDefineRegions(ActionOptions actionOptions, StateImage stateImage,
                                        RegionImagePairs pairs,
                                        Function<RegionImagePairs, Set<RegionImagePair>> definedOrUndefined, Scene scene) {
        Matches matches = new Matches();
        for (RegionImagePair pair : definedOrUndefined.apply(pairs)) {
            Matches newMatches =
                    findImage.find(actionOptions, stateImage, scene);
            matches.addAllResults(newMatches);
            newMatches.getBestMatch().ifPresent(matchObject -> {
                pair.setRegion(new Region(matchObject.getMatch()));
                pairs.setLastPairFound(pair);
            });
            if (breakCondition(newMatches, actionOptions)) break;
        }
        return matches;
    }

    private boolean breakCondition(Matches matches, ActionOptions actionOptions) {
        if (matches.isEmpty()) return false;
        return actionOptions.getFind() == FIRST;
    }

}
