package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.database.primitives.image.Image;
import io.github.jspinak.brobot.database.primitives.image.ImagePatterns;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.mock.Mock;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

/**
 * Returns Matches for the first Pattern found in a Brobot Image.
 * This Match is the best Match for the corresponding Pattern,
 * but not necessarily for all Patterns.
 */
@Component
public class FindFirstPattern implements FindPatternInterface {

    private final ImagePatterns imagePatterns;
    private final Mock mock;
    private final FindPattern findPattern;
    private final Time time;

    public FindFirstPattern(ImagePatterns imagePatterns, Mock mock, FindPattern findPattern, Time time) {
        this.imagePatterns = imagePatterns;
        this.mock = mock;
        this.findPattern = findPattern;
        this.time = time;
    }

    @Override
    public Matches find(Region region, StateImageObject stateImageObject, Image image,
                        ActionOptions actionOptions) {
        Matches matches = new Matches();
        if (BrobotSettings.mock && BrobotSettings.screenshot.isEmpty())
            return mock.getMatches(stateImageObject, region, actionOptions);
        else for (Pattern pattern : imagePatterns.getPatterns(image, actionOptions)) {
            Optional<Match> matchOptional = findPattern.findBest(region, pattern);
            if (matchOptional.isPresent()) {
                matches.addMatchObjects(stateImageObject, Collections.singletonList(matchOptional.get()),
                        time.getDuration(actionOptions.getAction()).getSeconds());
                break;
            }
        }
        /*
         * Store Snapshots before adjusting the Match. Match objects are adjusted in the
         * FindImage class after getting the results of the Find operation. If they are adjusted here
         * they will be adjusted twice.
         * A failed operation will be stored here without a Match.
         */
        matches.getDanglingSnapshots().addAllMatches(actionOptions, matches);
        return matches;
    }

}
