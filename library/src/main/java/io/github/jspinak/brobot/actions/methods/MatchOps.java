package io.github.jspinak.brobot.actions.methods;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.datatypes.primitives.match.Match;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchOps {

    public void addMatchListToMatches(List<Match> matchList, Matches matches) {
        matchList.forEach(matches::add);
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
        List<Match> matchList = matches.getMatchList();
        matches.setMatchList(matchList.subList(0, actionOptions.getMaxMatchesToActOn()));
    }

}
