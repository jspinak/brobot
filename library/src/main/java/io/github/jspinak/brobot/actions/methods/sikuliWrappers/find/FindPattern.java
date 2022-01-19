package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.database.primitives.match.MatchObject;
import io.github.jspinak.brobot.database.primitives.match.Matches;
import io.github.jspinak.brobot.database.primitives.region.Region;
import io.github.jspinak.brobot.database.state.stateObject.stateImageObject.StateImageObject;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Optional;

/**
 * Contains functions to find one or all Matches given an Image and a Region.
 * Converts Sikuli find operations to Optional<Match> or Matches.
 * Does not mock.
 */
@Component
public class FindPattern {

    private Time time;

    public FindPattern(Time time) {
        this.time = time;
    }

    /**
     * The Sikuli function 'find' finds the best Match for a Sikuli Pattern.
     */
    public Optional<Match> findSikuli(Region region, Pattern pattern) {
        try {
            Match match = region.sikuli().find(pattern);
            return Optional.of(match);
        } catch (FindFailed ignored) {}
        return Optional.empty();
    }

    public Matches findAll(Region region, Pattern pattern, StateImageObject stateImageObject,
                           ActionOptions actionOptions) {
        Matches matches = new Matches();
        Iterator<Match> newMatches;
        try {
            newMatches = region.sikuli().findAll(pattern);
            while (newMatches.hasNext()) {
                try {
                    matches.add(new MatchObject(newMatches.next(), stateImageObject,
                            time.getDuration(actionOptions.getAction()).getSeconds()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FindFailed ignored) {}
        return matches;
    }

}
