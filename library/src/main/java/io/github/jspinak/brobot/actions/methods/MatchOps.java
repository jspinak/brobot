package io.github.jspinak.brobot.actions.methods;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.script.Match;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MatchOps {

    private Time time;

    public MatchOps(Time time) {
        this.time = time;
    }

    public void addMatchListToMatches(List<Match> matchList, Matches matches, StateImageObject stateImageObject,
                                      ActionOptions actionOptions) {
        matchList.forEach(match -> {
            try {
                matches.add(new MatchObject(match, stateImageObject, time.getDuration(actionOptions.getAction()).getSeconds()));
            } catch (Exception e) {
                Report.println("Failed to create MatchObject.");
            }
        });
    }

    /**
     * Adds a generic StateImageObject to the MatchObject
     *
     * @param matches the Matches to add to
     * @param matchList the MatchList to add to the Matches
     */
    public void addGenericMatchObjects(List<Match> matchList, Matches matches, ActionOptions actionOptions) {
        for (Match match : matchList) {
            try {
                matches.add(new MatchObject(match, new StateImageObject.Builder().generic(),
                        time.getDuration(actionOptions.getAction()).getSeconds()));
            } catch (Exception e) {
                Report.println("Failed to create MatchObject.");
            }
        }
    }

    /**
     * Matches needs to be sorted before calling this method.
     * The first match in the list is the best match.
     *
     * @param matches the matches to trim
     * @param actionOptions specifies the number of max matches to return
     */
    public void limitNumberOfMatches(Matches matches, ActionOptions actionOptions) {
        if (actionOptions.getMaxMatchesToActOn() <= 0) return;
        if (matches.size() <= actionOptions.getMaxMatchesToActOn()) return;
        List<MatchObject> matchObjects = matches.getMatchObjects();
        matches.setMatchObjects(matchObjects.subList(0, actionOptions.getMaxMatchesToActOn()));
    }

}
