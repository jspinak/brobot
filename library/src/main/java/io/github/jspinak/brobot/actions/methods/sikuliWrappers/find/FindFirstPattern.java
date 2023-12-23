package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.datatypes.primitives.image.ImagePatterns;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.mock.Mock;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
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

    public FindFirstPattern(ImagePatterns imagePatterns, Mock mock, FindPattern findPattern) {
        this.imagePatterns = imagePatterns;
        this.mock = mock;
        this.findPattern = findPattern;
    }

    @Override
    public Matches find(Region region, StateImage stateImage, ActionOptions actionOptions, Scene scene) {
        Matches matches = new Matches();
        if (BrobotSettings.mock && BrobotSettings.screenshots.isEmpty())
            return mock.getMatches(stateImage, region, actionOptions);
        else for (Pattern pattern : imagePatterns.getPatterns(stateImage.getImage(), actionOptions)) {
            Optional<Match> matchOptional = findPattern.findBest(region, pattern, scene);
            if (matchOptional.isPresent()) {
                matches.addMatchObjects(stateImage, Collections.singletonList(matchOptional.get()),
                        Duration.between(matches.getStartTime(), LocalDateTime.now()).toSeconds()); //time.getDuration(actionOptions.getAction()).getSeconds());
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
