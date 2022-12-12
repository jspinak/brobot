package io.github.jspinak.brobot.actions.methods.sikuliWrappers.find;

import io.github.jspinak.brobot.actions.BrobotSettings;
import io.github.jspinak.brobot.actions.actionOptions.ActionOptions;
import io.github.jspinak.brobot.actions.methods.basicactions.find.color.pixelAnalysis.Scene;
import io.github.jspinak.brobot.actions.methods.time.Time;
import io.github.jspinak.brobot.datatypes.primitives.match.MatchObject;
import io.github.jspinak.brobot.datatypes.primitives.match.Matches;
import io.github.jspinak.brobot.datatypes.primitives.region.Region;
import io.github.jspinak.brobot.datatypes.state.stateObject.stateImageObject.StateImageObject;
import io.github.jspinak.brobot.reports.Report;
import org.sikuli.basics.Settings;
import org.sikuli.script.*;
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
    public Optional<Match> findBest(Region region, Pattern pattern, Scene scene) {
        if (scene == null || scene.getFilename().isEmpty()) return findLive(region, pattern);
        return findInSceneFromFile(region, pattern, scene);
    }

    private Finder getFinder(Scene scene) {
        String filename = ImagePath.getBundlePath() + "/" + scene.getFilename();
        File file = new File(filename);
        String path = file.getAbsolutePath();
        Report.println(path);
        return new Finder(path);
    }

    private Optional<Match> findInSceneFromFile(Region region, Pattern pattern, Scene scene) {
        Optional<Match> matchOptional = Optional.empty();
        Finder f = getFinder(scene);
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
                           ActionOptions actionOptions, Scene scene) {
        if (BrobotSettings.screenshots.isEmpty())
            return findAllLive(region, pattern, stateImageObject, actionOptions);
        return findAllInSceneFromFile(region, pattern, stateImageObject, actionOptions, scene);
    }

    private void addMatch(Matches matches, Match match, StateImageObject stateImageObject,
                     ActionOptions actionOptions, String sceneName) {
        try {
            MatchObject matchObject = new MatchObject(match, stateImageObject,
                    time.getDuration(actionOptions.getAction()).getSeconds());
            matchObject.setSceneName(sceneName);
            matches.add(matchObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Matches findAllInSceneFromFile(Region region, Pattern pattern, StateImageObject stateImageObject,
                                          ActionOptions actionOptions, Scene scene) {
        Finder f = getFinder(scene);
        f.findAll(pattern);
        Matches matches = new Matches();
        while (f.hasNext()) {
            Match nextMatch = f.next();
            if (region.contains(nextMatch))
                addMatch(matches, nextMatch, stateImageObject, actionOptions, scene.getName());
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
                addMatch(matches, newMatches.next(), stateImageObject, actionOptions, "screenshot0");
            }
        } catch (FindFailed ignored) {}
        return matches;
    }

}
