package io.github.jspinak.brobot.mock;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.image.Image;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchSnapshot;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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

    public Matches getMatches(StateImageObject stateImageObject, Region searchRegion,
                              ActionOptions actionOptions) {
        Report.println("Finding " + stateImageObject.getName() + "in mock");
        List<Match> matchList;
        Optional<MatchSnapshot> randomSnapshot =
                stateImageObject.getMatchHistory().getRandomSnapshot(actionOptions);
        if (randomSnapshot.isEmpty())
            matchList = getProbabilityMatches(stateImageObject, searchRegion, stateImageObject.getImage(),
                    actionOptions);
        else matchList = getHistoryMatches(randomSnapshot.get(), searchRegion);
        Matches matches = new Matches();
        matches.addMatchObjects(stateImageObject, matchList,
                time.getDuration(actionOptions.getAction()).getSeconds());
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

    private List<Match> getProbabilityMatches(StateImageObject stateImageObject, Region searchRegion,
                                              Image image, ActionOptions actionOptions) {
        List<Match> foundMatches = new ArrayList<>();
        if (!isFound(stateImageObject)) return foundMatches;
        int maxMatches = 10;
        if (actionOptions.getMaxMatchesToActOn() > 0) maxMatches = actionOptions.getMaxMatchesToActOn();
        if (actionOptions.getFind() == ActionOptions.Find.FIRST ||
            actionOptions.getFind() == ActionOptions.Find.EACH)
            maxMatches = 1;
        int numberOfMatches = new Random().nextInt(maxMatches) + 1; // at least 1 Match
        for (int i = 0; i < numberOfMatches; i++) {
            Match match = new MatchMaker.Builder().setImage(image).setSearchRegion(searchRegion).build();
            foundMatches.add(match);
        }
        //Report.format(Report.OutputLevel.HIGH,"%s: %s: #matches=%s| \n", "getAllMatches",
        //            image.getImageNames().toString(), foundMatches.size());
        return foundMatches;
    }

    private boolean isFound(StateImageObject stateImageObject) {
        int randomProbability = new Random().nextInt(100);
        int probObj = stateImageObject.getProbabilityExists();
        boolean found = probObj > randomProbability;
        Report.format(Report.OutputLevel.HIGH,"found=%s ", found);
        //if (!found) Report.format(Report.OutputLevel.HIGH,"%d<%d ", probObj, randomProbability);
        return found;
    }
}
