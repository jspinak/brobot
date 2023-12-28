package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImage.StateImage;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.reports.Report;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Mock Matches can be found with 1 of 2 methods:
 *   History: Using the MatchSnapshots associated with individual Images.
 *   Probability: Uses probabilities associated with the Image. This is less accurate than the
 *     History method because it has less data to work with. It is primarily used for Images
 *     that have never been found before, or as a 'quick and dirty' solution to mocking.
 */
@Component
public class MockFind {

    private Time time;

    public MockFind(Time time) {
        this.time = time;
    }

    public Matches getMatches(StateImage stateImage, Region searchRegion,
                              ActionOptions actionOptions) {
        Report.println("Finding " + stateImage.getName() + " in mock");
        List<io.github.jspinak.brobot.datatypes.primitives.match.Match> matchList = new ArrayList<>();
        Optional<MatchSnapshot> randomSnapshot = stateImage.getRandomSnapshot(actionOptions);
        if (randomSnapshot.isPresent()) matchList = getHistoryMatches(randomSnapshot.get(), searchRegion);
        Matches matches = new Matches();
        matches.getMatchList().addAll(matchList);
        return matches;
    }

    private List<Match> getHistoryMatches(MatchSnapshot matchSnapshot, Region searchRegion) {
        List<Match> foundMatches = new ArrayList<>();
        if (imageVanished(matchSnapshot)) return foundMatches; // match has vanished
        if (!imageHasAppeared(matchSnapshot)) return foundMatches; // match hasn't been found yet
        matchSnapshot.getMatchList().forEach(match -> {
            if (searchRegion.contains(match)) foundMatches.add(match); // match is in Region
        });
        return foundMatches;
    }

    private boolean imageVanished(MatchSnapshot matchSnapshot) {
        if (matchSnapshot.getActionOptions().getAction() == ActionOptions.Action.VANISH) {
            return time.expired(ActionOptions.Action.VANISH, matchSnapshot.getDuration());
        }
        return false;
    }

    private boolean imageHasAppeared(MatchSnapshot matchSnapshot) {
        return time.findExpired(matchSnapshot.getDuration());
    }

}
