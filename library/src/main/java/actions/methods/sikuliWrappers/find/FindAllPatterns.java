package actions.methods.sikuliWrappers.find;

import com.brobot.multimodule.actions.BrobotSettings;
import com.brobot.multimodule.actions.actionOptions.ActionOptions;
import com.brobot.multimodule.database.primitives.image.Image;
import com.brobot.multimodule.database.primitives.image.ImagePatterns;
import com.brobot.multimodule.database.primitives.match.Matches;
import com.brobot.multimodule.database.primitives.region.Region;
import com.brobot.multimodule.database.state.stateObject.stateImageObject.StateImageObject;
import com.brobot.multimodule.mock.Mock;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Finds all Matches for all Patterns in the Brobot Image.
 * Used by Find.ALL and Find.BEST.
 * The Find.BEST option finds all Matches and picks the best Match from these Matches.
 */
@Component
public class FindAllPatterns implements FindPatternInterface {

    private final ImagePatterns imagePatterns;
    private final Mock mock;
    private final FindPattern findPattern;
    private UseDefinedRegion useDefinedRegion;

    public FindAllPatterns(ImagePatterns imagePatterns, Mock mock, FindPattern findPattern,
                           UseDefinedRegion useDefinedRegion) {
        this.imagePatterns = imagePatterns;
        this.mock = mock;
        this.findPattern = findPattern;
        this.useDefinedRegion = useDefinedRegion;
    }

    @Override
    public Matches find(Region region, StateImageObject stateImageObject, Image image,
                        ActionOptions actionOptions) {
        if (BrobotSettings.mock) return mock.getMatches(stateImageObject, region, actionOptions);
        List<Pattern> patterns = imagePatterns.getPatterns(image, actionOptions);
        Matches matches = new Matches();
        for (Pattern pattern : patterns) {
            matches.addAll(findPattern.findAll(region, pattern, stateImageObject, actionOptions));
        }
        /*
         * Store Snapshots before adjusting the Match. This makes it easier to reuse.
         * A failed operation will be stored here without a Match.
         */
        matches.getDanglingSnapshots().addAllMatches(actionOptions, matches);
        return matches;
    }

}
