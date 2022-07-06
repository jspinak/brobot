package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Finder;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
import java.util.Optional;

/**
 * Contains functions to find one or all Matches given an Image and a Region.
 * Converts Sikuli find operations to {@code Optional<Match>} or Matches.
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
     * @param region the region in which to search
     * @param pattern the pattern to search for
     * @return a Match if found
     */
    public Optional<Match> findBest(Region region, Pattern pattern) {
        if (BrobotSettings.screenshot.isEmpty()) return findLive(region, pattern);
        return findInScreenshot(region, pattern);
    }

    private Finder getFinder() {
        String filename = BrobotSettings.screenshotPath + BrobotSettings.screenshot;
        File file = new File(filename);
        String path = file.getAbsolutePath();
        return new Finder(path);
    }

    private Optional<Match> findInScreenshot(Region region, Pattern pattern) {
        Optional<Match> matchOptional = Optional.empty();
        Finder f = getFinder();
        f.find(pattern);
        if (f.hasNext()) {
            Match nextMatch = f.next();
            if (region.contains(nextMatch)) matchOptional = Optional.of(nextMatch);
        }
        f.destroy();
        return matchOptional;
    }

    private Optional<Match> findLive(Region region, Pattern pattern) {
        try {
            Match match = region.sikuli().find(pattern);
            return Optional.of(match);
        } catch (FindFailed ignored) {}
        return Optional.empty();
    }

    public Matches findAll(Region region, Pattern pattern, StateImageObject stateImageObject,
                           ActionOptions actionOptions) {
        if (BrobotSettings.screenshot.isEmpty())
            return findAllLive(region, pattern, stateImageObject, actionOptions);
        return findAllInScreenshot(region, pattern, stateImageObject, actionOptions);
    }

    private void addMatch(Matches matches, Match match, StateImageObject stateImageObject,
                     ActionOptions actionOptions) {
        try {
            matches.add(new MatchObject(match, stateImageObject,
                    time.getDuration(actionOptions.getAction()).getSeconds()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Matches findAllInScreenshot(Region region, Pattern pattern, StateImageObject stateImageObject,
                                       ActionOptions actionOptions) {
        Finder f = getFinder();
        f.findAll(pattern);
        Matches matches = new Matches();
        while (f.hasNext()) {
            Match nextMatch = f.next();
            if (region.contains(nextMatch))
                addMatch(matches, nextMatch, stateImageObject, actionOptions);
        }
        f.destroy();
        return matches;
    }

    public Matches findAllLive(Region region, Pattern pattern, StateImageObject stateImageObject,
                           ActionOptions actionOptions) {
        Matches matches = new Matches();
        Iterator<Match> newMatches;
        try {
            newMatches = region.sikuli().findAll(pattern);
            while (newMatches.hasNext()) {
                addMatch(matches, newMatches.next(), stateImageObject, actionOptions);
            }
        } catch (FindFailed ignored) {}
        return matches;
    }

}
